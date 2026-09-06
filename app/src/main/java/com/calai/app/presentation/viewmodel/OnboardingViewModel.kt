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

/** Tổng số bước của Onboarding (5 bước). */
const val ONBOARDING_STEP_COUNT = 5

data class OnboardingUiState(
    val currentStep: Int = 0, // 0: WELCOME, 1: HEIGHT, 2: WEIGHT, 3: BIRTH_DATE, 4: GOAL
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedProfile: UserProfileDto? = null,

    // 1. HEIGHT (cm)
    val heightCm: Float = 170f,

    // 2. WEIGHT (kg)
    val weightKg: Float = 65f,

    // 3. BIRTH_DATE (day, month, year)
    val birthDay: Int = 15,
    val birthMonth: Int = 5,
    val birthYear: Int = 1998,

    // 4. GOAL: LOSE_WEIGHT, MAINTAIN, GAIN_WEIGHT
    val goal: String = "MAINTAIN",

    // Mặc định hỗ trợ tính toán BMR/TDEE
    val gender: String = "MALE",
    val activityLevel: String = "MODERATELY_ACTIVE"
) {
    val dateOfBirth: String
        get() = "%04d-%02d-%02d".format(birthYear, birthMonth, birthDay)

    /** Kiểm tra xem bước hiện tại đã hợp lệ để tiếp tục hay chưa. */
    fun canProceed(step: Int): Boolean = when (step) {
        0 -> true // Welcome
        1 -> heightCm in 50f..250f
        2 -> weightKg in 20f..300f
        3 -> birthYear in 1920..2020 && birthMonth in 1..12 && birthDay in 1..31
        4 -> goal.isNotBlank()
        else -> true
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setHeightCm(value: Float) {
        _uiState.update { it.copy(heightCm = value, errorMessage = null) }
    }

    fun setWeightKg(value: Float) {
        _uiState.update { it.copy(weightKg = value, errorMessage = null) }
    }

    fun setDateOfBirth(day: Int, month: Int, year: Int) {
        _uiState.update {
            it.copy(
                birthDay = day,
                birthMonth = month,
                birthYear = year,
                errorMessage = null
            )
        }
    }

    fun selectGoal(goal: String) {
        _uiState.update { it.copy(goal = goal, errorMessage = null) }
    }

    fun setCurrentStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(0, ONBOARDING_STEP_COUNT - 1)) }
    }

    /**
     * Gửi toàn bộ dữ liệu lên backend (PATCH /users/me) sau khi hoàn tất bước GOAL.
     */
    fun submit(onSuccess: (() -> Unit)? = null) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = UpdateProfileRequest(
                gender = state.gender,
                dateOfBirth = state.dateOfBirth,
                heightCm = state.heightCm,
                weightKg = state.weightKg,
                activityLevel = state.activityLevel,
                goal = state.goal
            )
            repository.updateProfile(request).onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isCompleted = true,
                        savedProfile = profile
                    )
                }
                onSuccess?.invoke()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Không thể lưu thông tin, vui lòng thử lại"
                    )
                }
            }
        }
    }
}
