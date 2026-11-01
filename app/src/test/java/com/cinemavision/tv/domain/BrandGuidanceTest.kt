package com.cinemavision.tv.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandGuidanceTest {

    @Test
    fun testAllBrandsHaveSpecificTips() {
        val brands = TvBrand.values()
        assertEquals(6, brands.size)

        for (brand in brands) {
            val tip = BrandTipProvider.getTips(brand)
            assertNotNull(tip)
            assertTrue(tip.pictureMode.isNotBlank())
            assertTrue(tip.colorTemp.isNotBlank())
            assertTrue(tip.sharpness.isNotBlank())
            assertTrue(tip.motionSmoothing.isNotBlank())
            assertTrue(tip.aspectSetting.isNotBlank())
        }
    }

    @Test
    fun testSonyBrandTips() {
        val tip = BrandTipProvider.getTips(TvBrand.SONY)
        assertEquals("Custom / Pro", tip.pictureMode)
        assertEquals("Expert 1", tip.colorTemp)
        assertEquals("50 (Neutral) / 0", tip.sharpness)
        assertEquals("Motionflow: Off / True Cinema", tip.motionSmoothing)
        assertEquals("Wide mode: Full / +1 Screen Area", tip.aspectSetting)
    }

    @Test
    fun testTclBrandTips() {
        val tip = BrandTipProvider.getTips(TvBrand.TCL)
        assertEquals("Movie", tip.pictureMode)
        assertEquals("Warm -5", tip.colorTemp)
        assertEquals("0", tip.sharpness)
        assertEquals("Action Clarity / LED Motion: Off", tip.motionSmoothing)
        assertEquals("Auto Format: Off, Just Scan: On", tip.aspectSetting)
    }

    @Test
    fun testHisenseBrandTips() {
        val tip = BrandTipProvider.getTips(TvBrand.HISENSE)
        assertEquals("Filmmaker Mode / Theater Day", tip.pictureMode)
        assertEquals("Warm", tip.colorTemp)
        assertEquals("0", tip.sharpness)
        assertEquals("Ultra Smooth Motion: Off", tip.motionSmoothing)
        assertEquals("Direct / 1:1", tip.aspectSetting)
    }

    @Test
    fun testSamsungBoxBrandTips() {
        val tip = BrandTipProvider.getTips(TvBrand.SAMSUNG_BOX)
        assertEquals("Movie / Filmmaker Mode", tip.pictureMode)
        assertEquals("Warm 2", tip.colorTemp)
        assertEquals("0", tip.sharpness)
        assertEquals("Auto Motion Plus: Off", tip.motionSmoothing)
        assertEquals("16:9 Standard / Fit to Screen: On", tip.aspectSetting)
    }

    @Test
    fun testLgBoxBrandTips() {
        val tip = BrandTipProvider.getTips(TvBrand.LG_BOX)
        assertEquals("Cinema / Filmmaker Mode", tip.pictureMode)
        assertEquals("Warm 50", tip.colorTemp)
        assertEquals("0", tip.sharpness)
        assertEquals("TruMotion: Off", tip.motionSmoothing)
        assertEquals("Original / Just Scan: On", tip.aspectSetting)
    }

    @Test
    fun testUniversalBrandTips() {
        val tip = BrandTipProvider.getTips(TvBrand.UNIVERSAL)
        assertEquals("Movie / Cinema", tip.pictureMode)
        assertEquals("Warm / Warm 2", tip.colorTemp)
        assertEquals("0", tip.sharpness)
        assertEquals("Motion Smoothing: Off", tip.motionSmoothing)
        assertEquals("Just Scan / 1:1 / Full", tip.aspectSetting)
    }

    @Test
    fun testTipForStepMapping() {
        for (brand in TvBrand.values()) {
            val tip = BrandTipProvider.getTips(brand)
            assertEquals(tip.pictureMode, BrandTipProvider.getTipForStep(brand, PatternStep.PICTURE_MODE_BASE))
            assertEquals(tip.aspectSetting, BrandTipProvider.getTipForStep(brand, PatternStep.ASPECT_RATIO))
            assertEquals(tip.sharpness, BrandTipProvider.getTipForStep(brand, PatternStep.SHARPNESS_GRID))
            assertEquals(tip.colorTemp, BrandTipProvider.getTipForStep(brand, PatternStep.GRAYSCALE_D65))
            assertEquals(tip.motionSmoothing, BrandTipProvider.getTipForStep(brand, PatternStep.MOTION_24P_JUDDER))

            // Black level & White level should have meaningful descriptive guidance
            assertTrue(BrandTipProvider.getTipForStep(brand, PatternStep.BLACK_LEVEL_PLUGE).isNotEmpty())
            assertTrue(BrandTipProvider.getTipForStep(brand, PatternStep.WHITE_LEVEL_CONTRAST).isNotEmpty())
        }
    }
}
