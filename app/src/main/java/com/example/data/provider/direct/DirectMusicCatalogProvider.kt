package com.example.data.provider.direct

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import com.example.domain.provider.MusicCatalogProvider

/**
 * Contract and reference implementation for a future direct artist-uploaded or licensed lossless/FLAC
 * master catalog provider.
 * Coexists seamlessly with [YouTubeMusicCatalogProvider] and [MockMusicCatalogProvider] via [MusicCatalogProvider].
 */
class DirectMusicCatalogProvider(
    override val providerId: String = "muse_direct",
    override val providerName: String = "MUSE Direct Masters"
) : MusicCatalogProvider {

    override suspend fun search(query: String): List<Track> = emptyList()
    override suspend fun getTrack(trackId: String): Track? = null
    override suspend fun getArtist(artistId: String): Artist? = null
    override suspend fun getAlbum(albumId: String): Album? = null
    override suspend fun getPlaylist(playlistId: String): Playlist? = null
    override suspend fun getFeaturedPlaylist(): Playlist = Playlist(
        id = "direct_featured",
        title = "Direct Artist Releases",
        providerId = providerId,
        providerName = providerName
    )
    override suspend fun getDailyMixes(): List<DailyMix> = emptyList()
    override suspend fun getMadeForYouPlaylists(): List<Playlist> = emptyList()
    override suspend fun getRecentlyPlayedTracks(): List<Track> = emptyList()
    override suspend fun getFreshDiscoveries(): List<Track> = emptyList()
    override suspend fun getRecommendedArtists(): List<Artist> = emptyList()
    override suspend fun getRecommendedAlbums(): List<Album> = emptyList()
    override suspend fun getBecauseYouListenedTo(): Pair<Artist, List<Track>> = Pair(
        Artist(id = "direct_art_spotlight", name = "MUSE Spotlight", providerId = providerId, providerName = providerName),
        emptyList()
    )
    override suspend fun getNewReleases(): List<Album> = emptyList()
    override suspend fun getRecommendations(context: RecommendationContext): List<Track> = emptyList()
    override suspend fun getAllTracks(): List<Track> = emptyList()
}
