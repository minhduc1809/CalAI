package com.calai.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.TextDeepInk

/**
 * Thẻ Bento Macro dạng Đá Quý (Gemstone Pastel Bento Card)
 * Tuân thủ quy tắc 9.2:
 * 4. Gradient 2 tông cùng họ màu + Specular light highlight góc trên-trái
 * 6. Icon glassmorphism nhẹ
 * 9. Chi tiết vi mô sang trọng: Bo góc 24-28dp, tỉ lệ phân cấp chữ
 */
@Composable
fun BentoMacroCard(
    title: String,
    consumedGrams: Int,
    targetGrams: Int,
    gradientColors: List<Color>,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val rawProgress = if (targetGrams > 0) {
        (consumedGrams.toFloat() / targetGrams.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 800),
        label = "macro_progress"
    )

    val cardBrush = Brush.verticalGradient(gradientColors)

    Box(
        modifier = modifier
            .height(138.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(cardBrush)
            .padding(16.dp)
    ) {
        // 1. Giả lập hiệu ứng highlight phản chiếu ánh sáng bề mặt đá quý (Spec 9.2 #4)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(24.dp.toPx(), 24.dp.toPx()),
                    radius = 50.dp.toPx()
                ),
                radius = 50.dp.toPx(),
                center = Offset(24.dp.toPx(), 24.dp.toPx())
            )
        }

        // 2. Icon Glassmorphism tròn ở góc trên phải (Spec 9.2 #6)
        Box(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(TextDeepInk.copy(alpha = 0.09f))
                .border(0.75.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextDeepInk,
                modifier = Modifier.size(18.dp)
            )
        }

        // 3. Nội dung văn bản & thanh tiến trình
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepInk,
                letterSpacing = (-0.2).sp
            )

            Column {
                // Thanh tiến trình đen bo tròn đầu
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TextDeepInk,
                    trackColor = TextDeepInk.copy(alpha = 0.14f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${consumedGrams}g",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDeepInk
                    )
                    Text(
                        text = "/ ${targetGrams}g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDeepInk.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

