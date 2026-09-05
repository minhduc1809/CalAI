package com.calai.app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CreateMealRequest
import com.calai.app.data.remote.dto.FoodRecognitionResultDto
import com.calai.app.domain.repository.CalAIRepository
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

data class CameraScanUiState(
    val selectedImageUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val result: FoodRecognitionResultDto? = null,
    val mealType: String = "LUNCH", // BREAKFAST, LUNCH, DINNER, SNACK
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

    fun onMealTypeSelect(type: String) {
        _uiState.value = _uiState.value.copy(mealType = type)
    }

    fun onImageCapturedOrSelected(uri: Uri, context: Context) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            isAnalyzing = true,
            result = null,
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
                        errorMessage = error.message ?: "Không thể phân tích ảnh"
                    )
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
            val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .format(Date())

            val items = if (result.items.isNotEmpty()) {
                result.items.map { item ->
                    CreateMealItemDto(
                        name = item.name,
                        servingSize = item.servingSize ?: result.servingSize,
                        quantity = 1.0,
                        calories = item.calories,
                        protein = item.protein,
                        carb = item.carb,
                        fat = item.fat,
                        source = "ai_vision"
                    )
                }
            } else {
                listOf(
                    CreateMealItemDto(
                        name = result.foodName,
                        servingSize = result.servingSize,
                        quantity = 1.0,
                        calories = result.totalCalories,
                        protein = result.totalProtein,
                        carb = result.totalCarb,
                        fat = result.totalFat,
                        source = "ai_vision"
                    )
                )
            }

            val request = CreateMealRequest(
                mealType = currentState.mealType,
                date = isoDate,
                items = items
            )

            repository.createRemoteMeal(request).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaveSuccess = true
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = err.message ?: "Không thể lưu bữa ăn"
                )
            }
        }
    }

    fun resetScan() {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            isAnalyzing = false,
            result = null,
            errorMessage = null,
            isSaveSuccess = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
