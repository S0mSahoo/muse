package com.example.data.provider

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.domain.model.Track
import com.example.domain.provider.PlaybackProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Production Android audio playback engine using [MediaPlayer] for streaming audio
 * and dynamic harmonized audio synthesis fallback when streams are unavailable or offline.
 */
class AndroidAudioPlaybackProvider : PlaybackProvider {

    private val tag = "MUSE_AudioPlayback"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null
    private var isPlaying: Boolean = false
    private var currentPositionMs: Long = 0L
    private var queue: List<Track> = emptyList()
    private var queueIndex: Int = 0

    // Synthesizer fallback components
    private var synthTrack: AudioTrack? = null
    private var synthJob: Job? = null
    @Volatile
    private var isSynthActive: Boolean = false

    override fun play(track: Track) {
        stopCurrentAudio()
        currentTrack = track
        isPlaying = true
        currentPositionMs = 0L

        val audioUrl = track.audioUrl.trim()
        if (audioUrl.isNotEmpty() && (audioUrl.startsWith("http://") || audioUrl.startsWith("https://"))) {
            playFromUrl(audioUrl, track)
        } else {
            // Harmonic ambient sound generator
            startSynthesizedAudio(track)
        }
    }

    private fun playFromUrl(url: String, track: Track) {
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { player ->
                    try {
                        if (isPlaying && currentTrack?.id == track.id) {
                            player.start()
                            Log.d(tag, "Streaming audio started for: ${track.title}")
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error starting MediaPlayer on prepare: ${e.message}")
                        startSynthesizedAudio(track)
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.w(tag, "MediaPlayer error ($what, $extra) for: ${track.title}. Falling back to synthesis.")
                    startSynthesizedAudio(track)
                    true
                }
                setOnCompletionListener {
                    Log.d(tag, "Audio completed for: ${track.title}")
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize MediaPlayer for ${track.title}: ${e.message}")
            startSynthesizedAudio(track)
        }
    }

    private fun startSynthesizedAudio(track: Track) {
        stopCurrentAudio()
        isSynthActive = true

        // Generate gentle, harmonious musical ambient chords based on the track's id / title
        val baseFreq = when (track.genres.firstOrNull()?.lowercase()) {
            "synthwave", "retrowave" -> 220.0 // A3
            "ambient", "idm" -> 174.61 // F3
            "acoustic", "soul", "bollywood" -> 261.63 // C4
            "nu-disco", "funk" -> 293.66 // D4
            "neo-classical" -> 196.00 // G3
            else -> 220.0
        }

        synthJob = scope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate / 2)

            val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            synthTrack = audioTrack
            try {
                audioTrack.play()
                val buffer = ShortArray(bufferSize)
                var phase1 = 0.0
                var phase2 = 0.0
                var phase3 = 0.0

                // Chord frequencies (root, major/minor third, fifth)
                val f1 = baseFreq
                val f2 = baseFreq * 1.25 // Major 3rd
                val f3 = baseFreq * 1.5  // Perfect 5th

                val inc1 = 2.0 * Math.PI * f1 / sampleRate
                val inc2 = 2.0 * Math.PI * f2 / sampleRate
                val inc3 = 2.0 * Math.PI * f3 / sampleRate

                while (isActive && isSynthActive && isPlaying) {
                    for (i in buffer.indices) {
                        val sample = (sin(phase1) * 0.35 + sin(phase2) * 0.25 + sin(phase3) * 0.2) * 0.4
                        buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()

                        phase1 += inc1
                        phase2 += inc2
                        phase3 += inc3
                        if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                        if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                        if (phase3 > 2.0 * Math.PI) phase3 -= 2.0 * Math.PI
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.e(tag, "Synth error: ${e.message}")
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Ignored on teardown
                }
            }
        }
    }

    private fun stopCurrentAudio() {
        isSynthActive = false
        synthJob?.cancel()
        synthJob = null

        synthTrack?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {
                // Ignore
            }
            synthTrack = null
        }

        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.reset()
                mp.release()
            } catch (e: Exception) {
                Log.e(tag, "Error releasing MediaPlayer: ${e.message}")
            }
            mediaPlayer = null
        }
    }

    override fun pause() {
        isPlaying = false
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error pausing MediaPlayer: ${e.message}")
        }
        isSynthActive = false
    }

    override fun resume() {
        if (currentTrack != null) {
            isPlaying = true
            try {
                mediaPlayer?.let {
                    it.start()
                    return
                }
            } catch (e: Exception) {
                Log.e(tag, "Error resuming MediaPlayer: ${e.message}")
            }
            currentTrack?.let { play(it) }
        }
    }

    override fun seekTo(positionMs: Long) {
        currentPositionMs = positionMs
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            Log.e(tag, "Error seeking MediaPlayer: ${e.message}")
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
}
