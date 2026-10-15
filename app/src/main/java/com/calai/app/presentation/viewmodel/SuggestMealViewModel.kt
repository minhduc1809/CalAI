package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.SuggestMealResponseDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuggestMealUiState(
    val loading: Boolean = false,
    val result: SuggestMealResponseDto? = null,
    val error: String? = null
)

/** Gợi ý món hợp phần calo/macro còn lại trong ngày (UG-AI-03). Chỉ gọi khi người dùng bấm, vì AI có hạn mức riêng. */
@HiltViewModel
class SuggestMealViewModel @Inject constructor(private val repository: CalAIRepository) : ViewModel() {
    private val _state = MutableStateFlow(SuggestMealUiState())
    val state: StateFlow<SuggestMealUiState> = _state.asStateFlow()

    fun load() {
        if (_state.value.loading) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            repository.fetchSuggestMeal()
                .onSuccess { r -> _state.update { SuggestMealUiState(result = r) } }
                .onFailure { e ->
                    _state.update { SuggestMealUiState(error = e.message ?: "AI tạm thời không khả dụng. Bạn có thể tìm món thủ công trong kho món.") }
                }
        }
    }
}
