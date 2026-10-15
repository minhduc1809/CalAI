package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.R
import com.calai.app.data.remote.dto.NotificationDto
import com.calai.app.presentation.components.CuteLoadingIndicator
import com.calai.app.presentation.components.DuotoneCheckmarkIcon
import com.calai.app.presentation.components.DuotoneDumbbellIcon
import com.calai.app.presentation.components.DuotoneEnergyIcon
import com.calai.app.presentation.components.DuotoneCalendarIcon
import com.calai.app.presentation.components.DuotoneMenuListIcon
import com.calai.app.presentation.components.DuotoneDietIcon
import com.calai.app.presentation.components.DuotoneFlameIcon
import com.calai.app.presentation.components.DuotoneSparkleIcon
import com.calai.app.presentation.components.DuotoneWaterIcon
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Màn Thông báo — dữ liệu thật 100% từ GET/PATCH/DELETE /notifications (không mock).
 * Hỗ trợ: đánh dấu đã đọc (từng cái/tất cả), xoá, chấm báo chưa đọc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onOpenCheckin: () -> Unit = {},
    onOpenGoalSetup: () -> Unit = {},
    onOpenWeeklySummary: () -> Unit = {},
    onOpenPremium: () -> Unit = {},
    onLogWorkout: () -> Unit = {},
    isDarkTheme: Boolean = true,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val border = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Thông báo", fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = textPrimary)
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllRead() }) {
                            Text("Đọc tất cả", color = PrimaryStrong, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    CuteLoadingIndicator(
                        rawResId = R.raw.loading_general,
                        size = 96.dp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null && uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(40.dp))
                        Text(uiState.errorMessage ?: "", color = textSecondary, fontSize = 13.sp)
                        TextButton(onClick = { viewModel.refresh() }) { Text("Thử lại", color = PrimaryStrong) }
                    }
                }
                uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = textSecondary, modifier = Modifier.size(40.dp))
                        Text("Chưa có thông báo nào", color = textSecondary, fontSize = 14.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            NotificationRow(
                                item = item,
                                surface = surface,
                                border = border,
                                shadowColor = shadowColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                onClick = {
                                    if (!item.isRead) viewModel.markRead(item.id)
                                    // Thông báo Check-in sẵn sàng (BR-06.7) mở thẳng màn Check-in
                                    if (item.type == "CHECKIN_READY") onOpenCheckin()
                                    // Chạm cân đích: gợi ý chuyển sang Duy trì nằm ở màn thiết lập mục tiêu
                                    if (item.type == "GOAL_REACHED") onOpenGoalSetup()
                                    if (item.type == "WEEKLY_SUMMARY_READY") onOpenWeeklySummary()
                                    // Vấn đề thanh toán / thanh toán thành công: xem tình trạng gói Premium
                                    if (item.type == "WORKOUT_REMINDER") onLogWorkout()
                                    if (item.type == "BILLING_ISSUE" || item.type == "PAYMENT_SUCCESS") onOpenPremium()
                                },
                                onDelete = { viewModel.delete(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Kiểu hiển thị riêng của từng loại thông báo: màu nhấn, nhãn ngắn và biểu tượng Duotone của app. */
private data class NotifStyle(val accent: Color, val label: String)

private fun notifStyle(type: String): NotifStyle = when (type) {
    "CHECKIN_READY" -> NotifStyle(Color(0xFFE9745F), "Check-in tuần")
    "DAY_COMPLETED" -> NotifStyle(Color(0xFF2E9E6B), "Ngày ghi đủ")
    "BILLING_ISSUE" -> NotifStyle(Color(0xFFD64545), "Vấn đề thanh toán")
    "WEEKLY_SUMMARY_READY" -> NotifStyle(Color(0xFF8B6FD0), "Tổng kết tuần")
    "FOOD_SUBMISSION_RESULT" -> NotifStyle(Color(0xFF3F8FD9), "Kết quả duyệt món")
    "GOAL_REACHED" -> NotifStyle(Color(0xFFD9A441), "Đạt mục tiêu")
    "PAYMENT_SUCCESS" -> NotifStyle(Color(0xFF2FA58C), "Thanh toán thành công")
    "WORKOUT_REMINDER" -> NotifStyle(Color(0xFFE9745F), "Nhắc tập luyện")
    else -> NotifStyle(VividOrange, "Thông báo")
}

@Composable
private fun NotificationTypeIcon(type: String, modifier: Modifier = Modifier) {
    val accent = notifStyle(type).accent
    when (type) {
        "CHECKIN_READY" -> DuotoneSparkleIcon(modifier = modifier, outlineColor = accent, accentColor = accent)
        "DAY_COMPLETED", "PAYMENT_SUCCESS" -> DuotoneCheckmarkIcon(modifier = modifier, outlineColor = accent, accentColor = accent)
        "BILLING_ISSUE" -> DuotoneEnergyIcon(modifier = modifier, outlineColor = accent, accentColor = accent)
        "WEEKLY_SUMMARY_READY" -> DuotoneCalendarIcon(modifier = modifier, outlineColor = accent, accentColor = accent)
        "FOOD_SUBMISSION_RESULT" -> DuotoneMenuListIcon(modifier = modifier, outlineColor = accent, accentColor = accent)
        "GOAL_REACHED" -> DuotoneFlameIcon(modifier = modifier, outlineColor = accent, accentColor = accent)
        "WORKOUT_REMINDER" -> DuotoneDumbbellIcon(modifier = modifier, outlineColor = accent, accentColor = accent)
        else -> DuotoneSparkleIcon(modifier = modifier, outlineColor = accent, accentColor = accent)
    }
}

private fun formatRelativeTime(iso: String): String {
    val parsers = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    )
    val date = parsers.firstNotNullOfOrNull { p -> try { p.parse(iso) } catch (_: Exception) { null } } ?: return ""
    val diffMs = System.currentTimeMillis() - date.time
    val minutes = diffMs / 60000
    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        minutes < 24 * 60 -> "${minutes / 60} giờ trước"
        else -> "${minutes / (24 * 60)} ngày trước"
    }
}

@Composable
private fun NotificationRow(
    item: NotificationDto,
    surface: Color,
    border: Color,
    shadowColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = cardShape, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(cardShape)
            .background(surface)
            .border(width = 1.dp, color = border, shape = cardShape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(notifStyle(item.type).accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                NotificationTypeIcon(type = item.type, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!item.isRead) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(VividOrange))
                    }
                    Text(
                        item.title, fontSize = 14.sp,
                        fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = textPrimary, modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(item.message, fontSize = 12.5.sp, color = textSecondary, lineHeight = 17.sp)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val st = notifStyle(item.type)
                    Text(
                        st.label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = st.accent,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(st.accent.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    Text(formatRelativeTime(item.createdAt), fontSize = 11.sp, color = textSecondary.copy(alpha = 0.7f))
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Xoá thông báo", tint = textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
