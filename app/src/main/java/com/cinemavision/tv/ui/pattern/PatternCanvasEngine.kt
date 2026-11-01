package com.cinemavision.tv.ui.pattern

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.cinemavision.tv.domain.ColorRange
import com.cinemavision.tv.domain.PatternStep

/**
 * High-precision canvas drawing engine for bit-accurate calibration patterns.
 * Anti-aliasing is explicitly disabled on line meshes to guarantee exact 1-pixel rendering
 * on Native 4K (3840x2160) and 1080p displays.
 */
class PatternCanvasEngine {

    private val fillPaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 32f
    }

    private val sharpnessWhitePaint = Paint().apply {
        isAntiAlias = false
        color = Color.WHITE
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val sharpnessBlackPaint = Paint().apply {
        isAntiAlias = false
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val arrowPaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 2f
    }

    private val gradientPaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL
    }

    private val whiteClipBoxRect = RectF()
    private val d65RefBoxRect = RectF()

    private var cachedGradientWidth = -1
    private var cachedGradientRange: ColorRange? = null

    companion object {
        private val LIMITED_PLUGE_BARS = listOf(12, 14, 16, 17, 18, 19, 20, 24, 28)
        private val FULL_PLUGE_BARS = listOf(0, 1, 2, 3, 4, 8, 12, 16)
        private val LIMITED_WHITE_BARS = listOf(230, 232, 234, 238, 242)
        private val FULL_WHITE_BARS = listOf(250, 252, 254, 255)
    }

    /**
     * Dispatches rendering of the appropriate test pattern for the given [step].
     */
    fun drawPattern(
        canvas: Canvas,
        width: Int,
        height: Int,
        step: PatternStep,
        range: ColorRange,
        flashState: Boolean
    ) {
        when (step) {
            PatternStep.PICTURE_MODE_BASE -> drawOverscan(canvas, width, height)
            PatternStep.ASPECT_RATIO -> drawOverscan(canvas, width, height)
            PatternStep.BLACK_LEVEL_PLUGE -> drawPluge(canvas, width, height, range, flashState)
            PatternStep.WHITE_LEVEL_CONTRAST -> drawWhiteClipping(canvas, width, height, range, flashState)
            PatternStep.SHARPNESS_GRID -> drawSharpnessGrid(canvas, width, height)
            PatternStep.GRAYSCALE_D65 -> drawGrayscaleRamp(canvas, width, height, range)
            PatternStep.MOTION_24P_JUDDER -> {
                // Fallback static background when 24p video player is not active
                canvas.drawColor(Color.BLACK)
            }
        }
    }

    /**
     * Renders 0% outer 1-pixel white border, 2.5% and 5% inner guide rectangles with arrows,
     * and a center crosshair.
     */
    fun drawOverscan(canvas: Canvas, width: Int, height: Int) {
        // Deep background
        canvas.drawColor(Color.BLACK)

        strokePaint.strokeWidth = 1f

        // 1. Outer 1-pixel white border (0% overscan)
        strokePaint.color = Color.WHITE
        canvas.drawRect(0f, 0f, (width - 1).toFloat(), (height - 1).toFloat(), strokePaint)

        // 2. 2.5% inner guide rectangle
        val bounds25 = PatternGeometry.calculateOverscanBounds(width, height, 2.5f)
        strokePaint.color = Color.LTGRAY
        canvas.drawRect(bounds25.left, bounds25.top, bounds25.right, bounds25.bottom, strokePaint)

        // 3. 5% inner guide rectangle
        val bounds50 = PatternGeometry.calculateOverscanBounds(width, height, 5.0f)
        strokePaint.color = Color.CYAN
        canvas.drawRect(bounds50.left, bounds50.top, bounds50.right, bounds50.bottom, strokePaint)

        // 4. Center crosshair
        val cx = width / 2f
        val cy = height / 2f
        strokePaint.color = Color.WHITE
        canvas.drawLine(0f, cy, width.toFloat(), cy, strokePaint)
        canvas.drawLine(cx, 0f, cx, height.toFloat(), strokePaint)

        // Center concentric circle
        strokePaint.color = Color.WHITE
        canvas.drawCircle(cx, cy, height * 0.15f, strokePaint)

        // 5. Directional guide arrows at top, bottom, left, right edges pointing to 0% border
        drawArrow(canvas, cx, bounds50.top, cx, 0f, Color.CYAN)
        drawArrow(canvas, cx, bounds50.bottom, cx, height.toFloat(), Color.CYAN)
        drawArrow(canvas, bounds50.left, cy, 0f, cy, Color.CYAN)
        drawArrow(canvas, bounds50.right, cy, width.toFloat(), cy, Color.CYAN)

        // Labels
        textPaint.color = Color.WHITE
        textPaint.textSize = 28f
        canvas.drawText("0% OVERSCAN (Outer White Border)", 40f, 60f, textPaint)
        textPaint.color = Color.LTGRAY
        canvas.drawText("2.5% Safe Frame", bounds25.left + 20f, bounds25.top + 40f, textPaint)
        textPaint.color = Color.CYAN
        canvas.drawText("5.0% Action Safe", bounds50.left + 20f, bounds50.top + 40f, textPaint)
    }

    /**
     * Renders PLUGE pattern with background at [ColorRange.blackLevel],
     * negative black bars (12, 14), and flashing near-black bars (17, 18, 19, 20, 24, 28).
     * If [flashState] is true, bars 17..28 are drawn with their luminance;
     * if false, they dim/blink to black level.
     */
    fun drawPluge(
        canvas: Canvas,
        width: Int,
        height: Int,
        range: ColorRange,
        flashState: Boolean
    ) {
        val bgLuminance = range.blackLevel
        canvas.drawColor(Color.rgb(bgLuminance, bgLuminance, bgLuminance))

        val barValues = if (range == ColorRange.LIMITED) {
            LIMITED_PLUGE_BARS
        } else {
            FULL_PLUGE_BARS
        }

        val rects = PatternGeometry.calculatePlugeBarRects(width, height, barValues.size)

        for (i in barValues.indices) {
            val value = barValues[i]
            val rect = rects[i]

            val effectiveLum = if (value <= range.blackLevel) {
                // Negative black or reference black: static
                value
            } else {
                // Near-black flashing bars
                if (flashState) value else range.blackLevel
            }

            fillPaint.color = Color.rgb(effectiveLum, effectiveLum, effectiveLum)
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, fillPaint)

            // Label for each bar
            textPaint.color = if (value <= range.blackLevel) Color.DKGRAY else Color.LTGRAY
            textPaint.textSize = 24f
            canvas.drawText("$value", rect.left + (rect.width / 4f), rect.top - 20f, textPaint)
        }

        // Instruction header
        textPaint.color = Color.WHITE
        textPaint.textSize = 32f
        canvas.drawText(
            "PLUGE Black Level: Bars 12 & 14 should blend. Bar 17+ should flash.",
            width * 0.15f,
            height * 0.12f,
            textPaint
        )
    }

    /**
     * Renders White Clipping / Contrast pattern with 85% luminance background,
     * reference white 235 box, and flashing highlight bars (230, 232, 234, 238, 242).
     */
    fun drawWhiteClipping(
        canvas: Canvas,
        width: Int,
        height: Int,
        range: ColorRange,
        flashState: Boolean
    ) {
        // 85% background luminance
        val bgLum = (255 * 0.85f).toInt()
        canvas.drawColor(Color.rgb(bgLum, bgLum, bgLum))

        // Center reference white 235 box
        whiteClipBoxRect.set(
            width * 0.12f,
            height * 0.18f,
            width * 0.88f,
            height * 0.82f
        )
        val refWhite = range.referenceWhite
        fillPaint.color = Color.rgb(refWhite, refWhite, refWhite)
        canvas.drawRect(whiteClipBoxRect, fillPaint)

        // Highlight bars
        val highlightBars = if (range == ColorRange.LIMITED) {
            LIMITED_WHITE_BARS
        } else {
            FULL_WHITE_BARS
        }

        val rects = PatternGeometry.calculatePlugeBarRects(width, height, highlightBars.size)

        for (i in highlightBars.indices) {
            val value = highlightBars[i]
            val rect = rects[i]

            // If flashState is true, render at bar luminance; if false, blend with reference white
            val effectiveLum = if (flashState) value else refWhite
            fillPaint.color = Color.rgb(effectiveLum, effectiveLum, effectiveLum)
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, fillPaint)

            // Bar numeric label
            textPaint.color = Color.BLACK
            textPaint.textSize = 24f
            canvas.drawText("$value", rect.left + (rect.width / 4f), rect.top - 20f, textPaint)
        }

        // Header label
        textPaint.color = Color.BLACK
        textPaint.textSize = 32f
        canvas.drawText(
            "White Clipping: Bars 230-234 should blink against Reference White ($refWhite)",
            width * 0.15f,
            height * 0.12f,
            textPaint
        )
    }

    /**
     * Renders Sharpness Grid with 50% neutral gray background, alternating 1-pixel
     * black/white horizontal & vertical lines, anti-ringing concentric circles, and diagonal steps.
     */
    fun drawSharpnessGrid(canvas: Canvas, width: Int, height: Int) {
        // 50% neutral gray
        canvas.drawColor(Color.rgb(128, 128, 128))

        // 1. Alternating 1-pixel vertical line mesh in left quadrant
        val vLeft = width * 0.08f
        val vRight = width * 0.35f
        val vTop = height * 0.25f
        val vBottom = height * 0.75f
        var x = vLeft
        while (x < vRight) {
            canvas.drawLine(x, vTop, x, vBottom, sharpnessWhitePaint)
            if (x + 1f < vRight) {
                canvas.drawLine(x + 1f, vTop, x + 1f, vBottom, sharpnessBlackPaint)
            }
            x += 2f
        }

        // 2. Alternating 1-pixel horizontal line mesh in right quadrant
        val hLeft = width * 0.65f
        val hRight = width * 0.92f
        val hTop = height * 0.25f
        val hBottom = height * 0.75f
        var y = hTop
        while (y < hBottom) {
            canvas.drawLine(hLeft, y, hRight, y, sharpnessWhitePaint)
            if (y + 1f < hBottom) {
                canvas.drawLine(hLeft, y + 1f, hRight, y + 1f, sharpnessBlackPaint)
            }
            y += 2f
        }

        // 3. Anti-ringing concentric circles in the center
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = height * 0.22f
        var r = 16f
        var ringIndex = 0
        while (r < maxRadius) {
            val ringPaint = if (ringIndex % 2 == 0) sharpnessWhitePaint else sharpnessBlackPaint
            canvas.drawCircle(cx, cy, r, ringPaint)
            r += 8f
            ringIndex++
        }

        // 4. Diagonal staircase steps (top center and bottom center)
        val diagStartX = cx - 120f
        val diagStartY = height * 0.08f
        for (i in 0 until 120 step 4) {
            val px = diagStartX + i
            val py = diagStartY + i
            canvas.drawLine(px, py, px + 4f, py, sharpnessWhitePaint)
            canvas.drawLine(px + 4f, py, px + 4f, py + 4f, sharpnessBlackPaint)
        }

        // Text
        textPaint.color = Color.WHITE
        textPaint.textSize = 28f
        canvas.drawText("Sharpness Grid: Halos around lines or circles indicate excessive sharpness", width * 0.15f, height * 0.06f, textPaint)
    }

    /**
     * Renders 20-step discrete grayscale patches on top half, continuous smooth
     * gradient on bottom half, and center D65 reference white box.
     */
    fun drawGrayscaleRamp(canvas: Canvas, width: Int, height: Int, range: ColorRange) {
        // Deep background
        canvas.drawColor(Color.BLACK)

        // 1. Top half: 20-step discrete grayscale patches
        val patchRects = PatternGeometry.calculateGrayscalePatchRects(width, height, 20)
        val levels = range.getGrayscaleLevels(20)

        for (i in patchRects.indices) {
            val rect = patchRects[i]
            val lum = levels[i]
            fillPaint.color = Color.rgb(lum, lum, lum)
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, fillPaint)

            // Step number label
            textPaint.color = if (lum > 128) Color.BLACK else Color.WHITE
            textPaint.textSize = 22f
            canvas.drawText("${i * 5}%", rect.left + 10f, rect.bottom - 20f, textPaint)
        }

        // 2. Bottom half: Continuous smooth linear gradient from black level to reference white
        val startColor = Color.rgb(range.blackLevel, range.blackLevel, range.blackLevel)
        val endColor = Color.rgb(range.referenceWhite, range.referenceWhite, range.referenceWhite)

        if (cachedGradientWidth != width || cachedGradientRange != range) {
            val gradientShader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                0f,
                startColor,
                endColor,
                Shader.TileMode.CLAMP
            )
            gradientPaint.shader = gradientShader
            cachedGradientWidth = width
            cachedGradientRange = range
        }
        canvas.drawRect(0f, height / 2f, width.toFloat(), height.toFloat(), gradientPaint)

        // 3. Center D65 reference white box
        val cx = width / 2f
        val cy = height / 2f
        val boxWidth = width * 0.16f
        val boxHeight = height * 0.14f
        d65RefBoxRect.set(
            cx - boxWidth / 2f,
            cy - boxHeight / 2f,
            cx + boxWidth / 2f,
            cy + boxHeight / 2f
        )

        val refWhite = range.referenceWhite
        fillPaint.color = Color.rgb(refWhite, refWhite, refWhite)
        canvas.drawRect(d65RefBoxRect, fillPaint)

        // 1-pixel border around reference white box
        strokePaint.color = Color.BLACK
        strokePaint.strokeWidth = 2f
        canvas.drawRect(d65RefBoxRect, strokePaint)

        // D65 reference text
        textPaint.color = Color.BLACK
        textPaint.textSize = 24f
        canvas.drawText("D65 Reference White", d65RefBoxRect.left + 15f, cy, textPaint)
        canvas.drawText("100% Rec.709 ($refWhite)", d65RefBoxRect.left + 25f, cy + 30f, textPaint)
    }

    private fun drawArrow(
        canvas: Canvas,
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        arrowColor: Int
    ) {
        arrowPaint.color = arrowColor
        canvas.drawLine(fromX, fromY, toX, toY, arrowPaint)
    }
}
