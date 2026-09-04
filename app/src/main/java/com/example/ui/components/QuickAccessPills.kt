package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseCoral
import com.example.ui.theme.MuseCyan
import com.example.ui.theme.MuseEmerald
import com.example.ui.theme.MuseIndigo
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary

data class QuickAccessItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun QuickAccessRow(
    modifier: Modifier = Modifier,
    onItemClick: (QuickAccessItem) -> Unit = {}
) {
    val items = listOf(
        QuickAccessItem("liked", "Liked Songs", Icons.Default.Favorite, MuseCoral),
        QuickAccessItem("daily_mix", "Daily Mix", Icons.Default.Shuffle, MuseViolet),
        QuickAccessItem("recent", "Recently Played", Icons.Default.History, MuseCyan),
        QuickAccessItem("chill", "Late Night", Icons.Default.Nightlight, MuseIndigo),
        QuickAccessItem("focus", "Deep Focus", Icons.Default.SelfImprovement, MuseEmerald),
        QuickAccessItem("discover", "Fresh Pulse", Icons.Default.ElectricBolt, Color(0xFFF59E0B))
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.id }) { item ->
            QuickAccessChip(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun QuickAccessChip(
    item: QuickAccessItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard.copy(alpha = 0.85f))
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary
        )
    }
}
