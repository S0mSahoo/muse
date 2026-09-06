package com.example.data.provider.direct

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.domain.model.Track
import com.example.domain.provider.PlaybackListener
import com.example.domain.provider.PlaybackProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Native Android [MediaPlayer] playback provider for direct streaming tracks (e.g. licensed
 * direct audio or HTTP audio streams).
 *
 * Explicitly free of synthetic or generated fallback audio.
 */
class NativeAudioPlaybackProvider : PlaybackProvider {

    companion object {
        private const val TAG = "MUSE_NativeAudioPlayer"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    private var isPlaying = false
    private var playbackListener: PlaybackListener? = null

    private var progressPollingJob: Job? = null
    private var queue: List<Track> = emptyList()
    private var queueIndex: Int = 0

    override fun setPlaybackListener(listener: PlaybackListener?) {
        this.playbackListener = listener
    }

    override fun play(track: Track) {
        stopPlayer()
        currentTrack = track
        this@NativeAudioPlaybackProvider.isPlaying = false

        val audioUrl = track.audioUrl.trim()
        if (audioUrl.isEmpty() || (!audioUrl.startsWith("http://") && !audioUrl.startsWith("https://"))) {
            Log.w(TAG, "No valid direct audio URL available for track: ${track.title}")
            playbackListener?.onPlaybackError("No playable audio stream available for this track.")
            return
        }

        playbackListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = true)

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    try {
                        if (currentTrack?.id == track.id) {
                            mp.start()
                            this@NativeAudioPlaybackProvider.isPlaying = true
                            playbackListener?.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
                            startProgressPolling()
                            Log.d(TAG, "Native playback started for: ${track.title}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting MediaPlayer onPrepared: ${e.message}")
                        playbackListener?.onPlaybackError("Unable to start audio playback.")
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra for track: ${track.title}")
                    this@NativeAudioPlaybackProvider.isPlaying = false
                    stopProgressPolling()
                    playbackListener?.onPlaybackError("Audio stream error ($what, $extra).")
                    true
                }
                setOnCompletionListener {
                    Log.d(TAG, "Native playback completed for: ${track.title}")
                    this@NativeAudioPlaybackProvider.isPlaying = false
                    stopProgressPolling()
                    playbackListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
                    playbackListener?.onPlaybackEnded()
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaPlayer for ${track.title}: ${e.message}")
            playbackListener?.onPlaybackError("Failed to initialize audio player.")
        }
    }

    override fun pause() {
        this@NativeAudioPlaybackProvider.isPlaying = false
        stopProgressPolling()
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
            playbackListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing MediaPlayer: ${e.message}")
        }
    }

    override fun resume() {
        try {
            mediaPlayer?.let {
                it.start()
                this@NativeAudioPlaybackProvider.isPlaying = true
                playbackListener?.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
                startProgressPolling()
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming MediaPlayer: ${e.message}")
        }
        currentTrack?.let { play(it) }
    }

    override fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            val duration = mediaPlayer?.duration?.toLong() ?: 0L
            playbackListener?.onPositionUpdated(positionMs, duration)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking MediaPlayer: ${e.message}")
        }
    }

    override fun skipNext() {
        if (queue.isNotEmpty()) {
            queueIndex = (queueIndex + 1) % queue.size
            play(queue[queueIndex])
        }
    }

    override fun skipPrevious() {
        if (queue.isNotEmpty()) {
            queueIndex = if (queueIndex - 1 < 0) queue.size - 1 else queueIndex - 1
            play(queue[queueIndex])
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
        stopPlayer()
    }

    private fun startProgressPolling() {
        stopProgressPolling()
        progressPollingJob = scope.launch {
            while (isActive && this@NativeAudioPlaybackProvider.isPlaying) {
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            val pos = mp.currentPosition.toLong().coerceAtLeast(0L)
                            val dur = mp.duration.toLong().coerceAtLeast(0L)
                            playbackListener?.onPositionUpdated(pos, dur)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore transient position lookup errors
                }
                delay(500)
            }
        }
    }

    private fun stopProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = null
    }

    private fun stopPlayer() {
        this@NativeAudioPlaybackProvider.isPlaying = false
        stopProgressPolling()
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.reset()
                mp.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
            }
            mediaPlayer = null
        }
    }
}
