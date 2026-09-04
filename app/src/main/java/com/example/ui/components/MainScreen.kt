package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.di.AppContainer
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.BackgroundDark
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route ?: "home"

    val showBottomBar = currentDestination !in listOf("login", "signup")

    val playbackRepository = AppContainer.playbackRepository
    val musicRepository = AppContainer.musicRepository
    val playbackState by playbackRepository.playbackState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showNowPlaying by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // App Screens Destination Host
        AppNavigation(navController = navController)

        // Persistent Bottom Area (MiniPlayer + Bottom Navigation Bar)
        if (showBottomBar) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                // Mini Player (Visible when a track is loaded, smoothly animated)
                MiniPlayer(
                    playbackState = playbackState,
                    onTogglePlayPause = { playbackRepository.togglePlayPause() },
                    onNext = { playbackRepository.next() },
                    onToggleLike = { trackId ->
                        scope.launch {
                            musicRepository.toggleTrackLike(trackId)
                            playbackRepository.toggleLike(trackId)
                        }
                    },
                    onClick = { showNowPlaying = true }
                )

                // Bottom Navigation Bar
                BottomNavBar(
                    currentRoute = currentDestination,
                    onNavigate = { route ->
                        if (currentDestination != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }

        // Fullscreen Modal Now Playing Sheet
        if (showNowPlaying && playbackState.currentTrack != null) {
            NowPlayingSheet(
                playbackState = playbackState,
                onDismiss = { showNowPlaying = false },
                onTogglePlayPause = { playbackRepository.togglePlayPause() },
                onNext = { playbackRepository.next() },
                onPrevious = { playbackRepository.previous() },
                onSeekTo = { pos -> playbackRepository.seekTo(pos) },
                onToggleLike = { trackId ->
                    scope.launch {
                        musicRepository.toggleTrackLike(trackId)
                        playbackRepository.toggleLike(trackId)
                    }
                },
                onToggleShuffle = { playbackRepository.toggleShuffle() },
                onToggleRepeat = { playbackRepository.toggleRepeat() }
            )
        }
    }
}
