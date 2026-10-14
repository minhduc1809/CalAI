package com.calai.app.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.calai.app.data.remote.dto.MenuItemDto
import com.calai.app.domain.util.MealTimeHelper
import com.calai.app.R
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.components.CuteLoadingIndicator
import com.calai.app.presentation.components.DuotoneCheckmarkIcon
import com.calai.app.presentation.components.DuotoneDietIcon
import com.calai.app.presentation.components.DuotoneMenuListIcon
import com.calai.app.presentation.components.DuotoneSparkleIcon
import com.calai.app.presentation.components.DuotoneTipIcon
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.CameraScanViewModel
import com.calai.app.presentation.viewmodel.ScanMode
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScanScreen(
    onBack: () -> Unit,
    onOpenPremium: () -> Unit = {},
    viewModel: CameraScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showCamera by remember { mutableStateOf(false) }

    val launchCamera = { showCamera = true }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                context,
                "Vui lòng cấp quyền Camera để chụp ảnh",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onImageCapturedOrSelected(it, context)
        }
    }

    if (showCamera) {
        androidx.activity.compose.BackHandler { showCamera = false }
        com.calai.app.presentation.components.InAppCamera(
            onCaptured = { uri ->
                showCamera = false
                viewModel.onImageCapturedOrSelected(uri, context)
            },
            onClose = { showCamera = false },
        )
        return
    }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            Toast.makeText(context, "Đã lưu bữa ăn vào nhật ký thành công!", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.scanMode == ScanMode.FOOD) "Quét Món Ăn Bằng AI" else "Quét Thực Đơn Quán Ăn",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thanh chuyển Tab chế độ quét (Món Ăn vs Thực Đơn)
            if (uiState.selectedImageUri == null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isFood = uiState.scanMode == ScanMode.FOOD
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isFood) VividOrange else Color.Transparent)
                                .clickable { viewModel.setScanMode(ScanMode.FOOD) }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                DuotoneDietIcon(
                                    size = 16.dp,
                                    outlineColor = if (isFood) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    accentColor = if (isFood) OnPrimary else PrimaryStrong
                                )
                                Text(
                                    text = "Đĩa Món Ăn",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFood) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isMenu = uiState.scanMode == ScanMode.MENU
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isMenu) VividOrange else Color.Transparent)
                                .clickable { viewModel.setScanMode(ScanMode.MENU) }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                DuotoneMenuListIcon(
                                    size = 16.dp,
                                    outlineColor = if (isMenu) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    accentColor = if (isMenu) OnPrimary else PrimaryStrong
                                )
                                Text(
                                    text = "Thực Đơn",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMenu) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            uiState.errorMessage?.let { errorMsg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CrimsonError.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonError)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (uiState.selectedImageUri == null) {
                Spacer(modifier = Modifier.height(10.dp))

                // Badge hạn mức — dữ liệu thật từ ai/quota; hết lượt thì bấm để xem gói Premium
                val quota = uiState.aiQuota
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (uiState.isQuotaExhausted) CrimsonError.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .padding(bottom = 14.dp)
                        .clickable(enabled = uiState.isQuotaExhausted) { onOpenPremium() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (uiState.isQuotaExhausted) CrimsonError else PrimaryStrong,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when {
                                quota == null -> "Đang tải hạn mức chụp ảnh AI..."
                                uiState.isQuotaExhausted -> "Đã hết lượt chụp hôm nay — bấm để xem gói Premium"
                                else -> "Còn ${quota.totalRemaining} lượt chụp AI hôm nay"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.isQuotaExhausted) CrimsonError else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Khung ngắm Camera
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(VividOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (uiState.scanMode == ScanMode.FOOD) Icons.Default.CameraAlt else Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = PrimaryStrong,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = if (uiState.scanMode == ScanMode.FOOD) "Đưa món ăn vào khung hình" else "Chụp toàn bộ thực đơn quán ăn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.scanMode == ScanMode.FOOD)
                                "NutriWise nhận diện món ăn và ước tính calo từ ảnh"
                            else
                                "AI sẽ bóc tách danh sách món và gợi ý món tối ưu nhất cho calo hôm nay của bạn",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AppButton(
                        text = "Chụp Ảnh",
                        onClick = {
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCameraPermission) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = OnPrimary) },
                        modifier = Modifier.weight(1f),
                        height = 54.dp,
                        shape = RoundedCornerShape(18.dp)
                    )

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thư Viện", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = PastelButtercup,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (uiState.scanMode == ScanMode.FOOD)
                                "Mẹo: Chụp từ góc nghiêng 45° với ánh sáng rõ ràng để AI ước tính kích thước đĩa và khẩu phần chính xác nhất."
                            else
                                "Mẹo: Đặt thực đơn thẳng và phẳng, đủ ánh sáng để AI đọc rõ tên món và giá tiền.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Ảnh đã chọn kèm tia Laser quét
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = uiState.selectedImageUri,
                        contentDescription = "Ảnh đã chụp",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (uiState.isAnalyzing) {
                        val infiniteTransition = rememberInfiniteTransition(label = "laser")
                        val scanOffsetY by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 230f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "laser_y"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = scanOffsetY.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, VividOrange, Color.White, VividOrange, Color.Transparent)
                                    )
                                )
                        )
                    }
                }

                if (uiState.isAnalyzing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Hiệu ứng loading "cute" khi AI đang phân tích ảnh (Phần 12.4 —
                        // đúng ngữ cảnh được khuyến nghị dùng Lottie thay vì spinner trơn).
                        CuteLoadingIndicator(rawResId = R.raw.loading_general, size = 96.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (uiState.scanMode == ScanMode.FOOD) "NutriWise đang nhận diện món ăn..." else "NutriWise đang đọc thực đơn...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // KẾT QUẢ 1: Quét món ăn (FOOD RESULT)
                uiState.result?.let { food ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.foodName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    Text(food.servingSize, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    color = VividOrange.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "${food.totalCalories.toInt()} kcal",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryStrong,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                MacroTag("Đạm (P)", "${food.totalProtein.toInt()}g", PastelLavender)
                                MacroTag("Tinh bột (C)", "${food.totalCarb.toInt()}g", PastelButtercup)
                                MacroTag("Chất béo (F)", "${food.totalFat.toInt()}g", PastelRose)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Danh sách món nhận diện: nhãn "Ước tính" và đổi món khi AI nhận sai (BR-11.1)
                            food.items.forEachIndexed { index, item ->
                                RecognizedItemRow(
                                    item = item,
                                    onPickAlternative = { alt -> viewModel.swapRecognizedItem(index, alt) }
                                )
                                if (index < food.items.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (food.unrecognized.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Chưa ước tính được: ${food.unrecognized.joinToString(", ")}. Bạn có thể tìm món này trong kho món để thêm thủ công.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Lời khuyên sức khỏe
                            if (food.healthTip.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DuotoneTipIcon(size = 16.dp, outlineColor = MaterialTheme.colorScheme.onSurfaceVariant, accentColor = VividOrange)
                                        Text(
                                            text = food.healthTip,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Chọn bữa ăn
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val mealTypes = listOf("BREAKFAST" to "Bữa Sáng", "LUNCH" to "Bữa Trưa", "DINNER" to "Bữa Tối", "SNACK" to "Bữa Phụ")
                                mealTypes.forEach { (type, label) ->
                                    val isSelected = uiState.mealType == type
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) VividOrange else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { viewModel.onMealTypeSelect(type) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.resetState() },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                                ) {
                                    Text("Quét Lại")
                                }

                                AppButton(
                                    text = "Lưu Bữa Ăn",
                                    onClick = { viewModel.saveRecognizedMeal() },
                                    enabled = !uiState.isSaving,
                                    isLoading = uiState.isSaving,
                                    modifier = Modifier.weight(1.5f),
                                    height = 50.dp,
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }
                }

                // KẾT QUẢ 2: Quét thực đơn (MENU SCAN RESULT)
                uiState.menuResult?.let { menu ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        menu.restaurantName?.let { name ->
                            Text(
                                text = "Quán: $name",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Surface(
                            color = PastelLavender.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavender.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DuotoneSparkleIcon(size = 16.dp, outlineColor = MaterialTheme.colorScheme.onBackground, accentColor = PastelLavender)
                                Text(
                                    text = menu.summaryAdvice,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Text(
                            text = "DANH SÁCH MÓN ĂN NHẬN DIỆN:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        menu.items.forEachIndexed { index, item ->
                            MenuItemCard(
                                item = item,
                                isSaving = uiState.isSaving,
                                isSelected = index in uiState.selectedMenuItemIndices,
                                onToggleSelect = { viewModel.toggleMenuItemSelection(index) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        menu.disclaimer?.takeIf { it.isNotBlank() }?.let { note ->
                            Text(
                                text = note,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }

                        if (uiState.selectedMenuItemIndices.isNotEmpty()) {
                            AppButton(
                                text = "Lưu ${uiState.selectedMenuItemIndices.size} Món Đã Chọn",
                                onClick = { viewModel.saveSelectedMenuItems() },
                                enabled = !uiState.isSaving,
                                isLoading = uiState.isSaving,
                                shape = RoundedCornerShape(14.dp),
                                height = 48.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetState() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                        ) {
                            Text("Chụp Menu Khác")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItemDto,
    isSaving: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit
) {
    val hasNutrition = item.nutritionAvailable != false
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else if (item.isRecommended || item.warning != null) 1.5.dp else 1.dp,
            if (item.warning != null) CrimsonError
            else if (isSelected) MintJade
            else if (item.isRecommended) VividOrange
            else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSaving && hasNutrition) { onToggleSelect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        enabled = !isSaving && hasNutrition,
                        colors = CheckboxDefaults.colors(checkedColor = MintJade)
                    )
                    Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        if (item.isRecommended) {
                            Surface(
                                color = VividOrange.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Gợi ý", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryStrong, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        if (item.isEstimate) {
                            Surface(
                                color = PastelLavender.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Ước tính", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    item.price?.let {
                        Text(it, fontSize = 13.sp, color = MintJade, fontWeight = FontWeight.SemiBold)
                    }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (hasNutrition) "${item.estimatedCalories} kcal" else "Chưa có số liệu",
                        fontSize = if (hasNutrition) 14.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasNutrition) VividOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (hasNutrition) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("P: ${item.protein}g", fontSize = 12.sp, color = PastelLavender, fontWeight = FontWeight.Medium)
                    Text("C: ${item.carbs}g", fontSize = 12.sp, color = PastelButtercup, fontWeight = FontWeight.Medium)
                    Text("F: ${item.fat}g", fontSize = 12.sp, color = PastelRose, fontWeight = FontWeight.Medium)
                }
                item.servingSize?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "Theo khẩu phần chuẩn: $it (khẩu phần ở quán có thể khác)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                Text(
                    "Bạn có thể tìm món này trong kho món để thêm thủ công.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item.warning?.let { warning ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(color = CrimsonError.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CrimsonError,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            item.recommendationReason?.let { reason ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DuotoneCheckmarkIcon(size = 13.dp, outlineColor = PastelMint)
                    Text(reason, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

        }
    }
}

@Composable
private fun MacroTag(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun createTempPictureUri(context: Context): Uri {
    val tempFile = File.createTempFile("camera_${System.currentTimeMillis()}", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}

/**
 * Một món trong kết quả nhận diện ảnh. Món không có trong kho mang nhãn "Ước tính"; món có trong kho cho phép đổi
 * sang tối đa 3 món gần nhất khi AI nhận sai.
 */
@Composable
private fun RecognizedItemRow(
    item: com.calai.app.data.remote.dto.FoodItemRecognitionDto,
    onPickAlternative: (com.calai.app.data.remote.dto.FoodAlternativeDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isEstimate) {
                        Surface(
                            color = VividOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Ước tính",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryStrong
                            )
                        }
                    }
                }
                Text(
                    item.servingSize.orEmpty(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${item.calories.toInt()} kcal",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (item.isEstimate) {
            Text(
                "Món chưa có trong kho nên số liệu chỉ là ước tính, độ chính xác thấp hơn.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (item.alternatives.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Nhận sai? Chọn món khác:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.alternatives.forEach { alt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onPickAlternative(alt) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "${alt.name} · ${alt.calories.toInt()} kcal",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
