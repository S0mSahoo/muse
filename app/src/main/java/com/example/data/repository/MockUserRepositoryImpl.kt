package com.example.data.repository

import com.example.data.local.LocalDataStore
import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockUserRepositoryImpl(
    private val localDataStore: LocalDataStore
) : UserRepository {

    override fun getCurrentUser(): Flow<User> = localDataStore.userProfile

    override fun updateProfile(name: String, handle: String): Flow<User> = flow {
        localDataStore.updateUserProfile(name, handle)
        emit(localDataStore.userProfile.value)
    }
}

