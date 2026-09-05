package com.calai.app.presentation.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.DayWorkoutPlanDto
import com.calai.app.data.remote.dto.DietMealsDto
import com.calai.app.data.remote.dto.DietRecommendationData
import com.calai.app.data.remote.dto.ExerciseGuideDto
import com.calai.app.data.remote.dto.MonthlyDietData
import com.calai.app.data.remote.dto.WorkoutRecommendationData
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.SuggestionsViewModel

/**
 * Màn hình Gợi Ý Cho Bạn (AI Recommendations) - Màn hình ưu tiên số 9 (CODING_RULES.md 10.2)
 * Tuân thủ quy tắc:
 * - Không dùng emoji làm icon UI
 * - Thẻ Thực đơn Bento Pastel gradient 2 tông dịu + Specular highlight góc trên-trái
 * - Tag pill dinh dưỡng dạng Glassmorphism
 * - Badge lộ trình tập dùng VividOrangeSoft alpha 15-20% + VividOrangeLight
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    onBack: () -> Unit,
    viewModel: SuggestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ObsidianBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gợi ý cho bạn",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 19.sp,
                        letterSpacing = (-0.3).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBackground)
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.diet == null && uiState.workout == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VividOrange)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 1. HEADER SECTION THỰC ĐƠN PHÙ HỢP
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DuotoneDietIcon(size = 24.dp, outlineColor = TextWhite, accentColor = VividOrange)
                    Text(
                        text = "Thực đơn phù hợp mục tiêu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = (-0.3).sp
                    )
                }
            }

            // Thẻ thực đơn Bento Pastel Đá Quý
            uiState.diet?.let { item { DietCard(it) } }

            // Lịch thực đơn nhiều ngày (Custom Duotone Calendar Icon)
            uiState.monthlyDiet?.let { monthly ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CharcoalSurface)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
                            .clickable { viewModel.toggleMonthlyView() }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DuotoneCalendarIcon(size = 20.dp, outlineColor = TextLightGrey, accentColor = LavenderGradientStart)
                            Text(
                                "Xem thực đơn ${monthly.totalDays ?: monthly.monthlyPlans?.size ?: 0} ngày",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                        Icon(
                            if (uiState.showMonthlyDiet) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (uiState.showMonthlyDiet) {
                    item {
                        MonthlyDietSection(
                            data = monthly,
                            selectedDay = uiState.selectedDayNumber,
                            onSelectDay = viewModel::selectDay
                        )
                    }
                }
            }

            // 2. HEADER SECTION LỘ TRÌNH TẬP LUYỆN
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DuotoneWorkoutIcon(size = 24.dp, outlineColor = TextWhite, accentColor = VividOrange)
                    Text(
                        text = "Lộ trình tập luyện gợi ý",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = (-0.3).sp
                    )
                }
            }

            uiState.workout?.let {
                item {
                    WorkoutCard(
                        data = it,
                        expandedDayName = uiState.expandedDayName,
                        onToggleDay = viewModel::toggleDayExpand
                    )
                }
            }

            // 3. HEADER SECTION KHO BÀI TẬP
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DuotoneExerciseIcon(size = 24.dp, outlineColor = TextWhite, accentColor = VividOrange)
                    Text(
                        text = "Kho bài tập chuẩn",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = (-0.3).sp
                    )
                }
            }

            item {
                ExerciseFilters(
                    selectedGender = uiState.selectedGender,
                    selectedLevel = uiState.selectedLevel,
                    onGenderSelect = viewModel::selectGender,
                    onLevelSelect = viewModel::selectLevel
                )
            }

            items(uiState.exercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    isExpanded = uiState.expandedExerciseId == exercise.id,
                    onClick = { viewModel.toggleExerciseExpand(exercise.id) }
                )
            }
        }
    }
}

@Composable
private fun DietCard(data: DietRecommendationData) {
    val plan = data.recommendedPlan
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(ProteinBrush)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        // Specular radial highlight góc trên-trái (Spec 10.2 & 10.4)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(28.dp.toPx(), 28.dp.toPx()),
                    radius = 65.dp.toPx()
                ),
                radius = 65.dp.toPx(),
                center = Offset(28.dp.toPx(), 28.dp.toPx())
            )
        }

        Column {
            Text(
                text = plan.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = TextDeepInk,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plan.description,
                fontSize = 13.sp,
                color = TextDeepInk.copy(alpha = 0.75f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Tag dinh dưỡng dạng Glassmorphism Pill (Spec 10.2)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassStatPill("${plan.targetCalo.toInt()} kcal")
                GlassStatPill("P ${plan.macroRatio.proteinPercent}%")
                GlassStatPill("C ${plan.macroRatio.carbPercent}%")
                GlassStatPill("F ${plan.macroRatio.fatPercent}%")
            }

            Spacer(modifier = Modifier.height(16.dp))

            MealBlocksList(plan.meals)

            if (data.availableOptions.size > 1) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Lựa chọn khác:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDeepInk.copy(alpha = 0.65f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.availableOptions.filter { it.id != plan.id }.forEach { option ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TextDeepInk.copy(alpha = 0.08f))
                                .border(0.75.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "${option.title} · ${option.targetCalo.toInt()} kcal",
                                fontSize = 11.5.sp,
                                color = TextDeepInk,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassStatPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TextDeepInk.copy(alpha = 0.09f))
            .border(0.75.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextDeepInk
        )
    }
}

@Composable
private fun MealBlocksList(meals: DietMealsDto, textColor: Color = TextDeepInk) {
    listOfNotNull(meals.breakfast, meals.lunch, meals.dinner, meals.snack).forEach { block ->
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = block.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "${block.totalCalories.toInt()} kcal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            block.items.forEach { food ->
                Text(
                    text = "• ${food.name} (${food.serving})",
                    fontSize = 12.5.sp,
                    color = textColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun MonthlyDietSection(
    data: MonthlyDietData,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit
) {
    val plans = data.monthlyPlans ?: emptyList()
    val currentPlan = plans.find { it.dayNumber == selectedDay } ?: plans.firstOrNull()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            plans.forEach { day ->
                val isSelected = day.dayNumber == (currentPlan?.dayNumber ?: -1)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) VividOrange else CharcoalSurface)
                        .border(1.dp, if (isSelected) VividOrangeLight else CharcoalBorder, RoundedCornerShape(12.dp))
                        .clickable { onSelectDay(day.dayNumber) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        "Ngày ${day.dayNumber}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) TextWhite else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        currentPlan?.let { plan ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(CharcoalCardElevated)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        plan.dayTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(plan.focusMessage, fontSize = 12.5.sp, color = TextMuted, lineHeight = 17.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStatPillDark("${plan.targetCalories.toInt()} kcal")
                        MiniStatPillDark("P ${plan.macroSummary.proteinGrams.toInt()}g")
                        MiniStatPillDark("C ${plan.macroSummary.carbGrams.toInt()}g")
                        MiniStatPillDark("F ${plan.macroSummary.fatGrams.toInt()}g")
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    MealBlocksList(plan.meals, textColor = TextLightGrey)
                }
            }
        }
    }
}

@Composable
private fun MiniStatPillDark(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalCard)
            .border(0.75.dp, CharcoalBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextWhite)
    }
}

@Composable
private fun WorkoutCard(
    data: WorkoutRecommendationData,
    expandedDayName: String?,
    onToggleDay: (String) -> Unit
) {
    val plan = data.recommendedWorkout
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CharcoalCardElevated)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = plan.title,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(plan.description, fontSize = 12.5.sp, color = TextMuted, lineHeight = 17.sp)
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

            plan.weeklySchedule.forEach { day ->
                DayRow(
                    day = day,
                    isExpanded = expandedDayName == day.dayName,
                    onClick = { onToggleDay(day.dayName) }
                )
            }
        }
    }
}

@Composable
private fun DayRow(day: DayWorkoutPlanDto, isExpanded: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalCard)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .padding(bottom = if (isExpanded) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(day.dayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(
                    text = if (day.exercises.isEmpty()) day.focus else "${day.focus} · ${day.estimatedMinutes} phút",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            if (day.exercises.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            day.exercises.forEach { ex ->
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "${ex.name} — ${ex.sets}x${ex.repsOrDuration}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextLightGrey
                    )
                    Text(
                        text = ex.instructions,
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
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
    onGenderSelect: (String) -> Unit,
    onLevelSelect: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterPill("Nam", selectedGender == "MALE") { onGenderSelect("MALE") }
            FilterPill("Nữ", selectedGender == "FEMALE") { onGenderSelect("FEMALE") }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill("Tất cả", selectedLevel == null) { onLevelSelect(null) }
            FilterPill("Dễ", selectedLevel == "BEGINNER") { onLevelSelect("BEGINNER") }
            FilterPill("Vừa", selectedLevel == "INTERMEDIATE") { onLevelSelect("INTERMEDIATE") }
            FilterPill("Nâng cao", selectedLevel == "ADVANCED") { onLevelSelect("ADVANCED") }
        }
    }
}

@Composable
private fun FilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) VividOrange else CharcoalCard)
            .border(1.dp, if (isSelected) VividOrangeLight else CharcoalBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextWhite else TextMuted
        )
    }
}

@Composable
private fun ExerciseCard(exercise: ExerciseGuideDto, isExpanded: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CharcoalCardElevated)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
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
                        color = TextWhite
                    )
                    Text(
                        text = "${exercise.targetMuscle} • ${exercise.sets}x${exercise.repsOrDuration}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                ExerciseInstructionRow("Chuẩn bị", exercise.instructions.preparation)
                ExerciseInstructionRow("Thực hiện", exercise.instructions.execution)
                ExerciseInstructionRow("Lỗi thường gặp", exercise.instructions.commonMistakes)
                ExerciseInstructionRow("Hít thở", exercise.instructions.breathing)
            }
        }
    }
}

@Composable
private fun ExerciseInstructionRow(label: String, content: String) {
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
            color = TextLightGrey,
            lineHeight = 16.sp
        )
    }
}

