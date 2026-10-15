package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.MealPlanDayDto
import com.calai.app.data.remote.dto.MealPlanItemDto
import com.calai.app.data.remote.dto.MealPlanSlotDto
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.theme.NwTheme
import com.calai.app.presentation.theme.OnPrimary
import com.calai.app.presentation.theme.PrimaryStrong
import com.calai.app.presentation.theme.VividOrange
import com.calai.app.presentation.viewmodel.MealPlanViewModel

private fun slotLabel(mealType: String) = when (mealType) {
    "BREAKFAST" -> "Bữa sáng"
    "LUNCH" -> "Bữa trưa"
    "DINNER" -> "Bữa tối"
    "SNACK" -> "Bữa phụ"
    else -> mealType
}

private fun fmtQuantity(q: Float): String = if (q % 1f == 0f) q.toInt().toString() else String.format("%.2f", q).trimEnd('0').trimEnd('.')

/** Màu nhận diện từng bữa: nền pastel (sáng) / nền tối pha màu (tối), kèm màu nhấn cho biểu tượng. */
private data class SlotStyle(val tintLight: Color, val accent: Color, val icon: ImageVector)

private fun slotStyle(mealType: String) = when (mealType) {
    "BREAKFAST" -> SlotStyle(Color(0xFFFFF2D9), Color(0xFFD9A441), Icons.Default.FreeBreakfast)
    "LUNCH" -> SlotStyle(Color(0xFFE2F5EF), Color(0xFF2FA58C), Icons.Default.LunchDining)
    "DINNER" -> SlotStyle(Color(0xFFEDE7FA), Color(0xFF8B6FD0), Icons.Default.DinnerDining)
    else -> SlotStyle(Color(0xFFFFE5DF), Color(0xFFE9745F), Icons.Default.Cookie)
}

private val ProteinTint = Color(0xFFE2F5EF)
private val CarbTint = Color(0xFFFFF2D9)
private val FatTint = Color(0xFFFFE5DF)
private val ProteinAccent = Color(0xFF2FA58C)
private val CarbAccent = Color(0xFFD9A441)
private val FatAccent = Color(0xFFE9745F)

/** Nền thẻ: pastel ở chế độ sáng, ở chế độ tối là màu nhấn pha loãng lên nền thẻ để vẫn tách khỏi nền đen. */
@Composable
private fun tinted(light: Color, accent: Color): Color =
    if (NwTheme.isDark) accent.copy(alpha = 0.16f).compositeOver(MaterialTheme.colorScheme.surface) else light

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
        if (!ui.needsProfile) SuggestMealCard()
        SectionTitle("Thực đơn hôm nay", "Các bữa được chia theo mục tiêu calo của bạn")

        when {
            ui.needsProfile -> InfoCard(
                text = "Hãy hoàn tất hồ sơ để có mục tiêu dinh dưỡng, thực đơn sẽ được dựng theo mục tiêu đó.",
                actionLabel = "Hoàn tất hồ sơ",
                onAction = onOpenProfile
            )
            ui.isLoadingToday && ui.today == null -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(28.dp), color = PrimaryStrong)
            }
            ui.today == null -> InfoCard(
                text = ui.todayError ?: "Không tải được thực đơn hôm nay",
                actionLabel = "Thử lại",
                onAction = viewModel::refreshToday
            )
            else -> {
                val today = ui.today!!
                today.message?.let { InfoCard(text = it) }
                SummaryCard(
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

        Spacer(Modifier.height(6.dp))
        SectionTitle("Kế hoạch dinh dưỡng", "Xem trước các ngày tới để chuẩn bị bữa ăn")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillChip("7 ngày", ui.planDays == 7) { viewModel.loadPlan(7) }
            PillChip("30 ngày", ui.planDays == 30) { viewModel.loadPlan(30) }
        }

        val plan = ui.plan
        when {
            ui.needsProfile -> Unit
            ui.isLoadingPlan && plan == null -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(28.dp), color = PrimaryStrong)
            }
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
                        PillChip(
                            label = "Ngày ${day.dayNumber}",
                            selected = index == ui.selectedDayIndex,
                            locked = day.locked
                        ) { viewModel.selectDay(index) }
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
                        Text(day.date, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        SummaryCard(
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
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Text(subtitle, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PillChip(label: String, selected: Boolean, locked: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) VividOrange else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) VividOrange else MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (locked) Icon(Icons.Default.Lock, null, modifier = Modifier.size(13.dp), tint = if (selected) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) OnPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoCard(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tinted(Color(0xFFFFF2D9), Color(0xFFD9A441)))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, null, tint = Color(0xFFD9A441), modifier = Modifier.size(20.dp))
            Text(text, fontSize = 13.5.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
        if (actionLabel != null && onAction != null) AppButton(text = actionLabel, onClick = onAction, height = 46.dp)
    }
}

/** Thẻ tổng quan: calo đã xếp / mục tiêu kèm thanh tiến độ, và 3 ô đa lượng mỗi ô một màu. */
@Composable
private fun SummaryCard(calories: Float, target: Float?, protein: Float, carb: Float, fat: Float) {
    val progress = if (target != null && target > 0f) (calories / target).coerceIn(0f, 1f) else 0f
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${calories.toInt()}", fontSize = 34.sp, fontWeight = FontWeight.Black, color = PrimaryStrong)
            Text(
                if (target != null) "/ ${target.toInt()} kcal" else "kcal",
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        if (target != null) {
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(VividOrange))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MacroTile("Đạm", protein, ProteinTint, ProteinAccent, Modifier.weight(1f))
            MacroTile("Tinh bột", carb, CarbTint, CarbAccent, Modifier.weight(1f))
            MacroTile("Béo", fat, FatTint, FatAccent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MacroTile(label: String, grams: Float, tintLight: Color, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tinted(tintLight, accent))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("${grams.toInt()}g", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
            Text(label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SlotCard(slot: MealPlanSlotDto, showStatus: Boolean, isLogging: Boolean, onLog: (() -> Unit)?) {
    val style = slotStyle(slot.mealType)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(tinted(style.tintLight, style.accent))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(style.accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) { Icon(style.icon, null, tint = style.accent, modifier = Modifier.size(21.dp)) }
            Text(slotLabel(slot.mealType), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text(
                "${slot.budgetCalories} kcal", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = if (NwTheme.isDark) 0.08f else 0.7f)).padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        if (showStatus) {
            val statusText = when (slot.status) {
                "LOGGED" -> if (slot.remainingCalories < 0) "Đã ăn, vượt ${-slot.remainingCalories} kcal so với dự kiến" else "Đã ăn"
                "PARTIAL" -> "Đã ăn ${slot.consumedCalories} kcal, còn ${slot.remainingCalories} kcal"
                else -> null
            }
            statusText?.let {
                Text(
                    it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F7A5A),
                    modifier = Modifier.clip(CircleShape).background(Color(0xFF2E9E6B).copy(alpha = 0.16f)).padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Các món nằm trong khối nền sáng để tách khỏi màu của bữa
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (NwTheme.isDark) MaterialTheme.colorScheme.surface else Color.White.copy(alpha = 0.85f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            slot.items.forEach { ItemRow(it, showStatus) }
        }

        if (slot.status == "PARTIAL") {
            slot.suggestionForRemaining?.let { rest ->
                if (rest.items.isNotEmpty()) {
                    Text("Gợi ý cho phần còn lại (${slot.remainingCalories} kcal):", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(if (NwTheme.isDark) MaterialTheme.colorScheme.surface else Color.White.copy(alpha = 0.85f)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) { rest.items.forEach { ItemRow(it, false) } }
                }
            }
        }

        if (!slot.withinTolerance && slot.items.isNotEmpty()) {
            Text("Kho món hiện có chưa ghép được đúng ngân sách bữa này.", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (onLog != null && slot.items.any { !it.logged } && slot.status != "LOGGED") {
            AppButton(
                text = if (isLogging) "Đang ghi..." else "Tôi đã ăn bữa này",
                onClick = onLog,
                enabled = !isLogging,
                modifier = Modifier.fillMaxWidth(),
                height = 46.dp,
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}

@Composable
private fun ItemRow(item: MealPlanItemDto, showLogged: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        if (showLogged && item.logged) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Đã ăn", modifier = Modifier.size(18.dp), tint = Color(0xFF2E9E6B))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${item.name} × ${fmtQuantity(item.quantity)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "${item.servingSize} · Đạm ${item.protein.toInt()}g · Tinh bột ${item.carb.toInt()}g · Béo ${item.fat.toInt()}g",
                fontSize = 11.5.sp, lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("${item.calories.toInt()} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false, color = PrimaryStrong)
    }
}

/** "Gợi ý món": món hợp phần calo/macro còn lại hôm nay. Bấm mới gọi AI (có hạn mức riêng). */
@Composable
private fun SuggestMealCard(viewModel: com.calai.app.presentation.viewmodel.SuggestMealViewModel = hiltViewModel()) {
    val st by viewModel.state.collectAsState()
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(tinted(Color(0xFFEDE7FA), Color(0xFF8B6FD0)))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF8B6FD0).copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF8B6FD0), modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Gợi ý món cho phần còn lại", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text("Món hợp lượng calo và đạm bạn còn thiếu hôm nay", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val result = st.result
        when {
            st.loading -> Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(26.dp), color = PrimaryStrong)
            }
            result != null -> {
                val gap = result.nutritionGap
                Text(
                    "Còn thiếu: ${gap.remainingCalories} kcal · Đạm ${gap.remainingProtein}g · Tinh bột ${gap.remainingCarbs}g · Béo ${gap.remainingFat}g",
                    fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
                )
                if (result.suggestions.isEmpty()) {
                    Text("Hôm nay bạn đã đủ calo, chưa cần thêm món.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                result.suggestions.forEach { m ->
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(if (NwTheme.isDark) MaterialTheme.colorScheme.surface else Color.White.copy(alpha = 0.85f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(m.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text("${m.calories} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryStrong, maxLines = 1, softWrap = false)
                        }
                        Text("${m.mealType} · Đạm ${m.protein}g · Tinh bột ${m.carbs}g · Béo ${m.fat}g", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (m.reason.isNotBlank()) Text(m.reason, fontSize = 12.5.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                if (result.advice.isNotBlank()) {
                    Text(result.advice, fontSize = 12.5.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            st.error != null -> Text(st.error!!, fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        AppButton(
            text = if (result != null || st.error != null) "Gợi ý lại" else "Gợi ý món cho tôi",
            onClick = viewModel::load,
            enabled = !st.loading,
            modifier = Modifier.fillMaxWidth(),
            height = 46.dp,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
