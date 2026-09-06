package com.example.data.playback

import android.util.Log
import com.example.data.local.LocalDataStore
import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.domain.provider.PlaybackListener
import com.example.domain.provider.PlaybackProvider
import com.example.domain.repository.LikedTracksRepository
import com.example.domain.repository.ListeningHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    @Volatile
    var lastRecordedProgressMs: Long = 0L
}

/**
 * Centralized Playback Engine and single source of truth for audio and video playback.
 * Coordinates playback providers (YouTube IFrame, Native audio), progress updates,
 * queue management, liked tracks synchronization, and listening history.
 */
class PlaybackManager(
    private val playbackProvider: PlaybackProvider,
    private val localDataStore: LocalDataStore,
    private val likedTracksRepository: LikedTracksRepository,
    private val listeningHistoryRepository: ListeningHistoryRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : PlaybackListener {

    companion object {
        private const val TAG = "MUSE_PlaybackManager"
    }

    private val _playbackState = MutableStateFlow(
        PlaybackState(
            currentTrack = null,
            isPlaying = false,
            isBuffering = false,
            playbackError = null,
            progressMs = 0L,
            durationMs = 1L,
            isShuffle = false,
            isRepeat = false,
            queue = emptyList(),
            queueIndex = 0
        )
    )
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var sessionGenerationCounter: Long = 0L
    private var currentSession: PlaybackSession? = null

    init {
        playbackProvider.setPlaybackListener(this)

        // Observe local liked track IDs to keep playback state synced everywhere
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

    override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
        _playbackState.update { current ->
            current.copy(
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                playbackError = if (isPlaying) null else current.playbackError
            )
        }
    }

    override fun onPositionUpdated(positionMs: Long, durationMs: Long) {
        val session = currentSession ?: return
        val currentDuration = if (durationMs > 1000L) durationMs else _playbackState.value.durationMs

        _playbackState.update { current ->
            current.copy(
                progressMs = positionMs,
                durationMs = currentDuration
            )
        }

        // Periodically update progress in listening history (every 15 seconds)
        if (positionMs - session.lastRecordedProgressMs >= 15000L && !session.isCompleted) {
            session.lastRecordedProgressMs = positionMs
            scope.launch {
                try {
                    val historyId = session.historyIdDeferred.await()
                    if (historyId != null && !session.isCompleted && currentSession?.generation == session.generation) {
                        listeningHistoryRepository.recordPlaybackProgress(historyId, positionMs, currentDuration)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error saving periodic progress: ${e.message}")
                }
            }
        }
    }

    override fun onPlaybackEnded() {
        Log.d(TAG, "Player reported natural playback ended")
        val state = _playbackState.value
        val session = currentSession

        if (session != null && !session.isCompleted) {
            session.isCompleted = true
            val effectiveDuration = if (state.durationMs > 1000L) state.durationMs else 1000L
            scope.launch {
                try {
                    val historyId = session.historyIdDeferred.await()
                    if (historyId != null) {
                        listeningHistoryRepository.recordPlaybackCompleted(historyId, effectiveDuration)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error recording natural completion: ${e.message}")
                }
            }
        }

        if (state.isRepeat && state.currentTrack != null) {
            Log.d(TAG, "Repeat is enabled: starting new session for track ${state.currentTrack.title}")
            createNewSession(state.currentTrack.id)
            playbackProvider.seekTo(0L)
            playbackProvider.resume()
        } else {
            next(isNaturalCompletion = true)
        }
    }

    override fun onPlaybackError(errorMessage: String) {
        Log.e(TAG, "Playback error received: $errorMessage")
        _playbackState.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                playbackError = errorMessage
            )
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
        localDataStore.recordRecentTrack(syncTrack.id)
        createNewSession(syncTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = syncTrack,
                isPlaying = false,
                isBuffering = true,
                playbackError = null,
                progressMs = 0L,
                durationMs = syncTrack.durationMs.coerceAtLeast(1000L),
                queue = syncQueue,
                queueIndex = index
            )
        }

        playbackProvider.play(syncTrack)
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
        localDataStore.recordRecentTrack(selectedTrack.id)
        createNewSession(selectedTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = selectedTrack,
                isPlaying = false,
                isBuffering = true,
                playbackError = null,
                progressMs = 0L,
                durationMs = selectedTrack.durationMs.coerceAtLeast(1000L),
                queue = syncTracks,
                queueIndex = validIndex
            )
        }

        playbackProvider.play(selectedTrack)
    }

    fun pause() {
        val state = _playbackState.value
        playbackProvider.pause()

        val session = currentSession
        if (session != null && !session.isCompleted) {
            scope.launch {
                try {
                    val historyId = session.historyIdDeferred.await()
                    if (historyId != null && !session.isCompleted && currentSession?.generation == session.generation) {
                        listeningHistoryRepository.recordPlaybackProgress(historyId, state.progressMs, state.durationMs)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error saving progress on pause: ${e.message}")
                }
            }
        }
    }

    fun resume() {
        val state = _playbackState.value
        if (state.currentTrack == null) return
        _playbackState.update { it.copy(playbackError = null) }
        playbackProvider.resume()
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
            val unplayed = state.queue.indices.filter { it != state.queueIndex }
            if (unplayed.isNotEmpty()) unplayed.random() else 0
        } else {
            (state.queueIndex + 1) % state.queue.size
        }

        val nextTrack = state.queue[nextIndex]
        localDataStore.recordRecentTrack(nextTrack.id)
        createNewSession(nextTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = nextTrack,
                isPlaying = false,
                isBuffering = true,
                playbackError = null,
                progressMs = 0L,
                durationMs = nextTrack.durationMs.coerceAtLeast(1000L),
                queueIndex = nextIndex
            )
        }

        playbackProvider.skipNext()
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
        localDataStore.recordRecentTrack(prevTrack.id)
        createNewSession(prevTrack.id)

        _playbackState.update {
            it.copy(
                currentTrack = prevTrack,
                isPlaying = false,
                isBuffering = true,
                playbackError = null,
                progressMs = 0L,
                durationMs = prevTrack.durationMs.coerceAtLeast(1000L),
                queueIndex = prevIndex
            )
        }

        playbackProvider.skipPrevious()
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
                Log.e(TAG, "Error toggling like: ${e.message}")
            }
        }
    }

    private fun createNewSession(trackId: String): PlaybackSession {
        val gen = ++sessionGenerationCounter
        val deferred = scope.async {
            try {
                listeningHistoryRepository.recordPlaybackStart(trackId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start listening session for track $trackId: ${e.message}")
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
        session.isCompleted = true

        scope.launch {
            try {
                val historyId = session.historyIdDeferred.await() ?: return@launch
                if (markCompleted) {
                    listeningHistoryRepository.recordPlaybackCompleted(historyId, durationMs)
                } else {
                    listeningHistoryRepository.recordPlaybackProgress(historyId, finalProgressMs, durationMs)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error finalizing listening session for ${session.trackId}: ${e.message}")
            }
        }
    }
}
