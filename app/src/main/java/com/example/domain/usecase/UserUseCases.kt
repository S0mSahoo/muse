package com.example.domain.usecase

import com.example.domain.model.User
import com.example.domain.model.UserEntitlements
import com.example.domain.model.UserTier
import com.example.domain.repository.EntitlementRepository
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetUserProfileUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): Flow<User> {
        return userRepository.getCurrentUser()
    }
}

class UpdateUserProfileUseCase(private val userRepository: UserRepository) {
    operator fun invoke(name: String, handle: String): Flow<User> {
        return userRepository.updateProfile(name, handle)
    }
}

class GetEntitlementsUseCase(private val entitlementRepository: EntitlementRepository) {
    operator fun invoke(): Flow<UserEntitlements> {
        return entitlementRepository.getEntitlements()
    }
}

class SetSubscriptionTierUseCase(private val entitlementRepository: EntitlementRepository) {
    operator fun invoke(tier: UserTier) {
        entitlementRepository.setSubscriptionTier(tier)
    }
}
