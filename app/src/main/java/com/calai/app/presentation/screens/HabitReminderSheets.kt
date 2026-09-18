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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.view.WindowManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.*
import com.calai.app.data.remote.dto.HabitReminderDto
import com.calai.app.data.remote.dto.SuggestedMealItemDto
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.HabitReminderViewModel

private sealed class HabitReminderPage {
    object List : HabitReminderPage()
    object Edit : HabitReminderPage()
    object Foods : HabitReminderPage()
    object Repeat : HabitReminderPage()
}

private data class ReminderEditForm(
    val timeOfDay: String,
    val windowStart: String?,
    val windowEnd: String?,
    val targetCalorieMin: String,
    val targetCalorieMax: String,
    val repeatDays: List<Int>,
    val advanceNoticeMinutes: Int
)

private fun HabitReminderDto.toForm() = ReminderEditForm(
    timeOfDay = timeOfDay,
    windowStart = windowStart,
    windowEnd = windowEnd,
    targetCalorieMin = targetCalorieMin?.toInt()?.toString() ?: "",
    targetCalorieMax = targetCalorieMax?.toInt()?.toString() ?: "",
    repeatDays = repeatDays,
    advanceNoticeMinutes = advanceNoticeMinutes
)

private val ISO_DAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

/** Micro-copy động viên ngắn gọn theo loại nhắc nhở — biến nhắc nhở thành gợi ý tích cực thay vì chỉ ra lệnh. */
private fun reminderTip(type: String): String? = when (type) {
    "BREAKFAST" -> "Ăn trong vòng 1 giờ sau khi thức dậy giúp khởi động trao đổi chất"
    "LUNCH" -> "Ăn đúng giờ trưa giúp duy trì năng lượng ổn định cho buổi chiều"
    "DINNER" -> "Ăn trước 20:00 giúp tối ưu hóa tiêu hóa & ngủ ngon"
    "SNACK" -> "Bữa phụ nhẹ giúp kiểm soát cơn đói giữa các bữa chính"
    else -> null
}

/** Icon thư viện (Material) + màu nền avatar tròn cho từng loại nhắc nhở — không dùng emoji/canvas vẽ tay. */
private fun reminderIcon(type: String): Pair<ImageVector, Color> = when (type) {
    "BREAKFAST" -> Icons.Default.FreeBreakfast to PastelButtercup
    "LUNCH" -> Icons.Default.LunchDining to PastelMint
    "DINNER" -> Icons.Default.DinnerDining to PastelMint
    "SNACK" -> Icons.Default.Cookie to PastelRose
    "WATER" -> Icons.Default.WaterDrop to PastelLavender
    else -> Icons.Default.NotificationsActive to PastelLavender
}

/** Card trắng dùng chung cho toàn bộ luồng Habit Reminder — nền trắng + viền mảnh + shadow nhẹ,
 * đúng phong cách tham khảo (không dùng surfaceVariant xám như bản nháp trước). */
@Composable
private fun WhiteCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(padding),
        content = content
    )
}

/**
 * WheelTimePicker — thay the android.app.TimePickerDialog xau.
 * Hai WheelPicker3D (gio 0-23 | phut 0-59) dat canh nhau trong WhiteCard.
 * Preview gio da chon hien thi phia tren, dau ":" lon o giua.
 */
@Composable
internal fun WheelTimePicker(
    timeString: String,
    onTimeChange: (String) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val parts = timeString.split(":")
    val initHour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 7
    val initMin  = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
    var hour   by remember(timeString) { mutableIntStateOf(initHour) }
    var minute by remember(timeString) { mutableIntStateOf(initMin) }

    WhiteCard(modifier = modifier, padding = 0.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preview + label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Thời gian báo chuông",
                        fontSize = 12.sp, color = TextInkSecondary, fontWeight = FontWeight.Medium
                    )
                    Text(
                        "%02d:%02d".format(hour, minute),
                        fontSize = 32.sp, fontWeight = FontWeight.Black,
                        color = CtaSolidOrange, letterSpacing = 2.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(VividOrangeSoft)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = VividOrange, modifier = Modifier.size(14.dp))
                        Text("Kéo để chọn", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = VividOrange)
                    }
                }
            }

            // Divider
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)))

            // Wheel area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF6F3EC))
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                // Center highlight strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VividOrangeSoft)
                        .border(1.5.dp, CtaSolidOrange.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    WheelPicker3D(
                        value = hour,
                        onValueChange = { h -> hour = h; onTimeChange("%02d:%02d".format(h, minute)) },
                        range = 0..23,
                        formatLabel = { "%02d".format(it) },
                        itemHeight = 52.dp, visibleItemCount = 5,
                        isDarkTheme = false,
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", fontSize = 30.sp, fontWeight = FontWeight.Black, color = CtaSolidOrange,
                        modifier = Modifier.padding(horizontal = 6.dp))
                    WheelPicker3D(
                        value = minute,
                        onValueChange = { m -> minute = m; onTimeChange("%02d:%02d".format(hour, m)) },
                        range = 0..59,
                        formatLabel = { "%02d".format(it) },
                        itemHeight = 52.dp, visibleItemCount = 5,
                        isDarkTheme = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitReminderCenterSheet(
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    viewModel: HabitReminderViewModel = hiltViewModel()
) {
    var page by remember { mutableStateOf<HabitReminderPage>(HabitReminderPage.List) }
    var form by remember { mutableStateOf<ReminderEditForm?>(null) }
    val listState by viewModel.listState.collectAsState()
    val editState by viewModel.editState.collectAsState()

    LaunchedEffect(editState.reminder?.id) { editState.reminder?.let { form = it.toForm() } }

    val onCloseAll: () -> Unit = { viewModel.closeEdit(); page = HabitReminderPage.List; onDismiss() }

    // Tieu de va nut trai theo page
    val topBarTitle = when (page) {
        is HabitReminderPage.List -> "Nhắc Nhở Bữa Ăn & Uống Nước"
        is HabitReminderPage.Edit -> "Chỉnh sửa ${editState.reminder?.label ?: ""}"
        is HabitReminderPage.Foods -> editState.reminder?.label ?: "Món ăn"
        is HabitReminderPage.Repeat -> "Lặp lại & Âm báo"
    }
    val onNavigateBack: (() -> Unit)? = when (page) {
        is HabitReminderPage.List -> null
        is HabitReminderPage.Edit -> { { viewModel.closeEdit(); page = HabitReminderPage.List } }
        is HabitReminderPage.Foods -> { { page = HabitReminderPage.Edit } }
        is HabitReminderPage.Repeat -> { { page = HabitReminderPage.Edit } }
    }

    Dialog(
        onDismissRequest = onCloseAll,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Dialog mặc định của Compose vẫn set chiều cao WRAP_CONTENT dù đã tắt usePlatformDefaultWidth
        // (flag đó chỉ ảnh hưởng chiều rộng) — ép window MATCH_PARENT để trang thực sự full-screen.
        val dialogWindow = (LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        SideEffect { dialogWindow?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                topBarTitle,
                                fontSize = 17.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground, maxLines = 1
                            )
                        },
                        navigationIcon = {
                            if (onNavigateBack != null) {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Quay lại",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            } else {
                                Spacer(Modifier.size(48.dp))
                            }
                        },
                        actions = {
                            IconButton(onClick = onCloseAll) {
                                Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextInkSecondary)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFFF6F3EC),
                            scrolledContainerColor = Color(0xFFF6F3EC)
                        )
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)))
                }
            },
            containerColor = Color(0xFFF6F3EC)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (page) {
                    is HabitReminderPage.List -> HabitReminderListContent(
                        isDark = isDarkTheme,
                        isLoading = listState.isLoading,
                        errorMessage = listState.errorMessage,
                        reminders = listState.reminders,
                        onRetry = viewModel::loadReminders,
                        onToggle = viewModel::toggleEnabled,
                        onEdit = { reminder -> viewModel.openEdit(reminder); page = HabitReminderPage.Edit },
                        onAddCustom = { label, time -> viewModel.createCustomReminder(label, time) {} },
                        onDelete = viewModel::deleteReminder,
                        onDone = { page = HabitReminderPage.List; onDismiss() }
                    )
                    is HabitReminderPage.Edit -> {
                        val reminder = editState.reminder
                        val currentForm = form
                        if (reminder != null && currentForm != null) {
                            HabitReminderEditContent(
                                reminder = reminder, form = currentForm, onFormChange = { form = it },
                                isDark = isDarkTheme, isSaving = editState.isSaving,
                                onOpenRepeatSettings = { page = HabitReminderPage.Repeat },
                                onOpenFoods = { page = HabitReminderPage.Foods },
                                onSave = {
                                    viewModel.saveEdit(
                                        timeOfDay = currentForm.timeOfDay, windowStart = currentForm.windowStart,
                                        windowEnd = currentForm.windowEnd,
                                        targetCalorieMin = currentForm.targetCalorieMin.toFloatOrNull(),
                                        targetCalorieMax = currentForm.targetCalorieMax.toFloatOrNull(),
                                        repeatDays = currentForm.repeatDays,
                                        advanceNoticeMinutes = currentForm.advanceNoticeMinutes
                                    ) { page = HabitReminderPage.List }
                                },
                                onCancel = { viewModel.closeEdit(); page = HabitReminderPage.List }
                            )
                        }
                    }
                    is HabitReminderPage.Foods -> {
                        val reminder = editState.reminder
                        if (reminder != null) {
                            HabitReminderFoodsContent(
                                reminder = reminder, suggestions = editState.suggestions,
                                isLoadingSuggestions = editState.isLoadingSuggestions,
                                onAddFood = { s -> viewModel.addFood(s.name, s.mealType, s.calories.toFloat(), s.protein.toFloat(), s.carbs.toFloat(), s.fat.toFloat()) },
                                onRemoveFood = viewModel::removeFood
                            )
                        }
                    }
                    is HabitReminderPage.Repeat -> {
                        val reminder = editState.reminder
                        val currentForm = form
                        if (reminder != null && currentForm != null) {
                            HabitReminderRepeatContent(
                                isDark = isDarkTheme, reminder = reminder,
                                form = currentForm, onFormChange = { form = it },
                                onBack = { page = HabitReminderPage.Edit }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderAvatar(type: String, size: androidx.compose.ui.unit.Dp = 40.dp) {
    val (icon, bg) = reminderIcon(type)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = TextDeepInk, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
private fun HabitReminderListContent(
    isDark: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    reminders: List<HabitReminderDto>,
    onRetry: () -> Unit,
    onToggle: (HabitReminderDto, Boolean) -> Unit,
    onEdit: (HabitReminderDto) -> Unit,
    onAddCustom: (String, String) -> Unit,
    onDelete: (HabitReminderDto) -> Unit,
    onDone: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (isLoading && reminders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VividOrange, strokeWidth = 2.5.dp)
        }
        return
    }

    if (errorMessage != null && reminders.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.WifiOff, contentDescription = null, tint = TextInkSecondary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Không tải được danh sách nhắc nhở", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
            Spacer(modifier = Modifier.height(4.dp))
            Text(errorMessage, fontSize = 12.sp, color = TextInkSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            AppButton(text = "Thử lại", onClick = onRetry, modifier = Modifier.height(44.dp), shape = RoundedCornerShape(12.dp))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Duy trì đồng hồ sinh học & thói quen lành mạnh", fontSize = 12.5.sp, color = TextInkSecondary)

        reminders.forEach { reminder ->
            HabitReminderListRow(
                reminder = reminder,
                onToggle = { onToggle(reminder, it) },
                onEdit = { onEdit(reminder) },
                onDelete = if (reminder.type == "CUSTOM") ({ onDelete(reminder) }) else null
            )
        }

        OutlinedButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = VividOrange, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Thêm nhắc nhở mới", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VividOrange)
        }

        AppButton(text = "Xong", onClick = onDone, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(14.dp))
    }

    if (showAddDialog) {
        AddCustomReminderDialog(
            onDismiss = { showAddDialog = false },
            onSave = { label, time -> onAddCustom(label, time); showAddDialog = false }
        )
    }
}

@Composable
private fun HabitReminderListRow(
    reminder: HabitReminderDto,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val badge = when (reminder.type) {
        "BREAKFAST" -> "Quan trọng" to VividOrange
        "LUNCH" -> "Cân bằng" to EmeraldSuccess
        "SNACK" -> "Tùy chọn" to TextInkSecondary
        else -> null
    }
    val kcalTarget = reminder.targetCalorieMax ?: reminder.targetCalorieMin

    WhiteCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReminderAvatar(reminder.type)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(reminder.label, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
                        if (badge != null) {
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(badge.second.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)
                            ) { Text(badge.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badge.second) }
                        } else if (kcalTarget != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmeraldSuccess))
                                Text("${kcalTarget.toInt()} kcal", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = EmeraldSuccess)
                            }
                        }
                    }
                    AppToggle(checked = reminder.enabled, onCheckedChange = onToggle)
                }

                if (reminder.windowStart != null && reminder.windowEnd != null) {
                    Text("Khung giờ: ${reminder.windowStart} - ${reminder.windowEnd}", fontSize = 11.5.sp, color = TextInkSecondary)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        reminder.timeOfDay,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        color = if (reminder.enabled) TextDeepInk else TextInkSecondary.copy(alpha = 0.6f)
                    )
                    TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 6.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = VividOrange, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Sửa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VividOrange)
                    }
                    if (!reminder.enabled) {
                        Text("Đang tắt", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextInkSecondary.copy(alpha = 0.6f))
                    }
                    if (reminder.foods.isNotEmpty()) {
                        val foodNames = reminder.foods.joinToString(", ") { it.name.substringBefore(" (") }
                        val totalKcal = reminder.foods.sumOf { it.calories.toInt() }
                        Box(modifier = Modifier.weight(1f, fill = false).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F1EA)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(foodNames, fontSize = 10.5.sp, color = TextInkSecondary, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(EmeraldSuccess.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("$totalKcal kcal", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, maxLines = 1)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Xoá", tint = CrimsonError, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitReminderEditContent(
    reminder: HabitReminderDto,
    form: ReminderEditForm,
    onFormChange: (ReminderEditForm) -> Unit,
    isDark: Boolean,
    isSaving: Boolean,
    onOpenRepeatSettings: () -> Unit,
    onOpenFoods: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val (icon, avatarBg) = reminderIcon(reminder.type)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card trạng thái nhanh — badge giờ hiện tại, tóm tắt việc đang sửa
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)).background(VividOrangeSoft).padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(VividOrange))
                Text("Chỉnh sửa ${reminder.label}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDeepInk)
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text("${form.timeOfDay} Hôm nay", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = VividOrange)
            }
        }

        // Hero card: avatar + tên + toggle + mục tiêu calo + gợi ý động viên + XP
        WhiteCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(avatarBg.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = TextDeepInk, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(reminder.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
                            Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(EmeraldSuccess.copy(alpha = 0.15f)).padding(horizontal = 7.dp, vertical = 2.dp)) {
                                Text("Tối nay", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                        }
                        if (reminder.type != "WATER") {
                            val min = form.targetCalorieMin.ifBlank { "?" }
                            val max = form.targetCalorieMax.ifBlank { "?" }
                            Text("Mục tiêu: $min – $max kcal", fontSize = 12.sp, color = TextInkSecondary)
                        }
                    }
                    AppToggle(checked = reminder.enabled, onCheckedChange = {})
                }

                reminderTip(reminder.type)?.let { tip ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(EmeraldSuccess.copy(alpha = 0.10f)).padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Text(tip, fontSize = 11.5.sp, color = TextDeepInk, lineHeight = 15.sp)
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(EmeraldSuccess).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("+50 Điểm", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                    }
                }
            }
        }

        // Bộ chọn giờ cuộn đẹp — thay thế TimePickerDialog
        WheelTimePicker(
            timeString = form.timeOfDay,
            onTimeChange = { newTime -> onFormChange(form.copy(timeOfDay = newTime)) },
            isDarkTheme = isDark
        )

        if (reminder.type != "WATER") {
            WhiteCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Mục tiêu calo cho bữa này", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextInkSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTextFieldCompact(label = "Tối thiểu (kcal)", value = form.targetCalorieMin, onValueChange = { onFormChange(form.copy(targetCalorieMin = it.filter(Char::isDigit))) }, modifier = Modifier.weight(1f))
                        AppTextFieldCompact(label = "Tối đa (kcal)", value = form.targetCalorieMax, onValueChange = { onFormChange(form.copy(targetCalorieMax = it.filter(Char::isDigit))) }, modifier = Modifier.weight(1f))
                    }
                }
            }

            WhiteCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Khung giờ ăn lý tưởng", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextInkSecondary)
                    val presets = listOf("18:30 - 20:00" to ("18:30" to "20:00"), "19:00 - 20:30" to ("19:00" to "20:30"))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.forEach { (labelText, window) ->
                            val isSelected = form.windowStart == window.first && form.windowEnd == window.second
                            SelectionPill(label = labelText, isSelected = isSelected, isDarkTheme = isDark, modifier = Modifier.weight(1f), onClick = { onFormChange(form.copy(windowStart = window.first, windowEnd = window.second)) })
                        }
                    }
                }
            }

            WhiteCard(modifier = Modifier.clickable(onClick = onOpenFoods)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = VividOrange, modifier = Modifier.size(20.dp))
                        Text("Món ăn đã gắn (${reminder.foods.size})", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = TextDeepInk)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextInkSecondary)
                }
            }
        }

        // Lap lai & am bao — link nhanh
        WhiteCard(modifier = Modifier.clickable(onClick = onOpenRepeatSettings)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = VividOrange, modifier = Modifier.size(20.dp))
                    Text("Lap lai & Am bao", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = TextDeepInk)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextInkSecondary)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                Text("Hủy", color = TextInkSecondary)
            }
            AppButton(text = "Lưu thay đổi", onClick = onSave, isLoading = isSaving, modifier = Modifier.weight(1f), height = 44.dp, shape = RoundedCornerShape(12.dp))
        }
    }
}

@Composable
private fun HabitReminderFoodsContent(
    reminder: HabitReminderDto,
    suggestions: List<SuggestedMealItemDto>,
    isLoadingSuggestions: Boolean,
    onAddFood: (SuggestedMealItemDto) -> Unit,
    onRemoveFood: (String) -> Unit
) {
    val totalCalories = reminder.foods.sumOf { it.calories.toInt() }
    val targetMax = (reminder.targetCalorieMax ?: reminder.targetCalorieMin ?: 0f).toInt()
    val progress = if (targetMax > 0) (totalCalories.toFloat() / targetMax).coerceIn(0f, 1f) else 0f
    val totalProtein = reminder.foods.sumOf { it.protein.toInt() }
    val totalCarb = reminder.foods.sumOf { it.carb.toInt() }
    val totalFat = reminder.foods.sumOf { it.fat.toInt() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (targetMax > 0) {
            WhiteCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = VividOrange, modifier = Modifier.size(14.dp))
                        Text("NHẮC NHỞ KHẨU PHẦN", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextInkSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$totalCalories / $targetMax kcal", fontSize = 16.sp, fontWeight = FontWeight.Black, color = VividOrange)
                        val remaining = (targetMax - totalCalories).coerceAtLeast(0)
                        Text("Còn thiếu $remaining kcal", fontSize = 11.sp, color = TextInkSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF0EEE6))) {
                    Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(CtaSolidOrange))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Đã lấp đầy ${(progress * 100).toInt()}% mục tiêu", fontSize = 11.5.sp, color = TextInkSecondary)
                    if (progress >= 0.7f) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(EmeraldSuccess.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                            Text("Đạt chuẩn Sạch", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MacroStat("Protein", totalProtein, PastelProteinLight, Modifier.weight(1f))
                    MacroStat("Carb", totalCarb, PastelCarbLight, Modifier.weight(1f))
                    MacroStat("Fat", totalFat, PastelFatLight, Modifier.weight(1f))
                }
            }
        }

        Text("Món ăn đã gắn vào nhắc nhở (${reminder.foods.size})", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextInkSecondary)
        if (reminder.foods.isEmpty()) {
            Text("Chưa gắn món ăn nào", fontSize = 12.5.sp, color = TextInkSecondary)
        }
        reminder.foods.forEach { food ->
            WhiteCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(PastelMint.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.RiceBowl, contentDescription = null, tint = TextDeepInk, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(food.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
                            Text(
                                text = "${food.servingSize ?: ""} • ${food.calories.toInt()} kcal • P:${food.protein.toInt()}g C:${food.carb.toInt()}g F:${food.fat.toInt()}g",
                                fontSize = 10.8.sp, color = TextInkSecondary
                            )
                        }
                    }
                    IconButton(onClick = { onRemoveFood(food.id) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Gỡ món", tint = CrimsonError, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VividOrange, modifier = Modifier.size(15.dp))
            Text("Gợi ý Ăn Sạch cho ${reminder.label}", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextInkSecondary)
        }
        if (isLoadingSuggestions) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), color = VividOrange, strokeWidth = 2.5.dp)
        }
        suggestions.forEach { suggestion ->
            WhiteCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(suggestion.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
                        Text("${suggestion.calories} kcal • P:${suggestion.protein}g C:${suggestion.carbs}g F:${suggestion.fat}g", fontSize = 11.sp, color = TextInkSecondary)
                    }
                    IconButton(onClick = { onAddFood(suggestion) }, modifier = Modifier.size(30.dp).clip(CircleShape).background(CtaSolidOrange)) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm vào nhắc nhở", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroStat(label: String, grams: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFF6F5F0)).padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Text(label, fontSize = 10.sp, color = TextInkSecondary)
        }
        Text("${grams}g", fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun HabitReminderRepeatContent(
    isDark: Boolean,
    reminder: HabitReminderDto,
    form: ReminderEditForm,
    onFormChange: (ReminderEditForm) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("${reminder.label} • ${form.timeOfDay} hàng ngày", fontSize = 12.sp, color = TextInkSecondary)

        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)).background(VividOrangeSoft).padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mục tiêu & Nhắc nhở", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDeepInk)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(EmeraldSuccess.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                Text("Đang kích hoạt", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
            }
        }

        WhiteCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = VividOrange, modifier = Modifier.size(18.dp))
                Text("Nhắc nhở đúng chuẩn giúp duy trì nhịp sống lành mạnh và chuẩn bị bữa ăn thong thả hơn.", fontSize = 12.sp, color = TextInkSecondary)
            }
        }

        WhiteCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Lặp lại các ngày trong tuần", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextInkSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { isoDay ->
                        val isSelected = form.repeatDays.contains(isoDay)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = if (isSelected) 4.dp else 0.dp,
                                    shape = CircleShape,
                                    ambientColor = CtaSolidOrange.copy(alpha = 0.35f),
                                    spotColor = CtaSolidOrange.copy(alpha = 0.35f)
                                )
                                .clip(CircleShape)
                                .background(if (isSelected) CtaSolidOrange else Color(0xFFF0EEE6))
                                .clickable {
                                    val updated = if (isSelected) form.repeatDays - isoDay else form.repeatDays + isoDay
                                    onFormChange(form.copy(repeatDays = updated.sorted()))
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ISO_DAY_LABELS[isoDay - 1], fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TextWhite else TextInkSecondary)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val presets = listOf("Hằng ngày" to listOf(1, 2, 3, 4, 5, 6, 7), "T2 - T6" to listOf(1, 2, 3, 4, 5), "Cuối tuần" to listOf(6, 7))
                    presets.forEach { (label, days) ->
                        SelectionPill(label = label, isSelected = form.repeatDays.sorted() == days.sorted(), isDarkTheme = isDark, modifier = Modifier.weight(1f), onClick = { onFormChange(form.copy(repeatDays = days)) })
                    }
                }
            }
        }

        WhiteCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Thời gian báo trước", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextInkSecondary)
                data class AdvanceOption(val minutes: Int, val title: String, val desc: String, val recommended: Boolean = false)
                listOf(
                    AdvanceOption(0, "Đúng giờ nhắc", "Chuông báo chuẩn xác ngay giờ ăn"),
                    AdvanceOption(15, "15 phút trước", "Sơ chế rau củ, dọn bàn ăn thong thả", recommended = true),
                    AdvanceOption(30, "30 phút trước", "Bật nồi cơm, hâm canh & làm chín món ăn")
                ).forEach { option ->
                    val isSelected = form.advanceNoticeMinutes == option.minutes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) VividOrangeSoft else Color(0xFFF6F5F0))
                            .border(width = if (isSelected) 1.5.dp else 0.dp, color = if (isSelected) VividOrange else Color.Transparent, shape = RoundedCornerShape(14.dp))
                            .clickable { onFormChange(form.copy(advanceNoticeMinutes = option.minutes)) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(option.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextDeepInk)
                                if (option.recommended) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(CtaSolidOrange).padding(horizontal = 7.dp, vertical = 2.dp)) {
                                        Text("KHUYẾN DÙNG", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    }
                                }
                            }
                            Text(option.desc, fontSize = 11.sp, color = TextInkSecondary)
                        }
                        RadioButton(selected = isSelected, onClick = { onFormChange(form.copy(advanceNoticeMinutes = option.minutes)) })
                    }
                }
            }
        }

        AppButton(text = "Lưu thay đổi", onClick = onBack, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(14.dp))
    }
}

