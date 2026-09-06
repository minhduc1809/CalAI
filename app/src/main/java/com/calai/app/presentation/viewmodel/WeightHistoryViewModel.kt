package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.WeightLogResponseDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightHistoryUiState(
    val isLoading: Boolean = true,
    val logs: List<WeightLogResponseDto> = emptyList(),
    val errorMessage: String? = null,
    // Bản ghi đang được sửa trên dialog, null nghĩa là không có dialog nào đang mở
    val editingLog: WeightLogResponseDto? = null,
    val editWeightText: String = "",
    val editNoteText: String = "",
    val isSaving: Boolean = false,
    val pendingDeleteLog: WeightLogResponseDto? = null
)

@HiltViewModel
class WeightHistoryViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightHistoryUiState())
    val uiState: StateFlow<WeightHistoryUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.fetchRemoteWeightLogs(limit = 200).onSuccess { logs ->
                _uiState.update { it.copy(isLoading = false, logs = logs.sortedByDescending { l -> l.date }) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun startEdit(log: WeightLogResponseDto) {
        _uiState.update {
            it.copy(editingLog = log, editWeightText = log.weightKg.toString(), editNoteText = log.note ?: "")
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingLog = null, editWeightText = "", editNoteText = "") }
    }

    fun setEditWeight(value: String) {
        _uiState.update { it.copy(editWeightText = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun setEditNote(value: String) {
        _uiState.update { it.copy(editNoteText = value) }
    }

    fun confirmEdit() {
        val state = _uiState.value
        val log = state.editingLog ?: return
        val newWeight = state.editWeightText.toFloatOrNull() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.updateRemoteWeightLog(
                logId = log.id,
                weightKg = newWeight,
                note = state.editNoteText.ifBlank { null }
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, editingLog = null, editWeightText = "", editNoteText = "") }
                loadLogs()
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Không thể cập nhật bản ghi") }
            }
        }
    }

    fun requestDelete(log: WeightLogResponseDto) {
        _uiState.update { it.copy(pendingDeleteLog = log) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDeleteLog = null) }
    }

    fun confirmDelete() {
        val log = _uiState.value.pendingDeleteLog ?: return
        viewModelScope.launch {
            repository.deleteRemoteWeightLog(log.id).onSuccess {
                _uiState.update { it.copy(pendingDeleteLog = null, logs = it.logs.filter { l -> l.id != log.id }) }
            }.onFailure { e ->
                _uiState.update { it.copy(pendingDeleteLog = null, errorMessage = e.message ?: "Không thể xóa bản ghi") }
            }
        }
    }
}
