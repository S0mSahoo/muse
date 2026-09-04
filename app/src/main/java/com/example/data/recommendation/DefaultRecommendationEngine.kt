package com.example.data.recommendation

import com.example.domain.model.RecommendationContext
import com.example.domain.model.Track
import com.example.domain.provider.MusicCatalogProvider
import com.example.domain.recommendation.CandidateGenerator
import com.example.domain.recommendation.RecommendationEngine
import com.example.domain.recommendation.TrackRanker

class DefaultCandidateGenerator(
    private val musicCatalogProvider: MusicCatalogProvider
) : CandidateGenerator {
    override suspend fun generateCandidates(context: RecommendationContext): List<Track> {
        return musicCatalogProvider.getAllTracks()
    }
}

class DefaultTrackRanker : TrackRanker {
    override suspend fun rankTracks(tracks: List<Track>, context: RecommendationContext): List<Track> {
        val mood = context.currentMood?.lowercase() ?: return tracks
        return tracks.sortedByDescending { track ->
            var score = 0
            if (context.likedTrackIds.contains(track.id)) score += 5
            if (track.genres.any { g -> g.contains(mood, ignoreCase = true) }) score += 10
            score
        }
    }
}

class DefaultRecommendationEngine(
    private val candidateGenerator: CandidateGenerator,
    private val trackRanker: TrackRanker
) : RecommendationEngine {
    override suspend fun getRecommendations(context: RecommendationContext): List<Track> {
        val candidates = candidateGenerator.generateCandidates(context)
        return trackRanker.rankTracks(candidates, context)
    }
}
