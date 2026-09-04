package com.example.data.repository

import com.example.data.local.LocalDataStore
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.domain.provider.MusicCatalogProvider
import com.example.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class MockMusicRepositoryImpl(
    private val musicCatalogProvider: MusicCatalogProvider,
    private val localDataStore: LocalDataStore
) : MusicRepository {

    private fun mapWithLikes(track: Track, likedIds: Set<String>): Track {
        return track.copy(isLiked = likedIds.contains(track.id))
    }

    private fun mapTracksWithLikes(tracks: List<Track>, likedIds: Set<String>): List<Track> {
        return tracks.map { mapWithLikes(it, likedIds) }
    }

    override fun getFeaturedPlaylist(): Flow<Playlist> = localDataStore.likedTrackIds.map { likedIds ->
        val playlist = musicCatalogProvider.getFeaturedPlaylist()
        playlist.copy(tracks = mapTracksWithLikes(playlist.tracks, likedIds))
    }

    override fun getDailyMixes(): Flow<List<DailyMix>> = localDataStore.likedTrackIds.map { likedIds ->
        val mixes = musicCatalogProvider.getDailyMixes()
        mixes.map { mix ->
            mix.copy(tracks = mapTracksWithLikes(mix.tracks, likedIds))
        }
    }

    override fun getMadeForYouPlaylists(): Flow<List<Playlist>> = flow {
        emit(musicCatalogProvider.getMadeForYouPlaylists())
    }

    override fun getRecentlyPlayedTracks(): Flow<List<Track>> = localDataStore.likedTrackIds.map { likedIds ->
        val tracks = musicCatalogProvider.getRecentlyPlayedTracks()
        mapTracksWithLikes(tracks, likedIds)
    }

    override fun getFreshDiscoveries(): Flow<List<Track>> = localDataStore.likedTrackIds.map { likedIds ->
        val tracks = musicCatalogProvider.getFreshDiscoveries()
        mapTracksWithLikes(tracks, likedIds)
    }

    override fun getRecommendedArtists(): Flow<List<Artist>> = flow {
        emit(musicCatalogProvider.getRecommendedArtists())
    }

    override fun getRecommendedAlbums(): Flow<List<Album>> = flow {
        emit(musicCatalogProvider.getRecommendedAlbums())
    }

    override fun getBecauseYouListenedTo(): Flow<Pair<Artist, List<Track>>> = localDataStore.likedTrackIds.map { likedIds ->
        val pair = musicCatalogProvider.getBecauseYouListenedTo()
        Pair(pair.first, mapTracksWithLikes(pair.second, likedIds))
    }

    override fun getNewReleases(): Flow<List<Album>> = flow {
        emit(musicCatalogProvider.getNewReleases())
    }

    override fun getLikedTracks(): Flow<List<Track>> = localDataStore.likedTrackIds.map { likedIds ->
        val all = musicCatalogProvider.getAllTracks()
        all.filter { likedIds.contains(it.id) }.map { it.copy(isLiked = true) }
    }

    override fun getLikedTrackIds(): StateFlow<Set<String>> {
        return localDataStore.likedTrackIds
    }

    override fun toggleTrackLike(trackId: String): Flow<Boolean> = flow {
        val result = localDataStore.toggleTrackLike(trackId)
        emit(result)
    }

    override suspend fun toggleLike(trackId: String): Boolean {
        return localDataStore.toggleTrackLike(trackId)
    }

    override suspend fun search(query: String): List<Track> {
        val tracks = musicCatalogProvider.search(query)
        val likedIds = localDataStore.likedTrackIds.value
        return mapTracksWithLikes(tracks, likedIds)
    }

    override suspend fun getTrack(trackId: String): Track? {
        val track = musicCatalogProvider.getTrack(trackId) ?: return null
        return mapWithLikes(track, localDataStore.likedTrackIds.value)
    }

    override suspend fun getPlaylist(playlistId: String): Playlist? {
        val playlist = musicCatalogProvider.getPlaylist(playlistId) ?: return null
        return playlist.copy(
            tracks = mapTracksWithLikes(playlist.tracks, localDataStore.likedTrackIds.value)
        )
    }

    override suspend fun getArtist(artistId: String): Artist? {
        return musicCatalogProvider.getArtist(artistId)
    }

    override suspend fun getAlbum(albumId: String): Album? {
        return musicCatalogProvider.getAlbum(albumId)
    }
}

