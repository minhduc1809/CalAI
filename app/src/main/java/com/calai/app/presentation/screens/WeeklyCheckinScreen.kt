package com.calai.app.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.CheckInCoachingDto
import com.calai.app.data.remote.dto.CheckInDto
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.CheckinOutcome
import com.calai.app.presentation.viewmodel.CheckinPhase
import com.calai.app.presentation.viewmodel.CheckinViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

// ── Bảng màu pastel của màn Check-in (dùng chung gradient của Bento để đồng bộ với Trang chủ) ──
internal val PeachSoft = CalorieCardGradientStart
internal val PeachLight = CalorieCardGradientEnd
internal val MintSoft = ProteinGradientStart
internal val MintLight = ProteinGradientEnd
internal val ButterSoft = CarbGradientStart
private val ButterLight = CarbGradientEnd
internal val RoseSoft = FatGradientStart
internal val RoseLight = FatGradientEnd
internal val LavenderSoft = LavenderGradientStart
internal val LavenderLight = LavenderGradientEnd
internal val SkySoft = Color(0xFF9CC7F5)
internal val SkyLight = Color(0xFFCBE3FB)

private val CardShape = RoundedCornerShape(26.dp)
private val TileShape = RoundedCornerShape(20.dp)

private val MOODS = listOf(
    Triple("GREAT", "😄", "Rất tốt"),
    Triple("GOOD", "🙂", "Tốt"),
    Triple("OKAY", "😐", "Bình thường"),
    Triple("BAD", "🙁", "Không tốt"),
    Triple("TERRIBLE", "😞", "Tệ")
)

@Composable
fun WeeklyCheckinScreen(
    onBack: () -> Unit,
    onOpenPremium: () -> Unit = {},
    onOpenGoalSetup: () -> Unit = {},
    isDarkTheme: Boolean = true,
    viewModel: CheckinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val phase = uiState.phase

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Quầng sáng pastel mờ phía trên cho cảm giác nhẹ nhàng
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            LavenderSoft.copy(alpha = if (isDarkTheme) 0.16f else 0.34f),
                            Color.Transparent
                        )
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
                title = "Check-in tuần",
                subtitle = headerSubtitle(phase),
                onBack = onBack
            )

            when (phase) {
                CheckinPhase.Loading -> LoadingSkeleton()

                is CheckinPhase.Error -> MessageCard(
                    icon = Icons.Default.Refresh,
                    tint = RoseSoft,
                    title = "Chưa tải được Check-in",
                    message = phase.message,
                    primaryText = "Thử lại",
                    onPrimary = { viewModel.load() }
                )

                CheckinPhase.NeedProfile -> MessageCard(
                    icon = Icons.Default.Flag,
                    tint = ButterSoft,
                    title = "Hoàn tất hồ sơ để bắt đầu",
                    message = "NutriWise cần chiều cao, cân nặng và mục tiêu của bạn để tính mục tiêu dinh dưỡng và đề xuất Check-in hằng tuần.",
                    primaryText = "Thiết lập mục tiêu",
                    onPrimary = onOpenGoalSetup
                )

                is CheckinPhase.QuotaExceeded -> PremiumUpsellCard(
                    message = phase.message,
                    onOpenPremium = onOpenPremium,
                    onBack = onBack
                )

                is CheckinPhase.Proposal -> {
                    val checkIn = phase.checkIn
                    StaggerIn(0) { ProposalHero(checkIn) }
                    StaggerIn(80) { WeekStats(checkIn) }
                    if (hasMacros(checkIn)) StaggerIn(160) { MacroCompare(checkIn) }
                    phase.coaching?.let { StaggerIn(240) { CoachingCard(it) } }
                    StaggerIn(320) {
                        MoodPicker(selected = uiState.mood, onSelect = viewModel::selectMood)
                    }
                    StaggerIn(400) {
                        ProposalActions(
                            checkIn = checkIn,
                            isSubmitting = uiState.isSubmitting,
                            errorMessage = uiState.actionError,
                            onAccept = viewModel::accept,
                            onDecline = viewModel::decline,
                            onSnooze = viewModel::snooze
                        )
                    }
                }

                is CheckinPhase.TooEarly -> StaggerIn(0) {
                    TooEarlyCard(firstDate = phase.firstDate, onBack = onBack)
                }

                is CheckinPhase.NeedMoreData -> {
                    StaggerIn(0) { NeedMoreDataHero(phase.checkIn) }
                    phase.coaching?.let { StaggerIn(100) { CoachingCard(it) } }
                    StaggerIn(180) { NeedMoreDataChecklist(phase.coaching?.todo.orEmpty()) }
                    uiState.nextCheckinDate?.let { next -> StaggerIn(220) { NextCheckinNote(next) } }
                    StaggerIn(260) {
                        AppButton(
                            text = "Đã hiểu",
                            onClick = viewModel::acknowledge,
                            enabled = !uiState.isSubmitting,
                            isLoading = uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    uiState.actionError?.let { ErrorLine(it) }
                }

                is CheckinPhase.Done -> DoneCard(
                    outcome = phase.outcome,
                    message = phase.message,
                    nextCheckinDate = uiState.nextCheckinDate,
                    onBack = onBack
                )
            }

            if (uiState.history.isNotEmpty() && phase != CheckinPhase.Loading) {
                HistorySection(uiState.history)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// KHUNG CHUNG
// ═══════════════════════════════════════════════════════════════════════════

@Composable
internal fun CheckinHeader(title: String, subtitle: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(6.dp, CircleShape, ambientColor = ShadowLightL1Ambient, spotColor = ShadowLightL1Spot)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun headerSubtitle(phase: CheckinPhase): String? {
    val checkIn = when (phase) {
        is CheckinPhase.Proposal -> phase.checkIn
        is CheckinPhase.NeedMoreData -> phase.checkIn
        is CheckinPhase.Done -> phase.checkIn
        else -> null
    } ?: return "Nhìn lại tuần vừa qua cùng NutriWise"
    return weekLabel(checkIn)
}

/** "05/10 – 11/10". Chỉ hiện khoảng ngày; số tuần không hiện vì các Check-in cũ mang số tuần không có ý nghĩa. */
private fun weekLabel(c: CheckInDto): String {
    val start = shortDate(c.weekStartDate)
    val end = shortDate(c.weekEndDate)
    return if (start != null && end != null) "Tuần $start – $end" else "Tuần vừa qua"
}

private fun shortDate(iso: String?): String? {
    if (iso == null || iso.length < 10) return null
    val parts = iso.substring(0, 10).split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}" else null
}

/** Cho mỗi khối xuất hiện lần lượt (mờ dần + trượt nhẹ lên) tạo cảm giác mượt khi vào màn. */
@Composable
internal fun StaggerIn(delayMs: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMs.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(420)) + slideInVertically(tween(420, easing = FastOutSlowInEasing)) { it / 8 }
    ) {
        content()
    }
}

@Composable
internal fun LoadingSkeleton() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        listOf(200.dp, 120.dp, 120.dp, 150.dp).forEach { h ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(h)
                    .alpha(pulse)
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CardShape)
            )
        }
    }
}

@Composable
internal fun PastelCard(
    modifier: Modifier = Modifier,
    tint: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, ambientColor = ShadowLightL2Ambient, spotColor = ShadowLightL2Spot)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .background(tint)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CardShape)
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
internal fun IconBadge(icon: ImageVector, soft: Color, light: Color, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(Brush.linearGradient(listOf(soft, light))),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = TextDeepInk, modifier = Modifier.size(size / 2))
    }
}

@Composable
private fun Pill(text: String, background: Color, textColor: Color = TextDeepInk) {
    Surface(color = background, shape = RoundedCornerShape(50)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
internal fun ErrorLine(message: String) {
    Text(message, color = CrimsonError, fontSize = 12.5.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}

// ═══════════════════════════════════════════════════════════════════════════
// ĐỀ XUẤT
// ═══════════════════════════════════════════════════════════════════════════

private fun reasonLabel(reason: String?): String = when (reason) {
    "EXPENDITURE_CHANGE" -> "Năng lượng tiêu hao thay đổi"
    "SMOOTHING" -> "Điều chỉnh từ từ cho an toàn"
    "GOAL_CHANGE" -> "Mục tiêu của bạn đã đổi"
    else -> "Mục tiêu đang phù hợp"
}

@Composable
private fun ProposalHero(c: CheckInDto) {
    val delta = c.proposedCalorieTarget - c.currentCalorieTarget
    // Số đếm từ mục tiêu hiện tại tới mục tiêu đề xuất khi màn hình vừa hiện
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val shown by animateIntAsState(
        targetValue = if (started) c.proposedCalorieTarget else c.currentCalorieTarget,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "hero_calories"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, CardShape, ambientColor = ShadowCtaAmbient, spotColor = ShadowLightL3Spot)
            .clip(CardShape)
            .background(Brush.linearGradient(listOf(LavenderSoft, LavenderLight, PeachLight)))
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, null, tint = TextDeepInk, modifier = Modifier.size(18.dp))
                Text(
                    if (delta == 0) "Mục tiêu tuần này" else "Đề xuất cho tuần này",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDeepInk.copy(alpha = 0.75f)
                )
            }

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "$shown",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDeepInk,
                    lineHeight = 56.sp
                )
                Text(
                    "kcal / ngày",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDeepInk.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Pill(
                    text = when {
                        delta > 0 -> "+$delta kcal"
                        delta < 0 -> "−${abs(delta)} kcal"
                        else -> "Giữ nguyên"
                    },
                    background = Color.White.copy(alpha = 0.7f)
                )
                if (delta != 0) {
                    Text(
                        "so với ${c.currentCalorieTarget} kcal hiện tại",
                        fontSize = 12.5.sp,
                        color = TextDeepInk.copy(alpha = 0.7f)
                    )
                }
            }

            Pill(text = reasonLabel(c.adjustmentReason), background = Color.White.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun WeekStats(c: CheckInDto) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Tuần vừa qua của bạn")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                modifier = Modifier.weight(1f),
                soft = PeachSoft, light = PeachLight,
                icon = Icons.Default.LocalFireDepartment,
                label = "Calo trung bình"
            ) {
                BigValue(c.avgDailyCalories?.roundToInt()?.toString() ?: "—", "kcal")
            }
            StatTile(
                modifier = Modifier.weight(1f),
                soft = MintSoft, light = MintLight,
                icon = Icons.Default.Verified,
                label = "Ngày đúng mục tiêu"
            ) {
                ComplianceRing(c.compliancePct)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                modifier = Modifier.weight(1f),
                soft = SkySoft, light = SkyLight,
                icon = Icons.Default.MonitorWeight,
                label = "Cân nặng xu hướng"
            ) {
                BigValue(c.weightAtCheckin?.let { String.format("%.1f", it) } ?: "—", "kg")
            }
            StatTile(
                modifier = Modifier.weight(1f),
                soft = ButterSoft, light = ButterLight,
                icon = Icons.Default.Flag,
                label = "Tiến độ mục tiêu"
            ) {
                GoalProgress(c.goalProgressPct)
            }
        }
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
internal fun StatTile(
    modifier: Modifier,
    soft: Color,
    light: Color,
    icon: ImageVector,
    label: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(132.dp)
            .shadow(6.dp, TileShape, ambientColor = ShadowLightL1Ambient, spotColor = ShadowLightL1Spot)
            .clip(TileShape)
            .background(Brush.linearGradient(listOf(light.copy(alpha = 0.95f), soft.copy(alpha = 0.55f))))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = TextDeepInk.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextDeepInk.copy(alpha = 0.75f), maxLines = 1)
            }
            content()
        }
    }
}

@Composable
internal fun BigValue(value: String, unit: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
        Text(unit, fontSize = 12.sp, color = TextDeepInk.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 5.dp))
    }
}

/** Vòng tròn tỉ lệ ngày đạt mục tiêu; chưa có dữ liệu thì hiện dấu gạch thay vì 0% gây hiểu nhầm. */
@Composable
private fun ComplianceRing(pct: Double?) {
    val target = ((pct ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
    val progress by animateFloatAsState(target, tween(1000, easing = FastOutSlowInEasing), label = "ring")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(58.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 9.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = Color.White.copy(alpha = 0.55f),
                    startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                if (pct != null) {
                    drawArc(
                        color = Color(0xFF1FA67A),
                        startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                        topLeft = Offset(inset, inset), size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Text(
                pct?.let { "${it.roundToInt()}%" } ?: "—",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDeepInk
            )
        }
    }
}

@Composable
private fun GoalProgress(pct: Double?) {
    val target = ((pct ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
    val progress by animateFloatAsState(target, tween(1000, easing = FastOutSlowInEasing), label = "goal")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            pct?.let { "${it.roundToInt()}%" } ?: "—",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDeepInk
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = Color(0xFFE59A1F),
            trackColor = Color.White.copy(alpha = 0.6f),
            strokeCap = StrokeCap.Round
        )
    }
}

private fun hasMacros(c: CheckInDto) =
    c.proposedProteinTarget != null && c.proposedCarbTarget != null && c.proposedFatTarget != null

@Composable
private fun MacroCompare(c: CheckInDto) {
    PastelCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(Icons.Default.Restaurant, MintSoft, MintLight)
                Column {
                    Text("Dinh dưỡng mỗi ngày", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Đạm, tinh bột, béo theo mục tiêu mới", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            MacroRow("Đạm", MintSoft, c.currentProteinTarget, c.proposedProteinTarget)
            MacroRow("Tinh bột", ButterSoft, c.currentCarbTarget, c.proposedCarbTarget)
            MacroRow("Chất béo", RoseSoft, c.currentFatTarget, c.proposedFatTarget)
        }
    }
}

@Composable
private fun MacroRow(label: String, color: Color, current: Double?, proposed: Double?) {
    val now = current?.roundToInt()
    val next = proposed?.roundToInt()
    val delta = if (now != null && next != null) next - now else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        if (now != null && delta != 0) {
            Text("$now g", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp).size(14.dp))
        }
        Text(
            if (next != null) "$next g" else "—",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CoachingCard(c: CheckInCoachingDto) {
    PastelCard(tint = ButterSoft.copy(alpha = 0.12f)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(Icons.Default.Lightbulb, ButterSoft, ButterLight)
                Text(
                    c.title ?: "Lời nhắn từ NutriWise",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            c.explanation?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 13.5.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            c.complianceMessage?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 13.5.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun MoodPicker(selected: String?, onSelect: (String) -> Unit) {
    PastelCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column {
                Text("Tuần qua bạn thấy thế nào?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Không bắt buộc, chỉ để bạn nhìn lại cảm xúc của mình", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MOODS.forEach { (key, emoji, label) ->
                    val isSelected = selected == key
                    val scale by animateFloatAsState(if (isSelected) 1.12f else 1f, tween(180), label = "mood_scale")
                    Column(
                        modifier = Modifier
                            .width(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) LavenderSoft.copy(alpha = 0.45f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) LavenderSoft else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { onSelect(key) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(emoji, fontSize = (24 * scale).sp)
                        Text(
                            label,
                            fontSize = 9.5.sp,
                            maxLines = 1,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProposalActions(
    checkIn: CheckInDto,
    isSubmitting: Boolean,
    errorMessage: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onSnooze: () -> Unit
) {
    val unchanged = checkIn.proposedCalorieTarget == checkIn.currentCalorieTarget
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppButton(
            text = if (unchanged) "Giữ mục tiêu ${checkIn.currentCalorieTarget} kcal" else "Áp dụng ${checkIn.proposedCalorieTarget} kcal",
            onClick = onAccept,
            enabled = !isSubmitting,
            isLoading = isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )
        if (!unchanged) {
            OutlinedButton(
                onClick = onDecline,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Text("Giữ ${checkIn.currentCalorieTarget} kcal hiện tại", fontWeight = FontWeight.SemiBold)
            }
        }
        TextButton(onClick = onSnooze, enabled = !isSubmitting, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Text("Để sau, nhắc mình lại sau 24 giờ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        errorMessage?.let { ErrorLine(it) }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// THIẾU DỮ LIỆU
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun NeedMoreDataHero(c: CheckInDto) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, CardShape, ambientColor = ShadowLightL2Ambient, spotColor = ShadowLightL3Spot)
            .clip(CardShape)
            .background(Brush.linearGradient(listOf(SkySoft, SkyLight, MintLight)))
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.EventRepeat, null, tint = TextDeepInk, modifier = Modifier.size(30.dp))
            Text("Cần thêm một chút dữ liệu", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
            Text(
                "Mục tiêu hiện tại ${c.currentCalorieTarget} kcal vẫn được giữ nguyên. Khi đủ dữ liệu, NutriWise sẽ đề xuất điều chỉnh cho bạn.",
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                color = TextDeepInk.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun NeedMoreDataChecklist(steps: List<String>) {
    PastelCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionTitle("Việc nhỏ để Check-in sau chính xác hơn")
            if (steps.isNotEmpty()) {
                // Việc cần làm cụ thể do backend tính từ dữ liệu thật của bạn
                val palette = listOf(
                    Triple(Icons.Default.Scale, SkySoft, SkyLight),
                    Triple(Icons.Default.Restaurant, MintSoft, MintLight),
                    Triple(Icons.Default.CalendarMonth, LavenderSoft, LavenderLight)
                )
                steps.forEachIndexed { i, step ->
                    val (icon, soft, light) = palette[i % palette.size]
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconBadge(icon, soft, light, size = 40.dp)
                        Text(
                            step,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                ChecklistItem(Icons.Default.Restaurant, MintSoft, MintLight, "Ghi đủ các bữa ăn", "Ghi lại bữa sáng, trưa, tối và bữa phụ mỗi ngày")
                ChecklistItem(Icons.Default.Scale, SkySoft, SkyLight, "Cân đều đặn", "Cân vài lần mỗi tuần, vào cùng một khung giờ")
                ChecklistItem(Icons.Default.CalendarMonth, LavenderSoft, LavenderLight, "Kiên trì vài ngày", "Chỉ cần vài ngày ghi đủ là hệ thống bắt đầu học được")
            }
        }
    }
}

@Composable
private fun ChecklistItem(icon: ImageVector, soft: Color, light: Color, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconBadge(icon, soft, light, size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// KẾT QUẢ / TRẠNG THÁI KHÁC
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun DoneCard(outcome: CheckinOutcome, message: String, nextCheckinDate: String?, onBack: () -> Unit) {
    val (soft, light, icon, title) = when (outcome) {
        CheckinOutcome.ACCEPTED -> DoneStyle(MintSoft, MintLight, Icons.Default.CheckCircle, "Đã cập nhật mục tiêu")
        CheckinOutcome.DECLINED -> DoneStyle(LavenderSoft, LavenderLight, Icons.Default.Check, "Đã giữ mục tiêu hiện tại")
        CheckinOutcome.SNOOZED -> DoneStyle(SkySoft, SkyLight, Icons.Default.Schedule, "Hẹn bạn sau nhé")
        CheckinOutcome.ACKNOWLEDGED -> DoneStyle(MintSoft, MintLight, Icons.Default.CheckCircle, "Đã ghi nhận")
        CheckinOutcome.EXPIRED -> DoneStyle(ButterSoft, ButterLight, Icons.Default.Lightbulb, "Đề xuất không còn phù hợp")
        CheckinOutcome.ALREADY_HANDLED -> DoneStyle(LavenderSoft, LavenderLight, Icons.Default.CheckCircle, "Bạn đã xong Check-in này")
    }
    StaggerIn(0) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, CardShape, ambientColor = ShadowLightL2Ambient, spotColor = ShadowLightL3Spot)
                    .clip(CardShape)
                    .background(Brush.linearGradient(listOf(soft, light)))
                    .padding(horizontal = 22.dp, vertical = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = TextDeepInk, modifier = Modifier.size(38.dp))
                    }
                    Text(title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk, textAlign = TextAlign.Center)
                    Text(message, fontSize = 13.5.sp, lineHeight = 20.sp, color = TextDeepInk.copy(alpha = 0.78f), textAlign = TextAlign.Center)
                }
            }
            // Đề xuất hết hạn hoặc đã hoãn thì không hứa "Check-in tiếp theo" như thể đã xong việc
            if (nextCheckinDate != null && outcome != CheckinOutcome.SNOOZED) NextCheckinNote(nextCheckinDate)
            AppButton(text = "Xong", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

private data class DoneStyle(val soft: Color, val light: Color, val icon: ImageVector, val title: String)

@Composable
internal fun MessageCard(
    icon: ImageVector,
    tint: Color,
    title: String,
    message: String,
    primaryText: String,
    onPrimary: () -> Unit
) {
    PastelCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(tint.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
            }
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Text(message, fontSize = 13.5.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            AppButton(text = primaryText, onClick = onPrimary, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PremiumUpsellCard(message: String, onOpenPremium: () -> Unit, onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, CardShape, ambientColor = ShadowCtaAmbient, spotColor = ShadowLightL3Spot)
                .clip(CardShape)
                .background(Brush.linearGradient(listOf(LavenderSoft, LavenderLight, RoseLight)))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = TextDeepInk, modifier = Modifier.size(26.dp))
                }
                Text("Bạn đã dùng hết Check-in của tháng này", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
                Text(message, fontSize = 13.5.sp, lineHeight = 20.sp, color = TextDeepInk.copy(alpha = 0.78f))
                Text(
                    "Premium có Check-in mỗi tuần, giúp mục tiêu luôn bám sát cơ thể của bạn.",
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDeepInk
                )
            }
        }
        AppButton(text = "Xem gói Premium", onClick = onOpenPremium, modifier = Modifier.fillMaxWidth())
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Để sau", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** "Thứ Hai, 19/10" từ khoá ngày YYYY-MM-DD; null nếu không đọc được. */
private fun formatCheckinDate(key: String?): String? {
    if (key == null || key.length < 10) return null
    val parts = key.substring(0, 10).split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    val cal = java.util.Calendar.getInstance().apply { clear(); set(year, month - 1, day) }
    val name = when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> "Thứ Hai"
        java.util.Calendar.TUESDAY -> "Thứ Ba"
        java.util.Calendar.WEDNESDAY -> "Thứ Tư"
        java.util.Calendar.THURSDAY -> "Thứ Năm"
        java.util.Calendar.FRIDAY -> "Thứ Sáu"
        java.util.Calendar.SATURDAY -> "Thứ Bảy"
        else -> "Chủ Nhật"
    }
    return "$name, ${parts[2]}/${parts[1]}"
}

/** Dòng nhỏ báo ngày Check-in kế tiếp, kèm gợi ý đổi ngày trong Cài đặt. */
@Composable
private fun NextCheckinNote(nextCheckinDate: String) {
    val label = formatCheckinDate(nextCheckinDate) ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(LavenderSoft.copy(alpha = 0.18f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        Column {
            Text("Check-in tiếp theo: $label", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("Bạn có thể đổi ngày trong Cài đặt", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Tài khoản mới: chưa có đủ một tuần trọn vẹn, nói rõ khi nào có Check-in đầu tiên. */
@Composable
private fun TooEarlyCard(firstDate: String?, onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, CardShape, ambientColor = ShadowLightL2Ambient, spotColor = ShadowLightL3Spot)
                .clip(CardShape)
                .background(Brush.linearGradient(listOf(MintSoft, SkyLight, LavenderLight)))
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.CalendarMonth, null, tint = TextDeepInk, modifier = Modifier.size(30.dp))
                Text("Check-in đầu tiên sắp tới rồi", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
                Text(
                    "Mỗi Check-in nhìn lại 7 ngày trọn vẹn của bạn, nên cần đủ một tuần kể từ khi bắt đầu dùng NutriWise.",
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = TextDeepInk.copy(alpha = 0.78f)
                )
                formatCheckinDate(firstDate)?.let {
                    Pill(text = "Check-in đầu tiên: $it", background = Color.White.copy(alpha = 0.7f))
                }
            }
        }
        PastelCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionTitle("Trong lúc chờ, bạn có thể")
                ChecklistItem(Icons.Default.Restaurant, MintSoft, MintLight, "Ghi bữa ăn mỗi ngày", "Càng đủ bữa, đề xuất càng sát với bạn")
                ChecklistItem(Icons.Default.Scale, SkySoft, SkyLight, "Cân vài lần mỗi tuần", "Cân vào cùng một khung giờ cho số liệu ổn định")
            }
        }
        firstDate?.let { NextCheckinNote(it) }
        AppButton(text = "Đã hiểu", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// LỊCH SỬ
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun HistorySection(history: List<CheckInDto>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Các tuần trước")
        history.forEach { HistoryRow(it) }
    }
}

@Composable
private fun HistoryRow(c: CheckInDto) {
    val (label, color) = when (c.status) {
        "ACCEPTED" -> "Đã áp dụng" to MintSoft
        "DECLINED" -> "Giữ nguyên" to LavenderSoft
        "ACKNOWLEDGED" -> "Thiếu dữ liệu" to SkySoft
        else -> "Hết hạn" to ButterSoft
    }
    val delta = c.proposedCalorieTarget - c.currentCalorieTarget
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(weekLabel(c), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            val detail = when {
                c.status == "ACCEPTED" && delta != 0 -> "${c.currentCalorieTarget} → ${c.proposedCalorieTarget} kcal"
                c.status == "ACCEPTED" -> "Giữ ${c.currentCalorieTarget} kcal"
                else -> "${c.currentCalorieTarget} kcal"
            }
            Text(detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Pill(label, color.copy(alpha = 0.45f), MaterialTheme.colorScheme.onSurface)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// THẺ NHẮC TRÊN TRANG CHỦ
// ═══════════════════════════════════════════════════════════════════════════

/** Thẻ pastel mời người dùng mở Check-in khi có Check-in đang chờ. */
@Composable
fun CheckinEntryCard(checkIn: CheckInDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val needsData = checkIn.status == "INSUFFICIENT_DATA"
    val unchanged = checkIn.proposedCalorieTarget == checkIn.currentCalorieTarget
    val headline = when {
        needsData -> "Check-in tuần đã sẵn sàng"
        unchanged -> "Mục tiêu tuần này vẫn phù hợp"
        else -> "NutriWise có đề xuất mới cho bạn"
    }
    val sub = when {
        needsData -> "Xem việc nhỏ để lần sau chính xác hơn"
        unchanged -> "Xem lại tuần vừa qua của bạn"
        else -> "Mục tiêu đề xuất ${checkIn.proposedCalorieTarget} kcal mỗi ngày"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, ambientColor = ShadowLightL2Ambient, spotColor = ShadowLightL2Spot)
            .clip(CardShape)
            .background(Brush.linearGradient(listOf(LavenderSoft, LavenderLight, PeachLight)))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = TextDeepInk, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(headline, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
            Text(sub, fontSize = 12.5.sp, color = TextDeepInk.copy(alpha = 0.75f))
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = TextDeepInk, modifier = Modifier.size(20.dp))
    }
}
