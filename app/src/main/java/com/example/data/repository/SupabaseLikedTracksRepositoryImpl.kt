package com.example.data.repository

import com.example.data.remote.SupabaseClient
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LikedTracksRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val authRepository: AuthRepository
) : LikedTracksRepository {
    private val client = SupabaseClient.client
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _likedTrackIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        refreshLikedTracks()
    }

    private fun refreshLikedTracks() {
        scope.launch {
            try {
                val userId = authRepository.getCurrentSession()
                if (userId != null) {
                    val list = client.from("liked_tracks")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeList<LikedTrackTableDto>()
                    _likedTrackIds.value = list.map { it.trackId }.toSet()
                    println("LikedTracksRepo: Fetched ${list.size} liked tracks for user $userId")
                } else {
                    _likedTrackIds.value = emptySet()
                }
            } catch (e: Exception) {
                println("LikedTracksRepo: Error fetching liked tracks: ${e.message}")
            }
        }
    }

    override fun getLikedTrackIds(): StateFlow<Set<String>> = _likedTrackIds.asStateFlow()

    override suspend fun isLiked(trackId: String): Boolean {
        return _likedTrackIds.value.contains(trackId)
    }

    override suspend fun addLike(trackId: String) {
        val userId = authRepository.getCurrentSession() ?: throw IllegalStateException("User not authenticated")
        try {
            client.from("liked_tracks").insert(
                LikedTrackTableDto(userId = userId, trackId = trackId)
            )
            _likedTrackIds.value = _likedTrackIds.value + trackId
            println("LikedTracksRepo: Added like for track $trackId")
        } catch (e: Exception) {
            println("LikedTracksRepo: Error adding like for track $trackId: ${e.message}")
            throw e
        }
    }

    override suspend fun removeLike(trackId: String) {
        val userId = authRepository.getCurrentSession() ?: throw IllegalStateException("User not authenticated")
        try {
            client.from("liked_tracks").delete {
                filter {
                    eq("user_id", userId)
                    eq("track_id", trackId)
                }
            }
            _likedTrackIds.value = _likedTrackIds.value - trackId
            println("LikedTracksRepo: Removed like for track $trackId")
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
