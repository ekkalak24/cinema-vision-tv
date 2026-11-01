package com.cinemavision.tv.domain

enum class TvBrand {
    SONY,
    TCL,
    HISENSE,
    SAMSUNG_BOX,
    LG_BOX,
    UNIVERSAL
}

data class BrandSettingTip(
    val pictureMode: String,
    val colorTemp: String,
    val sharpness: String,
    val motionSmoothing: String,
    val aspectSetting: String
)

object BrandTipProvider {
    private val tips: Map<TvBrand, BrandSettingTip> = mapOf(
        TvBrand.SONY to BrandSettingTip(
            pictureMode = "Custom / Pro",
            colorTemp = "Expert 1",
            sharpness = "50 (Neutral) / 0",
            motionSmoothing = "Motionflow: Off / True Cinema",
            aspectSetting = "Wide mode: Full / +1 Screen Area"
        ),
        TvBrand.TCL to BrandSettingTip(
            pictureMode = "Movie",
            colorTemp = "Warm -5",
            sharpness = "0",
            motionSmoothing = "Action Clarity / LED Motion: Off",
            aspectSetting = "Auto Format: Off, Just Scan: On"
        ),
        TvBrand.HISENSE to BrandSettingTip(
            pictureMode = "Filmmaker Mode / Theater Day",
            colorTemp = "Warm",
            sharpness = "0",
            motionSmoothing = "Ultra Smooth Motion: Off",
            aspectSetting = "Direct / 1:1"
        ),
        TvBrand.SAMSUNG_BOX to BrandSettingTip(
            pictureMode = "Movie / Filmmaker Mode",
            colorTemp = "Warm 2",
            sharpness = "0",
            motionSmoothing = "Auto Motion Plus: Off",
            aspectSetting = "16:9 Standard / Fit to Screen: On"
        ),
        TvBrand.LG_BOX to BrandSettingTip(
            pictureMode = "Cinema / Filmmaker Mode",
            colorTemp = "Warm 50",
            sharpness = "0",
            motionSmoothing = "TruMotion: Off",
            aspectSetting = "Original / Just Scan: On"
        ),
        TvBrand.UNIVERSAL to BrandSettingTip(
            pictureMode = "Movie / Cinema",
            colorTemp = "Warm / Warm 2",
            sharpness = "0",
            motionSmoothing = "Motion Smoothing: Off",
            aspectSetting = "Just Scan / 1:1 / Full"
        )
    )

    fun getTips(brand: TvBrand): BrandSettingTip {
        return tips[brand] ?: tips.getValue(TvBrand.UNIVERSAL)
    }

    fun getTipForStep(brand: TvBrand, step: PatternStep): String {
        val tip = getTips(brand)
        return when (step) {
            PatternStep.PICTURE_MODE_BASE -> tip.pictureMode
            PatternStep.ASPECT_RATIO -> tip.aspectSetting
            PatternStep.BLACK_LEVEL_PLUGE -> "Adjust Brightness: ensure bar 16 blends into background while bar 17+ remains visible"
            PatternStep.WHITE_LEVEL_CONTRAST -> "Adjust Contrast: preserve highlights up to bar 234 without clipping or color tint"
            PatternStep.SHARPNESS_GRID -> tip.sharpness
            PatternStep.GRAYSCALE_D65 -> tip.colorTemp
            PatternStep.MOTION_24P_JUDDER -> tip.motionSmoothing
        }
    }
}
