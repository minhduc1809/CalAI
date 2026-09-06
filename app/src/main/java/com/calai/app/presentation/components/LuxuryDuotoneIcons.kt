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

@Composable
fun DuotoneSunIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = VividOrange,
    accentColor: Color = CarbGradientStart,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val w = this.size.width
        val h = this.size.height
        val c = Offset(w / 2, h / 2)

        // Tâm mặt trời
        drawCircle(color = accentColor, radius = w * 0.22f, center = c)
        drawCircle(color = primaryColor, radius = w * 0.22f, center = c, style = stroke)

        // Các tia nắng tỏa 8 hướng
        val rayInner = w * 0.32f
        val rayOuter = w * 0.44f
        val angles = listOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)
        for (a in angles) {
            val rad = Math.toRadians(a)
            val cos = Math.cos(rad).toFloat()
            val sin = Math.sin(rad).toFloat()
            drawLine(
                color = primaryColor,
                start = Offset(c.x + rayInner * cos, c.y + rayInner * sin),
                end = Offset(c.x + rayOuter * cos, c.y + rayOuter * sin),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun DuotoneMoonIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = PastelLavender,
    accentColor: Color = LavenderGradientEnd,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.65f, h * 0.15f)
            cubicTo(w * 0.25f, h * 0.20f, w * 0.20f, h * 0.80f, w * 0.68f, h * 0.85f)
            cubicTo(w * 0.42f, h * 0.72f, w * 0.42f, h * 0.28f, w * 0.65f, h * 0.15f)
            close()
        }

        drawPath(path = path, color = accentColor.copy(alpha = 0.35f))
        drawPath(path = path, color = primaryColor, style = stroke)

        // Ngôi sao nhỏ điểm nhấn
        drawCircle(
            color = primaryColor,
            radius = 1.5.dp.toPx(),
            center = Offset(w * 0.75f, h * 0.35f)
        )
    }
}

@Composable
fun DuotoneFlameIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = VividOrange,
    accentColor: Color = CarbGradientStart,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val outerPath = Path().apply {
            moveTo(w * 0.50f, h * 0.10f)
            cubicTo(w * 0.65f, h * 0.30f, w * 0.88f, h * 0.55f, w * 0.78f, h * 0.78f)
            cubicTo(w * 0.70f, h * 0.92f, w * 0.30f, h * 0.92f, w * 0.22f, h * 0.78f)
            cubicTo(w * 0.12f, h * 0.55f, w * 0.40f, h * 0.38f, w * 0.42f, h * 0.22f)
            close()
        }
        drawPath(path = outerPath, color = primaryColor, style = stroke)

        // Ngọn lửa lõi accent
        val innerPath = Path().apply {
            moveTo(w * 0.50f, h * 0.52f)
            cubicTo(w * 0.60f, h * 0.62f, w * 0.62f, h * 0.78f, w * 0.50f, h * 0.84f)
            cubicTo(w * 0.38f, h * 0.78f, w * 0.40f, h * 0.62f, w * 0.50f, h * 0.52f)
            close()
        }
        drawPath(path = innerPath, color = accentColor)
    }
}

@Composable
fun DuotoneWaterIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    outlineColor: Color = PastelLavenderLight,
    accentColor: Color = LavenderGradientStart,
    primaryColor: Color = outlineColor
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.50f, h * 0.14f)
            cubicTo(w * 0.68f, h * 0.42f, w * 0.82f, h * 0.62f, w * 0.76f, h * 0.78f)
            cubicTo(w * 0.68f, h * 0.90f, w * 0.32f, h * 0.90f, w * 0.24f, h * 0.78f)
            cubicTo(w * 0.18f, h * 0.62f, w * 0.32f, h * 0.42f, w * 0.50f, h * 0.14f)
            close()
        }
        drawPath(path = path, color = accentColor.copy(alpha = 0.3f))
        drawPath(path = path, color = primaryColor, style = stroke)
    }
}
