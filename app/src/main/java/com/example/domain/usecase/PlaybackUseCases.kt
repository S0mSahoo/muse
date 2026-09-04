package com.example.domain.usecase

import com.example.domain.model.PlaybackState
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.domain.repository.PlaybackRepository
import kotlinx.coroutines.flow.StateFlow

class PlayTrackUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke(track: Track, queue: List<Track> = listOf(track)) {
        playbackRepository.playTrack(track, queue)
    }
}

class PlayPlaylistUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke(playlist: Playlist, startIndex: Int = 0) {
        playbackRepository.playPlaylist(playlist, startIndex)
    }
}

class TogglePlayPauseUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke() {
        playbackRepository.togglePlayPause()
    }
}

class PausePlaybackUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke() {
        playbackRepository.pause()
    }
}

class ResumePlaybackUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke() {
        playbackRepository.resume()
    }
}

class SkipNextUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke() {
        playbackRepository.next()
    }
}

class SkipPreviousUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke() {
        playbackRepository.previous()
    }
}

class SeekToUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke(positionMs: Long) {
        playbackRepository.seekTo(positionMs)
    }
}

class GetPlaybackStateUseCase(private val playbackRepository: PlaybackRepository) {
    operator fun invoke(): StateFlow<PlaybackState> {
        return playbackRepository.playbackState
    }
}
