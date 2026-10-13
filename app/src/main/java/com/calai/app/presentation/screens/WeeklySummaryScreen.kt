package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.WeeklySummaryDto
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.WeeklySummaryViewModel
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(MintSoft.copy(alpha = if (isDarkTheme) 0.14f else 0.30f), Color.Transparent)
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 28.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CheckinHeader(
                title = "Tổng kết tuần",
                subtitle = summary?.let { weekRangeLabel(it.weekStartDate, it.weekEndDate) }
                    ?: "Nhìn lại tuần vừa qua của bạn",
                onBack = onBack
            )

            when {
                uiState.isLoading -> LoadingSkeleton()

                summary == null -> MessageCard(
                    icon = Icons.Default.Refresh,
                    tint = RoseSoft,
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
                tint = MintSoft,
                title = "Tuần này chưa đủ dữ liệu",
                message = "Chưa có ngày nào ghi đủ bữa ăn trong tuần vừa qua. Ghi đủ các bữa mỗi ngày để có tổng kết chính xác nhé.",
                primaryText = "Đã hiểu",
                onPrimary = {}
            )
        }
        return
    }

    StaggerIn(0) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                modifier = Modifier.weight(1f),
                soft = PeachSoft, light = PeachLight,
                icon = Icons.Default.LocalFireDepartment,
                label = "Calo trung bình"
            ) { BigValue(summary.avgCalories?.roundToInt()?.toString() ?: "—", "kcal") }
            StatTile(
                modifier = Modifier.weight(1f),
                soft = SkySoft, light = SkyLight,
                icon = Icons.Default.MonitorWeight,
                label = "Cân nặng thay đổi"
            ) {
                val kg = summary.weightChangeKg
                BigValue(
                    when {
                        kg == null -> "—"
                        kg > 0 -> "+" + String.format("%.1f", kg)
                        kg < 0 -> "−" + String.format("%.1f", abs(kg))
                        else -> "0"
                    },
                    "kg"
                )
            }
        }
    }

    StaggerIn(80) {
        StatTile(
            modifier = Modifier.fillMaxWidth(),
            soft = MintSoft, light = MintLight,
            icon = Icons.Default.FitnessCenter,
            label = "Buổi tập đã hoàn thành"
        ) { BigValue((summary.workoutsCompleted ?: 0).toString(), "buổi") }
    }

    if (isPreview) {
        StaggerIn(160) { PremiumTeaser(onOpenPremium) }
    } else {
        StaggerIn(160) { MacroRow(summary) }
        if (summary.highlightText.isNotBlank()) StaggerIn(240) { HighlightCard(summary) }
        StaggerIn(320) {
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

@Composable
private fun MacroRow(summary: WeeklySummaryDto) {
    PastelCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Dinh dưỡng trung bình mỗi ngày")
            MacroLine("Đạm", MintSoft, summary.avgProtein)
            MacroLine("Tinh bột", ButterSoft, summary.avgCarb)
            MacroLine("Chất béo", RoseSoft, summary.avgFat)
        }
    }
}

@Composable
private fun MacroLine(label: String, color: Color, value: Float?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Text(
            "  $label",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            value?.let { "${it.roundToInt()} g" } ?: "—",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HighlightCard(summary: WeeklySummaryDto) {
    PastelCard(tint = LavenderSoft.copy(alpha = 0.14f)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(Icons.Default.AutoAwesome, LavenderSoft, LavenderLight)
                Text(
                    "Nhận xét từ NutriWise",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(summary.highlightText, fontSize = 13.5.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (summary.isFallback) {
                Text(
                    "Nhận xét mẫu vì AI tạm thời chưa khả dụng.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PremiumTeaser(onOpenPremium: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = ShadowCtaAmbient, spotColor = ShadowLightL3Spot)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(LavenderSoft, LavenderLight, RoseLight)))
            .clickable(onClick = onOpenPremium)
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Lock, null, tint = TextDeepInk, modifier = Modifier.size(24.dp)) }
            Text("Xem đầy đủ với Premium", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
            Text(
                "Đạm, tinh bột, chất béo trung bình và nhận xét cá nhân hoá từ NutriWise dành cho Premium.",
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                color = TextDeepInk.copy(alpha = 0.78f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Xem gói Premium", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = TextDeepInk, modifier = Modifier.padding(start = 6.dp).size(16.dp))
            }
        }
    }
}

/** Thẻ pastel trên Trang chủ mời xem tổng kết tuần vừa qua. */
@Composable
fun WeeklySummaryEntryCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = ShadowLightL2Ambient, spotColor = ShadowLightL2Spot)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(MintSoft, MintLight, SkyLight)))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Insights, null, tint = TextDeepInk, modifier = Modifier.size(22.dp)) }
        Column(modifier = Modifier.weight(1f)) {
            Text("Tổng kết tuần", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
            Text("Nhìn lại tuần vừa qua của bạn", fontSize = 12.5.sp, color = TextDeepInk.copy(alpha = 0.75f))
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = TextDeepInk, modifier = Modifier.size(20.dp))
    }
}
