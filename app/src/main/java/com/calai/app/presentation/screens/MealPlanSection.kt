package com.calai.app.presentation.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.MealPlanDayDto
import com.calai.app.data.remote.dto.MealPlanItemDto
import com.calai.app.data.remote.dto.MealPlanSlotDto
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.viewmodel.MealPlanViewModel

private fun slotLabel(mealType: String) = when (mealType) {
    "BREAKFAST" -> "Bữa sáng"
    "LUNCH" -> "Bữa trưa"
    "DINNER" -> "Bữa tối"
    "SNACK" -> "Bữa phụ"
    else -> mealType
}

private fun fmtQuantity(q: Float): String = if (q % 1f == 0f) q.toInt().toString() else String.format("%.2f", q).trimEnd('0').trimEnd('.')

/**
 * Thực đơn hôm nay + kế hoạch 7/30 ngày (từ kho món an toàn đã lọc dị ứng và chế độ ăn).
 * Free xem đủ 7 ngày; kế hoạch 30 ngày chỉ xem trước 7 ngày đầu, các ngày sau hiện khoá (đặc tả thực đơn 1.9).
 */
@Composable
fun MealPlanSection(
    onOpenPremium: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.message) {
        ui.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Thực đơn hôm nay", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        when {
            ui.needsProfile -> InfoCard(
                text = "Hãy hoàn tất hồ sơ để có mục tiêu dinh dưỡng, thực đơn sẽ được dựng theo mục tiêu đó.",
                actionLabel = "Hoàn tất hồ sơ",
                onAction = onOpenProfile
            )
            ui.isLoadingToday && ui.today == null -> CircularProgressIndicator(Modifier.size(28.dp))
            ui.today == null -> InfoCard(
                text = ui.todayError ?: "Không tải được thực đơn hôm nay",
                actionLabel = "Thử lại",
                onAction = viewModel::refreshToday
            )
            else -> {
                val today = ui.today!!
                today.message?.let { InfoCard(text = it) }
                SummaryLine(
                    calories = today.totals.calories,
                    target = today.targetCalories,
                    protein = today.totals.protein,
                    carb = today.totals.carb,
                    fat = today.totals.fat
                )
                today.meals.forEach { slot ->
                    SlotCard(
                        slot = slot,
                        showStatus = true,
                        isLogging = ui.loggingMealType == slot.mealType,
                        onLog = { viewModel.logSlot(today.date, slot) }
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Kế hoạch dinh dưỡng", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = ui.planDays == 7, onClick = { viewModel.loadPlan(7) }, label = { Text("7 ngày") })
            FilterChip(selected = ui.planDays == 30, onClick = { viewModel.loadPlan(30) }, label = { Text("30 ngày") })
        }

        val plan = ui.plan
        when {
            ui.needsProfile -> Unit
            ui.isLoadingPlan && plan == null -> CircularProgressIndicator(Modifier.size(28.dp))
            plan == null -> InfoCard(
                text = ui.planError ?: "Không tải được kế hoạch",
                actionLabel = "Thử lại",
                onAction = { viewModel.loadPlan(ui.planDays) }
            )
            plan.days.isEmpty() -> InfoCard(text = plan.message ?: "Chưa có kế hoạch phù hợp")
            else -> {
                plan.message?.let { InfoCard(text = it) }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    plan.days.forEachIndexed { index, day ->
                        FilterChip(
                            selected = index == ui.selectedDayIndex,
                            onClick = { viewModel.selectDay(index) },
                            label = { Text("Ngày ${day.dayNumber}") },
                            leadingIcon = if (day.locked) {
                                { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
                plan.lockedFromDay?.let {
                    Text(
                        "Bạn đang xem trước ${plan.previewDays ?: 7} ngày đầu. Ngày $it trở đi dành cho Premium.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val day: MealPlanDayDto? = plan.days.getOrNull(ui.selectedDayIndex)
                if (day != null) {
                    if (day.locked) {
                        InfoCard(
                            text = "Mở Premium để xem toàn bộ kế hoạch dinh dưỡng cá nhân hóa 30 ngày.",
                            actionLabel = "Xem gói Premium",
                            onAction = onOpenPremium
                        )
                    } else {
                        Text(day.date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        SummaryLine(
                            calories = day.totals.calories,
                            target = plan.targetCalories,
                            protein = day.totals.protein,
                            carb = day.totals.carb,
                            fat = day.totals.fat
                        )
                        day.slots.forEach { SlotCard(slot = it, showStatus = false, isLogging = false, onLog = null) }
                    }
                }
            }
        }
        SnackbarHost(snackbar)
    }
}

@Composable
private fun InfoCard(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text, fontSize = 13.5.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        if (actionLabel != null && onAction != null) AppButton(text = actionLabel, onClick = onAction)
    }
}

@Composable
private fun SummaryLine(calories: Float, target: Float?, protein: Float, carb: Float, fat: Float) {
    val targetText = target?.let { " / ${it.toInt()}" } ?: ""
    Text(
        "Tổng ${calories.toInt()}$targetText kcal · Đạm ${protein.toInt()}g · Tinh bột ${carb.toInt()}g · Béo ${fat.toInt()}g",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SlotCard(slot: MealPlanSlotDto, showStatus: Boolean, isLogging: Boolean, onLog: (() -> Unit)?) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(slotLabel(slot.mealType), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("${slot.budgetCalories} kcal", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (showStatus) {
            val statusText = when (slot.status) {
                "LOGGED" -> if (slot.remainingCalories < 0) "Đã ăn, vượt ${-slot.remainingCalories} kcal so với dự kiến" else "Đã ăn"
                "PARTIAL" -> "Đã ăn ${slot.consumedCalories} kcal, còn ${slot.remainingCalories} kcal"
                else -> null
            }
            statusText?.let { Text(it, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.primary) }
        }

        slot.items.forEach { ItemRow(it, showStatus) }

        if (slot.status == "PARTIAL") {
            slot.suggestionForRemaining?.let { rest ->
                if (rest.items.isNotEmpty()) {
                    Text("Gợi ý cho phần còn lại (${slot.remainingCalories} kcal):", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    rest.items.forEach { ItemRow(it, false) }
                }
            }
        }

        if (!slot.withinTolerance && slot.items.isNotEmpty()) {
            Text("Kho món hiện có chưa ghép được đúng ngân sách bữa này.", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (onLog != null && slot.items.any { !it.logged } && slot.status != "LOGGED") {
            TextButton(onClick = onLog, enabled = !isLogging) {
                Text(if (isLogging) "Đang ghi..." else "Tôi đã ăn bữa này")
            }
        }
    }
}

@Composable
private fun ItemRow(item: MealPlanItemDto, showLogged: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
            if (showLogged && item.logged) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Đã ăn", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text("${item.name} × ${fmtQuantity(item.quantity)}", fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "${item.servingSize} · Đạm ${item.protein.toInt()}g · Tinh bột ${item.carb.toInt()}g · Béo ${item.fat.toInt()}g",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text("${item.calories.toInt()} kcal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    }
}
