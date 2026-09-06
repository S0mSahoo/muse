package com.example.data.provider

import com.example.domain.model.Track
import com.example.domain.provider.PlaybackListener
import com.example.domain.provider.PlaybackProvider

/**
 * Mock implementation of [PlaybackProvider].
 * Serves as an audio renderer adapter that can later be swapped for ExoPlayer / Media3 / Partner SDKs.
 */
class MockPlaybackProvider : PlaybackProvider {

    private var currentTrack: Track? = null
    private var isPlaying: Boolean = false
    private var currentPositionMs: Long = 0L
    private var queue: List<Track> = emptyList()
    private var queueIndex: Int = 0

    override fun setPlaybackListener(listener: PlaybackListener?) {
        // No-op for mock provider
    }

    override fun play(track: Track) {
        currentTrack = track
        isPlaying = true
        currentPositionMs = 0L
    }

    override fun pause() {
        isPlaying = false
    }

    override fun resume() {
        if (currentTrack != null) {
            isPlaying = true
        }
    }

    override fun seekTo(positionMs: Long) {
        currentPositionMs = positionMs
    }

    override fun skipNext() {
        if (queue.isNotEmpty()) {
            queueIndex = (queueIndex + 1) % queue.size
            currentTrack = queue[queueIndex]
            currentPositionMs = 0L
            isPlaying = true
        }
    }

    override fun skipPrevious() {
        if (queue.isNotEmpty()) {
            queueIndex = if (queueIndex - 1 < 0) queue.size - 1 else queueIndex - 1
            currentTrack = queue[queueIndex]
            currentPositionMs = 0L
            isPlaying = true
        }
    }

    override fun setQueue(tracks: List<Track>, startIndex: Int) {
        queue = tracks
        queueIndex = startIndex.coerceIn(0, (tracks.size - 1).coerceAtLeast(0))
        if (tracks.isNotEmpty()) {
            currentTrack = tracks[queueIndex]
        }
    }

    override fun release() {
        isPlaying = false
        currentTrack = null
    }
}
