package com.calai.app.presentation.screens

import androidx.compose.foundation.background
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

@Composable
fun HomeScreen(
    onAddMealClick: () -> Unit,
    onCameraClick: () -> Unit = {},
    onNavigateTab: (DockTab) -> Unit = {},
    onLogout: () -> Unit = {},
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
                // 1. TOP HEADER: Avatar + Chào buổi sáng + Icons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(VividOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.username.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextWhite
                                )
                            }
                            Column {
                                Text(
                                    text = "Chào buổi sáng,",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = uiState.username.ifEmpty { "Bạn" },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = onLogout) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Đăng xuất",
                                    tint = TextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = TextWhite,
                                    modifier = Modifier.size(20.dp)
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
                        letterSpacing = (-0.3).sp
                    )
                }

                // 4. THẺ CALO LỚN MÀU PASTEL LAVENDER (Chuẩn ảnh mẫu)
                item {
                    val summary = uiState.dailySummary?.summary
                    val targetCal = (summary?.targetCalories ?: 2200.0).toInt()
                    val remainingCal = (summary?.remainingCalories ?: targetCal.toDouble()).toInt()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(PastelLavender)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Calories",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDeepInk
                                )

                                summary?.let {
                                    Text(
                                        text = "Đã nạp: ${it.consumedCalories.toInt()} kcal",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextDeepInk.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Thước đo bán nguyệt Arc Gauge
                            ArcCaloriesGauge(
                                remainingCalories = remainingCal,
                                targetCalories = targetCal
                            )

                            // Tỉ lệ scale ở dưới đáy card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0", fontSize = 12.sp, color = TextDeepInk.copy(alpha = 0.5f))
                                Text("Mục tiêu: $targetCal", fontSize = 12.sp, color = TextDeepInk.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 5. THẺ BENTO MACRO (Carbs Vàng + Protein Xanh Ngọc)
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
                            BentoMacroCard(
                                title = "Carbs",
                                consumedGrams = carbConsumed,
                                targetGrams = carbTarget,
                                containerColor = PastelButtercup,
                                icon = Icons.Default.Grain,
                                modifier = Modifier.weight(1f)
                            )

                            BentoMacroCard(
                                title = "Protein",
                                consumedGrams = proteinConsumed,
                                targetGrams = proteinTarget,
                                containerColor = PastelMint,
                                icon = Icons.Default.Egg,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Thẻ Fat thứ 3 (Trải dài thanh lịch)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PastelRose)
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Chất béo (Fat)", fontWeight = FontWeight.Bold, color = TextDeepInk, fontSize = 15.sp)
                                    Text("${fatConsumed}g / ${fatTarget}g", fontSize = 12.sp, color = TextDeepInk.copy(alpha = 0.6f))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(TextDeepInk.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Opacity, contentDescription = null, tint = TextDeepInk, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // 6. SECTION KẾ HOẠCH BỮA ĂN (Diet Plan)
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
                            color = TextWhite
                        )
                        Text(
                            text = "+ Thêm món",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividOrange,
                            modifier = Modifier.clickable { onAddMealClick() }
                        )
                    }
                }

                // Danh sách bữa ăn hoặc Trạng thái rỗng
                if (uiState.meals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Chưa có bữa ăn nào hôm nay",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Nhấn nút Quét AI bên dưới để chụp đĩa thức ăn và tính calo ngay lập tức.",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
            .clip(RoundedCornerShape(20.dp))
            .background(CharcoalSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = VividOrange.copy(alpha = 0.15f),
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
                            tint = TextMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(CharcoalCard)
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
