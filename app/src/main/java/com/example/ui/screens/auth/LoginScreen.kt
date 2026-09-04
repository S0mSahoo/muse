package com.example.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.di.AppContainer
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onNavigateToSignUp: () -> Unit, onNavigateToHome: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authRepository = AppContainer.authRepository

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    val result = authRepository.signIn(email, password)
                    isLoading = false
                    if (result.isSuccess) {
                        onNavigateToHome()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Signing in..." else "Sign In")
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        TextButton(onClick = onNavigateToSignUp) { Text("Create Account") }
    }
}
