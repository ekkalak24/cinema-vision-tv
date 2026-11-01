package com.cinemavision.tv.ui.player

import android.content.Context
import android.content.ContextWrapper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.cinemavision.tv.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JudderPlayerManagerTest {

    private lateinit var fakePlayer: FakeExoPlayer
    private lateinit var fakeContext: Context
    private lateinit var manager: JudderPlayerManager

    @Before
    fun setUp() {
        fakePlayer = FakeExoPlayer()
        fakeContext = object : ContextWrapper(null) {
            override fun getPackageName(): String = "com.cinemavision.tv"
        }
        manager = JudderPlayerManager(playerFactory = { fakePlayer })
    }

    @Test
    fun testTargetFrameRateIs24Fps() {
        assertEquals(24.0f, manager.targetFrameRate, 0.001f)
    }

    @Test
    fun testInitialStateBeforeInitialize() {
        assertNull(manager.player)
        assertFalse(manager.isPlaying)
    }

    @Test
    fun testInitializeConfiguresMutedRepeatModeAllAndAutoPlay() {
        manager.initialize(fakeContext)

        assertNotNull(manager.player)
        assertEquals(Player.REPEAT_MODE_ALL, fakePlayer.repeatMode)
        assertEquals(0.0f, fakePlayer.volume, 0.001f)
        assertTrue(fakePlayer.playWhenReady)
        assertTrue(fakePlayer.isPrepared)
    }

    @Test
    fun testInitializeSetsMediaItemWithCorrectUri() {
        manager.initialize(fakeContext)

        assertNotNull(fakePlayer.currentMediaItem)
        val expectedUriString = "android.resource://com.cinemavision.tv/${R.raw.judder_24p_test}"
        assertEquals(expectedUriString, fakePlayer.currentMediaItem?.uri?.toString())
    }

    @Test
    fun testInitializeWithCustomRawResId() {
        val customRawId = 0x7f0f0099
        manager.initialize(fakeContext, customRawId)

        val expectedUriString = "android.resource://com.cinemavision.tv/$customRawId"
        assertEquals(expectedUriString, fakePlayer.currentMediaItem?.uri?.toString())
    }

    @Test
    fun testAttachPlayerViewBindsPlayer() {
        manager.initialize(fakeContext)
        val playerView = PlayerView(fakeContext)

        manager.attachPlayerView(playerView)

        assertEquals(fakePlayer, playerView.player)
    }

    @Test
    fun testPauseAndResumeCallsPlayer() {
        manager.initialize(fakeContext)

        manager.resume()
        assertTrue(manager.isPlaying)

        manager.pause()
        assertFalse(manager.isPlaying)
    }

    @Test
    fun testIsPlayingReflectsPlaybackState() {
        manager.initialize(fakeContext)

        fakePlayer.playingState = true
        assertTrue(manager.isPlaying)

        fakePlayer.playingState = false
        assertFalse(manager.isPlaying)
    }

    @Test
    fun testReleaseTearsDownCleanly() {
        manager.initialize(fakeContext)
        fakePlayer.playingState = true

        manager.release()

        assertTrue(fakePlayer.isReleased)
        assertNull(manager.player)
        assertFalse(manager.isPlaying)
    }

    @Test
    fun testSafeOperationsWhenUninitialized() {
        manager.pause()
        manager.resume()
        manager.release()
        assertNull(manager.player)
        assertFalse(manager.isPlaying)
    }

    /**
     * Test double / fake implementing [ExoPlayer] to verify state and lifecycle invocations.
     */
    class FakeExoPlayer : ExoPlayer {
        var repeatModeVal: Int = Player.REPEAT_MODE_OFF
        var volumeVal: Float = 1.0f
        var playWhenReadyVal: Boolean = false
        var playingState: Boolean = false
        var isPrepared: Boolean = false
        var isReleased: Boolean = false
        var currentMediaItem: MediaItem? = null

        override fun getRepeatMode(): Int = repeatModeVal
        override fun setRepeatMode(repeatMode: Int) {
            this.repeatModeVal = repeatMode
        }

        override fun getVolume(): Float = volumeVal
        override fun setVolume(volume: Float) {
            this.volumeVal = volume
        }

        override fun getPlayWhenReady(): Boolean = playWhenReadyVal
        override fun setPlayWhenReady(playWhenReady: Boolean) {
            this.playWhenReadyVal = playWhenReady
        }

        override fun isPlaying(): Boolean = playingState

        override fun play() {
            playingState = true
        }

        override fun pause() {
            playingState = false
        }

        override fun prepare() {
            isPrepared = true
        }

        override fun release() {
            isReleased = true
            playingState = false
        }

        override fun stop() {
            playingState = false
        }

        override fun setMediaItem(mediaItem: MediaItem) {
            this.currentMediaItem = mediaItem
        }
    }
}
