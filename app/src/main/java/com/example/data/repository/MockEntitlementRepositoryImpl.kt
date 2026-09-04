package com.example.data.repository

import com.example.data.local.LocalDataStore
import com.example.domain.model.DailyAdState
import com.example.domain.model.UserEntitlements
import com.example.domain.model.UserTier
import com.example.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockEntitlementRepositoryImpl(
    private val localDataStore: LocalDataStore
) : EntitlementRepository {

    private val _adState = MutableStateFlow(DailyAdState(date = "2026-09-04", adShown = false, adCompleted = false))

    override fun getEntitlements(): Flow<UserEntitlements> = localDataStore.userEntitlements

    override fun getDailyAdState(): Flow<DailyAdState> = _adState.asStateFlow()

    override fun recordAdCompletion() {
        _adState.value = _adState.value.copy(adShown = true, adCompleted = true)
    }

    override fun setSubscriptionTier(tier: UserTier) {
        localDataStore.setSubscriptionTier(tier)
    }
}

