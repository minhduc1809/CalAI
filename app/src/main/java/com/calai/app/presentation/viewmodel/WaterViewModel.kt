package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.WaterTodayDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** null = chưa tải được dữ liệu thật từ server (card sẽ hiện trạng thái tải/lỗi, không hiện số giả). */
@HiltViewModel
class WaterViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _water = MutableStateFlow<WaterTodayDto?>(null)
    val water: StateFlow<WaterTodayDto?> = _water.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { refresh() }

    fun refresh() = apply { repository.getWaterToday() }

    fun addGlass() = apply { repository.addWaterGlass() }
    fun removeGlass() = apply { repository.undoWaterGlass() }

    private fun apply(call: suspend () -> Result<WaterTodayDto>) {
        viewModelScope.launch {
            call().onSuccess { _water.value = it; _error.value = null }
                .onFailure { _error.value = it.message }
        }
    }
}
