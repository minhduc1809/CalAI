package com.calai.app.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.*

/**
 * Wheel Picker chọn Ngày / Tháng / Năm sinh 3 cột cuộn dọc, snap mượt mà, hỗ trợ haptic feedback.
 */
@Composable
fun DateWheelPicker(
    selectedDay: Int,
    selectedMonth: Int,
    selectedYear: Int,
    onDateChange: (day: Int, month: Int, year: Int) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    minYear: Int = 1940,
    maxYear: Int = 2018
) {
    val daysInMonth = remember(selectedMonth, selectedYear) {
        getDaysInMonth(selectedMonth, selectedYear)
    }

    // Đảm bảo ngày không vượt quá số ngày của tháng khi đổi tháng
    LaunchedEffect(daysInMonth) {
        if (selectedDay > daysInMonth) {
            onDateChange(daysInMonth, selectedMonth, selectedYear)
        }
    }

    val days = remember(daysInMonth) { (1..daysInMonth).toList() }
    val months = remember { (1..12).toList() }
    val years = remember(minYear, maxYear) { (minYear..maxYear).toList() }

    val bgCard = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bgCard)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Vùng highlight ở giữa đại diện cho mục đang chọn
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cột Ngày
            WheelColumn(
                items = days,
                selectedIndex = days.indexOf(selectedDay.coerceIn(1, daysInMonth)).coerceAtLeast(0),
                onSelectIndex = { idx ->
                    onDateChange(days[idx], selectedMonth, selectedYear)
                },
                formatLabel = { "Ngày $it" },
                width = 90.dp,
                isDarkTheme = isDarkTheme
            )

            // Cột Tháng
            WheelColumn(
                items = months,
                selectedIndex = months.indexOf(selectedMonth).coerceAtLeast(0),
                onSelectIndex = { idx ->
                    val newMonth = months[idx]
                    val maxD = getDaysInMonth(newMonth, selectedYear)
                    onDateChange(selectedDay.coerceAtMost(maxD), newMonth, selectedYear)
                },
                formatLabel = { "Th $it" },
                width = 80.dp,
                isDarkTheme = isDarkTheme
            )

            // Cột Năm
            WheelColumn(
                items = years,
                selectedIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                onSelectIndex = { idx ->
                    val newYear = years[idx]
                    val maxD = getDaysInMonth(selectedMonth, newYear)
                    onDateChange(selectedDay.coerceAtMost(maxD), selectedMonth, newYear)
                },
                formatLabel = { "$it" },
                width = 85.dp,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun <T> WheelColumn(
    items: List<T>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    formatLabel: (T) -> String,
    width: Dp,
    isDarkTheme: Boolean
) {
    val itemHeight = 48.dp
    val visibleItemsCount = 3 // 1 item chính giữa + 2 item trên/dưới
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceAtLeast(0))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val view = LocalView.current

    // Cập nhật khi selectedIndex thay đổi từ bên ngoài
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != selectedIndex) {
            listState.scrollToItem(selectedIndex)
        }
    }

    // Khi cuộn xong dừng lại ở index nào thì emit index đó + haptic
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) selectedIndex
            else {
                val centerOffset = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                visible.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - centerOffset) }?.index ?: selectedIndex
            }
        }
    }

    LaunchedEffect(centerIndex) {
        if (centerIndex in items.indices && centerIndex != selectedIndex) {
            onSelectIndex(centerIndex)
            try {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            } catch (_: Exception) {}
        }
    }

    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = Modifier
            .width(width)
            .height(itemHeight * visibleItemsCount),
        contentPadding = PaddingValues(vertical = itemHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items.size) { index ->
            val isSelected = index == centerIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatLabel(items[index]),
                    fontSize = if (isSelected) 18.sp else 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) VividOrange else textMuted.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun getDaysInMonth(month: Int, year: Int): Int {
    return when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
