package com.calai.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.TextDeepInk

/**
 * Thước đo bán nguyệt (Arc Gauge) hiển thị Calo còn lại theo chuẩn thiết kế ảnh mẫu
 */
@Composable
fun ArcCaloriesGauge(
    remainingCalories: Int,
    targetCalories: Int,
    modifier: Modifier = Modifier,
    arcColor: Color = TextDeepInk,
    trackColor: Color = TextDeepInk.copy(alpha = 0.12f)
) {
    val progress = if (targetCalories > 0) {
        ((targetCalories - remainingCalories).toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 180.dp, height = 110.dp)
        ) {
            val strokeWidth = 22.dp.toPx()
            val diameter = size.width
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            // Vòng cung nền (nửa hình tròn từ 180 độ đến 0 độ)
            drawArc(
                color = trackColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(0f, 10.dp.toPx()),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Vòng cung tiến trình đã nạp
            drawArc(
                color = arcColor,
                startAngle = 180f,
                sweepAngle = 180f * progress,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(0f, 10.dp.toPx()),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Chữ số calo lớn và nhãn ở trung tâm vòng cung
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Text(
                text = "$remainingCalories",
                color = arcColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp
            )
            Text(
                text = "kcal còn lại",
                color = arcColor.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
