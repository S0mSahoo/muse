package com.example.domain.provider

import com.example.domain.model.Track

/**
 * Callback listener allowing concrete playback providers (e.g. YouTube, Native MediaPlayer)
 * to report real-time player events back to the centralized PlaybackManager.
 */
interface PlaybackListener {
    fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean)
    fun onPositionUpdated(positionMs: Long, durationMs: Long)
    fun onPlaybackEnded()
    fun onPlaybackError(errorMessage: String)
}

/**
 * Provider-independent interface for audio and video playback control.
 * Decouples the UI and playback state management from low-level platform players.
 */
interface PlaybackProvider {
    fun play(track: Track)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun setQueue(tracks: List<Track>, startIndex: Int = 0)
    fun setPlaybackListener(listener: PlaybackListener?)
    fun release() {}
}

