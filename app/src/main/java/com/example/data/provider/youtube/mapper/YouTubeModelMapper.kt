package com.example.data.provider.youtube.mapper

import com.example.data.provider.youtube.dto.YouTubeChannelDto
import com.example.data.provider.youtube.dto.YouTubePlaylistDto
import com.example.data.provider.youtube.dto.YouTubePlaylistItemDto
import com.example.data.provider.youtube.dto.YouTubeSearchResultDto
import com.example.data.provider.youtube.dto.YouTubeVideoDto
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.PlaybackAvailability
import com.example.domain.model.Playlist
import com.example.domain.model.ProviderContentType
import com.example.domain.model.Track
import java.util.regex.Pattern

/**
 * Maps external YouTube Data API v3 DTOs to provider-neutral MUSE domain models.
 * Completely isolates YouTube-specific formats, ISO-8601 durations, topic categories,
 * and thumbnail hierarchies from the domain layer.
 */
object YouTubeModelMapper {

    const val PROVIDER_ID = "youtube"
    const val PROVIDER_NAME = "YouTube Music"

    fun toTrack(videoDto: YouTubeVideoDto): Track {
        val videoId = videoDto.id ?: ""
        val snippet = videoDto.snippet
        val rawTitle = snippet?.title ?: "Untitled Track"
        val channelTitle = snippet?.channelTitle ?: "Unknown Artist"
        val parsed = parseArtistAndTitle(rawTitle, channelTitle)

        val durationMs = parseIsoDuration(videoDto.contentDetails?.duration)
        val artwork = snippet?.thumbnails?.getBestArtworkUrl() ?: ""
        val genres = extractGenres(videoDto)
        val contentType = determineContentType(rawTitle, videoDto.snippet?.tags)
        val availability = if (videoDto.contentDetails?.regionRestriction?.blocked?.isNotEmpty() == true) {
            PlaybackAvailability.REGION_RESTRICTED
        } else {
            PlaybackAvailability.AVAILABLE
        }

        return Track(
            id = "yt_$videoId",
            title = parsed.second,
            artist = parsed.first,
            artistId = snippet?.channelId?.let { "yt_art_$it" } ?: "",
            album = "Single",
            albumId = "",
            artworkUrl = artwork,
            durationMs = durationMs,
            releaseYear = parseYear(snippet?.publishedAt),
            genres = genres,
            isLiked = false,
            isExplicit = false,
            dominantColorHex = 0xFF8B5CF6,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME,
            source = "YOUTUBE",
            sourceTrackId = videoId,
            contentType = contentType,
            playbackAvailability = availability
        )
    }

    fun toTrack(searchResult: YouTubeSearchResultDto): Track {
        val videoId = searchResult.id?.videoId ?: ""
        val snippet = searchResult.snippet
        val rawTitle = snippet?.title ?: "Untitled Track"
        val channelTitle = snippet?.channelTitle ?: "Unknown Artist"
        val parsed = parseArtistAndTitle(rawTitle, channelTitle)
        val artwork = snippet?.thumbnails?.getBestArtworkUrl() ?: ""

        return Track(
            id = "yt_$videoId",
            title = parsed.second,
            artist = parsed.first,
            artistId = snippet?.channelId?.let { "yt_art_$it" } ?: "",
            album = "Single",
            albumId = "",
            artworkUrl = artwork,
            durationMs = 210000L,
            releaseYear = parseYear(snippet?.publishedAt),
            genres = listOf("Music"),
            isLiked = false,
            isExplicit = false,
            dominantColorHex = 0xFF8B5CF6,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME,
            source = "YOUTUBE",
            sourceTrackId = videoId,
            contentType = ProviderContentType.SONG,
            playbackAvailability = PlaybackAvailability.AVAILABLE
        )
    }

    fun toTrack(playlistItem: YouTubePlaylistItemDto): Track {
        val videoId = playlistItem.snippet?.resourceId?.videoId ?: playlistItem.id ?: ""
        val snippet = playlistItem.snippet
        val rawTitle = snippet?.title ?: "Untitled Track"
        val channelTitle = snippet?.channelTitle ?: "Unknown Artist"
        val parsed = parseArtistAndTitle(rawTitle, channelTitle)
        val artwork = snippet?.thumbnails?.getBestArtworkUrl() ?: ""

        return Track(
            id = "yt_$videoId",
            title = parsed.second,
            artist = parsed.first,
            artistId = snippet?.channelId?.let { "yt_art_$it" } ?: "",
            album = "Playlist Item",
            albumId = "",
            artworkUrl = artwork,
            durationMs = 210000L,
            releaseYear = parseYear(snippet?.publishedAt),
            genres = listOf("Music"),
            isLiked = false,
            isExplicit = false,
            dominantColorHex = 0xFF8B5CF6,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME,
            source = "YOUTUBE",
            sourceTrackId = videoId,
            contentType = ProviderContentType.SONG,
            playbackAvailability = PlaybackAvailability.AVAILABLE
        )
    }

    fun toArtist(channelDto: YouTubeChannelDto): Artist {
        val channelId = channelDto.id ?: ""
        val snippet = channelDto.snippet
        val name = snippet?.title ?: "Unknown Artist"
        val artwork = snippet?.thumbnails?.getBestArtworkUrl() ?: ""
        val subCount = channelDto.statistics?.subscriberCount?.let { formatSubscriberCount(it) } ?: ""

        return Artist(
            id = "yt_art_$channelId",
            name = name,
            imageUrl = artwork,
            monthlyListeners = subCount,
            genres = listOf("Music"),
            isFollowing = false,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME
        )
    }

    fun toPlaylist(playlistDto: YouTubePlaylistDto, tracks: List<Track> = emptyList()): Playlist {
        val playlistId = playlistDto.id ?: ""
        val snippet = playlistDto.snippet
        val title = snippet?.title ?: "YouTube Playlist"
        val description = snippet?.description ?: ""
        val curator = snippet?.channelTitle ?: "YouTube"
        val artwork = snippet?.thumbnails?.getBestArtworkUrl() ?: ""
        val count = playlistDto.contentDetails?.itemCount ?: tracks.size

        return Playlist(
            id = "yt_pl_$playlistId",
            title = title,
            description = description,
            artworkUrl = artwork,
            curator = curator,
            ownerType = "EXTERNAL",
            trackCount = count,
            durationMinutes = (tracks.sumOf { it.durationMs } / 60000L).toInt(),
            isCurated = false,
            isDailyMix = false,
            tracks = tracks,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME
        )
    }

    fun toAlbum(playlistDto: YouTubePlaylistDto, tracks: List<Track> = emptyList()): Album {
        val albumId = playlistDto.id ?: ""
        val snippet = playlistDto.snippet
        val title = snippet?.title ?: "Album"
        val artist = snippet?.channelTitle ?: "Unknown Artist"
        val artwork = snippet?.thumbnails?.getBestArtworkUrl() ?: ""
        val year = parseYear(snippet?.publishedAt)

        return Album(
            id = "yt_alb_$albumId",
            title = title,
            artist = artist,
            artistId = snippet?.channelId?.let { "yt_art_$it" } ?: "",
            artworkUrl = artwork,
            releaseDate = "$year",
            releaseYear = year,
            trackCount = tracks.size.coerceAtLeast(1),
            genres = listOf("Music"),
            tracks = tracks,
            providerId = PROVIDER_ID,
            providerName = PROVIDER_NAME
        )
    }

    /**
     * Parses standard ISO-8601 duration format (e.g. PT3M45S, PT1H2M30S) into milliseconds.
     */
    fun parseIsoDuration(durationStr: String?): Long {
        if (durationStr.isNullOrBlank()) return 210000L
        return try {
            val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matcher = pattern.matcher(durationStr)
            if (matcher.matches()) {
                val hours = matcher.group(1)?.toLongOrNull() ?: 0L
                val minutes = matcher.group(2)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(3)?.toLongOrNull() ?: 0L
                ((hours * 3600) + (minutes * 60) + seconds) * 1000L
            } else {
                210000L
            }
        } catch (e: Exception) {
            210000L
        }
    }

    private fun parseArtistAndTitle(rawTitle: String, fallbackChannel: String): Pair<String, String> {
        val cleanRaw = rawTitle
            .replace(Regex("(?i)\\s*[\\[(](?:official|music|video|audio|lyrics|hd|4k|remastered|visualizer)[^\\])]*[\\])]"), "")
            .trim()

        if (cleanRaw.contains(" - ")) {
            val parts = cleanRaw.split(" - ", limit = 2)
            val artistPart = parts[0].trim()
            val titlePart = parts[1].trim()
            if (artistPart.isNotEmpty() && titlePart.isNotEmpty()) {
                return Pair(artistPart, titlePart)
            }
        }
        return Pair(fallbackChannel.replace(Regex("(?i)\\s*-\\s*topic$"), "").trim(), cleanRaw)
    }

    private fun extractGenres(videoDto: YouTubeVideoDto): List<String> {
        val categories = videoDto.topicDetails?.topicCategories ?: emptyList()
        val genres = categories.mapNotNull { categoryUrl ->
            val lastSegment = categoryUrl.substringAfterLast("/")
            lastSegment
                .replace("_music", "", ignoreCase = true)
                .replace("_", " ")
                .trim()
                .takeIf { it.isNotBlank() }
        }.distinct()

        if (genres.isNotEmpty()) return genres

        val tags = videoDto.snippet?.tags ?: emptyList()
        val tagGenres = tags.filter { tag ->
            val lower = tag.lowercase()
            lower in setOf("pop", "rock", "hip hop", "electronic", "synthwave", "r&b", "indie", "jazz", "classical", "acoustic")
        }.distinct()

        return if (tagGenres.isNotEmpty()) tagGenres else listOf("Music")
    }

    private fun determineContentType(title: String, tags: List<String>?): ProviderContentType {
        val lower = title.lowercase()
        val combined = "$lower ${(tags ?: emptyList()).joinToString(" ").lowercase()}"
        return when {
            combined.contains("live") || combined.contains("concert") || combined.contains("festival") -> ProviderContentType.LIVE_PERFORMANCE
            combined.contains("official video") || combined.contains("music video") -> ProviderContentType.MUSIC_VIDEO
            combined.contains("podcast") || combined.contains("episode") -> ProviderContentType.PODCAST
            else -> ProviderContentType.SONG
        }
    }

    private fun parseYear(publishedAt: String?): Int {
        if (publishedAt.isNullOrBlank()) return 2024
        return publishedAt.take(4).toIntOrNull() ?: 2024
    }

    private fun formatSubscriberCount(countStr: String): String {
        val count = countStr.toLongOrNull() ?: return ""
        return when {
            count >= 1_000_000 -> String.format("%.1fM subscribers", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.0fK subscribers", count / 1000.0)
            else -> "$count subscribers"
        }
    }
}
