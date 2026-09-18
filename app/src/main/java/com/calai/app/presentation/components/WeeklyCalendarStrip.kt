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
    onClick: () -> Unit
) {
    val isSelected = day.isSelected

    // Scale animation: selected → 1f, unselected → 0.92f khi mới deselect
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "day_scale_${day.dateIso}"
    )

    val pillShadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    Box(
        modifier = Modifier
            .width(42.dp)
            .height(64.dp)
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(
                            elevation = if (isDarkTheme) 6.dp else 10.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = pillShadowColor,
                            spotColor = pillShadowColor
                        )
                } else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier.background(LavenderBrush) else Modifier
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        0.75.dp,
                        Color.White.copy(alpha = if (isDarkTheme) 0.45f else 0.70f),
                        RoundedCornerShape(16.dp)
                    )
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfWeek,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextDeepInk
                else if (day.isToday) VividOrange
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = day.dayOfMonth,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) TextDeepInk
                else if (day.isToday) VividOrange
                else MaterialTheme.colorScheme.onBackground
            )
            // Today dot indicator
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected -> TextDeepInk.copy(alpha = 0.5f)
                            day.isToday -> VividOrange
                            else -> Color.Transparent
                        }
                    )
            )
        }
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
