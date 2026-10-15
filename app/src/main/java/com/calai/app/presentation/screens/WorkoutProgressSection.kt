package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.HistorySessionDto
import com.calai.app.data.workout.WorkoutSessionLogic
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.WorkoutUiState
import com.calai.app.presentation.viewmodel.WorkoutViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

private fun typeName(t: String) = when (t) {
    "MAX_WEIGHT" -> "Tạ cao nhất"
    "EST_1RM" -> "1RM ước tính"
    "MAX_REPS_AT_WEIGHT" -> "Nhiều lần nhất ở một mức tạ"
    else -> "Nhiều lần nhất (tạ cơ thể)"
}

/**
 * Tiến bộ tập luyện (BR-10.6 → 10.8): thẻ đối chiếu số buổi tập, lịch tháng, volume 8 tuần, set theo nhóm cơ, kỷ lục.
 * Không có dữ liệu thì hiện trạng thái trống, không bịa số.
 */
@Composable
fun WorkoutProgressSection(
    ui: WorkoutUiState,
    vm: WorkoutViewModel,
    onToast: (String) -> Unit,
    onOpenWorkout: (String) -> Unit
) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { vm.loadActivityCheck(); vm.loadProgress() }
    LaunchedEffect(month) { vm.loadMonth(month.toString()); selectedDay = null }
    val unit = ui.weightUnit
    val unitLabel = if (unit == "lb") "lb" else "kg"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ui.activityCheck?.let { c ->
            PastelCard(tint = ButterSoft.copy(alpha = 0.18f)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Số buổi tập thực tế khác khai báo", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        c.message ?: "Bạn khai báo ${c.declaredDays ?: "?"} buổi/tuần, thực tế khoảng ${c.averagePerWeek?.let { String.format(Locale.US, "%.1f", it) } ?: "?"} buổi/tuần.",
                        fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (c.suggestedTrainingDays != null) {
                            TextButton(onClick = { vm.acceptActivityCheck { _, msg -> onToast(msg) } }) {
                                Text("Cập nhật thành ${c.suggestedTrainingDays} buổi/tuần")
                            }
                        }
                        TextButton(onClick = vm::dismissActivityCheck) { Text("Giữ nguyên") }
                    }
                }
            }
        }

        // ── Lịch tháng ──
        PastelCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { month = month.minusMonths(1) }) { Text("‹") }
                    Text("Tháng ${month.monthValue}/${month.year}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    TextButton(onClick = { month = month.plusMonths(1) }, enabled = month < YearMonth.now()) { Text("›") }
                }
                val history = ui.monthHistory?.takeIf { it.month == month.toString() }
                val byDay = history?.days?.associateBy { it.date } ?: emptyMap()
                val lead = month.atDay(1).dayOfWeek.value - 1
                val cells: List<LocalDate?> = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach {
                        Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (i in 0 until 7) {
                            val d: LocalDate? = week.getOrNull(i)
                            Box(modifier = Modifier.weight(1f).height(38.dp), contentAlignment = Alignment.Center) {
                                if (d != null) {
                                    val key = d.toString()
                                    val day = byDay[key]
                                    val sel = selectedDay == key
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(CircleShape)
                                            .background(
                                                when {
                                                    sel -> PeachSoft
                                                    day != null -> MintSoft.copy(alpha = 0.55f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable(enabled = day != null) { selectedDay = if (sel) null else key },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            d.dayOfMonth.toString(), fontSize = 12.sp,
                                            fontWeight = if (day != null) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (sel) TextDeepInk else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (history == null) {
                    Text("Đang tải lịch…", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (history.sessions.isEmpty()) {
                    Text("Tháng này chưa có buổi tập nào.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val list: List<HistorySessionDto> = selectedDay?.let { d -> history.sessions.filter { it.date.startsWith(d) } } ?: history.sessions
                    list.forEach { s ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onOpenWorkout(s.id) }
                                .androidxPadding(12),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "${s.date.take(10)} · ${s.durationMinutes} phút" + if (s.setCount > 0) " · ${s.setCount} set" else "",
                                    fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("${s.caloriesBurned.toInt()} kcal", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // ── Volume 8 tuần ──
        PastelCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Volume theo tuần ($unitLabel)", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                val items = ui.volume?.items.orEmpty()
                if (items.none { it.volume > 0f }) {
                    Text("Chưa có dữ liệu. Ghi buổi tập tạ để theo dõi volume.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val max = items.maxOf { it.volume }
                    Row(modifier = Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        items.forEach { w ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                Box(
                                    Modifier.fillMaxWidth().height((88 * WorkoutSessionLogic.safeRatio(w.volume, max)).coerceAtLeast(2f).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(LavenderSoft)
                                )
                                Text(w.weekStart.takeLast(5).replace('-', '/'), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // ── Set theo nhóm cơ (tuần này) ──
        PastelCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Số set theo nhóm cơ (tuần này)", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                val items = ui.setsPerMuscle?.items.orEmpty().filter { it.sets > 0 }
                if (items.isEmpty()) Text("Tuần này chưa có set nào.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val max = items.maxOfOrNull { it.sets } ?: 1
                items.forEach { m ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(m.muscle, modifier = Modifier.width(92.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Box(Modifier.fillMaxWidth(WorkoutSessionLogic.safeRatio(m.sets.toFloat(), max.toFloat())).height(10.dp).clip(RoundedCornerShape(50)).background(MintSoft))
                        }
                        Text("  ${m.sets}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // ── Kỷ lục ──
        PastelCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Kỷ lục cá nhân", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                val items = ui.records?.items.orEmpty().filter { it.records.isNotEmpty() }
                if (items.isEmpty()) Text("Chưa có kỷ lục. Hoàn thành buổi tập tạ để bắt đầu.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                items.take(10).forEach { ex ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(ex.exerciseName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        ex.records.forEach { r ->
                            val valueText = if (r.type == "MAX_REPS_BODYWEIGHT") "${r.value.toInt()} lần"
                            else {
                                val v = UserPreferencesManager.convertKg(if (r.type == "MAX_REPS_AT_WEIGHT") (r.weightKg ?: 0f) else r.value, unit)
                                val w = if (v == v.toInt().toFloat()) v.toInt().toString() else String.format(Locale.US, "%.1f", v)
                                if (r.type == "MAX_REPS_AT_WEIGHT") "${r.reps ?: r.value.toInt()} lần × $w $unitLabel" else "$w $unitLabel"
                            }
                            Text("${typeName(r.type)}: $valueText", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

private fun Modifier.androidxPadding(all: Int): Modifier = this.then(Modifier.padding(all.dp))
