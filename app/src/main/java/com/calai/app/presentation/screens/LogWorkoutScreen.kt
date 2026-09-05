package com.calai.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.WorkoutCategory
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.EditableWorkoutExercise
import com.calai.app.presentation.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.saveSuccessEvent) {
        if (uiState.saveSuccessEvent) {
            onSaveSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        // Quầng ambient glow loang nhẹ phá vỡ khối đen (Quy tắc 10.5)
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VividOrange.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PastelMint.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 1. TOP APP BAR VỚI NÚT BACK
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CharcoalSurface)
                            .border(1.dp, CharcoalBorder, CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Ghi Nhận Buổi Tập",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Theo dõi Sets, Reps, Calo & Tải tạ",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                // Badge MET Calo tự động
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(VividOrange.copy(alpha = 0.15f))
                        .border(1.dp, VividOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DuotoneEnergyIcon(size = 14.dp, outlineColor = VividOrange, accentColor = VividOrangeLight)
                        Text(
                            text = "${uiState.caloriesBurned.toInt()} kcal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividOrange
                        )
                    }
                }
            }

            // 2. NỘI DUNG CUỘN CHÍNH
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
            ) {
                // Section A: Chọn Loại Hình Tập Luyện (Horizontal categories)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "LOẠI HÌNH TẬP LUYỆN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categories = listOf(
                                WorkoutCategory.STRENGTH to "Tập Tạ / Gym",
                                WorkoutCategory.RUNNING to "Chạy Bộ",
                                WorkoutCategory.HIIT to "HIIT / Tabata",
                                WorkoutCategory.CYCLING to "Đạp Xe",
                                WorkoutCategory.CARDIO to "Cardio",
                                WorkoutCategory.YOGA to "Yoga",
                                WorkoutCategory.SWIMMING to "Bơi Lội",
                                WorkoutCategory.SPORTS to "Thể Thao"
                            )

                            categories.forEach { (cat, label) ->
                                val isSelected = uiState.selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) {
                                                Brush.horizontalGradient(listOf(VividOrange, VividOrangeLight))
                                            } else {
                                                Brush.linearGradient(listOf(CharcoalCard, CharcoalSurface))
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) VividOrange.copy(alpha = 0.6f) else CharcoalBorder,
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { viewModel.setCategory(cat) }
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (cat == WorkoutCategory.STRENGTH) {
                                            DuotoneDumbbellIcon(
                                                size = 14.dp,
                                                outlineColor = if (isSelected) TextWhite else TextMuted,
                                                accentColor = if (isSelected) TextWhite else VividOrange
                                            )
                                        } else {
                                            DuotoneEnergyIcon(
                                                size = 14.dp,
                                                outlineColor = if (isSelected) TextWhite else TextMuted,
                                                accentColor = if (isSelected) TextWhite else VividOrange
                                            )
                                        }
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) TextWhite else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section B: Tên Buổi Tập & Thời Lượng (Form card)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CharcoalCard)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Tên buổi tập
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Tên buổi tập / Hoạt động",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                OutlinedTextField(
                                    value = uiState.workoutName,
                                    onValueChange = { viewModel.setWorkoutName(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VividOrange,
                                        unfocusedBorderColor = CharcoalBorder,
                                        focusedContainerColor = CharcoalSurface,
                                        unfocusedContainerColor = CharcoalSurface
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Thời lượng và Calo MET
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Thời lượng phút
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Thời lượng (phút)",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CharcoalSurface)
                                            .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.setDuration(uiState.durationMinutes - 5) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        }
                                        Text(
                                            text = "${uiState.durationMinutes}p",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        IconButton(
                                            onClick = { viewModel.setDuration(uiState.durationMinutes + 5) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VividOrange)
                                        }
                                    }
                                }

                                // Mức gắng sức RPE (1-10)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Gắng sức (RPE 1-10)",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CharcoalSurface)
                                            .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.setRpe(uiState.rpe - 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        }
                                        Text(
                                            text = "RPE ${uiState.rpe}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.rpe >= 8) VividOrange else PastelMint
                                        )
                                        IconButton(
                                            onClick = { viewModel.setRpe(uiState.rpe + 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VividOrange)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section C: Danh Sách Bài Tập & Sets (Cho Gym / Kháng lực)
                if (uiState.selectedCategory == WorkoutCategory.STRENGTH) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BÀI TẬP & SETS CHI TIẾT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            TextButton(
                                onClick = { showAddExerciseDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+ Thêm bài tập",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividOrange
                                )
                            }
                        }
                    }

                    if (uiState.exercises.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(CharcoalCard)
                                    .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DuotoneDumbbellIcon(size = 32.dp, outlineColor = TextMuted, accentColor = VividOrange)
                                    Text(
                                        text = "Chưa có bài tập nào trong buổi",
                                        fontSize = 14.sp,
                                        color = TextMuted
                                    )
                                    Button(
                                        onClick = { showAddExerciseDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("+ Thêm bài tập đầu tiên", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(uiState.exercises) { _, exercise ->
                        ExerciseCardItem(
                            exercise = exercise,
                            onAddSet = { viewModel.addSet(exercise.id) },
                            onRemoveSet = { setNumber -> viewModel.removeSet(exercise.id, setNumber) },
                            onUpdateSet = { setNum, reps, kg, rpe ->
                                viewModel.updateSet(exercise.id, setNum, reps, kg, rpe)
                            },
                            onCompleteSet = { setNum ->
                                viewModel.completeSet(exercise.id, setNum)
                            },
                            onDeleteExercise = { viewModel.removeExercise(exercise.id) }
                        )
                    }
                }

                // Section D: Ghi Chú Buổi Tập
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "GHI CHÚ BUỔI TẬP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = { viewModel.setNote(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            placeholder = { Text("VD: Đẩy ngực lên tạ mới 80kg rất tốt, form chuẩn...", color = TextMuted.copy(alpha = 0.6f), fontSize = 13.sp) },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VividOrange,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedContainerColor = CharcoalCard,
                                unfocusedContainerColor = CharcoalCard
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                // Thông báo lỗi nếu có
                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // 3. REST TIMER FLOATING CARD (Đồng hồ đếm ngược nghỉ ngơi tròn nổi)
        AnimatedVisibility(
            visible = uiState.isRestTimerRunning,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(CharcoalCardElevated, CharcoalSurface)
                        )
                    )
                    .border(1.dp, VividOrange.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = VividOrange.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Vòng tròn đếm ngược
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(VividOrange.copy(alpha = 0.15f))
                            .border(2.dp, VividOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${uiState.restSecondsRemaining}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividOrange
                        )
                    }

                    Column {
                        Text(
                            text = "Đang nghỉ ngơi phục hồi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Chuẩn bị cho Set tiếp theo...",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    IconButton(
                        onClick = { viewModel.stopRestTimer() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Bỏ qua nghỉ",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 4. BOTTOM ACTION CTA BUTTON (Nổi khối 3D Tactile)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, ObsidianBackground.copy(alpha = 0.95f), ObsidianBackground)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Button(
                onClick = {
                    viewModel.saveWorkout(onSuccess = onSaveSuccess)
                },
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = VividOrange.copy(alpha = 0.4f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VividOrange,
                    disabledContainerColor = VividOrange.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = TextWhite,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DuotoneCheckmarkIcon(size = 18.dp, outlineColor = TextWhite, accentColor = TextWhite)
                        Text(
                            text = "Hoàn Thành & Lưu Buổi Tập",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }
            }
        }

        // 5. DIALOG THÊM BÀI TẬP MỚI
        if (showAddExerciseDialog) {
            AlertDialog(
                onDismissRequest = { showAddExerciseDialog = false },
                containerColor = CharcoalCard,
                title = {
                    Text(
                        text = "Thêm bài tập",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Nhập tên bài tập hoặc chọn từ danh sách mẫu:",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        OutlinedTextField(
                            value = newExerciseName,
                            onValueChange = { newExerciseName = it },
                            placeholder = { Text("VD: Incline Dumbbell Press", color = TextMuted) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VividOrange,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedContainerColor = CharcoalSurface,
                                unfocusedContainerColor = CharcoalSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Gợi ý nhanh các bài tập phổ biến
                        val quickPicks = listOf("Bench Press", "Squat", "Deadlift", "Lat Pulldown", "Overhead Press", "Bicep Curl")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickPicks.forEach { pick ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CharcoalSurface)
                                        .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                                        .clickable { newExerciseName = pick }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = pick, fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newExerciseName.isNotBlank()) {
                                viewModel.addExercise(newExerciseName)
                                newExerciseName = ""
                                showAddExerciseDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Thêm", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddExerciseDialog = false }) {
                        Text("Hủy", color = TextMuted)
                    }
                }
            )
        }
    }
}

/**
 * Card hiển thị chi tiết 1 bài tập cùng các set của nó
 */
@Composable
fun ExerciseCardItem(
    exercise: EditableWorkoutExercise,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onUpdateSet: (setNum: Int, reps: Int, weightKg: Float, rpe: Int?) -> Unit,
    onCompleteSet: (setNum: Int) -> Unit,
    onDeleteExercise: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CharcoalCard)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header bài tập: Tên + Nút xoá
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PastelMint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        DuotoneDumbbellIcon(size = 18.dp, outlineColor = TextWhite, accentColor = PastelMint)
                    }

                    Text(
                        text = exercise.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                IconButton(
                    onClick = onDeleteExercise,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa bài tập",
                        tint = TextMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Header bảng Sets (Cột: SET | KG | REPS | RPE | XONG)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "SET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "MỨC TẠ (KG)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "REPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "XONG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            }

            // Danh sách các dòng Sets
            exercise.sets.forEach { s ->
                var repsText by remember(s.reps) { mutableStateOf(s.reps.toString()) }
                var weightText by remember(s.weightKg) { mutableStateOf(s.weightKg.toString()) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (s.isCompleted) PastelMint.copy(alpha = 0.08f) else CharcoalSurface)
                        .border(1.dp, if (s.isCompleted) PastelMint.copy(alpha = 0.3f) else CharcoalBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cột Set Number
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(if (s.isCompleted) PastelMint else CharcoalCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${s.setNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (s.isCompleted) TextDeepInk else TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Cột Tạ (Kg)
                    BasicTextField(
                        value = weightText,
                        onValueChange = {
                            weightText = it
                            val parsed = it.toFloatOrNull() ?: s.weightKg
                            onUpdateSet(s.setNumber, s.reps, parsed, s.rpe)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = LocalTextStyle.current.copy(
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalCard)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Cột Reps
                    BasicTextField(
                        value = repsText,
                        onValueChange = {
                            repsText = it
                            val parsed = it.toIntOrNull() ?: s.reps
                            onUpdateSet(s.setNumber, parsed, s.weightKg, s.rpe)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalCard)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Nút Đánh dấu hoàn thành Set
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (s.isCompleted) PastelMint else CharcoalCardElevated)
                            .clickable { onCompleteSet(s.setNumber) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (s.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Đã hoàn thành",
                                tint = TextDeepInk,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "Lưu",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Nút Thêm Set
            Button(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "+ Thêm Set ${exercise.sets.size + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }
    }
}
