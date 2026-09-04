package com.example.data.repository

import com.example.data.playback.PlaybackManager
import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.domain.repository.PlaybackRepository
import kotlinx.coroutines.flow.StateFlow

class MockPlaybackRepositoryImpl(
    private val playbackManager: PlaybackManager
) : PlaybackRepository {

    override val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState

    override fun playTrack(track: Track, queue: List<Track>) {
        playbackManager.playTrack(track, queue)
    }

    override fun playPlaylist(playlist: Playlist, startIndex: Int) {
        playbackManager.playPlaylist(playlist, startIndex)
    }

    override fun pause() {
        playbackManager.pause()
    }

    override fun resume() {
        playbackManager.resume()
    }

    override fun togglePlayPause() {
        playbackManager.togglePlayPause()
    }

    override fun next() {
        playbackManager.next()
    }

    override fun previous() {
        playbackManager.previous()
    }

    override fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
    }

    override fun toggleLike(trackId: String) {
        playbackManager.toggleLike(trackId)
    }

    override fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    override fun toggleRepeat() {
        playbackManager.toggleRepeat()
    }
}

