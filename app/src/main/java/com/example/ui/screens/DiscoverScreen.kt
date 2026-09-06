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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.di.AppContainer
import com.example.domain.model.Track
import com.example.ui.components.PlaylistCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.TrackRow
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MuseCyan
import com.example.ui.theme.MuseEmerald
import com.example.ui.theme.MuseIndigo
import com.example.ui.theme.MuseViolet
import com.example.ui.theme.MuseVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BrowseCategory(
    val title: String,
    val subtitle: String,
    val query: String,
    val gradientColors: List<Long>
)

data class MoodItem(
    val id: String,
    val title: String,
    val query: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun DiscoverScreen(
    modifier: Modifier = Modifier
) {
    val musicRepository = AppContainer.musicRepository
    val playbackRepository = AppContainer.playbackRepository
    val scope = rememberCoroutineScope()

    val playlists by musicRepository.getMadeForYouPlaylists().collectAsStateWithLifecycle(initialValue = emptyList())
    val featuredPlaylist by musicRepository.getFeaturedPlaylist().collectAsStateWithLifecycle(initialValue = null)
    val playbackState by playbackRepository.playbackState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMoodId by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }

    // Debounced search logic
    LaunchedEffect(searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            searchResults = emptyList()
            isSearching = false
        } else {
            isSearching = true
            delay(300) // 300ms debounce
            try {
                val results = musicRepository.search(query)
                searchResults = results
            } catch (e: Exception) {
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        }
    }

    val moods = remember {
        listOf(
            MoodItem("focus", "Deep Focus & Flow", "Ambient Focus", Icons.Default.SelfImprovement, MuseEmerald),
            MoodItem("night", "Late Night Reverie", "Synthwave Night", Icons.Default.Nightlight, MuseIndigo),
            MoodItem("energy", "High Energy Beats", "Electronic Dance", Icons.Default.ElectricBolt, Color(0xFFF59E0B)),
            MoodItem("morning", "Sunrise Awakening", "Acoustic Morning", Icons.Default.WbSunny, MuseCyan),
            MoodItem("immersive", "Immersive Listening", "Spatial Soundscapes", Icons.Default.Headphones, MuseVioletLight)
        )
    }

    val categories = remember {
        listOf(
            BrowseCategory("Synthwave & Retrowave", "Neon synthesizers & 80s nostalgia", "Synthwave", listOf(0xFF8B5CF6, 0xFFEC4899)),
            BrowseCategory("Late Night Ambient", "Weightless pads & spatial textures", "Ambient", listOf(0xFF6366F1, 0xFF3B82F6)),
            BrowseCategory("Nu-Disco & Funk", "Upbeat grooves & French touch", "Nu-Disco", listOf(0xFF06B6D4, 0xFF10B981)),
            BrowseCategory("Bollywood & Sufi", "Soulful acoustics & classical poetry", "Arijit Singh", listOf(0xFFF59E0B, 0xFFEF4444)),
            BrowseCategory("Neo-Classical", "Modern pianos & orchestral minimalism", "Neo-Classical", listOf(0xFF6366F1, 0xFF8B5CF6)),
            BrowseCategory("Lo-Fi Study Beats", "Warm tape saturation & chill drums", "Lo-Fi Beats", listOf(0xFF10B981, 0xFF059669)),
            BrowseCategory("Deep Melodic House", "Hypnotic basslines & euphoric drops", "Melodic House", listOf(0xFF3B82F6, 0xFF1D4ED8)),
            BrowseCategory("Indie Acoustic", "Raw songwriting & intimate guitars", "Indie Acoustic", listOf(0xFFD97706, 0xFFB45309))
        )
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
            // Header & Search
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Discover",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Explore sonic spaces and global catalogs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Interactive Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotBlank()) MuseVioletLight else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search tracks, artists, moods or genres",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary.copy(alpha = 0.6f)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                cursorBrush = SolidColor(MuseVioletLight),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedMoodId = null
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SEARCH RESULTS VIEW (When query is present)
            if (searchQuery.isNotBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isSearching) "Searching..." else "Results for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MuseVioletLight,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "${searchResults.size} tracks",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (!isSearching && searchResults.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tracks found for \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching for another song, artist, album, or genre",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(searchResults, key = { it.id }) { track ->
                        val isPlaying = playbackState.currentTrack?.id == track.id && playbackState.isPlaying
                        TrackRow(
                            track = track,
                            isPlaying = isPlaying,
                            onTrackClick = {
                                playbackRepository.playTrack(track, searchResults)
                            },
                            onLikeClick = {
                                scope.launch {
                                    musicRepository.toggleLike(track.id)
                                }
                            }
                        )
                    }
                }
            } else {
                // DEFAULT DISCOVERY BROWSING (When query is empty)

                // 1. Mood & Activity Filter Chips
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)) {
                        SectionHeader(
                            title = "What are you in the mood for?",
                            subtitle = "Instant soundscapes for your state of mind"
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            items(moods) { mood ->
                                val isSelected = selectedMoodId == mood.id
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) mood.color.copy(alpha = 0.22f)
                                            else SurfaceCard
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) mood.color else BorderSubtle,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            if (isSelected) {
                                                selectedMoodId = null
                                                searchQuery = ""
                                            } else {
                                                selectedMoodId = mood.id
                                                searchQuery = mood.query
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = mood.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) mood.color else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = mood.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Curated Discovery Shelves
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        SectionHeader(
                            title = "Curated Discovery Shelves",
                            subtitle = "Handpicked by MUSE sound architects"
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            items(playlists) { playlist ->
                                PlaylistCard(
                                    playlist = playlist,
                                    onClick = { playbackRepository.playPlaylist(playlist) },
                                    onPlayClick = { playbackRepository.playPlaylist(playlist) }
                                )
                            }
                        }
                    }
                }

                // 3. Browse Genres Grid
                item {
                    SectionHeader(
                        title = "Explore Genres",
                        subtitle = "Immersive sonic worlds from across the globe"
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in categories.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CategoryCard(
                                    category = categories[i],
                                    onClick = {
                                        searchQuery = categories[i].query
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < categories.size) {
                                    CategoryCard(
                                        category = categories[i + 1],
                                        onClick = {
                                            searchQuery = categories[i + 1].query
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // 4. Ask MUSE Architecture Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MuseViolet.copy(alpha = 0.18f),
                                        MuseIndigo.copy(alpha = 0.12f),
                                        SurfaceElevated
                                    )
                                )
                            )
                            .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(18.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MuseVioletLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "MUSE SONIC INTELLIGENCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MuseVioletLight,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = "Natural Language Music Search",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Text(
                                text = "Search any song, artist, mood, or genre above to instantly stream and discover soundscapes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: BrowseCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(105.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = category.gradientColors.map { Color(it) }
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                role = Role.Button,
                onClick = onClick
            )
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = category.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
