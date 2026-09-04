package com.example.ui.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.di.AppContainer
import com.example.domain.model.AuthState
import com.example.domain.repository.AuthRepository
import com.example.ui.components.MainScreen
import androidx.compose.material3.MaterialTheme
import com.example.ui.screens.auth.LoginScreen

@Composable
fun AuthGate(
    authRepository: AuthRepository = AppContainer.authRepository
) {
    val authState by authRepository.authState.collectAsStateWithLifecycle(initialValue = AuthState.Loading)

    Crossfade(targetState = authState, label = "AuthGateCrossfade") { state ->
        when (state) {
            is AuthState.Loading -> {
                AuthLoadingScreen()
            }
            is AuthState.Authenticated -> {
                val navController = rememberNavController()
                MainScreen(navController = navController)
            }
            is AuthState.Unauthenticated, is AuthState.Error -> {
                LoginScreen()
            }
        }
    }
}

@Composable
fun AuthLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}
