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
        return try {
            val list = client.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeList<UserProfile>()
            list.firstOrNull()
        } catch (e: Exception) {
            println("ProfileRepo: Error getting profile: ${e.message}")
            null
        }
    }
    
    override suspend fun createProfile(profile: UserProfile) {
        val dto = ProfileTableDto(
            id = profile.id,
            username = profile.username,
            displayName = profile.displayName,
            avatarUrl = profile.avatarUrl
        )
        client.from("profiles").insert(dto)
    }
    
    override suspend fun updateProfile(profile: UserProfile) {
        val dto = ProfileTableDto(
            id = profile.id,
            username = profile.username,
            displayName = profile.displayName,
            avatarUrl = profile.avatarUrl
        )
        client.from("profiles").update(dto) {
            filter { eq("id", profile.id) }
        }
    }
}
