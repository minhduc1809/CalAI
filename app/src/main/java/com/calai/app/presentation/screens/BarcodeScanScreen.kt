package com.calai.app.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Quét mã vạch sản phẩm bằng camera (CameraX + ML Kit Barcode Scanning), tra cứu dinh dưỡng
 * qua OpenFoodFacts (GET recommendations/barcode/:code), rồi lưu thẳng vào nhật ký như CameraScanScreen.
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.camera.core.ExperimentalGetImage::class)
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
        containerColor = ObsidianBackground,
        topBar = {
            TopAppBar(
                title = { Text("Quét Mã Vạch", fontWeight = FontWeight.Bold, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBackground)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ObsidianBackground)
        ) {
            when {
                !hasCameraPermission -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Text("Cần quyền Camera để quét mã vạch", color = TextWhite, textAlign = TextAlign.Center)
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
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Text("Không tìm thấy sản phẩm với mã vạch này", color = TextWhite, textAlign = TextAlign.Center)
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
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
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
                        it.setSurfaceProvider(previewView.surfaceProvider)
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
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (_: Exception) {
                        // Camera đã unbind (màn hình bị rời khỏi lifecycle) — bỏ qua an toàn.
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp, 150.dp)
                .border(2.dp, VividOrange, RoundedCornerShape(16.dp))
        )

        Text(
            "Đưa mã vạch sản phẩm vào khung để quét",
            color = TextWhite,
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
                .background(CharcoalSurface)
                .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
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
                    Text(product.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${(product.calories * uiState.quantity).toInt()} kcal · ${product.servingSize ?: "100g"}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Số lượng khẩu phần", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.5f, 1f, 1.5f, 2f).forEach { mult ->
                    val isSelected = uiState.quantity == mult
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) VividOrange else CharcoalDock)
                            .clickable { viewModel.setQuantity(mult) }
                            .padding(vertical = 10.dp),
                    ) {
                        Text(
                            "${mult}x",
                            color = if (isSelected) TextWhite else TextMuted,
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
            Text("Thêm vào bữa nào?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
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
                Text("Lưu Vào Nhật Ký", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        TextButton(onClick = { viewModel.resetScan() }, modifier = Modifier.fillMaxWidth()) {
            Text("Quét sản phẩm khác", color = TextMuted)
        }
    }
}
