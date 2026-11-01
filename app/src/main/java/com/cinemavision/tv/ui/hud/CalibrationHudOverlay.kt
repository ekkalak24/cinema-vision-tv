package com.cinemavision.tv.ui.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemavision.tv.R
import com.cinemavision.tv.domain.ColorRange
import com.cinemavision.tv.domain.PatternStep

/**
 * Top-level Jetpack Compose for TV interactive Heads-Up Display (HUD) overlay.
 *
 * Floats over the background SurfaceView test pattern, auto-hiding after 6 seconds of
 * inactivity to allow unscaled, bit-accurate visual inspection.
 *
 * Provides:
 * - Step navigation pills ([1]..[7]) with active highlight and calibrated checkmark
 * - Live Cinema Match % meter with progress bar and tier badge
 * - Brand-specific TV setting instructions
 * - Detailed pattern adjustment guide
 * - Remote control D-pad hints
 */
@Composable
fun CalibrationHudOverlay(
    hudState: HudState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = hudState.isHudVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC121820)) // Dark slate HUD background #CC121820
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Top Bar: App Title, Step Indicator Pills, and Color Range Badge
                TopNavigationSection(hudState = hudState)

                // 2. Main Content Row: Brand Guidance (Left) + Cinema Accuracy Meter (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BrandSelectorCard(
                        hudState = hudState,
                        modifier = Modifier.weight(1f)
                    )

                    CinemaAccuracyMeter(
                        hudState = hudState,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 3. Step Instruction & Remote Control Hints
                BottomControlsSection(hudState = hudState)
            }
        }
    }
}

@Composable
private fun TopNavigationSection(hudState: HudState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Branding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                color = Color(0xFF00E5FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0284C7).copy(alpha = 0.3f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "4K D65",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Step Navigation Pills: [1]..[7]
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PatternStep.values().forEach { step ->
                val isActive = (step == hudState.currentStep)
                val isCalibrated = hudState.isStepCalibrated(step)

                val pillBg = when {
                    isActive -> Color(0xFF0284C7)
                    isCalibrated -> Color(0xFF15803D)
                    else -> Color(0xFF1E293B)
                }

                val pillBorder = when {
                    isActive -> Color(0xFF38BDF8)
                    isCalibrated -> Color(0xFF22C55E)
                    else -> Color(0xFF334155)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(pillBg)
                        .border(1.dp, pillBorder, RoundedCornerShape(6.dp))
                        .clickable { hudState.setStep(step) }
                        .focusable()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${step.id}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                        if (isCalibrated) {
                            Text(
                                text = "✓",
                                color = Color(0xFF86EFAC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Color Range Toggle Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                .clickable { hudState.toggleColorRange() }
                .focusable()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (hudState.colorRange == ColorRange.LIMITED) {
                    stringResource(R.string.color_range_limited)
                } else {
                    stringResource(R.string.color_range_full)
                },
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BottomControlsSection(hudState: HudState) {
    val stepTitle = when (hudState.currentStep) {
        PatternStep.PICTURE_MODE_BASE -> stringResource(R.string.step_picture_mode)
        PatternStep.ASPECT_RATIO -> stringResource(R.string.step_aspect_ratio)
        PatternStep.BLACK_LEVEL_PLUGE -> stringResource(R.string.step_black_level)
        PatternStep.WHITE_LEVEL_CONTRAST -> stringResource(R.string.step_white_level)
        PatternStep.SHARPNESS_GRID -> stringResource(R.string.step_sharpness)
        PatternStep.GRAYSCALE_D65 -> stringResource(R.string.step_grayscale)
        PatternStep.MOTION_24P_JUDDER -> stringResource(R.string.step_motion_judder)
    }

    val stepDesc = when (hudState.currentStep) {
        PatternStep.PICTURE_MODE_BASE -> stringResource(R.string.step_picture_mode_desc)
        PatternStep.ASPECT_RATIO -> stringResource(R.string.step_aspect_ratio_desc)
        PatternStep.BLACK_LEVEL_PLUGE -> stringResource(R.string.step_black_level_desc)
        PatternStep.WHITE_LEVEL_CONTRAST -> stringResource(R.string.step_white_level_desc)
        PatternStep.SHARPNESS_GRID -> stringResource(R.string.step_sharpness_desc)
        PatternStep.GRAYSCALE_D65 -> stringResource(R.string.step_grayscale_desc)
        PatternStep.MOTION_24P_JUDDER -> stringResource(R.string.step_motion_judder_desc)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Step Title and Detailed Description
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stepTitle,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stepDesc,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Navigation Hotkeys / Remote Control Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.nav_prev_step),
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
                Text(
                    text = stringResource(R.string.nav_next_step),
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
                Text(
                    text = stringResource(R.string.brand_switch_hint),
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
                Text(
                    text = stringResource(R.string.nav_toggle_hud),
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
            }

            Text(
                text = stringResource(R.string.nav_auto_hide_hint),
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }
    }
}
