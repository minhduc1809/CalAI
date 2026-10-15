package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.NewRecordDto
import com.calai.app.data.workout.DraftExercise
import com.calai.app.data.workout.DraftSet
import com.calai.app.data.workout.WorkoutSessionLogic
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ResumeState
import com.calai.app.presentation.viewmodel.SaveSummary
import com.calai.app.presentation.viewmodel.WorkoutViewModel
import java.util.Locale

private val CATEGORY_CHIPS = listOf(
    "STRENGTH" to "Tập tạ",
    "RUNNING" to "Chạy bộ",
    "HIIT" to "HIIT",
    "CYCLING" to "Đạp xe",
    "CARDIO" to "Cardio",
    "YOGA" to "Yoga",
    "SWIMMING" to "Bơi lội",
    "WALKING" to "Đi bộ",
    "SPORTS" to "Thể thao"
)

/** Hiển thị tạ theo đơn vị của người dùng (kg hoặc lb) nhưng luôn lưu kg. */
private fun fmtWeight(kg: Float, unit: String): String {
    val v = UserPreferencesManager.convertKg(kg, unit)
    return if (v == 0f) "" else if (v == v.toInt().toFloat()) v.toInt().toString() else String.format(Locale.US, "%.1f", v)
}

/**
 * Màn Ghi buổi tập (BR-10.4). Phiên tập được lưu bền trên máy: thoát giữa buổi vẫn tiếp tục được, mất mạng khi lưu thì
 * buổi tập vào hàng đợi và tự gửi lại, số lần/tạ của lần tập trước được điền sẵn, có đồng hồ nghỉ giữa set.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val draft = ui.draft
    var showAddExercise by remember { mutableStateOf(false) }
    var showRoutines by remember { mutableStateOf(false) }
    var showDiscard by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.ensureDraft() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(PeachSoft.copy(alpha = if (isDarkTheme) 0.14f else 0.32f), Color.Transparent)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CheckinHeader(
                        title = "Ghi buổi tập",
                        subtitle = if (draft?.category == "STRENGTH") "Set, số lần, tạ và đồng hồ nghỉ" else "Thời lượng và cường độ",
                        onBack = onBack
                    )
                }
                if (draft != null) {
                    CalorieBadge(calories = ui.estimatedCalories(System.currentTimeMillis()))
                }
            }
            Spacer(Modifier.height(10.dp))

            if (draft == null) {
                LoadingSkeleton()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 6.dp, bottom = 24.dp)
                ) {
                    // Bắt đầu nhanh: chỉ hiện khi phiên còn trống
                    if (!draft.hasContent && draft.exercises.isEmpty()) {
                        item {
                            QuickStartRow(
                                hasLast = ui.lastTemplate != null,
                                routineCount = ui.routines.size,
                                onRepeatLast = { ui.lastTemplate?.let(viewModel::startFromTemplate) },
                                onRoutines = { showRoutines = true }
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CATEGORY_CHIPS.forEach { (cat, label) ->
                                val selected = draft.category == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (selected) Brush.linearGradient(listOf(PeachSoft, PeachLight)) else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)))
                                        .border(1.dp, if (selected) PeachSoft else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                        .clickable { viewModel.setCategory(cat) }
                                        .padding(horizontal = 14.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        label,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) TextDeepInk else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item { SessionInfoCard(ui.draft!!, viewModel) }

                    if (draft.category == "STRENGTH") {
                        items(draft.exercises, key = { it.id }) { ex ->
                            ExerciseCard(ex, ui.weightUnit, viewModel)
                        }
                        item {
                            OutlinedAddButton(text = "Thêm bài tập", onClick = { showAddExercise = true })
                        }
                    }

                    item {
                        Text(
                            "Calo tập đã được tính trong mục tiêu hằng ngày của bạn, không cộng thêm vào phần còn lại.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ui.errorMessage?.let { item { ErrorLine(it) } }
                }

                if (ui.isRestTimerRunning) {
                    RestTimerCard(
                        remaining = ui.restSecondsRemaining,
                        total = ui.totalRestSeconds,
                        onSkip = viewModel::stopRestTimer
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (draft.hasContent || draft.exercises.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .clickable { showDiscard = true },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Delete, "Huỷ buổi tập", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    AppButton(
                        text = "Lưu buổi tập",
                        onClick = viewModel::save,
                        enabled = !ui.isSubmitting,
                        isLoading = ui.isSubmitting,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // ── Hộp thoại ──
    if (ui.resume == ResumeState.RESUME) {
        com.calai.app.presentation.components.NwAlertDialog(
            onDismissRequest = {},
            title = { Text("Tiếp tục buổi tập đang dở?") },
            text = { Text("Bạn có một buổi tập chưa lưu. Các set đã ghi vẫn còn nguyên.") },
            confirmButton = { TextButton(onClick = viewModel::acknowledgeResume) { Text("Tiếp tục") } },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.discardDraft()
                    viewModel.startBlank()
                }) { Text("Bắt đầu buổi mới") }
            }
        )
    }
    if (ui.resume == ResumeState.ABANDONED) {
        com.calai.app.presentation.components.NwAlertDialog(
            onDismissRequest = {},
            title = { Text("Buổi tập dở đã quá 12 giờ") },
            text = { Text("Bạn muốn lưu buổi tập này, hay huỷ để bắt đầu buổi mới?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.acknowledgeResume()
                    viewModel.save()
                }) { Text("Lưu lại") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.discardDraft()
                    viewModel.startBlank()
                }) { Text("Huỷ bỏ") }
            }
        )
    }
    if (showDiscard) {
        com.calai.app.presentation.components.NwAlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("Huỷ buổi tập này?") },
            text = { Text("Các set bạn đã nhập sẽ bị xoá.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscard = false
                    viewModel.discardDraft()
                    viewModel.startBlank()
                }) { Text("Huỷ buổi tập", color = CrimsonError) }
            },
            dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Giữ lại") } }
        )
    }
    ui.saveSummary?.let { summary ->
        SaveSummarySheet(
            summary = summary,
            weightUnit = ui.weightUnit,
            onSaveRoutine = { name, onDone -> viewModel.saveSummaryAsRoutine(name, onDone) },
            onDone = {
                viewModel.dismissSummary()
                onSaveSuccess()
            }
        )
    }
    if (showAddExercise) {
        AddExerciseSheet(
            viewModel = viewModel,
            onDismiss = { showAddExercise = false }
        )
    }
    if (showRoutines) {
        RoutinesSheet(
            routines = ui.routines,
            onPick = {
                viewModel.startFromRoutine(it)
                showRoutines = false
            },
            onDelete = viewModel::deleteRoutine,
            onDismiss = { showRoutines = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// THÀNH PHẦN
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CalorieBadge(calories: Float) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(PeachSoft, PeachLight)))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Default.LocalFireDepartment, null, tint = TextDeepInk, modifier = Modifier.size(15.dp))
        Text("≈ ${calories.toInt()} kcal", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
    }
}

@Composable
private fun QuickStartRow(hasLast: Boolean, routineCount: Int, onRepeatLast: () -> Unit, onRoutines: () -> Unit) {
    if (!hasLast && routineCount == 0) return
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        if (hasLast) {
            QuickStartCard(Modifier.weight(1f), Icons.Default.Replay, "Lặp lại buổi gần nhất", "Cùng bài, số liệu điền sẵn", MintSoft, MintLight, onRepeatLast)
        }
        if (routineCount > 0) {
            QuickStartCard(Modifier.weight(1f), Icons.Default.History, "Buổi tập mẫu", "$routineCount mẫu đã lưu", LavenderSoft, LavenderLight, onRoutines)
        }
    }
}

@Composable
private fun QuickStartCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    soft: Color,
    light: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(light, soft.copy(alpha = 0.6f))))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = TextDeepInk, modifier = Modifier.size(20.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
        Text(subtitle, fontSize = 11.sp, color = TextDeepInk.copy(alpha = 0.7f))
    }
}

@Composable
private fun SessionInfoCard(draft: com.calai.app.data.workout.WorkoutDraft, vm: WorkoutViewModel) {
    PastelCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = draft.name,
                onValueChange = vm::setName,
                label = { Text("Tên buổi tập") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Stepper(Modifier.weight(1f), "Thời lượng", "${draft.durationMinutes} phút", { vm.setDuration(draft.durationMinutes - 5) }, { vm.setDuration(draft.durationMinutes + 5) })
                Stepper(Modifier.weight(1f), "Mức gắng sức", "RPE ${draft.rpe}", { vm.setRpe(draft.rpe - 1) }, { vm.setRpe(draft.rpe + 1) })
            }
            OutlinedTextField(
                value = draft.note,
                onValueChange = vm::setNote,
                label = { Text("Ghi chú (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )
        }
    }
}

@Composable
private fun Stepper(modifier: Modifier, label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.size(44.dp).clickable(onClick = onMinus), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Remove, "Giảm", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Box(Modifier.size(44.dp).clickable(onClick = onPlus), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, "Tăng", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun OutlinedAddButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, LavenderSoft, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ExerciseCard(ex: DraftExercise, unit: String, vm: WorkoutViewModel) {
    PastelCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(ex.name, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    ex.primaryMuscle?.takeIf { it.isNotBlank() }?.let {
                        Text(it, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(Modifier.size(36.dp).clickable { vm.removeExercise(ex.id) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, "Xoá bài", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Nghỉ giữa các set cho riêng bài này (mặc định 90 giây)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Nghỉ ${ex.restSec} giây", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                MiniButton("−15") { vm.setRest(ex.id, ex.restSec - 15) }
                MiniButton("+15") { vm.setRest(ex.id, ex.restSec + 15) }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                HeaderCell("Set", Modifier.width(42.dp))
                HeaderCell(if (unit == "lb") "Tạ (lb)" else "Tạ (kg)", Modifier.weight(1f))
                HeaderCell("Lần", Modifier.weight(1f))
                HeaderCell("RPE", Modifier.width(56.dp))
                Spacer(Modifier.width(44.dp))
            }

            var workingIndex = 0
            ex.sets.forEach { s ->
                val label = if (s.isWarmup) "K" else { workingIndex += 1; workingIndex.toString() }
                SetRow(ex, s, label, unit, vm)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { vm.addSet(ex.id) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                Text("  Thêm set", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun MiniButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) { Text(text, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(text, modifier = modifier, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SetRow(ex: DraftExercise, s: DraftSet, label: String, unit: String, vm: WorkoutViewModel) {
    val done = s.isCompleted
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (done) MintSoft.copy(alpha = 0.22f) else Color.Transparent)
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Chạm vào số set để chuyển giữa set tập và set khởi động (K)
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (s.isWarmup) ButterSoft.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable { vm.toggleWarmup(ex.id, s.id) },
                contentAlignment = Alignment.Center
            ) { Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }

            NumberCell(
                modifier = Modifier.weight(1f),
                text = fmtWeight(s.weightKg, unit),
                placeholder = "0",
                decimal = true,
                onCommit = { v -> vm.setWeight(ex.id, s.id, UserPreferencesManager.convertToKg(v, unit)) }
            )
            NumberCell(
                modifier = Modifier.weight(1f),
                text = s.reps.toString(),
                placeholder = "0",
                decimal = false,
                onCommit = { v -> vm.setReps(ex.id, s.id, v.toInt()) }
            )
            NumberCell(
                modifier = Modifier.width(56.dp),
                text = s.rpe?.let { if (it == it.toInt().toFloat()) it.toInt().toString() else it.toString() } ?: "",
                placeholder = "–",
                decimal = true,
                onCommit = { v -> vm.setSetRpe(ex.id, s.id, if (v <= 0f) null else v) }
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (done) MintSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable { vm.toggleSetDone(ex.id, s.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, "Đã tập xong set", tint = if (done) TextDeepInk else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }
        if (!done && s.hint != null) {
            Text(s.hint, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 48.dp))
        }
        if (!s.isWarmup && WorkoutSessionLogic.isWeightJump(s.weightKg, ex.lastMaxKg)) {
            Row(modifier = Modifier.padding(start = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = CoralWarning, modifier = Modifier.size(13.dp))
                Text(
                    "  Tạ gấp hơn 2 lần lần trước. Bạn nhập đúng đơn vị (kg/lb) chưa?",
                    fontSize = 10.5.sp,
                    color = CoralWarning
                )
            }
        }
    }
}

/** Ô nhập số gọn; giữ chữ đang gõ riêng để con trỏ không nhảy, chỉ ghi vào phiên khi giá trị hợp lệ. */
@Composable
private fun NumberCell(modifier: Modifier, text: String, placeholder: String, decimal: Boolean, onCommit: (Float) -> Unit) {
    var local by remember(text) { mutableStateOf(text) }
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = local,
            onValueChange = { raw ->
                val cleaned = raw.replace(',', '.').filter { it.isDigit() || (decimal && it == '.') }.take(6)
                local = cleaned
                cleaned.toFloatOrNull()?.let(onCommit)
                if (cleaned.isEmpty()) onCommit(0f)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                    if (local.isEmpty()) Text(placeholder, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    inner()
                }
            }
        )
    }
}

@Composable
private fun RestTimerCard(remaining: Int, total: Int, onSkip: () -> Unit) {
    val progress = if (total <= 0) 0f else remaining.toFloat() / total
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(SkySoft, SkyLight)))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.Timer, null, tint = TextDeepInk)
        Column(modifier = Modifier.weight(1f)) {
            Text("Nghỉ giữa set", fontSize = 12.sp, color = TextDeepInk.copy(alpha = 0.75f))
            Text(String.format(Locale.US, "%d:%02d", remaining / 60, remaining % 60), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50)),
                color = TextDeepInk,
                trackColor = Color.White.copy(alpha = 0.55f)
            )
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.7f)).clickable(onClick = onSkip).padding(horizontal = 14.dp, vertical = 9.dp)
        ) { Text("Bỏ qua", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextDeepInk) }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// BẢNG TÓM TẮT, CHỌN BÀI, MẪU
// ═══════════════════════════════════════════════════════════════════════════

private fun recordLabel(r: NewRecordDto, unit: String): String {
    val name = r.exerciseName ?: "Bài tập"
    return when (r.type) {
        "MAX_WEIGHT" -> "$name: tạ cao nhất ${fmtWeight(r.value, unit).ifBlank { "0" }} ${if (unit == "lb") "lb" else "kg"}"
        "EST_1RM" -> "$name: 1RM ước tính ${fmtWeight(r.value, unit)} ${if (unit == "lb") "lb" else "kg"}"
        "MAX_REPS_BODYWEIGHT" -> "$name: ${r.value.toInt()} lần, kỷ lục mới"
        else -> "$name: ${r.reps ?: r.value.toInt()} lần ở ${fmtWeight(r.weightKg ?: 0f, unit)} ${if (unit == "lb") "lb" else "kg"}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveSummarySheet(
    summary: SaveSummary,
    weightUnit: String,
    onSaveRoutine: (String?, (Boolean, String) -> Unit) -> Unit,
    onDone: () -> Unit
) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    ModalBottomSheet(onDismissRequest = onDone, sheetState = sheet, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(if (summary.queued) "Đã lưu trên máy" else "Hoàn thành buổi tập!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(summary.workoutName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (summary.queued) {
                Text(
                    "Hiện chưa có kết nối. Buổi tập sẽ tự gửi lên khi có mạng, bạn không cần làm gì thêm.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryStat(Modifier.weight(1f), "Thời gian", "${summary.durationMinutes}", "phút", SkySoft, SkyLight)
                SummaryStat(
                    Modifier.weight(1f), "Volume",
                    fmtWeight(summary.volumeKg, weightUnit).ifBlank { "0" }, if (weightUnit == "lb") "lb" else "kg", MintSoft, MintLight
                )
                SummaryStat(Modifier.weight(1f), "Calo", summary.calories?.toInt()?.toString() ?: "—", "kcal", PeachSoft, PeachLight)
            }

            if (summary.newRecords.isNotEmpty()) {
                PastelCard(tint = ButterSoft.copy(alpha = 0.16f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconBadge(Icons.Default.EmojiEvents, ButterSoft, ButterSoft.copy(alpha = 0.5f), size = 36.dp)
                            Text("Kỷ lục mới!", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        summary.newRecords.forEach { r ->
                            Text("• ${recordLabel(r, weightUnit)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            summary.warnings.forEach { w ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, null, tint = CoralWarning, modifier = Modifier.size(16.dp))
                    Text(
                        "  ${w.name}: tạ cao nhất ${fmtWeight(w.currentKg, weightUnit)} so với ${fmtWeight(w.previousKg, weightUnit)} lần trước. Hãy kiểm tra lại đơn vị kg/lb nếu nhập nhầm.",
                        fontSize = 12.sp,
                        color = CoralWarning
                    )
                }
            }

            if (summary.savedWorkoutId != null) {
                OutlinedAddButton(text = "Lưu thành buổi tập mẫu") {
                    onSaveRoutine(null) { ok, msg ->
                        android.widget.Toast.makeText(ctx, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            AppButton(text = "Xong", onClick = onDone, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SummaryStat(modifier: Modifier, label: String, value: String, unit: String, soft: Color, light: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(light, soft.copy(alpha = 0.6f))))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, fontSize = 11.sp, color = TextDeepInk.copy(alpha = 0.7f))
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
        Text(unit, fontSize = 11.sp, color = TextDeepInk.copy(alpha = 0.7f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseSheet(viewModel: WorkoutViewModel, onDismiss: () -> Unit) {
    val ui by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var creatingMuscle by remember { mutableStateOf(false) }
    var muscle by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.loadLibrary() }

    val q = WorkoutSessionLogic.normalize(query)
    val custom = ui.customExercises.filter { q.isEmpty() || WorkoutSessionLogic.normalize(it.name).contains(q) }
    val library = ui.library.filter { q.isEmpty() || WorkoutSessionLogic.normalize(it.name).contains(q) || WorkoutSessionLogic.normalize(it.targetMuscle).contains(q) }.take(40)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Thêm bài tập", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Tìm bài (không cần gõ dấu)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            LazyColumn(modifier = Modifier.height(380.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (custom.isNotEmpty()) {
                    item { SectionTitle("Bài của tôi") }
                    items(custom, key = { "c" + it.id }) { c ->
                        ExerciseOption(c.name, c.primaryMuscle) {
                            viewModel.addExercise(c.name, "CUSTOM", c.id, c.primaryMuscle)
                            onDismiss()
                        }
                    }
                }
                if (library.isNotEmpty()) {
                    item { SectionTitle("Kho bài tập") }
                    items(library, key = { "l" + it.id }) { ex ->
                        ExerciseOption(ex.name, ex.targetMuscle) {
                            viewModel.addExercise(ex.name, "CATALOG", ex.id, ex.targetMuscle.substringBefore(',').trim())
                            onDismiss()
                        }
                    }
                }
                item {
                    if (query.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            OutlinedAddButton("Tạo bài \"${query.trim()}\"") { creatingMuscle = true }
                            if (creatingMuscle) {
                                OutlinedTextField(
                                    value = muscle,
                                    onValueChange = { muscle = it },
                                    label = { Text("Nhóm cơ chính (ví dụ Ngực, Chân)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                AppButton(
                                    text = "Tạo và thêm vào buổi tập",
                                    onClick = {
                                        viewModel.createCustomExercise(query.trim(), muscle.ifBlank { "Khác" }) { created ->
                                            if (created != null) {
                                                viewModel.addExercise(created.name, "CUSTOM", created.id, created.primaryMuscle)
                                                onDismiss()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseOption(name: String, muscle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (muscle.isNotBlank()) Text(muscle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutinesSheet(
    routines: List<com.calai.app.data.remote.dto.RoutineDto>,
    onPick: (com.calai.app.data.remote.dto.RoutineDto) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Buổi tập mẫu", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            if (routines.isEmpty()) {
                Text("Chưa có mẫu nào. Sau khi lưu một buổi tập, bạn có thể lưu nó thành mẫu.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            routines.forEach { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LavenderSoft.copy(alpha = 0.2f))
                        .clickable { onPick(r) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.name, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${r.items.size} bài", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(Modifier.size(36.dp).clickable { onDelete(r.id) }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Delete, "Xoá mẫu", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
