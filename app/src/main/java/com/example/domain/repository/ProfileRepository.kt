package com.example.domain.repository

import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun createProfile(profile: UserProfile)
    suspend fun updateProfile(profile: UserProfile)

    suspend fun ensureProfileExists(userId: String) {
        val existing = getProfile(userId)
        if (existing == null) {
            createProfile(UserProfile(id = userId, username = "NewUser", displayName = "User", avatarUrl = null))
        }
    }
}
