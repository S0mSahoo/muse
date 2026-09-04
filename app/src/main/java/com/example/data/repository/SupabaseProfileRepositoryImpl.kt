package com.example.data.repository

import com.example.data.remote.SupabaseClient
import com.example.domain.model.UserProfile
import com.example.domain.repository.ProfileRepository
import io.github.jan.supabase.postgrest.from

class SupabaseProfileRepositoryImpl : ProfileRepository {
    private val client = SupabaseClient.client
    
    override suspend fun getProfile(userId: String): UserProfile? {
        return client.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<UserProfile>()
    }
    
    override suspend fun createProfile(profile: UserProfile) {
        client.from("profiles").insert(profile)
    }
    
    override suspend fun updateProfile(profile: UserProfile) {
        client.from("profiles").update(profile) {
            filter { eq("id", profile.id) }
        }
    }
}
