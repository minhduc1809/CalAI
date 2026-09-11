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

/** Tổng số bước của Onboarding (26 bước — mỗi bước 1 câu hỏi, theo BRD mục 4.2.8). */
const val ONBOARDING_STEP_COUNT = 26

data class OnboardingUiState(
    // 0: WELCOME, 1: GENDER, 2: BIRTH_DATE, 3: HEIGHT, 4: WEIGHT, 5: BODY_FAT, 6: GOAL,
    // 7: TARGET_WEIGHT_RATE, 8: LIFESTYLE, 9: NUTRITION, 10: TRAINING, 11: PROGRAM_SETUP
    val currentStep: Int = 0,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedProfile: UserProfileDto? = null,

    // 1. GENDER — bắt buộc chọn, không có mặc định (sửa bug hardcode "MALE" trước đây)
    val gender: String = "",

    // 2. BIRTH_DATE (day, month, year)
    val birthDay: Int = 15,
    val birthMonth: Int = 5,
    val birthYear: Int = 1998,

    // 3. HEIGHT (cm)
    val heightCm: Float = 170f,

    // 4. WEIGHT (kg, cân nặng hiện tại)
    val weightKg: Float = 65f,

    // 5. BODY_FAT (%, optional)
    val bodyFatPercent: Float? = null,

    // 6. GOAL: LOSE_WEIGHT, MAINTAIN, GAIN_WEIGHT
    val goal: String = "MAINTAIN",

    // 7. TARGET_WEIGHT_RATE
    val targetWeightKg: Float = 65f,
    val weightRateKgPerWeek: Float = 0.5f,

    // 8. LIFESTYLE (giờ ngủ, mức stress, supplements, mức vận động)
    val sleepHours: Float = 7f,
    val stressLevel: String = "MEDIUM",
    val takesSupplements: Boolean = false,
    val activityLevel: String = "MODERATELY_ACTIVE",

    // 9. NUTRITION (chế độ ăn, số bữa/ngày, thời gian nấu, ngân sách, Intermittent Fasting)
    val dietType: String = "BALANCED",
    val mealsPerDay: Int = 3,
    val cookTimeMinutes: Int = 30,
    val foodBudgetLevel: String = "MEDIUM",
    val isIntermittentFasting: Boolean = false,
    val ifWindowStart: String = "12:00",
    val ifWindowEnd: String = "20:00",

    // 10. TRAINING (kinh nghiệm, mục tiêu tập, buổi/tuần, thiết bị, chấn thương, 1RM)
    val trainingExperience: String = "BEGINNER",
    val trainingGoal: String = "GENERAL_FITNESS",
    val sessionsPerWeek: String = "THREE_TO_FOUR",
    val equipmentAccess: String = "BODYWEIGHT_ONLY",
    val injuries: List<String> = emptyList(),
    val injuriesOtherNote: String = "",
    val oneRepMaxSquatKg: Float? = null,
    val oneRepMaxBenchKg: Float? = null,
    val oneRepMaxDeadliftKg: Float? = null,

    // 11. PROGRAM_SETUP
    val macroStyle: String = "BALANCED",
    val programType: String = "COACHED",
    val proteinPreference: String = "MID"
) {
    val dateOfBirth: String
        get() = "%04d-%02d-%02d".format(birthYear, birthMonth, birthDay)

    /** Kiểm tra xem bước hiện tại đã hợp lệ để tiếp tục hay chưa. */
    fun canProceed(step: Int): Boolean = when (step) {
        0 -> true // Welcome
        1 -> gender.isNotBlank() // Bắt buộc chọn giới tính — chặn Next nếu bỏ qua (BRD)
        2 -> birthYear in 1920..2020 && birthMonth in 1..12 && birthDay in 1..31
        3 -> heightCm in 50f..250f
        4 -> weightKg in 20f..300f
        5 -> true // Body Fat optional
        6 -> goal.isNotBlank()
        7 -> goal == "MAINTAIN" || (targetWeightKg in 20f..300f && weightRateKgPerWeek in 0.1f..1.5f)
        8 -> sleepHours in 0f..24f // Sleep Hours
        9 -> true // Stress Level — có default hợp lệ
        10 -> true // Supplements — boolean luôn hợp lệ
        11 -> true // Activity Level — có default hợp lệ
        12 -> true // Diet Type — có default hợp lệ
        13 -> mealsPerDay in 1..10 // Meals Per Day
        14 -> cookTimeMinutes in 0..300 // Cook Time
        15 -> true // Food Budget — có default hợp lệ
        16 -> !isIntermittentFasting || (ifWindowStart.isNotBlank() && ifWindowEnd.isNotBlank()) // Intermittent Fasting
        17 -> true // Training Experience — có default hợp lệ
        18 -> true // Training Goal — có default hợp lệ
        19 -> true // Sessions Per Week — có default hợp lệ
        20 -> true // Equipment Access — có default hợp lệ
        21 -> true // Injuries — optional, có default hợp lệ
        22 -> true // One Rep Max — optional
        23 -> true // Program Type — có default hợp lệ
        24 -> true // Macro Style — có default hợp lệ
        25 -> true // Protein Preference — có default hợp lệ
        else -> true
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectGender(value: String) {
        _uiState.update { it.copy(gender = value, errorMessage = null) }
    }

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

    fun selectBodyFatPercent(value: Float?) {
        _uiState.update { it.copy(bodyFatPercent = value, errorMessage = null) }
    }

    fun selectGoal(goal: String) {
        _uiState.update { it.copy(goal = goal, errorMessage = null) }
    }

    fun setTargetWeightKg(value: Float) {
        _uiState.update { it.copy(targetWeightKg = value, errorMessage = null) }
    }

    fun selectWeightRate(value: Float) {
        _uiState.update { it.copy(weightRateKgPerWeek = value, errorMessage = null) }
    }

    fun setSleepHours(value: Float) {
        _uiState.update { it.copy(sleepHours = value, errorMessage = null) }
    }

    fun selectStressLevel(level: String) {
        _uiState.update { it.copy(stressLevel = level, errorMessage = null) }
    }

    fun setTakesSupplements(value: Boolean) {
        _uiState.update { it.copy(takesSupplements = value, errorMessage = null) }
    }

    fun selectActivityLevel(level: String) {
        _uiState.update { it.copy(activityLevel = level, errorMessage = null) }
    }

    fun selectDietType(type: String) {
        _uiState.update { it.copy(dietType = type, errorMessage = null) }
    }

    fun setMealsPerDay(value: Int) {
        _uiState.update { it.copy(mealsPerDay = value, errorMessage = null) }
    }

    fun setCookTimeMinutes(value: Int) {
        _uiState.update { it.copy(cookTimeMinutes = value, errorMessage = null) }
    }

    fun selectFoodBudgetLevel(level: String) {
        _uiState.update { it.copy(foodBudgetLevel = level, errorMessage = null) }
    }

    fun setIntermittentFasting(enabled: Boolean) {
        _uiState.update { it.copy(isIntermittentFasting = enabled, errorMessage = null) }
    }

    fun setIfWindow(start: String, end: String) {
        _uiState.update { it.copy(ifWindowStart = start, ifWindowEnd = end, errorMessage = null) }
    }

    fun selectTrainingExperience(value: String) {
        _uiState.update { it.copy(trainingExperience = value, errorMessage = null) }
    }

    fun selectTrainingGoal(value: String) {
        _uiState.update { it.copy(trainingGoal = value, errorMessage = null) }
    }

    fun selectSessionsPerWeek(value: String) {
        _uiState.update { it.copy(sessionsPerWeek = value, errorMessage = null) }
    }

    fun selectEquipmentAccess(value: String) {
        _uiState.update { it.copy(equipmentAccess = value, errorMessage = null) }
    }

    /** "Không có" loại trừ mọi lựa chọn khác; các chấn thương khác toggle độc lập. */
    fun toggleInjury(value: String) {
        _uiState.update { state ->
            val current = state.injuries
            val updated = when {
                value == "NONE" -> if (current.contains("NONE")) emptyList() else listOf("NONE")
                current.contains(value) -> current - value
                else -> (current - "NONE") + value
            }
            state.copy(injuries = updated, errorMessage = null)
        }
    }

    fun setInjuriesOtherNote(value: String) {
        _uiState.update { it.copy(injuriesOtherNote = value, errorMessage = null) }
    }

    fun setOneRepMaxSquat(value: Float?) {
        _uiState.update { it.copy(oneRepMaxSquatKg = value, errorMessage = null) }
    }

    fun setOneRepMaxBench(value: Float?) {
        _uiState.update { it.copy(oneRepMaxBenchKg = value, errorMessage = null) }
    }

    fun setOneRepMaxDeadlift(value: Float?) {
        _uiState.update { it.copy(oneRepMaxDeadliftKg = value, errorMessage = null) }
    }

    fun clearOneRepMax() {
        _uiState.update {
            it.copy(oneRepMaxSquatKg = null, oneRepMaxBenchKg = null, oneRepMaxDeadliftKg = null, errorMessage = null)
        }
    }

    fun selectMacroStyle(value: String) {
        _uiState.update { it.copy(macroStyle = value, errorMessage = null) }
    }

    fun selectProgramType(value: String) {
        _uiState.update { it.copy(programType = value, errorMessage = null) }
    }

    fun selectProteinPreference(value: String) {
        _uiState.update { it.copy(proteinPreference = value, errorMessage = null) }
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
                goal = state.goal,
                bodyFatPercent = state.bodyFatPercent,
                targetWeightKg = state.targetWeightKg,
                weightRateKgPerWeek = state.weightRateKgPerWeek,
                sleepHours = state.sleepHours,
                stressLevel = state.stressLevel,
                takesSupplements = state.takesSupplements,
                dietType = state.dietType,
                mealsPerDay = state.mealsPerDay,
                cookTimeMinutes = state.cookTimeMinutes,
                foodBudgetLevel = state.foodBudgetLevel,
                isIntermittentFasting = state.isIntermittentFasting,
                ifWindowStart = if (state.isIntermittentFasting) state.ifWindowStart else null,
                ifWindowEnd = if (state.isIntermittentFasting) state.ifWindowEnd else null,
                trainingExperience = state.trainingExperience,
                trainingGoal = state.trainingGoal,
                sessionsPerWeek = state.sessionsPerWeek,
                equipmentAccess = state.equipmentAccess,
                injuries = state.injuries,
                injuriesOtherNote = state.injuriesOtherNote.ifBlank { null },
                oneRepMaxSquatKg = state.oneRepMaxSquatKg,
                oneRepMaxBenchKg = state.oneRepMaxBenchKg,
                oneRepMaxDeadliftKg = state.oneRepMaxDeadliftKg,
                macroStyle = state.macroStyle,
                programType = state.programType,
                proteinPreference = state.proteinPreference
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
