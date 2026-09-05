package com.calai.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calai.app.presentation.theme.*

/**
 * Bộ Custom Duotone Vector Icons chuẩn CalAI Design System (CODING_RULES.md 9.7 & 10.3)
 * Đặc trưng:
 * - Nét vẽ dày 1.5 - 1.75px, bo tròn đầu nét (StrokeCap.Round, StrokeJoin.Round)
 * - Đúng 2 lớp màu: Lớp nét viền ngoài (Outline) + Lớp nhấn tâm (Accent Fill)
 * - Tinh giản hình học sang trọng, TUYỆT ĐỐI KHÔNG dùng emoji hay icon 3D generic
 */

@Composable
fun DuotoneDietIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = TextWhite,
    accentColor: Color = VividOrange,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Đĩa tròn ngoài
        drawCircle(
            color = primaryColor,
            radius = w * 0.42f,
            center = Offset(w / 2, h / 2),
            style = stroke
        )

        // Tâm đĩa thức ăn (Lớp accent)
        drawCircle(
            color = accentColor,
            radius = w * 0.14f,
            center = Offset(w / 2, h / 2)
        )

        // Vạch nĩa & dao tối giản
        drawLine(
            color = primaryColor.copy(alpha = 0.6f),
            start = Offset(w * 0.28f, h * 0.35f),
            end = Offset(w * 0.28f, h * 0.65f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = primaryColor.copy(alpha = 0.6f),
            start = Offset(w * 0.72f, h * 0.35f),
            end = Offset(w * 0.72f, h * 0.65f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DuotoneWorkoutIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = TextWhite,
    accentColor: Color = VividOrange,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Trục tạ
        drawLine(
            color = primaryColor,
            start = Offset(w * 0.22f, h / 2),
            end = Offset(w * 0.78f, h / 2),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Bánh tạ 2 bên
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(w * 0.12f, h * 0.28f),
            size = Size(w * 0.14f, h * 0.44f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke
        )
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(w * 0.74f, h * 0.28f),
            size = Size(w * 0.14f, h * 0.44f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke
        )

        // Lõi tạ accent fill
        drawCircle(
            color = accentColor,
            radius = w * 0.09f,
            center = Offset(w / 2, h / 2)
        )
    }
}

@Composable
fun DuotoneDumbbellIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = TextWhite,
    accentColor: Color = VividOrange,
    primaryColor: Color = outlineColor
) {
    DuotoneWorkoutIcon(modifier, size, outlineColor, accentColor, primaryColor)
}

@Composable
fun DuotoneCalendarIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = TextWhite,
    accentColor: Color = LavenderGradientStart,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Khung lịch
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(w * 0.12f, h * 0.18f),
            size = Size(w * 0.76f, h * 0.70f),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
            style = stroke
        )

        // Thanh ngang lịch
        drawLine(
            color = primaryColor.copy(alpha = 0.5f),
            start = Offset(w * 0.12f, h * 0.38f),
            end = Offset(w * 0.88f, h * 0.38f),
            strokeWidth = 1.4.dp.toPx()
        )

        // 2 móc treo
        drawLine(
            color = primaryColor,
            start = Offset(w * 0.32f, h * 0.08f),
            end = Offset(w * 0.32f, h * 0.22f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = primaryColor,
            start = Offset(w * 0.68f, h * 0.08f),
            end = Offset(w * 0.68f, h * 0.22f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Điểm chấm ngày active (Accent fill)
        drawCircle(
            color = accentColor,
            radius = w * 0.09f,
            center = Offset(w * 0.5f, h * 0.62f)
        )
    }
}

@Composable
fun DuotoneExerciseIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = TextWhite,
    accentColor: Color = VividOrange,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        // Hình tia sét / năng lượng hình học
        val path = Path().apply {
            moveTo(w * 0.55f, h * 0.12f)
            lineTo(w * 0.25f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.52f)
            lineTo(w * 0.45f, h * 0.88f)
            lineTo(w * 0.75f, h * 0.44f)
            lineTo(w * 0.52f, h * 0.44f)
            close()
        }

        drawPath(path = path, color = primaryColor, style = stroke)
        drawCircle(
            color = accentColor,
            radius = w * 0.11f,
            center = Offset(w * 0.48f, h * 0.48f)
        )
    }
}

@Composable
fun DuotoneEnergyIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = TextWhite,
    accentColor: Color = VividOrange,
    primaryColor: Color = outlineColor
) {
    DuotoneExerciseIcon(modifier, size, outlineColor, accentColor, primaryColor)
}

@Composable
fun DuotoneCheckmarkIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = TextWhite,
    accentColor: Color = PastelMint,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.72f)
            lineTo(w * 0.78f, h * 0.28f)
        }
        drawPath(path = path, color = primaryColor, style = stroke)
    }
}
