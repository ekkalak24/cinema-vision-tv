package com.cinemavision.tv.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorRangeTest {

    @Test
    fun testLimitedRangeLevels() {
        val range = ColorRange.LIMITED
        assertEquals(16, range.blackLevel)
        assertEquals(235, range.referenceWhite)

        val pluge = range.getPlugeBars()
        assertEquals(listOf(12, 14, 16, 17, 18, 19, 20, 24), pluge)
        assertTrue(pluge.contains(16))
        assertTrue(pluge.contains(17))
        assertTrue(pluge.contains(18))
        assertTrue(pluge.contains(19))
        assertTrue(pluge.contains(20))

        val whiteClip = range.getWhiteClipBars()
        assertEquals(listOf(230, 232, 234, 235, 238, 242), whiteClip)
        assertTrue(whiteClip.contains(234))
        assertTrue(whiteClip.contains(235))
    }

    @Test
    fun testFullRangeLevels() {
        val range = ColorRange.FULL
        assertEquals(0, range.blackLevel)
        assertEquals(255, range.referenceWhite)

        val pluge = range.getPlugeBars()
        assertEquals(listOf(0, 1, 2, 3, 4, 8), pluge)
        assertTrue(pluge.contains(0))
        assertTrue(pluge.contains(1))
        assertTrue(pluge.contains(2))

        val whiteClip = range.getWhiteClipBars()
        assertEquals(listOf(250, 252, 254, 255), whiteClip)
        assertTrue(whiteClip.contains(254))
        assertTrue(whiteClip.contains(255))
    }

    @Test
    fun testGrayscaleLevelsLimited20Steps() {
        val levels = ColorRange.LIMITED.getGrayscaleLevels(20)
        assertEquals(21, levels.size)
        assertEquals(16, levels.first())
        assertEquals(235, levels.last())

        // Levels must be monotonically non-decreasing
        for (i in 0 until levels.size - 1) {
            assertTrue("Expected levels[$i] <= levels[${i + 1}]", levels[i] <= levels[i + 1])
        }

        // Midpoint check (50% luminance)
        val mid = levels[10]
        assertTrue("Midpoint $mid should be approximately 125-126", mid in 125..126)
    }

    @Test
    fun testGrayscaleLevelsFull20Steps() {
        val levels = ColorRange.FULL.getGrayscaleLevels(20)
        assertEquals(21, levels.size)
        assertEquals(0, levels.first())
        assertEquals(255, levels.last())

        for (i in 0 until levels.size - 1) {
            assertTrue("Expected levels[$i] <= levels[${i + 1}]", levels[i] <= levels[i + 1])
        }

        val mid = levels[10]
        assertTrue("Midpoint $mid should be approximately 127-128", mid in 127..128)
    }

    @Test
    fun testGrayscaleCustomSteps() {
        val levels = ColorRange.LIMITED.getGrayscaleLevels(10)
        assertEquals(11, levels.size)
        assertEquals(16, levels.first())
        assertEquals(235, levels.last())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGrayscaleZeroStepsThrows() {
        ColorRange.LIMITED.getGrayscaleLevels(0)
    }
}
