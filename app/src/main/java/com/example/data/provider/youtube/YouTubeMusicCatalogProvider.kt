package com.example.data.provider.youtube

import com.example.BuildConfig
import com.example.data.provider.youtube.api.YouTubeApiService
import com.example.data.provider.youtube.mapper.YouTubeModelMapper
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import com.example.domain.provider.MusicCatalogProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Concrete implementation of [MusicCatalogProvider] backed by the YouTube Data API v3.
 * Adheres strictly to provider-neutral domain models via [YouTubeModelMapper].
 * Never hardcodes API credentials and safely checks runtime build configuration.
 */
class YouTubeMusicCatalogProvider(
    private val apiKeyProvider: () -> String = { BuildConfig.YOUTUBE_API_KEY },
    private val apiService: YouTubeApiService = createDefaultApiService()
) : MusicCatalogProvider {

    override val providerId: String = YouTubeModelMapper.PROVIDER_ID
    override val providerName: String = YouTubeModelMapper.PROVIDER_NAME

    private fun getApiKey(): String {
        val key = apiKeyProvider()
        return if (key == "YOUR_YOUTUBE_API_KEY" || key.isBlank()) "" else key
    }

    private fun isConfigured(): Boolean = getApiKey().isNotBlank()

    override suspend fun search(query: String): List<Track> {
        if (!isConfigured() || query.isBlank()) return emptyList()
        return try {
            val response = apiService.search(
                query = query,
                type = "video",
                maxResults = 25,
                apiKey = getApiKey()
            )
            val videoIds = response.items.mapNotNull { it.id?.videoId }.filter { it.isNotBlank() }
            if (videoIds.isNotEmpty()) {
                val videosResponse = apiService.getVideosById(
                    ids = videoIds.joinToString(","),
                    apiKey = getApiKey()
                )
                videosResponse.items.map { YouTubeModelMapper.toTrack(it) }
            } else {
                response.items.map { YouTubeModelMapper.toTrack(it) }
            }
        } catch (e: Exception) {
            println("YouTubeProvider: Error executing search for '$query': ${e.message}")
            emptyList()
        }
    }

    override suspend fun getTrack(trackId: String): Track? {
        if (!isConfigured()) return null
        val cleanId = trackId.removePrefix("yt_")
        return try {
            val response = apiService.getVideosById(
                ids = cleanId,
                apiKey = getApiKey()
            )
            response.items.firstOrNull()?.let { YouTubeModelMapper.toTrack(it) }
        } catch (e: Exception) {
            println("YouTubeProvider: Error fetching track '$trackId': ${e.message}")
            null
        }
    }

    override suspend fun getArtist(artistId: String): Artist? {
        if (!isConfigured()) return null
        val cleanId = artistId.removePrefix("yt_art_")
        return try {
            val response = apiService.getChannelById(
                id = cleanId,
                apiKey = getApiKey()
            )
            response.items.firstOrNull()?.let { YouTubeModelMapper.toArtist(it) }
        } catch (e: Exception) {
            println("YouTubeProvider: Error fetching artist '$artistId': ${e.message}")
            null
        }
    }

    override suspend fun getAlbum(albumId: String): Album? {
        if (!isConfigured()) return null
        val cleanId = albumId.removePrefix("yt_alb_").removePrefix("yt_pl_")
        return try {
            val playlistResponse = apiService.getPlaylists(
                id = cleanId,
                apiKey = getApiKey()
            )
            val playlistDto = playlistResponse.items.firstOrNull() ?: return null
            val itemsResponse = apiService.getPlaylistItems(
                playlistId = cleanId,
                apiKey = getApiKey()
            )
            val tracks = itemsResponse.items.map { YouTubeModelMapper.toTrack(it) }
            YouTubeModelMapper.toAlbum(playlistDto, tracks)
        } catch (e: Exception) {
            println("YouTubeProvider: Error fetching album '$albumId': ${e.message}")
            null
        }
    }

    override suspend fun getPlaylist(playlistId: String): Playlist? {
        if (!isConfigured()) return null
        val cleanId = playlistId.removePrefix("yt_pl_")
        return try {
            val playlistResponse = apiService.getPlaylists(
                id = cleanId,
                apiKey = getApiKey()
            )
            val playlistDto = playlistResponse.items.firstOrNull() ?: return null
            val itemsResponse = apiService.getPlaylistItems(
                playlistId = cleanId,
                apiKey = getApiKey()
            )
            val tracks = itemsResponse.items.map { YouTubeModelMapper.toTrack(it) }
            YouTubeModelMapper.toPlaylist(playlistDto, tracks)
        } catch (e: Exception) {
            println("YouTubeProvider: Error fetching playlist '$playlistId': ${e.message}")
            null
        }
    }

    override suspend fun getFeaturedPlaylist(): Playlist {
        val tracks = getFreshDiscoveries()
        return Playlist(
            id = "yt_pl_featured",
            title = "YouTube Top Hits",
            description = "Popular music trending globally on YouTube",
            artworkUrl = tracks.firstOrNull()?.artworkUrl ?: "",
            curator = "YouTube Music",
            trackCount = tracks.size,
            tracks = tracks,
            providerId = providerId,
            providerName = providerName
        )
    }

    override suspend fun getDailyMixes(): List<DailyMix> {
        val tracks = getFreshDiscoveries()
        if (tracks.isEmpty()) return emptyList()
        return listOf(
            DailyMix(
                id = "yt_mix_1",
                mixNumber = 1,
                title = "Trending Mix",
                subtitle = "Top charting music on YouTube",
                description = "Daily curated mix based on current YouTube music trends",
                artworkUrl = tracks.firstOrNull()?.artworkUrl ?: "",
                tracks = tracks.take(10),
                providerId = providerId,
                providerName = providerName
            )
        )
    }

    override suspend fun getMadeForYouPlaylists(): List<Playlist> {
        val trending = getFreshDiscoveries()
        return listOf(
            Playlist(
                id = "yt_pl_trending",
                title = "Trending Worldwide",
                description = "The most played songs right now on YouTube",
                artworkUrl = trending.firstOrNull()?.artworkUrl ?: "",
                curator = "MUSE YouTube Engine",
                trackCount = trending.size,
                tracks = trending,
                providerId = providerId,
                providerName = providerName
            )
        )
    }

    override suspend fun getRecentlyPlayedTracks(): List<Track> {
        return emptyList()
    }

    override suspend fun getFreshDiscoveries(): List<Track> {
        if (!isConfigured()) return emptyList()
        return try {
            val response = apiService.getPopularMusicVideos(
                maxResults = 20,
                apiKey = getApiKey()
            )
            response.items.map { YouTubeModelMapper.toTrack(it) }
        } catch (e: Exception) {
            println("YouTubeProvider: Error fetching fresh discoveries: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getRecommendedArtists(): List<Artist> {
        return emptyList()
    }

    override suspend fun getRecommendedAlbums(): List<Album> {
        return emptyList()
    }

    override suspend fun getBecauseYouListenedTo(): Pair<Artist, List<Track>> {
        val artist = Artist(id = "yt_art_trend", name = "Trending Artists", providerId = providerId, providerName = providerName)
        return Pair(artist, getFreshDiscoveries())
    }

    override suspend fun getNewReleases(): List<Album> {
        return emptyList()
    }

    override suspend fun getRecommendations(context: RecommendationContext): List<Track> {
        val query = context.favoriteGenres.firstOrNull() ?: context.currentMood ?: "music"
        return search(query)
    }

    override suspend fun getAllTracks(): List<Track> {
        return getFreshDiscoveries()
    }

    companion object {
        private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"

        fun createDefaultApiService(): YouTubeApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(YOUTUBE_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(YouTubeApiService::class.java)
        }
    }
}
