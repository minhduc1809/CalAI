package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.UpdateProfileRequest
import com.calai.app.data.remote.dto.UserProfileDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalSetupUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null,
    // Giá trị gốc từ server, dùng để phát hiện có thay đổi hay không (Goal Change cần xác nhận)
    val original: UserProfileDto? = null,
    // Giá trị đang chỉnh sửa trên màn hình
    val goal: String = "MAINTAIN",
    val targetWeightKg: String = "",
    val weightRateKgPerWeek: Float = 0.5f,
    val macroStyle: String = "BALANCED"
)

@HiltViewModel
class GoalSetupViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalSetupUiState())
    val uiState: StateFlow<GoalSetupUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.fetchRemoteProfile().onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        original = profile,
                        goal = profile.goal ?: "MAINTAIN",
                        targetWeightKg = profile.targetWeightKg?.toString() ?: "",
                        weightRateKgPerWeek = profile.weightRateKgPerWeek ?: 0.5f,
                        macroStyle = profile.macroStyle ?: "BALANCED"
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun selectGoal(goal: String) {
        _uiState.update { it.copy(goal = goal) }
    }

    fun setTargetWeight(value: String) {
        _uiState.update { it.copy(targetWeightKg = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun selectRate(rate: Float) {
        _uiState.update { it.copy(weightRateKgPerWeek = rate) }
    }

    fun selectMacroStyle(style: String) {
        _uiState.update { it.copy(macroStyle = style) }
    }

    /** True nếu người dùng đã đổi khác với dữ liệu gốc — dùng để quyết định có cần hộp thoại xác nhận hay không. */
    fun hasChanges(): Boolean {
        val state = _uiState.value
        val original = state.original ?: return true
        return state.goal != (original.goal ?: "MAINTAIN") ||
            state.targetWeightKg != (original.targetWeightKg?.toString() ?: "") ||
            state.weightRateKgPerWeek != (original.weightRateKgPerWeek ?: 0.5f) ||
            state.macroStyle != (original.macroStyle ?: "BALANCED")
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = UpdateProfileRequest(
                goal = state.goal,
                targetWeightKg = state.targetWeightKg.toFloatOrNull(),
                weightRateKgPerWeek = state.weightRateKgPerWeek,
                macroStyle = state.macroStyle
            )
            repository.updateProfile(request).onSuccess { updated ->
                _uiState.update { it.copy(isSaving = false, isSaveSuccess = true, original = updated) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Không thể lưu mục tiêu") }
            }
        }
    }
}
