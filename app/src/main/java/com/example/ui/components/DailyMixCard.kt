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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.domain.model.DailyMix
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DailyMixCard(
    dailyMix: DailyMix,
    onPlayClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(dailyMix.dominantColorHex)

    Column(
        modifier = modifier
            .width(165.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Artwork & Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            MusicArtworkImage(
                artworkUrl = dailyMix.artworkUrl,
                contentDescription = dailyMix.title,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                fallbackGradientColors = listOf(dailyMix.dominantColorHex, 0xFF08080C)
            )

            // Gradient Shade
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x6608080C),
                                Color(0xCC08080C)
                            )
                        )
                    )
            )

            // Top Badge
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.9f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "MIX 0${dailyMix.mixNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            // Play Floating Button
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.BottomEnd)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play ${dailyMix.title}",
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }
        }

        // Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = dailyMix.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = dailyMix.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}
