package com.example.data.repository

import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import com.example.domain.provider.MusicCatalogProvider
import com.example.domain.recommendation.RecommendationEngine
import com.example.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockRecommendationRepositoryImpl(
    private val musicCatalogProvider: MusicCatalogProvider,
    private val recommendationEngine: RecommendationEngine
) : RecommendationRepository {

    override fun getMoodRecommendations(mood: String): Flow<List<Track>> = flow {
        val context = RecommendationContext(currentMood = mood)
        emit(recommendationEngine.getRecommendations(context))
    }

    override fun getDiscoverWeekly(): Flow<List<Track>> = flow {
        emit(musicCatalogProvider.getFreshDiscoveries())
    }

    override fun getTasteProfileGenres(): Flow<List<String>> = flow {
        emit(listOf("Synthwave", "Nu-Disco", "Indie Acoustic", "Neo-Soul", "Ambient"))
    }

    override fun getRecommendations(context: RecommendationContext): Flow<List<Track>> = flow {
        emit(recommendationEngine.getRecommendations(context))
    }
}

