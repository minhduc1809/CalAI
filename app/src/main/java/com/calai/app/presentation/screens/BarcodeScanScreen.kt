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

                uiState.notFound -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                        Text("Không tìm thấy sản phẩm với mã vạch này", color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                        Button(
                            onClick = { viewModel.resetScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
                        ) {
                            Text("Quét lại")
                        }
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
    // Zoom mặc định để vùng khung quét chiếm phần lớn khung hình phân tích — mã vạch
    // decode nhanh hơn nhiều so với để camera quét nguyên khung hình rộng (gốc của defect
    // "phải căn rất lâu mới quét được" — mã vạch quá nhỏ trong ảnh gửi cho ML Kit).
    val roiZoomRatio = 0.35f
    var camera by remember { mutableStateOf<Camera?>(null) }
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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
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
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                )
                val analyzerExecutor = Executors.newSingleThreadExecutor()

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(view.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.firstOrNull()?.rawValue?.let(onDetected)
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
                        boundCamera.cameraControl.setLinearZoom(roiZoomRatio)
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
                tint = if (isTorchOn) VividOrange else TextWhite
            )
        }

        Text(
            "Đưa mã vạch sản phẩm vào khung để quét — chạm để lấy nét",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        )

        if (isLookingUp) {
            CircularProgressIndicator(color = VividOrange, modifier = Modifier.align(Alignment.Center))
        }
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
                            color = if (isSelected) TextWhite else MaterialTheme.colorScheme.onSurfaceVariant,
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

        Button(
            onClick = { viewModel.saveScannedFood() },
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
                Text("Lưu Vào Nhật Ký", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextWhite)
            }
        }

        TextButton(onClick = { viewModel.resetScan() }, modifier = Modifier.fillMaxWidth()) {
            Text("Quét sản phẩm khác", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
