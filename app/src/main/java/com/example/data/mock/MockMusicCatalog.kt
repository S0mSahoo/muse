package com.example.data.mock

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.Track

object MockMusicCatalog {

    val sampleArtists = listOf(
        Artist(
            id = "art_1",
            name = "The Midnight",
            imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            monthlyListeners = "1.4M monthly listeners",
            genres = listOf("Synthwave", "Retrowave", "Electronic")
        ),
        Artist(
            id = "art_2",
            name = "L'Impératrice",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            monthlyListeners = "890K monthly listeners",
            genres = listOf("Nu-Disco", "French Pop", "Funk")
        ),
        Artist(
            id = "art_3",
            name = "Arijit Singh",
            imageUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
            monthlyListeners = "38.5M monthly listeners",
            genres = listOf("Bollywood", "Acoustic", "Sufi")
        ),
        Artist(
            id = "art_4",
            name = "Tycho",
            imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            monthlyListeners = "2.1M monthly listeners",
            genres = listOf("Ambient", "Chillwave", "IDM")
        ),
        Artist(
            id = "art_5",
            name = "FKJ",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            monthlyListeners = "3.2M monthly listeners",
            genres = listOf("Neo-Soul", "Nu-Jazz", "Electronic")
        ),
        Artist(
            id = "art_6",
            name = "Ólafur Arnalds",
            imageUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80",
            monthlyListeners = "1.8M monthly listeners",
            genres = listOf("Neo-Classical", "Ambient", "Cinematic")
        )
    )

    val sampleTracks = listOf(
        Track(
            id = "trk_1",
            title = "Sunset City Lights",
            artist = "The Midnight",
            album = "Endless Summer",
            artworkUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            durationMs = 248000L,
            releaseYear = 2023,
            genres = listOf("Synthwave", "Chillwave"),
            isLiked = true,
            dominantColorHex = 0xFF8B5CF6
        ),
        Track(
            id = "trk_2",
            title = "Anomalie Bleue",
            artist = "L'Impératrice",
            album = "Tako Tsubo",
            artworkUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            durationMs = 215000L,
            releaseYear = 2022,
            genres = listOf("Nu-Disco", "French Pop"),
            isLiked = false,
            dominantColorHex = 0xFF06B6D4
        ),
        Track(
            id = "trk_3",
            title = "Shayad (Acoustic Reprise)",
            artist = "Arijit Singh",
            album = "Midnight Sessions",
            artworkUrl = "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            durationMs = 232000L,
            releaseYear = 2023,
            genres = listOf("Acoustic", "Soul"),
            isLiked = true,
            dominantColorHex = 0xFFF59E0B
        ),
        Track(
            id = "trk_4",
            title = "Awake & Dreaming",
            artist = "Tycho",
            album = "Awake",
            artworkUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            durationMs = 283000L,
            releaseYear = 2021,
            genres = listOf("Ambient", "IDM"),
            isLiked = false,
            dominantColorHex = 0xFFEC4899
        ),
        Track(
            id = "trk_5",
            title = "Tadow (Live Euphoria)",
            artist = "FKJ",
            album = "French Kiwi Juice",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            durationMs = 312000L,
            releaseYear = 2020,
            genres = listOf("Neo-Soul", "Nu-Jazz"),
            isLiked = true,
            dominantColorHex = 0xFF10B981
        ),
        Track(
            id = "trk_6",
            title = "Spiral in the Sky",
            artist = "Ólafur Arnalds",
            album = "some kind of peace",
            artworkUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            durationMs = 195000L,
            releaseYear = 2024,
            genres = listOf("Neo-Classical"),
            isLiked = false,
            dominantColorHex = 0xFF6366F1
        ),
        Track(
            id = "trk_7",
            title = "Resonance Drift",
            artist = "HOME",
            album = "Odyssey",
            artworkUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            durationMs = 210000L,
            releaseYear = 2022,
            genres = listOf("Chillwave", "Synthwave"),
            isLiked = true,
            dominantColorHex = 0xFF9333EA
        ),
        Track(
            id = "trk_8",
            title = "Velvet Midnight",
            artist = "Leon Bridges",
            album = "Gold-Diggers Sound",
            artworkUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            durationMs = 204000L,
            releaseYear = 2023,
            genres = listOf("R&B", "Soul"),
            isLiked = false,
            dominantColorHex = 0xFFD97706
        )
    )

    val featuredPlaylist = Playlist(
        id = "pl_featured_01",
        title = "Midnight Reverie",
        description = "Deep ambient soundscapes, synth-drenched rhythms and late-night contemplation curated for your flow.",
        artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
        curator = "MUSE Curators",
        trackCount = 28,
        durationMinutes = 114,
        isCurated = true,
        tracks = sampleTracks,
        gradientColors = listOf(0xFF6366F1, 0xFF8B5CF6, 0xFFEC4899)
    )

    val dailyMixes = listOf(
        DailyMix(
            id = "dm_1",
            mixNumber = 1,
            title = "Daily Mix 1",
            subtitle = "The Midnight, Gunship, FM-84",
            description = "Your favorite synthwave anthems and melodic retrowave gems.",
            artworkUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
            artistsPreview = listOf("The Midnight", "FM-84", "Timecop1983", "Gunship"),
            dominantColorHex = 0xFF8B5CF6,
            tracks = listOf(sampleTracks[0], sampleTracks[6])
        ),
        DailyMix(
            id = "dm_2",
            mixNumber = 2,
            title = "Daily Mix 2",
            subtitle = "Arijit Singh, Prateek Kuhad, Anuv Jain",
            description = "Soulful acoustics, gentle hindi poetry and heartfelt ballads.",
            artworkUrl = "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=600&auto=format&fit=crop&q=80",
            artistsPreview = listOf("Arijit Singh", "Prateek Kuhad", "Anuv Jain", "Jasleen Royal"),
            dominantColorHex = 0xFFF59E0B,
            tracks = listOf(sampleTracks[2])
        ),
        DailyMix(
            id = "dm_3",
            mixNumber = 3,
            title = "Daily Mix 3",
            subtitle = "FKJ, Tom Misch, Masego",
            description = "Groovy neo-soul loops, live jazz improvisation and silky chords.",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            artistsPreview = listOf("FKJ", "Tom Misch", "Masego", "Jordan Rakei"),
            dominantColorHex = 0xFF10B981,
            tracks = listOf(sampleTracks[4], sampleTracks[7])
        ),
        DailyMix(
            id = "dm_4",
            mixNumber = 4,
            title = "Daily Mix 4",
            subtitle = "Tycho, Ólafur Arnalds, Nils Frahm",
            description = "Immersive ambient textures, gentle piano echoes and modern classical bliss.",
            artworkUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=600&auto=format&fit=crop&q=80",
            artistsPreview = listOf("Tycho", "Ólafur Arnalds", "Nils Frahm", "Max Richter"),
            dominantColorHex = 0xFF06B6D4,
            tracks = listOf(sampleTracks[3], sampleTracks[5])
        )
    )

    val madeForYouPlaylists = listOf(
        Playlist(
            id = "pl_mfy_1",
            title = "Ethereal Echoes",
            description = "Hypnotic dream pop and lush spatial reverbs.",
            artworkUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600&auto=format&fit=crop&q=80",
            curator = "Made for Som",
            trackCount = 32,
            gradientColors = listOf(0xFF8B5CF6, 0xFF3B82F6)
        ),
        Playlist(
            id = "pl_mfy_2",
            title = "Neon Horizon",
            description = "Cinematic driving music for night highways.",
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            curator = "MUSE Algorithmic",
            trackCount = 45,
            gradientColors = listOf(0xFFEC4899, 0xFF8B5CF6)
        ),
        Playlist(
            id = "pl_mfy_3",
            title = "Deep Focus Pulse",
            description = "Minimal organic house and lo-fi beats with zero vocals.",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            curator = "Focus Station",
            trackCount = 50,
            gradientColors = listOf(0xFF10B981, 0xFF06B6D4)
        ),
        Playlist(
            id = "pl_mfy_4",
            title = "Golden Hour Acoustics",
            description = "Warm organic guitars and sunset reflections.",
            artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80",
            curator = "Made for Som",
            trackCount = 24,
            gradientColors = listOf(0xFFF59E0B, 0xFFEF4444)
        )
    )

    val freshDiscoveries = listOf(
        Track(
            id = "disc_1",
            title = "Cascade in Minor",
            artist = "Kiasmos",
            album = "Blurred Ep",
            artworkUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            durationMs = 295000L,
            releaseYear = 2024,
            genres = listOf("Minimal Techno", "Neo-Classical"),
            dominantColorHex = 0xFF6366F1
        ),
        Track(
            id = "disc_2",
            title = "Floating in Tokyo",
            artist = "Kavinsky",
            album = "Reborn",
            artworkUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            durationMs = 210000L,
            releaseYear = 2024,
            genres = listOf("French Electro"),
            dominantColorHex = 0xFFFF5376
        ),
        Track(
            id = "disc_3",
            title = "Baarishein (Ambient Mix)",
            artist = "Anuv Jain",
            album = "Acoustic Tapes",
            artworkUrl = "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            durationMs = 224000L,
            releaseYear = 2024,
            genres = listOf("Indie Acoustic"),
            dominantColorHex = 0xFFF59E0B
        ),
        Track(
            id = "disc_4",
            title = "Solaris Dreams",
            artist = "Le Youth",
            album = "Reminders",
            artworkUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            durationMs = 260000L,
            releaseYear = 2024,
            genres = listOf("Melodic House"),
            dominantColorHex = 0xFF06B6D4
        )
    )

    val sampleAlbums = listOf(
        Album(
            id = "alb_1",
            title = "Endless Summer",
            artist = "The Midnight",
            artworkUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
            releaseYear = 2023,
            trackCount = 12,
            genres = listOf("Synthwave")
        ),
        Album(
            id = "alb_2",
            title = "Tako Tsubo",
            artist = "L'Impératrice",
            artworkUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=600&auto=format&fit=crop&q=80",
            releaseYear = 2022,
            trackCount = 13,
            genres = listOf("Nu-Disco")
        ),
        Album(
            id = "alb_3",
            title = "Awake",
            artist = "Tycho",
            artworkUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=600&auto=format&fit=crop&q=80",
            releaseYear = 2021,
            trackCount = 8,
            genres = listOf("Ambient")
        ),
        Album(
            id = "alb_4",
            title = "French Kiwi Juice",
            artist = "FKJ",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            releaseYear = 2020,
            trackCount = 12,
            genres = listOf("Neo-Soul")
        )
    )
}
