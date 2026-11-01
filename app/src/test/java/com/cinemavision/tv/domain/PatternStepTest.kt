package com.cinemavision.tv.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PatternStepTest {

    @Test
    fun testPatternStepValuesAndProperties() {
        val steps = PatternStep.values()
        assertEquals(7, steps.size)

        assertEquals(1, PatternStep.PICTURE_MODE_BASE.id)
        assertEquals("step_picture_mode", PatternStep.PICTURE_MODE_BASE.titleKey)
        assertEquals(20, PatternStep.PICTURE_MODE_BASE.weightPercent)

        assertEquals(2, PatternStep.ASPECT_RATIO.id)
        assertEquals("step_aspect_ratio", PatternStep.ASPECT_RATIO.titleKey)
        assertEquals(10, PatternStep.ASPECT_RATIO.weightPercent)

        assertEquals(3, PatternStep.BLACK_LEVEL_PLUGE.id)
        assertEquals("step_black_level", PatternStep.BLACK_LEVEL_PLUGE.titleKey)
        assertEquals(15, PatternStep.BLACK_LEVEL_PLUGE.weightPercent)

        assertEquals(4, PatternStep.WHITE_LEVEL_CONTRAST.id)
        assertEquals("step_white_level", PatternStep.WHITE_LEVEL_CONTRAST.titleKey)
        assertEquals(10, PatternStep.WHITE_LEVEL_CONTRAST.weightPercent)

        assertEquals(5, PatternStep.SHARPNESS_GRID.id)
        assertEquals("step_sharpness", PatternStep.SHARPNESS_GRID.titleKey)
        assertEquals(10, PatternStep.SHARPNESS_GRID.weightPercent)

        assertEquals(6, PatternStep.GRAYSCALE_D65.id)
        assertEquals("step_grayscale", PatternStep.GRAYSCALE_D65.titleKey)
        assertEquals(10, PatternStep.GRAYSCALE_D65.weightPercent)

        assertEquals(7, PatternStep.MOTION_24P_JUDDER.id)
        assertEquals("step_motion_judder", PatternStep.MOTION_24P_JUDDER.titleKey)
        assertEquals(15, PatternStep.MOTION_24P_JUDDER.weightPercent)

        val totalWeight = steps.fold(0) { acc, step -> acc + step.weightPercent }
        assertEquals(90, totalWeight)
    }

    @Test
    fun testNextNavigation() {
        assertEquals(PatternStep.ASPECT_RATIO, PatternStep.PICTURE_MODE_BASE.next())
        assertEquals(PatternStep.BLACK_LEVEL_PLUGE, PatternStep.ASPECT_RATIO.next())
        assertEquals(PatternStep.WHITE_LEVEL_CONTRAST, PatternStep.BLACK_LEVEL_PLUGE.next())
        assertEquals(PatternStep.SHARPNESS_GRID, PatternStep.WHITE_LEVEL_CONTRAST.next())
        assertEquals(PatternStep.GRAYSCALE_D65, PatternStep.SHARPNESS_GRID.next())
        assertEquals(PatternStep.MOTION_24P_JUDDER, PatternStep.GRAYSCALE_D65.next())
        assertNull(PatternStep.MOTION_24P_JUDDER.next())
    }

    @Test
    fun testPreviousNavigation() {
        assertNull(PatternStep.PICTURE_MODE_BASE.previous())
        assertEquals(PatternStep.PICTURE_MODE_BASE, PatternStep.ASPECT_RATIO.previous())
        assertEquals(PatternStep.ASPECT_RATIO, PatternStep.BLACK_LEVEL_PLUGE.previous())
        assertEquals(PatternStep.BLACK_LEVEL_PLUGE, PatternStep.WHITE_LEVEL_CONTRAST.previous())
        assertEquals(PatternStep.WHITE_LEVEL_CONTRAST, PatternStep.SHARPNESS_GRID.previous())
        assertEquals(PatternStep.SHARPNESS_GRID, PatternStep.GRAYSCALE_D65.previous())
        assertEquals(PatternStep.GRAYSCALE_D65, PatternStep.MOTION_24P_JUDDER.previous())
    }
}
