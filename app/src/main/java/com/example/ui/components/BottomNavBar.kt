package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.MuseVioletLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

enum class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    DISCOVER("discover", "Discover", Icons.Filled.Explore, Icons.Outlined.Explore),
    LIBRARY("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    SOCIAL("social", "Social", Icons.Filled.People, Icons.Outlined.People),
    PROFILE("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.98f))
            .border(
                width = 0.75.dp,
                color = BorderSubtle,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem.entries.forEach { item ->
                val isSelected = currentRoute == item.route

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) MuseVioletLight else TextMuted,
                    animationSpec = tween(durationMillis = 180),
                    label = "navIconTint"
                )

                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) MuseVioletLight else TextSecondary.copy(alpha = 0.7f),
                    animationSpec = tween(durationMillis = 180),
                    label = "navLabelColor"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = 28.dp, color = MuseViolet),
                            role = Role.Tab,
                            onClick = { onNavigate(item.route) }
                        )
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) MuseViolet.copy(alpha = 0.16f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = labelColor,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
