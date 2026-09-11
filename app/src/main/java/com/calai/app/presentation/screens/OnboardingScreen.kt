package com.calai.app.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.IconOptionRow
import com.calai.app.presentation.components.MacroStyleOptionRow
import com.calai.app.presentation.components.RateSelectionPill
import com.calai.app.presentation.components.SelectionPill
import com.calai.app.presentation.components.WheelPicker3D
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ONBOARDING_STEP_COUNT
import com.calai.app.presentation.viewmodel.OnboardingUiState
import com.calai.app.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

/**
 * Luồng Onboarding 26 bước theo BRD mục 4.2.8 (HorizontalPager, vuốt qua lại, tuần tự bắt buộc).
 * Mỗi bước chỉ hỏi ĐÚNG 1 câu hỏi — không nhồi nhiều câu vào cùng 1 màn (dễ đọc, dễ hoàn thành):
 * 0.  WELCOME                 — Chào mừng, giới thiệu app
 * 1.  GENDER                  — Chọn giới tính (bắt buộc, chặn Next nếu bỏ qua)
 * 2.  BIRTH_DATE              — Chọn ngày sinh bằng 3 cột WheelPicker3D (ngày/tháng/năm)
 * 3.  HEIGHT                  — Chọn chiều cao bằng WheelPicker3D (100-220 cm)
 * 4.  WEIGHT                  — Chọn cân nặng hiện tại bằng WheelPicker3D (30-180 kg)
 * 5.  BODY_FAT                — Chọn khoảng % mỡ cơ thể (optional, có thể bỏ qua)
 * 6.  GOAL                    — Chọn mục tiêu (3 card: Giảm cân / Duy trì / Tăng cân)
 * 7.  TARGET_WEIGHT_RATE      — Cân nặng mục tiêu + tốc độ thay đổi mong muốn
 * 8.  SLEEP_HOURS             — Giờ ngủ trung bình mỗi đêm
 * 9.  STRESS_LEVEL            — Mức độ căng thẳng gần đây
 * 10. SUPPLEMENTS             — Có dùng thực phẩm bổ sung không
 * 11. ACTIVITY_LEVEL          — Mức độ vận động hàng ngày
 * 12. DIET_TYPE               — Chế độ ăn (Cân bằng/Chay/Keto...)
 * 13. MEALS_PER_DAY           — Số bữa ăn mỗi ngày
 * 14. COOK_TIME               — Thời gian nấu ăn có sẵn mỗi bữa
 * 15. FOOD_BUDGET             — Mức ngân sách ăn uống
 * 16. INTERMITTENT_FASTING    — Có nhịn ăn gián đoạn không + khung giờ ăn
 * 17. TRAINING_EXPERIENCE     — Kinh nghiệm tập luyện
 * 18. TRAINING_GOAL           — Mục tiêu tập luyện chính
 * 19. SESSIONS_PER_WEEK       — Số buổi tập mong muốn mỗi tuần
 * 20. EQUIPMENT_ACCESS        — Thiết bị/nơi tập có sẵn
 * 21. INJURIES                — Chấn thương/hạn chế vận động (multi-select)
 * 22. ONE_REP_MAX             — 1RM Squat/Bench/Deadlift (optional)
 * 23. PROGRAM_TYPE            — Coached/Collaborative/Manual
 * 24. MACRO_STYLE             — Phong cách phân bổ macro
 * 25. PROTEIN_PREFERENCE      — Mức ưu tiên Protein
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { ONBOARDING_STEP_COUNT })
    val coroutineScope = rememberCoroutineScope()

    val bg = MaterialTheme.colorScheme.background
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    // Đồng bộ currentStep trong ViewModel khi lướt pager
    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentStep(pagerState.currentPage)
    }

    Scaffold(containerColor = bg) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isCompleted) {
                // ── Màn tổng kết sau khi PATCH /users/me thành công ──
                SummaryStep(
                    uiState = uiState,
                    isDarkTheme = isDarkTheme,
                    onFinish = onFinished,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Progress bar (animated) ──
                    val animatedProgress by animateFloatAsState(
                        targetValue = (pagerState.currentPage + 1f) / ONBOARDING_STEP_COUNT,
                        label = "progress"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Bước ${pagerState.currentPage + 1}/$ONBOARDING_STEP_COUNT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(VividOrangeSoft)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "${(animatedProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VividOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Thanh tiến độ segment theo nhóm câu hỏi (Cơ bản/Mục tiêu/Lối sống/Dinh dưỡng/Tập luyện/Chương trình)
                    // — trực quan hơn 1 thanh liền, giúp người dùng thấy còn "mảng" nào chưa xong.
                    val segmentBounds = listOf(0, 6, 8, 12, 17, 23, ONBOARDING_STEP_COUNT)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until segmentBounds.size - 1) {
                            val segStart = segmentBounds[i]
                            val segEnd = segmentBounds[i + 1]
                            val segFraction = ((pagerState.currentPage + 1 - segStart).toFloat() / (segEnd - segStart))
                                .coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .weight((segEnd - segStart).toFloat())
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.outline)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(segFraction)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(listOf(VividOrangeLight, VividOrange))
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── 5 trang nội dung (HorizontalPager) ──
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        userScrollEnabled = true
                    ) { page ->
                        when (page) {
                            0 -> WelcomePage(isDarkTheme)
                            1 -> GenderPage(uiState.gender, viewModel::selectGender, isDarkTheme)
                            2 -> BirthDatePage(uiState.birthDay, uiState.birthMonth, uiState.birthYear, viewModel::setDateOfBirth, isDarkTheme)
                            3 -> HeightPage(uiState.heightCm.toInt(), { viewModel.setHeightCm(it.toFloat()) }, isDarkTheme)
                            4 -> WeightPage(uiState.weightKg.toInt(), { viewModel.setWeightKg(it.toFloat()) }, isDarkTheme)
                            5 -> BodyFatPage(uiState.bodyFatPercent, viewModel::selectBodyFatPercent, isDarkTheme)
                            6 -> GoalPage(uiState.goal, viewModel::selectGoal, isDarkTheme)
                            7 -> TargetWeightRatePage(
                                goal = uiState.goal,
                                targetWeightKg = uiState.targetWeightKg,
                                weightRateKgPerWeek = uiState.weightRateKgPerWeek,
                                onTargetWeightChange = viewModel::setTargetWeightKg,
                                onRateSelect = viewModel::selectWeightRate,
                                isDarkTheme = isDarkTheme
                            )
                            8 -> SleepHoursPage(uiState.sleepHours, viewModel::setSleepHours, isDarkTheme)
                            9 -> StressLevelPage(uiState.stressLevel, viewModel::selectStressLevel, isDarkTheme)
                            10 -> SupplementsPage(uiState.takesSupplements, viewModel::setTakesSupplements, isDarkTheme)
                            11 -> ActivityLevelPage(uiState.activityLevel, viewModel::selectActivityLevel, isDarkTheme)
                            12 -> DietTypePage(uiState.dietType, viewModel::selectDietType, isDarkTheme)
                            13 -> MealsPerDayPage(uiState.mealsPerDay, viewModel::setMealsPerDay, isDarkTheme)
                            14 -> CookTimePage(uiState.cookTimeMinutes, viewModel::setCookTimeMinutes, isDarkTheme)
                            15 -> FoodBudgetPage(uiState.foodBudgetLevel, viewModel::selectFoodBudgetLevel, isDarkTheme)
                            16 -> IntermittentFastingPage(
                                isIntermittentFasting = uiState.isIntermittentFasting,
                                ifWindowStart = uiState.ifWindowStart,
                                ifWindowEnd = uiState.ifWindowEnd,
                                onIntermittentFastingChange = viewModel::setIntermittentFasting,
                                onIfWindowChange = viewModel::setIfWindow,
                                isDarkTheme = isDarkTheme
                            )
                            17 -> TrainingExperiencePage(uiState.trainingExperience, viewModel::selectTrainingExperience, isDarkTheme)
                            18 -> TrainingGoalPage(uiState.trainingGoal, viewModel::selectTrainingGoal, isDarkTheme)
                            19 -> SessionsPerWeekPage(uiState.sessionsPerWeek, viewModel::selectSessionsPerWeek, isDarkTheme)
                            20 -> EquipmentAccessPage(uiState.equipmentAccess, viewModel::selectEquipmentAccess, isDarkTheme)
                            21 -> InjuriesPage(
                                injuries = uiState.injuries,
                                injuriesOtherNote = uiState.injuriesOtherNote,
                                onInjuryToggle = viewModel::toggleInjury,
                                onInjuriesOtherNoteChange = viewModel::setInjuriesOtherNote,
                                isDarkTheme = isDarkTheme
                            )
                            22 -> OneRepMaxPage(
                                oneRepMaxSquatKg = uiState.oneRepMaxSquatKg,
                                oneRepMaxBenchKg = uiState.oneRepMaxBenchKg,
                                oneRepMaxDeadliftKg = uiState.oneRepMaxDeadliftKg,
                                onSquatChange = viewModel::setOneRepMaxSquat,
                                onBenchChange = viewModel::setOneRepMaxBench,
                                onDeadliftChange = viewModel::setOneRepMaxDeadlift,
                                onClearOneRepMax = viewModel::clearOneRepMax
                            )
                            23 -> ProgramTypePage(uiState.programType, viewModel::selectProgramType, isDarkTheme)
                            24 -> MacroStylePage(uiState.macroStyle, viewModel::selectMacroStyle, isDarkTheme)
                            25 -> ProteinPreferencePage(uiState.proteinPreference, viewModel::selectProteinPreference, isDarkTheme)
                        }
                    }

                    // ── Lỗi nếu có ──
                    uiState.errorMessage?.let {
                        Text(
                            it,
                            color = CoralWarning,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        )
                    }

                    // ── Nút điều hướng dưới cùng — thanh gradient nổi bật (giống mẫu tham chiếu) ──
                    val isLast = pagerState.currentPage == ONBOARDING_STEP_COUNT - 1
                    val canGo = uiState.canProceed(pagerState.currentPage)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.horizontalGradient(listOf(VividOrangeDark, VividOrange)))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút Quay lại (ẩn ở trang đầu)
                        if (pagerState.currentPage > 0) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                modifier = Modifier.weight(0.8f)
                            ) {
                                Text("‹ Quay lại", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextWhite)
                            }
                        }

                        // Nút Tiếp theo / Hoàn tất
                        Button(
                            onClick = {
                                if (isLast) viewModel.submit()
                                else coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            enabled = canGo && !uiState.isSaving,
                            modifier = Modifier
                                .weight(if (pagerState.currentPage > 0) 1.2f else 1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TextWhite,
                                disabledContainerColor = TextWhite.copy(alpha = 0.6f)
                            )
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(Modifier.size(20.dp), VividOrange, strokeWidth = 2.5.dp)
                            } else {
                                Text(
                                    when {
                                        isLast -> "Hoàn tất »"
                                        pagerState.currentPage == 0 -> "Bắt đầu »"
                                        else -> "Tiếp theo »"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = VividOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 0: WELCOME
// ══════════════════════════════════════════════════════════════
@Composable
private fun WelcomePage(isDarkTheme: Boolean) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(VividOrangeSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = VividOrange,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Chào mừng bạn đến với CalAI",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Trợ lý dinh dưỡng thông minh giúp bạn kiểm soát calo và đạt mục tiêu vóc dáng.",
            fontSize = 14.5.sp,
            color = textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(36.dp))

        listOf(
            "Nhận diện bữa ăn bằng AI" to "Chỉ cần chụp 1 ảnh, hệ thống tự tách calo & macro",
            "Mục tiêu cá nhân hóa" to "BMR, TDEE tính riêng dựa trên số đo cơ thể của bạn",
            "Theo dõi tiến độ" to "Cân nặng, dinh dưỡng và năng lượng cập nhật mỗi ngày"
        ).forEach { (title, desc) ->
            FeatureItem(title, desc, isDarkTheme)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FeatureItem(title: String, desc: String, isDarkTheme: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(VividOrangeSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = VividOrange, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 1: HEIGHT (WheelPicker3D, 100–220 cm)
// ══════════════════════════════════════════════════════════════
@Composable
private fun HeightPage(heightCm: Int, onChange: (Int) -> Unit, isDarkTheme: Boolean) {
    PickerPage(
        title = "Chiều cao của bạn?",
        subtitle = "Cuộn lên/xuống để chọn chiều cao",
        isDarkTheme = isDarkTheme
    ) {
        // Hiển thị giá trị lớn phía trên
        ValueDisplay(value = "$heightCm", unit = "cm", isDarkTheme = isDarkTheme)

        Spacer(Modifier.height(16.dp))

        // Wheel Picker 3D
        WheelPicker3D(
            value = heightCm,
            onValueChange = onChange,
            range = 100..220,
            modifier = Modifier.fillMaxWidth(0.55f),
            isDarkTheme = isDarkTheme
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 2: WEIGHT (WheelPicker3D, 30–180 kg)
// ══════════════════════════════════════════════════════════════
@Composable
private fun WeightPage(weightKg: Int, onChange: (Int) -> Unit, isDarkTheme: Boolean) {
    PickerPage(
        title = "Cân nặng hiện tại?",
        subtitle = "Cuộn lên/xuống để chọn cân nặng",
        isDarkTheme = isDarkTheme
    ) {
        ValueDisplay(value = "$weightKg", unit = "kg", isDarkTheme = isDarkTheme)

        Spacer(Modifier.height(16.dp))

        WheelPicker3D(
            value = weightKg,
            onValueChange = onChange,
            range = 30..180,
            modifier = Modifier.fillMaxWidth(0.55f),
            isDarkTheme = isDarkTheme
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 3: BIRTH_DATE (3 cột WheelPicker3D)
// ══════════════════════════════════════════════════════════════
@Composable
private fun BirthDatePage(
    day: Int,
    month: Int,
    year: Int,
    onDateChange: (Int, Int, Int) -> Unit,
    isDarkTheme: Boolean
) {
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val daysInMonth = remember(month, year) { getDaysInMonth(month, year) }

    // Điều chỉnh ngày nếu vượt quá số ngày trong tháng
    LaunchedEffect(daysInMonth) {
        if (day > daysInMonth) onDateChange(daysInMonth, month, year)
    }

    PickerPage(
        title = "Ngày sinh của bạn?",
        subtitle = "Dùng để tính chỉ số trao đổi chất (BMR & TDEE)",
        isDarkTheme = isDarkTheme
    ) {
        // Hiển thị ngày đã chọn
        ValueDisplay(
            value = "%02d/%02d/%d".format(day, month, year),
            unit = "",
            isDarkTheme = isDarkTheme
        )

        Spacer(Modifier.height(12.dp))

        // Nhãn cột
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Ngày", "Tháng", "Năm").forEach { label ->
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // 3 cột wheel picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Cột Ngày
            WheelPicker3D(
                value = day.coerceIn(1, daysInMonth),
                onValueChange = { onDateChange(it, month, year) },
                range = 1..daysInMonth,
                modifier = Modifier.weight(1f),
                itemHeight = 48.dp,
                isDarkTheme = isDarkTheme
            )

            // Cột Tháng
            WheelPicker3D(
                value = month,
                onValueChange = { newMonth ->
                    val maxDay = getDaysInMonth(newMonth, year)
                    onDateChange(day.coerceAtMost(maxDay), newMonth, year)
                },
                range = 1..12,
                modifier = Modifier.weight(1f),
                itemHeight = 48.dp,
                isDarkTheme = isDarkTheme
            )

            // Cột Năm
            WheelPicker3D(
                value = year,
                onValueChange = { newYear ->
                    val maxDay = getDaysInMonth(month, newYear)
                    onDateChange(day.coerceAtMost(maxDay), month, newYear)
                },
                range = 1940..2015,
                modifier = Modifier.weight(1.2f),
                itemHeight = 48.dp,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 4: GOAL (3 card chọn 1)
// ══════════════════════════════════════════════════════════════
@Composable
private fun GoalPage(
    selectedGoal: String,
    onSelect: (String) -> Unit,
    isDarkTheme: Boolean
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val goals = listOf(
        Triple("LOSE_WEIGHT", "Giảm cân", "Giảm mỡ thừa, cơ thể thon gọn và săn chắc") to Icons.AutoMirrored.Filled.TrendingDown,
        Triple("MAINTAIN", "Duy trì vóc dáng", "Giữ mức cân hiện tại, sống khỏe mỗi ngày") to Icons.AutoMirrored.Filled.TrendingFlat,
        Triple("GAIN_WEIGHT", "Tăng cân / Tăng cơ", "Xây dựng cơ bắp, tăng thể trọng khoa học") to Icons.AutoMirrored.Filled.TrendingUp
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                "Mục tiêu của bạn?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "CalAI tối ưu lượng calo nạp mỗi ngày theo mục tiêu này",
                fontSize = 14.sp,
                color = textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            goals.forEach { (info, icon) ->
                val (key, title, subtitle) = info
                val isSelected = selectedGoal == key

                val cardBg = when {
                    isSelected -> MaterialTheme.colorScheme.surfaceContainerHighest
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderCol = if (isSelected) VividOrange else (MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(if (isSelected) 2.dp else 1.dp, borderCol, RoundedCornerShape(20.dp))
                        .clickable { onSelect(key) }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) VividOrangeSoft else (MaterialTheme.colorScheme.surfaceVariant)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = if (isSelected) VividOrange else textSecondary, modifier = Modifier.size(26.dp))
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isSelected) VividOrange else textPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text(subtitle, fontSize = 12.5.sp, color = textSecondary, lineHeight = 16.sp)
                    }

                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, null, tint = VividOrange, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 1: GENDER (bắt buộc chọn, chặn Next nếu bỏ qua)
// ══════════════════════════════════════════════════════════════
@Composable
private fun GenderPage(
    selectedGender: String,
    onSelect: (String) -> Unit,
    isDarkTheme: Boolean
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val options = listOf(
        Triple("MALE", "Nam", Icons.Default.Male to VividOrange),
        Triple("FEMALE", "Nữ", Icons.Default.Female to PastelRose),
        Triple("OTHER", "Khác / Không muốn tiết lộ", Icons.Default.Close to PastelLavender)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
            Text("Bạn nhận diện giới tính nào?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                "Dùng để tính chính xác chỉ số trao đổi chất (BMR)",
                fontSize = 14.sp,
                color = textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            options.forEach { (key, title, iconAndColor) ->
                val (icon, accentColor) = iconAndColor
                IconOptionRow(
                    icon = icon,
                    accentColor = accentColor,
                    label = title,
                    isSelected = selectedGender == key,
                    isDarkTheme = isDarkTheme,
                    onClick = { onSelect(key) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 5: BODY_FAT (9 khoảng %, optional)
// ══════════════════════════════════════════════════════════════
@Composable
private fun BodyFatPage(
    bodyFatPercent: Float?,
    onSelect: (Float?) -> Unit,
    isDarkTheme: Boolean
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val ranges = listOf(
        "10–13%" to 11.5f, "14–17%" to 15.5f, "18–21%" to 19.5f,
        "22–25%" to 23.5f, "26–29%" to 27.5f, "30–34%" to 32f,
        "35–39%" to 37f, "40–49%" to 44.5f, "50%+" to 52f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tỷ lệ mỡ cơ thể (nếu biết)?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Giúp tính toán chính xác hơn — có thể bỏ qua nếu chưa biết",
            fontSize = 14.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        ranges.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value) ->
                    SelectionPill(
                        label = label,
                        isSelected = bodyFatPercent == value,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(value) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { onSelect(null) }) {
            Text("Bỏ qua, tôi chưa biết", color = textSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 7: TARGET_WEIGHT_RATE (cân nặng mục tiêu + tốc độ thay đổi)
// ══════════════════════════════════════════════════════════════
@Composable
private fun TargetWeightRatePage(
    goal: String,
    targetWeightKg: Float,
    weightRateKgPerWeek: Float,
    onTargetWeightChange: (Float) -> Unit,
    onRateSelect: (Float) -> Unit,
    isDarkTheme: Boolean
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val border = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mục tiêu cân nặng?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "CalAI dùng thông tin này để tính lượng calo mục tiêu mỗi ngày",
            fontSize = 14.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        if (goal == "MAINTAIN") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surface)
                    .border(1.dp, border, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Text(
                    "Bạn chọn Duy trì vóc dáng — CalAI sẽ giữ cân nặng hiện tại làm mục tiêu.",
                    fontSize = 14.sp,
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            SectionLabel("Cân nặng mục tiêu của bạn?", textPrimary)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = if (targetWeightKg > 0f) targetWeightKg.toString() else "",
                onValueChange = { text -> text.toFloatOrNull()?.let(onTargetWeightChange) },
                placeholder = { Text("VD: 62", color = textSecondary) },
                suffix = { Text("kg", color = textSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = surface,
                    unfocusedContainerColor = surface,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = border,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                )
            )

            Spacer(Modifier.height(20.dp))
            SectionLabel("Tốc độ thay đổi cân nặng mong muốn?", textPrimary)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { rate ->
                    RateSelectionPill(rate, weightRateKgPerWeek == rate, isDarkTheme, Modifier.weight(1f)) { onRateSelect(rate) }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "≈ ${(weightRateKgPerWeek * 7700 / 7).toInt()} kcal thâm hụt/thặng dư mỗi ngày",
                fontSize = 11.5.sp,
                color = textSecondary
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  HEADER DÙNG CHUNG: mỗi trang chỉ 1 câu hỏi (tránh nhồi nhét)
// ══════════════════════════════════════════════════════════════
@Composable
private fun QuestionPageHeader(title: String, subtitle: String) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        color = textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        subtitle,
        fontSize = 14.sp,
        color = textSecondary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))
}

@Composable
private fun QuestionPageScaffold(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        QuestionPageHeader(title, subtitle)
        content()
        Spacer(Modifier.height(8.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  NHÓM LIFESTYLE — mỗi câu hỏi 1 trang riêng
// ══════════════════════════════════════════════════════════════
@Composable
private fun SleepHoursPage(sleepHours: Float, onSleepHoursChange: (Float) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Bạn ngủ bao nhiêu giờ mỗi đêm?", "Giấc ngủ ảnh hưởng tới khả năng phục hồi của cơ thể") {
        listOf(4, 5, 6, 7, 8, 9, 10).chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { h ->
                    SelectionPill(
                        label = "$h h",
                        isSelected = sleepHours.toInt() == h,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onSleepHoursChange(h.toFloat()) }
                    )
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StressLevelPage(stressLevel: String, onStressSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Mức độ căng thẳng gần đây?", "Giúp CalAI hiểu trạng thái phục hồi tổng thể của bạn") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("LOW" to "Thấp", "MEDIUM" to "Trung bình", "HIGH" to "Cao").forEach { (key, label) ->
                SelectionPill(label, stressLevel == key, isDarkTheme, Modifier.weight(1f)) { onStressSelect(key) }
            }
        }
    }
}

@Composable
private fun SupplementsPage(takesSupplements: Boolean, onSupplementsChange: (Boolean) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Bạn có dùng thực phẩm bổ sung?", "Whey, Creatine, Multivitamin... — giúp AI Coach tư vấn sát hơn") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionPill("Có", takesSupplements, isDarkTheme, Modifier.weight(1f)) { onSupplementsChange(true) }
            SelectionPill("Không", !takesSupplements, isDarkTheme, Modifier.weight(1f)) { onSupplementsChange(false) }
        }
    }
}

@Composable
private fun ActivityLevelPage(activityLevel: String, onActivitySelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Mức độ vận động hàng ngày?", "Dùng để ước tính lượng calo tiêu hao (TDEE) ban đầu") {
        listOf(
            "SEDENTARY" to "Ít vận động",
            "LIGHTLY_ACTIVE" to "Vận động nhẹ",
            "MODERATELY_ACTIVE" to "Vận động vừa",
            "VERY_ACTIVE" to "Vận động nhiều",
            "EXTRA_ACTIVE" to "Vận động rất nhiều"
        ).forEach { (key, label) ->
            SelectionPill(
                label = label,
                isSelected = activityLevel == key,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onActivitySelect(key) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  NHÓM NUTRITION — mỗi câu hỏi 1 trang riêng
// ══════════════════════════════════════════════════════════════
@Composable
private fun DietTypePage(dietType: String, onDietTypeSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Chế độ ăn của bạn?", "CalAI sẽ lọc mọi gợi ý món ăn theo đúng chế độ này") {
        listOf(
            "BALANCED" to ("Cân bằng" to "Đa dạng nhóm thực phẩm"),
            "VEGETARIAN" to ("Ăn chay" to "Không thịt, không cá"),
            "VEGAN" to ("Thuần chay" to "Không sản phẩm từ động vật"),
            "KETO" to ("Keto" to "Ít tinh bột, nhiều chất béo"),
            "LOW_CARB" to ("Ít tinh bột" to "Giảm carb, giữ đạm & rau"),
            "PESCATARIAN" to ("Chay + hải sản" to "Không thịt đỏ/gia cầm"),
            "OTHER" to ("Khác" to "Chế độ ăn khác/tự do")
        ).forEach { (key, pair) ->
            val (label, desc) = pair
            MacroStyleOptionRow(label, desc, dietType == key, isDarkTheme) { onDietTypeSelect(key) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MealsPerDayPage(mealsPerDay: Int, onMealsPerDayChange: (Int) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Số bữa ăn mỗi ngày?", "Dùng để chia đều mục tiêu calo theo từng bữa") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (2..6).forEach { n ->
                SelectionPill("$n bữa", mealsPerDay == n, isDarkTheme, Modifier.weight(1f)) { onMealsPerDayChange(n) }
            }
        }
    }
}

@Composable
private fun CookTimePage(cookTimeMinutes: Int, onCookTimeChange: (Int) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Thời gian nấu ăn có sẵn mỗi bữa?", "Giúp AI gợi ý công thức phù hợp quỹ thời gian của bạn") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 45, 60, 90).forEach { m ->
                SelectionPill("$m'", cookTimeMinutes == m, isDarkTheme, Modifier.weight(1f)) { onCookTimeChange(m) }
            }
        }
    }
}

@Composable
private fun FoodBudgetPage(foodBudgetLevel: String, onFoodBudgetSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Mức ngân sách ăn uống?", "Chỉ ảnh hưởng thứ tự ưu tiên gợi ý món, không giới hạn cứng") {
        listOf(
            "LOW" to ("Tiết kiệm" to "Ưu tiên món rẻ, nguyên liệu đơn giản"),
            "MEDIUM" to ("Vừa phải" to "Cân đối chi phí và chất lượng"),
            "HIGH" to ("Thoải mái" to "Không giới hạn nhiều về chi phí")
        ).forEach { (key, pair) ->
            val (label, desc) = pair
            MacroStyleOptionRow(label, desc, foodBudgetLevel == key, isDarkTheme) { onFoodBudgetSelect(key) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun IntermittentFastingPage(
    isIntermittentFasting: Boolean,
    ifWindowStart: String,
    ifWindowEnd: String,
    onIntermittentFastingChange: (Boolean) -> Unit,
    onIfWindowChange: (String, String) -> Unit,
    isDarkTheme: Boolean
) {
    QuestionPageScaffold("Bạn có nhịn ăn gián đoạn (IF)?", "Intermittent Fasting — chỉ ăn trong 1 khung giờ cố định mỗi ngày") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionPill("Có", isIntermittentFasting, isDarkTheme, Modifier.weight(1f)) { onIntermittentFastingChange(true) }
            SelectionPill("Không", !isIntermittentFasting, isDarkTheme, Modifier.weight(1f)) { onIntermittentFastingChange(false) }
        }
        if (isIntermittentFasting) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ifWindowStart,
                    onValueChange = { onIfWindowChange(it, ifWindowEnd) },
                    label = { Text("Bắt đầu (HH:mm)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ifWindowEnd,
                    onValueChange = { onIfWindowChange(ifWindowStart, it) },
                    label = { Text("Kết thúc (HH:mm)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  NHÓM TRAINING — mỗi câu hỏi 1 trang riêng
// ══════════════════════════════════════════════════════════════
@Composable
private fun TrainingExperiencePage(trainingExperience: String, onExperienceSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Kinh nghiệm tập luyện của bạn?", "Giúp AI hiệu chỉnh độ khó chương trình tập") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("BEGINNER" to "Mới bắt đầu", "INTERMEDIATE" to "Trung bình", "ADVANCED" to "Nâng cao").forEach { (key, label) ->
                SelectionPill(label, trainingExperience == key, isDarkTheme, Modifier.weight(1f)) { onExperienceSelect(key) }
            }
        }
    }
}

@Composable
private fun TrainingGoalPage(trainingGoal: String, onGoalSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Mục tiêu tập luyện chính?", "AI sẽ đối chiếu với mục tiêu dinh dưỡng để cảnh báo nếu xung đột") {
        listOf(
            "BUILD_MUSCLE" to "Tăng cơ", "LOSE_FAT" to "Giảm mỡ", "STRENGTH" to "Tăng sức mạnh",
            "ENDURANCE" to "Tăng sức bền", "RECOMP" to "Vừa tăng cơ vừa giảm mỡ", "GENERAL_FITNESS" to "Sức khỏe tổng quát"
        ).chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (key, label) ->
                    SelectionPill(label, trainingGoal == key, isDarkTheme, Modifier.weight(1f)) { onGoalSelect(key) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SessionsPerWeekPage(sessionsPerWeek: String, onSessionsSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Số buổi tập mong muốn mỗi tuần?", "Kết hợp kinh nghiệm để AI đề xuất lịch tập (split) phù hợp") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ONE_TO_TWO" to "1-2 buổi", "THREE_TO_FOUR" to "3-4 buổi", "FIVE_TO_SIX" to "5-6 buổi", "SEVEN" to "7 buổi").forEach { (key, label) ->
                SelectionPill(label, sessionsPerWeek == key, isDarkTheme, Modifier.weight(1f)) { onSessionsSelect(key) }
            }
        }
    }
}

@Composable
private fun EquipmentAccessPage(equipmentAccess: String, onEquipmentSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Thiết bị/nơi tập bạn có sẵn?", "Dùng để lọc bài tập phù hợp khi AI tạo chương trình") {
        listOf(
            "FULL_GYM" to "Gym đầy đủ", "BASIC_GYM" to "Gym cơ bản",
            "HOME_DUMBBELL" to "Tại nhà (có tạ đơn)", "BODYWEIGHT_ONLY" to "Chỉ trọng lượng cơ thể"
        ).chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (key, label) ->
                    SelectionPill(label, equipmentAccess == key, isDarkTheme, Modifier.weight(1f)) { onEquipmentSelect(key) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InjuriesPage(
    injuries: List<String>,
    injuriesOtherNote: String,
    onInjuryToggle: (String) -> Unit,
    onInjuriesOtherNoteChange: (String) -> Unit,
    isDarkTheme: Boolean
) {
    QuestionPageScaffold("Bạn có chấn thương hoặc hạn chế vận động?", "AI sẽ loại trừ hoàn toàn bài tập rủi ro cao tương ứng") {
        listOf(
            "SHOULDER" to "Vai", "LOWER_BACK" to "Lưng dưới", "KNEE" to "Đầu gối",
            "WRIST" to "Cổ tay", "OTHER" to "Khác", "NONE" to "Không có"
        ).chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (key, label) ->
                    SelectionPill(label, injuries.contains(key), isDarkTheme, Modifier.weight(1f)) { onInjuryToggle(key) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (injuries.contains("OTHER")) {
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = injuriesOtherNote,
                onValueChange = onInjuriesOtherNoteChange,
                label = { Text("Mô tả chấn thương khác", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OneRepMaxPage(
    oneRepMaxSquatKg: Float?,
    oneRepMaxBenchKg: Float?,
    oneRepMaxDeadliftKg: Float?,
    onSquatChange: (Float?) -> Unit,
    onBenchChange: (Float?) -> Unit,
    onDeadliftChange: (Float?) -> Unit,
    onClearOneRepMax: () -> Unit
) {
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    QuestionPageScaffold("1RM hiện tại của bạn (nếu biết)?", "Mức tạ nặng nhất bạn nâng được 1 lần — tính bằng kg, có thể bỏ qua") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = oneRepMaxSquatKg?.toString() ?: "",
                onValueChange = { onSquatChange(it.toFloatOrNull()) },
                label = { Text("Squat", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = oneRepMaxBenchKg?.toString() ?: "",
                onValueChange = { onBenchChange(it.toFloatOrNull()) },
                label = { Text("Bench", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = oneRepMaxDeadliftKg?.toString() ?: "",
                onValueChange = { onDeadliftChange(it.toFloatOrNull()) },
                label = { Text("Deadlift", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onClearOneRepMax) {
            Text("Chưa biết", color = textSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  NHÓM PROGRAM SETUP — mỗi câu hỏi 1 trang riêng
// ══════════════════════════════════════════════════════════════
@Composable
private fun ProgramTypePage(programType: String, onProgramTypeSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Bạn muốn CalAI đồng hành thế nào?", "Quyết định mức tự động hoá khi điều chỉnh mục tiêu") {
        listOf(
            "COACHED" to ("CalAI dẫn dắt" to "Tự động điều chỉnh mục tiêu theo tiến độ"),
            "COLLABORATIVE" to ("Kết hợp" to "CalAI gợi ý, bạn xác nhận trước khi áp dụng"),
            "MANUAL" to ("Tự chủ" to "Bạn tự đặt và chỉnh mục tiêu")
        ).forEach { (key, pair) ->
            val (label, desc) = pair
            MacroStyleOptionRow(label, desc, programType == key, isDarkTheme) { onProgramTypeSelect(key) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MacroStylePage(macroStyle: String, onMacroStyleSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Bạn ưu tiên phong cách ăn nào?", "Quyết định cách phân bổ tỷ lệ Đạm/Tinh bột/Béo") {
        listOf(
            "BALANCED" to ("Cân bằng" to "30% đạm · 40% tinh bột · 30% béo"),
            "HIGH_CARB_LOW_FAT" to ("Nhiều tinh bột, ít béo" to "30% đạm · 55% tinh bột · 15% béo"),
            "LOW_CARB_HIGH_FAT" to ("Ít tinh bột, nhiều béo" to "35% đạm · 20% tinh bột · 45% béo"),
            "KETO" to ("Keto" to "25% đạm · 5% tinh bột · 70% béo")
        ).forEach { (key, pair) ->
            val (label, desc) = pair
            MacroStyleOptionRow(label, desc, macroStyle == key, isDarkTheme) { onMacroStyleSelect(key) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProteinPreferencePage(proteinPreference: String, onProteinPreferenceSelect: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Mức ưu tiên Protein?", "Bước cuối cùng trước khi CalAI tính mục tiêu calo & macro cho bạn") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("LOW" to "Thấp", "MID" to "Vừa", "HIGH" to "Cao", "VERY_HIGH" to "Rất cao").forEach { (key, label) ->
                SelectionPill(label, proteinPreference == key, isDarkTheme, Modifier.weight(1f)) { onProteinPreferenceSelect(key) }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// ══════════════════════════════════════════════════════════════
//  MÀN HÌNH TỔNG KẾT (sau khi API trả về thành công)
// ══════════════════════════════════════════════════════════════
@Composable
private fun SummaryStep(
    uiState: OnboardingUiState,
    isDarkTheme: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.savedProfile
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

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
            Icon(Icons.Default.CheckCircle, null, tint = VividOrange, modifier = Modifier.size(44.dp))
        }

        Spacer(Modifier.height(24.dp))
        Text("Thiết lập thành công!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("CalAI đã tính mục tiêu dinh dưỡng tối ưu cho bạn.", fontSize = 13.5.sp, color = textSecondary, textAlign = TextAlign.Center)

        Spacer(Modifier.height(28.dp))

        // Card kết quả calo & macro
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${profile?.targetCalories?.toInt() ?: 2000} kcal/ngày",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = VividOrange
                )
                Text("Mục tiêu năng lượng mỗi ngày", fontSize = 12.sp, color = textSecondary)

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    MacroItem("Đạm", "${profile?.targetProtein?.toInt() ?: 120}g", ProteinGradientStart)
                    MacroItem("Tinh bột", "${profile?.targetCarb?.toInt() ?: 220}g", CarbGradientStart)
                    MacroItem("Chất béo", "${profile?.targetFat?.toInt() ?: 55}g", FatGradientStart)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
        ) {
            Text("Bắt đầu trải nghiệm", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextWhite)
        }
    }
}

@Composable
private fun MacroItem(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(amount, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

// ══════════════════════════════════════════════════════════════
//  HELPERS — Layout chung cho HEIGHT / WEIGHT / BIRTH_DATE
// ══════════════════════════════════════════════════════════════

/** Layout khung cho các trang có picker: tiêu đề trên, content giữa, gợi ý dưới. */
@Composable
private fun PickerPage(
    title: String,
    subtitle: String,
    isDarkTheme: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Tiêu đề
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, fontSize = 14.sp, color = textSecondary, textAlign = TextAlign.Center)
        }

        // Nội dung picker
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            content()
        }

        // Gợi ý
        Text(
            "Vuốt lên/xuống để cuộn, thả tay để chốt",
            fontSize = 12.sp,
            color = textSecondary.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

/** Hiển thị giá trị lớn + đơn vị phía trên wheel picker. */
@Composable
private fun ValueDisplay(value: String, unit: String, isDarkTheme: Boolean) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            value,
            fontSize = 52.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 56.sp
        )
        if (unit.isNotBlank()) {
            Spacer(Modifier.width(6.dp))
            Text(
                unit,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VividOrange,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

// ── Hàm tính ngày trong tháng (hỗ trợ năm nhuận) ──

private fun getDaysInMonth(month: Int, year: Int): Int = when (month) {
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}
