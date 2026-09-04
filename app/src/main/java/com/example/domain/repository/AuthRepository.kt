package com.example.domain.repository

import com.example.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentSession(): String?
}
