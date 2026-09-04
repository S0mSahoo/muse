package com.example.domain.provider

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track

/**
 * Provider-independent interface for music catalog data.
 * Implementations can connect to local mock data, licensable catalogs, or streaming partner APIs
 * without requiring any UI changes.
 */
interface MusicCatalogProvider {
    val providerId: String
    val providerName: String

    suspend fun search(query: String): List<Track>
    suspend fun getTrack(trackId: String): Track?
    suspend fun getArtist(artistId: String): Artist?
    suspend fun getAlbum(albumId: String): Album?
    suspend fun getPlaylist(playlistId: String): Playlist?
    suspend fun getFeaturedPlaylist(): Playlist
    suspend fun getDailyMixes(): List<DailyMix>
    suspend fun getMadeForYouPlaylists(): List<Playlist>
    suspend fun getRecentlyPlayedTracks(): List<Track>
    suspend fun getFreshDiscoveries(): List<Track>
    suspend fun getRecommendedArtists(): List<Artist>
    suspend fun getRecommendedAlbums(): List<Album>
    suspend fun getBecauseYouListenedTo(): Pair<Artist, List<Track>>
    suspend fun getNewReleases(): List<Album>
    suspend fun getRecommendations(context: RecommendationContext): List<Track>
    suspend fun getAllTracks(): List<Track>
}
