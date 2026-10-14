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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.DayWorkoutPlanDto
import com.calai.app.data.remote.dto.ExerciseGuideDto
import com.calai.app.data.remote.dto.ExerciseInstructionsDto
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
    onNavigateTab: (com.calai.app.presentation.components.DockTab) -> Unit = {},
    isDarkTheme: Boolean = true,
    workoutViewModel: WorkoutViewModel = hiltViewModel(),
    suggestionsViewModel: SuggestionsViewModel = hiltViewModel()
) {
    val workoutUiState by workoutViewModel.uiState.collectAsState()
    val suggestionsUiState by suggestionsViewModel.uiState.collectAsState()

    var activeTab by remember { mutableStateOf(TrainingHubTab.PROGRAM) }
    var selectedWorkoutForDetail by remember { mutableStateOf<WorkoutLogDto?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    LaunchedEffect(Unit) {
        workoutViewModel.loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Quầng ambient glow loang mờ đa tầng chống bệt đen
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PastelLavender.copy(alpha = if (isDarkTheme) 0.08f else 0.15f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VividOrange.copy(alpha = if (isDarkTheme) 0.07f else 0.12f), Color.Transparent)
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
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Trung Tâm Tập Luyện",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Lộ trình, Lịch sử & Volume Load",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Nút CTA nhanh "Ghi buổi tập"
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = VividOrange.copy(alpha = 0.3f), spotColor = VividOrange.copy(alpha = 0.3f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(CtaSolidOrange)
                        .clickable {
                            workoutViewModel.startBlank()
                            onNavigateToLogWorkout()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                        Text(text = "Ghi tập", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            // 2. SEGMENTED TAB SELECTOR (Lộ trình | Lịch sử | Kho bài tập)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .shadow(
                        elevation = if (isDarkTheme) 2.dp else 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
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
                            .background(if (isSelected) CtaSolidOrange else Color.Transparent)
                            .clickable { activeTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        isDark = isDarkTheme,
                        libraryExercises = suggestionsUiState.exercises,
                        onLogExercise = { ex ->
                            workoutViewModel.startFromExercise(ex.name, "CATALOG", ex.id.takeUnless { it.startsWith("plan-") }, ex.targetMuscle.substringBefore(',').trim())
                            onNavigateToLogWorkout()
                        },
                        onStartWorkoutDay = { dayTitle, exerciseNames ->
                            workoutViewModel.startFromProgram(dayTitle, exerciseNames)
                            onNavigateToLogWorkout()
                        }
                    )
                }

                TrainingHubTab.HISTORY -> {
                    HistoryTabContent(
                        workoutList = workoutUiState.history,
                        summary = workoutUiState.summary,
                        isDark = isDarkTheme,
                        onSelectDetail = { selectedWorkoutForDetail = it },
                        onDelete = { workoutViewModel.deleteWorkout(it.id) },
                        progressSection = {
                            val ctx = androidx.compose.ui.platform.LocalContext.current
                            WorkoutProgressSection(
                                ui = workoutUiState,
                                vm = workoutViewModel,
                                onToast = { android.widget.Toast.makeText(ctx, it, android.widget.Toast.LENGTH_SHORT).show() },
                                onOpenWorkout = workoutViewModel::openWorkoutById
                            )
                        }
                    )
                }

                TrainingHubTab.LIBRARY -> {
                    LibraryTabContent(
                        exercises = suggestionsUiState.exercises,
                        isLoading = suggestionsUiState.isLoading,
                        errorMessage = suggestionsUiState.errorMessage,
                        onRetry = suggestionsViewModel::reload,
                        isDark = isDarkTheme,
                        onLogExercise = { ex ->
                            workoutViewModel.startFromExercise(ex.name, "CATALOG", ex.id, ex.targetMuscle.substringBefore(',').trim())
                            onNavigateToLogWorkout()
                        }
                    )
                }
            }
        }

        com.calai.app.presentation.components.FloatingBottomDock(
            currentTab = com.calai.app.presentation.components.DockTab.WORKOUT,
            isDarkTheme = isDarkTheme,
            onTabSelected = { tab -> if (tab != com.calai.app.presentation.components.DockTab.WORKOUT) onNavigateTab(tab) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 4. BOTTOM SHEET XEM CHI TIẾT BUỔI TẬP ĐÃ LƯU
        selectedWorkoutForDetail?.let { workout ->
            ModalBottomSheet(
                onDismissRequest = { selectedWorkoutForDetail = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                WorkoutDetailSheetContent(
                    workout = workout,
                    isDark = isDarkTheme,
                    onClose = { selectedWorkoutForDetail = null }
                )
            }
        }
    }
}


/**
 * TAB 1: LỘ TRÌNH TẬP LUYỆN 4 TUẦN
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProgramTabContent(
    workoutData: WorkoutRecommendationData?,
    isDark: Boolean = true,
    libraryExercises: List<ExerciseGuideDto> = emptyList(),
    onStartWorkoutDay: (dayTitle: String, exerciseNames: List<String>) -> Unit,
    onLogExercise: (ExerciseGuideDto) -> Unit = {}
) {
    var detail by remember { mutableStateOf<ExerciseGuideDto?>(null) }
    detail?.let { ex ->
        com.calai.app.presentation.components.ExerciseDetailSheet(
            exercise = ex,
            onDismiss = { detail = null },
            onLog = { onLogExercise(ex) }
        )
    }
    val scrollState = rememberScrollState()
    val plan = workoutData?.recommendedWorkout
    var showPlanInfo by remember { mutableStateOf(false) }

    // Tiến độ từng bài trong tuần: lưu cục bộ, tự đặt lại khi sang tuần mới.
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("plan_progress", android.content.Context.MODE_PRIVATE) }
    val weekKey = remember { java.time.LocalDate.now().let { "${it.year}-W${it.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)}" } }
    val done = remember(plan?.id) {
        mutableStateListOf<String>().apply {
            if (prefs.getString("week", null) != weekKey) prefs.edit().clear().putString("week", weekKey).apply()
            addAll(prefs.getStringSet("done_${plan?.id}", emptySet()) ?: emptySet())
        }
    }
    fun toggleDone(key: String) {
        if (!done.remove(key)) done.add(key)
        prefs.edit().putStringSet("done_${plan?.id}", done.toSet()).apply()
    }
    val totalCount = plan?.weeklySchedule?.sumOf { it.exercises.size } ?: 0
    val doneCount = plan?.weeklySchedule?.sumOf { d -> d.exercises.count { "${d.dayName}|${it.name}" in done } } ?: 0

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
                        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
                    )
                )
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                .clickable(enabled = plan != null) { showPlanInfo = true }
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
                    DuotoneCalendarIcon(size = 22.dp, outlineColor = MaterialTheme.colorScheme.onBackground, accentColor = PastelLavender)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = plan?.title ?: "Lộ trình Tập Luyện Chuẩn 4 Tuần",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${plan?.suitableForBmi ?: "Mọi thể trạng"} • ${planGoalLabel(plan?.goal)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelMint.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Đang hoạt động", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PastelMint)
                        }
                        if (plan != null) Text("Xem chi tiết ›", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryStrong)
                    }
                }
            }
        }
        if (showPlanInfo && plan != null) {
            ModalBottomSheet(
                onDismissRequest = { showPlanInfo = false },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(plan.title, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(plan.description, fontSize = 14.sp, lineHeight = 20.sp)
                    Text("Mục tiêu: ${planGoalLabel(plan.goal)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Mức độ: ${planLevelLabel(plan.level)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Phù hợp: ${plan.suitableForBmi}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Số buổi tập mỗi tuần: ${plan.weeklySchedule.count { it.exercises.isNotEmpty() }} · " +
                            "Đã xong tuần này: $doneCount/$totalCount bài",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tick vào ô tròn cạnh từng bài khi tập xong để theo dõi tiến độ tuần. Tiến độ tự đặt lại mỗi tuần mới.",
                        fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Đang tải lộ trình tập luyện...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            schedule.forEach { dayItem ->
                val isRest = dayItem.exercises.isEmpty()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isRest) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            if (isRest) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
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
                                        .background(if (isRest) MaterialTheme.colorScheme.surfaceContainerHighest else VividOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRest) {
                                        DuotoneCalendarIcon(size = 16.dp, outlineColor = MaterialTheme.colorScheme.onSurfaceVariant, accentColor = LavenderGradientStart)
                                    } else {
                                        DuotoneDumbbellIcon(size = 16.dp, outlineColor = MaterialTheme.colorScheme.onBackground, accentColor = VividOrange)
                                    }
                                }

                                Column {
                                    Text(
                                        text = dayItem.dayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = dayItem.focus,
                                        fontSize = 12.sp,
                                        color = if (isRest) MaterialTheme.colorScheme.onSurfaceVariant else PastelMint
                                    )
                                    if (!isRest) {
                                        val dayDone = dayItem.exercises.count { "${dayItem.dayName}|${it.name}" in done }
                                        Text(
                                            text = if (dayDone == dayItem.exercises.size) "Hoàn thành buổi tập ✓" else "Đã xong $dayDone/${dayItem.exercises.size} bài",
                                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PrimaryStrong
                                        )
                                    }
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
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                dayItem.exercises.forEach { exItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val key = exItem.name.trim().lowercase()
                                                detail = libraryExercises.firstOrNull { it.name.trim().lowercase() == key }
                                                    ?: ExerciseGuideDto(
                                                        id = "plan-" + exItem.name, name = exItem.name, genderTarget = "ALL",
                                                        level = "BEGINNER", targetMuscle = exItem.targetMuscle, equipment = "NO_EQUIPMENT",
                                                        sets = exItem.sets, repsOrDuration = exItem.repsOrDuration,
                                                        restSeconds = exItem.restSeconds, caloriesBurnedEstimate = exItem.caloriesBurnedEstimate,
                                                        instructions = ExerciseInstructionsDto(
                                                            preparation = "Khởi động nhẹ các khớp trước khi bắt đầu.",
                                                            execution = exItem.instructions,
                                                            commonMistakes = "Tập quá nhanh, mất kiểm soát động tác hoặc gồng cứng người.",
                                                            breathing = "Thở đều, không nín thở."
                                                        )
                                                    )
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val doneKey = "${dayItem.dayName}|${exItem.name}"
                                        val isDone = doneKey in done
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(if (isDone) VividOrange else Color.Transparent)
                                                .border(1.5.dp, VividOrange, CircleShape)
                                                .clickable { toggleDone(doneKey) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isDone) Text("✓", fontSize = 13.sp, fontWeight = FontWeight.Black, color = OnPrimary)
                                        }
                                        Text(
                                            text = "${exItem.name} (${exItem.sets}x${exItem.repsOrDuration})",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isDone) 0.5f else 0.9f),
                                            textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("Xem ›", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryStrong)
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
    isDark: Boolean = true,
    onSelectDetail: (WorkoutLogDto) -> Unit,
    onDelete: (WorkoutLogDto) -> Unit,
    progressSection: @Composable () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        progressSection()
        // Summary Widget Hôm nay (Active Calo + Minutes + Workouts)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
                    )
                )
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "VẬN ĐỘNG HÔM NAY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${summary?.totalActiveCalories ?: 0}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryStrong
                        )
                        Text(text = "Calo đã đốt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column {
                        Text(
                            text = "${summary?.totalDurationMinutes ?: 55}p",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelMint
                        )
                        Text(text = "Thời lượng", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column {
                        Text(
                            text = "${summary?.workoutCount ?: workoutList.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelLavender
                        )
                        Text(text = "Buổi tập", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Text(
            text = "CÁC BUỔI TẬP ĐÃ HOÀN THÀNH (${workoutList.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        if (workoutList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DuotoneDumbbellIcon(size = 36.dp, outlineColor = MaterialTheme.colorScheme.onSurfaceVariant, accentColor = VividOrange)
                    Text(text = "Chưa có buổi tập nào được ghi nhận", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            workoutList.forEach { workout ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
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
                                    DuotoneEnergyIcon(size = 18.dp, outlineColor = MaterialTheme.colorScheme.onBackground, accentColor = VividOrange)
                                }

                                Column {
                                    Text(
                                        text = workout.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${workout.date.take(10)} • ${workout.durationMinutes} phút • RPE ${workout.rpe ?: 8}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    color = PrimaryStrong
                                )
                            }
                        }

                        // Volume Load và Bài tập tóm tắt
                        if (workout.exercises.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${workout.exercises.size} bài tập (${workout.exercises.sumOf { it.sets.size }} sets)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = workout.note,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
}

/**
 * TAB 3: KHO BÀI TẬP (EXERCISE LIBRARY)
 */
@Composable
fun LibraryTabContent(
    exercises: List<ExerciseGuideDto>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    isDark: Boolean = true,
    onLogExercise: (ExerciseGuideDto) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf<ExerciseGuideDto?>(null) }
    detail?.let { ex ->
        com.calai.app.presentation.components.ExerciseDetailSheet(exercise = ex, onDismiss = { detail = null }, onLog = { onLogExercise(ex) })
    }
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
            placeholder = { Text("Tìm bài tập theo tên hoặc nhóm cơ...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VividOrange,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        )

        if (exercises.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PrimaryStrong, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    Text("Đang tải kho bài tập...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(
                        errorMessage ?: "Chưa có bài tập nào trong kho",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRetry) { Text("Thử lại") }
                }
            }
        } else if (filtered.isEmpty()) {
            Text(
                "Không có bài tập khớp \"$searchQuery\"",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { ex ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .clickable { detail = ex }
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VividOrange.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = PrimaryStrong, modifier = Modifier.size(22.dp))
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = ex.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = ex.targetMuscle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            ExerciseChip(exerciseLevelLabel(ex.level))
                            ExerciseChip(exerciseEquipmentLabel(ex.equipment))
                            ExerciseChip("${ex.sets} hiệp · ${ex.repsOrDuration}")
                        }

                        Text(
                            text = ex.instructions.execution,
                            fontSize = 12.5.sp,
                            color = (MaterialTheme.colorScheme.onBackground).copy(alpha = 0.8f),
                            lineHeight = 17.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Xem hướng dẫn chi tiết & video →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.calai.app.presentation.theme.PrimaryStrong
                        )

                        Button(
                            onClick = { onLogExercise(ex) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Ghi buổi tập với bài này", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
                        }
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
    isDark: Boolean = true,
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${workout.date.take(10)} • ${workout.durationMinutes} phút • ${workout.caloriesBurned.toInt()} kcal",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            workout.exercises.forEach { ex ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = ex.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ex.sets.forEach { s ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
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
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Ghi chú: ${workout.note}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                )
            }
        }

        AppButton(
            text = "Đóng",
            onClick = onClose,
            height = 48.dp,
            shape = RoundedCornerShape(14.dp)
        )
    }
}


@Composable
private fun ExerciseChip(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private fun exerciseLevelLabel(level: String) = when (level) {
    "BEGINNER" -> "Dễ"
    "INTERMEDIATE" -> "Vừa"
    "ADVANCED" -> "Nâng cao"
    else -> level
}

private fun exerciseEquipmentLabel(equipment: String) = when (equipment) {
    "NO_EQUIPMENT" -> "Không dụng cụ"
    "DUMBBELL" -> "Tạ đơn"
    "GYM_MACHINE" -> "Máy tập"
    "RESISTANCE_BAND" -> "Dây kháng lực"
    else -> equipment
}

private fun planGoalLabel(goal: String?): String = when (goal) {
    "LOSE_WEIGHT" -> "Giảm cân / đốt mỡ"
    "GAIN_WEIGHT", "GAIN_MUSCLE", "BUILD_MUSCLE" -> "Tăng cân / tăng cơ"
    "MAINTAIN" -> "Duy trì thể trạng"
    null, "" -> "Tăng cơ & giảm mỡ"
    else -> goal
}

private fun planLevelLabel(level: String): String = when (level) {
    "BEGINNER" -> "Người mới (dễ)"
    "INTERMEDIATE" -> "Trung bình"
    "ADVANCED" -> "Nâng cao"
    else -> level
}
