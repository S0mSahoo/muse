package com.example.data.provider

import com.example.data.mock.MockMusicCatalog
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import com.example.domain.provider.MusicCatalogProvider

/**
 * Mock implementation of [MusicCatalogProvider] backing the current UI prototype.
 * Easily swappable for licensed catalog providers without altering domain use cases or UI.
 */
class MockMusicCatalogProvider : MusicCatalogProvider {

    override val providerId: String = "muse_mock"
    override val providerName: String = "MUSE Catalog"

    override suspend fun search(query: String): List<Track> {
        if (query.isBlank()) return MockMusicCatalog.sampleTracks
        val trimmed = query.trim()
        return MockMusicCatalog.sampleTracks.filter { track ->
            track.title.contains(trimmed, ignoreCase = true) ||
                    track.artist.contains(trimmed, ignoreCase = true) ||
                    track.album.contains(trimmed, ignoreCase = true) ||
                    track.genres.any { it.contains(trimmed, ignoreCase = true) }
        }
    }

    override suspend fun getTrack(trackId: String): Track? {
        return MockMusicCatalog.sampleTracks.find { it.id == trackId }
            ?: MockMusicCatalog.freshDiscoveries.find { it.id == trackId }
    }

    override suspend fun getArtist(artistId: String): Artist? {
        return MockMusicCatalog.sampleArtists.find { it.id == artistId }
    }

    override suspend fun getAlbum(albumId: String): Album? {
        return MockMusicCatalog.sampleAlbums.find { it.id == albumId }
    }

    override suspend fun getPlaylist(playlistId: String): Playlist? {
        if (MockMusicCatalog.featuredPlaylist.id == playlistId) {
            return MockMusicCatalog.featuredPlaylist
        }
        return MockMusicCatalog.madeForYouPlaylists.find { it.id == playlistId }
    }

    override suspend fun getFeaturedPlaylist(): Playlist {
        return MockMusicCatalog.featuredPlaylist
    }

    override suspend fun getDailyMixes(): List<DailyMix> {
        return MockMusicCatalog.dailyMixes
    }

    override suspend fun getMadeForYouPlaylists(): List<Playlist> {
        return MockMusicCatalog.madeForYouPlaylists
    }

    override suspend fun getRecentlyPlayedTracks(): List<Track> {
        return MockMusicCatalog.sampleTracks
    }

    override suspend fun getFreshDiscoveries(): List<Track> {
        return MockMusicCatalog.freshDiscoveries
    }

    override suspend fun getRecommendedArtists(): List<Artist> {
        return MockMusicCatalog.sampleArtists
    }

    override suspend fun getRecommendedAlbums(): List<Album> {
        return MockMusicCatalog.sampleAlbums
    }

    override suspend fun getBecauseYouListenedTo(): Pair<Artist, List<Track>> {
        val artist = MockMusicCatalog.sampleArtists.firstOrNull()
            ?: Artist("art_1", "The Midnight")
        val tracks = MockMusicCatalog.sampleTracks.filter { it.artist == artist.name }
        return Pair(artist, tracks)
    }

    override suspend fun getNewReleases(): List<Album> {
        return MockMusicCatalog.sampleAlbums
    }

    override suspend fun getRecommendations(context: RecommendationContext): List<Track> {
        val all = MockMusicCatalog.sampleTracks + MockMusicCatalog.freshDiscoveries
        val mood = context.currentMood?.lowercase()
        return if (mood != null) {
            when (mood) {
                "focus" -> all.filter { it.genres.any { g -> g.contains("Ambient", true) || g.contains("Classical", true) } }
                "energy" -> all.filter { it.genres.any { g -> g.contains("Electro", true) || g.contains("Disco", true) } }
                "night" -> all.filter { it.genres.any { g -> g.contains("Synthwave", true) || g.contains("Chillwave", true) } }
                else -> all
            }.ifEmpty { all }
        } else {
            all.shuffled().take(6)
        }
    }

    override suspend fun getAllTracks(): List<Track> {
        return MockMusicCatalog.sampleTracks + MockMusicCatalog.freshDiscoveries
    }
}
