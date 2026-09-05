package com.calai.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.DayWorkoutPlanDto
import com.calai.app.data.remote.dto.ExerciseGuideDto
import com.calai.app.data.remote.dto.WorkoutLogDto
import com.calai.app.data.remote.dto.WorkoutRecommendationData
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.SuggestionsViewModel
import com.calai.app.presentation.viewmodel.WorkoutViewModel

enum class TrainingHubTab(val title: String) {
    PROGRAM("Lộ trình tập"),
    HISTORY("Lịch sử tập"),
    LIBRARY("Kho bài tập")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHubScreen(
    onBack: () -> Unit,
    onNavigateToLogWorkout: () -> Unit,
    workoutViewModel: WorkoutViewModel = hiltViewModel(),
    suggestionsViewModel: SuggestionsViewModel = hiltViewModel()
) {
    val workoutUiState by workoutViewModel.uiState.collectAsState()
    val suggestionsUiState by suggestionsViewModel.uiState.collectAsState()

    var activeTab by remember { mutableStateOf(TrainingHubTab.PROGRAM) }
    var selectedWorkoutForDetail by remember { mutableStateOf<WorkoutLogDto?>(null) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        workoutViewModel.loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        // Quầng ambient glow loang mờ đa tầng chống bệt đen
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PastelLavender.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VividOrange.copy(alpha = 0.07f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 1. TOP HEADER & NÚT BACK
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
                            text = "Trung Tâm Tập Luyện",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Lộ trình, Lịch sử & Volume Load",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                // Nút CTA nhanh "Ghi buổi tập"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(VividOrange, VividOrangeLight)))
                        .clickable {
                            workoutViewModel.resetForm()
                            onNavigateToLogWorkout()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                        Text(text = "Ghi tập", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                }
            }

            // 2. SEGMENTED TAB SELECTOR (Lộ trình | Lịch sử | Kho bài tập)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(22.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TrainingHubTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(VividOrange, VividOrangeLight))
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { activeTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextWhite else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. NỘI DUNG THEO TỪNG TAB
            when (activeTab) {
                TrainingHubTab.PROGRAM -> {
                    ProgramTabContent(
                        workoutData = suggestionsUiState.workout,
                        onStartWorkoutDay = { dayTitle, exerciseNames ->
                            workoutViewModel.prefillFromProgram(dayTitle, exerciseNames)
                            onNavigateToLogWorkout()
                        }
                    )
                }

                TrainingHubTab.HISTORY -> {
                    HistoryTabContent(
                        workoutList = workoutUiState.history,
                        summary = workoutUiState.summary,
                        onSelectDetail = { selectedWorkoutForDetail = it },
                        onDelete = { workoutViewModel.deleteWorkout(it.id) }
                    )
                }

                TrainingHubTab.LIBRARY -> {
                    LibraryTabContent(
                        exercises = suggestionsUiState.exercises,
                        onLogExercise = { ex ->
                            workoutViewModel.resetForm()
                            workoutViewModel.setWorkoutName("Buổi tập ${ex.name}")
                            workoutViewModel.addExercise(ex.name)
                            onNavigateToLogWorkout()
                        }
                    )
                }
            }
        }

        // 4. BOTTOM SHEET XEM CHI TIẾT BUỔI TẬP ĐÃ LƯU
        selectedWorkoutForDetail?.let { workout ->
            ModalBottomSheet(
                onDismissRequest = { selectedWorkoutForDetail = null },
                sheetState = sheetState,
                containerColor = CharcoalCardElevated,
                contentColor = TextWhite
            ) {
                WorkoutDetailSheetContent(
                    workout = workout,
                    onClose = { selectedWorkoutForDetail = null }
                )
            }
        }
    }
}

/**
 * TAB 1: LỘ TRÌNH TẬP LUYỆN 4 TUẦN
 */
@Composable
fun ProgramTabContent(
    workoutData: WorkoutRecommendationData?,
    onStartWorkoutDay: (dayTitle: String, exerciseNames: List<String>) -> Unit
) {
    val scrollState = rememberScrollState()
    val plan = workoutData?.recommendedWorkout

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Mục Tiêu & Số Buổi/Tuần
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(CharcoalCard, CharcoalSurface)
                    )
                )
                .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelLavender.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    DuotoneCalendarIcon(size = 22.dp, outlineColor = TextWhite, accentColor = PastelLavender)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan?.title ?: "Lộ trình Tập Luyện Chuẩn 4 Tuần",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "${plan?.suitableForBmi ?: "Mọi thể trạng"} • ${plan?.goal ?: "Tăng cơ & Giảm mỡ"}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelMint.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelMint
                    )
                }
            }
        }

        // Lịch tập từng ngày trong tuần
        val schedule: List<DayWorkoutPlanDto> = plan?.weeklySchedule ?: emptyList()

        if (schedule.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CharcoalCard)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Đang tải lộ trình tập luyện...", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            schedule.forEach { dayItem ->
                val isRest = dayItem.exercises.isEmpty()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isRest) CharcoalSurface.copy(alpha = 0.6f) else CharcoalCard)
                        .border(
                            1.dp,
                            if (isRest) CharcoalBorder.copy(alpha = 0.4f) else CharcoalBorder,
                            RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isRest) CharcoalCardElevated else VividOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRest) {
                                        DuotoneCalendarIcon(size = 16.dp, outlineColor = TextMuted, accentColor = LavenderGradientStart)
                                    } else {
                                        DuotoneDumbbellIcon(size = 16.dp, outlineColor = TextWhite, accentColor = VividOrange)
                                    }
                                }

                                Column {
                                    Text(
                                        text = dayItem.dayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = dayItem.focus,
                                        fontSize = 12.sp,
                                        color = if (isRest) TextMuted else PastelMint
                                    )
                                }
                            }

                            if (!isRest) {
                                Button(
                                    onClick = {
                                        val exNames = dayItem.exercises.map { it.name }
                                        onStartWorkoutDay("${dayItem.dayName} - ${dayItem.focus}", exNames)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "Tập ngay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Danh sách bài tập preview trong ngày
                        if (dayItem.exercises.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CharcoalSurface)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                dayItem.exercises.forEach { exItem ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(VividOrange)
                                        )
                                        Text(
                                            text = "${exItem.name} (${exItem.sets}x${exItem.repsOrDuration})",
                                            fontSize = 12.sp,
                                            color = TextWhite.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TAB 2: LỊCH SỬ & NHẬT KÝ TẬP LUYỆN
 */
@Composable
fun HistoryTabContent(
    workoutList: List<WorkoutLogDto>,
    summary: com.calai.app.data.remote.dto.WorkoutSummaryDto?,
    onSelectDetail: (WorkoutLogDto) -> Unit,
    onDelete: (WorkoutLogDto) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Widget Hôm nay (Active Calo + Minutes + Workouts)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(CharcoalCard, CharcoalSurface)
                    )
                )
                .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "VẬN ĐỘNG HÔM NAY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${summary?.totalActiveCalories ?: 420}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividOrange
                        )
                        Text(text = "Active kcal", fontSize = 11.sp, color = TextMuted)
                    }

                    Column {
                        Text(
                            text = "${summary?.totalDurationMinutes ?: 55}p",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelMint
                        )
                        Text(text = "Thời lượng", fontSize = 11.sp, color = TextMuted)
                    }

                    Column {
                        Text(
                            text = "${summary?.workoutCount ?: workoutList.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelLavender
                        )
                        Text(text = "Buổi tập", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        Text(
            text = "CÁC BUỔI TẬP ĐÃ HOÀN THÀNH (${workoutList.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        if (workoutList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CharcoalCard)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DuotoneDumbbellIcon(size = 36.dp, outlineColor = TextMuted, accentColor = VividOrange)
                    Text(text = "Chưa có buổi tập nào được ghi nhận", fontSize = 14.sp, color = TextMuted)
                }
            }
        } else {
            workoutList.forEach { workout ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
                        .clickable { onSelectDetail(workout) }
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(VividOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DuotoneEnergyIcon(size = 18.dp, outlineColor = TextWhite, accentColor = VividOrange)
                                }

                                Column {
                                    Text(
                                        text = workout.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${workout.date.take(10)} • ${workout.durationMinutes} phút • RPE ${workout.rpe ?: 8}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Calo tiêu hao badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VividOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${workout.caloriesBurned.toInt()} kcal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividOrange
                                )
                            }
                        }

                        // Volume Load và Bài tập tóm tắt
                        if (workout.exercises.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CharcoalSurface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${workout.exercises.size} bài tập (${workout.exercises.sumOf { it.sets.size }} sets)",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Volume: ${(workout.totalVolumeKg ?: 0f).toInt()} kg",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelMint
                                )
                            }
                        }

                        if (!workout.note.isNullOrBlank()) {
                            Text(
                                text = "💬 ${workout.note}",
                                fontSize = 12.sp,
                                color = TextMuted.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * TAB 3: KHO BÀI TẬP (EXERCISE LIBRARY)
 */
@Composable
fun LibraryTabContent(
    exercises: List<ExerciseGuideDto>,
    onLogExercise: (ExerciseGuideDto) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = exercises.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.targetMuscle.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm bài tập theo tên hoặc nhóm cơ...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VividOrange,
                unfocusedBorderColor = CharcoalBorder,
                focusedContainerColor = CharcoalCard,
                unfocusedContainerColor = CharcoalCard
            ),
            shape = RoundedCornerShape(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { ex ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PastelMint.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DuotoneDumbbellIcon(size = 18.dp, outlineColor = TextWhite, accentColor = PastelMint)
                                }

                                Column {
                                    Text(
                                        text = ex.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${ex.targetMuscle} • ${ex.equipment}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Button(
                                onClick = { onLogExercise(ex) },
                                colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "+ Ghi tập", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = ex.instructions.execution,
                            fontSize = 12.sp,
                            color = TextWhite.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * MODAL BOTTOM SHEET XEM CHI TIẾT 1 BUỔI TẬP
 */
@Composable
fun WorkoutDetailSheetContent(
    workout: WorkoutLogDto,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = workout.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "${workout.date.take(10)} • ${workout.durationMinutes} phút • ${workout.caloriesBurned.toInt()} kcal",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PastelMint.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "RPE ${workout.rpe ?: 8}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PastelMint
                )
            }
        }

        if (workout.exercises.isNotEmpty()) {
            Text(
                text = "DANH SÁCH BÀI TẬP (${workout.exercises.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            workout.exercises.forEach { ex ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = ex.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ex.sets.forEach { s ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CharcoalSurface)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${s.weightKg.toInt()}kg × ${s.reps}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PastelMint
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!workout.note.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalSurface)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Ghi chú: ${workout.note}",
                    fontSize = 12.sp,
                    color = TextWhite.copy(alpha = 0.9f)
                )
            }
        }

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Đóng", fontWeight = FontWeight.Bold)
        }
    }
}
