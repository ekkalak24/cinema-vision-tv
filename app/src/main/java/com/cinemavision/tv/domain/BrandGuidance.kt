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
            pictureMode = "โหมดภาพ: Custom / Pro",
            colorTemp = "อุณหภูมิสี: Expert 1",
            sharpness = "ความคมชัด: 50 (ค่ากลาง) หรือ 0",
            motionSmoothing = "Motionflow: Off / True Cinema",
            aspectSetting = "โหมดกว้าง: เต็มจอ / +1 พื้นที่หน้าจอ"
        ),
        TvBrand.TCL to BrandSettingTip(
            pictureMode = "โหมดภาพ: Movie",
            colorTemp = "อุณหภูมิสี: Warm -5",
            sharpness = "ความคมชัด: 0",
            motionSmoothing = "Action Clarity / LED Motion: Off",
            aspectSetting = "จัดรูปแบบอัตโนมัติ: ปิด, แค่สแกน (Just Scan): เปิด"
        ),
        TvBrand.HISENSE to BrandSettingTip(
            pictureMode = "โหมดภาพ: Filmmaker Mode / Theater Day",
            colorTemp = "อุณหภูมิสี: Warm",
            sharpness = "ความคมชัด: 0",
            motionSmoothing = "Ultra Smooth Motion: Off",
            aspectSetting = "สัดส่วนภาพ: ตรงจุด (Direct) / 1:1"
        ),
        TvBrand.SAMSUNG_BOX to BrandSettingTip(
            pictureMode = "โหมดภาพ: Movie / Filmmaker Mode",
            colorTemp = "อุณหภูมิสี: Warm 2",
            sharpness = "ความคมชัด: 0",
            motionSmoothing = "Auto Motion Plus: Off",
            aspectSetting = "ขนาดภาพ: 16:9 มาตรฐาน / พอดีจอ (Fit to Screen): เปิด"
        ),
        TvBrand.LG_BOX to BrandSettingTip(
            pictureMode = "โหมดภาพ: Cinema / Filmmaker Mode",
            colorTemp = "อุณหภูมิสี: Warm 50",
            sharpness = "ความคมชัด: 0",
            motionSmoothing = "TruMotion: Off",
            aspectSetting = "อัตราส่วน: ดั้งเดิม / สแกนพอดีจอ (Just Scan): เปิด"
        ),
        TvBrand.UNIVERSAL to BrandSettingTip(
            pictureMode = "โหมดภาพ: Movie / Cinema / โรงภาพยนตร์",
            colorTemp = "อุณหภูมิสี: Warm / Warm 2 (โทนอุ่น)",
            sharpness = "ความคมชัด: 0 (ปิดเส้นขอบขาวหลอกตา)",
            motionSmoothing = "Motion Smoothing: Off (ปิดระบบแทรกเฟรม)",
            aspectSetting = "สัดส่วนภาพ: Just Scan / 1:1 / เต็มจอ (ปิด Overscan)"
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
            PatternStep.BLACK_LEVEL_PLUGE -> "ปรับ Brightness (ระดับสีดำ): แถบ 16 ต้องกลืนกับพื้นหลัง และแถบ 17 เป็นต้นไปกะพริบจางๆ พอมองเห็น"
            PatternStep.WHITE_LEVEL_CONTRAST -> "ปรับ Contrast (ระดับสีขาว): แถบ 234 ต้องกะพริบมองเห็นได้ โดยไม่เพี้ยนหรือขาวล้น"
            PatternStep.SHARPNESS_GRID -> tip.sharpness
            PatternStep.GRAYSCALE_D65 -> tip.colorTemp
            PatternStep.MOTION_24P_JUDDER -> tip.motionSmoothing
        }
    }
}
