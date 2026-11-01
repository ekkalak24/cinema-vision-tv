package com.cinemavision.tv.ui.hud

import android.view.KeyEvent
import com.cinemavision.tv.domain.BASELINE_ACCURACY
import com.cinemavision.tv.domain.CinemaAccuracyLevel
import com.cinemavision.tv.domain.ColorRange
import com.cinemavision.tv.domain.PatternStep
import com.cinemavision.tv.domain.TvBrand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HudStateTest {

    private lateinit var state: HudState

    @Before
    fun setUp() {
        state = HudState(autoHideDelayMs = 6000L)
    }

    @Test
    fun testInitialState() {
        assertTrue("HUD overlay should be visible by default", state.isHudVisible)
        assertEquals("Initial step should be Picture Mode", PatternStep.PICTURE_MODE_BASE, state.currentStep)
        assertEquals("Default brand should be SONY", TvBrand.SONY, state.selectedBrand)
        assertEquals("Default color range should be LIMITED", ColorRange.LIMITED, state.colorRange)
        assertTrue("No steps calibrated initially", state.calibratedSteps.isEmpty())
        assertEquals("Baseline accuracy should match BASELINE_ACCURACY (35%)", BASELINE_ACCURACY, state.accuracyPercent)
        assertEquals("Baseline accuracy level should be FACTORY_DEFAULT", CinemaAccuracyLevel.FACTORY_DEFAULT, state.accuracyLevel)
        assertEquals("Step 1 contribution should be 20%", 20, state.currentStepWeight)
        assertFalse("Step 1 should not be calibrated initially", state.isCurrentStepCalibrated)
        assertNotNull("Brand tip should not be null", state.brandTip)
        assertTrue("Brand tip should contain Sony picture mode guidance", state.brandTip.contains("Custom"))
    }

    @Test
    fun testStepNavigationForwardAndBackward() {
        // Forward navigation
        assertTrue(state.nextStep())
        assertEquals(PatternStep.ASPECT_RATIO, state.currentStep)

        assertTrue(state.nextStep())
        assertEquals(PatternStep.BLACK_LEVEL_PLUGE, state.currentStep)

        assertTrue(state.nextStep())
        assertEquals(PatternStep.WHITE_LEVEL_CONTRAST, state.currentStep)

        assertTrue(state.nextStep())
        assertEquals(PatternStep.SHARPNESS_GRID, state.currentStep)

        assertTrue(state.nextStep())
        assertEquals(PatternStep.GRAYSCALE_D65, state.currentStep)

        assertTrue(state.nextStep())
        assertEquals(PatternStep.MOTION_24P_JUDDER, state.currentStep)

        // At end, nextStep returns false and does not overflow
        assertFalse(state.nextStep())
        assertEquals(PatternStep.MOTION_24P_JUDDER, state.currentStep)

        // Backward navigation
        assertTrue(state.previousStep())
        assertEquals(PatternStep.GRAYSCALE_D65, state.currentStep)

        // Jump to first step
        state.setStep(PatternStep.PICTURE_MODE_BASE)
        assertEquals(PatternStep.PICTURE_MODE_BASE, state.currentStep)

        // At start, previousStep returns false and does not underflow
        assertFalse(state.previousStep())
        assertEquals(PatternStep.PICTURE_MODE_BASE, state.currentStep)
    }

    @Test
    fun testBrandSelectionAndCycling() {
        state.selectBrand(TvBrand.TCL)
        assertEquals(TvBrand.TCL, state.selectedBrand)
        assertTrue("TCL tip for step 1 should contain Movie", state.brandTip.contains("Movie"))

        // Cycle through brands
        state.nextBrand() // HISENSE
        assertEquals(TvBrand.HISENSE, state.selectedBrand)

        state.nextBrand() // SAMSUNG_BOX
        assertEquals(TvBrand.SAMSUNG_BOX, state.selectedBrand)

        state.nextBrand() // LG_BOX
        assertEquals(TvBrand.LG_BOX, state.selectedBrand)

        state.nextBrand() // UNIVERSAL
        assertEquals(TvBrand.UNIVERSAL, state.selectedBrand)

        state.nextBrand() // wraps back to SONY
        assertEquals(TvBrand.SONY, state.selectedBrand)

        state.previousBrand() // wraps backwards to UNIVERSAL
        assertEquals(TvBrand.UNIVERSAL, state.selectedBrand)
    }

    @Test
    fun testToggleStepCalibratedUpdatesAccuracyAndLevel() {
        assertEquals(35, state.accuracyPercent)
        assertEquals(CinemaAccuracyLevel.FACTORY_DEFAULT, state.accuracyLevel)

        // Calibrate Step 1 (+20%)
        val cal1 = state.toggleStepCalibrated(PatternStep.PICTURE_MODE_BASE)
        assertTrue(cal1)
        assertTrue(state.isStepCalibrated(PatternStep.PICTURE_MODE_BASE))
        assertEquals(55, state.accuracyPercent)
        assertEquals(CinemaAccuracyLevel.IMPROVED, state.accuracyLevel)

        // Calibrate Step 3 (+15%)
        state.setStepCalibrated(PatternStep.BLACK_LEVEL_PLUGE, true)
        assertEquals(70, state.accuracyPercent)
        assertEquals(CinemaAccuracyLevel.IMPROVED, state.accuracyLevel)

        // Calibrate Step 4 (+10%)
        state.setStepCalibrated(PatternStep.WHITE_LEVEL_CONTRAST, true)
        assertEquals(80, state.accuracyPercent)
        assertEquals(CinemaAccuracyLevel.HIGH_FIDELITY, state.accuracyLevel)

        // Calibrate Step 2 (+10%) -> 90%
        state.setStepCalibrated(PatternStep.ASPECT_RATIO, true)
        assertEquals(90, state.accuracyPercent)
        assertEquals(CinemaAccuracyLevel.CINEMA_REFERENCE, state.accuracyLevel)

        // Uncalibrate Step 1 (-20%) while only steps 1, 2, 3, 4 are active
        // Before all steps: gained was 55 (35 + 55 = 90)
        // With step 1 removed: gained becomes 35 (35 + 35 = 70)
        state.setStepCalibrated(PatternStep.PICTURE_MODE_BASE, false)
        assertEquals(70, state.accuracyPercent)
        assertEquals(CinemaAccuracyLevel.IMPROVED, state.accuracyLevel)

        // Calibrate all steps: total gained = 90 -> 35 + 90 = 125 -> capped at 100
        PatternStep.values().forEach { state.setStepCalibrated(it, true) }
        assertEquals(100, state.accuracyPercent)
        assertEquals(CinemaAccuracyLevel.CINEMA_REFERENCE, state.accuracyLevel)
    }

    @Test
    fun testCurrentStepWeightContribution() {
        state.setStep(PatternStep.PICTURE_MODE_BASE)
        assertEquals(20, state.currentStepWeight)

        state.setStep(PatternStep.ASPECT_RATIO)
        assertEquals(10, state.currentStepWeight)

        state.setStep(PatternStep.BLACK_LEVEL_PLUGE)
        assertEquals(15, state.currentStepWeight)

        state.setStep(PatternStep.WHITE_LEVEL_CONTRAST)
        assertEquals(10, state.currentStepWeight)

        state.setStep(PatternStep.SHARPNESS_GRID)
        assertEquals(10, state.currentStepWeight)

        state.setStep(PatternStep.GRAYSCALE_D65)
        assertEquals(10, state.currentStepWeight)

        state.setStep(PatternStep.MOTION_24P_JUDDER)
        assertEquals(15, state.currentStepWeight)
    }

    @Test
    fun testColorRangeToggle() {
        assertEquals(ColorRange.LIMITED, state.colorRange)
        val newRange = state.toggleColorRange()
        assertEquals(ColorRange.FULL, newRange)
        assertEquals(ColorRange.FULL, state.colorRange)

        state.toggleColorRange()
        assertEquals(ColorRange.LIMITED, state.colorRange)
    }

    @Test
    fun testToggleHudVisibility() {
        assertTrue(state.isHudVisible)

        val visibleAfterToggle = state.toggleHudVisibility()
        assertFalse(visibleAfterToggle)
        assertFalse(state.isHudVisible)

        val visibleAgain = state.toggleHudVisibility()
        assertTrue(visibleAgain)
        assertTrue(state.isHudVisible)

        state.hideHud()
        assertFalse(state.isHudVisible)

        state.showHud()
        assertTrue(state.isHudVisible)
    }

    @Test
    fun testAutoHideTimerTransitionsToHiddenAfterDelay() {
        // Fast timer for unit test
        val shortState = HudState(autoHideDelayMs = 60L)
        assertTrue(shortState.isHudVisible)

        Thread.sleep(120L)
        assertFalse("HUD should auto-hide after 60ms delay", shortState.isHudVisible)
        shortState.cancelAutoHideTimer()
    }

    @Test
    fun testUserInteractionResetsAutoHideTimer() {
        val shortState = HudState(autoHideDelayMs = 100L)
        assertTrue(shortState.isHudVisible)

        // Wait 50ms, then trigger interaction
        Thread.sleep(50L)
        shortState.onUserInteraction()

        // Wait another 60ms (110ms total from start, but only 60ms from interaction)
        Thread.sleep(60L)
        assertTrue("HUD should still be visible because timer was reset", shortState.isHudVisible)

        // Wait another 70ms (130ms from interaction)
        Thread.sleep(70L)
        assertFalse("HUD should auto-hide after timer expires", shortState.isHudVisible)
        shortState.cancelAutoHideTimer()
    }

    @Test
    fun testUserInteractionRestoresVisibilityWhenHidden() {
        state.hideHud()
        assertFalse(state.isHudVisible)

        state.onUserInteraction()
        assertTrue("User interaction should restore HUD visibility", state.isHudVisible)
        state.cancelAutoHideTimer()
    }

    @Test
    fun testHandleKeyEventNavigation() {
        // DPAD_RIGHT -> next step
        assertTrue(state.handleKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT))
        assertEquals(PatternStep.ASPECT_RATIO, state.currentStep)

        // DPAD_LEFT -> previous step
        assertTrue(state.handleKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT))
        assertEquals(PatternStep.PICTURE_MODE_BASE, state.currentStep)

        // DPAD_CENTER -> toggle HUD
        assertTrue(state.handleKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER))
        assertFalse(state.isHudVisible)

        // DPAD_RIGHT when hidden -> restores visibility and moves to next step
        assertTrue(state.handleKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT))
        assertTrue("DPAD navigation should restore HUD visibility", state.isHudVisible)
        assertEquals(PatternStep.ASPECT_RATIO, state.currentStep)

        // DPAD_DOWN -> next brand
        assertTrue(state.handleKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(TvBrand.TCL, state.selectedBrand)

        // KEYCODE_C -> toggle calibration
        assertTrue(state.handleKeyEvent(KeyEvent.KEYCODE_C))
        assertTrue(state.isStepCalibrated(PatternStep.ASPECT_RATIO))

        // Unknown key -> not handled
        assertFalse(state.handleKeyEvent(KeyEvent.KEYCODE_VOLUME_UP))
        state.cancelAutoHideTimer()
    }
}
