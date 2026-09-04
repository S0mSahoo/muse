package com.example.domain.repository

import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User>
    fun updateProfile(name: String, handle: String): Flow<User>
}
