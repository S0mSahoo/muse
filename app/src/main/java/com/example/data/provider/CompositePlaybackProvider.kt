package com.example.data.provider

import com.example.data.provider.direct.NativeAudioPlaybackProvider
import com.example.data.provider.youtube.YouTubePlaybackProvider
import com.example.domain.model.Track
import com.example.domain.provider.PlaybackListener
import com.example.domain.provider.PlaybackProvider

/**
 * Provider-aware coordinator that transparently routes playback commands to either
 * [YouTubePlaybackProvider] (for YouTube video/music content using supported IFrame Player API)
 * or [NativeAudioPlaybackProvider] (for direct audio streams).
 *
 * Ensures only one audio/video player is actively executing at any given time.
 */
class CompositePlaybackProvider(
    val youTubePlaybackProvider: YouTubePlaybackProvider,
    val nativePlaybackProvider: NativeAudioPlaybackProvider
) : PlaybackProvider {

    private var activeProvider: PlaybackProvider? = null
    private var globalListener: PlaybackListener? = null

    private var currentQueue: List<Track> = emptyList()
    private var currentQueueIndex: Int = 0

    init {
        val proxyListener = object : PlaybackListener {
            override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
                globalListener?.onPlaybackStateChanged(isPlaying, isBuffering)
            }

            override fun onPositionUpdated(positionMs: Long, durationMs: Long) {
                globalListener?.onPositionUpdated(positionMs, durationMs)
            }

            override fun onPlaybackEnded() {
                globalListener?.onPlaybackEnded()
            }

            override fun onPlaybackError(errorMessage: String) {
                globalListener?.onPlaybackError(errorMessage)
            }
        }

        youTubePlaybackProvider.setPlaybackListener(proxyListener)
        nativePlaybackProvider.setPlaybackListener(proxyListener)
    }

    override fun setPlaybackListener(listener: PlaybackListener?) {
        this.globalListener = listener
    }

    override fun play(track: Track) {
        val isYouTube = isYouTubeTrack(track)
        val targetProvider: PlaybackProvider = if (isYouTube) {
            nativePlaybackProvider.pause()
            youTubePlaybackProvider
        } else {
            youTubePlaybackProvider.pause()
            nativePlaybackProvider
        }

        activeProvider = targetProvider
        targetProvider.play(track)
    }

    override fun pause() {
        activeProvider?.pause() ?: run {
            youTubePlaybackProvider.pause()
            nativePlaybackProvider.pause()
        }
    }

    override fun resume() {
        activeProvider?.resume()
    }

    override fun seekTo(positionMs: Long) {
        activeProvider?.seekTo(positionMs)
    }

    override fun skipNext() {
        if (currentQueue.isNotEmpty()) {
            currentQueueIndex = (currentQueueIndex + 1) % currentQueue.size
            play(currentQueue[currentQueueIndex])
        }
    }

    override fun skipPrevious() {
        if (currentQueue.isNotEmpty()) {
            currentQueueIndex = if (currentQueueIndex - 1 < 0) currentQueue.size - 1 else currentQueueIndex - 1
            play(currentQueue[currentQueueIndex])
        }
    }

    override fun setQueue(tracks: List<Track>, startIndex: Int) {
        currentQueue = tracks
        currentQueueIndex = startIndex.coerceIn(0, (tracks.size - 1).coerceAtLeast(0))
        youTubePlaybackProvider.setQueue(tracks, startIndex)
        nativePlaybackProvider.setQueue(tracks, startIndex)
    }

    override fun release() {
        youTubePlaybackProvider.release()
        nativePlaybackProvider.release()
        activeProvider = null
    }

    private fun isYouTubeTrack(track: Track): Boolean {
        return track.providerId == "youtube" ||
                track.source.equals("YOUTUBE", ignoreCase = true) ||
                track.id.startsWith("yt_") ||
                (track.sourceTrackId.isNotEmpty() && track.audioUrl.isEmpty())
    }
}
