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

class SupabaseAuthRepositoryImpl(
    private val profileRepository: ProfileRepository = SupabaseProfileRepositoryImpl()
) : AuthRepository {
    private val auth = SupabaseClient.client.auth

    override val authState: Flow<AuthState> = auth.sessionStatus
        .map { status ->
            if (status is SessionStatus.Authenticated) {
                AuthState.Authenticated
            } else if (status is SessionStatus.NotAuthenticated) {
                AuthState.Unauthenticated
            } else {
                if (auth.currentSessionOrNull() != null) AuthState.Authenticated else AuthState.Unauthenticated
            }
        }
        .onStart {
            emit(if (auth.currentSessionOrNull() != null) AuthState.Authenticated else AuthState.Loading)
        }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        auth.signInWith(IDToken) {
            provider = Google
            this.idToken = idToken
        }
        val userId = auth.currentSessionOrNull()?.user?.id
        if (userId != null) {
            try {
                profileRepository.ensureProfileExists(userId)
            } catch (e: Exception) {
                // Ignore profile creation errors if table is not configured
            }
        }
    }

    override suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        auth.signInWith(Google)
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
