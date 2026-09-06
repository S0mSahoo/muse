package com.example.data.playback

import com.example.data.local.LocalDataStore
import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.domain.provider.PlaybackProvider
import com.example.domain.repository.LikedTracksRepository
import com.example.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Encapsulates an active playback listening session to prevent race conditions across tracks.
 */
private data class PlaybackSession(
    val generation: Long,
    val trackId: String,
    val historyIdDeferred: Deferred<String?>
) {
    @Volatile
    var isCompleted: Boolean = false
}

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
    private var sessionGenerationCounter: Long = 0L
    private var currentSession: PlaybackSession? = null

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
        val state = _playbackState.value
        if (state.currentTrack != null) {
            finalizeCurrentSession(
                markCompleted = false,
                finalProgressMs = state.progressMs,
                durationMs = state.durationMs
            )
        }

        val likedIds = likedTracksRepository.getLikedTrackIds().value
        val syncTrack = track.copy(isLiked = likedIds.contains(track.id))
        val fullQueue = if (queue.contains(track)) queue else listOf(track) + queue
        val syncQueue = fullQueue.map { it.copy(isLiked = likedIds.contains(it.id)) }
        val index = syncQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        playbackProvider.setQueue(syncQueue, index)
        playbackProvider.play(syncTrack)
        localDataStore.recordRecentTrack(syncTrack.id)

        val session = createNewSession(syncTrack.id)

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
        startProgressTracker(session)
    }

    fun playPlaylist(playlist: Playlist, startIndex: Int = 0) {
        if (playlist.tracks.isEmpty()) return

        val state = _playbackState.value
        if (state.currentTrack != null) {
            finalizeCurrentSession(
                markCompleted = false,
                finalProgressMs = state.progressMs,
                durationMs = state.durationMs
            )
        }

        val likedIds = likedTracksRepository.getLikedTrackIds().value
        val syncTracks = playlist.tracks.map { it.copy(isLiked = likedIds.contains(it.id)) }
        val validIndex = startIndex.coerceIn(0, syncTracks.size - 1)
        val selectedTrack = syncTracks[validIndex]

        playbackProvider.setQueue(syncTracks, validIndex)
        playbackProvider.play(selectedTrack)
        localDataStore.recordRecentTrack(selectedTrack.id)

        val session = createNewSession(selectedTrack.id)

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
        startProgressTracker(session)
    }

    fun pause() {
        val state = _playbackState.value
        if (!state.isPlaying) return
        playbackProvider.pause()
        stopProgressTracker()

        val session = currentSession
        if (session != null && !session.isCompleted) {
            scope.launch {
                try {
                    val historyId = session.historyIdDeferred.await()
                    if (historyId != null && !session.isCompleted && currentSession?.generation == session.generation) {
                        listeningHistoryRepository.recordPlaybackProgress(historyId, state.progressMs, state.durationMs)
                    }
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
        val session = currentSession ?: return
        playbackProvider.resume()

        _playbackState.update { it.copy(isPlaying = true) }
        startProgressTracker(session)
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
            val priorSession = currentSession
            if (priorSession != null && !priorSession.isCompleted) {
                val ratio = if (state.durationMs > 0) state.progressMs.toFloat() / state.durationMs else 0f
                val isComplete = ratio >= 0.8f
                finalizeCurrentSession(
                    markCompleted = isComplete,
                    finalProgressMs = state.progressMs,
                    durationMs = state.durationMs
                )
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

        val newSession = createNewSession(nextTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = nextTrack,
                isPlaying = true,
                progressMs = 0L,
                durationMs = nextTrack.durationMs.coerceAtLeast(1000L),
                queueIndex = nextIndex
            )
        }
        startProgressTracker(newSession)
    }

    fun previous() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        if (state.progressMs > 3000L) {
            seekTo(0L)
            return
        }

        val priorSession = currentSession
        if (priorSession != null && !priorSession.isCompleted) {
            finalizeCurrentSession(
                markCompleted = false,
                finalProgressMs = state.progressMs,
                durationMs = state.durationMs
            )
        }

        val prevIndex = if (state.queueIndex - 1 < 0) state.queue.size - 1 else state.queueIndex - 1
        val prevTrack = state.queue[prevIndex]
        playbackProvider.skipPrevious()
        localDataStore.recordRecentTrack(prevTrack.id)

        val newSession = createNewSession(prevTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = prevTrack,
                isPlaying = true,
                progressMs = 0L,
                durationMs = prevTrack.durationMs.coerceAtLeast(1000L),
                queueIndex = prevIndex
            )
        }
        startProgressTracker(newSession)
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

    private fun createNewSession(trackId: String): PlaybackSession {
        val gen = ++sessionGenerationCounter
        val deferred = scope.async {
            try {
                listeningHistoryRepository.recordPlaybackStart(trackId)
            } catch (e: Exception) {
                println("PlaybackManager: Failed to start listening session for track $trackId: ${e.message}")
                null
            }
        }
        val session = PlaybackSession(
            generation = gen,
            trackId = trackId,
            historyIdDeferred = deferred
        )
        currentSession = session
        return session
    }

    private fun finalizeCurrentSession(
        markCompleted: Boolean,
        finalProgressMs: Long,
        durationMs: Long
    ) {
        val session = currentSession ?: return
        if (session.isCompleted) return

        scope.launch {
            try {
                val historyId = session.historyIdDeferred.await() ?: return@launch
                if (session.isCompleted) return@launch

                if (markCompleted) {
                    session.isCompleted = true
                    listeningHistoryRepository.recordPlaybackCompleted(historyId, durationMs)
                } else {
                    listeningHistoryRepository.recordPlaybackProgress(historyId, finalProgressMs, durationMs)
                }
            } catch (e: Exception) {
                println("PlaybackManager: Error finalizing listening session for ${session.trackId}: ${e.message}")
            }
        }
    }

    private fun startProgressTracker(session: PlaybackSession) {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying && currentSession?.generation == session.generation) {
                delay(1000L)
                val state = _playbackState.value
                if (!state.isPlaying || state.currentTrack == null || currentSession?.generation != session.generation) break

                val newPos = state.progressMs + 1000L
                if (newPos >= state.durationMs) {
                    if (!session.isCompleted) {
                        session.isCompleted = true
                        scope.launch {
                            try {
                                val historyId = session.historyIdDeferred.await()
                                if (historyId != null) {
                                    listeningHistoryRepository.recordPlaybackCompleted(historyId, state.durationMs)
                                }
                            } catch (e: Exception) {
                                println("PlaybackManager: Error recording natural completion for ${session.trackId}: ${e.message}")
                            }
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
                        if (!session.isCompleted) {
                            scope.launch {
                                try {
                                    val historyId = session.historyIdDeferred.await()
                                    if (historyId != null && !session.isCompleted && currentSession?.generation == session.generation) {
                                        listeningHistoryRepository.recordPlaybackProgress(historyId, newPos, state.durationMs)
                                    }
                                } catch (e: Exception) {
                                    println("PlaybackManager: Error updating progress for ${session.trackId}: ${e.message}")
                                }
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
