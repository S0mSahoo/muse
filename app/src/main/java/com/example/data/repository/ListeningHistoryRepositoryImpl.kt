package com.example.data.repository

import com.example.domain.model.ListeningEvent
import com.example.domain.model.ListeningStats
import com.example.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ListeningHistoryRepositoryImpl : ListeningHistoryRepository {

    private val _events = MutableStateFlow<List<ListeningEvent>>(emptyList())

    override suspend fun recordEvent(event: ListeningEvent) {
        _events.update { current ->
            (listOf(event) + current).take(200)
        }
    }

    override fun getRecentEvents(limit: Int): Flow<List<ListeningEvent>> {
        return _events.map { list -> list.take(limit) }
    }

    override fun getListeningStats(): Flow<ListeningStats> {
        return _events.map { list ->
            val totalPlayed = list.count { it.eventType == com.example.domain.model.ListeningEventType.PLAY_STARTED }
            val totalDuration = list.sumOf { it.durationListenedMs }
            ListeningStats(
                totalTracksPlayed = totalPlayed,
                totalDurationMs = totalDuration,
                topGenres = listOf("Synthwave", "Nu-Disco", "Ambient"),
                topArtists = listOf("The Midnight", "L'Impératrice", "FKJ")
            )
        }
    }

    override suspend fun clearHistory() {
        _events.value = emptyList()
    }
}
