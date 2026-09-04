package com.example.domain.provider

import com.example.domain.model.Track

/**
 * Provider-independent interface for audio playback control.
 * Decouples the UI and playback state management from low-level audio renderers or streaming players.
 */
interface PlaybackProvider {
    fun play(track: Track)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun setQueue(tracks: List<Track>, startIndex: Int = 0)
}
