package com.example.domain.usecase

import com.example.domain.model.User
import com.example.domain.model.UserEntitlements
import com.example.domain.model.UserTier
import com.example.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow

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
