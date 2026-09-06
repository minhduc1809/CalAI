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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.MacroStyleOptionRow
import com.calai.app.presentation.components.SelectionPill
import com.calai.app.presentation.components.WheelPicker3D
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ONBOARDING_STEP_COUNT
import com.calai.app.presentation.viewmodel.OnboardingUiState
import com.calai.app.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

/**
 * Luồng Onboarding 7 bước (HorizontalPager, vuốt qua lại):
 * 0. WELCOME    — Chào mừng, giới thiệu app
 * 1. HEIGHT     — Chọn chiều cao bằng WheelPicker3D (100-220 cm)
 * 2. WEIGHT     — Chọn cân nặng bằng WheelPicker3D (30-180 kg)
 * 3. BIRTH_DATE — Chọn ngày sinh bằng 3 cột WheelPicker3D (ngày/tháng/năm)
 * 4. GOAL       — Chọn mục tiêu (3 card: Giảm cân / Duy trì / Tăng cân)
 * 5. LIFESTYLE  — Giờ ngủ, mức stress, supplements, mức vận động (Lifestyle Profile)
 * 6. NUTRITION  — Chế độ ăn, số bữa/ngày, thời gian nấu, ngân sách (Nutrition Profile)
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

    val bg = if (isDarkTheme) ObsidianBackground else IvoryBackground
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

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
                        Text(
                            "${(animatedProgress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = VividOrange,
                        trackColor = if (isDarkTheme) CharcoalBorder else PearlBorder
                    )

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
                            1 -> HeightPage(uiState.heightCm.toInt(), { viewModel.setHeightCm(it.toFloat()) }, isDarkTheme)
                            2 -> WeightPage(uiState.weightKg.toInt(), { viewModel.setWeightKg(it.toFloat()) }, isDarkTheme)
                            3 -> BirthDatePage(uiState.birthDay, uiState.birthMonth, uiState.birthYear, viewModel::setDateOfBirth, isDarkTheme)
                            4 -> GoalPage(uiState.goal, viewModel::selectGoal, isDarkTheme)
                            5 -> LifestylePage(
                                sleepHours = uiState.sleepHours,
                                stressLevel = uiState.stressLevel,
                                takesSupplements = uiState.takesSupplements,
                                activityLevel = uiState.activityLevel,
                                onSleepHoursChange = viewModel::setSleepHours,
                                onStressSelect = viewModel::selectStressLevel,
                                onSupplementsChange = viewModel::setTakesSupplements,
                                onActivitySelect = viewModel::selectActivityLevel,
                                isDarkTheme = isDarkTheme
                            )
                            6 -> NutritionPage(
                                dietType = uiState.dietType,
                                mealsPerDay = uiState.mealsPerDay,
                                cookTimeMinutes = uiState.cookTimeMinutes,
                                foodBudgetLevel = uiState.foodBudgetLevel,
                                onDietTypeSelect = viewModel::selectDietType,
                                onMealsPerDayChange = viewModel::setMealsPerDay,
                                onCookTimeChange = viewModel::setCookTimeMinutes,
                                onFoodBudgetSelect = viewModel::selectFoodBudgetLevel,
                                isDarkTheme = isDarkTheme
                            )
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

                    // ── Nút điều hướng dưới cùng ──
                    val isLast = pagerState.currentPage == ONBOARDING_STEP_COUNT - 1
                    val canGo = uiState.canProceed(pagerState.currentPage)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Nút Quay lại (ẩn ở trang đầu)
                        if (pagerState.currentPage > 0) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                            ) {
                                Text("Quay lại", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VividOrange,
                                disabledContainerColor = VividOrange.copy(alpha = 0.35f)
                            )
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(Modifier.size(22.dp), TextWhite, strokeWidth = 2.5.dp)
                            } else {
                                Text(
                                    when {
                                        isLast -> "Hoàn tất"
                                        pagerState.currentPage == 0 -> "Bắt đầu"
                                        else -> "Tiếp theo"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextWhite
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
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

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
            .background(if (isDarkTheme) CharcoalSurface else PearlSurface)
            .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(16.dp))
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
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) TextWhite else TextInkPrimary)
            Text(desc, fontSize = 12.sp, color = if (isDarkTheme) TextMuted else TextInkMuted, lineHeight = 16.sp)
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
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

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
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

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
                    isSelected -> if (isDarkTheme) CharcoalCardElevated else PearlCardElevated
                    else -> if (isDarkTheme) CharcoalSurface else PearlSurface
                }
                val borderCol = if (isSelected) VividOrange else (if (isDarkTheme) CharcoalBorder else PearlBorder)

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
                            .background(if (isSelected) VividOrangeSoft else (if (isDarkTheme) CharcoalCard else PearlCard)),
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
//  TRANG 5: LIFESTYLE (giờ ngủ, mức stress, supplements, mức vận động)
// ══════════════════════════════════════════════════════════════
@Composable
private fun LifestylePage(
    sleepHours: Float,
    stressLevel: String,
    takesSupplements: Boolean,
    activityLevel: String,
    onSleepHoursChange: (Float) -> Unit,
    onStressSelect: (String) -> Unit,
    onSupplementsChange: (Boolean) -> Unit,
    onActivitySelect: (String) -> Unit,
    isDarkTheme: Boolean
) {
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Lối sống của bạn?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Giúp CalAI cá nhân hóa lời khuyên phù hợp hơn",
            fontSize = 14.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        SectionLabel("Bạn ngủ trung bình bao nhiêu giờ mỗi đêm?", textPrimary)
        Spacer(Modifier.height(10.dp))
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

        Spacer(Modifier.height(16.dp))
        SectionLabel("Mức độ căng thẳng gần đây?", textPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("LOW" to "Thấp", "MEDIUM" to "Trung bình", "HIGH" to "Cao").forEach { (key, label) ->
                SelectionPill(label, stressLevel == key, isDarkTheme, Modifier.weight(1f)) { onStressSelect(key) }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("Bạn có dùng thực phẩm bổ sung (supplements) không?", textPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionPill("Có", takesSupplements, isDarkTheme, Modifier.weight(1f)) { onSupplementsChange(true) }
            SelectionPill("Không", !takesSupplements, isDarkTheme, Modifier.weight(1f)) { onSupplementsChange(false) }
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("Mức độ vận động hàng ngày?", textPrimary)
        Spacer(Modifier.height(10.dp))
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
        Spacer(Modifier.height(8.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 6: NUTRITION (chế độ ăn, số bữa, thời gian nấu, ngân sách)
// ══════════════════════════════════════════════════════════════
@Composable
private fun NutritionPage(
    dietType: String,
    mealsPerDay: Int,
    cookTimeMinutes: Int,
    foodBudgetLevel: String,
    onDietTypeSelect: (String) -> Unit,
    onMealsPerDayChange: (Int) -> Unit,
    onCookTimeChange: (Int) -> Unit,
    onFoodBudgetSelect: (String) -> Unit,
    isDarkTheme: Boolean
) {
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Thói quen ăn uống?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "CalAI gợi ý thực đơn phù hợp với khẩu vị và thời gian của bạn",
            fontSize = 14.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        SectionLabel("Chế độ ăn của bạn?", textPrimary)
        Spacer(Modifier.height(10.dp))
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

        Spacer(Modifier.height(12.dp))
        SectionLabel("Số bữa ăn mỗi ngày?", textPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (2..6).forEach { n ->
                SelectionPill("$n bữa", mealsPerDay == n, isDarkTheme, Modifier.weight(1f)) { onMealsPerDayChange(n) }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("Thời gian nấu ăn có sẵn mỗi bữa?", textPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 45, 60, 90).forEach { m ->
                SelectionPill("$m'", cookTimeMinutes == m, isDarkTheme, Modifier.weight(1f)) { onCookTimeChange(m) }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("Mức ngân sách ăn uống?", textPrimary)
        Spacer(Modifier.height(10.dp))
        listOf(
            "LOW" to ("Tiết kiệm" to "Ưu tiên món rẻ, nguyên liệu đơn giản"),
            "MEDIUM" to ("Vừa phải" to "Cân đối chi phí và chất lượng"),
            "HIGH" to ("Thoải mái" to "Không giới hạn nhiều về chi phí")
        ).forEach { (key, pair) ->
            val (label, desc) = pair
            MacroStyleOptionRow(label, desc, foodBudgetLevel == key, isDarkTheme) { onFoodBudgetSelect(key) }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
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
                .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(24.dp))
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
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted

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
            color = if (isDarkTheme) TextWhite else TextInkPrimary,
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
