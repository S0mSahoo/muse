package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GuestSession(
    val id: String,
    val name: String,
    val age: Int,
    val country: String,
    val createdAt: Long
)
