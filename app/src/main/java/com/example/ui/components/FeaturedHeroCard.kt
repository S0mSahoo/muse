package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Playlist
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.MuseVioletLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FeaturedHeroCard(
    playlist: Playlist,
    onPlayClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        // Artwork Background
        MusicArtworkImage(
            artworkUrl = playlist.artworkUrl,
            contentDescription = playlist.title,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(22.dp),
            fallbackGradientColors = playlist.gradientColors
        )

        // Gradient Dark Overlays for legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x2208080C),
                            Color(0x8008080C),
                            Color(0xF508080C)
                        )
                    )
                )
        )

        // Subtle Ambient Glow Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x338B5CF6),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC181924))
                    .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MuseVioletLight,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "FEATURED MIX",
                    style = MaterialTheme.typography.labelSmall,
                    color = MuseVioletLight,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bottom Area: Title, Description, and Play FAB
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = playlist.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "${playlist.trackCount} tracks • ${playlist.curator}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MuseVioletLight.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // Play Button
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(MuseViolet, Color(0xFF6366F1))
                            )
                        ),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play ${playlist.title}",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
