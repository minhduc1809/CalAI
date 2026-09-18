package com.calai.app.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.R
import com.calai.app.data.remote.dto.MealResponseDto
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.HomeViewModel
import com.calai.app.presentation.viewmodel.formatMealLogTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Ngưỡng bề rộng màn hình được coi là tablet/landscape */
private val TABLET_BREAKPOINT_DP = 600.dp

/**
 * HomeScreen Dashboard — Premium Redesign
 * Layout order: Header → WeekStrip → Hero Calorie Ring → Macro Cards → Food Diary
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDateIso by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
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

                // ── 3. HERO CALORIE SECTION ─────────────────────────────────────────
                item {
                    val summary = uiState.dailySummary?.summary
                    val targetCal = (summary?.targetCalories ?: 2200.0).toInt()
                    val remainingCal = (summary?.remainingCalories ?: targetCal.toDouble()).toInt()
                    val consumedCal = (summary?.consumedCalories ?: 0.0).toInt()
                    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

                    CalorieHeroCard(
                        targetCalories = targetCal,
                        remainingCalories = remainingCal,
                        consumedCalories = consumedCal,
                        isDarkTheme = isDarkTheme,
                        shadowColor = shadowColor
                    )
                }

                // ── 4. MACRO CARDS (Carbs + Protein) ───────────────────────────────
                item {
                    val macros = uiState.dailySummary?.summary?.macros
                    val proteinConsumed = (macros?.protein?.consumed ?: 0.0).toInt()
                    val proteinTarget = (macros?.protein?.target ?: 140.0).toInt()
                    val carbConsumed = (macros?.carb?.consumed ?: 0.0).toInt()
                    val carbTarget = (macros?.carb?.target ?: 220.0).toInt()
                    val fatConsumed = (macros?.fat?.consumed ?: 0.0).toInt()
                    val fatTarget = (macros?.fat?.target ?: 65.0).toInt()
                    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

                    MacroSection(
                        carbConsumed = carbConsumed,
                        carbTarget = carbTarget,
                        proteinConsumed = proteinConsumed,
                        proteinTarget = proteinTarget,
                        fatConsumed = fatConsumed,
                        fatTarget = fatTarget,
                        isDarkTheme = isDarkTheme,
                        shadowColor = shadowColor
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
                                onCopy = { targetDate -> viewModel.copyMeal(meal.id, targetDate) }
                            )
                        }
                    }
                }
            }
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

/**
 * Hero Calorie Card — focal point của dashboard
 * Ring ở giữa, label + consumed/target ở dưới
 */
@Composable
private fun CalorieHeroCard(
    targetCalories: Int,
    remainingCalories: Int,
    consumedCalories: Int,
    isDarkTheme: Boolean,
    shadowColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 8.dp else 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.14f) else Color.White,
                        MaterialTheme.colorScheme.outline
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Card label row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(VividOrange)
                    )
                    Text(
                        text = "Calo hôm nay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.3).sp
                    )
                }

                // Mục tiêu badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Mục tiêu: ${String.format("%,d", targetCalories)} kcal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calorie Ring — HERO visual
            ArcCaloriesGauge(
                remainingCalories = remainingCalories,
                targetCalories = targetCalories,
                isDarkTheme = isDarkTheme,
                ringSize = 196.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieStatChip(
                    label = "Đã nạp",
                    value = "${String.format("%,d", consumedCalories)} kcal",
                    valueColor = VividOrange
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                CalorieStatChip(
                    label = "Còn lại",
                    value = "${String.format("%,d", remainingCalories.coerceAtLeast(0))} kcal",
                    valueColor = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                CalorieStatChip(
                    label = "Mục tiêu",
                    value = "${String.format("%,d", targetCalories)} kcal",
                    valueColor = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun CalorieStatChip(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            letterSpacing = (-0.2).sp
        )
    }
}

/**
 * Macro section: 2-col Carbs+Protein + 1 Fat row ngang
 */
@Composable
private fun MacroSection(
    carbConsumed: Int,
    carbTarget: Int,
    proteinConsumed: Int,
    proteinTarget: Int,
    fatConsumed: Int,
    fatTarget: Int,
    isDarkTheme: Boolean,
    shadowColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Section title
        Text(
            text = "Dinh dưỡng đa lượng",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.3).sp
        )

        // 2-column row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoMacroCard(
                title = "Carbs",
                consumedGrams = carbConsumed,
                targetGrams = carbTarget,
                gradientColors = if (isDarkTheme)
                    listOf(CarbGradientStart, CarbGradientEnd)
                else
                    listOf(CarbGradientStartLight, CarbGradientEndLight),
                icon = Icons.Default.Grain,
                modifier = Modifier.weight(1f),
                isDarkTheme = isDarkTheme
            )
            BentoMacroCard(
                title = "Protein",
                consumedGrams = proteinConsumed,
                targetGrams = proteinTarget,
                gradientColors = if (isDarkTheme)
                    listOf(ProteinGradientStart, ProteinGradientEnd)
                else
                    listOf(ProteinGradientStartLight, ProteinGradientEndLight),
                icon = Icons.Default.Egg,
                modifier = Modifier.weight(1f),
                isDarkTheme = isDarkTheme
            )
        }

        // Fat card ngang — redesigned với progress bar
        FatCard(
            fatConsumed = fatConsumed,
            fatTarget = fatTarget,
            isDarkTheme = isDarkTheme,
            shadowColor = shadowColor
        )
    }
}

/**
 * Fat Card ngang — progress bar + percentage
 */
@Composable
private fun FatCard(
    fatConsumed: Int,
    fatTarget: Int,
    isDarkTheme: Boolean,
    shadowColor: Color
) {
    val rawProgress = if (fatTarget > 0) {
        (fatConsumed.toFloat() / fatTarget.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 900),
        label = "fat_progress"
    )
    val percentage = (rawProgress * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 6.dp else 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDarkTheme) FatBrush else FatBrushLight)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDarkTheme) 0.35f else 0.65f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TextDeepInk.copy(alpha = 0.09f))
                            .border(0.75.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Opacity,
                            contentDescription = null,
                            tint = TextDeepInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Chất béo",
                            fontWeight = FontWeight.Bold,
                            color = TextDeepInk,
                            fontSize = 15.sp,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "${fatConsumed}g / ${fatTarget}g",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDeepInk.copy(alpha = 0.60f)
                        )
                    }
                }

                // Percentage badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TextDeepInk.copy(alpha = 0.10f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${percentage}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDeepInk
                    )
                }
            }

            // Progress bar
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
            ) {
                val barHeight = size.height
                val cornerRadius = barHeight / 2f

                // Track
                drawRoundRect(
                    color = TextDeepInk.copy(alpha = 0.12f),
                    size = androidx.compose.ui.geometry.Size(size.width, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )

                if (animatedProgress > 0.01f) {
                    val progressWidth = size.width * animatedProgress
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                FatGradientStart.copy(alpha = 0.8f),
                                TextDeepInk.copy(alpha = 0.5f)
                            ),
                            startX = 0f,
                            endX = progressWidth
                        ),
                        size = androidx.compose.ui.geometry.Size(progressWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                    )
                }
            }
        }
    }
}

/**
 * Food Diary section header
 */
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
    onCopy: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
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
