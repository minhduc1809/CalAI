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
import com.calai.app.data.remote.dto.DietMealsDto
import com.calai.app.data.remote.dto.DietRecommendationData
import com.calai.app.data.remote.dto.ExerciseGuideDto
import com.calai.app.data.remote.dto.MonthlyDietData
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
    isDarkTheme: Boolean = true,
    viewModel: SuggestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = if (isDarkTheme) ObsidianBackground else IvoryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gợi ý cho bạn",
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary,
                        fontSize = 19.sp,
                        letterSpacing = (-0.3).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = if (isDarkTheme) TextWhite else TextInkPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkTheme) ObsidianBackground else IvoryBackground
                )
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
                // 1. SECTION THỰC ĐƠN PHÙ HỢP
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DuotoneDietIcon(size = 24.dp, outlineColor = if (isDarkTheme) TextWhite else TextInkPrimary, accentColor = VividOrange)
                        Text(
                            text = "Thực đơn phù hợp mục tiêu",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) TextWhite else TextInkPrimary,
                            letterSpacing = (-0.3).sp
                        )
                    }
                }

                // Thẻ thực đơn Bento Pastel Đá Quý với tab bữa ăn gọn gàng (Anti-crowding)
                uiState.diet?.let { item { DietCard(it, isDarkTheme = isDarkTheme) } }

                // Lịch thực đơn nhiều ngày
                uiState.monthlyDiet?.let { monthly ->
                    item {
                        val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = if (isDarkTheme) 3.dp else 6.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    ambientColor = shadowColor,
                                    spotColor = shadowColor
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                                .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(18.dp))
                                .clickable { viewModel.toggleMonthlyView() }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DuotoneCalendarIcon(size = 20.dp, outlineColor = if (isDarkTheme) TextLightGrey else TextInkSecondary, accentColor = LavenderGradientStart)
                                Text(
                                    "Xem thực đơn ${monthly.totalDays ?: monthly.monthlyPlans?.size ?: 0} ngày",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkTheme) TextWhite else TextInkPrimary
                                )
                            }
                            Icon(
                                if (uiState.showMonthlyDiet) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = if (isDarkTheme) TextMuted else TextInkMuted,
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

                // 2. SECTION LỘ TRÌNH TẬP LUYỆN
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DuotoneWorkoutIcon(size = 24.dp, outlineColor = if (isDarkTheme) TextWhite else TextInkPrimary, accentColor = VividOrange)
                        Text(
                            text = "Lộ trình tập luyện gợi ý",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) TextWhite else TextInkPrimary,
                            letterSpacing = (-0.3).sp
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
                        DuotoneExerciseIcon(size = 24.dp, outlineColor = if (isDarkTheme) TextWhite else TextInkPrimary, accentColor = VividOrange)
                        Text(
                            text = "Kho bài tập chuẩn",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) TextWhite else TextInkPrimary,
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
private fun DietCard(data: DietRecommendationData, isDarkTheme: Boolean = true) {
    val plan = data.recommendedPlan
    var selectedMealType by remember { mutableStateOf("BREAKFAST") }
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 6.dp else 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(28.dp))
            .background(if (isDarkTheme) ProteinBrush else ProteinBrushLight)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDarkTheme) 0.35f else 0.65f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(20.dp)
    ) {
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

            // Tab chọn nhanh bữa ăn (Chống dồn text dày đặc - Spec 10.5)
            val mealTabs = listOf(
                "BREAKFAST" to "Sáng",
                "LUNCH" to "Trưa",
                "DINNER" to "Tối",
                "SNACK" to "Phụ"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TextDeepInk.copy(alpha = 0.08f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                mealTabs.forEach { (type, label) ->
                    val isTabActive = selectedMealType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isTabActive) Color.White else Color.Transparent)
                            .clickable { selectedMealType = type }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isTabActive) FontWeight.Black else FontWeight.SemiBold,
                            color = TextDeepInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chi tiết bữa ăn đang chọn
            val activeMealBlock = when (selectedMealType) {
                "BREAKFAST" -> plan.meals.breakfast
                "LUNCH" -> plan.meals.lunch
                "DINNER" -> plan.meals.dinner
                else -> plan.meals.snack
            }

            activeMealBlock?.let { block ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.45f))
                        .border(0.75.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = block.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDeepInk
                            )
                            Text(
                                text = "${block.totalCalories.toInt()} kcal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = TextDeepInk
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        block.items.forEach { food ->
                            Text(
                                text = "• ${food.name} (${food.serving})",
                                fontSize = 12.5.sp,
                                color = TextDeepInk.copy(alpha = 0.8f),
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            if (data.availableOptions.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lựa chọn thực đơn khác:",
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
            .background(if (isDarkTheme) CharcoalCardElevated else PearlCard)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.14f) else Color.White,
                        if (isDarkTheme) CharcoalBorder else PearlBorder
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
                color = if (isDarkTheme) TextWhite else TextInkPrimary,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(plan.description, fontSize = 12.5.sp, color = if (isDarkTheme) TextMuted else TextInkMuted, lineHeight = 17.sp)
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
            .background(if (isDarkTheme) CharcoalCard else PearlCard)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White,
                        if (isDarkTheme) CharcoalBorder else PearlBorder
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
                Text(day.dayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) TextWhite else TextInkPrimary)
                Text(
                    text = if (day.exercises.isEmpty()) day.focus else "${day.focus} · ${day.estimatedMinutes} phút",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) TextMuted else TextInkMuted
                )
            }
            if (day.exercises.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (isDarkTheme) TextMuted else TextInkMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                HorizontalDivider(color = if (isDarkTheme) CharcoalBorder else PearlBorder, thickness = 0.75.dp)
                Spacer(modifier = Modifier.height(8.dp))
                day.exercises.forEach { ex ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "${ex.name} — ${ex.sets}x${ex.repsOrDuration}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) TextLightGrey else TextInkPrimary
                        )
                        Text(
                            text = ex.instructions,
                            fontSize = 11.5.sp,
                            color = if (isDarkTheme) TextMuted else TextInkMuted,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (day.exercises.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onStartWorkout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DuotoneWorkoutIcon(size = 16.dp, outlineColor = TextWhite, accentColor = TextWhite)
                            Text("Bắt đầu & Ghi buổi tập này", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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
            .background(if (isSelected) VividOrange else if (isDarkTheme) CharcoalCard else PearlCard)
            .border(
                1.dp,
                if (isSelected) VividOrangeLight else if (isDarkTheme) CharcoalBorder else PearlBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextWhite else if (isDarkTheme) TextMuted else TextInkMuted
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
            .background(if (isDarkTheme) CharcoalCardElevated else PearlCard)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White,
                        if (isDarkTheme) CharcoalBorder else PearlBorder
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
                        color = if (isDarkTheme) TextWhite else TextInkPrimary
                    )
                    Text(
                        text = "${exercise.targetMuscle} • ${exercise.sets}x${exercise.repsOrDuration}",
                        fontSize = 12.sp,
                        color = if (isDarkTheme) TextMuted else TextInkMuted
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (isDarkTheme) TextMuted else TextInkMuted,
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
                Button(
                    onClick = onLogWorkout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DuotoneWorkoutIcon(size = 15.dp, outlineColor = TextWhite, accentColor = TextWhite)
                        Text("+ Ghi bài tập này vào buổi tập", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
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
            color = if (isDarkTheme) TextLightGrey else TextInkSecondary,
            lineHeight = 16.sp
        )
    }
}


