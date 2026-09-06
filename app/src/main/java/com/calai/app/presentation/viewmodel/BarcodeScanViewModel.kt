package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.BarcodeProductDto
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CreateMealRequest
import com.calai.app.domain.repository.CalAIRepository
import com.calai.app.domain.util.MealTimeHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class BarcodeScanUiState(
    val isLookingUp: Boolean = false,
    val product: BarcodeProductDto? = null,
    val notFound: Boolean = false,
    val quantity: Float = 1f,
    val mealType: String = MealTimeHelper.detectMealType(),
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BarcodeScanViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeScanUiState())
    val uiState: StateFlow<BarcodeScanUiState> = _uiState.asStateFlow()

    /** Gọi mỗi khi camera phát hiện 1 mã vạch — bỏ qua nếu đang tra cứu hoặc đã có kết quả. */
    fun onBarcodeDetected(code: String) {
        val state = _uiState.value
        if (state.isLookingUp || state.product != null || state.notFound) return

        _uiState.update { it.copy(isLookingUp = true) }
        viewModelScope.launch {
            repository.lookupBarcode(code).onSuccess { product ->
                _uiState.update {
                    it.copy(isLookingUp = false, product = product, notFound = product == null)
                }
            }.onFailure {
                _uiState.update { it.copy(isLookingUp = false, notFound = true) }
            }
        }
    }

    fun setQuantity(value: Float) {
        _uiState.update { it.copy(quantity = value) }
    }

    fun onMealTypeSelect(type: String) {
        _uiState.update { it.copy(mealType = type) }
    }

    fun resetScan() {
        _uiState.update {
            it.copy(
                isLookingUp = false,
                product = null,
                notFound = false,
                quantity = 1f,
                errorMessage = null
            )
        }
    }

    fun saveScannedFood() {
        val state = _uiState.value
        val product = state.product ?: return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
            val request = CreateMealRequest(
                mealType = state.mealType,
                date = isoDate,
                items = listOf(
                    CreateMealItemDto(
                        name = product.name,
                        servingSize = product.servingSize,
                        servingAmount = product.servingAmount,
                        servingUnit = product.servingUnit,
                        quantity = state.quantity,
                        calories = product.calories,
                        protein = product.protein,
                        carb = product.carb,
                        fat = product.fat,
                        source = "barcode"
                    )
                )
            )
            repository.createRemoteMeal(request).onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Không thể lưu bữa ăn") }
            }
        }
    }
}
