package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.DailyNutritionSummaryData
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val dailySummary: DailyNutritionSummaryData? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val username = repository.getCurrentUsername() ?: "Người dùng"
        _uiState.value = _uiState.value.copy(username = username, isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.fetchDailySummary()
            result.onSuccess { summaryData ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dailySummary = summaryData,
                    errorMessage = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Không thể kết nối đến máy chủ"
                )
            }
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            val result = repository.deleteRemoteMeal(mealId)
            result.onSuccess {
                // Tải lại tổng hợp dinh dưỡng sau khi xóa
                loadData()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Không thể xóa bữa ăn"
                )
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }
}
