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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.di.AppContainer
import com.example.domain.model.Track
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseCyan
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.MuseVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class FriendListeningActivity(
    val friendName: String,
    val track: Track,
    val timeAgo: String,
    val isLive: Boolean = false
)

@Composable
fun SocialScreen(
    modifier: Modifier = Modifier
) {
    val musicRepository = AppContainer.musicRepository
    val playbackRepository = AppContainer.playbackRepository

    val sampleTracks by musicRepository.getRecentlyPlayedTracks().collectAsStateWithLifecycle(initialValue = emptyList())
    val activities = remember(sampleTracks) {
        if (sampleTracks.isNotEmpty()) {
            listOfNotNull(
                sampleTracks.getOrNull(0)?.let { FriendListeningActivity("Aarav", it, "Listening now", isLive = true) },
                sampleTracks.getOrNull(1)?.let { FriendListeningActivity("Maya", it, "12m ago") },
                sampleTracks.getOrNull(4)?.let { FriendListeningActivity("Rohan", it, "1h ago") },
                sampleTracks.getOrNull(5)?.let { FriendListeningActivity("Elena", it, "3h ago") },
                sampleTracks.getOrNull(2)?.let { FriendListeningActivity("Liam", it, "5h ago") }
            )
        } else emptyList()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 148.dp) // Clearance for MiniPlayer + Bottom Navigation
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "Community",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Demo Sound Feed • Listen along with tastemakers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            items(activities) { activity ->
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
                            onClick = {
                                playbackRepository.playTrack(activity.track, sampleTracks)
                            }
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
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

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = activity.friendName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            if (activity.isLive) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Live",
                                    tint = MuseCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "${activity.track.title} • ${activity.track.artist}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = activity.timeAgo,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (activity.isLive) MuseCyan else TextMuted
                        )

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Listen along",
                            tint = if (activity.isLive) MuseCyan else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
