package com.calai.app.presentation.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.AppElevation
import com.calai.app.presentation.theme.ShadowDarkL2Ambient
import com.calai.app.presentation.theme.ShadowDarkL2Spot
import com.calai.app.presentation.theme.ShadowLightL2Ambient
import com.calai.app.presentation.theme.ShadowLightL2Spot
import com.calai.app.presentation.theme.TextDeepInk

/**
 * Premium Interactive Macro Card
 * - Percentage badge ở góc trên-phải cạnh title
 * - Custom canvas rounded gradient progress bar với subtle glow
 * - Icon nổi với gradient background
 * - Số grams lớn hơn + "of Xg" sub label
 * - Animated fill mượt (EaseOutCubic)
 */
@Composable
fun BentoMacroCard(
    title: String,
    consumedGrams: Int,
    targetGrams: Int,
    gradientColors: List<Color>,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    val rawProgress = if (targetGrams > 0) {
        (consumedGrams.toFloat() / targetGrams.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val easeOutCubic = Easing { t ->
        val t1 = t - 1f
        t1 * t1 * t1 + 1f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 900, easing = easeOutCubic),
        label = "macro_progress_${title}"
    )

    val percentage = (rawProgress * 100).toInt()
    val cardBrush = Brush.verticalGradient(gradientColors)
    val shadowAmbient = if (isDarkTheme) ShadowDarkL2Ambient else ShadowLightL2Ambient
    val shadowSpot    = if (isDarkTheme) ShadowDarkL2Spot    else ShadowLightL2Spot

    // Progress bar colors — dùng tông đậm của gradientColors
    val progressBarColor = gradientColors.firstOrNull() ?: gradientColors[0]
    val progressTrackColor = TextDeepInk.copy(alpha = 0.10f)
    val progressGlowColor = gradientColors.firstOrNull()?.copy(alpha = 0.35f) ?: Color.Transparent

    Box(
        modifier = modifier
            .height(152.dp)
            .shadow(
                elevation = AppElevation.L2,
                shape = RoundedCornerShape(26.dp),
                ambientColor = shadowAmbient,
                spotColor = shadowSpot
            )
            .clip(RoundedCornerShape(26.dp))
            .background(cardBrush)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDarkTheme) 0.40f else 0.70f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(16.dp)
    ) {
        // Specular highlight góc trên-trái
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 32.dp.toPx()
                ),
                radius = 32.dp.toPx(),
                center = Offset(0f, 0f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Row: Title + Percentage badge + Icon ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDeepInk,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    // Percentage badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TextDeepInk.copy(alpha = 0.10f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${percentage}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDeepInk.copy(alpha = 0.75f)
                        )
                    }
                }

                // Icon với gradient circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TextDeepInk.copy(alpha = 0.08f))
                        .border(0.75.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextDeepInk,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Bottom: grams + progress bar ──────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Số grams lớn
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "${consumedGrams}g",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDeepInk,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "/ ${targetGrams}g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDeepInk.copy(alpha = 0.55f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Custom gradient rounded progress bar với glow
                Box(modifier = Modifier.fillMaxWidth()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    ) {
                        val barHeight = size.height
                        val cornerRadius = barHeight / 2f

                        // Track
                        drawRoundRect(
                            color = progressTrackColor,
                            size = Size(size.width, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                        )

                        // Glow layer (chỉ khi progress > 0)
                        if (animatedProgress > 0.01f) {
                            val progressWidth = size.width * animatedProgress
                            drawRoundRect(
                                color = progressGlowColor,
                                size = Size(progressWidth, barHeight + 3.dp.toPx()),
                                topLeft = Offset(0f, -1.5.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                            )

                            // Gradient progress fill
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        progressBarColor.copy(alpha = 0.7f),
                                        progressBarColor
                                    ),
                                    startX = 0f,
                                    endX = progressWidth
                                ),
                                size = Size(progressWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                            )

                            // White tip highlight
                            val strokeWidth = 2.5.dp.toPx()
                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = strokeWidth,
                                center = Offset(progressWidth - strokeWidth, barHeight / 2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
