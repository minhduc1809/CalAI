package com.calai.app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CreateMealRequest
import com.calai.app.data.remote.dto.FoodRecognitionResultDto
import com.calai.app.data.remote.dto.MenuItemDto
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
    val errorMessage: String? = null
)

@HiltViewModel
class CameraScanViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraScanUiState())
    val uiState: StateFlow<CameraScanUiState> = _uiState.asStateFlow()

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
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
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
                        fat = item.fat.toFloat()
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

    fun saveMenuItemAsMeal(item: MenuItemDto) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val nowStr = dateFormat.format(Date())

                val request = CreateMealRequest(
                    mealType = currentState.mealType,
                    date = nowStr,
                    items = listOf(
                        CreateMealItemDto(
                            name = item.name,
                            servingSize = "1 phần",
                            servingAmount = 1.0f,
                            servingUnit = "PORTION",
                            quantity = 1.0f,
                            calories = item.estimatedCalories.toFloat(),
                            protein = item.protein.toFloat(),
                            carb = item.carbs.toFloat(),
                            fat = item.fat.toFloat()
                        )
                    )
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
                        errorMessage = "Lỗi khi lưu món ăn từ menu: ${err.message}"
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
