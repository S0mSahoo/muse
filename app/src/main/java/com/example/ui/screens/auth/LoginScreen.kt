package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.di.AppContainer
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isGuestSetup by remember { mutableStateOf(false) }

    // Guest setup form state
    var guestName by remember { mutableStateOf("") }
    var guestAge by remember { mutableStateOf("") }
    var guestCountry by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val authRepository = AppContainer.authRepository
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Hero Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MuseViolet, MuseVioletLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "MUSE Logo",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to MUSE",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = if (isGuestSetup) "Set up your guest profile to get started" else "Sign in with Google or continue as guest",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Form Card Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isGuestSetup) {
                    // Google Sign-In Button
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                        .setAutoSelectEnabled(false)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(context, request)
                                    val credential = result.credential
                                    if (credential is androidx.credentials.CustomCredential &&
                                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    ) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val idToken = googleIdTokenCredential.idToken
                                        val signInResult = authRepository.signInWithGoogle(idToken)
                                        if (signInResult.isFailure) {
                                            errorMessage = signInResult.exceptionOrNull()?.message ?: "Google sign-in failed"
                                        }
                                    } else {
                                        errorMessage = "Unexpected credential type"
                                    }
                                } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                                    errorMessage = null
                                } catch (e: Exception) {
                                    if (e.message?.contains("cancelled", ignoreCase = true) == true) {
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Authentication failed: ${e.message}"
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoading) "Signing in..." else "Continue with Google",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Continue as Guest Button
                    OutlinedButton(
                        onClick = {
                            isGuestSetup = true
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Continue as Guest",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                } else {
                    // Guest Setup Form
                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { guestName = it },
                        label = { Text("Name (Required)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = guestAge,
                        onValueChange = { guestAge = it },
                        label = { Text("Age (13+ Required)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = guestCountry,
                        onValueChange = { guestCountry = it },
                        label = { Text("Country (Required)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (guestName.isBlank()) {
                                errorMessage = "Name is required"
                                return@Button
                            }
                            val ageInt = guestAge.toIntOrNull()
                            if (ageInt == null || ageInt < 13) {
                                errorMessage = "Age must be a sensible value (13+)"
                                return@Button
                            }
                            if (guestCountry.isBlank()) {
                                errorMessage = "Country is required"
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val result = authRepository.signInAsGuest(guestName.trim(), ageInt, guestCountry.trim())
                                    if (result.isFailure) {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Guest sign-in failed"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MuseViolet),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoading) "Setting up..." else "Continue",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            isGuestSetup = false
                            errorMessage = null
                        },
                        enabled = !isLoading
                    ) {
                        Text("Back to Sign In", color = TextSecondary)
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
