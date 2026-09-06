package com.calai.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Thước đo bán nguyệt cao cấp (Dark Luxury Arc Gauge)
 * Tuân thủ quy tắc 9.2:
 * 1. Ambient glow / halo mềm mại phía sau số Calo trung tâm
 * 7. Progress ring gradient động (VividOrangeDark -> VividOrange -> VividOrangeLight) + glowing tip dot
 * 9. Chi tiết vi mô sang trọng: Typography phân cấp rõ rệt
 */
@Composable
fun ArcCaloriesGauge(
    remainingCalories: Int,
    targetCalories: Int,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    val rawProgress = if (targetCalories > 0) {
        ((targetCalories - remainingCalories).toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 900),
        label = "arc_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 220.dp, height = 130.dp)
        ) {
            val strokeWidth = 16.dp.toPx()
            val diameter = size.width - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, 12.dp.toPx())

            // 1. Ambient Halo Glow phía sau số Calo trung tâm (Spec 9.2 #1)
            val centerPoint = Offset(size.width / 2, size.height * 0.72f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VividOrangeGlow.copy(alpha = if (isDarkTheme) 0.35f else 0.12f),
                        VividOrangeSoft.copy(alpha = if (isDarkTheme) 0.15f else 0.04f),
                        Color.Transparent
                    ),
                    center = centerPoint,
                    radius = size.width * 0.45f
                ),
                radius = size.width * 0.45f,
                center = centerPoint
            )

            // 2. Vòng cung track nền
            drawArc(
                color = if (isDarkTheme) CharcoalBorder.copy(alpha = 0.8f) else PearlBorder,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Vòng cung tiến trình Gradient động (Spec 9.2 #7)
            if (animatedProgress > 0f) {
                val gradientBrush = Brush.horizontalGradient(
                    colors = listOf(
                        VividOrangeDark,
                        VividOrange,
                        VividOrangeLight
                    ),
                    startX = 0f,
                    endX = size.width
                )

                drawArc(
                    brush = gradientBrush,
                    startAngle = 180f,
                    sweepAngle = 180f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 4. Glowing indicator dot ở đầu vòng cung
                val currentAngleRad = Math.toRadians((180.0 + 180.0 * animatedProgress)).toFloat()
                val radius = diameter / 2f
                val arcCenter = Offset(topLeft.x + radius, topLeft.y + radius)
                val dotX = arcCenter.x + radius * cos(currentAngleRad)
                val dotY = arcCenter.y + radius * sin(currentAngleRad)

                // Vòng sáng mờ của dot
                drawCircle(
                    color = VividOrangeLight.copy(alpha = 0.4f),
                    radius = 8.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                // Nhân sáng của dot
                drawCircle(
                    color = TextWhite,
                    radius = 3.5.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        // 5. Chữ số Calo lớn trung tâm với tỷ lệ Editorial (Spec 9.2 #5)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 30.dp)
        ) {
            val formattedCalories = String.format("%,d", remainingCalories)
            Text(
                text = buildAnnotatedString {
                    append(formattedCalories)
                    withStyle(
                        SpanStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) TextMuted else TextInkMuted
                        )
                    ) {
                        append(" kcal")
                    }
                },
                color = if (isDarkTheme) TextWhite else TextInkPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Còn lại hôm nay",
                color = if (isDarkTheme) TextMuted else TextInkSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp
            )
        }
    }
}

