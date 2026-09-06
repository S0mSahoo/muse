package com.example.data.provider

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import com.example.domain.provider.MusicCatalogProvider

/**
 * Composite provider that routes lookup queries to the appropriate underlying provider
 * based on track/artist/album IDs (e.g. `yt_` -> YouTube, `direct_` -> Direct Master, other -> Mock),
 * and can aggregate or prioritize search across providers.
 */
class CompositeMusicCatalogProvider(
    private val primaryProvider: MusicCatalogProvider,
    private val providers: Map<String, MusicCatalogProvider> = emptyMap(),
    private val fallbackProvider: MusicCatalogProvider? = null
) : MusicCatalogProvider {

    override val providerId: String = "muse_composite"
    override val providerName: String = "MUSE Federated Catalog"

    private fun resolveProviderForId(id: String): MusicCatalogProvider {
        return when {
            id.startsWith("yt_") -> providers["youtube"] ?: primaryProvider
            id.startsWith("direct_") -> providers["muse_direct"] ?: primaryProvider
            else -> providers[id] ?: primaryProvider
        }
    }

    override suspend fun search(query: String): List<Track> {
        val primaryResults = primaryProvider.search(query)
        if (primaryResults.isNotEmpty()) return primaryResults
        for (provider in providers.values) {
            val res = provider.search(query)
            if (res.isNotEmpty()) return res
        }
        return fallbackProvider?.search(query) ?: emptyList()
    }

    override suspend fun getTrack(trackId: String): Track? {
        val provider = resolveProviderForId(trackId)
        val track = provider.getTrack(trackId)
        if (track != null) return track
        return fallbackProvider?.getTrack(trackId)
    }

    override suspend fun getArtist(artistId: String): Artist? {
        val provider = resolveProviderForId(artistId)
        return provider.getArtist(artistId) ?: fallbackProvider?.getArtist(artistId)
    }

    override suspend fun getAlbum(albumId: String): Album? {
        val provider = resolveProviderForId(albumId)
        return provider.getAlbum(albumId) ?: fallbackProvider?.getAlbum(albumId)
    }

    override suspend fun getPlaylist(playlistId: String): Playlist? {
        val provider = resolveProviderForId(playlistId)
        return provider.getPlaylist(playlistId) ?: fallbackProvider?.getPlaylist(playlistId)
    }

    override suspend fun getFeaturedPlaylist(): Playlist {
        return primaryProvider.getFeaturedPlaylist()
    }

    override suspend fun getDailyMixes(): List<DailyMix> {
        val mixes = primaryProvider.getDailyMixes()
        if (mixes.isNotEmpty()) return mixes
        return fallbackProvider?.getDailyMixes() ?: emptyList()
    }

    override suspend fun getMadeForYouPlaylists(): List<Playlist> {
        val playlists = primaryProvider.getMadeForYouPlaylists()
        if (playlists.isNotEmpty()) return playlists
        return fallbackProvider?.getMadeForYouPlaylists() ?: emptyList()
    }

    override suspend fun getRecentlyPlayedTracks(): List<Track> {
        return primaryProvider.getRecentlyPlayedTracks()
    }

    override suspend fun getFreshDiscoveries(): List<Track> {
        val fresh = primaryProvider.getFreshDiscoveries()
        if (fresh.isNotEmpty()) return fresh
        return fallbackProvider?.getFreshDiscoveries() ?: emptyList()
    }

    override suspend fun getRecommendedArtists(): List<Artist> {
        val artists = primaryProvider.getRecommendedArtists()
        if (artists.isNotEmpty()) return artists
        return fallbackProvider?.getRecommendedArtists() ?: emptyList()
    }

    override suspend fun getRecommendedAlbums(): List<Album> {
        val albums = primaryProvider.getRecommendedAlbums()
        if (albums.isNotEmpty()) return albums
        return fallbackProvider?.getRecommendedAlbums() ?: emptyList()
    }

    override suspend fun getBecauseYouListenedTo(): Pair<Artist, List<Track>> {
        val pair = primaryProvider.getBecauseYouListenedTo()
        if (pair.second.isNotEmpty()) return pair
        return fallbackProvider?.getBecauseYouListenedTo() ?: pair
    }

    override suspend fun getNewReleases(): List<Album> {
        val releases = primaryProvider.getNewReleases()
        if (releases.isNotEmpty()) return releases
        return fallbackProvider?.getNewReleases() ?: emptyList()
    }

    override suspend fun getRecommendations(context: RecommendationContext): List<Track> {
        val recs = primaryProvider.getRecommendations(context)
        if (recs.isNotEmpty()) return recs
        return fallbackProvider?.getRecommendations(context) ?: emptyList()
    }

    override suspend fun getAllTracks(): List<Track> {
        return primaryProvider.getAllTracks()
    }
}
