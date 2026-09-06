package com.example.data.provider.online.mapper

import com.example.data.provider.online.dto.ITunesTrackDto
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.PlaybackAvailability
import com.example.domain.model.Playlist
import com.example.domain.model.ProviderContentType
import com.example.domain.model.Track

object OnlineMusicModelMapper {

    const val PROVIDER_ID = "itunes"
    const val PROVIDER_NAME = "Global Music Network"

    fun toTrack(dto: ITunesTrackDto): Track {
        val trackId = dto.trackId?.toString() ?: (dto.artistName.hashCode().toString() + "_" + dto.trackName.hashCode().toString())
        val artistName = dto.artistName ?: "Unknown Artist"
        val trackTitle = dto.trackName ?: "Untitled Track"
        val albumName = dto.collectionName ?: "Single"
        val artwork = getHighResArtworkUrl(dto.artworkUrl100 ?: dto.artworkUrl60 ?: dto.artworkUrl30)
        val durationMs = dto.trackTimeMillis ?: 210000L
        val releaseYear = parseYear(dto.releaseDate)
        val genre = dto.primaryGenreName ?: "Music"
        val isExplicit = dto.trackExplicitness.equals("explicit", ignoreCase = true)

        return Track(
            id = "net_$trackId",
            title = trackTitle,
            artist = artistName,
            artistId = dto.artistId?.let { "net_art_$it" } ?: "",
            album = albumName,
            albumId = dto.collectionId?.let { "net_alb_$it" } ?: "",
            artworkUrl = artwork,
            audioUrl = dto.previewUrl ?: "",
            durationMs = durationMs,
            releaseYear = releaseYear,
            genres = listOf(genre),
            isLiked = false,
            isExplicit = isExplicit,
            dominantColorHex = 0xFF8B5CF6,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME,
            source = "GLOBAL_MUSIC",
            sourceTrackId = trackId,
            contentType = ProviderContentType.SONG,
            playbackAvailability = PlaybackAvailability.AVAILABLE
        )
    }

    fun toArtist(dto: ITunesTrackDto): Artist {
        val artistId = dto.artistId?.toString() ?: dto.artistName.hashCode().toString()
        val name = dto.artistName ?: "Unknown Artist"
        val artwork = getHighResArtworkUrl(dto.artworkUrl100 ?: dto.artworkUrl60)
        val genre = dto.primaryGenreName ?: "Music"

        return Artist(
            id = "net_art_$artistId",
            name = name,
            imageUrl = artwork,
            monthlyListeners = "Global Artist",
            genres = listOf(genre),
            isFollowing = false,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME
        )
    }

    fun toAlbum(dto: ITunesTrackDto, tracks: List<Track> = emptyList()): Album {
        val collectionId = dto.collectionId?.toString() ?: dto.collectionName.hashCode().toString()
        val title = dto.collectionName ?: "Album"
        val artist = dto.artistName ?: "Unknown Artist"
        val artwork = getHighResArtworkUrl(dto.artworkUrl100 ?: dto.artworkUrl60)
        val year = parseYear(dto.releaseDate)
        val genre = dto.primaryGenreName ?: "Music"

        return Album(
            id = "net_alb_$collectionId",
            title = title,
            artist = artist,
            artistId = dto.artistId?.let { "net_art_$it" } ?: "",
            artworkUrl = artwork,
            releaseDate = "$year",
            releaseYear = year,
            trackCount = tracks.size.coerceAtLeast(1),
            genres = listOf(genre),
            tracks = tracks,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME
        )
    }

    private fun getHighResArtworkUrl(originalUrl: String?): String {
        if (originalUrl.isNullOrBlank()) return ""
        return originalUrl
            .replace("100x100bb.jpg", "600x600bb.jpg")
            .replace("60x60bb.jpg", "600x600bb.jpg")
            .replace("30x30bb.jpg", "600x600bb.jpg")
            .replace("100x100bb.png", "600x600bb.png")
    }

    private fun parseYear(releaseDate: String?): Int {
        if (releaseDate.isNullOrBlank()) return 2024
        return releaseDate.take(4).toIntOrNull() ?: 2024
    }
}
