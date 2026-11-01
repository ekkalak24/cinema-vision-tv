package com.cinemavision.tv

import android.view.KeyEvent
import android.view.View
import androidx.media3.ui.PlayerView
import com.cinemavision.tv.domain.ColorRange
import com.cinemavision.tv.domain.PatternStep
import com.cinemavision.tv.ui.hud.HudState
import com.cinemavision.tv.ui.pattern.PatternSurfaceView
import com.cinemavision.tv.ui.player.JudderPlayerManager

/**
 * Abstraction target for test pattern display layer.
 */
interface PatternDisplayTarget {
    var currentStep: PatternStep
    var colorRange: ColorRange
    var isVisible: Boolean
    fun requestRender()
}

/**
 * Abstraction target for video player display layer.
 */
interface PlayerDisplayTarget {
    var isVisible: Boolean
    fun attachToPlayer(manager: JudderPlayerManager)
}

/**
 * Concrete adapter binding [PatternSurfaceView] to [PatternDisplayTarget].
 */
class SurfaceViewPatternTarget(
    private val surfaceView: PatternSurfaceView
) : PatternDisplayTarget {
    override var currentStep: PatternStep
        get() = surfaceView.currentStep
        set(value) {
            surfaceView.currentStep = value
        }

    override var colorRange: ColorRange
        get() = surfaceView.colorRange
        set(value) {
            surfaceView.colorRange = value
        }

    override var isVisible: Boolean
        get() = surfaceView.visibility == View.VISIBLE
        set(value) {
            surfaceView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override fun requestRender() {
        surfaceView.requestRender()
    }
}

/**
 * Concrete adapter binding [PlayerView] to [PlayerDisplayTarget].
 */
class ExoPlayerViewTarget(
    private val playerView: PlayerView
) : PlayerDisplayTarget {
    override var isVisible: Boolean
        get() = playerView.visibility == View.VISIBLE
        set(value) {
            playerView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override fun attachToPlayer(manager: JudderPlayerManager) {
        manager.attachPlayerView(playerView)
    }
}

/**
 * Coordinates dual-layer rendering (Pattern Canvas & Media3 Judder Player), HUD state synchronization,
 * TV remote key dispatching, and lifecycle management for CinemaVision TV.
 */
class MainActivityCoordinator(
    val hudState: HudState,
    val playerManager: JudderPlayerManager,
    val patternTarget: PatternDisplayTarget,
    val playerTarget: PlayerDisplayTarget
) {

    constructor(
        hudState: HudState,
        playerManager: JudderPlayerManager,
        patternSurfaceView: PatternSurfaceView,
        playerView: PlayerView
    ) : this(
        hudState = hudState,
        playerManager = playerManager,
        patternTarget = SurfaceViewPatternTarget(patternSurfaceView),
        playerTarget = ExoPlayerViewTarget(playerView)
    )

    init {
        val previousListener = hudState.onStateChanged
        hudState.onStateChanged = {
            previousListener?.invoke()
            syncWithState()
        }
        syncWithState()
    }

    /**
     * Synchronizes display layers with the active [HudState]:
     * - If [PatternStep.MOTION_24P_JUDDER], displays and plays the Media3 judder video, dimming the pattern view.
     * - Otherwise, hides and pauses the judder player, displaying the corresponding test pattern on canvas.
     */
    fun syncWithState() {
        val step = hudState.currentStep
        val range = hudState.colorRange

        if (step == PatternStep.MOTION_24P_JUDDER) {
            playerTarget.isVisible = true
            playerTarget.attachToPlayer(playerManager)
            playerManager.resume()
            patternTarget.isVisible = false
        } else {
            playerTarget.isVisible = false
            playerManager.pause()
            patternTarget.isVisible = true
            patternTarget.currentStep = step
            patternTarget.colorRange = range
            patternTarget.requestRender()
        }
    }

    /**
     * Handles remote navigation key codes. Only processes [KeyEvent.ACTION_DOWN].
     * Returns true if consumed, false otherwise.
     */
    fun handleKeyEvent(keyCode: Int, action: Int = KeyEvent.ACTION_DOWN): Boolean {
        if (action == KeyEvent.ACTION_DOWN) {
            return hudState.handleKeyEvent(keyCode)
        }
        return false
    }

    /**
     * Dispatches an Android [KeyEvent] to the navigation coordinator.
     */
    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return handleKeyEvent(event.keyCode, event.action)
    }

    /**
     * Resumes playback if currently on the 24p Judder cadence step.
     */
    fun onResume() {
        if (hudState.currentStep == PatternStep.MOTION_24P_JUDDER) {
            playerManager.resume()
        }
    }

    /**
     * Pauses video playback on activity pause to prevent background CPU/GPU usage.
     */
    fun onPause() {
        playerManager.pause()
    }

    /**
     * Releases media player resources upon activity destruction.
     */
    fun onDestroy() {
        playerManager.release()
    }
}
