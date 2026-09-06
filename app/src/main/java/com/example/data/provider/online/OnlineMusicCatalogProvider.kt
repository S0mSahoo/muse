package com.example.data.provider.online

import com.example.data.provider.online.api.OnlineMusicApiService
import com.example.data.provider.online.mapper.OnlineMusicModelMapper
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import com.example.domain.provider.MusicCatalogProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class OnlineMusicCatalogProvider(
    private val apiService: OnlineMusicApiService = createDefaultApiService()
) : MusicCatalogProvider {

    override val providerId: String = OnlineMusicModelMapper.PROVIDER_ID
    override val providerName: String = OnlineMusicModelMapper.PROVIDER_NAME

    // In-memory cache to make track lookups fast and reduce network overhead
    private val trackCache = ConcurrentHashMap<String, Track>()
    private val artistCache = ConcurrentHashMap<String, Artist>()
    private val albumCache = ConcurrentHashMap<String, Album>()

    override suspend fun search(query: String): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val response = apiService.searchSongs(term = query, limit = 30)
            val tracks = response.results.map { dto ->
                val track = OnlineMusicModelMapper.toTrack(dto)
                trackCache[track.id] = track
                track
            }
            tracks
        } catch (e: Exception) {
            println("OnlineMusicProvider: search failed for '$query': ${e.message}")
            emptyList()
        }
    }

    override suspend fun getTrack(trackId: String): Track? = withContext(Dispatchers.IO) {
        trackCache[trackId]?.let { return@withContext it }
        val cleanId = trackId.removePrefix("net_")
        try {
            val response = apiService.lookupById(id = cleanId, limit = 1)
            val dto = response.results.firstOrNull() ?: return@withContext null
            val track = OnlineMusicModelMapper.toTrack(dto)
            trackCache[track.id] = track
            track
        } catch (e: Exception) {
            println("OnlineMusicProvider: getTrack failed for '$trackId': ${e.message}")
            null
        }
    }

    override suspend fun getArtist(artistId: String): Artist? = withContext(Dispatchers.IO) {
        artistCache[artistId]?.let { return@withContext it }
        val cleanId = artistId.removePrefix("net_art_")
        try {
            val response = apiService.lookupById(id = cleanId, limit = 1)
            val dto = response.results.firstOrNull() ?: return@withContext null
            val artist = OnlineMusicModelMapper.toArtist(dto)
            artistCache[artist.id] = artist
            artist
        } catch (e: Exception) {
            println("OnlineMusicProvider: getArtist failed for '$artistId': ${e.message}")
            null
        }
    }

    override suspend fun getAlbum(albumId: String): Album? = withContext(Dispatchers.IO) {
        albumCache[albumId]?.let { return@withContext it }
        val cleanId = albumId.removePrefix("net_alb_")
        try {
            val response = apiService.lookupById(id = cleanId, limit = 20)
            val firstDto = response.results.firstOrNull() ?: return@withContext null
            val tracks = response.results.map { dto ->
                val track = OnlineMusicModelMapper.toTrack(dto)
                trackCache[track.id] = track
                track
            }
            val album = OnlineMusicModelMapper.toAlbum(firstDto, tracks)
            albumCache[album.id] = album
            album
        } catch (e: Exception) {
            println("OnlineMusicProvider: getAlbum failed for '$albumId': ${e.message}")
            null
        }
    }

    override suspend fun getPlaylist(playlistId: String): Playlist? = withContext(Dispatchers.IO) {
        val tracks = getFreshDiscoveries()
        Playlist(
            id = playlistId,
            title = "Curated Music Selection",
            description = "Real global music curated for your listening journey",
            artworkUrl = tracks.firstOrNull()?.artworkUrl ?: "",
            curator = "MUSE Curators",
            trackCount = tracks.size,
            tracks = tracks,
            providerId = providerId,
            providerName = providerName
        )
    }

    override suspend fun getFeaturedPlaylist(): Playlist = withContext(Dispatchers.IO) {
        val tracks = search("The Weeknd Daft Punk Synthwave").ifEmpty {
            search("Top Hits")
        }
        Playlist(
            id = "net_pl_featured",
            title = "Midnight Reverie",
            description = "Deep ambient soundscapes, synth-drenched rhythms, and modern electronic hits.",
            artworkUrl = tracks.firstOrNull()?.artworkUrl ?: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
            curator = "MUSE Curators",
            ownerType = "CURATED",
            trackCount = tracks.size,
            durationMinutes = (tracks.sumOf { it.durationMs } / 60000L).toInt(),
            isCurated = true,
            isDailyMix = false,
            tracks = tracks,
            providerId = providerId,
            providerName = providerName
        )
    }

    override suspend fun getDailyMixes(): List<DailyMix> = withContext(Dispatchers.IO) {
        val mix1Tracks = search("Synthwave Retrowave Gunship").ifEmpty { search("Electronic") }
        val mix2Tracks = search("Arijit Singh Prateek Kuhad").ifEmpty { search("Acoustic") }
        val mix3Tracks = search("FKJ Masego Tom Misch").ifEmpty { search("Nu-Disco") }

        listOf(
            DailyMix(
                id = "net_mix_1",
                mixNumber = 1,
                title = "Daily Mix 1",
                subtitle = "The Midnight, Gunship, FM-84",
                description = "Neon synthesizers, retro drum machines, and late-night driving anthems.",
                artworkUrl = mix1Tracks.firstOrNull()?.artworkUrl ?: "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&auto=format&fit=crop&q=80",
                tracks = mix1Tracks.take(10),
                providerId = providerId,
                providerName = providerName
            ),
            DailyMix(
                id = "net_mix_2",
                mixNumber = 2,
                title = "Daily Mix 2",
                subtitle = "Arijit Singh, Prateek Kuhad, Anuv Jain",
                description = "Soulful poetry, acoustic arrangements, and expressive vocal melodies.",
                artworkUrl = mix2Tracks.firstOrNull()?.artworkUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
                tracks = mix2Tracks.take(10),
                providerId = providerId,
                providerName = providerName
            ),
            DailyMix(
                id = "net_mix_3",
                mixNumber = 3,
                title = "Daily Mix 3",
                subtitle = "FKJ, Masego, Tom Misch",
                description = "Smooth neo-soul chords, French house grooves, and live instrumental vibes.",
                artworkUrl = mix3Tracks.firstOrNull()?.artworkUrl ?: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
                tracks = mix3Tracks.take(10),
                providerId = providerId,
                providerName = providerName
            )
        )
    }

    override suspend fun getMadeForYouPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        val synthTracks = search("Synthwave").take(15)
        val ambientTracks = search("Ambient Sleep").take(15)
        val funkTracks = search("Nu Disco Funk").take(15)

        listOf(
            Playlist(
                id = "net_pl_synth",
                title = "Electronic Odyssey",
                description = "Pulsing basslines and crystalline synth melodies crafted for nocturnal flow.",
                artworkUrl = synthTracks.firstOrNull()?.artworkUrl ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
                curator = "MUSE Curators",
                ownerType = "MADE_FOR_YOU",
                trackCount = synthTracks.size,
                durationMinutes = (synthTracks.sumOf { it.durationMs } / 60000L).toInt(),
                isCurated = true,
                isDailyMix = false,
                tracks = synthTracks,
                providerId = providerId,
                providerName = providerName
            ),
            Playlist(
                id = "net_pl_ambient",
                title = "Midnight Ambient",
                description = "Weightless textures and soothing drone harmonies to settle the mind.",
                artworkUrl = ambientTracks.firstOrNull()?.artworkUrl ?: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500&auto=format&fit=crop&q=80",
                curator = "MUSE Curators",
                ownerType = "MADE_FOR_YOU",
                trackCount = ambientTracks.size,
                durationMinutes = (ambientTracks.sumOf { it.durationMs } / 60000L).toInt(),
                isCurated = true,
                isDailyMix = false,
                tracks = ambientTracks,
                providerId = providerId,
                providerName = providerName
            ),
            Playlist(
                id = "net_pl_funk",
                title = "Nu-Disco & Funk",
                description = "Infectious basslines, bright chords, and timeless groove.",
                artworkUrl = funkTracks.firstOrNull()?.artworkUrl ?: "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&auto=format&fit=crop&q=80",
                curator = "MUSE Curators",
                ownerType = "MADE_FOR_YOU",
                trackCount = funkTracks.size,
                durationMinutes = (funkTracks.sumOf { it.durationMs } / 60000L).toInt(),
                isCurated = true,
                isDailyMix = false,
                tracks = funkTracks,
                providerId = providerId,
                providerName = providerName
            )
        )
    }

    override suspend fun getRecentlyPlayedTracks(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = search("The Midnight L'Imperatrice").take(5)
        tracks
    }

    override suspend fun getFreshDiscoveries(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = search("Popular Hits 2024").take(15)
        tracks
    }

    override suspend fun getRecommendedArtists(): List<Artist> = withContext(Dispatchers.IO) {
        listOf(
            Artist(
                id = "net_art_1",
                name = "The Midnight",
                imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&auto=format&fit=crop&q=80",
                monthlyListeners = "1.8M",
                genres = listOf("Synthwave", "Retrowave"),
                providerId = providerId,
                providerName = providerName
            ),
            Artist(
                id = "net_art_2",
                name = "L'Impératrice",
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&auto=format&fit=crop&q=80",
                monthlyListeners = "1.2M",
                genres = listOf("Nu-Disco", "French Pop"),
                providerId = providerId,
                providerName = providerName
            ),
            Artist(
                id = "net_art_3",
                name = "FKJ",
                imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&auto=format&fit=crop&q=80",
                monthlyListeners = "3.4M",
                genres = listOf("Neo-Soul", "Electronic"),
                providerId = providerId,
                providerName = providerName
            ),
            Artist(
                id = "net_art_4",
                name = "Arijit Singh",
                imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&auto=format&fit=crop&q=80",
                monthlyListeners = "38.5M",
                genres = listOf("Bollywood", "Sufi", "Acoustic"),
                providerId = providerId,
                providerName = providerName
            )
        )
    }

    override suspend fun getRecommendedAlbums(): List<Album> = withContext(Dispatchers.IO) {
        listOf(
            Album(
                id = "net_alb_1",
                title = "Endless Summer",
                artist = "The Midnight",
                artistId = "net_art_1",
                artworkUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&auto=format&fit=crop&q=80",
                releaseDate = "2016",
                releaseYear = 2016,
                trackCount = 12,
                genres = listOf("Synthwave"),
                providerId = providerId,
                providerName = providerName
            ),
            Album(
                id = "net_alb_2",
                title = "Tako Tsubo",
                artist = "L'Impératrice",
                artistId = "net_art_2",
                artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop&q=80",
                releaseDate = "2021",
                releaseYear = 2021,
                trackCount = 13,
                genres = listOf("Nu-Disco"),
                providerId = providerId,
                providerName = providerName
            ),
            Album(
                id = "net_alb_3",
                title = "French Kiwi Juice",
                artist = "FKJ",
                artistId = "net_art_3",
                artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
                releaseDate = "2017",
                releaseYear = 2017,
                trackCount = 12,
                genres = listOf("Electronic"),
                providerId = providerId,
                providerName = providerName
            )
        )
    }

    override suspend fun getBecauseYouListenedTo(): Pair<Artist, List<Track>> = withContext(Dispatchers.IO) {
        val artist = Artist(
            id = "net_art_1",
            name = "The Midnight",
            imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&auto=format&fit=crop&q=80",
            monthlyListeners = "1.8M",
            genres = listOf("Synthwave"),
            providerId = providerId,
            providerName = providerName
        )
        val tracks = search("The Midnight").take(6)
        Pair(artist, tracks)
    }

    override suspend fun getNewReleases(): List<Album> = withContext(Dispatchers.IO) {
        getRecommendedAlbums()
    }

    override suspend fun getRecommendations(context: RecommendationContext): List<Track> = withContext(Dispatchers.IO) {
        val query = context.favoriteGenres.firstOrNull() ?: context.currentMood ?: "Pop Hits"
        search(query)
    }

    override suspend fun getAllTracks(): List<Track> = withContext(Dispatchers.IO) {
        trackCache.values.toList().ifEmpty {
            search("Top Hits").also { tracks ->
                tracks.forEach { trackCache[it.id] = it }
            }
        }
    }

    companion object {
        private const val ITUNES_BASE_URL = "https://itunes.apple.com/"

        fun createDefaultApiService(): OnlineMusicApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(ITUNES_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(OnlineMusicApiService::class.java)
        }
    }
}
