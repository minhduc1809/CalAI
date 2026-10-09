package com.calai.app.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.DayWorkoutPlanDto
import com.calai.app.data.remote.dto.ExerciseGuideDto
import com.calai.app.R
import com.calai.app.data.remote.dto.WorkoutRecommendationData
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.SuggestionsViewModel

/**
 * Màn hình Gợi Ý Cho Bạn (AI Recommendations) - Tuân thủ CODING_RULES.md 10.2 & 10.5
 * - Chống nhồi chữ (Anti-crowding): Tab chọn bữa ăn trực quan, preview lịch tập gọn gàng
 * - Quầng sáng mờ Ambient Glow nền phá vỡ khối đen đặc
 * - Vector Duotone Icons rõ ràng, không emoji, không vỡ nét
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    onBack: () -> Unit,
    onNavigateToLogWorkout: () -> Unit = {},
    onNavigateToWorkoutHub: () -> Unit = {},
    onOpenPremium: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    isDarkTheme: Boolean = true,
    viewModel: SuggestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gợi ý cho bạn",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 19.sp,
                        letterSpacing = (-0.3).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.workout == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CuteLoadingIndicator(rawResId = R.raw.loading_general, size = 96.dp)
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Ambient glow nền góc trên-phải (Spec 10.5 - Chống bệt đen màn hình)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ProteinGradientStart.copy(alpha = if (isDarkTheme) 0.08f else 0.03f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.15f),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VividOrange.copy(alpha = if (isDarkTheme) 0.06f else 0.02f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.55f),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.15f, size.height * 0.55f)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp) // Khoảng thở rộng rãi (Spec 10.5)
            ) {
                // 1. THỰC ĐƠN HÔM NAY + KẾ HOẠCH 7/30 NGÀY (từ kho món an toàn, đã lọc dị ứng và chế độ ăn)
                item {
                    MealPlanSection(
                        onOpenPremium = onOpenPremium,
                        onOpenProfile = onOpenProfile
                    )
                }

                // 2. SECTION LỘ TRÌNH TẬP LUYỆN
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DuotoneWorkoutIcon(size = 24.dp, outlineColor = MaterialTheme.colorScheme.onBackground, accentColor = VividOrange)
                            Text(
                                text = "Lộ trình tập luyện gợi ý",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.3).sp
                            )
                        }
                        Text(
                            text = "Workout Hub →",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividOrange,
                            modifier = Modifier.clickable { onNavigateToWorkoutHub() }
                        )
                    }
                }

                uiState.workout?.let {
                    item {
                        WorkoutCard(
                            data = it,
                            isDarkTheme = isDarkTheme,
                            expandedDayName = uiState.expandedDayName,
                            onToggleDay = viewModel::toggleDayExpand,
                            onStartWorkout = onNavigateToLogWorkout
                        )
                    }
                }

                // 3. SECTION KHO BÀI TẬP
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DuotoneExerciseIcon(size = 24.dp, outlineColor = MaterialTheme.colorScheme.onBackground, accentColor = VividOrange)
                        Text(
                            text = "Kho bài tập chuẩn",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.3).sp
                        )
                    }
                }

                item {
                    ExerciseFilters(
                        selectedGender = uiState.selectedGender,
                        selectedLevel = uiState.selectedLevel,
                        isDarkTheme = isDarkTheme,
                        onGenderSelect = viewModel::selectGender,
                        onLevelSelect = viewModel::selectLevel
                    )
                }

                items(uiState.exercises) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        isDarkTheme = isDarkTheme,
                        isExpanded = uiState.expandedExerciseId == exercise.id,
                        onClick = { viewModel.toggleExerciseExpand(exercise.id) },
                        onLogWorkout = onNavigateToLogWorkout
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    data: WorkoutRecommendationData,
    expandedDayName: String?,
    isDarkTheme: Boolean = true,
    onToggleDay: (String) -> Unit,
    onStartWorkout: () -> Unit
) {
    val plan = data.recommendedWorkout
    var showAllDays by remember { mutableStateOf(false) }
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 6.dp else 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.14f) else Color.White,
                        MaterialTheme.colorScheme.outline
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = plan.title,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(plan.description, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Badge trạng thái chuẩn Spec 10.2 (VividOrangeSoft 15-20% + VividOrangeLight)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VividOrangeSoft)
                    .border(0.75.dp, VividOrange.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Phù hợp: ${plan.suitableForBmi}",
                    color = VividOrangeLight,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chỉ hiển thị 2 ngày đầu làm preview, còn lại ẩn sau nút mở rộng (Spec 10.5)
            val displayedDays = if (showAllDays) plan.weeklySchedule else plan.weeklySchedule.take(2)

            displayedDays.forEach { day ->
                DayRow(
                    day = day,
                    isDarkTheme = isDarkTheme,
                    isExpanded = expandedDayName == day.dayName,
                    onClick = { onToggleDay(day.dayName) },
                    onStartWorkout = onStartWorkout
                )
            }

            if (plan.weeklySchedule.size > 2) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { showAllDays = !showAllDays },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showAllDays) "Thu gọn lịch tập" else "Xem đầy đủ (${plan.weeklySchedule.size} ngày)",
                        color = VividOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DayRow(
    day: DayWorkoutPlanDto,
    isDarkTheme: Boolean = true,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onStartWorkout: () -> Unit = {}
) {
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 3.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White,
                        MaterialTheme.colorScheme.outline
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(day.dayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = if (day.exercises.isEmpty()) day.focus else "${day.focus} · ${day.estimatedMinutes} phút",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (day.exercises.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(8.dp))
                day.exercises.forEach { ex ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "${ex.name} — ${ex.sets}x${ex.repsOrDuration}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = ex.instructions,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (day.exercises.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    AppButton(
                        text = "Bắt đầu & Ghi buổi tập này",
                        onClick = onStartWorkout,
                        leadingIcon = { DuotoneWorkoutIcon(size = 16.dp, outlineColor = TextWhite, accentColor = TextWhite) },
                        height = 40.dp,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ExerciseFilters(
    selectedGender: String,
    selectedLevel: String?,
    isDarkTheme: Boolean = true,
    onGenderSelect: (String) -> Unit,
    onLevelSelect: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterPill("Nam", selectedGender == "MALE", isDarkTheme) { onGenderSelect("MALE") }
            FilterPill("Nữ", selectedGender == "FEMALE", isDarkTheme) { onGenderSelect("FEMALE") }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill("Tất cả", selectedLevel == null, isDarkTheme) { onLevelSelect(null) }
            FilterPill("Dễ", selectedLevel == "BEGINNER", isDarkTheme) { onLevelSelect("BEGINNER") }
            FilterPill("Vừa", selectedLevel == "INTERMEDIATE", isDarkTheme) { onLevelSelect("INTERMEDIATE") }
            FilterPill("Nâng cao", selectedLevel == "ADVANCED", isDarkTheme) { onLevelSelect("ADVANCED") }
        }
    }
}

@Composable
private fun FilterPill(label: String, isSelected: Boolean, isDarkTheme: Boolean = true, onClick: () -> Unit) {
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    Box(
        modifier = Modifier
            .shadow(
                elevation = if (isSelected) 4.dp else 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) VividOrange else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (isSelected) VividOrangeLight else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseGuideDto,
    isExpanded: Boolean,
    isDarkTheme: Boolean = true,
    onClick: () -> Unit,
    onLogWorkout: () -> Unit = {}
) {
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 4.dp else 8.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White,
                        MaterialTheme.colorScheme.outline
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${exercise.targetMuscle} • ${exercise.sets}x${exercise.repsOrDuration}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                ExerciseInstructionRow("Chuẩn bị", exercise.instructions.preparation, isDarkTheme)
                ExerciseInstructionRow("Thực hiện", exercise.instructions.execution, isDarkTheme)
                ExerciseInstructionRow("Lỗi thường gặp", exercise.instructions.commonMistakes, isDarkTheme)
                ExerciseInstructionRow("Hít thở", exercise.instructions.breathing, isDarkTheme)

                Spacer(modifier = Modifier.height(10.dp))
                AppButton(
                    text = "+ Ghi bài tập này vào buổi tập",
                    onClick = onLogWorkout,
                    leadingIcon = { DuotoneWorkoutIcon(size = 15.dp, outlineColor = TextWhite, accentColor = TextWhite) },
                    height = 38.dp,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

@Composable
private fun ExerciseInstructionRow(label: String, content: String, isDarkTheme: Boolean = true) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = VividOrangeLight
        )
        Text(
            text = content,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            lineHeight = 16.sp
        )
    }
}


