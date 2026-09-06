package com.example.data.repository

import com.example.data.local.LocalDataStore
import com.example.data.remote.SupabaseClient
import com.example.domain.model.ListeningHistoryEntry
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.ListeningHistoryRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListeningHistoryTableDto(
    @SerialName("user_id") val userId: String,
    @SerialName("track_id") val trackId: String,
    @SerialName("played_at") val playedAt: Long? = null,
    @SerialName("position_ms") val positionMs: Long,
    @SerialName("duration_ms") val durationMs: Long,
    @SerialName("completed") val completed: Boolean
)

class SupabaseListeningHistoryRepositoryImpl(
    private val authRepository: AuthRepository,
    private val localDataStore: LocalDataStore
) : ListeningHistoryRepository {
    private val client = SupabaseClient.client
    private val mutex = Mutex()
    private val lastPersistedTime = mutableMapOf<String, Long>()

    private fun isGuest(): Boolean = authRepository.getGuestSession() != null
    private suspend fun getUserId(): String? = authRepository.getCurrentSession()

    override suspend fun recordPlaybackStart(trackId: String) {
        if (isGuest()) {
            localDataStore.addGuestHistoryEntry(ListeningHistoryEntry(userId = getUserId() ?: "guest", trackId = trackId))
            return
        }
        val userId = getUserId() ?: return
        
        // New entry for start
        try {
            client.from("listening_history").insert(
                ListeningHistoryTableDto(userId = userId, trackId = trackId, positionMs = 0, durationMs = 0, completed = false)
            )
        } catch (e: Exception) {
            println("Error recording playback start: ${e.message}")
            throw e
        }
    }

    override suspend fun recordPlaybackProgress(trackId: String, positionMs: Long, durationMs: Long) {
        if (isGuest()) {
            // Local guest history update logic
            return
        }
        val userId = getUserId() ?: return
        
        mutex.withLock {
            val lastTime = lastPersistedTime[trackId] ?: 0L
            if (System.currentTimeMillis() - lastTime < 15000) return
        }

        try {
            client.from("listening_history")
                .update({ set("position_ms", positionMs); set("duration_ms", durationMs) }) {
                    filter { eq("user_id", userId); eq("track_id", trackId); eq("completed", false) }
                }
            mutex.withLock { lastPersistedTime[trackId] = System.currentTimeMillis() }
        } catch (e: Exception) {
            println("Error recording playback progress: ${e.message}")
            throw e
        }
    }

    override suspend fun recordPlaybackCompleted(trackId: String, durationMs: Long) {
        if (isGuest()) {
            // Local guest history update logic
            return
        }
        val userId = getUserId() ?: return
        
        try {
            client.from("listening_history")
                .update({ set("completed", true); set("duration_ms", durationMs); set("position_ms", durationMs) }) {
                    filter { eq("user_id", userId); eq("track_id", trackId); eq("completed", false) }
                }
        } catch (e: Exception) {
            println("Error recording playback completion: ${e.message}")
            throw e
        }
    }

    override suspend fun getRecentHistory(limit: Int): List<ListeningHistoryEntry> {
        // Implementation for authenticated/guest...
        return emptyList()
    }

    override suspend fun getHistory(): List<ListeningHistoryEntry> {
        return emptyList()
    }
}
