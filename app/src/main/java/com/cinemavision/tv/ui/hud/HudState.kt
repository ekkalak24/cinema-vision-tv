package com.cinemavision.tv.ui.hud

import android.view.KeyEvent
import com.cinemavision.tv.domain.BrandTipProvider
import com.cinemavision.tv.domain.CinemaAccuracyCalculator
import com.cinemavision.tv.domain.CinemaAccuracyLevel
import com.cinemavision.tv.domain.ColorRange
import com.cinemavision.tv.domain.PatternStep
import com.cinemavision.tv.domain.TvBrand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Default auto-hide timeout for HUD overlay (6.0 seconds of inactivity).
 */
const val DEFAULT_AUTO_HIDE_DELAY_MS: Long = 6000L

/**
 * Manages reactive state and remote-control navigation for the CinemaVision TV HUD overlay.
 *
 * Tracks current calibration pattern step, TV manufacturer brand setting guidance,
 * color space range, verified calibrated steps, live Cinema Reference Accuracy match score (%),
 * and controls auto-hiding behavior to ensure clean pattern inspection.
 */
class HudState(
    val autoHideDelayMs: Long = DEFAULT_AUTO_HIDE_DELAY_MS,
    private val coroutineScope: CoroutineScope? = null
) {
    /**
     * Whether the interactive HUD overlay is currently visible to the viewer.
     */
    var isHudVisible: Boolean = true
        internal set

    /**
     * Currently active test pattern calibration step (1..7).
     */
    var currentStep: PatternStep = PatternStep.PICTURE_MODE_BASE
        internal set

    /**
     * Selected TV brand for manufacturer-specific menu setting instructions.
     */
    var selectedBrand: TvBrand = TvBrand.SONY
        internal set

    /**
     * Active video signal color range (LIMITED 16-235 standard or FULL 0-255).
     */
    var colorRange: ColorRange = ColorRange.LIMITED
        internal set

    private val _calibratedSteps = mutableSetOf<PatternStep>()

    /**
     * Set of steps verified and marked as calibrated by the user.
     */
    val calibratedSteps: Set<PatternStep>
        get() = _calibratedSteps.toSet()

    /**
     * Current Cinema Reference Accuracy Match percentage (baseline ~35% up to 100%).
     */
    val accuracyPercent: Int
        get() = CinemaAccuracyCalculator.calculateAccuracy(_calibratedSteps)

    /**
     * High-level cinema fidelity tier (Factory Default, Improved, High Fidelity, Cinema Reference).
     */
    val accuracyLevel: CinemaAccuracyLevel
        get() = CinemaAccuracyCalculator.getLevel(accuracyPercent)

    /**
     * Weight percentage contribution toward reference standard for the active step.
     */
    val currentStepWeight: Int
        get() = currentStep.weightPercent

    /**
     * Indicates whether the active calibration step has been verified and calibrated.
     */
    val isCurrentStepCalibrated: Boolean
        get() = _calibratedSteps.contains(currentStep)

    /**
     * Contextual menu setting tip for the currently active brand and step.
     */
    val brandTip: String
        get() = BrandTipProvider.getTipForStep(selectedBrand, currentStep)

    private var autoHideJob: Job? = null

    init {
        startAutoHideTimer()
    }

    /**
     * Checks if the specified [step] has been marked as calibrated.
     */
    fun isStepCalibrated(step: PatternStep): Boolean = _calibratedSteps.contains(step)

    /**
     * Updates the calibration verification status of [step].
     */
    fun setStepCalibrated(step: PatternStep, calibrated: Boolean) {
        if (calibrated) {
            _calibratedSteps.add(step)
        } else {
            _calibratedSteps.remove(step)
        }
        onUserInteraction()
    }

    /**
     * Toggles the calibration verification status of [step] (defaults to [currentStep]).
     */
    fun toggleStepCalibrated(step: PatternStep = currentStep): Boolean {
        val willBeCalibrated = !_calibratedSteps.contains(step)
        setStepCalibrated(step, willBeCalibrated)
        return willBeCalibrated
    }

    /**
     * Advances to the next calibration step if available.
     * Restores HUD visibility and resets the auto-hide timer.
     * Returns true if step changed, false if already on last step.
     */
    fun nextStep(): Boolean {
        val next = currentStep.next()
        return if (next != null) {
            currentStep = next
            onUserInteraction()
            true
        } else {
            onUserInteraction()
            false
        }
    }

    /**
     * Moves to the previous calibration step if available.
     * Restores HUD visibility and resets the auto-hide timer.
     * Returns true if step changed, false if already on first step.
     */
    fun previousStep(): Boolean {
        val prev = currentStep.previous()
        return if (prev != null) {
            currentStep = prev
            onUserInteraction()
            true
        } else {
            onUserInteraction()
            false
        }
    }

    /**
     * Directly navigates to the specified [step].
     */
    fun setStep(step: PatternStep) {
        currentStep = step
        onUserInteraction()
    }

    /**
     * Sets the active TV brand to [brand].
     */
    fun selectBrand(brand: TvBrand) {
        selectedBrand = brand
        onUserInteraction()
    }

    /**
     * Cycles to the next TV brand in the supported list.
     */
    fun nextBrand() {
        val brands = TvBrand.values()
        val nextIndex = (selectedBrand.ordinal + 1) % brands.size
        selectedBrand = brands[nextIndex]
        onUserInteraction()
    }

    /**
     * Cycles to the previous TV brand in the supported list.
     */
    fun previousBrand() {
        val brands = TvBrand.values()
        val prevIndex = if (selectedBrand.ordinal - 1 < 0) brands.size - 1 else selectedBrand.ordinal - 1
        selectedBrand = brands[prevIndex]
        onUserInteraction()
    }

    /**
     * Toggles between [ColorRange.LIMITED] (16-235) and [ColorRange.FULL] (0-255).
     */
    fun toggleColorRange(): ColorRange {
        colorRange = if (colorRange == ColorRange.LIMITED) ColorRange.FULL else ColorRange.LIMITED
        onUserInteraction()
        return colorRange
    }

    /**
     * Toggles HUD overlay visibility immediately.
     */
    fun toggleHudVisibility(): Boolean {
        isHudVisible = !isHudVisible
        if (isHudVisible) {
            startAutoHideTimer()
        } else {
            cancelAutoHideTimer()
        }
        return isHudVisible
    }

    /**
     * Makes the HUD visible and restarts auto-hide timer.
     */
    fun showHud() {
        isHudVisible = true
        startAutoHideTimer()
    }

    /**
     * Hides the HUD overlay and cancels pending auto-hide timer.
     */
    fun hideHud() {
        isHudVisible = false
        cancelAutoHideTimer()
    }

    /**
     * Resets the auto-hide countdown and restores visibility upon remote control user interaction.
     */
    fun onUserInteraction() {
        isHudVisible = true
        startAutoHideTimer()
    }

    /**
     * Starts the auto-hide countdown coroutine if [autoHideDelayMs] > 0 and HUD is visible.
     */
    fun startAutoHideTimer() {
        cancelAutoHideTimer()
        if (autoHideDelayMs > 0 && isHudVisible) {
            val scope = coroutineScope ?: CoroutineScope(Dispatchers.Default)
            autoHideJob = scope.launch {
                delay(autoHideDelayMs)
                isHudVisible = false
            }
        }
    }

    /**
     * Cancels any active auto-hide countdown coroutine.
     */
    fun cancelAutoHideTimer() {
        autoHideJob?.cancel()
        autoHideJob = null
    }

    /**
     * Dispatches TV remote D-Pad key events to HUD navigation.
     *
     * - `DPAD_LEFT`: Previous step
     * - `DPAD_RIGHT`: Next step
     * - `DPAD_UP`: Previous brand
     * - `DPAD_DOWN`: Next brand
     * - `DPAD_CENTER` / `ENTER`: Toggle HUD visibility
     * - `KEYCODE_C`: Toggle step calibrated status
     *
     * Returns true if the key event was consumed by navigation, false otherwise.
     */
    fun handleKeyEvent(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                previousStep()
                true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                nextStep()
                true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                previousBrand()
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                nextBrand()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                toggleHudVisibility()
                true
            }
            KeyEvent.KEYCODE_C -> {
                toggleStepCalibrated()
                true
            }
            else -> false
        }
    }
}
