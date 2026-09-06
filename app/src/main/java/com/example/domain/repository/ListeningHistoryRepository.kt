package com.example.domain.repository

import com.example.domain.model.ListeningHistoryEntry

interface ListeningHistoryRepository {
    suspend fun recordPlaybackStart(trackId: String)
    suspend fun recordPlaybackProgress(trackId: String, positionMs: Long, durationMs: Long)
    suspend fun recordPlaybackCompleted(trackId: String, durationMs: Long)
    suspend fun getRecentHistory(limit: Int): List<ListeningHistoryEntry>
    suspend fun getHistory(): List<ListeningHistoryEntry>
}
