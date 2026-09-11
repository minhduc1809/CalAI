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
    viewModel: CameraScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.onImageCapturedOrSelected(tempPhotoUri!!, context)
        }
    }

    val launchCamera = {
        val uri = createTempPictureUri(context)
        tempPhotoUri = uri
        cameraLauncher.launch(uri)
    }

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
                        color = TextWhite,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextWhite)
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
                                    outlineColor = if (isFood) TextWhite else TextMuted,
                                    accentColor = if (isFood) TextWhite else VividOrange
                                )
                                Text(
                                    text = "Đĩa Món Ăn",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFood) TextWhite else TextMuted
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
                                    outlineColor = if (isMenu) TextWhite else TextMuted,
                                    accentColor = if (isMenu) TextWhite else VividOrange
                                )
                                Text(
                                    text = "Thực Đơn (Menu)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMenu) TextWhite else TextMuted
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
                            color = TextWhite,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (uiState.selectedImageUri == null) {
                Spacer(modifier = Modifier.height(10.dp))

                // Badge hạn mức — dữ liệu thật từ ai/quota, bấm vào để mua thêm lượt
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
                        .clickable { viewModel.openPurchaseSheet() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (uiState.isQuotaExhausted) CrimsonError else VividOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when {
                                quota == null -> "Đang tải hạn mức chụp ảnh AI..."
                                uiState.isQuotaExhausted -> "Đã hết lượt chụp hôm nay — bấm để mua thêm"
                                else -> "Còn ${quota.totalRemaining} lượt chụp AI (${quota.freeRemaining} miễn phí hôm nay)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.isQuotaExhausted) CrimsonError else TextWhite
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
                                if (uiState.scanMode == ScanMode.FOOD) Icons.Default.CameraAlt else Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = VividOrange,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = if (uiState.scanMode == ScanMode.FOOD) "Đưa món ăn vào khung hình" else "Chụp toàn bộ thực đơn quán ăn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.scanMode == ScanMode.FOOD)
                                "Gemini AI sẽ nhận diện món ăn và tính toán calo chuẩn xác"
                            else
                                "AI sẽ bóc tách danh sách món và gợi ý món tối ưu nhất cho calo hôm nay của bạn",
                            fontSize = 12.sp,
                            color = TextMuted,
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
                    Button(
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
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chụp Ảnh", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
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
                            color = TextMuted
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
                        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = VividOrange)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (uiState.scanMode == ScanMode.FOOD) "Gemini AI đang phân tích món ăn..." else "AI đang đọc thực đơn quán ăn...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
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
                                    Text(food.foodName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text(food.servingSize, fontSize = 13.sp, color = TextMuted)
                                }
                                Surface(
                                    color = VividOrange.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "${food.totalCalories.toInt()} kcal",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = VividOrange,
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
                                        DuotoneTipIcon(size = 16.dp, outlineColor = TextMuted, accentColor = VividOrange)
                                        Text(
                                            text = food.healthTip,
                                            fontSize = 12.5.sp,
                                            color = TextMuted
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
                                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TextWhite else TextMuted)
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
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                                ) {
                                    Text("Quét Lại")
                                }

                                Button(
                                    onClick = { viewModel.saveRecognizedMeal() },
                                    enabled = !uiState.isSaving,
                                    modifier = Modifier.weight(1.5f).height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextWhite)
                                    } else {
                                        Text("Lưu Bữa Ăn", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
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
                                color = TextWhite
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
                                DuotoneSparkleIcon(size = 16.dp, outlineColor = TextWhite, accentColor = PastelLavender)
                                Text(
                                    text = menu.summaryAdvice,
                                    fontSize = 13.sp,
                                    color = TextWhite
                                )
                            }
                        }

                        Text(
                            text = "DANH SÁCH MÓN ĂN NHẬN DIỆN:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
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

                        if (uiState.selectedMenuItemIndices.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.saveSelectedMenuItems() },
                                enabled = !uiState.isSaving,
                                colors = ButtonDefaults.buttonColors(containerColor = VividOrange, contentColor = TextWhite),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(
                                    "Lưu ${uiState.selectedMenuItemIndices.size} Món Đã Chọn",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetState() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                        ) {
                            Text("Chụp Menu Khác")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (uiState.showPurchaseSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissPurchaseSheet() },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp).padding(bottom = 24.dp)) {
                Text(
                    "Mua Thêm Lượt Chụp Ảnh AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Lượt mua không bao giờ hết hạn, dùng sau khi hết 5 lượt miễn phí mỗi ngày",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.aiPackages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VividOrange)
                    }
                } else {
                    uiState.aiPackages.forEach { pkg ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                if (pkg.isPopular || pkg.bestValue) 1.5.dp else 1.dp,
                                if (pkg.isPopular || pkg.bestValue) VividOrange else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable(enabled = !uiState.isPurchasingCredits) {
                                    viewModel.purchaseAiCredits(pkg.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(pkg.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text("${pkg.credits} lượt chụp", fontSize = 12.sp, color = TextMuted)
                                }
                                Text(
                                    "${pkg.priceVnd}đ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividOrange
                                )
                            }
                        }
                    }
                }

                if (uiState.isPurchasingCredits) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VividOrange, modifier = Modifier.size(24.dp))
                    }
                }
            }
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
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else if (item.isRecommended) 1.5.dp else 1.dp,
            if (isSelected) MintJade else if (item.isRecommended) VividOrange else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSaving) { onToggleSelect() }
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
                        enabled = !isSaving,
                        colors = CheckboxDefaults.colors(checkedColor = MintJade)
                    )
                    Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        if (item.isRecommended) {
                            Surface(
                                color = VividOrange.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Gợi ý", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VividOrange, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
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
                        "${item.estimatedCalories} kcal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividOrange,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("P: ${item.protein}g", fontSize = 12.sp, color = PastelLavender, fontWeight = FontWeight.Medium)
                Text("C: ${item.carbs}g", fontSize = 12.sp, color = PastelButtercup, fontWeight = FontWeight.Medium)
                Text("F: ${item.fat}g", fontSize = 12.sp, color = PastelRose, fontWeight = FontWeight.Medium)
            }

            item.recommendationReason?.let { reason ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DuotoneCheckmarkIcon(size = 13.dp, outlineColor = PastelMint)
                    Text(reason, fontSize = 12.sp, color = TextMuted)
                }
            }

        }
    }
}

@Composable
private fun MacroTag(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

private fun createTempPictureUri(context: Context): Uri {
    val tempFile = File.createTempFile("camera_${System.currentTimeMillis()}", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}
