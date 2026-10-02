package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.data.remote.dto.HabitReminderDto
import com.calai.app.data.remote.dto.WaterTodayDto
import com.calai.app.presentation.theme.*
import java.util.Calendar

// ── Rule chọn bữa hiển thị ─────────────────────────────────────────────────

enum class MealPhase { IN_WINDOW, UPCOMING, TOMORROW }

data class MealReminderPick(val reminder: HabitReminderDto, val phase: MealPhase, val minutesUntil: Int)

private val MEAL_TYPES = setOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")

private fun toMinutes(hhmm: String?): Int? {
    val p = hhmm?.split(":") ?: return null
    val h = p.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
    val m = p.getOrNull(1)?.trim()?.toIntOrNull() ?: return null
    return h * 60 + m
}

/**
 * Chọn 1 bữa cho dashboard: đang trong khung giờ → bữa đó; chưa tới → bữa gần nhất sắp tới;
 * hết bữa hôm nay → bữa sớm nhất của ngày mai. Chỉ xét nhắc nhở bật + lặp vào ngày tương ứng.
 */
fun pickMealReminder(
    reminders: List<HabitReminderDto>,
    loggedMealTypes: Set<String> = emptySet(),
    now: Calendar = Calendar.getInstance()
): MealReminderPick? {
    val meals = reminders.filter { it.enabled && it.type in MEAL_TYPES }
    if (meals.isEmpty()) return null

    val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val isoToday = ((now.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1 // Calendar: CN=1 → ISO: T2=1..CN=7
    val isoTomorrow = isoToday % 7 + 1

    fun start(r: HabitReminderDto) = toMinutes(r.windowStart) ?: toMinutes(r.timeOfDay) ?: 0
    fun end(r: HabitReminderDto) = toMinutes(r.windowEnd) ?: (start(r) + 60)

    // Bữa đã có món log thật hôm nay thì bỏ qua, chuyển sang bữa kế tiếp
    val today = meals.filter { isoToday in it.repeatDays && it.type !in loggedMealTypes }.sortedBy(::start)
    today.firstOrNull { nowMin in start(it)..end(it) }?.let {
        return MealReminderPick(it, MealPhase.IN_WINDOW, 0)
    }
    today.firstOrNull { start(it) > nowMin }?.let {
        return MealReminderPick(it, MealPhase.UPCOMING, start(it) - nowMin)
    }
    val tomorrow = meals.filter { isoTomorrow in it.repeatDays }.minByOrNull(::start) ?: today.minByOrNull(::start)
    return tomorrow?.let { MealReminderPick(it, MealPhase.TOMORROW, 24 * 60 - nowMin + start(it)) }
}

// Rule tiến độ uống nước dùng chung với ReminderWorker — xem WaterProgressRule.kt

// ── UI ─────────────────────────────────────────────────────────────────────

@Composable
fun MealReminderCard(
    pick: MealReminderPick?,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val bg = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.10f), spotColor = Color.Black.copy(alpha = 0.10f))
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        if (pick == null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.NotificationsActive, null, tint = VividOrange, modifier = Modifier.size(20.dp))
                Text(
                    "Chưa có nhắc nhở bữa ăn — chạm để thiết lập",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        val r = pick.reminder
        val (statusText, statusColor) = when (pick.phase) {
            MealPhase.IN_WINDOW -> "Đến giờ ăn" to EmeraldSuccess
            MealPhase.UPCOMING -> (if (pick.minutesUntil < 60) "Còn ${pick.minutesUntil} phút" else "Còn ${pick.minutesUntil / 60}h${pick.minutesUntil % 60}p") to VividOrange
            MealPhase.TOMORROW -> "Ngày mai" to TextInkSecondary
        }
        val kcal = r.targetCalorieMax ?: r.targetCalorieMin
        val window = if (r.windowStart != null && r.windowEnd != null) "${r.windowStart} - ${r.windowEnd}" else r.timeOfDay

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(r.label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(999.dp))
                        .background(statusColor.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)
                ) { Text(statusText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = statusColor) }
            }
            Text(window, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = if (r.foods.isEmpty()) "Chạm để nhận gợi ý món phù hợp" else r.foods.joinToString(", ") { it.name.substringBefore(" (") },
            fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                Text(
                    text = if (kcal != null) "${kcal.toInt()} kcal" else "-- kcal",
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(EmeraldSuccess),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Add, "Thêm món", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
fun WaterCard(
    water: WaterTodayDto?,
    errorMessage: String?,
    onRetry: () -> Unit,
    isDarkTheme: Boolean,
    onAddGlass: () -> Unit,
    onRemoveGlass: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (water == null) {
        val placeholderShape = RoundedCornerShape(20.dp)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(placeholderShape)
                .background(if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White)
                .clickable(enabled = errorMessage != null, onClick = onRetry)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (errorMessage == null) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = VividOrange)
                Text("Đang tải lượng nước hôm nay...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Icon(Icons.Default.WifiOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Text("Không tải được dữ liệu nước — chạm để thử lại", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }
    val drankMl = water.totalMl
    val goalMl = water.goalMl
    val glassMl = water.glassMl
    val shape = RoundedCornerShape(20.dp)
    val bg = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
    val waterBlue = Color(0xFF3B9EF5)
    val totalGlasses = (goalMl / glassMl).coerceAtLeast(1)
    val filled = (drankMl / glassMl).coerceAtMost(totalGlasses)
    val behindMl = com.calai.app.domain.WaterProgressRule.expectedMl(goalMl) - drankMl

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.10f), spotColor = Color.Black.copy(alpha = 0.10f))
            .clip(shape)
            .background(bg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Uống nước", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "%.2f L / %.1f L".format(drankMl / 1000f, goalMl / 1000f),
                    fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Settings, "Cài đặt nhắc nước", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(totalGlasses) { index ->
                val isFilled = index < filled
                val isNext = index == filled
                WaterGlassIcon(
                    filled = isFilled,
                    isNext = isNext,
                    color = waterBlue,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = isNext || index == filled - 1) {
                            if (isNext) onAddGlass() else onRemoveGlass()
                        }
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        val hint = when {
            drankMl >= goalMl -> "Bạn đã đạt mục tiêu nước hôm nay" to EmeraldSuccess
            behindMl >= glassMl -> "Đang thiếu ${behindMl}ml so với tiến độ — uống thêm nhé" to VividOrange
            else -> "Đúng tiến độ, tiếp tục phát huy" to MaterialTheme.colorScheme.onSurfaceVariant
        }
        Text(hint.first, fontSize = 11.5.sp, color = hint.second)
    }
}

/** Ly nước vẽ bằng Canvas: thân ly hình thang, mực nước đầy + bọt khí khi đã uống, dấu + ở ly kế tiếp. */
@Composable
private fun WaterGlassIcon(filled: Boolean, isNext: Boolean, color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val glassW = minOf(w, h * 0.78f)
        val left = (w - glassW) / 2f
        val topInset = glassW * 0.06f
        val bottomInset = glassW * 0.16f
        val glassTop = h * 0.14f
        val glassBottom = h * 0.98f
        val r = glassW * 0.14f

        val body = androidx.compose.ui.graphics.Path().apply {
            moveTo(left + topInset, glassTop)
            lineTo(left + glassW - topInset, glassTop)
            lineTo(left + glassW - bottomInset, glassBottom - r)
            quadraticBezierTo(left + glassW - bottomInset, glassBottom, left + glassW - bottomInset - r, glassBottom)
            lineTo(left + bottomInset + r, glassBottom)
            quadraticBezierTo(left + bottomInset, glassBottom, left + bottomInset, glassBottom - r)
            close()
        }

        drawPath(body, color = color.copy(alpha = if (filled) 0.18f else 0.10f))
        if (filled) {
            clipPath(body) {
                drawRect(color, topLeft = androidx.compose.ui.geometry.Offset(left, glassTop + (glassBottom - glassTop) * 0.12f),
                    size = androidx.compose.ui.geometry.Size(glassW, glassBottom))
            }
            val bubble = Color.White.copy(alpha = 0.55f)
            drawCircle(bubble, radius = glassW * 0.07f, center = androidx.compose.ui.geometry.Offset(w / 2f - glassW * 0.12f, glassTop + (glassBottom - glassTop) * 0.45f))
            drawCircle(bubble, radius = glassW * 0.045f, center = androidx.compose.ui.geometry.Offset(w / 2f + glassW * 0.14f, glassTop + (glassBottom - glassTop) * 0.62f))
        }
        drawPath(
            body,
            color = color.copy(alpha = if (filled || isNext) 0.9f else 0.35f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )
        if (isNext) {
            val c = androidx.compose.ui.geometry.Offset(w / 2f, (glassTop + glassBottom) / 2f + h * 0.04f)
            val arm = glassW * 0.16f
            val stroke = 2.dp.toPx()
            drawLine(color, androidx.compose.ui.geometry.Offset(c.x - arm, c.y), androidx.compose.ui.geometry.Offset(c.x + arm, c.y), strokeWidth = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(color, androidx.compose.ui.geometry.Offset(c.x, c.y - arm), androidx.compose.ui.geometry.Offset(c.x, c.y + arm), strokeWidth = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}
