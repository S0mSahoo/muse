package com.example.data.local

import com.example.domain.model.Playlist
import com.example.domain.model.User
import com.example.domain.model.UserEntitlements
import com.example.domain.model.UserTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory / local storage coordinator for user-specific data.
 * Acts as the single source of truth for liked tracks, recent history, custom playlists, and profile state.
 * Structured so that Room or SharedPreferences can be plugged in seamlessly behind this layer.
 */
class LocalDataStore {

    private val _likedTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val likedTrackIds: StateFlow<Set<String>> = _likedTrackIds.asStateFlow()

    private val _recentlyPlayedTrackIds = MutableStateFlow<List<String>>(emptyList())
    val recentlyPlayedTrackIds: StateFlow<List<String>> = _recentlyPlayedTrackIds.asStateFlow()

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()

    private val _userProfile = MutableStateFlow(
        User(
            id = "",
            name = "",
            handle = "",
            avatarUrl = "",
            subscriptionTier = UserTier.STANDARD
        )
    )
    val userProfile: StateFlow<User> = _userProfile.asStateFlow()

    private val _userEntitlements = MutableStateFlow(
        UserEntitlements(
            subscriptionTier = UserTier.STANDARD,
            canPlayInBackground = false,
            canPlayWhenScreenLocked = false,
            canDownload = false,
            maximumAudioQuality = "Standard",
            adsEnabled = true,
            aiFeaturesLevel = "Standard"
        )
    )
    val userEntitlements: StateFlow<UserEntitlements> = _userEntitlements.asStateFlow()

    fun isTrackLiked(trackId: String): Boolean {
        return _likedTrackIds.value.contains(trackId)
    }

    fun toggleTrackLike(trackId: String): Boolean {
        var isNowLiked = false
        _likedTrackIds.update { current ->
            if (current.contains(trackId)) {
                isNowLiked = false
                current - trackId
            } else {
                isNowLiked = true
                current + trackId
            }
        }
        return isNowLiked
    }

    fun recordRecentTrack(trackId: String) {
        _recentlyPlayedTrackIds.update { current ->
            listOf(trackId) + current.filter { it != trackId }
        }
    }

    fun addCustomPlaylist(playlist: Playlist) {
        _userPlaylists.update { current ->
            listOf(playlist) + current.filter { it.id != playlist.id }
        }
    }

    fun updateUserProfile(name: String, handle: String) {
        _userProfile.update { it.copy(name = name, handle = handle) }
    }

    fun clearProfile() {
        _userProfile.update {
            it.copy(
                id = "",
                name = "",
                handle = "",
                avatarUrl = ""
            )
        }
        _likedTrackIds.value = emptySet()
        _recentlyPlayedTrackIds.value = emptyList()
        _userPlaylists.value = emptyList()
    }

    fun setSubscriptionTier(tier: UserTier) {
        _userProfile.update { it.copy(subscriptionTier = tier) }
        _userEntitlements.update {
            if (tier == UserTier.PREMIUM) {
                it.copy(
                    subscriptionTier = UserTier.PREMIUM,
                    canPlayInBackground = true,
                    canPlayWhenScreenLocked = true,
                    canDownload = true,
                    maximumAudioQuality = "Enhanced",
                    adsEnabled = false,
                    aiFeaturesLevel = "Pro"
                )
            } else {
                it.copy(
                    subscriptionTier = UserTier.STANDARD,
                    canPlayInBackground = false,
                    canPlayWhenScreenLocked = false,
                    canDownload = false,
                    maximumAudioQuality = "Standard",
                    adsEnabled = true,
                    aiFeaturesLevel = "Standard"
                )
            }
        }
    }
}
