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
        println("Auth: Attempting Google sign-in")
        auth.signInWith(IDToken) {
            provider = Google
            this.idToken = idToken
        }
        val user = auth.currentSessionOrNull()?.user
        if (user != null) {
            println("Auth: Sign-in successful for user: ${user.id}")
            try {
                val rawName = user.userMetadata?.get("full_name") ?: user.userMetadata?.get("name")
                val rawAvatar = user.userMetadata?.get("avatar_url") ?: user.userMetadata?.get("picture")
                val rawUsername = user.userMetadata?.get("preferred_username")

                val name = (rawName as? JsonPrimitive)?.contentOrNull
                val avatar = (rawAvatar as? JsonPrimitive)?.contentOrNull
                val username = (rawUsername as? JsonPrimitive)?.contentOrNull

                println("Auth: Ensuring profile exists for user: ${user.id}")
                profileRepository.ensureProfileExists(
                    userId = user.id,
                    defaultName = name,
                    defaultUsername = username,
                    defaultAvatarUrl = avatar
                )
                println("Auth: Profile check/creation complete")
            } catch (e: Exception) {
                println("AuthRepo: ensureProfileExists failed for user ${user.id}: ${e.message}")
                throw e
            }
        } else {
            println("Auth: Sign-in succeeded, but no user found in session")
            throw Exception("No user found after sign-in")
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
    override suspend fun getCurrentUserEmail(): String? = auth.currentSessionOrNull()?.user?.email
}
