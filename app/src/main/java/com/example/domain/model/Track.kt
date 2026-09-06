package com.example.domain.model

enum class ProviderContentType {
    SONG,
    MUSIC_VIDEO,
    PODCAST,
    AUDIO_TRACK,
    OFFICIAL_RELEASE,
    LIVE_PERFORMANCE
}

enum class PlaybackAvailability {
    AVAILABLE,
    REGION_RESTRICTED,
    PREVIEW_ONLY,
    UNAVAILABLE
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val album: String = "",
    val albumId: String = "",
    val artworkUrl: String = "",
    val durationMs: Long = 210000L,
    val releaseYear: Int = 2024,
    val genres: List<String> = emptyList(),
    val isLiked: Boolean = false,
    val isExplicit: Boolean = false,
    val dominantColorHex: Long = 0xFF8B5CF6,
    val providerId: String = "muse_mock",
    val providerName: String = "MUSE Catalog",
    val source: String = "MUSE_CATALOG",
    val sourceTrackId: String = "",
    val contentType: ProviderContentType = ProviderContentType.SONG,
    val playbackAvailability: PlaybackAvailability = PlaybackAvailability.AVAILABLE
)
