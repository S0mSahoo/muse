package com.example.di

import com.example.data.local.LocalDataStore
import com.example.data.playback.PlaybackManager
import com.example.data.remote.SupabaseClient
import com.example.data.provider.MockMusicCatalogProvider
import com.example.data.provider.MockPlaybackProvider
import com.example.data.recommendation.DefaultCandidateGenerator
import com.example.data.recommendation.DefaultRecommendationEngine
import com.example.data.recommendation.DefaultTrackRanker
import com.example.data.repository.SupabaseListeningHistoryRepositoryImpl
import com.example.data.repository.MockEntitlementRepositoryImpl
import com.example.data.repository.MockMusicRepositoryImpl
import com.example.data.repository.MockPlaybackRepositoryImpl
import com.example.data.repository.MockRecommendationRepositoryImpl
import com.example.data.repository.SupabaseProfileRepositoryImpl
import com.example.data.repository.SupabaseAuthRepositoryImpl
import com.example.data.repository.SupabaseLikedTracksRepositoryImpl
import com.example.domain.provider.MusicCatalogProvider
import com.example.domain.provider.PlaybackProvider
import com.example.domain.recommendation.RecommendationEngine
import com.example.domain.repository.EntitlementRepository
import com.example.domain.repository.ListeningHistoryRepository
import com.example.domain.repository.MusicRepository
import com.example.domain.repository.PlaybackRepository
import com.example.domain.repository.ProfileRepository
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LikedTracksRepository
import com.example.domain.repository.RecommendationRepository
import com.example.domain.usecase.GetDiscoverContentUseCase
import com.example.domain.usecase.GetEntitlementsUseCase
import com.example.domain.usecase.GetHomeContentUseCase
import com.example.domain.usecase.GetLikedTracksUseCase
import com.example.domain.usecase.GetPlaybackStateUseCase
import com.example.domain.usecase.GetRecommendationsUseCase
import com.example.domain.usecase.PausePlaybackUseCase
import com.example.domain.usecase.PlayPlaylistUseCase
import com.example.domain.usecase.PlayTrackUseCase
import com.example.domain.usecase.ResumePlaybackUseCase
import com.example.domain.usecase.SearchMusicUseCase
import com.example.domain.usecase.SeekToUseCase
import com.example.domain.usecase.SetSubscriptionTierUseCase
import com.example.domain.usecase.SkipNextUseCase
import com.example.domain.usecase.SkipPreviousUseCase
import com.example.domain.usecase.ToggleLikeUseCase
import com.example.domain.usecase.TogglePlayPauseUseCase
import io.github.jan.supabase.auth.auth

object AppContainer {
    val localDataStore: LocalDataStore by lazy { LocalDataStore() }
    val supabaseClient = SupabaseClient.client

    val mockMusicCatalogProvider: MusicCatalogProvider by lazy { MockMusicCatalogProvider() }
    val youTubeMusicCatalogProvider: com.example.data.provider.youtube.YouTubeMusicCatalogProvider by lazy { 
        com.example.data.provider.youtube.YouTubeMusicCatalogProvider() 
    }
    val directMusicCatalogProvider: com.example.data.provider.direct.DirectMusicCatalogProvider by lazy { 
        com.example.data.provider.direct.DirectMusicCatalogProvider() 
    }

    // Active music catalog provider (keeps Mock as active provider/fallback)
    val musicCatalogProvider: MusicCatalogProvider by lazy { mockMusicCatalogProvider }
    val playbackProvider: PlaybackProvider by lazy { MockPlaybackProvider() }

    val listeningHistoryRepository: ListeningHistoryRepository by lazy { 
        SupabaseListeningHistoryRepositoryImpl(authRepository, localDataStore) 
    }
    val candidateGenerator by lazy { DefaultCandidateGenerator(musicCatalogProvider) }
    val trackRanker by lazy { DefaultTrackRanker() }
    val recommendationEngine: RecommendationEngine by lazy {
        DefaultRecommendationEngine(candidateGenerator, trackRanker)
    }

    val recommendationRepository: RecommendationRepository by lazy {
        MockRecommendationRepositoryImpl(musicCatalogProvider, recommendationEngine)
    }
    
    val profileRepository: ProfileRepository by lazy {
        SupabaseProfileRepositoryImpl()
    }
    
    val authRepository: AuthRepository by lazy {
        SupabaseAuthRepositoryImpl()
    }

    val likedTracksRepository: LikedTracksRepository by lazy {
        SupabaseLikedTracksRepositoryImpl(authRepository)
    }
    
    val entitlementRepository: EntitlementRepository by lazy {
        MockEntitlementRepositoryImpl(localDataStore)
    }
    val musicRepository: MusicRepository by lazy {
        MockMusicRepositoryImpl(musicCatalogProvider, likedTracksRepository)
    }

    val playbackManager: PlaybackManager by lazy {
        PlaybackManager(
            playbackProvider = playbackProvider,
            localDataStore = localDataStore,
            likedTracksRepository = likedTracksRepository,
            listeningHistoryRepository = listeningHistoryRepository
        )
    }
    val playbackRepository: PlaybackRepository by lazy {
        MockPlaybackRepositoryImpl(playbackManager)
    }

    val playTrackUseCase by lazy { PlayTrackUseCase(playbackRepository) }
    val playPlaylistUseCase by lazy { PlayPlaylistUseCase(playbackRepository) }
    val togglePlayPauseUseCase by lazy { TogglePlayPauseUseCase(playbackRepository) }
    val pausePlaybackUseCase by lazy { PausePlaybackUseCase(playbackRepository) }
    val resumePlaybackUseCase by lazy { ResumePlaybackUseCase(playbackRepository) }
    val skipNextUseCase by lazy { SkipNextUseCase(playbackRepository) }
    val skipPreviousUseCase by lazy { SkipPreviousUseCase(playbackRepository) }
    val seekToUseCase by lazy { SeekToUseCase(playbackRepository) }
    val getPlaybackStateUseCase by lazy { GetPlaybackStateUseCase(playbackRepository) }

    val getHomeContentUseCase by lazy { GetHomeContentUseCase(musicRepository) }
    val getDiscoverContentUseCase by lazy { GetDiscoverContentUseCase(musicRepository, recommendationRepository) }
    val toggleLikeUseCase by lazy { ToggleLikeUseCase(musicRepository) }
    val getLikedTracksUseCase by lazy { GetLikedTracksUseCase(musicRepository) }
    val searchMusicUseCase by lazy { SearchMusicUseCase(musicRepository) }
    val getRecommendationsUseCase by lazy { GetRecommendationsUseCase(recommendationRepository) }

    val getEntitlementsUseCase by lazy { GetEntitlementsUseCase(entitlementRepository) }
    val setSubscriptionTierUseCase by lazy { SetSubscriptionTierUseCase(entitlementRepository) }
}
