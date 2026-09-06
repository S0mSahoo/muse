package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ListeningHistoryEntry(
    val id: String? = null,
    val userId: String,
    val trackId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val completed: Boolean = false
)
