package com.example.domain.repository

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MusicRepository {
    fun getFeaturedPlaylist(): Flow<Playlist>
    fun getDailyMixes(): Flow<List<DailyMix>>
    fun getMadeForYouPlaylists(): Flow<List<Playlist>>
    fun getRecentlyPlayedTracks(): Flow<List<Track>>
    fun getFreshDiscoveries(): Flow<List<Track>>
    fun getRecommendedArtists(): Flow<List<Artist>>
    fun getRecommendedAlbums(): Flow<List<Album>>
    fun getBecauseYouListenedTo(): Flow<Pair<Artist, List<Track>>>
    fun getNewReleases(): Flow<List<Album>>
    fun getLikedTracks(): Flow<List<Track>>
    fun getLikedTrackIds(): StateFlow<Set<String>>
    fun toggleTrackLike(trackId: String): Flow<Boolean>
    suspend fun toggleLike(trackId: String): Boolean
    suspend fun search(query: String): List<Track>
    suspend fun getTrack(trackId: String): Track?
    suspend fun getPlaylist(playlistId: String): Playlist?
    suspend fun getArtist(artistId: String): Artist?
    suspend fun getAlbum(albumId: String): Album?
}

