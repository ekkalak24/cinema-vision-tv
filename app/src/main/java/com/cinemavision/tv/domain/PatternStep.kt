package com.cinemavision.tv.domain

enum class PatternStep(val id: Int, val titleKey: String, val weightPercent: Int) {
    PICTURE_MODE_BASE(1, "step_picture_mode", 20),
    ASPECT_RATIO(2, "step_aspect_ratio", 10),
    BLACK_LEVEL_PLUGE(3, "step_black_level", 15),
    WHITE_LEVEL_CONTRAST(4, "step_white_level", 10),
    SHARPNESS_GRID(5, "step_sharpness", 10),
    GRAYSCALE_D65(6, "step_grayscale", 10),
    MOTION_24P_JUDDER(7, "step_motion_judder", 15);

    fun next(): PatternStep? {
        val all = values()
        val index = ordinal
        return if (index < all.size - 1) all[index + 1] else null
    }

    fun previous(): PatternStep? {
        val all = values()
        val index = ordinal
        return if (index > 0) all[index - 1] else null
    }
}
