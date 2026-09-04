package com.example.domain.repository

import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun createProfile(profile: UserProfile)
    suspend fun updateProfile(profile: UserProfile)

    suspend fun ensureProfileExists(userId: String, defaultName: String = "User", defaultUsername: String = "user_${userId.take(8)}") {
        val existing = getProfile(userId)
        if (existing == null) {
            createProfile(
                UserProfile(
                    id = userId,
                    username = defaultUsername,
                    displayName = defaultName,
                    avatarUrl = null
                )
            )
        }
    }
}
