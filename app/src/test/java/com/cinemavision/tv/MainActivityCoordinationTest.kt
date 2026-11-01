package com.cinemavision.tv

import android.content.Context
import android.content.ContextWrapper
import android.view.KeyEvent
import com.cinemavision.tv.domain.ColorRange
import com.cinemavision.tv.domain.PatternStep
import com.cinemavision.tv.ui.hud.HudState
import com.cinemavision.tv.ui.player.JudderPlayerManager
import com.cinemavision.tv.ui.player.JudderPlayerManagerTest.FakeExoPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MainActivityCoordinationTest {

    private lateinit var hudState: HudState
    private lateinit var fakePlayer: FakeExoPlayer
    private lateinit var fakeContext: Context
    private lateinit var playerManager: JudderPlayerManager
    private lateinit var patternTarget: FakePatternDisplayTarget
    private lateinit var playerTarget: FakePlayerDisplayTarget
    private lateinit var coordinator: MainActivityCoordinator

    @Before
    fun setUp() {
        hudState = HudState(autoHideDelayMs = 0L) // Disable auto-hide for deterministic tests
        fakePlayer = FakeExoPlayer()
        fakeContext = object : ContextWrapper(null) {
            override fun getPackageName(): String = "com.cinemavision.tv"
        }
        playerManager = JudderPlayerManager(playerFactory = { fakePlayer })
        playerManager.initialize(fakeContext)

        patternTarget = FakePatternDisplayTarget()
        playerTarget = FakePlayerDisplayTarget()

        coordinator = MainActivityCoordinator(
            hudState = hudState,
            playerManager = playerManager,
            patternTarget = patternTarget,
            playerTarget = playerTarget
        )
    }

    @Test
    fun testInitialStateSynchronizesPatternViewAndHidesPlayer() {
        assertEquals(PatternStep.PICTURE_MODE_BASE, hudState.currentStep)
        assertTrue("Pattern view should be visible on initial step", patternTarget.isVisible)
        assertEquals(PatternStep.PICTURE_MODE_BASE, patternTarget.currentStep)
        assertEquals(ColorRange.LIMITED, patternTarget.colorRange)

        assertFalse("Player view should be hidden on initial step", playerTarget.isVisible)
        assertFalse("Player should not be playing on initial step", playerManager.isPlaying)
    }

    @Test
    fun testStepSwitchingSteps1Through6ShowPatternAndHidePlayer() {
        val staticSteps = listOf(
            PatternStep.PICTURE_MODE_BASE,
            PatternStep.ASPECT_RATIO,
            PatternStep.BLACK_LEVEL_PLUGE,
            PatternStep.WHITE_LEVEL_CONTRAST,
            PatternStep.SHARPNESS_GRID,
            PatternStep.GRAYSCALE_D65
        )

        for (step in staticSteps) {
            hudState.setStep(step)

            assertTrue("Pattern view should be visible for step $step", patternTarget.isVisible)
            assertEquals("Pattern view step should match $step", step, patternTarget.currentStep)
            assertFalse("Player view should be hidden for step $step", playerTarget.isVisible)
            assertFalse("Player should be paused for step $step", playerManager.isPlaying)
        }
    }

    @Test
    fun testStepSwitchingStep7Motion24pShowsPlayerAndHidesPattern() {
        hudState.setStep(PatternStep.MOTION_24P_JUDDER)

        assertTrue("Player view should be visible for Judder step", playerTarget.isVisible)
        assertEquals("Player view should be attached to manager", playerManager, playerTarget.attachedManager)
        assertTrue("Player should be playing for Judder step", playerManager.isPlaying)
        assertFalse("Pattern view should be hidden/dimmed for Judder step", patternTarget.isVisible)
    }

    @Test
    fun testStepSwitchingFromJudderBackToPatternPausesPlayerAndShowsPattern() {
        // Move to Judder step
        hudState.setStep(PatternStep.MOTION_24P_JUDDER)
        assertTrue(playerTarget.isVisible)
        assertTrue(playerManager.isPlaying)
        assertFalse(patternTarget.isVisible)

        // Move back to Grayscale D65
        hudState.setStep(PatternStep.GRAYSCALE_D65)
        assertFalse("Player view should be hidden when leaving Judder step", playerTarget.isVisible)
        assertFalse("Player should be paused when leaving Judder step", playerManager.isPlaying)
        assertTrue("Pattern view should be restored", patternTarget.isVisible)
        assertEquals(PatternStep.GRAYSCALE_D65, patternTarget.currentStep)
    }

    @Test
    fun testAllSevenStepsSequentialTransitions() {
        val allSteps = PatternStep.values()
        assertEquals(7, allSteps.size)

        for (step in allSteps) {
            hudState.setStep(step)
            if (step == PatternStep.MOTION_24P_JUDDER) {
                assertTrue("Player visible on $step", playerTarget.isVisible)
                assertTrue("Player playing on $step", playerManager.isPlaying)
                assertFalse("Pattern hidden on $step", patternTarget.isVisible)
            } else {
                assertFalse("Player hidden on $step", playerTarget.isVisible)
                assertFalse("Player paused on $step", playerManager.isPlaying)
                assertTrue("Pattern visible on $step", patternTarget.isVisible)
                assertEquals(step, patternTarget.currentStep)
            }
        }
    }

    @Test
    fun testColorRangeSynchronization() {
        assertEquals(ColorRange.LIMITED, patternTarget.colorRange)

        hudState.toggleColorRange()
        assertEquals(ColorRange.FULL, hudState.colorRange)
        assertEquals("Pattern target should synchronize to FULL color range", ColorRange.FULL, patternTarget.colorRange)

        hudState.toggleColorRange()
        assertEquals(ColorRange.LIMITED, hudState.colorRange)
        assertEquals("Pattern target should synchronize to LIMITED color range", ColorRange.LIMITED, patternTarget.colorRange)
    }

    @Test
    fun testRemoteKeyConsumptionWhenHudVisible() {
        assertTrue(hudState.isHudVisible)

        // DPAD_CENTER / ENTER toggles calibrated step
        assertFalse(hudState.isCurrentStepCalibrated)
        assertTrue(coordinator.handleKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN))
        assertTrue(hudState.isCurrentStepCalibrated)

        // DPAD_RIGHT advances step
        assertEquals(PatternStep.PICTURE_MODE_BASE, hudState.currentStep)
        assertTrue(coordinator.handleKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN))
        assertEquals(PatternStep.ASPECT_RATIO, hudState.currentStep)

        // DPAD_LEFT moves to previous step
        assertTrue(coordinator.handleKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_DOWN))
        assertEquals(PatternStep.PICTURE_MODE_BASE, hudState.currentStep)

        // DPAD_UP & DPAD_DOWN cycle brands
        assertTrue(coordinator.handleKeyEvent(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.ACTION_DOWN))
        assertTrue(coordinator.handleKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.ACTION_DOWN))

        // BACK hides HUD overlay
        assertTrue(hudState.isHudVisible)
        assertTrue(coordinator.handleKeyEvent(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_DOWN))
        assertFalse("HUD should be hidden by BACK key", hudState.isHudVisible)
    }

    @Test
    fun testRemoteKeyConsumptionWhenHudHidden() {
        hudState.hideHud()
        assertFalse(hudState.isHudVisible)

        // BACK key when HUD is hidden should NOT be consumed, allowing system back / app exit
        assertFalse("BACK when HUD is hidden should return false", coordinator.handleKeyEvent(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_DOWN))

        // DPAD_CENTER / ENTER unhides HUD
        assertTrue(coordinator.handleKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN))
        assertTrue("HUD should be shown again", hudState.isHudVisible)
    }

    @Test
    fun testNonActionDownKeyEventsNotConsumed() {
        // ACTION_UP should not be consumed by coordinator
        assertFalse(coordinator.handleKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP))
        assertFalse(coordinator.handleKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_UP))
        assertFalse(coordinator.handleKeyEvent(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_UP))
    }

    @Test
    fun testUnhandledKeyCodesNotConsumed() {
        assertFalse(coordinator.handleKeyEvent(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.ACTION_DOWN))
        assertFalse(coordinator.handleKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.ACTION_DOWN))
        assertFalse(coordinator.handleKeyEvent(KeyEvent.KEYCODE_MUTE, KeyEvent.ACTION_DOWN))
    }

    @Test
    fun testLifecycleOnPausePausesPlayer() {
        hudState.setStep(PatternStep.MOTION_24P_JUDDER)
        assertTrue(playerManager.isPlaying)

        coordinator.onPause()
        assertFalse("Player should be paused onPause", playerManager.isPlaying)
    }

    @Test
    fun testLifecycleOnResumeResumesPlayerOnJudderStep() {
        hudState.setStep(PatternStep.MOTION_24P_JUDDER)
        coordinator.onPause()
        assertFalse(playerManager.isPlaying)

        coordinator.onResume()
        assertTrue("Player should resume playback onResume if on Judder step", playerManager.isPlaying)
    }

    @Test
    fun testLifecycleOnResumeDoesNotResumePlayerOnStaticStep() {
        hudState.setStep(PatternStep.GRAYSCALE_D65)
        assertFalse(playerManager.isPlaying)

        coordinator.onResume()
        assertFalse("Player should not play onResume when on static pattern step", playerManager.isPlaying)
    }

    @Test
    fun testDispatchKeyEventObjectForwarding() {
        // Test dispatchKeyEvent with KeyEvent objects directly
        val enterDown = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        assertFalse(hudState.isCurrentStepCalibrated)
        assertTrue("dispatchKeyEvent should consume ACTION_DOWN ENTER", coordinator.dispatchKeyEvent(enterDown))
        assertTrue(hudState.isCurrentStepCalibrated)

        val rightDown = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)
        assertTrue(coordinator.dispatchKeyEvent(rightDown))
        assertEquals(PatternStep.ASPECT_RATIO, hudState.currentStep)

        val rightUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT)
        assertFalse("dispatchKeyEvent should not consume ACTION_UP", coordinator.dispatchKeyEvent(rightUp))
    }

    @Test
    fun testAdaptersForPatternSurfaceViewAndPlayerView() {
        val playerView = androidx.media3.ui.PlayerView(fakeContext)
        val playerAdapter = ExoPlayerViewTarget(playerView)

        // View visibility defaults to VISIBLE initially
        assertTrue(playerAdapter.isVisible)
        assertEquals(android.view.View.VISIBLE, playerView.visibility)

        playerAdapter.isVisible = false
        assertEquals(android.view.View.GONE, playerView.visibility)
        assertFalse(playerAdapter.isVisible)

        playerAdapter.isVisible = true
        assertEquals(android.view.View.VISIBLE, playerView.visibility)
        assertTrue(playerAdapter.isVisible)

        playerAdapter.attachToPlayer(playerManager)
        assertEquals(fakePlayer, playerView.player)
    }

    @Test
    fun testLifecycleOnDestroyReleasesPlayer() {
        coordinator.onDestroy()
        assertNull("Player should be released onDestroy", playerManager.player)
    }

    /**
     * Fake target representing PatternSurfaceView for testing state changes without native rendering.
     */
    class FakePatternDisplayTarget : PatternDisplayTarget {
        override var currentStep: PatternStep = PatternStep.BLACK_LEVEL_PLUGE
        override var colorRange: ColorRange = ColorRange.LIMITED
        override var isVisible: Boolean = true
        var renderRequestCount: Int = 0

        override fun requestRender() {
            renderRequestCount++
        }
    }

    /**
     * Fake target representing PlayerView for testing attachment and visibility.
     */
    class FakePlayerDisplayTarget : PlayerDisplayTarget {
        override var isVisible: Boolean = false
        var attachedManager: JudderPlayerManager? = null

        override fun attachToPlayer(manager: JudderPlayerManager) {
            this.attachedManager = manager
        }
    }
}
