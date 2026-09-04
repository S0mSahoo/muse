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

    val startDestination = when (authState) {
        is AuthState.Authenticated -> "home"
        else -> "login"
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // If we're on login or signup and suddenly authenticated, go home
                if (navController.currentDestination?.route in listOf("login", "signup")) {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                // If we're not on login, go to login
                if (navController.currentDestination?.route != "login") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { 
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onNavigateToHome = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            ) 
        }
        composable("signup") { 
            SignUpScreen(
                onNavigateToHome = { navController.navigate("home") { popUpTo("signup") { inclusive = true } } }
            ) 
        }
        composable("home") { HomeScreen() }
        composable("discover") { DiscoverScreen() }
        composable("library") { LibraryScreen() }
        composable("social") { SocialScreen() }
        composable("profile") { ProfileScreen() }
    }
}
