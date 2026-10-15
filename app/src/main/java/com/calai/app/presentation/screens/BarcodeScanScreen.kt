package com.calai.app.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import android.util.Size
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.components.SelectionPill
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.BarcodeScanUiState
import com.calai.app.presentation.viewmodel.BarcodeScanViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Quét mã vạch sản phẩm bằng camera (CameraX + ML Kit Barcode Scanning), tra cứu dinh dưỡng
 * qua OpenFoodFacts (GET recommendations/barcode/:code), rồi lưu thẳng vào nhật ký như CameraScanScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanScreen(
    onBack: () -> Unit,
    viewModel: BarcodeScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) onBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Quét Mã Vạch", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                !hasCameraPermission -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                        Text("Cần quyền Camera để quét mã vạch", color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                        ) {
                            Text("Cấp quyền")
                        }
                    }
                }

                uiState.lookupError != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                        Text(uiState.lookupError!!, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                        if (!uiState.lookupQuotaExceeded) {
                            Button(onClick = { viewModel.resetScan() }, colors = ButtonDefaults.buttonColors(containerColor = VividOrange)) { Text("Quét lại") }
                        }
                    }
                }

                uiState.notFound && uiState.showCreateForm -> {
                    BarcodeCreateForm(
                        code = uiState.scannedCode.orEmpty(),
                        isCreating = uiState.isCreating,
                        error = uiState.errorMessage,
                        onCancel = viewModel::closeCreateForm,
                        onCreate = viewModel::createFoodForBarcode
                    )
                }

                uiState.notFound -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                        Text("Không tìm thấy sản phẩm. Tạo món mới với mã vạch này?", color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                        Button(
                            onClick = { viewModel.openCreateForm() },
                            colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                        ) {
                            Text("Tạo món với mã vạch này")
                        }
                        TextButton(onClick = { viewModel.resetScan() }) { Text("Quét lại") }
                    }
                }

                uiState.product != null -> {
                    BarcodeResultCard(uiState = uiState, viewModel = viewModel)
                }

                else -> {
                    BarcodeCameraPreview(
                        isLookingUp = uiState.isLookingUp,
                        onDetected = viewModel::onBarcodeDetected,
                        lifecycleOwner = lifecycleOwner
                    )
                }
            }
        }
    }
}

@Composable
private fun BarcodeCameraPreview(
    isLookingUp: Boolean,
    onDetected: (String) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    var camera by remember { mutableStateOf<Camera?>(null) }
    var zoomRatio by remember { mutableStateOf(1f) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var showManual by remember { mutableStateOf(false) }
    var manualCode by remember { mutableStateOf("") }
    val pickImage = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) scanBarcodeFromImage(context, uri, onDetected) {
            android.widget.Toast.makeText(context, "Không đọc được mã vạch trong ảnh. Hãy chọn ảnh rõ nét, chụp thẳng và đủ sáng.", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    var isTorchOn by remember { mutableStateOf(false) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // Animation pulsing cho khung quét trong lúc đang cố dò mã — trước đây khung tĩnh
    // hoàn toàn khiến người dùng không biết camera có đang hoạt động hay không.
    val infiniteTransition = rememberInfiniteTransition(label = "barcode-scan-pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLineOffset"
    )

    // Lấy nét lại giữa khung mỗi 2 giây: điện thoại hay "ngủ" lấy nét khi mã vạch đứng yên ở khoảng cách gần
    LaunchedEffect(camera) {
        val cam = camera ?: return@LaunchedEffect
        while (true) {
            val p = SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(0.5f, 0.5f)
            cam.cameraControl.startFocusAndMetering(
                FocusMeteringAction.Builder(p, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS).build()
            )
            kotlinx.coroutines.delay(2000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Chụm hai ngón để zoom (1×–8×)
                    detectTransformGestures { _, _, zoom, _ ->
                        val cam = camera ?: return@detectTransformGestures
                        if (zoom == 1f) return@detectTransformGestures
                        val st = cam.cameraInfo.zoomState.value
                        val next = (zoomRatio * zoom).coerceIn(st?.minZoomRatio ?: 1f, minOf(st?.maxZoomRatio ?: 1f, 8f))
                        zoomRatio = next
                        cam.cameraControl.setZoomRatio(next)
                    }
                }
                .pointerInput(Unit) {
                    // Tap-to-focus: khắc phục auto-focus mặc định lấy nét chậm ở khoảng
                    // cách gần (10-15cm), đúng khoảng cách quét mã vạch thực tế.
                    detectTapGestures { offset ->
                        val view = previewView ?: return@detectTapGestures
                        val cam = camera ?: return@detectTapGestures
                        val factory = SurfaceOrientedMeteringPointFactory(
                            view.width.toFloat(),
                            view.height.toFloat()
                        )
                        val point = factory.createPoint(offset.x, offset.y)
                        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        cam.cameraControl.startFocusAndMetering(action)
                    }
                },
            factory = { ctx ->
                val view = PreviewView(ctx)
                previewView = view
                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(SCAN_FORMATS)
                        .build()
                )
                val analyzerExecutor = Executors.newSingleThreadExecutor()

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(view.surfaceProvider)
                    }
                    // Mặc định CameraX phân tích ở ~640×480 nên vạch mã bị mờ; yêu cầu Full HD để đọc nét hơn
                    val analysis = ImageAnalysis.Builder()
                        .setResolutionSelector(
                            ResolutionSelector.Builder()
                                .setResolutionStrategy(ResolutionStrategy(Size(1920, 1080), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                                .build()
                        )
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.firstNotNullOfOrNull { it.rawValue ?: it.displayValue }?.let(onDetected)
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }
                    try {
                        cameraProvider.unbindAll()
                        val boundCamera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                        // Zoom vào vùng trung tâm (khớp khung quét hiển thị) + lấy nét gần
                        // ngay khi mở camera, không đợi người dùng phải tự tap.
                        val zs = boundCamera.cameraInfo.zoomState.value
                        val start = zoomRatio.coerceIn(zs?.minZoomRatio ?: 1f, minOf(zs?.maxZoomRatio ?: 1f, 8f))
                        zoomRatio = start
                        boundCamera.cameraControl.setZoomRatio(start)
                        val centerFactory = SurfaceOrientedMeteringPointFactory(1f, 1f)
                        val centerPoint = centerFactory.createPoint(0.5f, 0.5f)
                        boundCamera.cameraControl.startFocusAndMetering(
                            FocusMeteringAction.Builder(centerPoint, FocusMeteringAction.FLAG_AF).build()
                        )
                        camera = boundCamera
                    } catch (_: Exception) {
                        // Camera đã unbind (màn hình bị rời khỏi lifecycle) — bỏ qua an toàn.
                    }
                }, ContextCompat.getMainExecutor(ctx))

                view
            }
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp, 150.dp)
                .border(2.dp, VividOrange.copy(alpha = pulseAlpha), RoundedCornerShape(16.dp))
        ) {
            // Vạch quét chạy dọc trong lúc đang dò mã — báo hiệu camera đang hoạt động,
            // trước đây không có gì khiến người dùng tưởng app bị đứng.
            if (!isLookingUp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .offset(y = (scanLineOffset * 150).dp)
                        .height(2.dp)
                        .background(VividOrange.copy(alpha = 0.9f))
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BarcodeToolButton(Icons.Default.Image, "Chọn ảnh") { pickImage.launch("image/*") }
            BarcodeToolButton(Icons.Default.Keyboard, "Nhập mã") { showManual = true }
        }

        IconButton(
            onClick = {
                val cam = camera ?: return@IconButton
                isTorchOn = !isTorchOn
                cam.cameraControl.enableTorch(isTorchOn)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Bật/tắt đèn flash",
                tint = if (isTorchOn) VividOrange else MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(1f, 2f, 3f, 5f).forEach { z ->
                val active = kotlin.math.abs(zoomRatio - z) < 0.2f
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (active) VividOrange else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable {
                            val cam = camera ?: return@clickable
                            val st = cam.cameraInfo.zoomState.value
                            val v = z.coerceIn(st?.minZoomRatio ?: 1f, minOf(st?.maxZoomRatio ?: 1f, 8f))
                            zoomRatio = v
                            cam.cameraControl.setZoomRatio(v)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("${z.toInt()}×", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (active) OnPrimary else MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Text(
            "Đưa mã vạch vào khung — chụm hai ngón hoặc bấm 1×–5× để phóng to",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        if (isLookingUp) {
            CircularProgressIndicator(color = PrimaryStrong, modifier = Modifier.align(Alignment.Center))
        }
    }

    if (showManual) {
        com.calai.app.presentation.components.NwAlertDialog(
            onDismissRequest = { showManual = false },
            title = { Text("Nhập mã vạch") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = manualCode,
                    onValueChange = { v -> manualCode = v.filter { it.isLetterOrDigit() }.take(20) },
                    label = { Text("Dãy số dưới mã vạch") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    enabled = manualCode.length >= 6,
                    onClick = { showManual = false; onDetected(manualCode); manualCode = "" }
                ) { Text("Tra cứu", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showManual = false }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun BarcodeResultCard(uiState: BarcodeScanUiState, viewModel: BarcodeScanViewModel) {
    val product = uiState.product ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                Column {
                    Text(product.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${(product.calories * uiState.quantity).toInt()} kcal · ${product.servingSize ?: "100g"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Số lượng khẩu phần", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.5f, 1f, 1.5f, 2f).forEach { mult ->
                    val isSelected = uiState.quantity == mult
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) VividOrange else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.setQuantity(mult) }
                            .padding(vertical = 10.dp),
                    ) {
                        Text(
                            "${mult}x",
                            color = if (isSelected) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Thêm vào bữa nào?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("BREAKFAST" to "Sáng", "LUNCH" to "Trưa", "DINNER" to "Tối", "SNACK" to "Phụ").forEach { (key, label) ->
                    SelectionPill(
                        label = label,
                        isSelected = uiState.mealType == key,
                        isDarkTheme = true,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onMealTypeSelect(key) }
                    )
                }
            }
        }

        uiState.errorMessage?.let {
            Text(it, color = CoralWarning, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.weight(1f))

        AppButton(
            text = "Lưu Vào Nhật Ký",
            onClick = { viewModel.saveScannedFood() },
            enabled = !uiState.isSaving,
            isLoading = uiState.isSaving,
            height = 52.dp,
            shape = RoundedCornerShape(16.dp)
        )

        TextButton(onClick = { viewModel.resetScan() }, modifier = Modifier.fillMaxWidth()) {
            Text("Quét sản phẩm khác", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@Composable
private fun BarcodeCreateForm(
    code: String,
    isCreating: Boolean,
    error: String?,
    onCancel: () -> Unit,
    onCreate: (String, Float, Float, Float, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val kcalValue = kcal.replace(',', '.').toFloatOrNull()
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Tạo món cho mã vạch $code", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("Số liệu cho một phần ăn. Lần sau quét mã này sẽ tìm thấy ngay.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.material3.OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        androidx.compose.material3.OutlinedTextField(value = kcal, onValueChange = { kcal = it }, label = { Text("Calo (kcal)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Triple("Đạm (g)", protein, { v: String -> protein = v }), Triple("Tinh bột (g)", carb, { v: String -> carb = v }), Triple("Béo (g)", fat, { v: String -> fat = v })).forEach { (label, value, set) ->
                androidx.compose.material3.OutlinedTextField(value = value, onValueChange = set, label = { Text(label, fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        Button(
            onClick = {
                onCreate(
                    name, kcalValue ?: 0f,
                    protein.replace(',', '.').toFloatOrNull() ?: 0f,
                    carb.replace(',', '.').toFloatOrNull() ?: 0f,
                    fat.replace(',', '.').toFloatOrNull() ?: 0f
                )
            },
            enabled = name.isNotBlank() && kcalValue != null && !isCreating,
            colors = ButtonDefaults.buttonColors(containerColor = VividOrange),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isCreating) "Đang tạo…" else "Tạo và dùng món này") }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Huỷ") }
    }
}


/** Chỉ dò các định dạng mã vạch sản phẩm/mã QR phổ biến: nhanh và ít nhầm hơn FORMAT_ALL_FORMATS. */
private const val SCAN_FORMATS = Barcode.FORMAT_EAN_13 or Barcode.FORMAT_EAN_8 or Barcode.FORMAT_UPC_A or
    Barcode.FORMAT_UPC_E or Barcode.FORMAT_CODE_128 or Barcode.FORMAT_CODE_39 or Barcode.FORMAT_CODE_93 or
    Barcode.FORMAT_ITF or Barcode.FORMAT_CODABAR or Barcode.FORMAT_QR_CODE or Barcode.FORMAT_DATA_MATRIX

@Composable
private fun BarcodeToolButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
        Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    }
}

/**
 * Đọc mã vạch từ một ảnh trong thư viện. Thử ảnh gốc, rồi xoay 90°/180°/270° (mã vạch chụp ngang/ngược),
 * và cuối cùng thử lại ảnh thu nhỏ (ảnh quá lớn đôi khi làm bộ dò bỏ sót).
 */
private fun scanBarcodeFromImage(context: android.content.Context, uri: android.net.Uri, onFound: (String) -> Unit, onNone: () -> Unit) {
    val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(SCAN_FORMATS).build())
    val bitmap = try {
        context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) }
    } catch (_: Exception) { null }
    if (bitmap == null) { onNone(); return }

    val attempts = mutableListOf<Pair<android.graphics.Bitmap, Int>>()
    listOf(0, 90, 180, 270).forEach { attempts += bitmap to it }
    val maxSide = maxOf(bitmap.width, bitmap.height)
    if (maxSide > 1600) {
        val k = 1600f / maxSide
        val small = android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * k).toInt(), (bitmap.height * k).toInt(), true)
        listOf(0, 90).forEach { attempts += small to it }
    }

    fun tryAt(i: Int) {
        if (i >= attempts.size) { onNone(); return }
        val (bmp, rot) = attempts[i]
        scanner.process(InputImage.fromBitmap(bmp, rot))
            .addOnSuccessListener { list ->
                val code = list.firstNotNullOfOrNull { it.rawValue ?: it.displayValue }
                if (code != null) onFound(code) else tryAt(i + 1)
            }
            .addOnFailureListener { tryAt(i + 1) }
    }
    tryAt(0)
}
