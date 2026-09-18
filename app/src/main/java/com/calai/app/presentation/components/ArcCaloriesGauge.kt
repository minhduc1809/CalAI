package com.calai.app.presentation.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium Circular Calorie Ring
 * - Full 270° sweep arc (thay bán nguyệt cũ)
 * - Multi-layer: outer glow track + inner track + gradient progress stroke
 * - Glowing tip dot với halo
 * - Animated draw từ 0 → progress (900ms EaseOutQuart)
 * - Center: số kcal lớn + unit + label + consumed info
 */
@Composable
fun ArcCaloriesGauge(
    remainingCalories: Int,
    targetCalories: Int,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    ringSize: Dp = 200.dp
) {
    val consumedCalories = (targetCalories - remainingCalories).coerceAtLeast(0)
    val rawProgress = if (targetCalories > 0) {
        (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // EaseOutQuart easing cho animation mượt hơn
    val easeOutQuart = Easing { t ->
        val t1 = t - 1f
        1f - t1 * t1 * t1 * t1
    }

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 1100, easing = easeOutQuart),
        label = "calorie_ring_progress"
    )

    // Góc bắt đầu: 135° (góc 7h30 — để arc 270° nằm đẹp)
    val startAngleDeg = 135f
    val sweepAngleDeg = 270f

    Box(
        modifier = modifier
            .size(ringSize)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val glowStrokeWidth = 30.dp.toPx()
            val innerPad = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(innerPad, innerPad)
            val centerPoint = Offset(size.width / 2f, size.height / 2f)

            // ── Layer 1: Outer ambient glow ring (mờ, track) ──────────────────────
            drawArc(
                color = VividOrange.copy(alpha = if (isDarkTheme) 0.06f else 0.04f),
                startAngle = startAngleDeg,
                sweepAngle = sweepAngleDeg,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = glowStrokeWidth, cap = StrokeCap.Round)
            )

            // ── Layer 2: Inner track (nền arc) ────────────────────────────────────
            val trackColor = if (isDarkTheme)
                Color.White.copy(alpha = 0.08f)
            else
                Color(0xFF1A2540).copy(alpha = 0.07f)
            drawArc(
                color = trackColor,
                startAngle = startAngleDeg,
                sweepAngle = sweepAngleDeg,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // ── Layer 3: Gradient progress arc ───────────────────────────────────
            if (animatedProgress > 0.005f) {
                val progressSweep = sweepAngleDeg * animatedProgress
                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(
                        VividOrangeDark,
                        VividOrange,
                        VividOrangeLight,
                        Color(0xFFFFCCA0)
                    ),
                    center = centerPoint
                )
                drawArc(
                    brush = gradientBrush,
                    startAngle = startAngleDeg,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // ── Layer 4: Glowing tip dot ──────────────────────────────────────
                val tipAngleRad = Math.toRadians(
                    (startAngleDeg + progressSweep).toDouble()
                ).toFloat()
                val arcRadius = (arcSize.width / 2f)
                val tipX = centerPoint.x + arcRadius * cos(tipAngleRad)
                val tipY = centerPoint.y + arcRadius * sin(tipAngleRad)

                // Outer halo
                drawCircle(
                    color = VividOrangeLight.copy(alpha = 0.35f),
                    radius = 10.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
                // Mid ring
                drawCircle(
                    color = VividOrange.copy(alpha = 0.6f),
                    radius = 6.5.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
                // Core white dot
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
            }

            // ── Layer 5: Central ambient glow ────────────────────────────────────
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VividOrangeGlow.copy(alpha = if (isDarkTheme) 0.25f else 0.10f),
                        Color.Transparent
                    ),
                    center = centerPoint,
                    radius = size.width * 0.38f
                ),
                radius = size.width * 0.38f,
                center = centerPoint
            )
        }

        // ── Center text ───────────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Label nhỏ trên số
            Text(
                text = "CÒN LẠI",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Số kcal lớn
            val formattedRemaining = String.format("%,d", remainingCalories.coerceAtLeast(0))
            Text(
                text = buildAnnotatedString {
                    append(formattedRemaining)
                    withStyle(
                        SpanStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        append("\nkcal")
                    }
                },
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp,
                lineHeight = 38.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Đã nạp info
            val consumedFormatted = String.format("%,d", consumedCalories)
            Text(
                text = "Đã nạp $consumedFormatted kcal",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = VividOrange,
                textAlign = TextAlign.Center
            )
        }
    }
}
