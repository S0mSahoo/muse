package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.PlaybackState
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.MuseCoral
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MiniPlayer(
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleLike: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playbackState.currentTrack

    AnimatedVisibility(
        visible = track != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 260)
        ) + fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 220)
        ) + fadeOut(animationSpec = tween(durationMillis = 180)),
        modifier = modifier
    ) {
        if (track == null) return@AnimatedVisibility

        val dominantColor = Color(track.dominantColorHex)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceElevated.copy(alpha = 0.96f))
                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = MuseViolet),
                    role = Role.Button,
                    onClick = onClick
                )
        ) {
            // Ambient track glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                dominantColor.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Artwork
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    MusicArtworkImage(
                        artworkUrl = track.artworkUrl,
                        contentDescription = "Album art for ${track.title}",
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(10.dp),
                        fallbackGradientColors = listOf(track.dominantColorHex, 0xFF08080C)
                    )
                }

                // Track Info with Animated Transition
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = track.title,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith
                                    fadeOut(animationSpec = tween(150))
                        },
                        label = "miniPlayerTitle"
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
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
                        label = "miniPlayerArtist"
                    ) { artist ->
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }

                // Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Like Button with scale pop
                    val likeScale by animateFloatAsState(
                        targetValue = if (track.isLiked) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "miniLikeScale"
                    )

                    IconButton(
                        onClick = { onToggleLike(track.id) },
                        modifier = Modifier
                            .size(40.dp)
                            .scale(likeScale)
                    ) {
                        Icon(
                            imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (track.isLiked) "Unlike ${track.title}" else "Like ${track.title}",
                            tint = if (track.isLiked) MuseCoral else TextSecondary.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Play / Pause Button with subtle scale transition
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MuseViolet),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        if (playbackState.isBuffering) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Next Button
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next track",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Progress bar at the bottom
            val progressFraction = if (playbackState.durationMs > 0) {
                (playbackState.progressMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0x1AFFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(MuseViolet, Color(0xFFA78BFA))
                            )
                        )
                )
            }
        }
    }
}
