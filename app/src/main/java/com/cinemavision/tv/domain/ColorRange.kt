package com.cinemavision.tv.domain

import kotlin.math.roundToInt

enum class ColorRange(val blackLevel: Int, val referenceWhite: Int) {
    LIMITED(16, 235),
    FULL(0, 255);

    fun getPlugeBars(): List<Int> {
        return when (this) {
            LIMITED -> listOf(12, 14, 16, 17, 18, 19, 20, 24)
            FULL -> listOf(0, 1, 2, 3, 4, 8)
        }
    }

    fun getWhiteClipBars(): List<Int> {
        return when (this) {
            LIMITED -> listOf(230, 232, 234, 235, 238, 242)
            FULL -> listOf(250, 252, 254, 255)
        }
    }

    fun getGrayscaleLevels(steps: Int = 20): List<Int> {
        require(steps > 0) { "Steps must be greater than 0" }
        val range = referenceWhite - blackLevel
        return (0..steps).map { i ->
            blackLevel + (range.toDouble() * i / steps).roundToInt()
        }
    }
}
