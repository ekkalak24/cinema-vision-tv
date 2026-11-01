package com.cinemavision.tv.ui.hud

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemavision.tv.R
import com.cinemavision.tv.domain.CinemaAccuracyLevel

/**
 * Visual meter displaying the live Cinema Reference Accuracy Match percentage,
 * level badge, animated progress bar, active step contribution, and calibration toggle button.
 */
@Composable
fun CinemaAccuracyMeter(
    hudState: HudState,
    modifier: Modifier = Modifier
) {
    CinemaAccuracyMeter(
        accuracyPercent = hudState.accuracyPercent,
        accuracyLevel = hudState.accuracyLevel,
        stepContribution = hudState.currentStepWeight,
        isStepCalibrated = hudState.isCurrentStepCalibrated,
        onToggleCalibrated = { hudState.toggleStepCalibrated() },
        modifier = modifier
    )
}

@Composable
fun CinemaAccuracyMeter(
    accuracyPercent: Int,
    accuracyLevel: CinemaAccuracyLevel,
    stepContribution: Int,
    isStepCalibrated: Boolean,
    onToggleCalibrated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelColor = when (accuracyLevel) {
        CinemaAccuracyLevel.FACTORY_DEFAULT -> Color(0xFF94A3B8) // Slate Gray
        CinemaAccuracyLevel.IMPROVED -> Color(0xFF38BDF8) // Sky Blue
        CinemaAccuracyLevel.HIGH_FIDELITY -> Color(0xFF818CF8) // Indigo
        CinemaAccuracyLevel.CINEMA_REFERENCE -> Color(0xFFF59E0B) // Amber / Gold
    }

    val levelText = when (accuracyLevel) {
        CinemaAccuracyLevel.FACTORY_DEFAULT -> stringResource(R.string.level_factory_default)
        CinemaAccuracyLevel.IMPROVED -> stringResource(R.string.level_improved)
        CinemaAccuracyLevel.HIGH_FIDELITY -> stringResource(R.string.level_high_fidelity)
        CinemaAccuracyLevel.CINEMA_REFERENCE -> stringResource(R.string.level_cinema_reference)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (accuracyPercent / 100f).coerceIn(0f, 1f),
        label = "AccuracyProgress"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xE61A222D)) // Card dark slate
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header: Title and Percentage Value
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.hud_cinema_match),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$accuracyPercent%",
                    color = levelColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Accuracy Level Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(levelColor.copy(alpha = 0.2f))
                    .border(1.dp, levelColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = levelText,
                    color = levelColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Live Match Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F172A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(levelColor)
            )
        }

        // Bottom Row: Step Contribution & Calibrate Toggle Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step contribution badge
            Text(
                text = stringResource(R.string.hud_step_contribution, stepContribution),
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            // Interactive Toggle Button (for D-Pad / Remote click)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isStepCalibrated) Color(0xFF15803D) else Color(0xFF334155)
                    )
                    .border(
                        1.dp,
                        if (isStepCalibrated) Color(0xFF22C55E) else Color(0xFF64748B),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleCalibrated() }
                    .focusable()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isStepCalibrated) "✓" else "○",
                        color = if (isStepCalibrated) Color(0xFFF0FDF4) else Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isStepCalibrated) {
                            stringResource(R.string.hud_step_calibrated)
                        } else {
                            stringResource(R.string.hud_mark_calibrated)
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
