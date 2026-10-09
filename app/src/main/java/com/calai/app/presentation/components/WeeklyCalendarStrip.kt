package com.calai.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class DayItem(
    val dayOfWeek: String, // T2, T3, T4...
    val dayOfMonth: String, // 08, 09, 10...
    val dateIso: String,    // YYYY-MM-DD
    val isSelected: Boolean = false,
    val isToday: Boolean = false
)

/**
 * Floating Week Selector — Premium redesign
 * - Shadow mềm cho cả strip
 * - Active pill: LavenderBrush + shadow riêng
 * - Today: dot indicator nhỏ cam bên dưới số ngày
 * - Scale animation khi chọn ngày mới
 */
@Composable
fun WeeklyCalendarStrip(
    selectedDateIso: String,
    onDateSelected: (String) -> Unit,
    isDarkTheme: Boolean = true,
    incompleteDates: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    val days = remember(selectedDateIso) {
        generateWeekDays(selectedDateIso)
    }
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 5.dp else 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { day ->
                DayPill(
                    day = day,
                    isDarkTheme = isDarkTheme,
                    showIncompleteDot = day.dateIso in incompleteDates,
                    onClick = { onDateSelected(day.dateIso) }
                )
            }
        }
    }
}

@Composable
private fun DayPill(
    day: DayItem,
    isDarkTheme: Boolean = true,
    showIncompleteDot: Boolean = false,
    onClick: () -> Unit
) {
    val isSelected = day.isSelected

    // Nảy nhẹ kiểu "cute" khi được chọn — spring bouncy thay vì tween phẳng, có cảm giác ngộ nghĩnh.
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "day_scale_${day.dateIso}"
    )
    val liftY by animateFloatAsState(
        targetValue = if (isSelected) -4f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "day_lift_${day.dateIso}"
    )

    val pillShadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    val pillShape = RoundedCornerShape(20.dp)
    val pillHeight = if (isSelected) 76.dp else 74.dp

    Box(
        modifier = Modifier
            .width(42.dp)
            .height(94.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Chấm cảnh báo: ngày đã có bữa ăn nhưng chưa được tính là đầy đủ (BR-07.6)
        if (showIncompleteDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CoralWarning)
            )
        }

        // Tai gấu ôm sát 2 góc trên của ô (không phải đầu tròn nổi tách rời) — đúng yêu cầu "ôm vào ô".
        if (isSelected) {
            BearEars(
                pillWidth = 42.dp,
                modifier = Modifier.offset(y = (liftY + 10).dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 18.dp)
                .width(42.dp)
                .height(pillHeight)
                .scale(scale)
                .offset(y = liftY.dp)
                .then(
                    if (isSelected) {
                        Modifier
                            .shadow(
                                elevation = if (isDarkTheme) 8.dp else 12.dp,
                                shape = pillShape,
                                ambientColor = pillShadowColor,
                                spotColor = pillShadowColor
                            )
                    } else Modifier
                )
                .clip(pillShape)
                .background(
                    // Đổi hẳn sang tông cam chủ đạo — ô đang chọn gradient cam đậm, ô thường nền cam nhạt
                    // (trước đó hoàn toàn trong suốt/trắng trơn, không đồng bộ màu thương hiệu).
                    if (isSelected) Brush.verticalGradient(listOf(VividOrangeLight, VividOrange))
                    else Brush.verticalGradient(listOf(VividOrange.copy(alpha = 0.10f), VividOrange.copy(alpha = 0.10f)))
                )
                .then(
                    if (isSelected) {
                        Modifier.border(
                            0.75.dp,
                            Color.White.copy(alpha = if (isDarkTheme) 0.45f else 0.70f),
                            pillShape
                        )
                    } else Modifier
                )
                .clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day.dayOfWeek,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White
                    else if (day.isToday) VividOrange
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = day.dayOfMonth,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White
                    else if (day.isToday) VividOrange
                    else MaterialTheme.colorScheme.onBackground
                )
            }

            // Mõm gấu ở đáy mỗi ô — chỉ mắt + mũi, KHÔNG vẽ miệng cười. Ô đang chọn: "thân" trắng
            // CHẠM SÁT đáy + 2 góc bo (bottomStart/End) TRÙNG KHỚP với pillShape — không còn padding
            // dưới của Column cũ khiến mảng trắng bị thụt vào, hở góc bo tím phía dưới như trước.
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp, topStart = 10.dp, topEnd = 10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    BearSnoutFace(dotColor = TextDeepInk.copy(alpha = 0.75f))
                }
            } else {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
                    BearSnoutFace(
                        dotColor = if (day.isToday) VividOrange.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

/** 2 tai gấu ôm sát 2 góc trên của ô lịch (không phải đầu tròn nổi tách rời khỏi ô). */
@Composable
private fun BearEars(pillWidth: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val earSize = 16.dp
    Row(
        modifier = modifier.width(pillWidth),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BearEar(earSize)
        BearEar(earSize)
    }
}

@Composable
private fun BearEar(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(3.dp, CircleShape, ambientColor = DarkShadow, spotColor = DarkShadow)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.5f)
                .clip(CircleShape)
                .background(VividOrangeLight.copy(alpha = 0.6f))
        )
    }
}

/** Mõm gấu tối giản: chỉ 2 mắt chấm tròn + 1 mũi — KHÔNG vẽ miệng cười theo yêu cầu thiết kế. */
@Composable
private fun BearSnoutFace(dotColor: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxWidth().height(12.dp)) {
        val w = this.size.width; val h = this.size.height
        val eyeY = h * 0.32f
        val eyeOffsetX = w * 0.24f
        drawCircle(dotColor, radius = h * 0.17f, center = Offset(w / 2f - eyeOffsetX, eyeY))
        drawCircle(dotColor, radius = h * 0.17f, center = Offset(w / 2f + eyeOffsetX, eyeY))
        // Mũi — hình oval nhỏ ở giữa, thấp hơn mắt một chút
        drawOval(
            color = dotColor,
            topLeft = Offset(w / 2f - h * 0.22f, h * 0.62f),
            size = androidx.compose.ui.geometry.Size(h * 0.44f, h * 0.3f)
        )
    }
}

private fun generateWeekDays(selectedDateIso: String): List<DayItem> {
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    val dayNames = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    val todayIso = isoFormat.format(Date())

    val list = mutableListOf<DayItem>()
    for (i in 0..6) {
        val currentIso = isoFormat.format(calendar.time)
        val dayNum = dayFormat.format(calendar.time)
        list.add(
            DayItem(
                dayOfWeek = dayNames[i],
                dayOfMonth = dayNum,
                dateIso = currentIso,
                isSelected = currentIso == selectedDateIso,
                isToday = currentIso == todayIso
            )
        )
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    return list
}
