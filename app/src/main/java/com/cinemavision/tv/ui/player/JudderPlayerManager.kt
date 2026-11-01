package com.cinemavision.tv.ui.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.cinemavision.tv.R

/**
 * Lifecycle-aware player manager utilizing AndroidX Media3 ExoPlayer for the
 * 24p True Film Cadence test pattern.
 *
 * Configures seamless looping ([Player.REPEAT_MODE_ALL]), muted audio (0.0f),
 * and provides frame-accurate playback to identify 3:2 pulldown judder and motion smoothing.
 */
class JudderPlayerManager(
    private val playerFactory: ((Context) -> ExoPlayer)? = null
) {
    /**
     * Active ExoPlayer instance, or null if released / uninitialized.
     */
    var player: ExoPlayer? = null
        private set

    /**
     * Target cadence frame rate for film content (24.0 fps).
     */
    val targetFrameRate: Float = 24.0f

    /**
     * Indicates whether the underlying player is actively playing.
     */
    val isPlaying: Boolean
        get() = player?.isPlaying ?: false

    /**
     * Initializes the ExoPlayer instance with the given [rawResId], configuring
     * seamless repeat mode, muted audio, and auto-play when ready.
     */
    fun initialize(context: Context, rawResId: Int = R.raw.judder_24p_test) {
        val exoPlayer = playerFactory?.invoke(context) ?: ExoPlayer.Builder(context).build()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.volume = 0.0f
        exoPlayer.playWhenReady = true

        val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        this.player = exoPlayer
    }

    /**
     * Attaches this player instance to the provided [playerView].
     */
    fun attachPlayerView(playerView: PlayerView) {
        playerView.player = player
    }

    /**
     * Pauses playback if active.
     */
    fun pause() {
        player?.pause()
    }

    /**
     * Resumes playback if player is initialized.
     */
    fun resume() {
        player?.play()
    }

    /**
     * Releases media player resources and detaches the player.
     */
    fun release() {
        player?.release()
        player = null
    }
}
