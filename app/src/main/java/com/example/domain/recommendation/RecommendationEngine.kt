package com.example.domain.recommendation

import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface CandidateGenerator {
    suspend fun generateCandidates(context: RecommendationContext): List<Track>
}

interface TrackRanker {
    suspend fun rankTracks(tracks: List<Track>, context: RecommendationContext): List<Track>
}

interface TasteProfileRepository {
    fun getTasteProfileGenres(): Flow<List<String>>
    fun getTasteProfileArtists(): Flow<List<String>>
}

interface RecommendationEngine {
    suspend fun getRecommendations(context: RecommendationContext): List<Track>
}
