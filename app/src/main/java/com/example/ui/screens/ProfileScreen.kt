package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.example.domain.model.UserProfile
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import coil.request.ImageRequest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.di.AppContainer
import com.example.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import com.example.domain.model.User
import com.example.domain.model.UserTier
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseCoral
import com.example.ui.theme.MuseCyan
import com.example.ui.theme.MuseEmerald
import com.example.ui.theme.MuseIndigo
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.MuseVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val authRepository = AppContainer.authRepository
    val profileRepository = AppContainer.profileRepository
    val musicRepository = AppContainer.musicRepository
    
    val authUserEmail = com.example.data.remote.SupabaseClient.client.auth.currentSessionOrNull()?.user?.email
    val userIdState = remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        userIdState.value = authRepository.getCurrentSession()
    }
    val userId = userIdState.value ?: ""
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                profileRepository.ensureProfileExists(userId)
                userProfile = profileRepository.getProfile(userId)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    val likedTracks by musicRepository.getLikedTracks().collectAsStateWithLifecycle(initialValue = emptyList())
    val userPlaylists by AppContainer.localDataStore.userPlaylists.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedInfoDialog by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 148.dp) // Clearance for MiniPlayer + Bottom Navigation
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = { selectedInfoDialog = "settings" },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // User Identity Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .border(2.dp, MuseViolet, CircleShape)
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userProfile?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop&q=80")
                                .crossfade(300)
                                .build(),
                            contentDescription = "Profile picture for ${userProfile?.displayName ?: "User"}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = userProfile?.displayName?.takeIf { !it.isBlank() } 
                            ?: authUserEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() } 
                            ?: (if (isLoading) "Loading..." else "MUSE User"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Text(
                        text = userProfile?.username?.takeIf { !it.isBlank() } 
                            ?: authUserEmail 
                            ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    val dobText = userProfile?.dob
                    val genderText = userProfile?.gender
                    if (!dobText.isNullOrBlank() || !genderText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = listOfNotNull(dobText, genderText).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Tier Status Pill
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MuseViolet.copy(alpha = 0.15f))
                            .border(1.dp, MuseViolet.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        val tierTitle = "MUSE Standard Tier"
                        Text(
                            text = tierTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MuseVioletLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceElevated)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(label = "Playlists", count = "${userPlaylists.size}")
                        ProfileStatItem(label = "Liked Tracks", count = "${likedTracks.size}")
                        ProfileStatItem(label = "Artists", count = "0")
                    }
                }
            }

            // Top Listening Genres Vibe
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Your Sonic Signature",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Based on your recent listening habits",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Segmented color bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(modifier = Modifier.weight(0.42f).fillMaxHeight().background(MuseViolet))
                        Box(modifier = Modifier.weight(0.28f).fillMaxHeight().background(MuseIndigo))
                        Box(modifier = Modifier.weight(0.18f).fillMaxHeight().background(MuseCyan))
                        Box(modifier = Modifier.weight(0.12f).fillMaxHeight().background(Color(0xFFF59E0B)))
                    }

                    // Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GenreLegendItem(color = MuseViolet, name = "Synthwave", percent = "42%")
                        GenreLegendItem(color = MuseIndigo, name = "Ambient", percent = "28%")
                        GenreLegendItem(color = MuseCyan, name = "Nu-Disco", percent = "18%")
                        GenreLegendItem(color = Color(0xFFF59E0B), name = "Acoustic", percent = "12%")
                    }
                }
            }

            // Options List
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileOptionItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your display name, username, and photo",
                        onClick = { selectedInfoDialog = "edit_profile" }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Diamond,
                        title = "MUSE Premium",
                        subtitle = "Explore premium listening features",
                        onClick = { selectedInfoDialog = "premium" }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.GraphicEq,
                        title = "Audio Engine & Equalizer",
                        subtitle = "Output preferences and listening controls",
                        onClick = { selectedInfoDialog = "audio" }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Storage,
                        title = "Storage & Cache",
                        subtitle = "Manage temporary storage and local data",
                        onClick = { selectedInfoDialog = "storage" }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Security,
                        title = "Privacy & Listening Data",
                        subtitle = "On-device profile state and preferences",
                        onClick = { selectedInfoDialog = "privacy" }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Logout,
                        title = "Sign Out",
                        subtitle = "Sign out of your MUSE account",
                        onClick = {
                            scope.launch {
                                authRepository.signOut()
                            }
                        }
                    )
                }
            }
        }

        // Info Dialog
        if (selectedInfoDialog != null) {
            val (title, body) = when (selectedInfoDialog) {
                "edit_profile" -> Pair(
                    "Edit Profile",
                    "Update your profile"
                )
                "premium" -> Pair(
                    "MUSE Premium",
                    "MUSE Premium is designed to offer expanded capabilities such as custom sound profiles, enhanced playback options, and seamless listening."
                )
                "audio" -> Pair(
                    "Audio Engine & Equalizer",
                    "Playback is powered by the MUSE Audio Core with stereo rendering and responsive audio routing."
                )
                "storage" -> Pair(
                    "Storage & Cache",
                    "Application cache and listening history are safely stored on your local device."
                )
                "privacy" -> Pair(
                    "Privacy & Listening Data",
                    "MUSE respects your listening privacy. Preferences and playback state remain stored locally."
                )
                else -> Pair(
                    "MUSE Settings",
                    "MUSE Music Application version 1.2.0.\nDesigned for immersive sound and minimal distraction."
                )
            }

            AlertDialog(
                onDismissRequest = { selectedInfoDialog = null },
                containerColor = SurfaceElevated,
                title = {
                    Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                },
                text = {
                    if (selectedInfoDialog == "edit_profile") {
                        Column {
                            var displayName by remember { mutableStateOf(userProfile?.displayName ?: "") }
                            var username by remember { mutableStateOf(userProfile?.username ?: "") }
                            var avatarUrl by remember { mutableStateOf(userProfile?.avatarUrl ?: "") }

                            androidx.compose.material3.OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Display Name") })
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedTextField(value = avatarUrl, onValueChange = { avatarUrl = it }, label = { Text("Avatar URL") })
                            
                            Button(onClick = {
                                scope.launch {
                                    val updatedProfile = userProfile?.copy(displayName = displayName, username = username, avatarUrl = avatarUrl)
                                    if (updatedProfile != null) {
                                        profileRepository.updateProfile(updatedProfile)
                                        userProfile = updatedProfile
                                        selectedInfoDialog = null
                                    }
                                }
                            }) { Text("Save") }
                        }
                    } else {
                        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                },
                confirmButton = {
                    if (selectedInfoDialog != "edit_profile") {
                        Button(
                            onClick = { selectedInfoDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MuseViolet)
                        ) {
                            Text("Got it", color = Color.White)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileStatItem(
    label: String,
    count: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun GenreLegendItem(
    color: Color,
    name: String,
    percent: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$name $percent",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = TextSecondary
        )
    }
}

@Composable
private fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = MuseViolet),
                role = Role.Button,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MuseVioletLight,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
