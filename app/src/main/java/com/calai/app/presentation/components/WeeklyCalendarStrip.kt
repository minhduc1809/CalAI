package com.calai.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val isSelected: Boolean = false
)

/**
 * Thanh lịch tuần ngang dạng đảo (Dark Luxury Weekly Calendar Strip)
 * Tuân thủ quy tắc 9.3:
 * - Trạng thái đang chọn dùng Lavender Gradient (LavenderGradientStart -> LavenderGradientEnd)
 * - Chữ trên pill active dùng TextDeepInk
 * - Nền thanh dùng CharcoalSurface + viền CharcoalBorder
 */
@Composable
fun WeeklyCalendarStrip(
    selectedDateIso: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = remember(selectedDateIso) {
        generateWeekDays(selectedDateIso)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            DayPill(
                day = day,
                onClick = { onDateSelected(day.dateIso) }
            )
        }
    }
}

@Composable
private fun DayPill(
    day: DayItem,
    onClick: () -> Unit
) {
    val isSelected = day.isSelected

    val pillModifier = if (isSelected) {
        Modifier
            .width(42.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LavenderBrush)
            .border(0.75.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    } else {
        Modifier
            .width(42.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .clickable { onClick() }
    }

    Box(
        modifier = pillModifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfWeek,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextDeepInk else TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = day.dayOfMonth,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) TextDeepInk else TextWhite
            )
        }
    }
}

private fun generateWeekDays(selectedDateIso: String): List<DayItem> {
    val calendar = Calendar.getInstance()
    // Lùi về thứ 2 của tuần này
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    val dayNames = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())

    val list = mutableListOf<DayItem>()
    for (i in 0..6) {
        val currentIso = isoFormat.format(calendar.time)
        val dayNum = dayFormat.format(calendar.time)
        list.add(
            DayItem(
                dayOfWeek = dayNames[i],
                dayOfMonth = dayNum,
                dateIso = currentIso,
                isSelected = currentIso == selectedDateIso
            )
        )
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    return list
}

