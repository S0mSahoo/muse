package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.Track
import com.example.ui.theme.MuseCoral
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TrackRow(
    track: Track,
    isPlaying: Boolean,
    onTrackClick: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTrackClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            MusicArtworkImage(
                artworkUrl = track.artworkUrl,
                contentDescription = track.title,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp)
            )

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x998B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Title and Artist
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isPlaying) MuseViolet else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Like Button
        IconButton(
            onClick = onLikeClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (track.isLiked) "Unlike" else "Like",
                tint = if (track.isLiked) MuseCoral else TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }

        // More options button
        IconButton(
            onClick = { /* More options sheet */ },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
