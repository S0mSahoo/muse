package com.example.data.repository

import com.example.data.remote.SupabaseClient
import com.example.domain.model.AuthState
import com.example.domain.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SupabaseAuthRepositoryImpl : AuthRepository {
    private val auth = SupabaseClient.client.auth

    init {
        // Clear any persisted session on app startup to prevent bypassing login state
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signOut()
                com.example.di.AppContainer.localDataStore.clearProfile()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override val authState: Flow<AuthState> = auth.sessionStatus
        .map { session ->
            if (session != null) {
                updateProfileFromCurrentSession()
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
        .onStart { emit(AuthState.Loading) }

    private fun updateProfileFromCurrentSession() {
        val currentUser = auth.currentSessionOrNull()?.user
        val email = currentUser?.email ?: ""
        val name = if (email.isNotEmpty()) email.substringBefore("@") else "User"
        val handle = if (email.isNotEmpty()) "@${email.substringBefore("@")}" else "@user"
        com.example.di.AppContainer.localDataStore.updateUserProfile(name, handle)
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        updateProfileFromCurrentSession()
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        updateProfileFromCurrentSession()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
        com.example.di.AppContainer.localDataStore.clearProfile()
    }.onFailure { e ->
        println("Auth: Sign out failed: ${e.message}")
    }.onSuccess {
        println("Auth: Sign out succeeded")
    }

    override suspend fun getCurrentSession(): String? = auth.currentSessionOrNull()?.user?.id
}
