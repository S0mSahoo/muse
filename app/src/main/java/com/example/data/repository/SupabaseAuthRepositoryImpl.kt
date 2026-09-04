package com.example.data.repository

import com.example.data.remote.SupabaseClient
import com.example.domain.model.AuthState
import com.example.domain.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SupabaseAuthRepositoryImpl : AuthRepository {
    private val auth = SupabaseClient.client.auth

    override val authState: Flow<AuthState> = auth.sessionStatus
        .map { session ->
            if (session != null) AuthState.Authenticated else AuthState.Unauthenticated
        }
        .onStart { emit(AuthState.Loading) }

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val userId = auth.currentSessionOrNull()?.user?.id
        if (userId != null) {
            SupabaseProfileRepositoryImpl().ensureProfileExists(userId)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        auth.signInWith(IDToken) {
            provider = Google
            this.idToken = idToken
        }
        val userId = auth.currentSessionOrNull()?.user?.id
        if (userId != null) {
            SupabaseProfileRepositoryImpl().ensureProfileExists(userId)
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
