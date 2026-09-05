package com.example.data.repository

import com.example.data.remote.SupabaseClient
import com.example.domain.model.AuthState
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.ProfileRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class SupabaseAuthRepositoryImpl(
    private val profileRepository: ProfileRepository = SupabaseProfileRepositoryImpl()
) : AuthRepository {
    private val auth = SupabaseClient.client.auth

    override val authState: Flow<AuthState> = auth.sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> AuthState.Authenticated
                is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
                is SessionStatus.Initializing -> {
                    if (auth.currentSessionOrNull() != null) AuthState.Authenticated else AuthState.Loading
                }
                else -> {
                    if (auth.currentSessionOrNull() != null) AuthState.Authenticated else AuthState.Unauthenticated
                }
            }
        }
        .onStart {
            val current = auth.currentSessionOrNull()
            emit(if (current != null) AuthState.Authenticated else AuthState.Loading)
        }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        auth.signInWith(IDToken) {
            provider = Google
            this.idToken = idToken
        }
        val user = auth.currentSessionOrNull()?.user
        if (user != null) {
            try {
                val rawName = user.userMetadata?.get("full_name") ?: user.userMetadata?.get("name")
                val rawAvatar = user.userMetadata?.get("avatar_url") ?: user.userMetadata?.get("picture")
                val rawUsername = user.userMetadata?.get("preferred_username")

                val name = (rawName as? JsonPrimitive)?.contentOrNull ?: user.email?.substringBefore("@") ?: "User"
                val avatar = (rawAvatar as? JsonPrimitive)?.contentOrNull
                val username = (rawUsername as? JsonPrimitive)?.contentOrNull ?: user.email?.substringBefore("@") ?: "user_${user.id.take(8)}"

                profileRepository.ensureProfileExists(
                    userId = user.id,
                    defaultName = name,
                    defaultUsername = username,
                    defaultAvatarUrl = avatar
                )
            } catch (e: Exception) {
                println("AuthRepo: ensureProfileExists failed: ${e.message}")
            }
        }
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
    }.onFailure { e ->
        println("Auth: Sign out failed: ${e.message}")
    }.onSuccess {
        println("Auth: Sign out succeeded")
    }

    override suspend fun getCurrentSession(): String? = auth.currentSessionOrNull()?.user?.id
}
