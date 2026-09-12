package com.calai.app.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RestaurantMenu
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
 * Luồng Onboarding v2 — 13 màn theo CHANGELOG_Onboarding_v2.md (HorizontalPager, vuốt qua lại,
 * tuần tự bắt buộc). Rút gọn từ 36 trường/26 màn xuống còn 15 trường bắt buộc + 2 trường tuỳ chọn
 * có điều kiện, gom nhóm theo 3 bước lớn (Cơ thể / Mục tiêu-Dinh dưỡng / Tập luyện):
 * 0.  OVERVIEW                      — Giới thiệu 3 bước sắp tới
 * 1.  STEP1_INTRO                   — Intro "Về cơ thể bạn"
 * 2.  BODY_METRICS                  — Form gộp: giới tính, ngày sinh, chiều cao, cân nặng, đơn vị
 * 3.  BODY_FAT                      — % mỡ cơ thể: nhập tay hoặc đo bằng thước dây (US Navy)
 * 4.  STEP2_INTRO                   — Intro bước Mục tiêu & Dinh dưỡng
 * 5.  GOAL                          — Mục tiêu (dual-mode: chung / chính xác)
 * 6.  DIET_STYLE                    — Phong cách ăn (macro_style, 8 lựa chọn)
 * 7.  ALLERGIES                     — Dị ứng thực phẩm (multi-select, optional)
 * 8.  STEP3_INTRO                   — Intro bước Tập luyện
 * 9.  TRAINING_EXPERIENCE_GOAL      — Kinh nghiệm + mục tiêu tập luyện (gộp)
 * 10. TRAINING_SCHEDULE_EQUIPMENT   — Số buổi/tuần + thiết bị (gộp; suy ra activityLevel tự động)
 * 11. INJURIES                      — Chấn thương/hạn chế vận động (giữ riêng vì lý do an toàn)
 * 12. SUMMARY                       — Tổng kết sau khi lưu thành công
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
                        .imePadding()
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

                    // Thanh tiến độ segment theo nhóm: Overview | Bước 1 (Cơ thể) | Bước 2 (Mục tiêu/Dinh dưỡng)
                    // | Bước 3 (Tập luyện) | Injuries | Summary — trực quan hơn 1 thanh liền.
                    val segmentBounds = listOf(0, 1, 4, 5, 8, 9, 11, ONBOARDING_STEP_COUNT)
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
                                    .weight(1f)
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

                    // Tự động lướt sang câu tiếp theo sau khi chọn (chỉ áp cho câu chọn-1-đáp-án đơn giản,
                    // không áp cho trang có wheel picker, nhập số, multi-select hay form gộp nhiều mục).
                    fun autoAdvance() {
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(220)
                            if (pagerState.currentPage < ONBOARDING_STEP_COUNT - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }

                    // ── 13 trang nội dung (HorizontalPager) ──
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        userScrollEnabled = true
                    ) { page ->
                        when (page) {
                            0 -> OverviewPage(isDarkTheme)
                            1 -> StepIntroPage(
                                stepNumber = 1,
                                icon = Icons.Default.FitnessCenter,
                                title = "Về cơ thể bạn",
                                subtitle = "Vài số đo cơ bản để NutriWise tính chỉ số trao đổi chất (BMR/TDEE) chính xác"
                            )
                            2 -> BodyMetricsPage(uiState, viewModel, isDarkTheme)
                            3 -> BodyFatPage(uiState, viewModel, isDarkTheme)
                            4 -> StepIntroPage(
                                stepNumber = 2,
                                icon = Icons.Default.RestaurantMenu,
                                title = "Mục tiêu & Dinh dưỡng",
                                subtitle = "Cho biết bạn muốn hướng tới điều gì và ăn uống theo phong cách nào"
                            )
                            5 -> GoalPage(uiState, viewModel, isDarkTheme)
                            6 -> DietStylePage(uiState.macroStyle, viewModel::selectMacroStyle, isDarkTheme)
                            7 -> AllergiesPage(uiState.allergies, viewModel::toggleAllergy, isDarkTheme)
                            8 -> StepIntroPage(
                                stepNumber = 3,
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                title = "Về việc tập luyện",
                                subtitle = "Giúp AI Coach thiết kế lịch tập và chương trình phù hợp với bạn"
                            )
                            9 -> TrainingExperienceGoalPage(uiState, viewModel, isDarkTheme)
                            10 -> TrainingScheduleEquipmentPage(uiState, viewModel, isDarkTheme)
                            11 -> InjuriesPage(
                                injuries = uiState.injuries,
                                injuriesOtherNote = uiState.injuriesOtherNote,
                                onInjuryToggle = viewModel::toggleInjury,
                                onInjuriesOtherNoteChange = viewModel::setInjuriesOtherNote,
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

                    // ── Nút điều hướng dưới cùng — 2 khối tách riêng (không còn chung 1 box) ──
                    val isLast = pagerState.currentPage == ONBOARDING_STEP_COUNT - 1
                    val canGo = uiState.canProceed(pagerState.currentPage)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Khối Quay lại — pill viền riêng (ẩn ở trang đầu)
                        if (pagerState.currentPage > 0) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                modifier = Modifier
                                    .weight(0.85f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = textPrimary
                                )
                            ) {
                                Text("‹ Quay lại", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }

                        // Khối Tiếp theo / Hoàn tất — pill cam riêng
                        Button(
                            onClick = {
                                if (isLast) viewModel.submit()
                                else coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            enabled = canGo && !uiState.isSaving,
                            modifier = Modifier
                                .weight(if (pagerState.currentPage > 0) 1.15f else 1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VividOrange,
                                disabledContainerColor = VividOrange.copy(alpha = 0.35f)
                            )
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(Modifier.size(20.dp), TextWhite, strokeWidth = 2.5.dp)
                            } else {
                                Text(
                                    when {
                                        isLast -> "Hoàn tất »"
                                        pagerState.currentPage == 0 -> "Bắt đầu »"
                                        else -> "Tiếp theo »"
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
//  TRANG 0: OVERVIEW — giới thiệu 3 bước sắp tới
// ══════════════════════════════════════════════════════════════
@Composable
private fun OverviewPage(isDarkTheme: Boolean) {
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
            "Thiết lập tài khoản của bạn",
            fontSize = 26.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Chỉ mất khoảng 2 phút qua 3 bước ngắn gọn để NutriWise cá nhân hóa mục tiêu cho bạn.",
            fontSize = 14.5.sp,
            color = textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(36.dp))

        listOf(
            "1. Về cơ thể bạn" to "Giới tính, ngày sinh, chiều cao, cân nặng và % mỡ cơ thể",
            "2. Mục tiêu & Dinh dưỡng" to "Mục tiêu cân nặng, phong cách ăn và dị ứng thực phẩm",
            "3. Tập luyện" to "Kinh nghiệm, lịch tập, thiết bị và chấn thương (nếu có)"
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
//  TRANG INTRO DÙNG CHUNG cho 3 bước lớn (1/2/3) — không có input
// ══════════════════════════════════════════════════════════════
@Composable
private fun StepIntroPage(
    stepNumber: Int,
    icon: ImageVector,
    title: String,
    subtitle: String
) {
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
                .clip(RoundedCornerShape(12.dp))
                .background(VividOrangeSoft)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                "BƯỚC $stepNumber / 3",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VividOrange
            )
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(VividOrangeSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = VividOrange, modifier = Modifier.size(44.dp))
        }

        Spacer(Modifier.height(28.dp))

        Text(
            title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            subtitle,
            fontSize = 14.sp,
            color = textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 2: BODY_METRICS — gộp Giới tính/Ngày sinh/Chiều cao/Cân nặng/Đơn vị
// ══════════════════════════════════════════════════════════════
@Composable
private fun BodyMetricsPage(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel,
    isDarkTheme: Boolean
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val daysInMonth = remember(uiState.birthMonth, uiState.birthYear) {
        getDaysInMonth(uiState.birthMonth, uiState.birthYear)
    }
    LaunchedEffect(daysInMonth) {
        if (uiState.birthDay > daysInMonth) {
            viewModel.setDateOfBirth(daysInMonth, uiState.birthMonth, uiState.birthYear)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Về cơ thể bạn", fontSize = 22.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Dùng để tính chính xác chỉ số trao đổi chất (BMR & TDEE)",
            fontSize = 13.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        // ── Đơn vị đo (METRIC/IMPERIAL) — toggle đơn giản, không chặn Next ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            listOf("METRIC" to "kg / cm", "IMPERIAL" to "lb / ft").forEach { (key, label) ->
                val isSelected = uiState.units == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isSelected) VividOrange else Color.Transparent)
                        .clickable { viewModel.selectUnits(key) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) TextWhite else textSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionDivider("Giới tính")
        Spacer(Modifier.height(12.dp))

        // Segmented control 2 lựa chọn (Nam/Nữ) theo đặc tả UI chính thức (màn 22)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            listOf("MALE" to "Nam", "FEMALE" to "Nữ").forEach { (key, label) ->
                val isSelected = uiState.gender == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isSelected) VividOrange else Color.Transparent)
                        .clickable { viewModel.selectGender(key) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TextWhite else textSecondary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionDivider("Ngày sinh")
        Spacer(Modifier.height(12.dp))

        ValueDisplay(
            value = "%02d/%02d/%d".format(uiState.birthDay, uiState.birthMonth, uiState.birthYear),
            unit = "",
            isDarkTheme = isDarkTheme
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Ngày", "Tháng", "Năm").forEach { label ->
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WheelPicker3D(
                value = uiState.birthDay.coerceIn(1, daysInMonth),
                onValueChange = { viewModel.setDateOfBirth(it, uiState.birthMonth, uiState.birthYear) },
                range = 1..daysInMonth,
                modifier = Modifier.weight(1f),
                itemHeight = 44.dp,
                isDarkTheme = isDarkTheme
            )
            WheelPicker3D(
                value = uiState.birthMonth,
                onValueChange = { newMonth ->
                    val maxDay = getDaysInMonth(newMonth, uiState.birthYear)
                    viewModel.setDateOfBirth(uiState.birthDay.coerceAtMost(maxDay), newMonth, uiState.birthYear)
                },
                range = 1..12,
                modifier = Modifier.weight(1f),
                itemHeight = 44.dp,
                isDarkTheme = isDarkTheme
            )
            WheelPicker3D(
                value = uiState.birthYear,
                onValueChange = { newYear ->
                    val maxDay = getDaysInMonth(uiState.birthMonth, newYear)
                    viewModel.setDateOfBirth(uiState.birthDay.coerceAtMost(maxDay), uiState.birthMonth, newYear)
                },
                range = 1940..2015,
                modifier = Modifier.weight(1.2f),
                itemHeight = 44.dp,
                isDarkTheme = isDarkTheme
            )
        }

        Spacer(Modifier.height(24.dp))
        SectionDivider("Chiều cao")
        Spacer(Modifier.height(12.dp))
        ValueDisplay(value = "${uiState.heightCm.toInt()}", unit = "cm", isDarkTheme = isDarkTheme)
        Spacer(Modifier.height(8.dp))
        WheelPicker3D(
            value = uiState.heightCm.toInt(),
            onValueChange = { viewModel.setHeightCm(it.toFloat()) },
            range = 100..220,
            modifier = Modifier.fillMaxWidth(0.55f),
            isDarkTheme = isDarkTheme
        )

        Spacer(Modifier.height(24.dp))
        SectionDivider("Cân nặng hiện tại")
        Spacer(Modifier.height(12.dp))
        ValueDisplay(value = "${uiState.weightKg.toInt()}", unit = "kg", isDarkTheme = isDarkTheme)
        Spacer(Modifier.height(8.dp))
        WheelPicker3D(
            value = uiState.weightKg.toInt(),
            onValueChange = { viewModel.setWeightKg(it.toFloat()) },
            range = 30..180,
            modifier = Modifier.fillMaxWidth(0.55f),
            isDarkTheme = isDarkTheme
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionDivider(label: String) {
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
        Text(
            label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = textSecondary,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 3: BODY_FAT — manual hoặc navy-tape (2 nhánh)
// ══════════════════════════════════════════════════════════════
@Composable
private fun BodyFatPage(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel,
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
        Text("Tỷ lệ mỡ cơ thể (nếu biết)?", fontSize = 22.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Giúp tính toán chính xác hơn — có thể bỏ qua nếu chưa biết",
            fontSize = 13.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        // ── 2 lựa chọn dạng Large Card (theo đặc tả UI chính thức, màn 23) ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                "manual" to ("Tôi có số đo chính xác" to "Đã biết % mỡ cơ thể"),
                "navy_tape" to ("Đo bằng thước dây" to "Ước tính từ vòng eo/cổ/hông")
            ).forEach { (key, pair) ->
                val (title, desc) = pair
                val isSelected = uiState.bodyFatSource == key
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else surface)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) VividOrange else border, RoundedCornerShape(18.dp))
                        .clickable { viewModel.selectBodyFatSource(key) }
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = if (isSelected) VividOrange else textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(desc, fontSize = 11.sp, textAlign = TextAlign.Center, color = textSecondary, lineHeight = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (uiState.bodyFatSource == "manual") {
            // Nhập tay 0-100, 1 chữ số thập phân — dùng state chuỗi cục bộ để tránh bug con trỏ Float↔String
            var bodyFatText by remember(uiState.bodyFatSource) {
                mutableStateOf(uiState.bodyFatPercent?.let { "%.1f".format(it) } ?: "")
            }
            OutlinedTextField(
                value = bodyFatText,
                onValueChange = { text ->
                    bodyFatText = text
                    text.toFloatOrNull()?.let { if (it in 0f..100f) viewModel.selectBodyFatPercent(it) }
                },
                placeholder = { Text("VD: 18.5", color = textSecondary) },
                suffix = { Text("%", color = textSecondary) },
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
        } else {
            // Navy-tape: neck, waist, (hip nếu nữ)
            var neckText by remember(uiState.bodyFatSource) { mutableStateOf(uiState.neckCm?.toString() ?: "") }
            var waistText by remember(uiState.bodyFatSource) { mutableStateOf(uiState.waistCm?.toString() ?: "") }
            var hipText by remember(uiState.bodyFatSource) { mutableStateOf(uiState.hipCm?.toString() ?: "") }

            fun pushMeasurements() {
                viewModel.setNavyTapeMeasurements(
                    neckCm = neckText.toFloatOrNull(),
                    waistCm = waistText.toFloatOrNull(),
                    hipCm = if (uiState.gender == "FEMALE") hipText.toFloatOrNull() else null
                )
            }

            OutlinedTextField(
                value = neckText,
                onValueChange = { neckText = it; pushMeasurements() },
                label = { Text("Vòng cổ (cm)", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = surface, unfocusedContainerColor = surface,
                    focusedBorderColor = VividOrange, unfocusedBorderColor = border
                )
            )
            MeasurementHint("Đo ngay dưới yết hầu, thước ôm sát nhưng không siết chặt")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = waistText,
                onValueChange = { waistText = it; pushMeasurements() },
                label = { Text("Vòng eo (cm)", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = surface, unfocusedContainerColor = surface,
                    focusedBorderColor = VividOrange, unfocusedBorderColor = border
                )
            )
            MeasurementHint("Đo ngang rốn khi thở ra tự nhiên")
            if (uiState.gender == "FEMALE") {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = hipText,
                    onValueChange = { hipText = it; pushMeasurements() },
                    label = { Text("Vòng hông (cm)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = surface, unfocusedContainerColor = surface,
                        focusedBorderColor = VividOrange, unfocusedBorderColor = border
                    )
                )
                MeasurementHint("Đo tại điểm rộng nhất của vòng hông")
            }

            uiState.bodyFatPercent?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    "≈ %.1f%% mỡ cơ thể (tính theo công thức US Navy)".format(it),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VividOrange,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { viewModel.selectBodyFatPercent(null) }) {
            Text("Bỏ qua, tôi chưa biết", color = textSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 5: GOAL (Q070) — dual-mode: chung / chính xác
// ══════════════════════════════════════════════════════════════
@Composable
private fun GoalPage(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel,
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
        Text("Mục tiêu của bạn?", fontSize = 22.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "NutriWise tối ưu lượng calo nạp mỗi ngày theo mục tiêu này",
            fontSize = 13.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        // ── Toggle 2 chế độ ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            listOf("general" to "Mục tiêu chung", "precise" to "Mục tiêu chính xác").forEach { (key, label) ->
                val isSelected = uiState.goalMode == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isSelected) VividOrange else Color.Transparent)
                        .clickable { viewModel.selectGoalMode(key) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TextWhite else textSecondary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (uiState.goalMode == "general") {
            val choices = listOf(
                Triple("lose_fat", "Giảm mỡ", "Giảm mỡ thừa, cơ thể thon gọn và săn chắc") to Icons.AutoMirrored.Filled.TrendingDown,
                Triple("maintain", "Duy trì", "Giữ mức cân hiện tại, sống khỏe mỗi ngày") to Icons.AutoMirrored.Filled.TrendingFlat,
                Triple("build_muscle", "Tăng cơ", "Xây dựng cơ bắp, tăng thể trọng khoa học") to Icons.AutoMirrored.Filled.TrendingUp
            )
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                choices.forEach { (info, icon) ->
                    val (key, title, subtitle) = info
                    val isSelected = uiState.generalChoice == key
                    val cardBg = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else surface
                    val borderCol = if (isSelected) VividOrange else border

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBg)
                            .border(if (isSelected) 2.dp else 1.dp, borderCol, RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectGeneralGoalChoice(key) }
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) VividOrangeSoft else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = if (isSelected) VividOrange else textSecondary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isSelected) VividOrange else textPrimary)
                            Spacer(Modifier.height(3.dp))
                            Text(subtitle, fontSize = 12.sp, color = textSecondary, lineHeight = 16.sp)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, null, tint = VividOrange, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        } else {
            SectionLabel("Cân nặng mục tiêu của bạn?", textPrimary)
            Spacer(Modifier.height(10.dp))
            // State chuỗi cục bộ — tránh vòng lặp Float→String làm con trỏ nhảy khi gõ.
            var targetWeightText by remember {
                mutableStateOf(if (uiState.targetWeightKg > 0f) uiState.targetWeightKg.toInt().toString() else "")
            }
            OutlinedTextField(
                value = targetWeightText,
                onValueChange = { text ->
                    targetWeightText = text
                    text.toFloatOrNull()?.let(viewModel::setTargetWeightKg)
                },
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
                    RateSelectionPill(rate, uiState.weightRateKgPerWeek == rate, isDarkTheme, Modifier.weight(1f)) {
                        viewModel.selectWeightRate(rate)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "≈ ${(uiState.weightRateKgPerWeek * 7700 / 7).toInt()} kcal thâm hụt/thặng dư mỗi ngày",
                fontSize = 11.5.sp,
                color = textSecondary
            )

            // Tóm tắt động: số tuần dự kiến đạt mục tiêu (tính từ |target-current|/rate)
            val diffKg = kotlin.math.abs(uiState.targetWeightKg - uiState.weightKg)
            if (uiState.weightRateKgPerWeek > 0f && diffKg > 0.01f) {
                val weeks = kotlin.math.ceil(diffKg / uiState.weightRateKgPerWeek).toInt()
                Spacer(Modifier.height(10.dp))
                Text(
                    "Đạt được sau khoảng $weeks tuần",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividOrange,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Cảnh báo mềm nếu tốc độ ngoài dải khuyến nghị (0.25-0.75 kg/tuần)
            if (uiState.weightRateKgPerWeek !in 0.25f..0.75f && uiState.weightRateKgPerWeek > 0f) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Lưu ý: tốc độ này nhanh hơn mức khuyến nghị thông thường, cân nhắc điều chỉnh để bền vững hơn",
                    fontSize = 11.sp,
                    color = CoralWarning,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 6: DIET_STYLE (Q071) — macro_style, 8 lựa chọn gộp
// ══════════════════════════════════════════════════════════════
@Composable
private fun DietStylePage(macroStyle: String, onSelect: (String) -> Unit, isDarkTheme: Boolean) {
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    QuestionPageScaffold("Bạn ưu tiên phong cách ăn nào?", "Quyết định cách phân bổ tỷ lệ Đạm/Tinh bột/Béo và gợi ý món ăn") {
        listOf(
            "any" to ("Bất cứ điều gì" to "Ăn đa dạng, không loại trừ nhóm thực phẩm nào"),
            "balanced" to ("Cân bằng" to "Không loại trừ — 30% đạm · 40% tinh bột · 30% béo"),
            "high_protein" to ("Nhiều đạm" to "Ưu tiên protein cho tăng cơ/giảm mỡ"),
            "low_carb_high_fat" to ("Ít tinh bột, nhiều béo" to "Không gồm tinh bột tinh chế, đường"),
            "keto" to ("Keto" to "Không gồm ngũ cốc giàu tinh bột, đường"),
            "mediterranean" to ("Địa Trung Hải" to "Không gồm thịt đỏ, thịt chế biến"),
            "vegan" to ("Thuần chay" to "Không gồm mọi sản phẩm từ động vật"),
            "vegetarian" to ("Ăn chay" to "Không gồm thịt, cá")
        ).forEach { (key, pair) ->
            val (label, desc) = pair
            MacroStyleOptionRow(label, desc, macroStyle == key, isDarkTheme) { onSelect(key) }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Không tìm thấy phong cách phù hợp? Chọn \"Bất cứ điều gì\" để tuỳ chỉnh chi tiết sau trong Cài đặt",
            fontSize = 11.5.sp,
            color = textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 7: ALLERGIES (Q050) — multi-select chip, optional
// ══════════════════════════════════════════════════════════════
@Composable
private fun AllergiesPage(allergies: List<String>, onToggle: (String) -> Unit, isDarkTheme: Boolean) {
    QuestionPageScaffold("Bạn có dị ứng thực phẩm nào không?", "AI sẽ tránh gợi ý món ăn chứa các thành phần này (có thể bỏ qua)") {
        listOf(
            "DAIRY" to "Sản phẩm từ sữa", "EGG" to "Trứng", "FISH" to "Cá",
            "GLUTEN" to "Gluten", "PEANUT" to "Đậu phộng", "SESAME" to "Mè",
            "SHELLFISH" to "Hải sản có vỏ", "SOY" to "Đậu nành", "TREE_NUT" to "Hạt cây"
        ).chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (key, label) ->
                    SelectionPill(label, allergies.contains(key), isDarkTheme, Modifier.weight(1f)) { onToggle(key) }
                }
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PastelMint.copy(alpha = 0.16f))
                .padding(14.dp)
        ) {
            Text(
                "Bạn có thể thiết lập danh sách loại trừ chi tiết hơn trong Cài đặt sau này",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 9: TRAINING_EXPERIENCE_GOAL — gộp kinh nghiệm + mục tiêu tập
// ══════════════════════════════════════════════════════════════
@Composable
private fun TrainingExperienceGoalPage(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel,
    isDarkTheme: Boolean
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kinh nghiệm & mục tiêu tập luyện?", fontSize = 22.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Giúp AI hiệu chỉnh độ khó chương trình tập phù hợp",
            fontSize = 13.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        SectionDivider("Kinh nghiệm tập luyện")
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("BEGINNER" to "Mới bắt đầu", "INTERMEDIATE" to "Trung bình", "ADVANCED" to "Nâng cao").forEach { (key, label) ->
                SelectionPill(label, uiState.trainingExperience == key, isDarkTheme, Modifier.weight(1f)) {
                    viewModel.selectTrainingExperience(key)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionDivider("Mục tiêu tập luyện chính")
        Spacer(Modifier.height(12.dp))
        listOf(
            "BUILD_MUSCLE" to "Tăng cơ", "LOSE_FAT" to "Giảm mỡ", "STRENGTH" to "Tăng sức mạnh",
            "ENDURANCE" to "Tăng sức bền", "RECOMP" to "Vừa tăng cơ vừa giảm mỡ", "GENERAL_FITNESS" to "Sức khỏe tổng quát"
        ).chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (key, label) ->
                    SelectionPill(label, uiState.trainingGoal == key, isDarkTheme, Modifier.weight(1f)) {
                        viewModel.selectTrainingGoal(key)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Khối nâng cao (tuỳ chọn) — chỉ hiện khi kinh nghiệm khác "Mới bắt đầu" ──
        if (uiState.trainingExperience != "BEGINNER") {
            Spacer(Modifier.height(24.dp))
            SectionDivider("Câu hỏi nâng cao (tuỳ chọn)")
            Spacer(Modifier.height(12.dp))

            var squatText by remember { mutableStateOf(uiState.oneRepMaxSquatKg?.let { formatOneRepMax(it) } ?: "") }
            var benchText by remember { mutableStateOf(uiState.oneRepMaxBenchKg?.let { formatOneRepMax(it) } ?: "") }
            var deadliftText by remember { mutableStateOf(uiState.oneRepMaxDeadliftKg?.let { formatOneRepMax(it) } ?: "") }

            Text("1RM hiện tại (kg, nếu biết)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = squatText,
                    onValueChange = { squatText = it; viewModel.setOneRepMaxSquat(it.toFloatOrNull()) },
                    label = { Text("Squat", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = benchText,
                    onValueChange = { benchText = it; viewModel.setOneRepMaxBench(it.toFloatOrNull()) },
                    label = { Text("Bench", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = deadliftText,
                    onValueChange = { deadliftText = it; viewModel.setOneRepMaxDeadlift(it.toFloatOrNull()) },
                    label = { Text("Deadlift", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Kiểu chia lịch ưa thích", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
            Spacer(Modifier.height(8.dp))
            listOf(
                "FULL_BODY" to "Full Body", "UPPER_LOWER" to "Upper-Lower",
                "PUSH_PULL_LEGS" to "Push-Pull-Legs", "BRO_SPLIT" to "Bro Split"
            ).chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (key, label) ->
                        SelectionPill(label, uiState.preferredSplit == key, isDarkTheme, Modifier.weight(1f)) {
                            viewModel.selectPreferredSplit(key)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            TextButton(onClick = {
                squatText = ""; benchText = ""; deadliftText = ""
                viewModel.clearOneRepMax()
                viewModel.selectPreferredSplit(null)
            }) {
                Text("Bỏ qua", color = textSecondary, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

// ══════════════════════════════════════════════════════════════
//  TRANG 10: TRAINING_SCHEDULE_EQUIPMENT — gộp buổi/tuần + thiết bị
//  (activityLevel giờ được suy ra tự động từ sessionsPerWeek, không hỏi riêng nữa)
// ══════════════════════════════════════════════════════════════
@Composable
private fun TrainingScheduleEquipmentPage(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel,
    isDarkTheme: Boolean
) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lịch tập & thiết bị của bạn?", fontSize = 22.sp, fontWeight = FontWeight.Black, color = textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Kết hợp kinh nghiệm để AI đề xuất lịch tập (split) và bài tập phù hợp",
            fontSize = 13.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        SectionDivider("Số buổi tập mong muốn mỗi tuần")
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "ONE_TO_TWO" to "1-2 buổi", "THREE_TO_FOUR" to "3-4 buổi",
                "FIVE_TO_SIX" to "5-6 buổi", "SEVEN" to "7 buổi"
            ).forEach { (key, label) ->
                SelectionPill(label, uiState.sessionsPerWeek == key, isDarkTheme, Modifier.weight(1f)) {
                    viewModel.selectSessionsPerWeek(key)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionDivider("Thiết bị/nơi tập bạn có sẵn")
        Spacer(Modifier.height(12.dp))
        listOf(
            "FULL_GYM" to "Gym đầy đủ", "BASIC_GYM" to "Gym cơ bản",
            "HOME_DUMBBELL" to "Tại nhà (có tạ đơn)", "BODYWEIGHT_ONLY" to "Chỉ trọng lượng cơ thể"
        ).chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (key, label) ->
                    SelectionPill(label, uiState.equipmentAccess == key, isDarkTheme, Modifier.weight(1f)) {
                        viewModel.selectEquipmentAccess(key)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
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
        lineHeight = 31.sp,
        fontWeight = FontWeight.Black,
        color = textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 12.dp)
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
//  TRANG 11: INJURIES (Q046) — giữ nguyên, tách riêng vì lý do an toàn
// ══════════════════════════════════════════════════════════════
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
        if (injuries.isEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Vui lòng xác nhận để đảm bảo an toàn khi tập",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = VividOrange,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Dòng hướng dẫn ngắn (thay cho icon (i) mở video) cho từng ô đo vòng cơ thể. */
@Composable
private fun MeasurementHint(text: String) {
    Text(
        "ⓘ $text",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = Modifier.padding(top = 3.dp, start = 2.dp)
    )
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
//  MÀN HÌNH TỔNG KẾT (sau khi API trả về thành công) — giữ nguyên
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
        Text("NutriWise đã tính mục tiêu dinh dưỡng tối ưu cho bạn.", fontSize = 13.5.sp, color = textSecondary, textAlign = TextAlign.Center)

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
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ══════════════════════════════════════════════════════════════
//  HELPERS — Layout chung cho hiển thị giá trị wheel picker
// ══════════════════════════════════════════════════════════════

/** Hiển thị giá trị lớn + đơn vị phía trên wheel picker. */
@Composable
private fun ValueDisplay(value: String, unit: String, isDarkTheme: Boolean) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            value,
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 48.sp
        )
        if (unit.isNotBlank()) {
            Spacer(Modifier.width(6.dp))
            Text(
                unit,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VividOrange,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

/** Định dạng số 1RM: bỏ phần thập phân nếu là số nguyên (VD: 80.0 -> "80"). */
private fun formatOneRepMax(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

// ── Hàm tính ngày trong tháng (hỗ trợ năm nhuận) ──

private fun getDaysInMonth(month: Int, year: Int): Int = when (month) {
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}
