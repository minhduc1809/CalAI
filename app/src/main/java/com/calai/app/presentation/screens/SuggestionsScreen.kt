package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.DayWorkoutPlanDto
import com.calai.app.data.remote.dto.DietRecommendationData
import com.calai.app.data.remote.dto.ExerciseGuideDto
import com.calai.app.data.remote.dto.WorkoutRecommendationData
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.SuggestionsViewModel

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
                title = { Text("Gợi ý cho bạn", fontWeight = FontWeight.Bold, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBackground)
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.diet == null && uiState.workout == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VividOrange)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { SectionLabel("🍽️ Thực đơn phù hợp mục tiêu") }
            uiState.diet?.let { item { DietCard(it) } }

            item { SectionLabel("🏋️ Lộ trình tập luyện gợi ý") }
            uiState.workout?.let {
                item {
                    WorkoutCard(
                        data = it,
                        expandedDayName = uiState.expandedDayName,
                        onToggleDay = viewModel::toggleDayExpand
                    )
                }
            }

            item { SectionLabel("💪 Kho bài tập") }
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
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
}

@Composable
private fun DietCard(data: DietRecommendationData) {
    val plan = data.recommendedPlan
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PastelMint)
            .padding(18.dp)
    ) {
        Column {
            Text(plan.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
            Spacer(modifier = Modifier.height(4.dp))
            Text(plan.description, fontSize = 12.5.sp, color = TextDeepInk.copy(alpha = 0.75f), lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStatPill("${plan.targetCalo.toInt()} kcal", TextDeepInk)
                MiniStatPill("P ${plan.macroRatio.proteinPercent}%", TextDeepInk)
                MiniStatPill("C ${plan.macroRatio.carbPercent}%", TextDeepInk)
                MiniStatPill("F ${plan.macroRatio.fatPercent}%", TextDeepInk)
            }

            Spacer(modifier = Modifier.height(14.dp))

            listOfNotNull(plan.meals.breakfast, plan.meals.lunch, plan.meals.dinner, plan.meals.snack).forEach { block ->
                Column(modifier = Modifier.padding(bottom = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(block.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
                        Text("${block.totalCalories.toInt()} kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDeepInk.copy(alpha = 0.7f))
                    }
                    block.items.forEach { food ->
                        Text(
                            "• ${food.name} (${food.serving})",
                            fontSize = 12.sp,
                            color = TextDeepInk.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            if (data.availableOptions.size > 1) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Lựa chọn khác:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextDeepInk.copy(alpha = 0.6f))
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("${option.title} · ${option.targetCalo.toInt()} kcal", fontSize = 11.sp, color = TextDeepInk, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatPill(text: String, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.4f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
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
            .clip(RoundedCornerShape(22.dp))
            .background(CharcoalSurface)
            .padding(18.dp)
    ) {
        Column {
            Text(plan.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(4.dp))
            Text(plan.description, fontSize = 12.5.sp, color = TextMuted, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = VividOrange.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    "Phù hợp: ${plan.suitableForBmi}",
                    color = VividOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            plan.weeklySchedule.forEach { day ->
                DayRow(day = day, isExpanded = expandedDayName == day.dayName, onClick = { onToggleDay(day.dayName) })
            }
        }
    }
}

@Composable
private fun DayRow(day: DayWorkoutPlanDto, isExpanded: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalCard)
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
                Text(day.dayName, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(
                    if (day.exercises.isEmpty()) day.focus else "${day.focus} · ${day.estimatedMinutes} phút",
                    fontSize = 11.5.sp,
                    color = TextMuted
                )
            }
            if (day.exercises.isNotEmpty()) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(10.dp))
            day.exercises.forEach { ex ->
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("${ex.name} — ${ex.sets}x${ex.repsOrDuration}", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextLightGrey)
                    Text(ex.instructions, fontSize = 11.5.sp, color = TextMuted, lineHeight = 16.sp)
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
            .background(if (isSelected) VividOrange else CharcoalSurface)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            label,
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
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalSurface)
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
                    Text(exercise.name, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(
                        "${exercise.targetMuscle} • ${exercise.sets}x${exercise.repsOrDuration}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
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
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VividOrange)
        Text(content, fontSize = 12.sp, color = TextLightGrey, lineHeight = 16.sp)
    }
}
