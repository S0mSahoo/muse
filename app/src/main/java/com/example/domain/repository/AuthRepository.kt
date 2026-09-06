package com.example.domain.repository

import com.example.domain.model.AuthState
import com.example.domain.model.GuestSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signInAsGuest(name: String, age: Int, country: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentSession(): String?
    suspend fun getCurrentUserEmail(): String?
    fun getGuestSession(): GuestSession?
}
