package com.example.domain.model

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Guest : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
