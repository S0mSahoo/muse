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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.di.AppContainer
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Playlist
import com.example.ui.components.MusicArtworkImage
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseCoral
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.MuseVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier
) {
    val musicRepository = AppContainer.musicRepository
    val playbackRepository = AppContainer.playbackRepository

    val likedTracks by musicRepository.getLikedTracks().collectAsStateWithLifecycle(initialValue = emptyList())
    val basePlaylists by musicRepository.getMadeForYouPlaylists().collectAsStateWithLifecycle(initialValue = emptyList())
    val sampleArtists by musicRepository.getRecommendedArtists().collectAsStateWithLifecycle(initialValue = emptyList())
    val sampleAlbums by musicRepository.getRecommendedAlbums().collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedFilter by remember { mutableStateOf("all") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var customPlaylists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    val userPlaylists = remember(basePlaylists, customPlaylists) {
        customPlaylists + basePlaylists
    }

    val filterOptions = listOf(
        "all" to "All",
        "playlists" to "Playlists",
        "artists" to "Artists",
        "albums" to "Albums"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 148.dp) // Clearance for MiniPlayer + Navigation
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
                    Column {
                        Text(
                            text = "Your Library",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Saved music & custom collections",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MuseViolet)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Playlist",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MuseViolet.copy(alpha = 0.2f) else SurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) MuseVioletLight else BorderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedFilter = key }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MuseVioletLight else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 1. Liked Songs Card (Always visible on 'all' or 'playlists')
            if (selectedFilter == "all" || selectedFilter == "playlists") {
                item {
                    val count = likedTracks.size
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MuseCoral.copy(alpha = 0.15f),
                                        SurfaceCard
                                    )
                                )
                            )
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = MuseCoral),
                                role = Role.Button,
                                onClick = {
                                    if (likedTracks.isNotEmpty()) {
                                        val likedPlaylist = Playlist(
                                            id = "pl_liked_auto",
                                            title = "Liked Songs",
                                            curator = "You",
                                            trackCount = likedTracks.size,
                                            tracks = likedTracks
                                        )
                                        playbackRepository.playPlaylist(likedPlaylist)
                                    }
                                }
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(MuseCoral, Color(0xFFFF8099))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Liked Songs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Auto collection • $count songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Liked Songs",
                            tint = MuseCoral,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 2. Playlists List
            if (selectedFilter == "all" || selectedFilter == "playlists") {
                items(userPlaylists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = MuseViolet),
                                role = Role.Button,
                                onClick = { playbackRepository.playPlaylist(playlist) }
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            MusicArtworkImage(
                                artworkUrl = playlist.artworkUrl,
                                contentDescription = playlist.title,
                                modifier = Modifier.size(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                fallbackGradientColors = playlist.gradientColors
                            )

                            Column {
                                Text(
                                    text = playlist.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Playlist • ${playlist.trackCount} tracks • ${playlist.curator}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 3. Artists List (if filter is artists)
            if (selectedFilter == "artists") {
                items(sampleArtists) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MuseVioletLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${artist.genres.joinToString(" • ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // 4. Albums List (if filter is albums)
            if (selectedFilter == "albums") {
                items(sampleAlbums) { album ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        MusicArtworkImage(
                            artworkUrl = album.artworkUrl,
                            contentDescription = album.title,
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Column {
                            Text(
                                text = album.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Album • ${album.artist} • ${album.releaseYear}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Create Playlist Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = SurfaceElevated,
                title = {
                    Text(
                        text = "Create Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Give your new collection a memorable name.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            placeholder = { Text("e.g. Midnight Highway Drives", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MuseVioletLight,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                val created = Playlist(
                                    id = "pl_custom_${System.currentTimeMillis()}",
                                    title = newPlaylistName.trim(),
                                    curator = "You",
                                    trackCount = 0,
                                    gradientColors = listOf(0xFF8B5CF6, 0xFF3B82F6)
                                )
                                customPlaylists = listOf(created) + customPlaylists
                                newPlaylistName = ""
                                showCreateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MuseViolet)
                    ) {
                        Text("Create", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
