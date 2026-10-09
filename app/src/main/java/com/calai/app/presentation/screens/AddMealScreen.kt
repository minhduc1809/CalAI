package com.calai.app.presentation.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CustomFoodDto
import com.calai.app.data.remote.dto.FoodItemDto
import com.calai.app.data.remote.dto.toFoodItemDto
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.components.AppFormDialog
import com.calai.app.presentation.components.AppTextField
import com.calai.app.presentation.components.AppTextFieldCompact
import com.calai.app.presentation.components.DuotoneFlameIcon
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

    // Quick Add tự hiển thị lỗi ngay trong dialog của nó; các lỗi khác (vd. tạo món ăn riêng
    // đã đóng dialog trước khi biết kết quả) hiện qua Toast để không bị "biến mất trong im lặng".
    val context = LocalContext.current
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null && !showQuickAddDialog) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (showQuickAddDialog) {
        QuickAddDialog(
            isSaving = uiState.isSaving,
            errorMessage = uiState.errorMessage,
            isDark = isDarkTheme,
            onDismiss = {
                showQuickAddDialog = false
                viewModel.clearError()
            },
            onConfirm = { name, calories, protein, carb, fat ->
                viewModel.quickAdd(name, calories, protein, carb, fat)
            }
        )
    }

    if (showCreateCustomFoodDialog) {
        CreateCustomFoodDialog(
            isDark = isDarkTheme,
            onDismiss = { showCreateCustomFoodDialog = false },
            onConfirm = { name, servingSize, servingAmount, servingUnit, calories, protein, carb, fat, ingredients ->
                viewModel.createCustomFood(name, servingSize, servingAmount, servingUnit, calories, protein, carb, fat, ingredients)
                showCreateCustomFoodDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thêm Bữa Ăn",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (uiState.selectedFoods.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = if (isDarkTheme) 8.dp else 12.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

                        AppButton(
                            text = "Lưu Vào Nhật Ký Bữa Ăn",
                            onClick = { viewModel.saveMeal() },
                            enabled = !uiState.isSaving,
                            isLoading = uiState.isSaving,
                            height = 50.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
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
                                .shadow(
                                    elevation = if (isSelected) 4.dp else 0.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    ambientColor = CtaSolidOrange.copy(alpha = 0.35f),
                                    spotColor = CtaSolidOrange.copy(alpha = 0.35f)
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) CtaSolidOrange else MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CtaSolidOrange else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.onMealTypeSelect(type) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onBackground
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
                        .background(MaterialTheme.colorScheme.surface)
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
                                    text = "Quét Ảnh Bằng AI",
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
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Gemini AI tự nhận diện món, khẩu phần & calo trong 1s",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
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
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            // 4b. QUÉT MÃ VẠCH SẢN PHẨM (Barcode Scanner)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
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
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
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
                        color = MaterialTheme.colorScheme.onBackground
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Thanh tìm kiếm
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Tìm phở bò, cơm tấm, ức gà, trứng...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = VividOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
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
                                    .background(if (isSelected) PastelLavender else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, if (isSelected) PastelLavender else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.onCategorySelect(cat) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextDeepInk else MaterialTheme.colorScheme.onBackground
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Gốc: ${item.servingSize ?: "1 phần"} • ${item.calories.toInt()} kcal",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newQty = (item.quantity - 0.5f).coerceAtLeast(0.5f)
                            onQuantityChange(newQty)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
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
                        Icon(Icons.Default.Add, contentDescription = "Tăng", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                    }
                }

                // Quick Multiplier Chips: 0.5x, 1x, 1.5x, 2x
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { mult ->
                        val isCurrent = (item.quantity == mult)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) VividOrange else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onQuantityChange(mult) }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${mult}x",
                                fontSize = 11.5.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = "${food.servingSize?.ifEmpty { "1 phần" } ?: "1 phần"} • ${food.protein.toInt()}g P • ${food.carb.toInt()}g C • ${food.fat.toInt()}g F",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Icon(Icons.Default.Delete, contentDescription = "Xóa món riêng", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Chọn món", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${food.servingSize.ifEmpty { "1 phần" }} • ${food.protein.toInt()}g P • ${food.carb.toInt()}g C • ${food.fat.toInt()}g F",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        tint = if (isFavorite) VividOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Chọn món",
                        tint = MaterialTheme.colorScheme.onBackground,
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
    errorMessage: String? = null,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, calories: Float, protein: Float, carb: Float, fat: Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AppFormDialog(
        title = "Thêm mới — Nhập nhanh Calo/Macro",
        onDismiss = onDismiss,
        actions = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Hủy", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AppButton(
                text = "Lưu",
                onClick = {
                    onConfirm(
                        name,
                        calories.toFloatOrNull() ?: 0f,
                        protein.toFloatOrNull() ?: 0f,
                        carb.toFloatOrNull() ?: 0f,
                        fat.toFloatOrNull() ?: 0f
                    )
                },
                enabled = !isSaving && calories.toFloatOrNull() != null,
                isLoading = isSaving,
                modifier = Modifier.weight(1f),
                height = 44.dp,
                shape = RoundedCornerShape(12.dp)
            )
        }
    ) {
        AppTextField(
            label = "Tên món / ghi chú",
            value = name,
            onValueChange = { name = it },
            placeholder = "VD: Ăn vặt buổi chiều"
        )
        AppTextField(
            label = "Calories (kcal)",
            value = calories,
            onValueChange = { calories = it.filter { c -> c.isDigit() } },
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            placeholder = "0"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextFieldCompact(
                label = "Protein (g)",
                value = protein,
                onValueChange = { protein = it.filter { c -> c.isDigit() } },
                modifier = Modifier.weight(1f)
            )
            AppTextFieldCompact(
                label = "Carb (g)",
                value = carb,
                onValueChange = { carb = it.filter { c -> c.isDigit() } },
                modifier = Modifier.weight(1f)
            )
            AppTextFieldCompact(
                label = "Fat (g)",
                value = fat,
                onValueChange = { fat = it.filter { c -> c.isDigit() } },
                modifier = Modifier.weight(1f)
            )
        }
        if (errorMessage != null) {
            Text(errorMessage, color = CrimsonError, fontSize = 12.5.sp)
        }
    }
}

private data class RecipeIngredientDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "",
    var calories: String = "",
    var protein: String = "",
    var carb: String = "",
    var fat: String = ""
)

@Composable
private fun CreateCustomFoodDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String, servingSize: String, servingAmount: Float?, servingUnit: String?,
        calories: Float, protein: Float, carb: Float, fat: Float,
        ingredients: List<com.calai.app.data.remote.dto.RecipeIngredientDto>?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var servingAmount by remember { mutableStateOf("") }
    var servingUnit by remember { mutableStateOf("PORTION") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    // Recipe: nhiều nguyên liệu, calo/macro tổng tự cộng dồn — thay vì bắt tự tính tay rồi nhập 1 số duy nhất.
    var isRecipeMode by remember { mutableStateOf(false) }
    val ingredientDrafts = remember { mutableStateListOf(RecipeIngredientDraft()) }

    val proteinColor = if (isDark) ProteinGradientStart else ProteinGradientStartLight
    val carbColor = if (isDark) CarbGradientStart else CarbGradientStartLight
    val fatColor = if (isDark) FatGradientStart else FatGradientStartLight

    val recipeTotalCalories = ingredientDrafts.sumOf { (it.calories.toFloatOrNull() ?: 0f).toDouble() }.toFloat()
    val recipeTotalProtein = ingredientDrafts.sumOf { (it.protein.toFloatOrNull() ?: 0f).toDouble() }.toFloat()
    val recipeTotalCarb = ingredientDrafts.sumOf { (it.carb.toFloatOrNull() ?: 0f).toDouble() }.toFloat()
    val recipeTotalFat = ingredientDrafts.sumOf { (it.fat.toFloatOrNull() ?: 0f).toDouble() }.toFloat()

    val effectiveCalories = if (isRecipeMode) recipeTotalCalories else (calories.toFloatOrNull() ?: 0f)
    val effectiveProtein = if (isRecipeMode) recipeTotalProtein else (protein.toFloatOrNull() ?: 0f)
    val effectiveCarb = if (isRecipeMode) recipeTotalCarb else (carb.toFloatOrNull() ?: 0f)
    val effectiveFat = if (isRecipeMode) recipeTotalFat else (fat.toFloatOrNull() ?: 0f)

    // Tỷ lệ macro theo calo (Protein/Carb = 4 kcal/g, Fat = 9 kcal/g) — chỉ mang tính minh họa,
    // không phải nguồn tính calo chính thức.
    val proteinKcal = effectiveProtein * 4f
    val carbKcal = effectiveCarb * 4f
    val fatKcal = effectiveFat * 9f
    val macroKcalTotal = proteinKcal + carbKcal + fatKcal
    val proteinRatio = if (macroKcalTotal > 0f) proteinKcal / macroKcalTotal else 0f
    val carbRatio = if (macroKcalTotal > 0f) carbKcal / macroKcalTotal else 0f
    val fatRatio = if (macroKcalTotal > 0f) fatKcal / macroKcalTotal else 0f

    val canSave = name.isNotBlank() && if (isRecipeMode) {
        ingredientDrafts.any { it.name.isNotBlank() && (it.calories.toFloatOrNull() ?: 0f) > 0f }
    } else {
        calories.toFloatOrNull() != null
    }

    AppFormDialog(
        title = "Thêm mới — Món ăn riêng",
        subtitle = "Tùy chỉnh khẩu phần & giá trị dinh dưỡng",
        fullScreen = true,
        onDismiss = onDismiss,
        actions = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Hủy", color = TextInkSecondary)
            }
            AppButton(
                text = "Lưu món ăn",
                onClick = {
                    val ingredientsPayload = if (isRecipeMode) {
                        ingredientDrafts
                            .filter { it.name.isNotBlank() && (it.calories.toFloatOrNull() ?: 0f) > 0f }
                            .map {
                                com.calai.app.data.remote.dto.RecipeIngredientDto(
                                    name = it.name,
                                    calories = it.calories.toFloatOrNull() ?: 0f,
                                    protein = it.protein.toFloatOrNull() ?: 0f,
                                    carb = it.carb.toFloatOrNull() ?: 0f,
                                    fat = it.fat.toFloatOrNull() ?: 0f
                                )
                            }
                    } else null

                    onConfirm(
                        name,
                        servingSize,
                        servingAmount.toFloatOrNull(),
                        servingUnit,
                        effectiveCalories,
                        effectiveProtein,
                        effectiveCarb,
                        effectiveFat,
                        ingredientsPayload
                    )
                },
                enabled = canSave,
                modifier = Modifier.weight(1f),
                height = 44.dp,
                shape = RoundedCornerShape(12.dp)
            )
        }
    ) {
        AppTextField(
            label = "Tên món ăn",
            value = name,
            onValueChange = { name = it },
            placeholder = "VD: Cơm gà xối mỡ",
            trailingHint = "bắt buộc"
        )

        // Chuyển đổi Món đơn giản (nhập thẳng 1 số calo) / Công thức nhiều nguyên liệu (tự cộng dồn)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionPill(
                label = "Món đơn giản",
                isSelected = !isRecipeMode,
                isDarkTheme = isDark,
                modifier = Modifier.weight(1f),
                onClick = { isRecipeMode = false }
            )
            SelectionPill(
                label = "Công thức nhiều nguyên liệu",
                isSelected = isRecipeMode,
                isDarkTheme = isDark,
                modifier = Modifier.weight(1f),
                onClick = { isRecipeMode = true }
            )
        }

        AppTextField(
            label = "Khẩu phần chuẩn",
            value = servingSize,
            onValueChange = { servingSize = it },
            placeholder = "VD: 1 phần 300g"
        )

        // Số lượng tiêu thụ — stepper -/+ (giữ đúng pattern đã dùng ở card "Khẩu phần món đã chọn"
        // trong màn này) thay vì ô nhập số trần, cho phép chỉnh nhanh không cần bàn phím.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Số lượng tiêu thụ",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextInkSecondary
                )
                Text(
                    text = "Đơn vị đo lường",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextInkSecondary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val current = servingAmount.toFloatOrNull() ?: 1f
                            servingAmount = (current - 1f).coerceAtLeast(1f).let {
                                if (it == it.toInt().toFloat()) it.toInt().toString() else it.toString()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = servingAmount.ifBlank { "1" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividOrange,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    IconButton(
                        onClick = {
                            val current = servingAmount.toFloatOrNull() ?: 1f
                            servingAmount = (current + 1f).let {
                                if (it == it.toInt().toFloat()) it.toInt().toString() else it.toString()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                    }
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
            }
        }

        if (isRecipeMode) {
            // Danh sách nguyên liệu — mỗi dòng tự cộng dồn vào tổng calo/macro của cả món,
            // thay vì bắt người dùng tự cộng tay rồi nhập 1 số duy nhất.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ingredientDrafts.forEachIndexed { index, draft ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.TextField(
                                value = draft.name,
                                onValueChange = { ingredientDrafts[index] = draft.copy(name = it) },
                                placeholder = { Text("Tên nguyên liệu ${index + 1}") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            if (ingredientDrafts.size > 1) {
                                IconButton(onClick = { ingredientDrafts.removeAt(index) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Gỡ nguyên liệu", tint = CoralWarning, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AppTextFieldCompact(
                                label = "Kcal", value = draft.calories,
                                onValueChange = { ingredientDrafts[index] = draft.copy(calories = it.filter { c -> c.isDigit() }) },
                                modifier = Modifier.weight(1f), leadingDot = VividOrange
                            )
                            AppTextFieldCompact(
                                label = "P", value = draft.protein,
                                onValueChange = { ingredientDrafts[index] = draft.copy(protein = it.filter { c -> c.isDigit() }) },
                                modifier = Modifier.weight(1f), leadingDot = proteinColor
                            )
                            AppTextFieldCompact(
                                label = "C", value = draft.carb,
                                onValueChange = { ingredientDrafts[index] = draft.copy(carb = it.filter { c -> c.isDigit() }) },
                                modifier = Modifier.weight(1f), leadingDot = carbColor
                            )
                            AppTextFieldCompact(
                                label = "F", value = draft.fat,
                                onValueChange = { ingredientDrafts[index] = draft.copy(fat = it.filter { c -> c.isDigit() }) },
                                modifier = Modifier.weight(1f), leadingDot = fatColor
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { ingredientDrafts.add(RecipeIngredientDraft()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = VividOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thêm nguyên liệu", color = VividOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Tổng cộng — chỉ đọc, tự tính từ các nguyên liệu bên trên
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VividOrangeSoft)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TỔNG CỘNG MÓN", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextInkSecondary)
                        Text(
                            "${recipeTotalProtein.toInt()}g P • ${recipeTotalCarb.toInt()}g C • ${recipeTotalFat.toInt()}g F",
                            fontSize = 11.5.sp, color = TextInkSecondary
                        )
                    }
                    Text("${recipeTotalCalories.toInt()} kcal", fontSize = 20.sp, fontWeight = FontWeight.Black, color = VividOrange)
                }
            }
        } else {
            // Năng lượng — thẻ nổi bật riêng (icon duotone lửa, đúng ngữ nghĩa Calories),
            // thay vì dồn chung 1 ô nhập trần như 3 field macro bên dưới.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VividOrangeSoft)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DuotoneFlameIcon(size = 26.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NĂNG LƯỢNG",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInkSecondary
                    )
                    Text(
                        text = "Calories (kcal)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                androidx.compose.material3.TextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = VividOrange,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    ),
                    placeholder = { Text("0", textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.fillMaxWidth()) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(96.dp)
                )
            }

            // Tỷ lệ dinh dưỡng đa lượng — thanh nhiều màu theo đúng token macro có sẵn (Protein/Carb/Fat),
            // chỉ minh họa tỷ lệ giữa 3 macro đã nhập, không phải nguồn tính calo.
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tỷ lệ dinh dưỡng đa lượng",
                        fontSize = 11.sp,
                        color = TextInkSecondary
                    )
                    Text(
                        text = "100% Khẩu phần",
                        fontSize = 11.sp,
                        color = TextInkSecondary
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (macroKcalTotal > 0f) {
                        Box(modifier = Modifier.weight(proteinRatio.coerceAtLeast(0.0001f)).fillMaxHeight().background(proteinColor))
                        Box(modifier = Modifier.weight(carbRatio.coerceAtLeast(0.0001f)).fillMaxHeight().background(carbColor))
                        Box(modifier = Modifier.weight(fatRatio.coerceAtLeast(0.0001f)).fillMaxHeight().background(fatColor))
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextFieldCompact(
                    label = "Protein",
                    value = protein,
                    onValueChange = { protein = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    leadingDot = proteinColor
                )
                AppTextFieldCompact(
                    label = "Carb",
                    value = carb,
                    onValueChange = { carb = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    leadingDot = carbColor
                )
                AppTextFieldCompact(
                    label = "Fat",
                    value = fat,
                    onValueChange = { fat = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    leadingDot = fatColor
                )
            }
        }
    }
}
