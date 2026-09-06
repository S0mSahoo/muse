package com.example.data.repository

import com.example.data.local.LocalDataStore
import com.example.data.remote.SupabaseClient
import com.example.domain.model.ListeningHistoryEntry
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.ListeningHistoryRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class ListeningHistoryTableDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("track_id") val trackId: String,
    @SerialName("played_at") val playedAt: String? = null,
    @SerialName("position_ms") val positionMs: Long = 0L,
    @SerialName("duration_ms") val durationMs: Long = 0L,
    @SerialName("completed") val completed: Boolean = false
) {
    fun toDomain(): ListeningHistoryEntry {
        val parsedTime = playedAt?.let { parseTimestamp(it) } ?: System.currentTimeMillis()
        return ListeningHistoryEntry(
            id = id,
            userId = userId,
            trackId = trackId,
            playedAt = parsedTime,
            positionMs = positionMs,
            durationMs = durationMs,
            completed = completed
        )
    }

    private fun parseTimestamp(dateStr: String): Long {
        return try {
            Instant.parse(dateStr).toEpochMilli()
        } catch (e: Exception) {
            try {
                OffsetDateTime.parse(dateStr).toInstant().toEpochMilli()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }
}

class SupabaseListeningHistoryRepositoryImpl(
    private val authRepository: AuthRepository,
    private val localDataStore: LocalDataStore
) : ListeningHistoryRepository {
    private val client = SupabaseClient.client
    private val mutex = Mutex()
    private val lastPersistedTime = mutableMapOf<String, Long>()
    private val completedSessions = mutableSetOf<String>()

    private fun isGuest(): Boolean = authRepository.getGuestSession() != null
    private suspend fun getUserId(): String? = authRepository.getCurrentSession()

    override suspend fun recordPlaybackStart(trackId: String): String? {
        if (isGuest()) {
            val sessionId = "guest_${UUID.randomUUID()}"
            val entry = ListeningHistoryEntry(
                id = sessionId,
                userId = "guest",
                trackId = trackId,
                playedAt = System.currentTimeMillis(),
                positionMs = 0L,
                durationMs = 0L,
                completed = false
            )
            localDataStore.addGuestHistoryEntry(entry)
            println("ListeningHistoryRepo: Created guest history session $sessionId for track $trackId")
            return sessionId
        }

        val userId = getUserId() ?: return null
        val historyId = UUID.randomUUID().toString()

        try {
            client.from("listening_history").insert(
                ListeningHistoryTableDto(
                    id = historyId,
                    userId = userId,
                    trackId = trackId,
                    positionMs = 0L,
                    durationMs = 0L,
                    completed = false
                )
            )
            println("ListeningHistoryRepo: Created Supabase history session $historyId for user $userId, track $trackId")
            return historyId
        } catch (e: Exception) {
            println("ListeningHistoryRepo: Error recording playback start for track $trackId: ${e.message}")
            throw e
        }
    }

    override suspend fun recordPlaybackProgress(historyId: String, positionMs: Long, durationMs: Long) {
        if (isGuest()) {
            localDataStore.updateGuestHistoryProgress(historyId, positionMs, durationMs)
            return
        }

        val userId = getUserId() ?: return

        mutex.withLock {
            val now = System.currentTimeMillis()
            val lastTime = lastPersistedTime[historyId] ?: 0L
            if (now - lastTime < 15_000L) {
                return
            }

            try {
                client.from("listening_history")
                    .update({
                        set("position_ms", positionMs)
                        set("duration_ms", durationMs)
                    }) {
                        filter {
                            eq("id", historyId)
                            eq("user_id", userId)
                        }
                    }
                lastPersistedTime[historyId] = System.currentTimeMillis()
                println("ListeningHistoryRepo: Updated progress for history session $historyId ($positionMs/$durationMs ms)")
            } catch (e: Exception) {
                println("ListeningHistoryRepo: Error recording playback progress for session $historyId: ${e.message}")
                throw e
            }
        }
    }

    override suspend fun recordPlaybackCompleted(historyId: String, durationMs: Long) {
        if (isGuest()) {
            val shouldComplete = mutex.withLock {
                if (completedSessions.contains(historyId)) {
                    false
                } else {
                    completedSessions.add(historyId)
                    true
                }
            }
            if (shouldComplete) {
                localDataStore.updateGuestHistoryCompleted(historyId, durationMs)
                println("ListeningHistoryRepo: Completed guest history session $historyId")
            }
            return
        }

        val userId = getUserId() ?: return

        mutex.withLock {
            if (completedSessions.contains(historyId)) {
                println("ListeningHistoryRepo: Duplicate completion ignored for session $historyId")
                return
            }

            try {
                client.from("listening_history")
                    .update({
                        set("completed", true)
                        set("duration_ms", durationMs)
                        set("position_ms", durationMs)
                    }) {
                        filter {
                            eq("id", historyId)
                            eq("user_id", userId)
                        }
                    }
                completedSessions.add(historyId)
                println("ListeningHistoryRepo: Marked history session $historyId as completed in Supabase")
            } catch (e: Exception) {
                println("ListeningHistoryRepo: Error recording playback completion for session $historyId: ${e.message}")
                throw e
            }
        }
    }

    override suspend fun getRecentHistory(limit: Int): List<ListeningHistoryEntry> {
        if (isGuest()) {
            return localDataStore.getRecentGuestHistory(limit)
        }

        val userId = getUserId() ?: return emptyList()

        try {
            val list = client.from("listening_history")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order(column = "played_at", order = Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ListeningHistoryTableDto>()
            return list.map { it.toDomain() }
        } catch (e: Exception) {
            println("ListeningHistoryRepo: Error getting recent history from Supabase: ${e.message}")
            throw e
        }
    }

    override suspend fun getHistory(): List<ListeningHistoryEntry> {
        if (isGuest()) {
            return localDataStore.getGuestHistory()
        }

        val userId = getUserId() ?: return emptyList()

        try {
            val list = client.from("listening_history")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order(column = "played_at", order = Order.DESCENDING)
                }
                .decodeList<ListeningHistoryTableDto>()
            return list.map { it.toDomain() }
        } catch (e: Exception) {
            println("ListeningHistoryRepo: Error getting history from Supabase: ${e.message}")
            throw e
        }
    }
}
