package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    isDarkTheme: Boolean = true
) {
    val viewModel: GoalSetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val bg = if (isDarkTheme) ObsidianBackground else IvoryBackground
    val surface = if (isDarkTheme) CharcoalSurface else PearlCard
    val border = if (isDarkTheme) CharcoalBorder else PearlBorder
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) onBack()
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = surface,
            title = { Text("Bạn có chắc muốn thay đổi mục tiêu?", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Calo & macro mục tiêu hàng ngày sẽ được tính lại ngay theo lựa chọn mới.",
                    color = textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showConfirmDialog = false; viewModel.save() }) {
                    Text("Xác nhận", color = VividOrange, fontWeight = FontWeight.Bold)
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
                }
            }

            // 1. Loại mục tiêu
            Text("Bạn muốn giảm cân, duy trì hay tăng cân?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalPill("LOSE_WEIGHT", "Giảm cân", uiState.goal, isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal(it) }
                GoalPill("MAINTAIN", "Duy trì", uiState.goal, isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal(it) }
                GoalPill("GAIN_WEIGHT", "Tăng cân", uiState.goal, isDarkTheme, Modifier.weight(1f)) { viewModel.selectGoal(it) }
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
                    RatePill(rate, uiState.weightRateKgPerWeek, isDarkTheme, Modifier.weight(1f)) { viewModel.selectRate(it) }
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
                MacroStyleRow("BALANCED", "Cân bằng", "30% đạm · 40% tinh bột · 30% béo", uiState.macroStyle, isDarkTheme) { viewModel.selectMacroStyle(it) }
                MacroStyleRow("HIGH_CARB_LOW_FAT", "Nhiều tinh bột, ít béo", "30% đạm · 55% tinh bột · 15% béo", uiState.macroStyle, isDarkTheme) { viewModel.selectMacroStyle(it) }
                MacroStyleRow("LOW_CARB_HIGH_FAT", "Ít tinh bột, nhiều béo", "35% đạm · 20% tinh bột · 45% béo", uiState.macroStyle, isDarkTheme) { viewModel.selectMacroStyle(it) }
                MacroStyleRow("KETO", "Keto", "25% đạm · 5% tinh bột · 70% béo", uiState.macroStyle, isDarkTheme) { viewModel.selectMacroStyle(it) }
            }

            uiState.errorMessage?.let {
                Text(it, color = CoralWarning, fontSize = 12.5.sp)
            }

            Button(
                onClick = {
                    if (viewModel.hasChanges()) showConfirmDialog = true else viewModel.save()
                },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextWhite)
                } else {
                    Text("Lưu Mục Tiêu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
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

@Composable
private fun GoalPill(
    value: String,
    label: String,
    selected: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    val isSelected = value == selected
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) VividOrange else (if (isDarkTheme) CharcoalSurface else PearlCard))
            .border(1.dp, if (isSelected) VividOrange else (if (isDarkTheme) CharcoalBorder else PearlBorder), RoundedCornerShape(14.dp))
            .clickable { onSelect(value) }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TextWhite else (if (isDarkTheme) TextMuted else TextInkMuted)
        )
    }
}

@Composable
private fun RatePill(
    rate: Float,
    selected: Float,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (Float) -> Unit
) {
    val isSelected = rate == selected
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) VividOrange else (if (isDarkTheme) CharcoalSurface else PearlCard))
            .border(1.dp, if (isSelected) VividOrange else (if (isDarkTheme) CharcoalBorder else PearlBorder), RoundedCornerShape(12.dp))
            .clickable { onSelect(rate) }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "${rate} kg",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TextWhite else (if (isDarkTheme) TextMuted else TextInkMuted)
        )
    }
}

@Composable
private fun MacroStyleRow(
    value: String,
    label: String,
    desc: String,
    selected: String,
    isDarkTheme: Boolean,
    onSelect: (String) -> Unit
) {
    val isSelected = value == selected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) VividOrangeSoft else (if (isDarkTheme) CharcoalSurface else PearlCard))
            .border(1.dp, if (isSelected) VividOrange else (if (isDarkTheme) CharcoalBorder else PearlBorder), RoundedCornerShape(14.dp))
            .clickable { onSelect(value) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) VividOrange else (if (isDarkTheme) TextWhite else TextInkPrimary)
            )
            Text(desc, fontSize = 11.5.sp, color = if (isDarkTheme) TextMuted else TextInkMuted)
        }
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(value) },
            colors = RadioButtonDefaults.colors(selectedColor = VividOrange)
        )
    }
}
