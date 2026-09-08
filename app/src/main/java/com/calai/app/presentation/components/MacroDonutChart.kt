package com.calai.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.*

/**
 * Donut chart động cho phân bổ Protein/Carb/Fat — 3 cung nối tiếp, gradient động,
 * animate sweep từ 0 khi dữ liệu thay đổi. Tuân thủ Dark Luxury Canvas (dùng brush có sẵn trong Color.kt).
 */
@Composable
fun MacroDonutChart(
    proteinPercent: Int,
    carbPercent: Int,
    fatPercent: Int,
    centerLabel: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProtein by animateFloatAsState(
        targetValue = proteinPercent.toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "protein_sweep"
    )
    val animatedCarb by animateFloatAsState(
        targetValue = carbPercent.toFloat(),
        animationSpec = tween(durationMillis = 900, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "carb_sweep"
    )
    val animatedFat by animateFloatAsState(
        targetValue = fatPercent.toFloat(),
        animationSpec = tween(durationMillis = 900, delayMillis = 240, easing = FastOutSlowInEasing),
        label = "fat_sweep"
    )

    Box(modifier = modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Track nền
            drawArc(
                color = if (isDarkTheme) CharcoalBorder.copy(alpha = 0.6f) else PearlBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            var startAngle = -90f

            val proteinSweep = 360f * (animatedProtein / 100f)
            if (proteinSweep > 0f) {
                drawArc(
                    brush = if (isDarkTheme) ProteinBrush else ProteinBrushLight,
                    startAngle = startAngle,
                    sweepAngle = proteinSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += proteinSweep
            }

            val carbSweep = 360f * (animatedCarb / 100f)
            if (carbSweep > 0f) {
                drawArc(
                    brush = if (isDarkTheme) CarbBrush else CarbBrushLight,
                    startAngle = startAngle,
                    sweepAngle = carbSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += carbSweep
            }

            val fatSweep = 360f * (animatedFat / 100f)
            if (fatSweep > 0f) {
                drawArc(
                    brush = if (isDarkTheme) FatBrush else FatBrushLight,
                    startAngle = startAngle,
                    sweepAngle = fatSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkTheme) TextWhite else TextInkPrimary
            )
            Text(
                text = "kcal TB",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) TextMuted else TextInkMuted
            )
        }
    }
}
