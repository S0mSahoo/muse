package com.example.data.provider.youtube

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.domain.model.PlaybackAvailability
import com.example.domain.model.Track
import com.example.domain.provider.PlaybackListener
import com.example.domain.provider.PlaybackProvider

/**
 * Official in-app YouTube playback provider using the supported YouTube IFrame Player API
 * hosted inside a customized Android WebView.
 *
 * Adheres strictly to YouTube platform guidelines:
 * - Does NOT extract raw audio streams or bypass restrictions
 * - Uses supported IFrame Player API embedded inside the MUSE experience
 * - Propagates real player state (ready, playing, paused, buffering, ended, error)
 * - Thread-safe with session generation tokens to ignore stale callbacks on track change
 */
class YouTubePlaybackProvider(
    private val contextProvider: () -> Context? = { null }
) : PlaybackProvider {

    companion object {
        private const val TAG = "MUSE_YouTubePlayer"
        private const val BASE_URL = "https://www.youtube.com"
        private const val JS_BRIDGE_NAME = "MUSE_Bridge"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webView: WebView? = null
    private var isPlayerReady = false
    private var pendingAction: (() -> Unit)? = null

    private var currentTrack: Track? = null
    private var queue: List<Track> = emptyList()
    private var queueIndex: Int = 0

    private var playbackListener: PlaybackListener? = null
    private var sessionGeneration: Long = 0L

    /**
     * Retrieves or initializes the singleton WebView host for the YouTube IFrame Player.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun getPlayerView(context: Context): View {
        if (webView == null) {
            val appCtx = context.applicationContext
            val view = WebView(appCtx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(0xFF08080C.toInt())

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "YouTube player HTML host loaded.")
                    }
                }

                addJavascriptInterface(YouTubeJsBridge(), JS_BRIDGE_NAME)
                loadDataWithBaseURL(BASE_URL, buildPlayerHtml(), "text/html", "UTF-8", null)
            }
            webView = view
        }
        return webView!!
    }

    private fun ensureWebViewInitialized() {
        if (webView == null) {
            contextProvider()?.let { ctx ->
                getPlayerView(ctx)
            }
        }
    }

    override fun setPlaybackListener(listener: PlaybackListener?) {
        this.playbackListener = listener
    }

    override fun play(track: Track) {
        val currentGen = ++sessionGeneration
        currentTrack = track

        // Validate playability before issuing load commands
        when (track.playbackAvailability) {
            PlaybackAvailability.EMBEDDING_DISABLED -> {
                Log.w(TAG, "Cannot play track ${track.title}: embedding is disabled by owner.")
                playbackListener?.onPlaybackError("Playback isn't available for this video (embedding restricted by owner).")
                return
            }
            PlaybackAvailability.REGION_RESTRICTED -> {
                Log.w(TAG, "Cannot play track ${track.title}: region restricted.")
                playbackListener?.onPlaybackError("This video is restricted in your region.")
                return
            }
            PlaybackAvailability.UNAVAILABLE -> {
                Log.w(TAG, "Cannot play track ${track.title}: unavailable.")
                playbackListener?.onPlaybackError("This video is currently unavailable on YouTube.")
                return
            }
            else -> { /* Available */ }
        }

        val videoId = track.sourceTrackId.ifEmpty {
            if (track.id.startsWith("yt_")) track.id.removePrefix("yt_") else track.id
        }

        if (videoId.isBlank()) {
            Log.e(TAG, "No valid YouTube video ID found for track: ${track.title}")
            playbackListener?.onPlaybackError("Invalid YouTube track identifier.")
            return
        }

        // Notify MUSE that buffering has initiated
        playbackListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = true)

        runOnMain {
            ensureWebViewInitialized()
            val loadCommand = {
                if (sessionGeneration == currentGen) {
                    val script = "loadVideo('$videoId', 0);"
                    webView?.evaluateJavascript(script, null)
                    Log.d(TAG, "Issued YouTube loadVideo for: $videoId (generation: $currentGen)")
                }
            }

            if (isPlayerReady && webView != null) {
                loadCommand()
            } else {
                pendingAction = loadCommand
            }
        }
    }

    override fun pause() {
        runOnMain {
            if (isPlayerReady && webView != null) {
                webView?.evaluateJavascript("pauseVideo();", null)
            }
        }
    }

    override fun resume() {
        runOnMain {
            if (isPlayerReady && webView != null) {
                webView?.evaluateJavascript("playVideo();", null)
            } else if (currentTrack != null) {
                currentTrack?.let { play(it) }
            }
        }
    }

    override fun seekTo(positionMs: Long) {
        val seconds = (positionMs / 1000f).coerceAtLeast(0f)
        runOnMain {
            if (isPlayerReady && webView != null) {
                webView?.evaluateJavascript("seekTo($seconds);", null)
            }
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
        runOnMain {
            sessionGeneration++
            try {
                webView?.evaluateJavascript("stopVideo();", null)
                (webView?.parent as? ViewGroup)?.removeView(webView)
                webView?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying YouTube WebView: ${e.message}")
            }
            webView = null
            isPlayerReady = false
            pendingAction = null
        }
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    private inner class YouTubeJsBridge {

        @JavascriptInterface
        fun onReady() {
            mainHandler.post {
                isPlayerReady = true
                Log.d(TAG, "YouTube IFrame Player is ready.")
                pendingAction?.invoke()
                pendingAction = null
            }
        }

        @JavascriptInterface
        fun onStateChange(state: Int, currentTimeSeconds: Double, durationSeconds: Double) {
            val gen = sessionGeneration
            mainHandler.post {
                if (gen != sessionGeneration) {
                    Log.d(TAG, "Ignoring stale state callback ($state) for previous generation")
                    return@post
                }

                val curMs = (currentTimeSeconds * 1000).toLong().coerceAtLeast(0L)
                val durMs = (durationSeconds * 1000).toLong().coerceAtLeast(0L)

                when (state) {
                    0 -> { // ENDED
                        Log.d(TAG, "YouTube playback ended naturally.")
                        playbackListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
                        if (durMs > 0) {
                            playbackListener?.onPositionUpdated(durMs, durMs)
                        }
                        playbackListener?.onPlaybackEnded()
                    }
                    1 -> { // PLAYING
                        Log.d(TAG, "YouTube is playing ($curMs / $durMs ms)")
                        playbackListener?.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
                        if (durMs > 0) {
                            playbackListener?.onPositionUpdated(curMs, durMs)
                        }
                    }
                    2 -> { // PAUSED
                        Log.d(TAG, "YouTube is paused ($curMs ms)")
                        playbackListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
                        playbackListener?.onPositionUpdated(curMs, durMs)
                    }
                    3 -> { // BUFFERING
                        Log.d(TAG, "YouTube is buffering")
                        playbackListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = true)
                    }
                    5 -> { // CUED
                        playbackListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
                    }
                    -1 -> { // UNSTARTED
                        // Waiting for initial play/buffer
                    }
                }
            }
        }

        @JavascriptInterface
        fun onTimeUpdate(currentTimeSeconds: Double, durationSeconds: Double) {
            val gen = sessionGeneration
            mainHandler.post {
                if (gen != sessionGeneration) return@post
                val curMs = (currentTimeSeconds * 1000).toLong().coerceAtLeast(0L)
                val durMs = (durationSeconds * 1000).toLong().coerceAtLeast(0L)
                if (durMs > 0) {
                    playbackListener?.onPositionUpdated(curMs, durMs)
                }
            }
        }

        @JavascriptInterface
        fun onError(errorCode: Int) {
            val gen = sessionGeneration
            mainHandler.post {
                if (gen != sessionGeneration) return@post
                Log.e(TAG, "YouTube Player Error: $errorCode")
                val message = when (errorCode) {
                    101, 150 -> "Playback isn't available for this video (embedding restricted by owner)."
                    100 -> "This video was removed or marked private on YouTube."
                    2 -> "Invalid YouTube video identifier."
                    5 -> "HTML5 player error on YouTube."
                    else -> "Unable to play this track from YouTube (error code $errorCode)."
                }
                playbackListener?.onPlaybackError(message)
            }
        }
    }

    private fun buildPlayerHtml(): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body { width: 100%; height: 100%; background-color: #08080C; overflow: hidden; }
                    #player { width: 100%; height: 100%; position: absolute; top: 0; left: 0; }
                </style>
            </head>
            <body>
                <div id="player"></div>
                <script>
                    var tag = document.createElement('script');
                    tag.src = "https://www.youtube.com/iframe_api";
                    var firstScriptTag = document.getElementsByTagName('script')[0];
                    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                    var player = null;
                    var isReady = false;
                    var pendingCmd = null;
                    var timeInterval = null;

                    function onYouTubeIframeAPIReady() {
                        player = new YT.Player('player', {
                            height: '100%',
                            width: '100%',
                            playerVars: {
                                'autoplay': 1,
                                'controls': 1,
                                'playsinline': 1,
                                'rel': 0,
                                'fs': 0,
                                'modestbranding': 1,
                                'enablejsapi': 1,
                                'origin': 'https://www.youtube.com'
                            },
                            events: {
                                'onReady': onPlayerReady,
                                'onStateChange': onPlayerStateChange,
                                'onError': onPlayerError
                            }
                        });
                    }

                    function onPlayerReady(event) {
                        isReady = true;
                        if (window.$JS_BRIDGE_NAME) {
                            window.$JS_BRIDGE_NAME.onReady();
                        }
                        if (pendingCmd) {
                            var cmd = pendingCmd;
                            pendingCmd = null;
                            cmd();
                        }
                        startTimePolling();
                    }

                    function onPlayerStateChange(event) {
                        var cur = (player && typeof player.getCurrentTime === 'function') ? player.getCurrentTime() : 0;
                        var dur = (player && typeof player.getDuration === 'function') ? player.getDuration() : 0;
                        if (window.$JS_BRIDGE_NAME) {
                            window.$JS_BRIDGE_NAME.onStateChange(event.data, cur, dur);
                        }
                    }

                    function onPlayerError(event) {
                        if (window.$JS_BRIDGE_NAME) {
                            window.$JS_BRIDGE_NAME.onError(event.data);
                        }
                    }

                    function startTimePolling() {
                        if (timeInterval) clearInterval(timeInterval);
                        timeInterval = setInterval(function() {
                            if (player && typeof player.getPlayerState === 'function' && player.getPlayerState() === 1) {
                                var cur = player.getCurrentTime() || 0;
                                var dur = player.getDuration() || 0;
                                if (window.$JS_BRIDGE_NAME) {
                                    window.$JS_BRIDGE_NAME.onTimeUpdate(cur, dur);
                                }
                            }
                        }, 500);
                    }

                    function loadVideo(videoId, startSeconds) {
                        if (isReady && player && typeof player.loadVideoById === 'function') {
                            player.loadVideoById({
                                videoId: videoId,
                                startSeconds: startSeconds || 0
                            });
                        } else {
                            pendingCmd = function() { loadVideo(videoId, startSeconds); };
                        }
                    }

                    function playVideo() {
                        if (isReady && player && typeof player.playVideo === 'function') {
                            player.playVideo();
                        } else {
                            pendingCmd = function() { playVideo(); };
                        }
                    }

                    function pauseVideo() {
                        if (isReady && player && typeof player.pauseVideo === 'function') {
                            player.pauseVideo();
                        }
                    }

                    function seekTo(seconds) {
                        if (isReady && player && typeof player.seekTo === 'function') {
                            player.seekTo(seconds, true);
                        }
                    }

                    function stopVideo() {
                        if (isReady && player && typeof player.stopVideo === 'function') {
                            player.stopVideo();
                        }
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
