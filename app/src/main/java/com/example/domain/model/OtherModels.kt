package com.example.domain.model

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String = "",
    val monthlyListeners: String = "",
    val genres: List<String> = emptyList(),
    val isFollowing: Boolean = false,
    val providerId: String = "muse_mock",
    val providerName: String = "MUSE Catalog"
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val artworkUrl: String = "",
    val releaseDate: String = "2024",
    val releaseYear: Int = 2024,
    val trackCount: Int = 10,
    val genres: List<String> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val providerId: String = "muse_mock",
    val providerName: String = "MUSE Catalog"
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val artworkUrl: String = "",
    val curator: String = "MUSE Curators",
    val ownerType: String = "CURATED",
    val trackCount: Int = 20,
    val durationMinutes: Int = 0,
    val isCurated: Boolean = true,
    val isDailyMix: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val gradientColors: List<Long> = listOf(0xFF6366F1, 0xFF8B5CF6),
    val providerId: String = "muse_mock",
    val providerName: String = "MUSE Catalog"
)

data class DailyMix(
    val id: String,
    val mixNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val artworkUrl: String = "",
    val artistsPreview: List<String> = emptyList(),
    val dominantColorHex: Long = 0xFF8B5CF6,
    val tracks: List<Track> = emptyList(),
    val providerId: String = "muse_mock",
    val providerName: String = "MUSE Catalog"
)

enum class UserTier {
    STANDARD,
    PREMIUM
}

typealias SubscriptionTier = UserTier

data class User(
    val id: String = "user_som_01",
    val name: String = "Som",
    val handle: String = "@som",
    val avatarUrl: String = "",
    val subscriptionTier: UserTier = UserTier.STANDARD
)

data class UserEntitlements(
    val subscriptionTier: UserTier = UserTier.STANDARD,
    val canPlayInBackground: Boolean = false,
    val canPlayWhenScreenLocked: Boolean = false,
    val canDownload: Boolean = false,
    val maximumAudioQuality: String = "Standard",
    val adsEnabled: Boolean = true,
    val aiFeaturesLevel: String = "Standard"
)

data class DailyAdState(
    val date: String = "",
    val adShown: Boolean = false,
    val adCompleted: Boolean = false
)

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 1L,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = 0
) {
    val currentQueueIndex: Int get() = queueIndex
    val positionMs: Long get() = progressMs
}

enum class ListeningEventType {
    PLAY_STARTED,
    PLAY_PAUSED,
    PLAY_RESUMED,
    PLAY_COMPLETED,
    SKIPPED,
    LIKED,
    UNLIKED
}

data class ListeningEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val trackId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val playbackPositionMs: Long = 0L,
    val durationListenedMs: Long = 0L,
    val completionRatio: Float = 0f,
    val eventType: ListeningEventType
)

data class ListeningStats(
    val totalTracksPlayed: Int = 0,
    val totalDurationMs: Long = 0L,
    val topGenres: List<String> = emptyList(),
    val topArtists: List<String> = emptyList()
)

data class RecommendationContext(
    val currentMood: String? = null,
    val recentlyPlayedTracks: List<Track> = emptyList(),
    val likedTrackIds: Set<String> = emptySet(),
    val favoriteArtists: List<String> = emptyList(),
    val favoriteGenres: List<String> = emptyList(),
    val listeningTimePatterns: Map<String, Int> = emptyMap(),
    val skipCount: Int = 0,
    val completionCount: Int = 0,
    val discoveryPreference: String = "BALANCED"
)

data class Genre(val id: String, val name: String, val colorHex: Long = 0xFF8B5CF6)
data class Mood(val id: String, val name: String, val icon: String = "")
data class MusicSource(val id: String, val name: String)

sealed class HomeSectionItem {
    data class Featured(val playlist: Playlist) : HomeSectionItem()
    data class DailyMixes(val mixes: List<DailyMix>) : HomeSectionItem()
    data class Playlists(val title: String, val subtitle: String? = null, val playlists: List<Playlist>) : HomeSectionItem()
    data class RecentTracks(val title: String, val tracks: List<Track>) : HomeSectionItem()
    data class Discoveries(val title: String, val subtitle: String? = null, val tracks: List<Track>) : HomeSectionItem()
    data class Artists(val title: String, val subtitle: String? = null, val artists: List<Artist>) : HomeSectionItem()
    data class Albums(val title: String, val albums: List<Album>) : HomeSectionItem()
}

