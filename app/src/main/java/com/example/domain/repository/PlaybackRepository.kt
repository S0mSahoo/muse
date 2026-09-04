package com.example.domain.repository

import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface PlaybackRepository {
    val playbackState: StateFlow<PlaybackState>
    fun playTrack(track: Track, queue: List<Track> = listOf(track))
    fun playPlaylist(playlist: Playlist, startIndex: Int = 0)
    fun pause()
    fun resume()
    fun togglePlayPause()
    fun next()
    fun previous()
    fun seekTo(positionMs: Long)
    fun toggleLike(trackId: String)
    fun toggleShuffle()
    fun toggleRepeat()
}
