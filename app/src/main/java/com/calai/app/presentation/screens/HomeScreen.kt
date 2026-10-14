package com.calai.app.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.R
import com.calai.app.data.remote.dto.MealResponseDto
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.HabitReminderViewModel
import com.calai.app.presentation.viewmodel.HomeViewModel
import com.calai.app.presentation.viewmodel.WaterViewModel
import com.calai.app.presentation.viewmodel.formatMealLogTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Ngưỡng bề rộng màn hình được coi là tablet/landscape */
private val TABLET_BREAKPOINT_DP = 600.dp

/**
 * HomeScreen Dashboard — Premium Redesign
 * Layout order: Header → WeekStrip → 4 chỉ số dinh dưỡng → Nhắc nhở bữa ăn & nước → Food Diary
 * Business logic 100% giữ nguyên từ ViewModel + API.
 */
@Composable
fun HomeScreen(
    onAddMealClick: () -> Unit,
    onCameraClick: () -> Unit = {},
    onNavigateTab: (DockTab) -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenSuggestions: () -> Unit = {},
    onOpenWorkoutHub: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenCheckin: () -> Unit = {},
    onOpenWeeklySummary: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onThemeChanged: (Boolean) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    checkinViewModel: com.calai.app.presentation.viewmodel.CheckinBannerViewModel = hiltViewModel(),
    reminderViewModel: HabitReminderViewModel = hiltViewModel(),
    waterViewModel: WaterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingCheckin by checkinViewModel.pending.collectAsState()
    val pendingSync by viewModel.pendingRequests.collectAsState()
    var confirmDeleteWholeMeal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    confirmDeleteWholeMeal?.let { mealId ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDeleteWholeMeal = null },
            title = { Text("Xoá cả bữa này?") },
            text = { Text("Đây là món cuối của bữa. Gỡ món này sẽ xoá luôn cả bữa ăn.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.deleteMeal(mealId)
                    confirmDeleteWholeMeal = null
                }) { Text("Xoá cả bữa") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDeleteWholeMeal = null }) { Text("Giữ lại") }
            }
        )
    }
    val reminderState by reminderViewModel.listState.collectAsState()
    val water by waterViewModel.water.collectAsState()
    val waterError by waterViewModel.error.collectAsState()
    var showReminderSheet by remember { mutableStateOf(false) }
    var selectedDateIso by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    // Quay lại Home sau khi log bữa (camera/thêm món) hay đổi habit reminder không tự phát dữ liệu mới
    // vì NavHost giữ nguyên ViewModel của back-stack entry — phải tự tải lại khi màn hình resume.
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadData(selectedDateIso)
                reminderViewModel.loadReminders()
                waterViewModel.refresh()
                checkinViewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Subtle radial gradient background — atmosphere chứ không cạnh tranh content
                if (isDarkTheme) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0A0A0A),
                            ObsidianBackground
                        ),
                        radius = 1200f
                    )
                } else {
                    // Giảm độ xám-xanh của nền (trước là 0xFFF0F4FF, ngả xám khiến card trắng bị "chìm"
                    // vào nền) — giờ sáng/sạch hơn, card nổi rõ nhờ chênh lệch độ sáng + shadow mạnh hơn.
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            IvoryBackground
                        ),
                        radius = 1400f
                    )
                }
            )
    ) {
        val isTablet = maxWidth >= TABLET_BREAKPOINT_DP

        if (uiState.isLoading && uiState.dailySummary == null) {
            CuteLoadingIndicator(
                rawResId = R.raw.loading_general,
                size = 96.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        if (isTablet) Modifier.widthIn(max = 640.dp) else Modifier.fillMaxWidth()
                    )
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 28.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── 1. HEADER ───────────────────────────────────────────────────────
                item {
                    DashboardHeader(
                        username = uiState.displayName ?: uiState.username,
                        avatar = uiState.avatar,
                        isDarkTheme = isDarkTheme,
                        onSearchClick = onSearchClick,
                        onOpenNotifications = onOpenNotifications
                    )
                }

                // ── 2. WEEK SELECTOR ────────────────────────────────────────────────
                item {
                    WeeklyCalendarStrip(
                        selectedDateIso = selectedDateIso,
                        isDarkTheme = isDarkTheme,
                        incompleteDates = uiState.incompleteDates,
                        onDateSelected = { newDate ->
                            selectedDateIso = newDate
                            viewModel.loadData(newDate)
                        }
                    )
                }

                // ── 3. HERO CALORIES ───────────────────────────────────────────────
                item {
                    val summary = uiState.dailySummary?.summary
                    // Bữa đang chờ gửi vẫn được cộng tạm vào "Đã ăn" (BR-07.5)
                    val pend = com.calai.app.data.sync.pendingMealTotalsFor(pendingSync, selectedDateIso)
                    CalorieHeroCard(
                        consumed = summary?.consumedCalories?.let { (it + pend.calories).toInt() },
                        target = summary?.targetCalories?.toInt(),
                        remaining = summary?.remainingCalories?.let { (it - pend.calories).toInt() },
                        isDarkTheme = isDarkTheme
                    )
                }

                // ── 3a. HÀNG ĐỢI GỬI LẠI: bữa ăn/buổi tập chưa lên được máy chủ ──────────
                if (pendingSync.isNotEmpty()) {
                    item {
                        PendingSyncBanner(
                            items = pendingSync,
                            onRetry = viewModel::retryPending,
                            onDelete = viewModel::deletePending
                        )
                    }
                }

                // ── 3b. CHECK-IN TUẦN: chỉ hiện khi đang có Check-in chờ (BR-06) ──────────
                pendingCheckin?.let { pending ->
                    item {
                        CheckinEntryCard(checkIn = pending, onClick = onOpenCheckin)
                    }
                }

                // ── 3c. TỔNG KẾT TUẦN ────────────────────────────────────────────────────
                item {
                    WeeklySummaryEntryCard(onClick = onOpenWeeklySummary)
                }

                // ── 4. 4 Ô THÔNG SỐ: Nước / Cân nặng / Calories / Đa lượng (dữ liệu thật từ BE) ──
                item {
                    val summary = uiState.dailySummary?.summary
                    val macros = summary?.macros
                    val pend = com.calai.app.data.sync.pendingMealTotalsFor(pendingSync, selectedDateIso)
                    DashboardStatTiles(
                        stats = DashboardStats(
                            weightText = uiState.weightText, targetWeightText = uiState.targetWeightText,
                            weightProgress = uiState.weightProgress,
                            proteinG = macros?.protein?.consumed?.let { (it + pend.protein).toInt() }, proteinTarget = macros?.protein?.target?.toInt(),
                            carbG = macros?.carb?.consumed?.let { (it + pend.carb).toInt() }, carbTarget = macros?.carb?.target?.toInt(),
                            fatG = macros?.fat?.consumed?.let { (it + pend.fat).toInt() }, fatTarget = macros?.fat?.target?.toInt()
                        ),
                        isDarkTheme = isDarkTheme
                    )
                }

                // ── 4b. NHẮC NHỞ BỮA ĂN + UỐNG NƯỚC ─────────────────────────────────
                item {
                    MealReminderCard(
                        pick = pickMealReminder(
                            reminderState.reminders,
                            loggedMealTypes = if (selectedDateIso == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                                uiState.meals.map { it.mealType }.toSet() else emptySet()
                        ),
                        isDarkTheme = isDarkTheme,
                        onClick = { showReminderSheet = true }
                    )
                }
                item {
                    WaterCard(
                        water = water,
                        errorMessage = waterError,
                        onRetry = waterViewModel::refresh,
                        isDarkTheme = isDarkTheme,
                        onAddGlass = waterViewModel::addGlass,
                        onRemoveGlass = waterViewModel::removeGlass,
                        onOpenSettings = { showReminderSheet = true }
                    )
                }

                // ── 5. FOOD DIARY HEADER ────────────────────────────────────────────
                item {
                    FoodDiaryHeader(onAddMealClick = onAddMealClick)
                }

                // ── 6. MEALS LIST / EMPTY STATE ─────────────────────────────────────
                if (uiState.meals.isEmpty()) {
                    item {
                        EmptyMealState(
                            isDarkTheme = isDarkTheme,
                            onCameraClick = onCameraClick
                        )
                    }
                } else {
                    uiState.mealGroups.forEach { group ->
                        item(key = "group_${group.label}") {
                            MealGroupLabel(label = group.label)
                        }
                        items(group.meals, key = { it.id }) { meal ->
                            MealItemRow(
                                meal = meal,
                                isDarkTheme = isDarkTheme,
                                showTimelineTime = uiState.mealStructureMode != "FIXED_MEALS",
                                onDelete = { viewModel.deleteMeal(meal.id) },
                                onChangeMealType = { newType -> viewModel.changeMealType(meal.id, newType) },
                                onCopy = { targetDate -> viewModel.copyMeal(meal.id, targetDate) },
                                onUpdateItemQuantity = { itemId, qty ->
                                    if (viewModel.wouldEmptyMeal(meal, itemId, qty)) confirmDeleteWholeMeal = meal.id
                                    else viewModel.updateMealItemQuantity(meal, itemId, qty)
                                },
                                onRemoveItem = { itemId ->
                                    if (viewModel.wouldEmptyMeal(meal, itemId)) confirmDeleteWholeMeal = meal.id
                                    else viewModel.removeMealItem(meal, itemId)
                                }
                            )
                        }
                    }

                    // BR-05.2 / BR-07.6: hỏi người dùng đã ghi đủ các bữa của ngày chưa (không hỏi ngày tương lai)
                    val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    if (selectedDateIso <= todayIso) {
                        item(key = "day_completeness") {
                            DayCompletenessCard(
                                status = uiState.dailySummary?.logStatus,
                                isDarkTheme = isDarkTheme,
                                onSelect = { completeness ->
                                    viewModel.setDayCompleteness(selectedDateIso, completeness)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showReminderSheet) {
            HabitReminderCenterSheet(
                isDarkTheme = isDarkTheme,
                onDismiss = { showReminderSheet = false },
                viewModel = reminderViewModel
            )
        }

        // ── FLOATING BOTTOM DOCK ────────────────────────────────────────────────
        FloatingBottomDock(
            currentTab = DockTab.HOME,
            isDarkTheme = isDarkTheme,
            onTabSelected = { tab ->
                when (tab) {
                    DockTab.HOME -> {}
                    DockTab.SCAN -> onCameraClick()
                    else -> onNavigateTab(tab)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// SUBCOMPONENTS
// ════════════════════════════════════════════════════════════════════════════

/**
 * Dashboard Header — Avatar + Greeting + floating action buttons
 */
@Composable
private fun DashboardHeader(
    username: String,
    avatar: String? = null,
    isDarkTheme: Boolean,
    onSearchClick: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + Greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar badge với gradient + shadow
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(
                        elevation = if (isDarkTheme) 6.dp else 12.dp,
                        shape = CircleShape,
                        ambientColor = VividOrange.copy(alpha = 0.4f),
                        spotColor = VividOrange.copy(alpha = 0.4f)
                    )
                    .clip(CircleShape)
                    .background(CtaSolidOrange)
                    .border(
                        1.5.dp,
                        Color.White.copy(alpha = if (isDarkTheme) 0.30f else 0.60f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatar != null) {
                    com.calai.app.presentation.components.UserAvatar(avatar = avatar, displayName = username, size = 46.dp, fontSize = 19)
                } else {
                    Text(
                        text = username.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = OnPrimary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = getGreeting(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = username.ifEmpty { "Bạn" },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // Action buttons — floating controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingIconButton(
                icon = Icons.Default.Search,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                isDarkTheme = isDarkTheme,
                contentDescription = "Tìm kiếm",
                onClick = onSearchClick
            )

            FloatingIconButton(
                icon = Icons.Default.NotificationsNone,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                isDarkTheme = isDarkTheme,
                contentDescription = "Thông báo",
                onClick = onOpenNotifications
            )
        }
    }
}

/** Floating icon button với background riêng + border + shadow */
@Composable
private fun FloatingIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isDarkTheme: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(120),
        label = "fab_scale"
    )
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    Box(
        modifier = Modifier
            .size(38.dp)
            .scale(scale)
            .shadow(
                elevation = if (isDarkTheme) 4.dp else 8.dp,
                shape = CircleShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun FoodDiaryHeader(onAddMealClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(120),
        label = "add_meal_scale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Nhật ký bữa ăn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = "Ghi lại món ăn của bạn hôm nay",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
        }

        // "+ Thêm món" — real button với elevation
        Box(
            modifier = Modifier
                .scale(scale)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = VividOrange.copy(alpha = 0.25f),
                    spotColor = VividOrange.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(CtaSolidOrange)
                .clickable(interactionSource = interactionSource, indication = null) { onAddMealClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = OnPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Thêm món",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
            }
        }
    }
}

/**
 * Meal group label
 */
@Composable
private fun MealGroupLabel(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(VividOrange)
        )
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.3.sp
        )
    }
}

/**
 * Premium Empty State — icon lớn + glow + CTA button
 */
@Composable
private fun EmptyMealState(isDarkTheme: Boolean, onCameraClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "empty_cta_scale"
    )
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 4.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(vertical = 36.dp, horizontal = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon với glow background
            Box(contentAlignment = Alignment.Center) {
                // Glow halo
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    VividOrange.copy(alpha = if (isDarkTheme) 0.15f else 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(
                            elevation = if (isDarkTheme) 6.dp else 10.dp,
                            shape = CircleShape,
                            ambientColor = VividOrange.copy(alpha = 0.3f),
                            spotColor = VividOrange.copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    VividOrangeLight.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            )
                        )
                        .border(1.dp, VividOrange.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = PrimaryStrong,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Chưa có bữa ăn nào",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bắt đầu theo dõi bữa ăn đầu tiên\ncủa bạn hôm nay.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AI Scan CTA button
            Box(
                modifier = Modifier
                    .scale(scale)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = VividOrange.copy(alpha = 0.4f),
                        spotColor = VividOrange.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(CtaSolidOrange)
                    .clickable(interactionSource = interactionSource, indication = null) { onCameraClick() }
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = OnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Quét món ăn bằng AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// MEAL ITEM ROW — giữ nguyên business logic, nâng cấp UI
// ════════════════════════════════════════════════════════════════════════════

private val MEAL_TYPE_LABELS = listOf(
    "BREAKFAST" to "Bữa Sáng",
    "LUNCH" to "Bữa Trưa",
    "DINNER" to "Bữa Tối",
    "SNACK" to "Bữa Phụ"
)

private fun mealTypeColor(mealType: String): Color = when (mealType) {
    "BREAKFAST" -> Color(0xFFF2C265) // vàng ấm
    "LUNCH" -> Color(0xFF4FBFA5)      // xanh ngọc
    "DINNER" -> Color(0xFF6FB1F0)     // xanh dương
    else -> Color(0xFFB69AE8)         // tím nhạt cho bữa phụ
}

@Composable
private fun MealItemRow(
    meal: MealResponseDto,
    isDarkTheme: Boolean = true,
    showTimelineTime: Boolean = true,
    onDelete: () -> Unit,
    onChangeMealType: (String) -> Unit,
    onCopy: (String) -> Unit,
    onUpdateItemQuantity: (itemId: String, newQuantity: Float) -> Unit = { _, _ -> },
    onRemoveItem: (itemId: String) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
    var showEditItemsDialog by remember { mutableStateOf(false) }
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    val mealTypeName = when (meal.mealType) {
        "BREAKFAST" -> "Bữa Sáng"
        "LUNCH" -> "Bữa Trưa"
        "DINNER" -> "Bữa Tối"
        else -> "Bữa Phụ"
    }
    val typeAccentColor = mealTypeColor(meal.mealType)

    if (showCopyDialog) {
        CopyMealDialog(
            isDarkTheme = isDarkTheme,
            onDismiss = { showCopyDialog = false },
            onConfirm = { targetDate ->
                onCopy(targetDate)
                showCopyDialog = false
            }
        )
    }

    if (showEditItemsDialog) {
        EditMealItemsDialog(
            meal = meal,
            isDarkTheme = isDarkTheme,
            onDismiss = { showEditItemsDialog = false },
            onUpdateQuantity = onUpdateItemQuantity,
            onRemoveItem = onRemoveItem
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 4.dp else 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.10f) else MaterialTheme.colorScheme.outline,
                        MaterialTheme.colorScheme.outline
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        // Left accent border theo meal type color
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(typeAccentColor, typeAccentColor.copy(alpha = 0.4f))
                    )
                )
                .align(Alignment.CenterStart)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Meal type badge với accent color
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(typeAccentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = mealTypeName,
                            color = typeAccentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (showTimelineTime) {
                        formatMealLogTime(meal.createdAt)?.let { time ->
                            Text(
                                text = time,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                val itemsSummary = meal.items.joinToString(", ") { "${it.name} (${it.quantity.toInt()}x)" }
                Text(
                    text = itemsSummary.ifEmpty { "Món ăn tổng hợp" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${meal.totalProtein.toInt()}g P  •  ${meal.totalCarb.toInt()}g C  •  ${meal.totalFat.toInt()}g F",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${meal.totalCalories.toInt()}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "kcal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Thêm hành động",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            "Chuyển thành",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        MEAL_TYPE_LABELS.filter { it.first != meal.mealType }.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                                },
                                onClick = {
                                    showMenu = false
                                    onChangeMealType(type)
                                }
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        DropdownMenuItem(
                            text = {
                                Text("Sửa món ăn", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            },
                            onClick = { showMenu = false; showEditItemsDialog = true }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Sao chép sang ngày khác", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            },
                            onClick = { showMenu = false; showCopyDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa bữa ăn", color = CoralWarning, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null,
                                    tint = CoralWarning, modifier = Modifier.size(16.dp))
                            },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// EDIT MEAL ITEMS DIALOG — sửa số lượng / gỡ từng món riêng lẻ trong 1 bữa ăn đã log
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun EditMealItemsDialog(
    meal: MealResponseDto,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onUpdateQuantity: (itemId: String, newQuantity: Float) -> Unit,
    onRemoveItem: (itemId: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Sửa món ăn", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "Chỉnh số lượng hoặc gỡ từng món khỏi bữa ăn này",
                fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                meal.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                            Text(
                                "${(item.calories * item.quantity).toInt()} kcal",
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = { onUpdateQuantity(item.id, item.quantity - 0.5f) },
                                modifier = Modifier.size(28.dp)
                            ) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Giảm", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                            Text(
                                "${item.quantity}x", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.widthIn(min = 28.dp), textAlign = TextAlign.Center
                            )
                            IconButton(
                                onClick = { onUpdateQuantity(item.id, item.quantity + 0.5f) },
                                modifier = Modifier.size(28.dp)
                            ) { Icon(Icons.Default.AddCircleOutline, contentDescription = "Tăng", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                            IconButton(
                                onClick = { onRemoveItem(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) { Icon(Icons.Default.Delete, contentDescription = "Gỡ món", tint = CoralWarning, modifier = Modifier.size(16.dp)) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            AppButton(text = "Xong", onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(14.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// COPY MEAL DIALOG — giữ nguyên
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun CopyMealDialog(
    isDarkTheme: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (targetDate: String) -> Unit
) {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = java.util.Calendar.getInstance()

    fun dateAfter(days: Int): String {
        calendar.time = Date()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
        return fmt.format(calendar.time)
    }

    val presets = listOf(
        "Hôm nay" to dateAfter(0),
        "Ngày mai" to dateAfter(1),
        "Sau 2 ngày" to dateAfter(2)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Sao chép bữa ăn sang ngày khác",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { (label, dateStr) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { onConfirm(dateStr) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ════════════════════════════════════════════════════════════════════════════
// HELPERS
// ════════════════════════════════════════════════════════════════════════════

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Chào buổi sáng,"
        hour < 18 -> "Chào buổi chiều,"
        else -> "Chào buổi tối,"
    }
}

/**
 * Thẻ "Bạn đã ghi đủ các bữa của ngày này chưa?" (BR-05.2). Chỉ những ngày đầy đủ mới được dùng để
 * tính mức tiêu hao và điều chỉnh mục tiêu, nên ngày quên ghi bữa không làm app bắt bạn ăn ít đi.
 * Bấm lại lựa chọn đang chọn sẽ trả về chế độ tự động.
 */
@Composable
private fun DayCompletenessCard(
    status: com.calai.app.data.remote.dto.DayLogStatusDto?,
    isDarkTheme: Boolean,
    onSelect: (String) -> Unit
) {
    val completeness = status?.completeness ?: "AUTO"
    val isComplete = status?.isComplete ?: false
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            "Bạn đã ghi đủ các bữa của ngày này chưa?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            when (completeness) {
                "COMPLETE" -> "Đã ghi nhận: ngày này được dùng để tính mức tiêu hao của bạn."
                "INCOMPLETE" -> "Đã ghi nhận: ngày này sẽ không được dùng để tính toán."
                else ->
                    if (isComplete) "Hệ thống đang tính ngày này là đã đủ vì bạn ghi nhiều bữa."
                    else "Chỉ ngày ghi đủ mới được dùng để tính mức tiêu hao, giúp mục tiêu chính xác hơn."
            },
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionPill("Đã ghi đủ", completeness == "COMPLETE", isDarkTheme, Modifier.weight(1f)) {
                onSelect(if (completeness == "COMPLETE") "AUTO" else "COMPLETE")
            }
            SelectionPill("Chưa đủ", completeness == "INCOMPLETE", isDarkTheme, Modifier.weight(1f)) {
                onSelect(if (completeness == "INCOMPLETE") "AUTO" else "INCOMPLETE")
            }
        }
    }
}


@Composable
private fun PendingSyncBanner(
    items: List<com.calai.app.data.local.entity.PendingRequestEntity>,
    onRetry: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    val failed = items.filter { it.status == com.calai.app.data.local.entity.PendingRequestEntity.STATUS_FAILED }
    val waiting = items.size - failed.size
    var showFailed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (failed.isNotEmpty()) Color(0xFFFFE3DC) else Color(0xFFFFF1D9))
            .clickable(enabled = failed.isNotEmpty()) { showFailed = true }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            if (failed.isNotEmpty()) "${failed.size} mục chưa gửi được. Chạm để xem" else "$waiting mục đang chờ gửi khi có mạng",
            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3A2A1A)
        )
        Text(
            "Dữ liệu đã lưu trên máy, không bị mất.",
            fontSize = 11.5.sp, color = Color(0xFF3A2A1A).copy(alpha = 0.7f)
        )
    }
    if (showFailed) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFailed = false },
            title = { Text("Mục chưa gửi được") },
            text = {
                androidx.compose.foundation.layout.Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                    failed.forEach { f ->
                        androidx.compose.foundation.layout.Column {
                            Text(if (f.kind == "MEAL") "Bữa ăn" else "Buổi tập", fontWeight = FontWeight.Bold)
                            Text(f.lastError ?: "Máy chủ từ chối", fontSize = 12.sp)
                            androidx.compose.foundation.layout.Row {
                                androidx.compose.material3.TextButton(onClick = { onRetry(f.localId) }) { Text("Gửi lại") }
                                androidx.compose.material3.TextButton(onClick = { onDelete(f.localId) }) { Text("Xoá") }
                            }
                        }
                    }
                }
            },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { showFailed = false }) { Text("Đóng") } }
        )
    }
}
