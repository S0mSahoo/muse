package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PlaybackState
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseCoral
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.MuseVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    playbackState: PlaybackState,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    val track = playbackState.currentTrack ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showOptionsMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        val dominantColor = Color(track.dominantColorHex)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.38f),
                            BackgroundDark.copy(alpha = 0.95f),
                            BackgroundDark
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dismiss Now Playing",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLAYING FROM PLAYLIST",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = track.album.ifEmpty { "MUSE Curations" },
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Playback Options",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Album Artwork
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
                ) {
                    MusicArtworkImage(
                        artworkUrl = track.artworkUrl,
                        contentDescription = "Artwork for ${track.title}",
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(22.dp),
                        fallbackGradientColors = listOf(track.dominantColorHex, 0xFF08080C)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title, Artist, and Like Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = track.title,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith
                                        fadeOut(animationSpec = tween(150))
                            },
                            label = "nowPlayingTitle"
                        ) { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        AnimatedContent(
                            targetState = track.artist,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith
                                        fadeOut(animationSpec = tween(150))
                            },
                            label = "nowPlayingArtist"
                        ) { artist ->
                            Text(
                                text = artist,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    val likeScale by animateFloatAsState(
                        targetValue = if (track.isLiked) 1.2f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "nowPlayingLikeScale"
                    )

                    IconButton(
                        onClick = { onToggleLike(track.id) },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(likeScale)
                    ) {
                        Icon(
                            imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (track.isLiked) "Unlike ${track.title}" else "Like ${track.title}",
                            tint = if (track.isLiked) MuseCoral else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrubber Slider
                var sliderPosition by remember(playbackState.progressMs) {
                    mutableFloatStateOf(playbackState.progressMs.toFloat())
                }

                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = { onSeekTo(sliderPosition.toLong()) },
                    valueRange = 0f..playbackState.durationMs.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = MuseViolet,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeMs(sliderPosition.toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = formatTimeMs(playbackState.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main Controls (Shuffle, Prev, Play, Next, Repeat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = if (playbackState.isShuffle) "Shuffle On" else "Shuffle Off",
                            tint = if (playbackState.isShuffle) MuseVioletLight else TextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Play/Pause Center Button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MuseViolet, Color(0xFF6366F1))
                                )
                            )
                            .clickable(onClick = onTogglePlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (playbackState.isRepeat) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = if (playbackState.isRepeat) "Repeat One" else "Repeat Off",
                            tint = if (playbackState.isRepeat) MuseVioletLight else TextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Quality and Output Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Honest, development-safe audio representation
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MuseVioletLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "MUSE Audio Core • Stereo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MuseVioletLight,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Audio Output",
                            tint = TextSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Internal Stereo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
