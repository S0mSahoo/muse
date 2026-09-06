package com.example.data.repository

import com.example.data.local.LocalDataStore
import com.example.data.remote.SupabaseClient
import com.example.di.AppContainer
import com.example.domain.model.AuthState
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LikedTracksRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikedTrackTableDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("track_id")
    val trackId: String
)

class SupabaseLikedTracksRepositoryImpl(
    private val authRepository: AuthRepository,
    private val localDataStore: LocalDataStore = AppContainer.localDataStore
) : LikedTracksRepository {
    private val client = SupabaseClient.client
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Backing state for Supabase-authenticated user liked track IDs
    private val _supabaseLikedTrackIds = MutableStateFlow<Set<String>>(emptySet())

    // Unified public StateFlow that emits the active session's liked track IDs
    private val _activeLikedTrackIds = MutableStateFlow<Set<String>>(emptySet())

    private var currentAuthenticatedUserId: String? = null
    private var activeRefreshJob: Job? = null
    private val refreshMutex = Mutex()

    init {
        observeAuthState()
        observeGuestLikes()
        observeSupabaseLikes()
    }

    private fun isGuest(): Boolean {
        return authRepository.getGuestSession() != null
    }

    private fun observeAuthState() {
        scope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        val userId = authRepository.getCurrentSession()
                        if (userId != null && !isGuest()) {
                            onUserAuthenticated(userId)
                        } else {
                            onUserSignedOut()
                        }
                    }
                    is AuthState.Guest -> {
                        onGuestMode()
                    }
                    is AuthState.Unauthenticated, is AuthState.Error -> {
                        onUserSignedOut()
                    }
                    is AuthState.Loading -> {
                        // Await state resolution
                    }
                }
            }
        }
    }

    private fun observeGuestLikes() {
        scope.launch {
            localDataStore.likedTrackIds.collect { guestLikes ->
                if (isGuest()) {
                    _activeLikedTrackIds.value = guestLikes
                }
            }
        }
    }

    private fun observeSupabaseLikes() {
        scope.launch {
            _supabaseLikedTrackIds.collect { supabaseLikes ->
                if (!isGuest() && currentAuthenticatedUserId != null) {
                    _activeLikedTrackIds.value = supabaseLikes
                }
            }
        }
    }

    private fun onGuestMode() {
        scope.launch {
            refreshMutex.withLock {
                activeRefreshJob?.cancel()
                activeRefreshJob = null
                currentAuthenticatedUserId = null
                _supabaseLikedTrackIds.value = emptySet()
                _activeLikedTrackIds.value = localDataStore.likedTrackIds.value
                println("LikedTracksRepo: Switched to Guest Mode - using local likes")
            }
        }
    }

    private fun onUserSignedOut() {
        scope.launch {
            refreshMutex.withLock {
                activeRefreshJob?.cancel()
                activeRefreshJob = null
                currentAuthenticatedUserId = null
                _supabaseLikedTrackIds.value = emptySet()
                _activeLikedTrackIds.value = emptySet()
                println("LikedTracksRepo: Cleared liked tracks on sign out")
            }
        }
    }

    private fun onUserAuthenticated(userId: String) {
        scope.launch {
            refreshMutex.withLock {
                if (currentAuthenticatedUserId == userId && activeRefreshJob?.isActive == true) {
                    println("LikedTracksRepo: Refresh already in progress for user $userId")
                    return@withLock
                }

                // If user changed or first time loading for this user
                if (currentAuthenticatedUserId != userId) {
                    println("LikedTracksRepo: User changed from $currentAuthenticatedUserId to $userId. Clearing previous state.")
                    activeRefreshJob?.cancel()
                    _supabaseLikedTrackIds.value = emptySet()
                    _activeLikedTrackIds.value = emptySet()
                    currentAuthenticatedUserId = userId
                }

                activeRefreshJob = scope.launch {
                    fetchLikedTracksFromSupabase(userId)
                }
            }
        }
    }

    private suspend fun fetchLikedTracksFromSupabase(userId: String) {
        try {
            println("LikedTracksRepo: Fetching liked tracks from Supabase for user $userId")
            val list = client.from("liked_tracks")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<LikedTrackTableDto>()

            if (currentAuthenticatedUserId == userId) {
                val trackIds = list.map { it.trackId }.toSet()
                _supabaseLikedTrackIds.value = trackIds
                _activeLikedTrackIds.value = trackIds
                println("LikedTracksRepo: Fetched ${trackIds.size} liked tracks for user $userId")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("LikedTracksRepo: Failed to fetch liked tracks from Supabase for $userId: ${e.message}")
            if (currentAuthenticatedUserId == userId) {
                _supabaseLikedTrackIds.value = emptySet()
                _activeLikedTrackIds.value = emptySet()
            }
        }
    }

    override fun getLikedTrackIds(): StateFlow<Set<String>> = _activeLikedTrackIds.asStateFlow()

    override suspend fun isLiked(trackId: String): Boolean {
        return _activeLikedTrackIds.value.contains(trackId)
    }

    override suspend fun addLike(trackId: String) {
        if (isGuest()) {
            localDataStore.addTrackLike(trackId)
            return
        }
        val userId = authRepository.getCurrentSession() ?: throw IllegalStateException("User not authenticated")
        try {
            client.from("liked_tracks").insert(
                LikedTrackTableDto(userId = userId, trackId = trackId)
            )
            _supabaseLikedTrackIds.value = _supabaseLikedTrackIds.value + trackId
            _activeLikedTrackIds.value = _activeLikedTrackIds.value + trackId
            println("LikedTracksRepo: Added like for track $trackId in Supabase")
        } catch (e: Exception) {
            println("LikedTracksRepo: Error adding like for track $trackId: ${e.message}")
            throw e
        }
    }

    override suspend fun removeLike(trackId: String) {
        if (isGuest()) {
            localDataStore.removeTrackLike(trackId)
            return
        }
        val userId = authRepository.getCurrentSession() ?: throw IllegalStateException("User not authenticated")
        try {
            client.from("liked_tracks").delete {
                filter {
                    eq("user_id", userId)
                    eq("track_id", trackId)
                }
            }
            _supabaseLikedTrackIds.value = _supabaseLikedTrackIds.value - trackId
            _activeLikedTrackIds.value = _activeLikedTrackIds.value - trackId
            println("LikedTracksRepo: Removed like for track $trackId from Supabase")
        } catch (e: Exception) {
            println("LikedTracksRepo: Error removing like for track $trackId: ${e.message}")
            throw e
        }
    }

    override suspend fun toggleLike(trackId: String): Boolean {
        val currentlyLiked = isLiked(trackId)
        if (currentlyLiked) {
            removeLike(trackId)
            return false
        } else {
            addLike(trackId)
            return true
        }
    }
}
