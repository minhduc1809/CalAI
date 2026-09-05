package com.calai.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.TextDeepInk

/**
 * Thẻ Bento hiển thị một nhóm chất dinh dưỡng (Protein, Carbs, Fat) với nền màu Pastel
 */
@Composable
fun BentoMacroCard(
    title: String,
    consumedGrams: Int,
    targetGrams: Int,
    containerColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val progress = if (targetGrams > 0) {
        (consumedGrams.toFloat() / targetGrams.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .padding(16.dp)
    ) {
        // Icon huy hiệu tròn mờ góc trên bên phải
        Box(
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(TextDeepInk.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextDeepInk,
                modifier = Modifier.size(18.dp)
            )
        }

        // Nội dung chính
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepInk
            )

            Column {
                // Thanh tiến trình đen bo tròn đầu
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TextDeepInk,
                    trackColor = TextDeepInk.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${consumedGrams}g",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDeepInk
                    )
                    Text(
                        text = "${targetGrams}g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDeepInk.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
