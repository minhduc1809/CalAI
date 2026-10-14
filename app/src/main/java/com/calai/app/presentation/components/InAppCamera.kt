package com.calai.app.presentation.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Camera chụp món trong app (CameraX): ảnh độ phân giải cao ưu tiên chất lượng, chạm để lấy nét, chụm hai ngón hoặc
 * nút 1×/2×/3× để zoom, đèn pin. Thay cho camera hệ thống vốn cho ảnh mờ ở một số máy và không zoom được.
 */
@Composable
fun InAppCamera(
    onCaptured: (Uri) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var torchOn by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setJpegQuality(95)
            .build()
    }

    fun applyZoom(target: Float) {
        val cam = camera ?: return
        val state = cam.cameraInfo.zoomState.value
        val min = state?.minZoomRatio ?: 1f
        val max = minOf(state?.maxZoomRatio ?: 1f, 8f)
        val clamped = target.coerceIn(min, max)
        zoomRatio = clamped
        cam.cameraControl.setZoomRatio(clamped)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val view = previewView ?: return@detectTapGestures
                        val cam = camera ?: return@detectTapGestures
                        val point = SurfaceOrientedMeteringPointFactory(view.width.toFloat(), view.height.toFloat())
                            .createPoint(offset.x, offset.y)
                        cam.cameraControl.startFocusAndMetering(
                            FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                .build()
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ -> if (zoom != 1f) applyZoom(zoomRatio * zoom) }
                },
            factory = { ctx ->
                val view = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                previewView = view
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                    try {
                        provider.unbindAll()
                        val bound = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                        // Lấy nét giữa khung ngay khi mở để ảnh đầu tiên không bị mờ.
                        val center = SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(0.5f, 0.5f)
                        bound.cameraControl.startFocusAndMetering(FocusMeteringAction.Builder(center, FocusMeteringAction.FLAG_AF).build())
                        camera = bound
                    } catch (_: Exception) {
                        // Camera đã bị huỷ liên kết khi rời màn hình: bỏ qua.
                    }
                }, ContextCompat.getMainExecutor(ctx))
                view
            },
        )

        Row(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RoundIconButton(onClose) { Icon(Icons.Default.Close, contentDescription = "Đóng camera", tint = Color.White) }
            Box(modifier = Modifier.weight(1f))
            RoundIconButton({
                val cam = camera ?: return@RoundIconButton
                torchOn = !torchOn
                cam.cameraControl.enableTorch(torchOn)
            }) {
                Icon(if (torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff, contentDescription = "Đèn pin", tint = Color.White)
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.Black.copy(alpha = 0.45f)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    listOf(1f, 2f, 3f).forEach { z ->
                        val active = kotlin.math.abs(zoomRatio - z) < 0.15f
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.Transparent)
                                .pointerInput(z) { detectTapGestures { applyZoom(z) } },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${z.toInt()}×", color = if (active) Color.Black else Color.White, fontSize = 13.sp)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(BorderStroke(4.dp, Color.White), CircleShape)
                        .padding(7.dp)
                        .clip(CircleShape)
                        .background(if (capturing) Color.Gray else Color.White)
                        .pointerInput(capturing) {
                            detectTapGestures {
                                if (capturing) return@detectTapGestures
                                capturing = true
                                takePhoto(context, imageCapture, onCaptured) { capturing = false }
                            }
                        },
                )
                Box(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)),
    ) { content() }
}

private fun takePhoto(context: Context, capture: ImageCapture, onCaptured: (Uri) -> Unit, onDone: () -> Unit) {
    val file = File.createTempFile("scan_cam_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
    val options = ImageCapture.OutputFileOptions.Builder(file).build()
    capture.takePicture(options, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            onDone()
            onCaptured(Uri.fromFile(file))
        }

        override fun onError(exception: ImageCaptureException) {
            onDone()
            Toast.makeText(context, "Không chụp được ảnh, hãy thử lại", Toast.LENGTH_SHORT).show()
        }
    })
}
