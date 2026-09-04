package com.example.domain.repository

import com.example.domain.model.ListeningEvent
import com.example.domain.model.ListeningStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing and recording listening events.
 * Provides analytical signals for taste profiling, recommendation scoring, and history playback.
 */
interface ListeningHistoryRepository {
    suspend fun recordEvent(event: ListeningEvent)
    fun getRecentEvents(limit: Int = 50): Flow<List<ListeningEvent>>
    fun getListeningStats(): Flow<ListeningStats>
    suspend fun clearHistory()
}
