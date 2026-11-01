package com.cinemavision.tv.ui.pattern

data class RectFData(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

object PatternGeometry {

    /**
     * Calculates the inner overscan bounding box for a given [width], [height],
     * and [marginPercent] (e.g. 0f, 2.5f, 5.0f, or equivalent fractions 0.025f, 0.05f).
     */
    fun calculateOverscanBounds(width: Int, height: Int, marginPercent: Float): RectFData {
        if (width <= 0 || height <= 0) return RectFData(0f, 0f, 0f, 0f)
        val fraction = if (marginPercent > 1.0f) marginPercent / 100f else if (marginPercent < 0f) 0f else marginPercent
        val marginX = width * fraction
        val marginY = height * fraction
        return RectFData(
            left = marginX,
            top = marginY,
            right = width - marginX,
            bottom = height - marginY
        )
    }

    /**
     * Calculates uniformly spaced, vertically centered rects for [barCount] PLUGE bars.
     * The bars are horizontally centered and symmetric.
     */
    fun calculatePlugeBarRects(width: Int, height: Int, barCount: Int): List<RectFData> {
        if (barCount <= 0 || width <= 0 || height <= 0) return emptyList()

        val top = height * 0.2f
        val bottom = height * 0.8f

        // Span 70% of screen width centered
        val totalAreaWidth = width * 0.7f
        val startX = (width - totalAreaWidth) / 2f

        val slotWidth = totalAreaWidth / barCount
        val barWidth = slotWidth * 0.75f
        val gap = slotWidth * 0.25f
        val offsetInSlot = gap / 2f

        return (0 until barCount).map { i ->
            val left = startX + i * slotWidth + offsetInSlot
            val right = left + barWidth
            RectFData(left, top, right, bottom)
        }
    }

    /**
     * Calculates [steps] discrete grayscale patches spanning the full canvas width
     * across the top half of the screen ([top] = 0, [bottom] = height / 2).
     */
    fun calculateGrayscalePatchRects(width: Int, height: Int, steps: Int = 20): List<RectFData> {
        if (steps <= 0 || width <= 0 || height <= 0) return emptyList()

        val top = 0f
        val bottom = height / 2f
        val stepWidth = width.toFloat() / steps

        return (0 until steps).map { i ->
            val left = i * stepWidth
            val right = if (i == steps - 1) width.toFloat() else (i + 1) * stepWidth
            RectFData(left, top, right, bottom)
        }
    }
}
