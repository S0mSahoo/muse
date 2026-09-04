package com.example.domain.repository

import com.example.domain.model.DailyAdState
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserEntitlements
import kotlinx.coroutines.flow.Flow

interface EntitlementRepository {
    fun getEntitlements(): Flow<UserEntitlements>
    fun getDailyAdState(): Flow<DailyAdState>
    fun recordAdCompletion()
    fun setSubscriptionTier(tier: SubscriptionTier)
}
