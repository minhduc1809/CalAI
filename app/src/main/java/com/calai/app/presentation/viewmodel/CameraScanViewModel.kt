package com.calai.app.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.AiQuotaDto
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CreateMealRequest
import com.calai.app.data.remote.dto.FoodAlternativeDto
import com.calai.app.data.remote.dto.FoodItemRecognitionDto
import com.calai.app.data.remote.dto.FoodRecognitionResultDto
import com.calai.app.data.remote.dto.ScanMenuResponseDto
import com.calai.app.domain.repository.CalAIRepository
import com.calai.app.domain.util.MealTimeHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** Kích thước tối đa (px) của cạnh dài ảnh sau khi resize trước khi upload lên server AI */
private const val MAX_UPLOAD_IMAGE_DIMENSION_PX = 1920

/** Chất lượng nén JPEG khi re-encode ảnh trước khi upload (0-100) */
private const val UPLOAD_JPEG_QUALITY = 90

enum class ScanMode {
    FOOD,       // Quét ảnh món ăn
    MENU        // Quét thực đơn quán ăn
}

data class CameraScanUiState(
    val scanMode: ScanMode = ScanMode.FOOD,
    val selectedImageUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val result: FoodRecognitionResultDto? = null,
    val menuResult: ScanMenuResponseDto? = null,
    val mealType: String = MealTimeHelper.detectMealType(),
    val detectedTimeStr: String? = null,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val aiQuota: AiQuotaDto? = null,
    val isQuotaExhausted: Boolean = false,
    val selectedMenuItemIndices: Set<Int> = emptySet()
)

@HiltViewModel
class CameraScanViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraScanUiState())
    val uiState: StateFlow<CameraScanUiState> = _uiState.asStateFlow()

    init {
        loadAiQuota()
    }

    fun loadAiQuota() {
        viewModelScope.launch {
            repository.fetchAiQuota().onSuccess { quota ->
                _uiState.value = _uiState.value.copy(
                    aiQuota = quota,
                    isQuotaExhausted = quota.totalRemaining <= 0
                )
            }
        }
    }

    fun setScanMode(mode: ScanMode) {
        _uiState.value = _uiState.value.copy(
            scanMode = mode,
            result = null,
            menuResult = null,
            errorMessage = null
        )
    }

    fun onMealTypeSelect(type: String) {
        _uiState.value = _uiState.value.copy(mealType = type)
    }

    fun onImageCapturedOrSelected(uri: Uri, context: Context) {
        if (_uiState.value.scanMode == ScanMode.FOOD && _uiState.value.isQuotaExhausted) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Bạn đã dùng hết lượt nhận diện món hôm nay. Lượt sẽ được làm mới vào ngày mai, hoặc mở Premium để có nhiều lượt hơn."
            )
            return
        }

        val (autoMealType, timeStr) = MealTimeHelper.detectMealTypeFromImage(context, uri)

        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            isAnalyzing = true,
            result = null,
            menuResult = null,
            mealType = autoMealType,
            detectedTimeStr = timeStr,
            errorMessage = null,
            isSaveSuccess = false
        )

        viewModelScope.launch {
            try {
                val tempFile = withContext(Dispatchers.IO) {
                    val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                    downscaleAndCompressToJpeg(context, uri, file)
                    file
                }

                if (_uiState.value.scanMode == ScanMode.FOOD) {
                    val scanResult = repository.recognizeFood(tempFile)
                    scanResult.onSuccess { data ->
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            result = data,
                            errorMessage = null
                        )
                        loadAiQuota()
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            errorMessage = error.message ?: "Không thể phân tích món ăn"
                        )
                    }
                } else {
                    // Chế độ quét MENU
                    val menuScanResult = repository.scanMenu(tempFile)
                    menuScanResult.onSuccess { data ->
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            menuResult = data,
                            errorMessage = null
                        )
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            errorMessage = error.message ?: "Không thể đọc menu thực đơn"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = "Lỗi khi xử lý tệp ảnh: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Người dùng chọn một món khác trong kho khi AI nhận sai (BR-11.1): thay món ở vị trí [index] bằng món kho
     * đã chọn, tính theo một khẩu phần chuẩn của kho, rồi cộng lại tổng. Món sau khi đổi là AI_MATCHED (số liệu kho).
     */
    fun swapRecognizedItem(index: Int, alternative: FoodAlternativeDto) {
        val result = _uiState.value.result ?: return
        if (index !in result.items.indices) return

        val replaced = FoodItemRecognitionDto(
            name = alternative.name,
            servingSize = alternative.servingSize,
            calories = alternative.calories,
            protein = alternative.protein,
            carb = alternative.carb,
            fat = alternative.fat,
            sourceType = "AI_MATCHED",
            sourceId = alternative.sourceId,
            isEstimate = false,
            confidence = 1.0,
            confidenceLevel = "HIGH",
            // Món cũ trở thành lựa chọn đổi lại, tránh người dùng đổi nhầm rồi không quay lại được
            alternatives = (result.items[index].alternatives.filter { it.sourceId != alternative.sourceId } +
                listOfNotNull(result.items[index].takeIf { it.sourceId != null }?.let { old ->
                    FoodAlternativeDto(
                        name = old.name,
                        servingSize = old.servingSize,
                        sourceId = old.sourceId,
                        calories = old.calories,
                        protein = old.protein,
                        carb = old.carb,
                        fat = old.fat
                    )
                })).take(3)
        )
        val newItems = result.items.toMutableList().also { it[index] = replaced }
        _uiState.value = _uiState.value.copy(
            result = result.copy(
                foodName = newItems.joinToString(" + ") { it.name },
                servingSize = if (newItems.size == 1) (newItems[0].servingSize ?: "") else "${newItems.size} món",
                totalCalories = newItems.sumOf { it.calories },
                totalProtein = newItems.sumOf { it.protein },
                totalCarb = newItems.sumOf { it.carb },
                totalFat = newItems.sumOf { it.fat },
                items = newItems
            )
        )
    }

    fun saveRecognizedMeal() {
        val result = _uiState.value.result ?: return
        val currentState = _uiState.value

        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val nowStr = dateFormat.format(Date())

                val items = result.items.map { item ->
                    CreateMealItemDto(
                        name = item.name,
                        servingSize = item.servingSize,
                        servingAmount = 1.0f,
                        servingUnit = "PORTION",
                        quantity = 1.0f,
                        calories = item.calories.toFloat(),
                        protein = item.protein.toFloat(),
                        carb = item.carb.toFloat(),
                        fat = item.fat.toFloat(),
                        source = "ai_vision",
                        // AI_MATCHED: số liệu từ kho món; AI_ESTIMATE: ước tính (gắn nhãn khi hiển thị lại)
                        sourceType = item.sourceType ?: "AI_ESTIMATE",
                        sourceId = item.sourceId
                    )
                }

                val request = CreateMealRequest(
                    mealType = currentState.mealType,
                    date = nowStr,
                    items = items
                )

                val saveResult = repository.createRemoteMeal(request)
                saveResult.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSaveSuccess = true
                    )
                }.onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Lỗi khi lưu bữa ăn: ${err.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Lỗi hệ thống: ${e.localizedMessage}"
                )
            }
        }
    }

    fun toggleMenuItemSelection(index: Int) {
        // Món chưa có số liệu dinh dưỡng không được chọn: lưu vào nhật ký sẽ ghi 0 kcal gây sai tổng ngày
        if (_uiState.value.menuResult?.items?.getOrNull(index)?.nutritionAvailable == false) return
        _uiState.value = _uiState.value.copy(
            selectedMenuItemIndices = _uiState.value.selectedMenuItemIndices.let { current ->
                if (index in current) current - index else current + index
            }
        )
    }

    fun saveSelectedMenuItems() {
        val menu = _uiState.value.menuResult ?: return
        val selectedItems = _uiState.value.selectedMenuItemIndices
            .sorted()
            .mapNotNull { index -> menu.items.getOrNull(index) }
        if (selectedItems.isEmpty()) return

        val currentState = _uiState.value
        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val nowStr = dateFormat.format(Date())

                val request = CreateMealRequest(
                    mealType = currentState.mealType,
                    date = nowStr,
                    items = selectedItems.map { item ->
                        CreateMealItemDto(
                            name = item.name,
                            servingSize = "1 phần",
                            servingAmount = 1.0f,
                            servingUnit = "PORTION",
                            quantity = 1.0f,
                            calories = item.estimatedCalories.toFloat(),
                            protein = item.protein.toFloat(),
                            carb = item.carbs.toFloat(),
                            fat = item.fat.toFloat(),
                            source = "ai_vision",
                            sourceType = item.sourceType ?: "AI_ESTIMATE",
                            sourceId = item.sourceId
                        )
                    }
                )

                val saveResult = repository.createRemoteMeal(request)
                saveResult.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSaveSuccess = true,
                        selectedMenuItemIndices = emptySet()
                    )
                }.onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Lỗi khi lưu các món đã chọn: ${err.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Lỗi: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CameraScanUiState(
            scanMode = _uiState.value.scanMode,
            mealType = MealTimeHelper.detectMealType()
        )
    }
}

/**
 * Giảm kích thước ảnh (tối đa ~[MAX_UPLOAD_IMAGE_DIMENSION_PX]px cạnh dài) và nén lại JPEG
 * (chất lượng [UPLOAD_JPEG_QUALITY]) trước khi upload, tránh gửi ảnh gốc chưa xử lý lên server AI.
 * Dùng `inJustDecodeBounds` để lấy kích thước ảnh trước, tránh cấp phát bitmap đầy đủ không cần thiết.
 */
private fun downscaleAndCompressToJpeg(context: Context, sourceUri: Uri, destFile: File) {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        BitmapFactory.decodeStream(input, null, boundsOptions)
    }

    val (originalWidth, originalHeight) = boundsOptions.outWidth to boundsOptions.outHeight
    var inSampleSize = 1
    if (originalWidth > 0 && originalHeight > 0) {
        val longestSide = maxOf(originalWidth, originalHeight)
        while (longestSide / inSampleSize > MAX_UPLOAD_IMAGE_DIMENSION_PX * 2) {
            inSampleSize *= 2
        }
    }

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = inSampleSize }
    val decodedBitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
        BitmapFactory.decodeStream(input, null, decodeOptions)
    } ?: return

    val bitmapToUpload = decodedBitmap.let { bitmap ->
        val longestSide = maxOf(bitmap.width, bitmap.height)
        if (longestSide <= MAX_UPLOAD_IMAGE_DIMENSION_PX) {
            bitmap
        } else {
            val scale = MAX_UPLOAD_IMAGE_DIMENSION_PX.toFloat() / longestSide
            val targetWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
            val targetHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            if (scaled !== bitmap) bitmap.recycle()
            scaled
        }
    }

    FileOutputStream(destFile).use { output ->
        bitmapToUpload.compress(Bitmap.CompressFormat.JPEG, UPLOAD_JPEG_QUALITY, output)
    }
    bitmapToUpload.recycle()
}
