package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CustomFoodDto
import com.calai.app.data.remote.dto.FoodItemDto
import com.calai.app.data.remote.dto.toFoodItemDto
import com.calai.app.presentation.components.SelectionPill
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.AddMealViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onBack: () -> Unit,
    onCameraClick: () -> Unit = {},
    onBarcodeClick: () -> Unit = {},
    isDarkTheme: Boolean = true,
    viewModel: AddMealViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showCreateCustomFoodDialog by remember { mutableStateOf(false) }

    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onBack()
        }
    }

    if (showQuickAddDialog) {
        QuickAddDialog(
            isSaving = uiState.isSaving,
            isDark = isDarkTheme,
            onDismiss = { showQuickAddDialog = false },
            onConfirm = { name, calories, protein, carb, fat ->
                viewModel.quickAdd(name, calories, protein, carb, fat)
            }
        )
    }

    if (showCreateCustomFoodDialog) {
        CreateCustomFoodDialog(
            isDark = isDarkTheme,
            onDismiss = { showCreateCustomFoodDialog = false },
            onConfirm = { name, servingSize, servingAmount, servingUnit, calories, protein, carb, fat ->
                viewModel.createCustomFood(name, servingSize, servingAmount, servingUnit, calories, protein, carb, fat)
                showCreateCustomFoodDialog = false
            }
        )
    }

    Scaffold(
        containerColor = if (isDarkTheme) ObsidianBackground else IvoryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thêm Bữa Ăn",
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = if (isDarkTheme) TextWhite else TextInkPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkTheme) ObsidianBackground else IvoryBackground
                )
            )
        },
        bottomBar = {
            if (uiState.selectedFoods.isNotEmpty()) {
                Surface(
                    color = if (isDarkTheme) CharcoalSurface else PearlCard,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = if (isDarkTheme) 8.dp else 12.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isDarkTheme) CharcoalBorder else PearlBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        val totalCal = uiState.selectedFoods.sumOf { (it.calories * it.quantity).toDouble() }
                        val totalP = uiState.selectedFoods.sumOf { (it.protein * it.quantity).toDouble() }
                        val totalC = uiState.selectedFoods.sumOf { (it.carb * it.quantity).toDouble() }
                        val totalF = uiState.selectedFoods.sumOf { (it.fat * it.quantity).toDouble() }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Đã chọn ${uiState.selectedFoods.size} món",
                                    fontSize = 13.sp,
                                    color = if (isDarkTheme) TextMuted else TextInkMuted
                                )
                                Text(
                                    text = "${totalP.toInt()}g P • ${totalC.toInt()}g C • ${totalF.toInt()}g F",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkTheme) PastelLavender else VividOrange
                                )
                            }

                            Text(
                                "${totalCal.toInt()} kcal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = VividOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.saveMeal() },
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextWhite)
                            } else {
                                Text("Lưu Vào Nhật Ký Bữa Ăn", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // 1. Phân loại bữa ăn (Sáng, Trưa, Tối, Phụ)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val mealTypes = listOf(
                        "BREAKFAST" to "Bữa Sáng",
                        "LUNCH" to "Bữa Trưa",
                        "DINNER" to "Bữa Tối",
                        "SNACK" to "Bữa Phụ"
                    )
                    mealTypes.forEach { (type, label) ->
                        val isSelected = uiState.mealType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) VividOrange else if (isDarkTheme) CharcoalSurface else PearlCard)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) VividOrange else if (isDarkTheme) CharcoalBorder else PearlBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.onMealTypeSelect(type) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextWhite else if (isDarkTheme) TextMuted else TextInkPrimary
                            )
                        }
                    }
                }
            }

            // 2. MÓN ĐÃ CHỌN VÀ CHUẨN HÓA KHẨU PHẦN (Serving Size Normalization)
            if (uiState.selectedFoods.isNotEmpty()) {
                item {
                    Text(
                        text = "Khẩu phần món đã chọn",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary
                    )
                }

                itemsIndexed(uiState.selectedFoods) { index, item ->
                    SelectedFoodServingCard(
                        item = item,
                        isDark = isDarkTheme,
                        onQuantityChange = { newQty -> viewModel.updateFoodQuantity(index, newQty) },
                        onRemove = { viewModel.removeFoodFromMeal(index) }
                    )
                }
            }

            // 3. CARD QUÉT ẢNH BẰNG AI (AI Vision Scanner)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDarkTheme) 6.dp else 8.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = shadowColor,
                            spotColor = shadowColor
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                        .border(1.5.dp, VividOrange.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                        .clickable { onCameraClick() }
                        .padding(18.dp)
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
                                    text = "AI Vision Scanner",
                                    color = VividOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Chụp ảnh đĩa thức ăn",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) TextWhite else TextInkPrimary
                            )
                            Text(
                                text = "Gemini AI tự nhận diện món, khẩu phần & calo trong 1s",
                                fontSize = 12.sp,
                                color = if (isDarkTheme) TextMuted else TextInkMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(VividOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 4. NHẬP NHANH CALO/MACRO (Quick Add)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                        .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(16.dp))
                        .clickable { showQuickAddDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = VividOrange, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Nhập nhanh Calo / Macro (không cần chọn món)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isDarkTheme) TextMuted else TextInkMuted, modifier = Modifier.size(18.dp))
                }
            }

            // 4b. QUÉT MÃ VẠCH SẢN PHẨM (Barcode Scanner)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                        .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(16.dp))
                        .clickable { onBarcodeClick() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = VividOrange, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Quét mã vạch sản phẩm đóng gói",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isDarkTheme) TextMuted else TextInkMuted, modifier = Modifier.size(18.dp))
                }
            }

            // 5. MÓN ĂN RIÊNG CỦA TÔI (Custom Foods)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Món của tôi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary
                    )
                    Text(
                        text = "+ Tạo món riêng",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividOrange,
                        modifier = Modifier.clickable { showCreateCustomFoodDialog = true }
                    )
                }
            }
            if (uiState.customFoods.isEmpty()) {
                item {
                    Text(
                        text = "Chưa có món riêng — tạo món bạn hay ăn để thêm nhanh chóng.",
                        fontSize = 12.5.sp,
                        color = if (isDarkTheme) TextMuted else TextInkMuted
                    )
                }
            } else {
                items(uiState.customFoods) { food ->
                    CustomFoodRow(
                        food = food,
                        isDark = isDarkTheme,
                        onAdd = { viewModel.addFoodToMeal(food.toFoodItemDto()) },
                        onDelete = { viewModel.deleteCustomFood(food.id) }
                    )
                }
            }

            // 6. TRA CỨU THỦ CÔNG KHO MÓN VIỆT
            item {
                Text(
                    text = "Hoặc tra cứu thủ công từ kho món Việt (120+ món)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) TextWhite else TextInkPrimary
                )
            }

            // Thanh tìm kiếm
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Tìm phở bò, cơm tấm, ức gà, trứng...", color = if (isDarkTheme) TextMuted else TextInkMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = if (isDarkTheme) TextMuted else TextInkMuted) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = if (isDarkTheme) TextMuted else TextInkMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDarkTheme) CharcoalSurface else PearlCard,
                        unfocusedContainerColor = if (isDarkTheme) CharcoalSurface else PearlCard,
                        focusedBorderColor = VividOrange,
                        unfocusedBorderColor = if (isDarkTheme) CharcoalBorder else PearlBorder,
                        focusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary,
                        unfocusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary
                    )
                )
            }

            // Danh mục món ăn
            if (uiState.categories.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.categories.forEach { cat ->
                            val isSelected = uiState.selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PastelLavender else if (isDarkTheme) CharcoalCard else PearlCard)
                                    .border(1.dp, if (isSelected) PastelLavender else if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.onCategorySelect(cat) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextDeepInk else if (isDarkTheme) TextWhite else TextInkPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Danh sách kết quả món ăn
            items(uiState.searchResults) { food ->
                FoodSearchResultCard(
                    food = food,
                    isDark = isDarkTheme,
                    isFavorite = food.name in uiState.favoriteNames,
                    onAdd = { viewModel.addFoodToMeal(food) },
                    onToggleFavorite = { viewModel.toggleFavorite(food.name) }
                )
            }
        }
    }
}

@Composable
private fun SelectedFoodServingCard(
    item: CreateMealItemDto,
    isDark: Boolean,
    onQuantityChange: (Float) -> Unit,
    onRemove: () -> Unit
) {
    val scaledCal = (item.calories * item.quantity).toInt()
    val scaledP = (item.protein * item.quantity).toInt()
    val scaledC = (item.carb * item.quantity).toInt()
    val scaledF = (item.fat * item.quantity).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) CharcoalSurface else PearlCard)
            .border(1.dp, if (isDark) CharcoalBorder else PearlBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else TextInkPrimary
                    )
                    Text(
                        text = "Gốc: ${item.servingSize ?: "1 phần"} • ${item.calories.toInt()} kcal",
                        fontSize = 11.5.sp,
                        color = if (isDark) TextMuted else TextInkMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$scaledCal kcal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = VividOrange
                    )

                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa món",
                            tint = if (isDark) TextMuted else TextInkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Macro summary pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServingMacroChip(label = "Protein", value = "${scaledP}g", color = if (isDark) ProteinGradientStart else PastelProteinLight, isDark = isDark)
                ServingMacroChip(label = "Carbs", value = "${scaledC}g", color = if (isDark) CarbGradientStart else PastelCarbLight, isDark = isDark)
                ServingMacroChip(label = "Fat", value = "${scaledF}g", color = if (isDark) FatGradientStart else PastelFatLight, isDark = isDark)
            }

            // Stepper & Quick Multipliers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stepper [-] Qty [+]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) CharcoalDock else PearlDock)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newQty = (item.quantity - 0.5f).coerceAtLeast(0.5f)
                            onQuantityChange(newQty)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = if (isDark) TextWhite else TextInkPrimary, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = String.format("%.1fx", item.quantity),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividOrange,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = {
                            val newQty = item.quantity + 0.5f
                            onQuantityChange(newQty)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng", tint = if (isDark) TextWhite else TextInkPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                // Quick Multiplier Chips: 0.5x, 1x, 1.5x, 2x
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { mult ->
                        val isCurrent = (item.quantity == mult)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) VividOrange else if (isDark) CharcoalDock else PearlDock)
                                .clickable { onQuantityChange(mult) }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${mult}x",
                                fontSize = 11.5.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) TextWhite else if (isDark) TextMuted else TextInkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServingMacroChip(label: String, value: String, color: Color, isDark: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) CharcoalDock else PearlDock)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 10.5.sp, color = if (isDark) TextMuted else TextInkMuted)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun CustomFoodRow(
    food: CustomFoodDto,
    isDark: Boolean,
    onAdd: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) CharcoalSurface else PearlCard)
            .border(1.dp, if (isDark) CharcoalBorder else PearlBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDark) TextWhite else TextInkPrimary)
                Text(
                    text = "${food.servingSize?.ifEmpty { "1 phần" } ?: "1 phần"} • ${food.protein.toInt()}g P • ${food.carb.toInt()}g C • ${food.fat.toInt()}g F",
                    fontSize = 12.sp,
                    color = if (isDark) TextMuted else TextInkMuted
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${food.calories.toInt()} kcal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividOrange,
                    modifier = Modifier.padding(end = 4.dp)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa món riêng", tint = if (isDark) TextMuted else TextInkMuted, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) CharcoalCard else PearlCard)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Chọn món", tint = if (isDark) TextWhite else TextInkPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun FoodSearchResultCard(
    food: FoodItemDto,
    isDark: Boolean,
    isFavorite: Boolean,
    onAdd: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) CharcoalSurface else PearlCard)
            .border(1.dp, if (isDark) CharcoalBorder else PearlBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else TextInkPrimary
                )
                Text(
                    text = "${food.servingSize.ifEmpty { "1 phần" }} • ${food.protein.toInt()}g P • ${food.carb.toInt()}g C • ${food.fat.toInt()}g F",
                    fontSize = 12.sp,
                    color = if (isDark) TextMuted else TextInkMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${food.calories.toInt()} kcal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividOrange,
                    modifier = Modifier.padding(end = 4.dp)
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Bỏ yêu thích" else "Đánh dấu yêu thích",
                        tint = if (isFavorite) VividOrange else if (isDark) TextMuted else TextInkMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) CharcoalCard else PearlCard)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Chọn món",
                        tint = if (isDark) TextWhite else TextInkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAddDialog(
    isSaving: Boolean,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, calories: Float, protein: Float, carb: Float, fat: Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    @Composable
    fun textFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isDark) CharcoalCard else PearlSurface,
        unfocusedContainerColor = if (isDark) CharcoalCard else PearlSurface,
        focusedBorderColor = VividOrange,
        unfocusedBorderColor = if (isDark) CharcoalBorder else PearlBorder,
        focusedTextColor = if (isDark) TextWhite else TextInkPrimary,
        unfocusedTextColor = if (isDark) TextWhite else TextInkPrimary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) CharcoalSurface else PearlCard,
        title = { Text("Nhập nhanh Calo / Macro", color = if (isDark) TextWhite else TextInkPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món / ghi chú") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { c -> c.isDigit() } },
                        label = { Text("Protein (g)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = carb,
                        onValueChange = { carb = it.filter { c -> c.isDigit() } },
                        label = { Text("Carb (g)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter { c -> c.isDigit() } },
                        label = { Text("Fat (g)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    onConfirm(
                        name,
                        calories.toFloatOrNull() ?: 0f,
                        protein.toFloatOrNull() ?: 0f,
                        carb.toFloatOrNull() ?: 0f,
                        fat.toFloatOrNull() ?: 0f
                    )
                }
            ) {
                Text(if (isSaving) "Đang lưu..." else "Lưu", color = VividOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = if (isDark) TextMuted else TextInkMuted)
            }
        }
    )
}

@Composable
private fun CreateCustomFoodDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, servingSize: String, servingAmount: Float?, servingUnit: String?, calories: Float, protein: Float, carb: Float, fat: Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var servingAmount by remember { mutableStateOf("") }
    var servingUnit by remember { mutableStateOf("PORTION") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    @Composable
    fun textFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isDark) CharcoalCard else PearlSurface,
        unfocusedContainerColor = if (isDark) CharcoalCard else PearlSurface,
        focusedBorderColor = VividOrange,
        unfocusedBorderColor = if (isDark) CharcoalBorder else PearlBorder,
        focusedTextColor = if (isDark) TextWhite else TextInkPrimary,
        unfocusedTextColor = if (isDark) TextWhite else TextInkPrimary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) CharcoalSurface else PearlCard,
        title = { Text("Tạo món ăn riêng", color = if (isDark) TextWhite else TextInkPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món ăn") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = servingSize,
                    onValueChange = { servingSize = it },
                    label = { Text("Khẩu phần (VD: 1 phần 300g)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = servingAmount,
                        onValueChange = { servingAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Số lượng", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    listOf("GRAM" to "g", "ML" to "ml", "PORTION" to "phần").forEach { (key, label) ->
                        SelectionPill(
                            label = label,
                            isSelected = servingUnit == key,
                            isDarkTheme = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = { servingUnit = key }
                        )
                    }
                }
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { c -> c.isDigit() } },
                        label = { Text("Protein (g)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = carb,
                        onValueChange = { carb = it.filter { c -> c.isDigit() } },
                        label = { Text("Carb (g)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter { c -> c.isDigit() } },
                        label = { Text("Fat (g)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name,
                        servingSize,
                        servingAmount.toFloatOrNull(),
                        servingUnit,
                        calories.toFloatOrNull() ?: 0f,
                        protein.toFloatOrNull() ?: 0f,
                        carb.toFloatOrNull() ?: 0f,
                        fat.toFloatOrNull() ?: 0f
                    )
                }
            ) {
                Text("Lưu", color = VividOrange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = if (isDark) TextMuted else TextInkMuted)
            }
        }
    )
}
