package com.example.domain.repository

import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun createProfile(profile: UserProfile)
    suspend fun updateProfile(profile: UserProfile)

    suspend fun ensureProfileExists(
        userId: String,
        defaultName: String? = null,
        defaultUsername: String? = null,
        defaultAvatarUrl: String? = null
    ) {
        println("ProfileRepo: Ensuring profile exists for user: $userId")
        val existing = getProfile(userId)
        if (existing == null) {
            println("ProfileRepo: Profile not found during recovery check, attempting upsert/creation for user: $userId")
            createProfile(
                UserProfile(
                    id = userId,
                    username = defaultUsername,
                    displayName = defaultName,
                    avatarUrl = defaultAvatarUrl
                )
            )
            println("ProfileRepo: Recovery upsert/creation success for user: $userId")
        } else {
            println("ProfileRepo: Profile already exists for user: $userId, skipping recovery creation")
        }
    }
}
