package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.MacroStyleOptionRow
import com.calai.app.presentation.components.RateSelectionPill
import com.calai.app.presentation.components.SelectionPill
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ONBOARDING_STEP_COUNT
import com.calai.app.presentation.viewmodel.OnboardingUiState
import com.calai.app.presentation.viewmodel.OnboardingViewModel

private val STEP_TITLES = listOf(
    "Bạn là ai?",
    "Số đo cơ thể",
    "Mức độ vận động",
    "Mục tiêu của bạn",
    "Phong cách ăn uống"
)

/**
 * Onboarding Wizard — thu thập hồ sơ ban đầu ngay sau khi Đăng ký (BRD: ONBOARDING > Wizard UX).
 * 5 bước + 1 màn Hoàn tất, dùng chung PATCH /users/me (1 lần submit ở bước cuối).
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bg = if (isDarkTheme) ObsidianBackground else IvoryBackground
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

    Scaffold(containerColor = bg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            if (uiState.isCompleted) {
                OnboardingSummaryStep(
                    uiState = uiState,
                    isDarkTheme = isDarkTheme,
                    onFinish = onFinished,
                    modifier = Modifier.weight(1f)
                )
                return@Scaffold
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress bar theo bước (Wizard UX)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(ONBOARDING_STEP_COUNT) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (index <= uiState.currentStep) VividOrange
                                else (if (isDarkTheme) CharcoalCard else PearlCard)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Bước ${uiState.currentStep + 1}/$ONBOARDING_STEP_COUNT",
                fontSize = 12.sp,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                STEP_TITLES.getOrElse(uiState.currentStep) { "" },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (uiState.currentStep) {
                    0 -> UserProfileStep(uiState, isDarkTheme, viewModel)
                    1 -> BodyMetricsStep(uiState, isDarkTheme, viewModel)
                    2 -> ActivityStep(uiState, isDarkTheme, viewModel)
                    3 -> GoalStep(uiState, isDarkTheme, viewModel)
                    4 -> ProgramStep(uiState, isDarkTheme, viewModel)
                }

                uiState.errorMessage?.let {
                    Text(it, color = CoralWarning, fontSize = 12.5.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.currentStep > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Quay lại", color = textSecondary, fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = { viewModel.nextStep() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextWhite)
                    } else {
                        Text(
                            if (uiState.currentStep == ONBOARDING_STEP_COUNT - 1) "Hoàn tất" else "Tiếp tục",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepLabel(text: String, isDarkTheme: Boolean) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isDarkTheme) TextWhite else TextInkPrimary)
}

@Composable
private fun onboardingFieldColors(isDarkTheme: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = if (isDarkTheme) CharcoalSurface else PearlCard,
    unfocusedContainerColor = if (isDarkTheme) CharcoalSurface else PearlCard,
    focusedBorderColor = VividOrange,
    unfocusedBorderColor = if (isDarkTheme) CharcoalBorder else PearlBorder,
    focusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary,
    unfocusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary
)

// --- Bước 1: User Profile ---
@Composable
private fun UserProfileStep(uiState: OnboardingUiState, isDarkTheme: Boolean, viewModel: OnboardingViewModel) {
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    fun syncDate() {
        if (day.isNotBlank() && month.isNotBlank() && year.length == 4) {
            viewModel.setDateOfBirth("$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepLabel("Giới tính sinh học của bạn là gì?", isDarkTheme)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionPill("Nam", uiState.gender == "MALE", isDarkTheme, Modifier.weight(1f)) { viewModel.selectGender("MALE") }
            SelectionPill("Nữ", uiState.gender == "FEMALE", isDarkTheme, Modifier.weight(1f)) { viewModel.selectGender("FEMALE") }
        }

        StepLabel("Ngày sinh của bạn?", isDarkTheme)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = day,
                onValueChange = { day = it.filter { c -> c.isDigit() }.take(2); syncDate() },
                placeholder = { Text("Ngày", color = if (isDarkTheme) TextMuted else TextInkMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = onboardingFieldColors(isDarkTheme)
            )
            OutlinedTextField(
                value = month,
                onValueChange = { month = it.filter { c -> c.isDigit() }.take(2); syncDate() },
                placeholder = { Text("Tháng", color = if (isDarkTheme) TextMuted else TextInkMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = onboardingFieldColors(isDarkTheme)
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it.filter { c -> c.isDigit() }.take(4); syncDate() },
                placeholder = { Text("Năm", color = if (isDarkTheme) TextMuted else TextInkMuted) },
                singleLine = true,
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(14.dp),
                colors = onboardingFieldColors(isDarkTheme)
            )
        }
    }
}

// --- Bước 2: Body Metrics ---
@Composable
private fun BodyMetricsStep(uiState: OnboardingUiState, isDarkTheme: Boolean, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepLabel("Chiều cao của bạn?", isDarkTheme)
        OutlinedTextField(
            value = uiState.heightCm,
            onValueChange = { viewModel.setHeightCm(it) },
            placeholder = { Text("VD: 170", color = if (isDarkTheme) TextMuted else TextInkMuted) },
            suffix = { Text("cm", color = if (isDarkTheme) TextMuted else TextInkMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = onboardingFieldColors(isDarkTheme)
        )

        StepLabel("Cân nặng hiện tại của bạn?", isDarkTheme)
        OutlinedTextField(
            value = uiState.weightKg,
            onValueChange = { viewModel.setWeightKg(it) },
            placeholder = { Text("VD: 65", color = if (isDarkTheme) TextMuted else TextInkMuted) },
            suffix = { Text("kg", color = if (isDarkTheme) TextMuted else TextInkMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = onboardingFieldColors(isDarkTheme)
        )

        StepLabel("Mức mỡ cơ thể của bạn khoảng bao nhiêu? (tuỳ chọn)", isDarkTheme)
        OutlinedTextField(
            value = uiState.bodyFatPercent,
            onValueChange = { viewModel.setBodyFatPercent(it) },
            placeholder = { Text("Bỏ qua nếu chưa biết", color = if (isDarkTheme) TextMuted else TextInkMuted) },
            suffix = { Text("%", color = if (isDarkTheme) TextMuted else TextInkMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = onboardingFieldColors(isDarkTheme)
        )
    }
}

// --- Bước 3: Activity Info ---
@Composable
private fun ActivityStep(uiState: OnboardingUiState, isDarkTheme: Boolean, viewModel: OnboardingViewModel) {
    val options = listOf(
        "SEDENTARY" to ("Ít vận động" to "Công việc bàn giấy, hiếm khi tập luyện"),
        "LIGHTLY_ACTIVE" to ("Vận động nhẹ" to "Tập nhẹ 1-3 buổi/tuần"),
        "MODERATELY_ACTIVE" to ("Vận động vừa" to "Tập vừa 3-5 buổi/tuần"),
        "VERY_ACTIVE" to ("Vận động nhiều" to "Tập nặng 6-7 buổi/tuần"),
        "EXTRA_ACTIVE" to ("Vận động rất nhiều" to "Vận động viên / lao động chân tay nặng")
    )
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepLabel("Bạn tập luyện/vận động ở mức nào trong tuần?", isDarkTheme)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, pair) ->
                val (label, desc) = pair
                MacroStyleOptionRow(label, desc, uiState.activityLevel == value, isDarkTheme) { viewModel.selectActivityLevel(value) }
            }
        }
    }
}

// --- Bước 4: Goal Selection ---
@Composable
private fun GoalStep(uiState: OnboardingUiState, isDarkTheme: Boolean, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepLabel("Bạn muốn giảm cân, duy trì hay tăng cân?", isDarkTheme)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionPill("Giảm cân", uiState.goal == "LOSE_WEIGHT", isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal("LOSE_WEIGHT") }
            SelectionPill("Duy trì", uiState.goal == "MAINTAIN", isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal("MAINTAIN") }
            SelectionPill("Tăng cân", uiState.goal == "GAIN_WEIGHT", isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal("GAIN_WEIGHT") }
        }

        if (uiState.goal != "MAINTAIN") {
            StepLabel("Cân nặng mục tiêu của bạn?", isDarkTheme)
            OutlinedTextField(
                value = uiState.targetWeightKg,
                onValueChange = { viewModel.setTargetWeightKg(it) },
                placeholder = { Text("VD: 60", color = if (isDarkTheme) TextMuted else TextInkMuted) },
                suffix = { Text("kg", color = if (isDarkTheme) TextMuted else TextInkMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = onboardingFieldColors(isDarkTheme)
            )

            StepLabel("Tốc độ thay đổi cân nặng mong muốn?", isDarkTheme)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { rate ->
                    RateSelectionPill(rate, uiState.weightRateKgPerWeek == rate, isDarkTheme, Modifier.weight(1f)) { viewModel.selectRate(rate) }
                }
            }
        }
    }
}

// --- Bước 5: Program Setup (Macro Style) ---
@Composable
private fun ProgramStep(uiState: OnboardingUiState, isDarkTheme: Boolean, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepLabel("Bạn ưu tiên phong cách ăn nào?", isDarkTheme)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroStyleOptionRow("Cân bằng", "30% đạm · 40% tinh bột · 30% béo", uiState.macroStyle == "BALANCED", isDarkTheme) { viewModel.selectMacroStyle("BALANCED") }
            MacroStyleOptionRow("Nhiều tinh bột, ít béo", "30% đạm · 55% tinh bột · 15% béo", uiState.macroStyle == "HIGH_CARB_LOW_FAT", isDarkTheme) { viewModel.selectMacroStyle("HIGH_CARB_LOW_FAT") }
            MacroStyleOptionRow("Ít tinh bột, nhiều béo", "35% đạm · 20% tinh bột · 45% béo", uiState.macroStyle == "LOW_CARB_HIGH_FAT", isDarkTheme) { viewModel.selectMacroStyle("LOW_CARB_HIGH_FAT") }
            MacroStyleOptionRow("Keto", "25% đạm · 5% tinh bột · 70% béo", uiState.macroStyle == "KETO", isDarkTheme) { viewModel.selectMacroStyle("KETO") }
        }
    }
}

// --- Màn Hoàn tất ---
@Composable
private fun OnboardingSummaryStep(
    uiState: OnboardingUiState,
    isDarkTheme: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.savedProfile
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(VividOrangeSoft),
            contentAlignment = Alignment.Center
        ) {
            Text("🎉", fontSize = 40.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Hồ sơ đã sẵn sàng!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "NutriWise đã tính toán mục tiêu dinh dưỡng riêng cho bạn",
            fontSize = 13.5.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${profile?.targetCalories?.toInt() ?: 0} kcal/ngày",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = VividOrange
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryMacroPill("Protein", "${profile?.targetProtein?.toInt() ?: 0}g", ProteinGradientStart, textSecondary)
                    SummaryMacroPill("Carb", "${profile?.targetCarb?.toInt() ?: 0}g", CarbGradientStart, textSecondary)
                    SummaryMacroPill("Fat", "${profile?.targetFat?.toInt() ?: 0}g", FatGradientStart, textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
        ) {
            Text("Bắt Đầu Ngay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SummaryMacroPill(label: String, amount: String, accent: androidx.compose.ui.graphics.Color, mutedColor: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(amount, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accent)
        Text(label, fontSize = 11.sp, color = mutedColor)
    }
}
