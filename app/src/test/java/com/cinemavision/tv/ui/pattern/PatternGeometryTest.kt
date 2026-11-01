package com.cinemavision.tv.ui.pattern

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternGeometryTest {

    companion object {
        private const val DELTA = 0.001f
        private const val WIDTH_4K = 3840
        private const val HEIGHT_4K = 2160
        private const val WIDTH_1080P = 1920
        private const val HEIGHT_1080P = 1080
    }

    // --- Overscan Calculations: 4K ---

    @Test
    fun testOverscan4KZeroPercent() {
        val bounds = PatternGeometry.calculateOverscanBounds(WIDTH_4K, HEIGHT_4K, 0f)
        assertEquals(0f, bounds.left, DELTA)
        assertEquals(0f, bounds.top, DELTA)
        assertEquals(3840f, bounds.right, DELTA)
        assertEquals(2160f, bounds.bottom, DELTA)
        assertEquals(3840f, bounds.width, DELTA)
        assertEquals(2160f, bounds.height, DELTA)
    }

    @Test
    fun testOverscan4KTwoPointFivePercent() {
        val bounds = PatternGeometry.calculateOverscanBounds(WIDTH_4K, HEIGHT_4K, 2.5f)
        // 3840 * 0.025 = 96; 2160 * 0.025 = 54
        assertEquals(96f, bounds.left, DELTA)
        assertEquals(54f, bounds.top, DELTA)
        assertEquals(3744f, bounds.right, DELTA)
        assertEquals(2106f, bounds.bottom, DELTA)
        assertEquals(3648f, bounds.width, DELTA)
        assertEquals(2052f, bounds.height, DELTA)
    }

    @Test
    fun testOverscan4KFivePercent() {
        val bounds = PatternGeometry.calculateOverscanBounds(WIDTH_4K, HEIGHT_4K, 5.0f)
        // 3840 * 0.05 = 192; 2160 * 0.05 = 108
        assertEquals(192f, bounds.left, DELTA)
        assertEquals(108f, bounds.top, DELTA)
        assertEquals(3648f, bounds.right, DELTA)
        assertEquals(2052f, bounds.bottom, DELTA)
        assertEquals(3456f, bounds.width, DELTA)
        assertEquals(1944f, bounds.height, DELTA)
    }

    @Test
    fun testOverscanMarginPercentFractionEquivalence() {
        val boundsFromPercent = PatternGeometry.calculateOverscanBounds(WIDTH_4K, HEIGHT_4K, 5.0f)
        val boundsFromFraction = PatternGeometry.calculateOverscanBounds(WIDTH_4K, HEIGHT_4K, 0.05f)
        assertEquals(boundsFromPercent.left, boundsFromFraction.left, DELTA)
        assertEquals(boundsFromPercent.top, boundsFromFraction.top, DELTA)
        assertEquals(boundsFromPercent.right, boundsFromFraction.right, DELTA)
        assertEquals(boundsFromPercent.bottom, boundsFromFraction.bottom, DELTA)
    }

    // --- Overscan Calculations: 1080p ---

    @Test
    fun testOverscan1080pZeroPercent() {
        val bounds = PatternGeometry.calculateOverscanBounds(WIDTH_1080P, HEIGHT_1080P, 0f)
        assertEquals(0f, bounds.left, DELTA)
        assertEquals(0f, bounds.top, DELTA)
        assertEquals(1920f, bounds.right, DELTA)
        assertEquals(1080f, bounds.bottom, DELTA)
    }

    @Test
    fun testOverscan1080pTwoPointFivePercent() {
        val bounds = PatternGeometry.calculateOverscanBounds(WIDTH_1080P, HEIGHT_1080P, 2.5f)
        // 1920 * 0.025 = 48; 1080 * 0.025 = 27
        assertEquals(48f, bounds.left, DELTA)
        assertEquals(27f, bounds.top, DELTA)
        assertEquals(1872f, bounds.right, DELTA)
        assertEquals(1053f, bounds.bottom, DELTA)
    }

    @Test
    fun testOverscan1080pFivePercent() {
        val bounds = PatternGeometry.calculateOverscanBounds(WIDTH_1080P, HEIGHT_1080P, 5.0f)
        // 1920 * 0.05 = 96; 1080 * 0.05 = 54
        assertEquals(96f, bounds.left, DELTA)
        assertEquals(54f, bounds.top, DELTA)
        assertEquals(1824f, bounds.right, DELTA)
        assertEquals(1026f, bounds.bottom, DELTA)
    }

    // --- PLUGE Bar Geometry & Spacing ---

    @Test
    fun testPlugeBarCountAndDimensions() {
        val barCount = 8
        val rects = PatternGeometry.calculatePlugeBarRects(WIDTH_4K, HEIGHT_4K, barCount)
        assertEquals(barCount, rects.size)

        val firstWidth = rects[0].width
        val firstHeight = rects[0].height
        assertTrue(firstWidth > 0f)
        assertTrue(firstHeight > 0f)

        for (rect in rects) {
            assertEquals(firstWidth, rect.width, DELTA)
            assertEquals(firstHeight, rect.height, DELTA)
            assertTrue(rect.left >= 0f)
            assertTrue(rect.right <= WIDTH_4K)
            assertTrue(rect.top >= 0f)
            assertTrue(rect.bottom <= HEIGHT_4K)
        }
    }

    @Test
    fun testPlugeBarSpacingAndSymmetry() {
        val barCount = 8
        val rects = PatternGeometry.calculatePlugeBarRects(WIDTH_4K, HEIGHT_4K, barCount)

        // Spacing between all adjacent bars should be uniform and positive
        val expectedSpacing = rects[1].left - rects[0].right
        assertTrue("Spacing must be positive", expectedSpacing > 0f)

        for (i in 0 until barCount - 1) {
            val spacing = rects[i + 1].left - rects[i].right
            assertEquals(expectedSpacing, spacing, DELTA)
        }

        // Symmetrical horizontal margins: left margin == right margin
        val leftMargin = rects.first().left
        val rightMargin = WIDTH_4K - rects.last().right
        assertEquals(leftMargin, rightMargin, DELTA)

        // Vertically centered: top margin == bottom margin
        val topMargin = rects.first().top
        val bottomMargin = HEIGHT_4K - rects.first().bottom
        assertEquals(topMargin, bottomMargin, DELTA)
    }

    @Test
    fun testPlugeBarEdgeCases() {
        assertTrue(PatternGeometry.calculatePlugeBarRects(WIDTH_4K, HEIGHT_4K, 0).isEmpty())
        assertTrue(PatternGeometry.calculatePlugeBarRects(WIDTH_4K, HEIGHT_4K, -1).isEmpty())
        assertTrue(PatternGeometry.calculatePlugeBarRects(0, HEIGHT_4K, 5).isEmpty())
    }

    // --- Grayscale Patch Geometry: 20-Step ---

    @Test
    fun testGrayscalePatchesSpanFullCanvasWidth4K() {
        val steps = 20
        val rects = PatternGeometry.calculateGrayscalePatchRects(WIDTH_4K, HEIGHT_4K, steps)
        assertEquals(steps, rects.size)

        // First patch starts exactly at 0
        assertEquals(0f, rects.first().left, DELTA)

        // Last patch ends exactly at width
        assertEquals(WIDTH_4K.toFloat(), rects.last().right, DELTA)

        // Contiguous: each patch starts where the previous ends
        for (i in 0 until steps - 1) {
            assertEquals(rects[i].right, rects[i + 1].left, DELTA)
        }

        // Each patch has identical width: 3840 / 20 = 192
        val expectedStepWidth = WIDTH_4K.toFloat() / steps
        assertEquals(192f, expectedStepWidth, DELTA)
        for (rect in rects) {
            assertEquals(expectedStepWidth, rect.width, DELTA)
        }

        // Patches sit in top half of canvas
        val halfHeight = HEIGHT_4K / 2f
        for (rect in rects) {
            assertEquals(0f, rect.top, DELTA)
            assertEquals(halfHeight, rect.bottom, DELTA)
            assertEquals(halfHeight, rect.height, DELTA)
        }
    }

    @Test
    fun testGrayscalePatchesSpanFullCanvasWidth1080p() {
        val steps = 20
        val rects = PatternGeometry.calculateGrayscalePatchRects(WIDTH_1080P, HEIGHT_1080P, steps)
        assertEquals(steps, rects.size)

        assertEquals(0f, rects.first().left, DELTA)
        assertEquals(WIDTH_1080P.toFloat(), rects.last().right, DELTA)

        val expectedStepWidth = WIDTH_1080P.toFloat() / steps
        assertEquals(96f, expectedStepWidth, DELTA)
        for (rect in rects) {
            assertEquals(expectedStepWidth, rect.width, DELTA)
            assertEquals(0f, rect.top, DELTA)
            assertEquals(540f, rect.bottom, DELTA)
        }
    }

    @Test
    fun testGrayscalePatchesEdgeCases() {
        assertTrue(PatternGeometry.calculateGrayscalePatchRects(WIDTH_4K, HEIGHT_4K, 0).isEmpty())
        assertTrue(PatternGeometry.calculateGrayscalePatchRects(WIDTH_4K, HEIGHT_4K, -5).isEmpty())
        assertTrue(PatternGeometry.calculateGrayscalePatchRects(0, HEIGHT_4K, 20).isEmpty())
    }
}
