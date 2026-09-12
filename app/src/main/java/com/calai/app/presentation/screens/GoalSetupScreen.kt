package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.MacroStyleOptionRow
import com.calai.app.presentation.components.RateSelectionPill
import com.calai.app.presentation.components.SelectionPill
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.GoalSetupViewModel

/**
 * Màn hình Mục tiêu & Chương trình (Goal Selection / Goal Change / Program Setup theo BRD).
 * Dùng chung field goal/targetWeightKg/weightRateKgPerWeek/macroStyle đã có sẵn ở PATCH users/me.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean = true,
    onOpenExpenditureDetail: () -> Unit = {}
) {
    val viewModel: GoalSetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val border = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) onBack()
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = surface,
            title = {
                Text("Xác nhận đổi mục tiêu?", fontWeight = FontWeight.Bold, color = textPrimary)
            },
            text = {
                Text(
                    "Đổi loại mục tiêu sẽ đặt lại lộ trình hiện tại. Hệ thống sẽ tính lại calo và macro mục tiêu phù hợp với bạn.",
                    color = textSecondary,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.save()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                ) {
                    Text("Đồng ý", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy", color = textSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Mục tiêu & Chương trình", fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VividOrange)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Xem trước mục tiêu hiện tại
            uiState.original?.let { profile ->
                SectionCard(surface, border, shadowColor) {
                    Text("Mục tiêu hiện tại", fontSize = 13.sp, color = textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${profile.targetCalories?.toInt() ?: 0} kcal/ngày",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = VividOrange
                    )
                    Text(
                        "P ${profile.targetProtein?.toInt() ?: 0}g · C ${profile.targetCarb?.toInt() ?: 0}g · F ${profile.targetFat?.toInt() ?: 0}g",
                        fontSize = 12.5.sp,
                        color = textSecondary
                    )
                    uiState.expenditure?.let { expenditure ->
                        Spacer(modifier = Modifier.height(10.dp))
                        val isHolding = expenditure.status == "HOLDING"
                        val badgeColor = if (isHolding) EmeraldSuccess else CoralWarning
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .clickable { onOpenExpenditureDetail() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Text(
                                if (isHolding) "Expenditure đã ổn định (Adaptive) ➔" else "Đang cập nhật Expenditure ➔",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(expenditure.message, fontSize = 11.sp, color = textSecondary)
                    }
                }
            }

            // 1. Loại mục tiêu
            Text("Bạn muốn giảm cân, duy trì hay tăng cân?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectionPill("Giảm cân", uiState.goal == "LOSE_WEIGHT", isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal("LOSE_WEIGHT") }
                SelectionPill("Duy trì", uiState.goal == "MAINTAIN", isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal("MAINTAIN") }
                SelectionPill("Tăng cân", uiState.goal == "GAIN_WEIGHT", isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal("GAIN_WEIGHT") }
            }

            // 2. Cân nặng mục tiêu
            Text("Cân nặng mục tiêu của bạn?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            OutlinedTextField(
                value = uiState.targetWeightKg,
                onValueChange = { viewModel.setTargetWeight(it) },
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

            // 3. Tốc độ thay đổi cân nặng
            Text("Tốc độ thay đổi cân nặng mong muốn?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { rate ->
                    RateSelectionPill(rate, uiState.weightRateKgPerWeek == rate, isDarkTheme, Modifier.weight(1f)) { viewModel.selectRate(rate) }
                }
            }
            Text(
                "≈ ${(uiState.weightRateKgPerWeek * 7700 / 7).toInt()} kcal thâm hụt/thặng dư mỗi ngày",
                fontSize = 11.5.sp,
                color = textSecondary
            )

            // 4. Phong cách Macro
            Text("Bạn ưu tiên phong cách ăn nào?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroStyleOptionRow("Cân bằng", "30% đạm · 40% tinh bột · 30% béo", uiState.macroStyle == "BALANCED", isDarkTheme) { viewModel.selectMacroStyle("BALANCED") }
                MacroStyleOptionRow("Nhiều tinh bột, ít béo", "30% đạm · 55% tinh bột · 15% béo", uiState.macroStyle == "HIGH_CARB_LOW_FAT", isDarkTheme) { viewModel.selectMacroStyle("HIGH_CARB_LOW_FAT") }
                MacroStyleOptionRow("Ít tinh bột, nhiều béo", "35% đạm · 20% tinh bột · 45% béo", uiState.macroStyle == "LOW_CARB_HIGH_FAT", isDarkTheme) { viewModel.selectMacroStyle("LOW_CARB_HIGH_FAT") }
                MacroStyleOptionRow("Keto", "25% đạm · 5% tinh bột · 70% béo", uiState.macroStyle == "KETO", isDarkTheme) { viewModel.selectMacroStyle("KETO") }
            }

            uiState.errorMessage?.let {
                Text(it, color = CoralWarning, fontSize = 12.5.sp)
            }

            com.calai.app.presentation.components.AppButton(
                text = "Lưu Mục Tiêu",
                onClick = {
                    if (viewModel.hasChanges()) showConfirmDialog = true else viewModel.save()
                },
                isLoading = uiState.isSaving,
                height = 52.dp,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun SectionCard(
    surface: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    shadowColor: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp), ambientColor = shadowColor, spotColor = shadowColor)
            .clip(RoundedCornerShape(22.dp))
            .background(surface)
            .border(1.dp, border, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(content = content)
    }
}

