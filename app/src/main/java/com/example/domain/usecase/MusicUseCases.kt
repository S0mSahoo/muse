package com.example.domain.usecase

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.ListeningEvent
import com.example.domain.model.Playlist
import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import com.example.domain.repository.ListeningHistoryRepository
import com.example.domain.repository.MusicRepository
import com.example.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class HomeContent(
    val featuredPlaylist: Playlist,
    val dailyMixes: List<DailyMix>,
    val madeForYouPlaylists: List<Playlist>,
    val recentlyPlayedTracks: List<Track>,
    val freshDiscoveries: List<Track>,
    val recommendedArtists: List<Artist>,
    val recommendedAlbums: List<Album>,
    val becauseYouListenedTo: Pair<Artist, List<Track>>
)

class GetHomeContentUseCase(private val musicRepository: MusicRepository) {
    operator fun invoke(): Flow<HomeContent> {
        return combine(
            musicRepository.getFeaturedPlaylist(),
            musicRepository.getDailyMixes(),
            musicRepository.getMadeForYouPlaylists(),
            musicRepository.getRecentlyPlayedTracks(),
            musicRepository.getFreshDiscoveries(),
            musicRepository.getRecommendedArtists(),
            musicRepository.getRecommendedAlbums(),
            musicRepository.getBecauseYouListenedTo()
        ) { params ->
            HomeContent(
                featuredPlaylist = params[0] as Playlist,
                dailyMixes = @Suppress("UNCHECKED_CAST") (params[1] as List<DailyMix>),
                madeForYouPlaylists = @Suppress("UNCHECKED_CAST") (params[2] as List<Playlist>),
                recentlyPlayedTracks = @Suppress("UNCHECKED_CAST") (params[3] as List<Track>),
                freshDiscoveries = @Suppress("UNCHECKED_CAST") (params[4] as List<Track>),
                recommendedArtists = @Suppress("UNCHECKED_CAST") (params[5] as List<Artist>),
                recommendedAlbums = @Suppress("UNCHECKED_CAST") (params[6] as List<Album>),
                becauseYouListenedTo = @Suppress("UNCHECKED_CAST") (params[7] as Pair<Artist, List<Track>>)
            )
        }
    }
}

class ToggleLikeUseCase(private val musicRepository: MusicRepository) {
    suspend operator fun invoke(trackId: String): Boolean {
        return musicRepository.toggleLike(trackId)
    }
}

class GetLikedTracksUseCase(private val musicRepository: MusicRepository) {
    operator fun invoke(): Flow<List<Track>> {
        return musicRepository.getLikedTracks()
    }
}

class SearchMusicUseCase(private val musicRepository: MusicRepository) {
    suspend operator fun invoke(query: String): List<Track> {
        return musicRepository.search(query)
    }
}

class GetDiscoverContentUseCase(
    private val musicRepository: MusicRepository,
    private val recommendationRepository: RecommendationRepository
) {
    fun getCuratedPlaylists(): Flow<List<Playlist>> = musicRepository.getMadeForYouPlaylists()
    fun getMoodTracks(mood: String): Flow<List<Track>> = recommendationRepository.getMoodRecommendations(mood)
}

class GetRecommendationsUseCase(private val recommendationRepository: RecommendationRepository) {
    operator fun invoke(context: RecommendationContext): Flow<List<Track>> {
        return recommendationRepository.getRecommendations(context)
    }
}

class RecordListeningEventUseCase(private val listeningHistoryRepository: ListeningHistoryRepository) {
    suspend operator fun invoke(event: ListeningEvent) {
        listeningHistoryRepository.recordEvent(event)
    }
}
