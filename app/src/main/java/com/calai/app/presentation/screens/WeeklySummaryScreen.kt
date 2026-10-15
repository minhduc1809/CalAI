package com.calai.app.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.WeeklySummaryDto
import com.calai.app.presentation.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

/** "05/10 – 11/10" từ hai chuỗi ISO (lấy 10 ký tự đầu). */
private fun weekRangeLabel(start: String?, end: String?): String? {
    fun short(iso: String?): String? {
        if (iso == null || iso.length < 10) return null
        val p = iso.substring(0, 10).split("-")
        return if (p.size == 3) "${p[2]}/${p[1]}" else null
    }
    val a = short(start)
    val b = short(end)
    return if (a != null && b != null) "$a – $b" else null
}

// Màu dữ liệu thống nhất toàn app (calo cam đào, đạm tím xanh, tinh bột vàng ấm, béo xanh ngọc)
private val ProteinColor = Color(0xFF4FBFA5)
private val CarbColor = Color(0xFFF2C265)
private val FatColor = Color(0xFF8F9CF2)

/** Nền peach của mảng hero: sáng = peach nhạt, tối = peach tối ấm; luôn đọc được chữ chính. */
@Composable
internal fun heroBrush(): Brush =
    if (NwTheme.isDark) Brush.linearGradient(listOf(PeachDarkTint, Color(0xFF1B1210)))
    else Brush.linearGradient(listOf(PeachSoft, PeachMist))

/**
 * Màn Tổng kết tuần (BR-11.7). Kỳ dữ liệu trùng với Check-in; chỉ ngày ghi đủ vào trung bình. Free xem calo, cân nặng
 * và số buổi tập; macro và lời nhận xét do AI viết dành cho Premium.
 */
@Composable
fun WeeklySummaryScreen(
    onBack: () -> Unit,
    onOpenPremium: () -> Unit = {},
    isDarkTheme: Boolean = true,
    viewModel: com.calai.app.presentation.viewmodel.WeeklySummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CheckinHeader(
                title = "Tổng kết tuần",
                subtitle = summary?.let { weekRangeLabel(it.weekStartDate, it.weekEndDate) }
                    ?: "Tuần vừa qua",
                onBack = onBack
            )

            when {
                uiState.isLoading -> LoadingSkeleton()

                summary == null -> MessageCard(
                    icon = Icons.Default.Refresh,
                    tint = PeachSoft,
                    title = "Chưa tải được tổng kết",
                    message = uiState.errorMessage ?: "Vui lòng thử lại.",
                    primaryText = "Thử lại",
                    onPrimary = viewModel::load
                )

                else -> SummaryContent(
                    summary = summary,
                    isRefreshing = uiState.isRefreshing,
                    refreshError = uiState.refreshError,
                    onRefresh = viewModel::refresh,
                    onOpenPremium = onOpenPremium
                )
            }
        }
    }
}

@Composable
private fun SummaryContent(
    summary: WeeklySummaryDto,
    isRefreshing: Boolean,
    refreshError: String?,
    onRefresh: () -> Unit,
    onOpenPremium: () -> Unit
) {
    val hasData = summary.avgCalories != null
    val isPreview = summary.isPreview == true

    if (!hasData) {
        StaggerIn(0) {
            MessageCard(
                icon = Icons.Default.Insights,
                tint = PeachSoft,
                title = "Tuần này chưa đủ dữ liệu",
                message = "Chưa có ngày nào ghi đủ bữa ăn trong tuần vừa qua. Ghi đủ các bữa mỗi ngày để có tổng kết chính xác nhé.",
                primaryText = "Đã hiểu",
                onPrimary = {}
            )
        }
        return
    }

    StaggerIn(0) { HeroCard(summary) }

    if (isPreview) {
        StaggerIn(120) { PremiumTeaser(onOpenPremium) }
    } else {
        StaggerIn(120) { MacroRibbon(summary) }
        if (summary.highlightText.isNotBlank()) StaggerIn(200) { HighlightCard(summary) }
        StaggerIn(280) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Text(if (isRefreshing) "  Đang làm mới..." else "  Làm mới tổng kết", fontWeight = FontWeight.SemiBold)
                }
                refreshError?.let { ErrorLine(it) }
            }
        }
    }
}

/** Mảng peach lớn: số calo trung bình là nhân vật chính; cân nặng và buổi tập là hai chỉ số phụ bên dưới. */
@Composable
private fun HeroCard(summary: WeeklySummaryDto) {
    val ink = MaterialTheme.colorScheme.onBackground
    val inkSoft = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(heroBrush())
    ) {
        // Đường cong hữu cơ làm điểm nhấn, không mang dữ liệu
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = VividOrangeLight.copy(alpha = if (NwTheme.isDark) 0.16f else 0.30f),
                radius = size.width * 0.38f,
                center = Offset(size.width * 0.96f, size.height * 0.02f)
            )
            drawCircle(
                color = VividOrangeLight.copy(alpha = if (NwTheme.isDark) 0.10f else 0.18f),
                radius = size.width * 0.22f,
                center = Offset(size.width * 1.02f, size.height * 0.46f)
            )
        }
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column {
                Text("Calo trung bình mỗi ngày", fontSize = 13.sp, color = inkSoft, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        summary.avgCalories?.roundToInt()?.toString() ?: "—",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = ink,
                        letterSpacing = (-1.5).sp
                    )
                    Text("  kcal", fontSize = 16.sp, color = inkSoft, modifier = Modifier.padding(bottom = 10.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MiniStat(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.MonitorWeight,
                    label = "Cân nặng",
                    value = summary.weightChangeKg.let { kg ->
                        when {
                            kg == null -> "—"
                            kg > 0 -> "+" + String.format("%.1f", kg) + " kg"
                            kg < 0 -> "−" + String.format("%.1f", abs(kg)) + " kg"
                            else -> "0 kg"
                        }
                    }
                )
                MiniStat(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.FitnessCenter,
                    label = "Buổi tập",
                    value = "${summary.workoutsCompleted ?: 0} buổi"
                )
            }
        }
    }
}

@Composable
private fun MiniStat(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(VividOrangeSoft),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = PrimaryStrong, modifier = Modifier.size(18.dp)) }
        Column {
            Text(label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Dải macro: độ dài từng đoạn tỉ lệ với số gram trung bình; bên dưới là số cụ thể (không chỉ dựa vào màu). */
@Composable
private fun MacroRibbon(summary: WeeklySummaryDto) {
    val p = summary.avgProtein ?: 0f
    val c = summary.avgCarb ?: 0f
    val f = summary.avgFat ?: 0f
    val total = (p + c + f).takeIf { it > 0f } ?: 1f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Dinh dưỡng mỗi ngày", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Row(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (p > 0f) Box(Modifier.weight(p / total).fillMaxSize().background(ProteinColor))
            if (c > 0f) Box(Modifier.weight(c / total).fillMaxSize().background(CarbColor))
            if (f > 0f) Box(Modifier.weight(f / total).fillMaxSize().background(FatColor))
            if (p + c + f <= 0f) Box(Modifier.weight(1f).fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MacroLegend("Đạm", p, ProteinColor)
            MacroLegend("Tinh bột", c, CarbColor)
            MacroLegend("Chất béo", f, FatColor)
        }
    }
}

@Composable
private fun MacroLegend(label: String, grams: Float, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "${grams.roundToInt()} g",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun HighlightCard(summary: WeeklySummaryDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
    ) {
        Box(Modifier.width(5.dp).fillMaxSize().background(VividOrange))
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, null, tint = PrimaryStrong, modifier = Modifier.size(18.dp))
                Text("Nhận xét từ NutriWise", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(summary.highlightText, fontSize = 14.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurface)
            if (summary.isFallback) {
                Text("Nhận xét mẫu vì AI tạm thời chưa khả dụng.", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PremiumTeaser(onOpenPremium: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(heroBrush())
            .border(1.dp, VividOrange.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .clickable(onClick = onOpenPremium)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(VividOrange),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Lock, null, tint = OnPrimary, modifier = Modifier.size(22.dp)) }
        Column(modifier = Modifier.weight(1f)) {
            Text("Xem đầy đủ với Premium", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text("Đạm, tinh bột, chất béo và nhận xét cá nhân hoá", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}

/** Thẻ trên Trang chủ mời xem tổng kết tuần vừa qua. Tông peach, chữ tương phản cao ở cả sáng và tối. */
@Composable
fun WeeklySummaryEntryCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(heroBrush())
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(VividOrange),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Insights, null, tint = OnPrimary, modifier = Modifier.size(22.dp)) }
        Column(modifier = Modifier.weight(1f)) {
            Text("Tổng kết tuần", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text("Nhìn lại tuần vừa qua", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}
