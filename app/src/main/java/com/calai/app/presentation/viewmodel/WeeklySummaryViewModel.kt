package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.WeeklySummaryDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeeklySummaryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val summary: WeeklySummaryDto? = null,
    val errorMessage: String? = null,
    /** Lỗi khi làm mới nhưng vẫn còn tổng kết cũ để xem. */
    val refreshError: String? = null
)

/** Tổng kết tuần (BR-11.7): kỳ dữ liệu trùng kỳ Check-in, backend tự cache theo tuần. */
@HiltViewModel
class WeeklySummaryViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklySummaryUiState())
    val uiState: StateFlow<WeeklySummaryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.fetchWeeklySummary().fold(
                onSuccess = { s -> _uiState.update { it.copy(isLoading = false, summary = s) } },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Không thể tải tổng kết tuần") }
                }
            )
        }
    }

    /** Premium: tạo lại tổng kết (bỏ qua cache). Free nhận lỗi từ máy chủ và giữ nguyên bản đang xem. */
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, refreshError = null) }
        viewModelScope.launch {
            repository.regenerateWeeklySummary().fold(
                onSuccess = { s -> _uiState.update { it.copy(isRefreshing = false, summary = s) } },
                onFailure = { e ->
                    _uiState.update { it.copy(isRefreshing = false, refreshError = e.message ?: "Không thể làm mới tổng kết") }
                }
            )
        }
    }
}
