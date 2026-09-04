package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.ui.components.AlbumCard
import com.example.ui.components.ArtistCard
import com.example.ui.components.DailyMixCard
import com.example.ui.components.FeaturedHeroCard
import com.example.ui.components.MusicArtworkImage
import com.example.ui.components.MusicSection
import com.example.ui.components.PlaylistCard
import com.example.ui.components.QuickAccessRow
import com.example.ui.components.SectionHeader
import com.example.ui.components.TrackRow
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 148.dp) // Clearance for MiniPlayer + Bottom Navigation
        ) {
            // 1. Top Header: Personalized Greeting & Profile
            item {
                HomeHeader(
                    greeting = uiState.greeting,
                    subtitle = uiState.subtitle,
                    avatarUrl = uiState.user.avatarUrl,
                    modifier = Modifier.statusBarsPadding()
                )
            }

            // 2. Quick Access Bar
            item {
                QuickAccessRow(
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                    onItemClick = { item ->
                        viewModel.onFilterSelected(item.id)
                    }
                )
            }

            // 3. Featured Hero Experience ("Made for you" Anchor)
            uiState.featuredPlaylist?.let { heroPlaylist ->
                item {
                    FeaturedHeroCard(
                        playlist = heroPlaylist,
                        onPlayClick = { viewModel.playPlaylist(heroPlaylist) },
                        onClick = { viewModel.playPlaylist(heroPlaylist) },
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                }
            }

            // 4. Daily Mixes Section
            if (uiState.dailyMixes.isNotEmpty()) {
                item {
                    MusicSection(
                        title = "Your Daily Mixes",
                        subtitle = "Familiar favorites with a few discoveries",
                        items = uiState.dailyMixes
                    ) { mix ->
                        DailyMixCard(
                            dailyMix = mix,
                            onPlayClick = { viewModel.playDailyMix(mix) },
                            onClick = { viewModel.playDailyMix(mix) }
                        )
                    }
                }
            }

            // 5. Recently Played Tracks
            if (uiState.recentlyPlayedTracks.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        SectionHeader(
                            title = "Recently Played",
                            subtitle = "Pick up right where you left off"
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            items(uiState.recentlyPlayedTracks.take(6)) { track ->
                                CompactRecentTrackCard(
                                    track = track,
                                    onClick = { viewModel.playTrack(track, uiState.recentlyPlayedTracks) }
                                )
                            }
                        }
                    }
                }
            }

            // 6. Made For You Playlists
            if (uiState.madeForYouPlaylists.isNotEmpty()) {
                item {
                    MusicSection(
                        title = "Made For You",
                        subtitle = "Curated automatically from your listening patterns",
                        items = uiState.madeForYouPlaylists
                    ) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { viewModel.playPlaylist(playlist) },
                            onPlayClick = { viewModel.playPlaylist(playlist) }
                        )
                    }
                }
            }

            // 7. Fresh Discoveries ("Songs you probably haven't heard yet")
            if (uiState.freshDiscoveries.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        SectionHeader(
                            title = "Fresh Discoveries",
                            subtitle = "Songs you probably haven't heard yet"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            uiState.freshDiscoveries.forEach { track ->
                                TrackRow(
                                    track = track,
                                    isPlaying = false,
                                    onTrackClick = { viewModel.playTrack(track, uiState.freshDiscoveries) },
                                    onLikeClick = { viewModel.toggleLike(track.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 8. Because You Listened To [Artist]
            if (uiState.becauseYouListenedArtist != null && uiState.becauseYouListenedTracks.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        SectionHeader(
                            title = "Because You Listened To",
                            subtitle = uiState.becauseYouListenedArtist?.name ?: ""
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            uiState.becauseYouListenedTracks.forEach { track ->
                                TrackRow(
                                    track = track,
                                    isPlaying = false,
                                    onTrackClick = { viewModel.playTrack(track, uiState.becauseYouListenedTracks) },
                                    onLikeClick = { viewModel.toggleLike(track.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 9. Recommended Artists
            if (uiState.recommendedArtists.isNotEmpty()) {
                item {
                    MusicSection(
                        title = "Recommended Artists",
                        subtitle = "Musicians expanding your horizons",
                        items = uiState.recommendedArtists
                    ) { artist ->
                        ArtistCard(
                            artist = artist,
                            onClick = { /* Open Artist Profile */ }
                        )
                    }
                }
            }

            // 10. New Releases & Albums
            if (uiState.recommendedAlbums.isNotEmpty()) {
                item {
                    MusicSection(
                        title = "New Releases & Albums",
                        subtitle = "Freshly mastered full-length projects",
                        items = uiState.recommendedAlbums
                    ) { album ->
                        AlbumCard(
                            album = album,
                            onClick = { /* Open Album */ }
                        )
                    }
                }
            }

            // Extra bottom spacing to ensure clean clearance
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    subtitle: String,
    avatarUrl: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { /* Notifications */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // User Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, BorderSubtle, CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(300)
                            .build(),
                        contentDescription = "User profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactRecentTrackCard(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MusicArtworkImage(
            artworkUrl = track.artworkUrl,
            contentDescription = track.title,
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            fallbackGradientColors = listOf(track.dominantColorHex, 0xFF08080C)
        )

        Column(modifier = Modifier.padding(end = 4.dp)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}
