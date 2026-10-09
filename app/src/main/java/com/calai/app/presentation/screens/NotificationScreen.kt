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
                            Text("Đọc tất cả", color = VividOrange, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                        TextButton(onClick = { viewModel.refresh() }) { Text("Thử lại", color = VividOrange) }
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
                                onClick = { if (!item.isRead) viewModel.markRead(item.id) },
                                onDelete = { viewModel.delete(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Icon theo loại thông báo — dùng duy nhất bộ Duotone custom của app (CODING_RULES 9.7/10.3),
 * tuyệt đối không Material icon mặc định/emoji. Tất cả đồng nhất tông cam thương hiệu. */
@Composable
private fun NotificationTypeIcon(type: String, modifier: Modifier = Modifier) {
    when (type) {
        "WATER_REMINDER" -> DuotoneWaterIcon(modifier = modifier, outlineColor = VividOrange, accentColor = VividOrange)
        "AI_MESSAGE" -> DuotoneSparkleIcon(modifier = modifier, outlineColor = VividOrange, accentColor = VividOrange)
        "MEAL_REMINDER" -> DuotoneDietIcon(modifier = modifier, outlineColor = VividOrange, accentColor = VividOrange)
        else -> DuotoneFlameIcon(modifier = modifier, outlineColor = VividOrange, accentColor = VividOrange)
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
                modifier = Modifier.size(40.dp).clip(CircleShape).background(VividOrange.copy(alpha = 0.12f)),
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
                Spacer(Modifier.height(4.dp))
                Text(formatRelativeTime(item.createdAt), fontSize = 11.sp, color = textSecondary.copy(alpha = 0.7f))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Xoá thông báo", tint = textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
