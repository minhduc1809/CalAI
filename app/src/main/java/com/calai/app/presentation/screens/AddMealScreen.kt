package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.remote.dto.CustomFoodDto
import com.calai.app.data.remote.dto.FoodItemDto
import com.calai.app.data.remote.dto.toFoodItemDto
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.AddMealViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onBack: () -> Unit,
    onCameraClick: () -> Unit = {},
    viewModel: AddMealViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showCreateCustomFoodDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onBack()
        }
    }

    if (showQuickAddDialog) {
        QuickAddDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showQuickAddDialog = false },
            onConfirm = { name, calories, protein, carb, fat ->
                viewModel.quickAdd(name, calories, protein, carb, fat)
            }
        )
    }

    if (showCreateCustomFoodDialog) {
        CreateCustomFoodDialog(
            onDismiss = { showCreateCustomFoodDialog = false },
            onConfirm = { name, servingSize, calories, protein, carb, fat ->
                viewModel.createCustomFood(name, servingSize, calories, protein, carb, fat)
                showCreateCustomFoodDialog = false
            }
        )
    }

    Scaffold(
        containerColor = ObsidianBackground,
        topBar = {
            TopAppBar(
                title = { Text("Thêm Bữa Ăn", fontWeight = FontWeight.Bold, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBackground)
            )
        },
        bottomBar = {
            if (uiState.selectedFoods.isNotEmpty()) {
                Surface(
                    color = CharcoalSurface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        val totalCal = uiState.selectedFoods.sumOf { (it.calories * it.quantity).toDouble() }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Đã chọn ${uiState.selectedFoods.size} món",
                                fontSize = 15.sp,
                                color = TextMuted
                            )
                            Text(
                                "${totalCal.toInt()} kcal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = VividOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.saveMeal() },
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextWhite)
                            } else {
                                Text("Lưu Vào Nhật Ký", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
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
                                .background(if (isSelected) VividOrange else CharcoalSurface)
                                .clickable { viewModel.onMealTypeSelect(type) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextWhite else TextMuted
                            )
                        }
                    }
                }
            }

            // 2. LỰA CHỌN 1: CARD QUÉT ẢNH BẰNG AI (Tâm điểm thị giác)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CharcoalSurface)
                        .border(1.5.dp, VividOrange.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .clickable { onCameraClick() }
                        .padding(20.dp)
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Chụp ảnh đĩa thức ăn",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Gemini AI tự nhận diện món, khẩu phần & calo trong 1 giây",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(VividOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            // 2b. LỰA CHỌN 1b: NHẬP NHANH CALO/MACRO (Quick Add — không cần chọn món)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CharcoalSurface)
                        .clickable { showQuickAddDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = VividOrange, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Nhập nhanh Calo/Macro (không cần chọn món)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            // 2c. MÓN ĂN RIÊNG CỦA TÔI (Custom Foods)
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
                        color = TextWhite
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
                        text = "Chưa có món ăn riêng nào — tạo món bạn hay ăn để lần sau thêm nhanh hơn.",
                        fontSize = 12.5.sp,
                        color = TextMuted
                    )
                }
            } else {
                items(uiState.customFoods) { food ->
                    CustomFoodRow(
                        food = food,
                        onAdd = { viewModel.addFoodToMeal(food.toFoodItemDto()) },
                        onDelete = { viewModel.deleteCustomFood(food.id) }
                    )
                }
            }

            // 3. LỰA CHỌN 2: TÌM KIẾM THỦ CÔNG 120+ MÓN VIỆT
            item {
                Text(
                    text = "Hoặc tra cứu thủ công từ kho món Việt",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            // Thanh tìm kiếm
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Tìm phở bò, cơm tấm, ức gà, trứng...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CharcoalSurface,
                        unfocusedContainerColor = CharcoalSurface,
                        focusedBorderColor = VividOrange,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
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
                                    .background(if (isSelected) PastelLavender else CharcoalCard)
                                    .clickable { viewModel.onCategorySelect(cat) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextDeepInk else TextWhite
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
                    isFavorite = food.name in uiState.favoriteNames,
                    onAdd = { viewModel.addFoodToMeal(food) },
                    onToggleFavorite = { viewModel.toggleFavorite(food.name) }
                )
            }
        }
    }
}

@Composable
private fun CustomFoodRow(
    food: CustomFoodDto,
    onAdd: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(
                    text = "${food.servingSize?.ifEmpty { "1 phần" } ?: "1 phần"} • ${food.protein.toInt()}g P • ${food.carb.toInt()}g C • ${food.fat.toInt()}g F",
                    fontSize = 12.sp,
                    color = TextMuted
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
                    Icon(Icons.Default.Delete, contentDescription = "Xóa món riêng", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CharcoalCard)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Chọn món", tint = TextWhite, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun FoodSearchResultCard(
    food: FoodItemDto,
    isFavorite: Boolean,
    onAdd: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalSurface)
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
                    color = TextWhite
                )
                Text(
                    text = "${food.servingSize.ifEmpty { "1 phần" }} • ${food.protein.toInt()}g P • ${food.carb.toInt()}g C • ${food.fat.toInt()}g F",
                    fontSize = 12.sp,
                    color = TextMuted
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
                        tint = if (isFavorite) VividOrange else TextMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CharcoalCard)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Chọn món",
                        tint = TextWhite,
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
        focusedContainerColor = CharcoalCard,
        unfocusedContainerColor = CharcoalCard,
        focusedBorderColor = VividOrange,
        unfocusedBorderColor = CharcoalBorder,
        focusedTextColor = TextWhite,
        unfocusedTextColor = TextWhite
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        title = { Text("Nhập nhanh Calo/Macro", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món / ghi chú", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text("Calories (kcal)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { c -> c.isDigit() } },
                        label = { Text("Protein (g)", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = carb,
                        onValueChange = { carb = it.filter { c -> c.isDigit() } },
                        label = { Text("Carb (g)", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter { c -> c.isDigit() } },
                        label = { Text("Fat (g)", color = TextMuted, fontSize = 11.sp) },
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
                Text("Hủy", color = TextMuted)
            }
        }
    )
}

@Composable
private fun CreateCustomFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, servingSize: String, calories: Float, protein: Float, carb: Float, fat: Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    @Composable
    fun textFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = CharcoalCard,
        unfocusedContainerColor = CharcoalCard,
        focusedBorderColor = VividOrange,
        unfocusedBorderColor = CharcoalBorder,
        focusedTextColor = TextWhite,
        unfocusedTextColor = TextWhite
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        title = { Text("Tạo món ăn riêng", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món ăn", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = servingSize,
                    onValueChange = { servingSize = it },
                    label = { Text("Khẩu phần (VD: 1 phần 300g)", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text("Calories (kcal)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { c -> c.isDigit() } },
                        label = { Text("Protein (g)", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = carb,
                        onValueChange = { carb = it.filter { c -> c.isDigit() } },
                        label = { Text("Carb (g)", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter { c -> c.isDigit() } },
                        label = { Text("Fat (g)", color = TextMuted, fontSize = 11.sp) },
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
                Text("Hủy", color = TextMuted)
            }
        }
    )
}
