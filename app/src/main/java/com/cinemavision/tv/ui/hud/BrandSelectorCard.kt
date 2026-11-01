package com.cinemavision.tv.ui.hud

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinemavision.tv.R
import com.cinemavision.tv.domain.BrandTipProvider
import com.cinemavision.tv.domain.PatternStep
import com.cinemavision.tv.domain.TvBrand

/**
 * Brand-specific contextual setting card displaying exact TV menu paths
 * and values for Sony, TCL, Hisense, Samsung, LG, and Universal displays.
 */
@Composable
fun BrandSelectorCard(
    hudState: HudState,
    modifier: Modifier = Modifier
) {
    BrandSelectorCard(
        selectedBrand = hudState.selectedBrand,
        currentStep = hudState.currentStep,
        onBrandSelected = { brand -> hudState.selectBrand(brand) },
        onNextBrand = { hudState.nextBrand() },
        onPreviousBrand = { hudState.previousBrand() },
        modifier = modifier
    )
}

@Composable
fun BrandSelectorCard(
    selectedBrand: TvBrand,
    currentStep: PatternStep,
    onBrandSelected: (TvBrand) -> Unit = {},
    onNextBrand: () -> Unit = {},
    onPreviousBrand: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val brandDisplayName = when (selectedBrand) {
        TvBrand.SONY -> stringResource(R.string.brand_sony)
        TvBrand.TCL -> stringResource(R.string.brand_tcl)
        TvBrand.HISENSE -> stringResource(R.string.brand_hisense)
        TvBrand.SAMSUNG_BOX -> stringResource(R.string.brand_samsung)
        TvBrand.LG_BOX -> stringResource(R.string.brand_lg)
        TvBrand.UNIVERSAL -> stringResource(R.string.brand_universal)
    }

    val tip = BrandTipProvider.getTipForStep(selectedBrand, currentStep)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xE61A222D)) // Card dark slate
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card Header: Brand Guidance Title and Selected Brand Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.brand_selector_title),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = brandDisplayName,
                    color = Color(0xFFF8FAFC),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // D-Pad Switch Hint & Interactive Brand Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF334155))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
                        .clickable { onPreviousBrand() }
                        .focusable()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▲",
                        color = Color(0xFFF8FAFC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF334155))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
                        .clickable { onNextBrand() }
                        .focusable()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▼",
                        color = Color(0xFFF8FAFC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Contextual Instruction Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.recommended_setting),
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tip,
                    color = Color(0xFFF1F5F9),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp
                )
            }
        }

        // Footer Switch Hint
        Text(
            text = stringResource(R.string.brand_switch_hint),
            color = Color(0xFF64748B),
            fontSize = 11.sp
        )
    }
}
