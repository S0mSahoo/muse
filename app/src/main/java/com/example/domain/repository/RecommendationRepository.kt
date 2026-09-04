package com.example.domain.repository

import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface RecommendationRepository {
    fun getMoodRecommendations(mood: String): Flow<List<Track>>
    fun getDiscoverWeekly(): Flow<List<Track>>
    fun getTasteProfileGenres(): Flow<List<String>>
    fun getRecommendations(context: RecommendationContext): Flow<List<Track>>
}

