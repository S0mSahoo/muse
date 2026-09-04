package com.example.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.di.AppContainer
import com.example.domain.model.AuthState
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SocialScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.SignUpScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    val authRepository = AppContainer.authRepository
    val authState by authRepository.authState.collectAsState(initial = AuthState.Loading)

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { 
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onNavigateToHome = { } // AuthState will handle navigation
            ) 
        }
        composable("signup") { 
            SignUpScreen(
                onNavigateToHome = { } // AuthState will handle navigation
            ) 
        }
        composable("home") { HomeScreen() }
        composable("discover") { DiscoverScreen() }
        composable("library") { LibraryScreen() }
        composable("social") { SocialScreen() }
        composable("profile") { ProfileScreen() }
    }
}
