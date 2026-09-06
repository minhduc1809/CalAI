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

/** Tổng số bước của Onboarding Wizard (chưa tính bước Hoàn tất). */
const val ONBOARDING_STEP_COUNT = 5

data class OnboardingUiState(
    val currentStep: Int = 0, // 0..ONBOARDING_STEP_COUNT-1, sau đó sang màn Hoàn tất
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedProfile: UserProfileDto? = null,

    // Bước 1 — User Profile
    val gender: String? = null, // MALE, FEMALE
    val dateOfBirth: String = "", // YYYY-MM-DD

    // Bước 2 — Body Metrics
    val heightCm: String = "",
    val weightKg: String = "",
    val bodyFatPercent: String = "", // tuỳ chọn

    // Bước 3 — Activity Info
    val activityLevel: String = "MODERATELY_ACTIVE",

    // Bước 4 — Goal Selection
    val goal: String = "MAINTAIN",
    val targetWeightKg: String = "",
    val weightRateKgPerWeek: Float = 0.5f,

    // Bước 5 — Program Setup
    val macroStyle: String = "BALANCED"
) {
    /** Từng bước cho phép Next hay chưa (validate tối thiểu). */
    fun canProceedFromCurrentStep(): Boolean = when (currentStep) {
        0 -> gender != null && dateOfBirth.isNotBlank()
        1 -> heightCm.toFloatOrNull() != null && weightKg.toFloatOrNull() != null
        2 -> true
        3 -> goal == "MAINTAIN" || targetWeightKg.toFloatOrNull() != null
        4 -> true
        else -> true
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectGender(gender: String) = _uiState.update { it.copy(gender = gender, errorMessage = null) }
    fun setDateOfBirth(value: String) = _uiState.update { it.copy(dateOfBirth = value, errorMessage = null) }
    fun setHeightCm(value: String) = _uiState.update { it.copy(heightCm = value.filter { c -> c.isDigit() }, errorMessage = null) }
    fun setWeightKg(value: String) = _uiState.update { it.copy(weightKg = value.filter { c -> c.isDigit() || c == '.' }, errorMessage = null) }
    fun setBodyFatPercent(value: String) = _uiState.update { it.copy(bodyFatPercent = value.filter { c -> c.isDigit() || c == '.' }) }
    fun selectActivityLevel(level: String) = _uiState.update { it.copy(activityLevel = level) }
    fun selectGoal(goal: String) = _uiState.update { it.copy(goal = goal, errorMessage = null) }
    fun setTargetWeightKg(value: String) = _uiState.update { it.copy(targetWeightKg = value.filter { c -> c.isDigit() || c == '.' }, errorMessage = null) }
    fun selectRate(rate: Float) = _uiState.update { it.copy(weightRateKgPerWeek = rate) }
    fun selectMacroStyle(style: String) = _uiState.update { it.copy(macroStyle = style) }

    fun nextStep() {
        val state = _uiState.value
        if (!state.canProceedFromCurrentStep()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đầy đủ thông tin trước khi tiếp tục") }
            return
        }
        if (state.currentStep >= ONBOARDING_STEP_COUNT - 1) {
            submit()
        } else {
            _uiState.update { it.copy(currentStep = it.currentStep + 1, errorMessage = null) }
        }
    }

    fun previousStep() {
        _uiState.update {
            if (it.currentStep > 0) it.copy(currentStep = it.currentStep - 1, errorMessage = null) else it
        }
    }

    private fun submit() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = UpdateProfileRequest(
                gender = state.gender,
                dateOfBirth = state.dateOfBirth.ifBlank { null },
                heightCm = state.heightCm.toFloatOrNull(),
                weightKg = state.weightKg.toFloatOrNull(),
                activityLevel = state.activityLevel,
                goal = state.goal,
                targetWeightKg = if (state.goal == "MAINTAIN") null else state.targetWeightKg.toFloatOrNull(),
                weightRateKgPerWeek = state.weightRateKgPerWeek,
                bodyFatPercent = state.bodyFatPercent.toFloatOrNull(),
                macroStyle = state.macroStyle
            )
            repository.updateProfile(request).onSuccess { profile ->
                _uiState.update { it.copy(isSaving = false, isCompleted = true, savedProfile = profile) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Không thể lưu hồ sơ, vui lòng thử lại") }
            }
        }
    }
}
