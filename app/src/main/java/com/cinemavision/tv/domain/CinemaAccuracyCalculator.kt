package com.cinemavision.tv.domain

const val BASELINE_ACCURACY = 35

enum class CinemaAccuracyLevel {
    FACTORY_DEFAULT,
    IMPROVED,
    HIGH_FIDELITY,
    CINEMA_REFERENCE
}

object CinemaAccuracyCalculator {
    const val BASELINE = BASELINE_ACCURACY

    fun calculateAccuracy(calibratedSteps: Set<PatternStep>): Int {
        val gained = calibratedSteps.fold(0) { acc, step -> acc + step.weightPercent }
        return minOf(100, BASELINE_ACCURACY + gained)
    }

    fun getLevel(accuracyPercent: Int): CinemaAccuracyLevel {
        return when {
            accuracyPercent < 50 -> CinemaAccuracyLevel.FACTORY_DEFAULT
            accuracyPercent in 50..74 -> CinemaAccuracyLevel.IMPROVED
            accuracyPercent in 75..89 -> CinemaAccuracyLevel.HIGH_FIDELITY
            else -> CinemaAccuracyLevel.CINEMA_REFERENCE
        }
    }
}

fun calculateAccuracy(calibratedSteps: Set<PatternStep>): Int =
    CinemaAccuracyCalculator.calculateAccuracy(calibratedSteps)

fun getCinemaAccuracyLevel(accuracyPercent: Int): CinemaAccuracyLevel =
    CinemaAccuracyCalculator.getLevel(accuracyPercent)
