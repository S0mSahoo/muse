package com.example.data.playback

import com.example.data.local.LocalDataStore
import com.example.domain.model.ListeningEvent
import com.example.domain.model.ListeningEventType
import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.domain.provider.PlaybackProvider
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
 * Coordinates audio providers, progress tracking, queue management, like synchronization, and listening event metrics.
 */
class PlaybackManager(
    private val playbackProvider: PlaybackProvider,
    private val localDataStore: LocalDataStore,
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
    private var playbackStartTimestamp: Long = 0L

    init {
        // Observe local liked track IDs to keep playback state perfectly synced everywhere
        scope.launch {
            localDataStore.likedTrackIds.collect { likedIds ->
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
        val likedIds = localDataStore.likedTrackIds.value
        val syncTrack = track.copy(isLiked = likedIds.contains(track.id))
        val fullQueue = if (queue.contains(track)) queue else listOf(track) + queue
        val syncQueue = fullQueue.map { it.copy(isLiked = likedIds.contains(it.id)) }
        val index = syncQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        playbackProvider.setQueue(syncQueue, index)
        playbackProvider.play(syncTrack)
        localDataStore.recordRecentTrack(syncTrack.id)

        playbackStartTimestamp = System.currentTimeMillis()
        recordEvent(syncTrack.id, 0L, ListeningEventType.PLAY_STARTED)

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
        val likedIds = localDataStore.likedTrackIds.value
        val syncTracks = playlist.tracks.map { it.copy(isLiked = likedIds.contains(it.id)) }
        val validIndex = startIndex.coerceIn(0, syncTracks.size - 1)
        val selectedTrack = syncTracks[validIndex]

        playbackProvider.setQueue(syncTracks, validIndex)
        playbackProvider.play(selectedTrack)
        localDataStore.recordRecentTrack(selectedTrack.id)

        playbackStartTimestamp = System.currentTimeMillis()
        recordEvent(selectedTrack.id, 0L, ListeningEventType.PLAY_STARTED)

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

        state.currentTrack?.let { track ->
            recordEvent(track.id, state.progressMs, ListeningEventType.PLAY_PAUSED)
        }

        _playbackState.update { it.copy(isPlaying = false) }
    }

    fun resume() {
        val state = _playbackState.value
        if (state.currentTrack == null || state.isPlaying) return
        playbackProvider.resume()

        state.currentTrack?.let { track ->
            recordEvent(track.id, state.progressMs, ListeningEventType.PLAY_RESUMED)
        }

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

    fun next() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        state.currentTrack?.let { track ->
            val ratio = if (state.durationMs > 0) state.progressMs.toFloat() / state.durationMs else 0f
            if (ratio < 0.8f) {
                recordEvent(track.id, state.progressMs, ListeningEventType.SKIPPED)
            } else {
                recordEvent(track.id, state.progressMs, ListeningEventType.PLAY_COMPLETED)
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

        playbackStartTimestamp = System.currentTimeMillis()
        recordEvent(nextTrack.id, 0L, ListeningEventType.PLAY_STARTED)

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

        val prevIndex = if (state.queueIndex - 1 < 0) state.queue.size - 1 else state.queueIndex - 1
        val prevTrack = state.queue[prevIndex]
        playbackProvider.skipPrevious()
        localDataStore.recordRecentTrack(prevTrack.id)

        playbackStartTimestamp = System.currentTimeMillis()
        recordEvent(prevTrack.id, 0L, ListeningEventType.PLAY_STARTED)

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
        val isLiked = localDataStore.toggleTrackLike(trackId)
        recordEvent(
            trackId = trackId,
            pos = _playbackState.value.progressMs,
            type = if (isLiked) ListeningEventType.LIKED else ListeningEventType.UNLIKED
        )
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
                    recordEvent(state.currentTrack.id, state.durationMs, ListeningEventType.PLAY_COMPLETED)
                    if (state.isRepeat) {
                        seekTo(0L)
                    } else {
                        next()
                    }
                } else {
                    _playbackState.update { it.copy(progressMs = newPos) }
                }
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun recordEvent(trackId: String, pos: Long, type: ListeningEventType) {
        scope.launch {
            val durListened = (System.currentTimeMillis() - playbackStartTimestamp).coerceAtLeast(0L)
            val duration = _playbackState.value.durationMs.coerceAtLeast(1L)
            val ratio = (pos.toFloat() / duration).coerceIn(0f, 1f)
            listeningHistoryRepository.recordEvent(
                ListeningEvent(
                    trackId = trackId,
                    playbackPositionMs = pos,
                    durationListenedMs = durListened,
                    completionRatio = ratio,
                    eventType = type
                )
            )
        }
    }
}
