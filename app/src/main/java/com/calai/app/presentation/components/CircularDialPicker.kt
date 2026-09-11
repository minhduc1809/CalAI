package com.calai.app.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * CIRCULAR DIAL PICKER có quán tính (Inertial Circular Dial Picker):
 * - Vòng tròn lớn ở giữa màn hình, vẽ bằng Canvas.
 * - GIAI ĐOẠN 1 (Đang chạm & kéo): Direct manipulation bám sát theo góc atan2 của ngón tay, không trễ, cập nhật realtime giá trị.
 * - GIAI ĐOẠN 2 (Sau khi nhả tay): Áp dụng quán tính qua VelocityTracker + exponentialDecay + snap về số nguyên gần nhất.
 * - Haptic feedback mỗi khi giá trị thay đổi 1 đơn vị.
 */
@Composable
fun CircularDialPicker(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    modifier: Modifier = Modifier,
    size: Dp = 300.dp,
    step: Float = 1f,
    majorStep: Int = 10,
    isDarkTheme: Boolean = true,
    accentColor: Color = VividOrange
) {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val density = LocalDensity.current

    // Màu sắc theo theme
    val dialBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline
    val tickNormalCol = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val tickMajorCol = MaterialTheme.colorScheme.onSecondaryContainer
    val textNumberCol = MaterialTheme.colorScheme.onSurfaceVariant
    val centerValueCol = MaterialTheme.colorScheme.onBackground

    // Góc quay (angle in degrees) tương ứng với giá trị.
    // Quy ước: Xoay thuận chiều kim đồng hồ (CW) = tăng giá trị.
    // Đặt mỗi 1 đơn vị = degreesPerUnit độ.
    // Ví dụ: 3 độ/đơn vị => 1 vòng 360 độ = 120 đơn vị. Rất mượt mà và dễ xoay.
    val degreesPerUnit = 3.5f

    // Góc hiện tại điều khiển bằng Animatable để hỗ trợ decay animation
    val currentAngleAnim = remember { Animatable(value * degreesPerUnit) }

    // Đồng bộ khi value bên ngoài thay đổi mà không trong lúc drag
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        if (!isDragging && abs(currentAngleAnim.value - value * degreesPerUnit) > 0.01f) {
            currentAngleAnim.snapTo(value * degreesPerUnit)
        }
    }

    // Giá trị nguyên hiện tại để kích hoạt Haptic khi nhảy số
    var lastHapticValue by remember { mutableIntStateOf(value.roundToInt()) }

    fun triggerHaptic(newVal: Int) {
        if (newVal != lastHapticValue) {
            lastHapticValue = newVal
            try {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            } catch (_: Exception) {}
        }
    }

    // Giới hạn góc tương ứng min/max range
    val minAngle = range.start * degreesPerUnit
    val maxAngle = range.endInclusive * degreesPerUnit

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(dialBg)
            .pointerInput(range, degreesPerUnit) {
                val centerOffset = Offset(size.toPx() / 2f, size.toPx() / 2f)
                val velocityTracker = VelocityTracker()

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isDragging = true
                    velocityTracker.resetTracking()
                    velocityTracker.addPosition(down.uptimeMillis, down.position)

                    var lastTouchAngle = atan2(
                        (down.position.y - centerOffset.y).toDouble(),
                        (down.position.x - centerOffset.x).toDouble()
                    ).toFloat()

                    // GIAI ĐOẠN 1: Ngón tay đang giữ và kéo
                    // Dừng mọi animation trước đó
                    coroutineScope.launch {
                        currentAngleAnim.stop()
                    }

                    var isPointerDown = true
                    while (isPointerDown) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()

                        if (change != null && change.pressed) {
                            velocityTracker.addPosition(change.uptimeMillis, change.position)

                            val currentTouchAngle = atan2(
                                (change.position.y - centerOffset.y).toDouble(),
                                (change.position.x - centerOffset.x).toDouble()
                            ).toFloat()

                            // Tính delta góc (xử lý nhảy qua -PI / +PI)
                            var deltaAngleRad = currentTouchAngle - lastTouchAngle
                            while (deltaAngleRad > Math.PI) deltaAngleRad -= (2 * Math.PI).toFloat()
                            while (deltaAngleRad < -Math.PI) deltaAngleRad += (2 * Math.PI).toFloat()

                            val deltaAngleDeg = Math.toDegrees(deltaAngleRad.toDouble()).toFloat()
                            lastTouchAngle = currentTouchAngle

                            // Cập nhật góc tức thời bám sát tay
                            val newAngle = (currentAngleAnim.value + deltaAngleDeg).coerceIn(minAngle, maxAngle)
                            coroutineScope.launch {
                                currentAngleAnim.snapTo(newAngle)
                            }

                            val calculatedValue = (newAngle / degreesPerUnit).coerceIn(range.start, range.endInclusive)
                            onValueChange(calculatedValue)
                            triggerHaptic(calculatedValue.roundToInt())

                            change.consume()
                        } else {
                            isPointerDown = false
                        }
                    }

                    // GIAI ĐOẠN 2: Thả tay ra -> Áp dụng quán tính (decay)
                    isDragging = false
                    val velocity = velocityTracker.calculateVelocity()
                    // Ước lượng vận tốc góc (degrees/sec) từ vận tốc tiếp tuyến (tangential velocity)
                    // v_angular = v_tangential / radius * (180 / PI)
                    val radiusPx = size.toPx() / 2f
                    // Vector từ tâm đến điểm chạm cuối
                    val rx = (down.position.x - centerOffset.x) / radiusPx
                    val ry = (down.position.y - centerOffset.y) / radiusPx
                    // Vận tốc tiếp tuyến = (-ry * vx + rx * vy)
                    val tangentialVelocityPxPerSec = -ry * velocity.x + rx * velocity.y
                    val angularVelocityDegPerSec = (tangentialVelocityPxPerSec / radiusPx) * (180f / Math.PI.toFloat())

                    coroutineScope.launch {
                        try {
                            // Quán tính decay xoay tiếp
                            val decaySpec = exponentialDecay<Float>(
                                frictionMultiplier = 1.6f,
                                absVelocityThreshold = 8f
                            )
                            currentAngleAnim.animateDecay(
                                initialVelocity = angularVelocityDegPerSec.coerceIn(-2500f, 2500f),
                                animationSpec = decaySpec
                            ) {
                                // Trong lúc trớn, liên tục cập nhật số và haptic
                                val boundedAngle = value.coerceIn(minAngle, maxAngle)
                                if (value < minAngle || value > maxAngle) {
                                    // Chặn lại khi chạm giới hạn
                                    coroutineScope.launch { currentAngleAnim.snapTo(boundedAngle) }
                                }
                                val realTimeVal = (boundedAngle / degreesPerUnit).coerceIn(range.start, range.endInclusive)
                                onValueChange(realTimeVal)
                                triggerHaptic(realTimeVal.roundToInt())
                            }
                        } catch (_: CancellationException) {
                            // Animation bị gián đoạn do chạm mới
                        } finally {
                            // Snap nhẹ về đúng vạch nguyên gần nhất
                            val finalSnappedValue = (currentAngleAnim.value / degreesPerUnit)
                                .roundToInt()
                                .toFloat()
                                .coerceIn(range.start, range.endInclusive)
                            val targetSnapAngle = finalSnappedValue * degreesPerUnit
                            currentAngleAnim.snapTo(targetSnapAngle)
                            onValueChange(finalSnappedValue)
                            triggerHaptic(finalSnappedValue.roundToInt())
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val currentValueFloat = (currentAngleAnim.value / degreesPerUnit).coerceIn(range.start, range.endInclusive)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val outerRadius = size.toPx() / 2f - 8.dp.toPx()
            val innerRadiusNormal = outerRadius - 14.dp.toPx()
            val innerRadiusMajor = outerRadius - 24.dp.toPx()

            // 1. Vẽ vòng viền mỏng ngoài
            drawCircle(
                color = borderCol,
                radius = outerRadius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )

            // 2. Vẽ các vạch chia xung quanh vòng tròn
            // Chỉ vẽ các vạch nằm trong phạm vi góc có thể nhìn thấy xung quanh
            // Vạch ở đỉnh (270 độ hoặc -90 độ) là vị trí con trỏ kim chỉ báo
            val baseAngle = currentAngleAnim.value
            val totalStart = range.start.toInt()
            val totalEnd = range.endInclusive.toInt()

            // Vẽ chữ số bằng native paint
            val textPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = density.run { 11.sp.toPx() }
                color = textNumberCol.toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.BOLD
                )
            }

            drawIntoCanvas { canvas ->
                for (unitVal in totalStart..totalEnd step step.toInt()) {
                    // Góc của vạch này trên vòng:
                    // Kim chỉ nằm ở đỉnh trên (góc 270° hoặc -90°).
                    // Khi currentAngleAnim = unitVal * degreesPerUnit, vạch này sẽ nằm ngay đỉnh (-90°).
                    val angleOffsetFromCurrent = (unitVal * degreesPerUnit) - baseAngle
                    val drawAngleDeg = -90f + angleOffsetFromCurrent

                    // Tối ưu: Chỉ vẽ những vạch nằm trên vòng
                    val angleRad = Math.toRadians(drawAngleDeg.toDouble())
                    val isMajor = (unitVal % majorStep == 0)

                    val startR = if (isMajor) innerRadiusMajor else innerRadiusNormal
                    val pStart = Offset(
                        x = center.x + startR * cos(angleRad).toFloat(),
                        y = center.y + startR * sin(angleRad).toFloat()
                    )
                    val pEnd = Offset(
                        x = center.x + outerRadius * cos(angleRad).toFloat(),
                        y = center.y + outerRadius * sin(angleRad).toFloat()
                    )

                    drawLine(
                        color = if (isMajor) tickMajorCol else tickNormalCol,
                        start = pStart,
                        end = pEnd,
                        strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.2.dp.toPx()
                    )

                    // Vẽ số mỗi majorStep (10 đơn vị)
                    if (isMajor) {
                        val textRadius = innerRadiusMajor - 16.dp.toPx()
                        val tx = center.x + textRadius * cos(angleRad).toFloat()
                        val ty = center.y + textRadius * sin(angleRad).toFloat() + 4.dp.toPx()
                        canvas.nativeCanvas.drawText(unitVal.toString(), tx, ty, textPaint)
                    }
                }
            }

            // 3. Điểm/Kim chỉ báo cố định ở đỉnh trên (Pointer Indicator)
            val pointerLen = 16.dp.toPx()
            val pointerTop = Offset(center.x, center.y - outerRadius - 3.dp.toPx())
            val pointerBottom = Offset(center.x, center.y - outerRadius + pointerLen)

            // Vạch kim màu cam nổi bật
            drawLine(
                color = accentColor,
                start = pointerTop,
                end = pointerBottom,
                strokeWidth = 3.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Chấm tròn nhỏ ở chân kim
            drawCircle(
                color = accentColor,
                radius = 3.5.dp.toPx(),
                center = pointerTop
            )
        }

        // 4. Số lớn hiển thị giá trị hiện tại ở chính giữa vòng tròn
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentValueFloat.roundToInt().toString(),
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                color = centerValueCol,
                lineHeight = 62.sp
            )
            Text(
                text = unit.lowercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = VividOrange,
                letterSpacing = 1.sp
            )
        }
    }
}
