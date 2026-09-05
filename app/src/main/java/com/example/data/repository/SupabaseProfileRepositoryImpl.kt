package com.example.data.repository

import com.example.data.remote.SupabaseClient
import com.example.domain.model.UserProfile
import com.example.domain.repository.ProfileRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class ProfileTableDto(
    val id: String,
    val username: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

class SupabaseProfileRepositoryImpl : ProfileRepository {
    private val client = SupabaseClient.client
    
    override suspend fun getProfile(userId: String): UserProfile? {
        println("ProfileRepo: Profile lookup started for user: $userId")
        return try {
            val list = client.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeList<UserProfile>()
            val profile = list.firstOrNull()
            if (profile == null) {
                println("ProfileRepo: Profile not found for user: $userId")
            } else {
                println("ProfileRepo: Profile found successfully for user: $userId")
            }
            profile
        } catch (e: Exception) {
            println("ProfileRepo: Real database/network/decoding error during lookup for user $userId: ${e.message}")
            throw e
        }
    }
    
    override suspend fun createProfile(profile: UserProfile) {
        println("ProfileRepo: Recovery upsert attempt for user: ${profile.id}")
        val dto = ProfileTableDto(
            id = profile.id,
            username = profile.username,
            displayName = profile.displayName,
            avatarUrl = profile.avatarUrl
        )
        try {
            // Use upsert to be safe and idempotent if trigger or another process already inserted it
            client.from("profiles").upsert(dto)
            println("ProfileRepo: Recovery upsert success for user: ${profile.id}")
        } catch (e: Exception) {
            println("ProfileRepo: Recovery upsert failure for user ${profile.id}: ${e.message}")
            throw e
        }
    }
    
    override suspend fun updateProfile(profile: UserProfile) {
        println("ProfileRepo: Profile update attempt for user: ${profile.id}")
        val dto = ProfileTableDto(
            id = profile.id,
            username = profile.username,
            displayName = profile.displayName,
            avatarUrl = profile.avatarUrl
        )
        try {
            client.from("profiles").update(dto) {
                filter { eq("id", profile.id) }
            }
            println("ProfileRepo: Profile update success for user: ${profile.id}")
        } catch (e: Exception) {
            println("ProfileRepo: Profile update failure for user ${profile.id}: ${e.message}")
            throw e
        }
    }
}
