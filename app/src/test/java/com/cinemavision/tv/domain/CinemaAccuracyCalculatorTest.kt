package com.cinemavision.tv.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CinemaAccuracyCalculatorTest {

    @Test
    fun testBaselineAccuracy() {
        assertEquals(35, BASELINE_ACCURACY)
        assertEquals(35, CinemaAccuracyCalculator.calculateAccuracy(emptySet()))
    }

    @Test
    fun testAccuracyIncrementalSteps() {
        val steps = mutableSetOf<PatternStep>()

        // 35 baseline + 20 (PICTURE_MODE_BASE) = 55
        steps.add(PatternStep.PICTURE_MODE_BASE)
        assertEquals(55, CinemaAccuracyCalculator.calculateAccuracy(steps))

        // 55 + 10 (ASPECT_RATIO) = 65
        steps.add(PatternStep.ASPECT_RATIO)
        assertEquals(65, CinemaAccuracyCalculator.calculateAccuracy(steps))

        // 65 + 15 (BLACK_LEVEL_PLUGE) = 80
        steps.add(PatternStep.BLACK_LEVEL_PLUGE)
        assertEquals(80, CinemaAccuracyCalculator.calculateAccuracy(steps))

        // 80 + 10 (WHITE_LEVEL_CONTRAST) = 90
        steps.add(PatternStep.WHITE_LEVEL_CONTRAST)
        assertEquals(90, CinemaAccuracyCalculator.calculateAccuracy(steps))

        // 90 + 10 (SHARPNESS_GRID) = 100 (capped at 100)
        steps.add(PatternStep.SHARPNESS_GRID)
        assertEquals(100, CinemaAccuracyCalculator.calculateAccuracy(steps))

        // Add remaining steps, must still cap at 100
        steps.add(PatternStep.GRAYSCALE_D65)
        steps.add(PatternStep.MOTION_24P_JUDDER)
        assertEquals(100, CinemaAccuracyCalculator.calculateAccuracy(steps))
    }

    @Test
    fun testAccuracyAllStepsCappedAt100() {
        val allSteps = PatternStep.values().toSet()
        // 35 + 90 = 125 -> capped at 100
        val accuracy = CinemaAccuracyCalculator.calculateAccuracy(allSteps)
        assertEquals(100, accuracy)
    }

    @Test
    fun testAccuracyLevelClassification() {
        // < 50: FACTORY_DEFAULT
        assertEquals(CinemaAccuracyLevel.FACTORY_DEFAULT, CinemaAccuracyCalculator.getLevel(0))
        assertEquals(CinemaAccuracyLevel.FACTORY_DEFAULT, CinemaAccuracyCalculator.getLevel(35))
        assertEquals(CinemaAccuracyLevel.FACTORY_DEFAULT, CinemaAccuracyCalculator.getLevel(49))

        // 50..74: IMPROVED
        assertEquals(CinemaAccuracyLevel.IMPROVED, CinemaAccuracyCalculator.getLevel(50))
        assertEquals(CinemaAccuracyLevel.IMPROVED, CinemaAccuracyCalculator.getLevel(65))
        assertEquals(CinemaAccuracyLevel.IMPROVED, CinemaAccuracyCalculator.getLevel(74))

        // 75..89: HIGH_FIDELITY
        assertEquals(CinemaAccuracyLevel.HIGH_FIDELITY, CinemaAccuracyCalculator.getLevel(75))
        assertEquals(CinemaAccuracyLevel.HIGH_FIDELITY, CinemaAccuracyCalculator.getLevel(80))
        assertEquals(CinemaAccuracyLevel.HIGH_FIDELITY, CinemaAccuracyCalculator.getLevel(89))

        // >= 90: CINEMA_REFERENCE
        assertEquals(CinemaAccuracyLevel.CINEMA_REFERENCE, CinemaAccuracyCalculator.getLevel(90))
        assertEquals(CinemaAccuracyLevel.CINEMA_REFERENCE, CinemaAccuracyCalculator.getLevel(95))
        assertEquals(CinemaAccuracyLevel.CINEMA_REFERENCE, CinemaAccuracyCalculator.getLevel(100))
    }
}
