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
    isDarkTheme: Boolean = true,
    onThemeChanged: (Boolean) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    reminderViewModel: HabitReminderViewModel = hiltViewModel(),
    waterViewModel: WaterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
                            Color(0xFF161924),
                            ObsidianBackground
                        ),
                        radius = 1200f
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF0F4FF),
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
                        username = uiState.username,
                        isDarkTheme = isDarkTheme,
                        onThemeChanged = onThemeChanged,
                        onOpenSuggestions = onOpenSuggestions,
                        onLogout = onLogout
                    )
                }

                // ── 2. WEEK SELECTOR ────────────────────────────────────────────────
                item {
                    WeeklyCalendarStrip(
                        selectedDateIso = selectedDateIso,
                        isDarkTheme = isDarkTheme,
                        onDateSelected = { newDate ->
                            selectedDateIso = newDate
                            viewModel.loadData(newDate)
                        }
                    )
                }

                // ── 3. HERO CALORIES ───────────────────────────────────────────────
                item {
                    val summary = uiState.dailySummary?.summary
                    CalorieHeroCard(
                        consumed = summary?.consumedCalories?.toInt(),
                        target = summary?.targetCalories?.toInt(),
                        remaining = summary?.remainingCalories?.toInt(),
                        isDarkTheme = isDarkTheme
                    )
                }

                // ── 4. 4 Ô THÔNG SỐ: Nước / Cân nặng / Calories / Đa lượng (dữ liệu thật từ BE) ──
                item {
                    val summary = uiState.dailySummary?.summary
                    val macros = summary?.macros
                    DashboardStatTiles(
                        stats = DashboardStats(
                            weightText = uiState.weightText, targetWeightText = uiState.targetWeightText,
                            weightProgress = uiState.weightProgress,
                            proteinG = macros?.protein?.consumed?.toInt(), proteinTarget = macros?.protein?.target?.toInt(),
                            carbG = macros?.carb?.consumed?.toInt(), carbTarget = macros?.carb?.target?.toInt(),
                            fatG = macros?.fat?.consumed?.toInt(), fatTarget = macros?.fat?.target?.toInt()
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
                                onUpdateItemQuantity = { itemId, qty -> viewModel.updateMealItemQuantity(meal, itemId, qty) },
                                onRemoveItem = { itemId -> viewModel.removeMealItem(meal, itemId) }
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
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onOpenSuggestions: () -> Unit,
    onLogout: () -> Unit
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
                Text(
                    text = username.take(1).uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    color = Color.White
                )
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
            TactileThemeSwitch(
                isDarkTheme = isDarkTheme,
                onThemeChanged = onThemeChanged
            )

            FloatingIconButton(
                icon = Icons.Default.AutoAwesome,
                iconTint = VividOrange,
                isDarkTheme = isDarkTheme,
                contentDescription = "Gợi ý cho bạn",
                onClick = onOpenSuggestions
            )

            FloatingIconButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                isDarkTheme = isDarkTheme,
                contentDescription = "Đăng xuất",
                onClick = onLogout
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
        targetValue = if (isPressed) 0.90f else 1f,
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
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Thêm món",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
                        tint = VividOrange,
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
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Quét món ăn bằng AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
    "BREAKFAST" -> Color(0xFFFBBF24) // amber
    "LUNCH" -> Color(0xFF34D399)      // mint
    "DINNER" -> Color(0xFF60A5FA)     // blue
    else -> Color(0xFFA78BFA)         // purple for snack
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
                        if (isDarkTheme) Color.White.copy(alpha = 0.10f) else Color.White,
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
