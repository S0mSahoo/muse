package com.example.data.playback

import com.example.data.local.LocalDataStore
import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.domain.provider.PlaybackProvider
import com.example.domain.repository.LikedTracksRepository
import com.example.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Centralized Playback Engine and single source of truth for audio playback.
 * Coordinates audio providers, progress tracking, queue management, like synchronization, and listening history.
 */
class PlaybackManager(
    private val playbackProvider: PlaybackProvider,
    private val localDataStore: LocalDataStore,
    private val likedTracksRepository: LikedTracksRepository,
    private val listeningHistoryRepository: ListeningHistoryRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    private val _playbackState = MutableStateFlow(
        PlaybackState(
            currentTrack = null,
            isPlaying = false,
            progressMs = 0L,
            durationMs = 1L,
            isShuffle = false,
            isRepeat = false,
            queue = emptyList(),
            queueIndex = 0
        )
    )
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private var currentSessionHistoryId: String? = null
    private var currentSessionCompleted: Boolean = false

    init {
        // Observe local liked track IDs to keep playback state perfectly synced everywhere
        scope.launch {
            likedTracksRepository.getLikedTrackIds().collect { likedIds ->
                _playbackState.update { state ->
                    val updatedQueue = state.queue.map { track ->
                        track.copy(isLiked = likedIds.contains(track.id))
                    }
                    val current = state.currentTrack
                    val updatedCurrent = if (current != null) {
                        current.copy(isLiked = likedIds.contains(current.id))
                    } else null

                    state.copy(
                        currentTrack = updatedCurrent,
                        queue = updatedQueue
                    )
                }
            }
        }
    }

    fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
        val likedIds = likedTracksRepository.getLikedTrackIds().value
        val syncTrack = track.copy(isLiked = likedIds.contains(track.id))
        val fullQueue = if (queue.contains(track)) queue else listOf(track) + queue
        val syncQueue = fullQueue.map { it.copy(isLiked = likedIds.contains(it.id)) }
        val index = syncQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        playbackProvider.setQueue(syncQueue, index)
        playbackProvider.play(syncTrack)
        localDataStore.recordRecentTrack(syncTrack.id)

        startNewListeningSession(syncTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = syncTrack,
                isPlaying = true,
                progressMs = 0L,
                durationMs = syncTrack.durationMs.coerceAtLeast(1000L),
                queue = syncQueue,
                queueIndex = index
            )
        }
        startProgressTracker()
    }

    fun playPlaylist(playlist: Playlist, startIndex: Int = 0) {
        if (playlist.tracks.isEmpty()) return
        val likedIds = likedTracksRepository.getLikedTrackIds().value
        val syncTracks = playlist.tracks.map { it.copy(isLiked = likedIds.contains(it.id)) }
        val validIndex = startIndex.coerceIn(0, syncTracks.size - 1)
        val selectedTrack = syncTracks[validIndex]

        playbackProvider.setQueue(syncTracks, validIndex)
        playbackProvider.play(selectedTrack)
        localDataStore.recordRecentTrack(selectedTrack.id)

        startNewListeningSession(selectedTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = selectedTrack,
                isPlaying = true,
                progressMs = 0L,
                durationMs = selectedTrack.durationMs.coerceAtLeast(1000L),
                queue = syncTracks,
                queueIndex = validIndex
            )
        }
        startProgressTracker()
    }

    fun pause() {
        val state = _playbackState.value
        if (!state.isPlaying) return
        playbackProvider.pause()
        stopProgressTracker()

        val historyId = currentSessionHistoryId
        if (historyId != null && !currentSessionCompleted) {
            scope.launch {
                try {
                    listeningHistoryRepository.recordPlaybackProgress(historyId, state.progressMs, state.durationMs)
                } catch (e: Exception) {
                    println("PlaybackManager: Error saving progress on pause: ${e.message}")
                }
            }
        }

        _playbackState.update { it.copy(isPlaying = false) }
    }

    fun resume() {
        val state = _playbackState.value
        if (state.currentTrack == null || state.isPlaying) return
        playbackProvider.resume()

        _playbackState.update { it.copy(isPlaying = true) }
        startProgressTracker()
    }

    fun togglePlayPause() {
        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun next(isNaturalCompletion: Boolean = false) {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        if (!isNaturalCompletion) {
            val historyId = currentSessionHistoryId
            if (historyId != null && !currentSessionCompleted) {
                val ratio = if (state.durationMs > 0) state.progressMs.toFloat() / state.durationMs else 0f
                if (ratio >= 0.8f) {
                    currentSessionCompleted = true
                    scope.launch {
                        try {
                            listeningHistoryRepository.recordPlaybackCompleted(historyId, state.durationMs)
                        } catch (e: Exception) {
                            println("PlaybackManager: Error recording completion on next: ${e.message}")
                        }
                    }
                } else {
                    scope.launch {
                        try {
                            listeningHistoryRepository.recordPlaybackProgress(historyId, state.progressMs, state.durationMs)
                        } catch (e: Exception) {
                            println("PlaybackManager: Error recording progress on next: ${e.message}")
                        }
                    }
                }
            }
        }

        val nextIndex = if (state.isShuffle) {
            (state.queue.indices - state.queueIndex).randomOrNull() ?: 0
        } else {
            (state.queueIndex + 1) % state.queue.size
        }

        val nextTrack = state.queue[nextIndex]
        playbackProvider.skipNext()
        localDataStore.recordRecentTrack(nextTrack.id)

        startNewListeningSession(nextTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = nextTrack,
                isPlaying = true,
                progressMs = 0L,
                durationMs = nextTrack.durationMs.coerceAtLeast(1000L),
                queueIndex = nextIndex
            )
        }
        startProgressTracker()
    }

    fun previous() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        if (state.progressMs > 3000L) {
            seekTo(0L)
            return
        }

        val historyId = currentSessionHistoryId
        if (historyId != null && !currentSessionCompleted) {
            scope.launch {
                try {
                    listeningHistoryRepository.recordPlaybackProgress(historyId, state.progressMs, state.durationMs)
                } catch (e: Exception) {
                    println("PlaybackManager: Error recording progress on previous: ${e.message}")
                }
            }
        }

        val prevIndex = if (state.queueIndex - 1 < 0) state.queue.size - 1 else state.queueIndex - 1
        val prevTrack = state.queue[prevIndex]
        playbackProvider.skipPrevious()
        localDataStore.recordRecentTrack(prevTrack.id)

        startNewListeningSession(prevTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = prevTrack,
                isPlaying = true,
                progressMs = 0L,
                durationMs = prevTrack.durationMs.coerceAtLeast(1000L),
                queueIndex = prevIndex
            )
        }
        startProgressTracker()
    }

    fun seekTo(positionMs: Long) {
        val state = _playbackState.value
        val clamped = positionMs.coerceIn(0L, state.durationMs)
        playbackProvider.seekTo(clamped)
        _playbackState.update { it.copy(progressMs = clamped) }
    }

    fun toggleShuffle() {
        _playbackState.update { it.copy(isShuffle = !it.isShuffle) }
    }

    fun toggleRepeat() {
        _playbackState.update { it.copy(isRepeat = !it.isRepeat) }
    }

    fun toggleLike(trackId: String) {
        scope.launch {
            try {
                likedTracksRepository.toggleLike(trackId)
            } catch (e: Exception) {
                println("PlaybackManager: Error toggling like: ${e.message}")
            }
        }
    }

    private fun startNewListeningSession(trackId: String) {
        currentSessionCompleted = false
        currentSessionHistoryId = null
        scope.launch {
            try {
                currentSessionHistoryId = listeningHistoryRepository.recordPlaybackStart(trackId)
            } catch (e: Exception) {
                println("PlaybackManager: Error starting listening session for track $trackId: ${e.message}")
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                delay(1000L)
                val state = _playbackState.value
                if (!state.isPlaying || state.currentTrack == null) break

                val newPos = state.progressMs + 1000L
                if (newPos >= state.durationMs) {
                    val historyId = currentSessionHistoryId
                    if (historyId != null && !currentSessionCompleted) {
                        currentSessionCompleted = true
                        try {
                            listeningHistoryRepository.recordPlaybackCompleted(historyId, state.durationMs)
                        } catch (e: Exception) {
                            println("PlaybackManager: Error recording natural completion: ${e.message}")
                        }
                    }
                    if (state.isRepeat) {
                        seekTo(0L)
                    } else {
                        next(isNaturalCompletion = true)
                    }
                } else {
                    _playbackState.update { it.copy(progressMs = newPos) }
                    // Record progress every 15 seconds
                    if (newPos % 15000L == 0L) {
                        val historyId = currentSessionHistoryId
                        if (historyId != null && !currentSessionCompleted) {
                            try {
                                listeningHistoryRepository.recordPlaybackProgress(historyId, newPos, state.durationMs)
                            } catch (e: Exception) {
                                println("PlaybackManager: Error updating progress: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }
}
