package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.MealResponseDto
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Màn hình Home Dashboard - Hero Screen theo chuẩn Dark Luxury Canvas (CODING_RULES.md 9.2 & 9.6 #1)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        if (uiState.isLoading && uiState.dailySummary == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = VividOrange
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. TOP HEADER: Avatar + Chào buổi sáng + TactileThemeSwitch + Icons (Spec 9.2 #6, Spec 10.5)
                item {
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(VividOrange)
                                    .border(1.5.dp, VividOrangeLight.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.username.take(1).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = TextWhite
                                )
                            }
                            Column {
                                Text(
                                    text = "Chào buổi sáng,",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMuted
                                )
                                Text(
                                    text = uiState.username.ifEmpty { "Bạn" },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    letterSpacing = (-0.3).sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Nút chuyển giao diện Sáng / Tối dạng khối xúc giác 3D (Spec 10.5)
                            TactileThemeSwitch(
                                isDarkTheme = isDarkTheme,
                                onThemeChanged = onThemeChanged
                            )

                            // Nút Gợi ý món & Lộ trình tập luyện thông minh
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalSurface)
                                    .border(1.dp, CharcoalBorder, CircleShape)
                                    .clickable { onOpenSuggestions() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Gợi ý cho bạn",
                                    tint = VividOrange,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Nút Đăng xuất
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalSurface)
                                    .border(1.dp, CharcoalBorder, CircleShape)
                                    .clickable { onLogout() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Đăng xuất",
                                    tint = TextMuted,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // 2. THANH LỊCH TUẦN NGANG (Weekly Day Strip)
                item {
                    WeeklyCalendarStrip(
                        selectedDateIso = selectedDateIso,
                        onDateSelected = { newDate ->
                            selectedDateIso = newDate
                            viewModel.loadData(newDate)
                        }
                    )
                }

                // 3. TIÊU ĐỀ SECTION
                item {
                    Text(
                        text = "Tổng quan calo hôm nay",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = (-0.4).sp
                    )
                }

                // 4. HERO CARD CALORIES — Nền CharcoalCardElevated + Viền CharcoalBorder + Arc Gauge Gradient (Spec 9.2 #1, #3, #7)
                item {
                    val summary = uiState.dailySummary?.summary
                    val targetCal = (summary?.targetCalories ?: 2200.0).toInt()
                    val remainingCal = (summary?.remainingCalories ?: targetCal.toDouble()).toInt()
                    val consumedCal = (summary?.consumedCalories ?: 0.0).toInt()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(CharcoalCardElevated)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(28.dp))
                            .padding(22.dp)
                    ) {
                        Column {
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
                                        text = "Calories",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                Surface(
                                    color = VividOrangeSoft,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Đã nạp: $consumedCal kcal",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VividOrange,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Thước đo bán nguyệt Arc Gauge với Gradient & Ambient Glow
                            ArcCaloriesGauge(
                                remainingCalories = remainingCal,
                                targetCalories = targetCal,
                                isDarkTheme = true
                            )

                            // Tỉ lệ scale ở dưới đáy card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0 kcal",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Mục tiêu: $targetCal kcal",
                                    fontSize = 12.sp,
                                    color = TextLightGrey,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 5. THẺ BENTO MACRO ĐÁ QUÝ — 2 Cột + 1 Hàng Ngang (Spec 9.2 #2, #4)
                item {
                    val macros = uiState.dailySummary?.summary?.macros
                    val proteinConsumed = (macros?.protein?.consumed ?: 0.0).toInt()
                    val proteinTarget = (macros?.protein?.target ?: 140.0).toInt()

                    val carbConsumed = (macros?.carb?.consumed ?: 0.0).toInt()
                    val carbTarget = (macros?.carb?.target ?: 220.0).toInt()

                    val fatConsumed = (macros?.fat?.consumed ?: 0.0).toInt()
                    val fatTarget = (macros?.fat?.target ?: 65.0).toInt()

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Carbs Vàng Hổ Phách
                            BentoMacroCard(
                                title = "Carbs",
                                consumedGrams = carbConsumed,
                                targetGrams = carbTarget,
                                gradientColors = listOf(CarbGradientStart, CarbGradientEnd),
                                icon = Icons.Default.Grain,
                                modifier = Modifier.weight(1f)
                            )

                            // Protein Xanh Ngọc Lục Bảo
                            BentoMacroCard(
                                title = "Protein",
                                consumedGrams = proteinConsumed,
                                targetGrams = proteinTarget,
                                gradientColors = listOf(ProteinGradientStart, ProteinGradientEnd),
                                icon = Icons.Default.Egg,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Thẻ Fat Hồng Ngọc Ngang (Horizontal Gem Card)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(86.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(FatBrush)
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = "Chất béo (Fat)",
                                        fontWeight = FontWeight.Bold,
                                        color = TextDeepInk,
                                        fontSize = 15.sp,
                                        letterSpacing = (-0.2).sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${fatConsumed}g / ${fatTarget}g mục tiêu",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDeepInk.copy(alpha = 0.65f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
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
                            }
                        }
                    }
                }

                // 6. SECTION NHẬT KÝ BỮA ĂN (Diet Plan)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nhật ký bữa ăn",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            letterSpacing = (-0.3).sp
                        )
                        Surface(
                            color = VividOrangeSoft,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable { onAddMealClick() }
                        ) {
                            Text(
                                text = "+ Thêm món",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VividOrange,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Danh sách bữa ăn hoặc Trạng thái rỗng
                if (uiState.meals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(CharcoalCardElevated)
                                        .border(1.dp, CharcoalBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = VividOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Chưa có bữa ăn nào hôm nay",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Nhấn nút Camera Quét AI bên dưới để chụp món ăn và tính calo tức thì.",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.meals) { meal ->
                        MealItemRow(
                            meal = meal,
                            onDelete = { viewModel.deleteMeal(meal.id) },
                            onChangeMealType = { newType -> viewModel.changeMealType(meal.id, newType) },
                            onCopy = { targetDate -> viewModel.copyMeal(meal.id, targetDate) }
                        )
                    }
                }
            }
        }

        // 7. THANH ĐIỀU HƯỚNG NỔI DẠNG ĐẢO (Floating Island Dock)
        FloatingBottomDock(
            currentTab = DockTab.HOME,
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

private val MEAL_TYPE_LABELS = listOf(
    "BREAKFAST" to "Bữa Sáng",
    "LUNCH" to "Bữa Trưa",
    "DINNER" to "Bữa Tối",
    "SNACK" to "Bữa Phụ"
)

@Composable
private fun MealItemRow(
    meal: MealResponseDto,
    onDelete: () -> Unit,
    onChangeMealType: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }

    val mealTypeName = when (meal.mealType) {
        "BREAKFAST" -> "Bữa Sáng"
        "LUNCH" -> "Bữa Trưa"
        "DINNER" -> "Bữa Tối"
        else -> "Bữa Phụ"
    }

    if (showCopyDialog) {
        CopyMealDialog(
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
            .clip(RoundedCornerShape(22.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = VividOrangeSoft,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = mealTypeName,
                        color = VividOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val itemsSummary = meal.items.joinToString(", ") { "${it.name} (${it.quantity.toInt()}x)" }
                Text(
                    text = itemsSummary.ifEmpty { "Món ăn tổng hợp" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${meal.totalProtein.toInt()}g P • ${meal.totalCarb.toInt()}g C • ${meal.totalFat.toInt()}g F",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${meal.totalCalories.toInt()} kcal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Thêm hành động",
                            tint = TextMuted.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(CharcoalCardElevated)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            "Chuyển thành",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        MEAL_TYPE_LABELS.filter { it.first != meal.mealType }.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = TextWhite, fontSize = 13.sp) },
                                onClick = {
                                    showMenu = false
                                    onChangeMealType(type)
                                }
                            )
                        }
                        HorizontalDivider(color = CharcoalBorder)
                        DropdownMenuItem(
                            text = { Text("Sao chép sang ngày khác", color = TextWhite, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                showCopyDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa bữa ăn", color = CoralWarning, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralWarning, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyMealDialog(
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
        containerColor = CharcoalSurface,
        title = { Text("Sao chép bữa ăn sang ngày khác", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { (label, dateStr) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalCard)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                            .clickable { onConfirm(dateStr) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(dateStr, color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = TextMuted) }
        }
    )
}

