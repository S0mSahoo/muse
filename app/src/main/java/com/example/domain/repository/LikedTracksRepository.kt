package com.example.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface LikedTracksRepository {
    fun getLikedTrackIds(): StateFlow<Set<String>>
    suspend fun isLiked(trackId: String): Boolean
    suspend fun addLike(trackId: String)
    suspend fun removeLike(trackId: String)
    suspend fun toggleLike(trackId: String): Boolean
}
