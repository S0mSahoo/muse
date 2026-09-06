package com.example.domain.repository

import com.example.domain.model.ListeningHistoryEntry

interface ListeningHistoryRepository {
    suspend fun recordPlaybackStart(trackId: String): String?
    suspend fun recordPlaybackProgress(historyId: String, positionMs: Long, durationMs: Long)
    suspend fun recordPlaybackCompleted(historyId: String, durationMs: Long)
    suspend fun getRecentHistory(limit: Int): List<ListeningHistoryEntry>
    suspend fun getHistory(): List<ListeningHistoryEntry>
}
