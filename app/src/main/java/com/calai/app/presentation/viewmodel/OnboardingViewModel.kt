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
import kotlin.math.log10

/** Tổng số bước của Onboarding v2 (13 màn — theo CHANGELOG_Onboarding_v2.md). */
const val ONBOARDING_STEP_COUNT = 13

data class OnboardingUiState(
    // 0: OVERVIEW, 1: STEP1_INTRO, 2: BODY_METRICS, 3: BODY_FAT,
    // 4: STEP2_INTRO, 5: GOAL, 6: DIET_STYLE, 7: ALLERGIES,
    // 8: STEP3_INTRO, 9: TRAINING_EXPERIENCE_GOAL, 10: TRAINING_SCHEDULE_EQUIPMENT,
    // 11: INJURIES, 12: SUMMARY
    val currentStep: Int = 0,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedProfile: UserProfileDto? = null,

    // ── BODY METRICS (màn 2 — gộp Gender/BirthDate/Height/Weight/Units) ──
    val gender: String = "",
    val pregnancyStatus: String = "NONE", // "NONE" | "PREGNANT" | "LACTATING"
    val birthDay: Int = 15,
    val birthMonth: Int = 5,
    val birthYear: Int = 1998,
    val heightCm: Float = 170f,
    val weightKg: Float = 65f,
    val units: String = "METRIC",

    // ── BODY FAT (màn 3 — manual hoặc navy-tape) ──
    val bodyFatPercent: Float? = null,
    val bodyFatSource: String = "manual", // "manual" | "navy_tape"
    val neckCm: Float? = null,
    val waistCm: Float? = null,
    val hipCm: Float? = null,

    // ── GOAL (màn 6 — dual-mode: general/precise) ──
    val goal: String = "MAINTAIN",
    val goalMode: String = "general", // "general" | "precise"
    val generalChoice: String = "lose_fat", // "lose_fat" | "maintain" | "build_muscle"
    val targetWeightKg: Float = 65f,
    val weightRateKgPerWeek: Float = 0.5f,

    // ── DIET STYLE (màn 7, BR-02.1 — 2 câu hỏi độc lập) ──
    // dietType: OMNIVORE | PESCATARIAN | VEGETARIAN | VEGAN | HALAL (lọc món); macroStyle: BALANCED | HIGH_CARB_LOW_FAT | LOW_CARB_HIGH_FAT | KETO (chia macro)
    val macroStyle: String = "BALANCED",

    // ── ALLERGIES (màn 8) ──
    val allergies: List<String> = emptyList(),

    // ── TRAINING (màn 10, 11 — gộp) ──
    val trainingExperience: String = "BEGINNER",
    val trainingGoal: String = "GENERAL_FITNESS",
    val preferredSplit: String? = null,
    val sessionsPerWeek: String = "THREE_TO_FOUR",
    val equipmentAccess: String = "BODYWEIGHT_ONLY",

    // ── INJURIES (màn 12 — giữ nguyên, tách riêng vì lý do an toàn) ──
    val injuries: List<String> = emptyList(),
    val injuriesOtherNote: String = "",
    val oneRepMaxSquatKg: Float? = null,
    val oneRepMaxBenchKg: Float? = null,
    val oneRepMaxDeadliftKg: Float? = null,

    // ── Trường đã bị loại khỏi luồng hỏi nhưng vẫn giữ default để không phá DTO/backend ──
    val activityLevel: String = "MODERATELY_ACTIVE", // giờ được suy ra tự động từ sessionsPerWeek
    val dietType: String = "OMNIVORE", // BR-02.1: chế độ ăn, tách riêng khỏi macroStyle
    val sleepHours: Float = 7f,
    val stressLevel: String = "MEDIUM",
    val takesSupplements: Boolean = false,
    val mealsPerDay: Int = 3,
    val cookTimeMinutes: Int = 30,
    val foodBudgetLevel: String = "MEDIUM",
    val isIntermittentFasting: Boolean = false,
    val ifWindowStart: String = "12:00",
    val ifWindowEnd: String = "20:00"
) {
    val dateOfBirth: String
        get() = "%04d-%02d-%02d".format(birthYear, birthMonth, birthDay)

    /** Số buổi tập/tuần đại diện của lựa chọn hiện tại; backend suy ra activityLevel từ số này (BR-03.6). */
    val trainingDaysPerWeek: Int
        get() = sessionsPerWeekToInt(sessionsPerWeek)

    /** Chỉ dùng để hiển thị ước tính tạm; giá trị chính thức do backend suy ra. */
    val derivedActivityLevel: String
        get() {
            val n = sessionsPerWeekToInt(sessionsPerWeek)
            return when {
                n <= 0 -> "SEDENTARY"
                n <= 2 -> "LIGHTLY_ACTIVE"
                n <= 4 -> "MODERATELY_ACTIVE"
                n <= 6 -> "VERY_ACTIVE"
                else -> "EXTRA_ACTIVE"
            }
        }

    /** Kiểm tra xem bước hiện tại đã hợp lệ để tiếp tục hay chưa (13 bước, đánh số 0-12). */
    fun canProceed(step: Int): Boolean = when (step) {
        0 -> true // Overview
        1 -> true // Step 1 intro
        2 -> {
            val age = calculateAge(birthYear, birthMonth, birthDay)
            gender.isNotBlank() &&
                age in 18..100 &&
                heightCm in 50f..250f && weightKg in 20f..300f // Body Metrics (BR-02.2)
        }
        3 -> if (bodyFatSource == "navy_tape") {
            val neckOk = neckCm != null && neckCm in 20f..200f
            val waistOk = waistCm != null && waistCm in 20f..200f
            val hipOk = gender != "FEMALE" || (hipCm != null && hipCm in 20f..200f)
            neckOk && waistOk && hipOk
        } else true // Body Fat — optional (manual luôn hợp lệ, kể cả để trống)
        4 -> true // Step 2 intro
        5 -> {
            val isRestricted = pregnancyStatus in listOf("PREGNANT", "LACTATING")
            if (isRestricted && (goal == "LOSE_WEIGHT" || (goalMode == "general" && generalChoice == "lose_fat") || (goalMode == "precise" && targetWeightKg < weightKg))) {
                false // Phụ nữ mang thai hoặc cho con bú bị cấm giảm cân (BR-02.2)
            } else if (goalMode == "precise") {
                targetWeightKg in 20f..300f &&
                    weightRateKgPerWeek in 0.0f..1.5f &&
                    !(weightRateKgPerWeek == 0f && targetWeightKg != weightKg)
            } else true
        } // Goal
        6 -> dietType.isNotBlank() && macroStyle.isNotBlank() // Diet Style (chế độ ăn + cách chia macro)
        7 -> true // Allergies — optional
        8 -> true // Step 3 intro
        9 -> true // Training Experience & Goal — có default hợp lệ
        10 -> true // Training Schedule & Equipment — có default hợp lệ
        11 -> true // Injuries — optional
        12 -> true // Summary
        else -> true
    }
}

/** Tính tuổi chính xác từ ngày tháng năm sinh (BR-02.2: 18–100 tuổi). */
private fun calculateAge(year: Int, month: Int, day: Int): Int {
    val today = java.util.Calendar.getInstance()
    var age = today.get(java.util.Calendar.YEAR) - year
    val currentMonth = today.get(java.util.Calendar.MONTH) + 1
    val currentDay = today.get(java.util.Calendar.DAY_OF_MONTH)
    if (currentMonth < month || (currentMonth == month && currentDay < day)) {
        age--
    }
    return age
}

/** Chuyển sessionsPerWeek (enum chuỗi) sang số buổi/tuần đại diện, dùng để suy ra activityLevel. */
private fun sessionsPerWeekToInt(sessionsPerWeek: String): Int = when (sessionsPerWeek) {
    "ZERO" -> 0
    "ONE_TO_TWO" -> 2
    "THREE_TO_FOUR" -> 4
    "FIVE_TO_SIX" -> 5
    "SEVEN" -> 7
    else -> 3
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadDraft()
    }

    private fun loadDraft() {
        viewModelScope.launch {
            repository.getOnboardingDraft().onSuccess { draft ->
                if (draft != null && draft.data != null) {
                    val d = draft.data
                    _uiState.update { s ->
                        s.copy(
                            currentStep = draft.step.coerceIn(0, ONBOARDING_STEP_COUNT - 1),
                            gender = (d["gender"] as? String) ?: s.gender,
                            pregnancyStatus = (d["pregnancyStatus"] as? String) ?: s.pregnancyStatus,
                            birthYear = (d["birthYear"] as? Number)?.toInt() ?: s.birthYear,
                            birthMonth = (d["birthMonth"] as? Number)?.toInt() ?: s.birthMonth,
                            birthDay = (d["birthDay"] as? Number)?.toInt() ?: s.birthDay,
                            heightCm = (d["heightCm"] as? Number)?.toFloat() ?: s.heightCm,
                            weightKg = (d["weightKg"] as? Number)?.toFloat() ?: s.weightKg,
                            units = (d["units"] as? String) ?: s.units,
                            bodyFatPercent = (d["bodyFatPercent"] as? Number)?.toFloat() ?: s.bodyFatPercent,
                            goal = (d["goal"] as? String) ?: s.goal,
                            goalMode = (d["goalMode"] as? String) ?: s.goalMode,
                            generalChoice = (d["generalChoice"] as? String) ?: s.generalChoice,
                            targetWeightKg = (d["targetWeightKg"] as? Number)?.toFloat() ?: s.targetWeightKg,
                            weightRateKgPerWeek = (d["weightRateKgPerWeek"] as? Number)?.toFloat() ?: s.weightRateKgPerWeek,
                            macroStyle = (d["macroStyle"] as? String) ?: s.macroStyle,
                            dietType = (d["dietType"] as? String) ?: s.dietType,
                            allergies = (d["allergies"] as? List<*>)?.filterIsInstance<String>() ?: s.allergies,
                            trainingExperience = (d["trainingExperience"] as? String) ?: s.trainingExperience,
                            trainingGoal = (d["trainingGoal"] as? String) ?: s.trainingGoal,
                            sessionsPerWeek = (d["sessionsPerWeek"] as? String) ?: s.sessionsPerWeek,
                            equipmentAccess = (d["equipmentAccess"] as? String) ?: s.equipmentAccess
                        )
                    }
                }
            }
        }
    }

    private fun saveDraft() {
        val state = _uiState.value
        if (state.currentStep == 0) return
        viewModelScope.launch {
            val dataMap = mapOf(
                "gender" to state.gender,
                "pregnancyStatus" to state.pregnancyStatus,
                "birthYear" to state.birthYear,
                "birthMonth" to state.birthMonth,
                "birthDay" to state.birthDay,
                "heightCm" to state.heightCm,
                "weightKg" to state.weightKg,
                "units" to state.units,
                "bodyFatPercent" to state.bodyFatPercent,
                "goal" to state.goal,
                "goalMode" to state.goalMode,
                "generalChoice" to state.generalChoice,
                "targetWeightKg" to state.targetWeightKg,
                "weightRateKgPerWeek" to state.weightRateKgPerWeek,
                "macroStyle" to state.macroStyle,
                "dietType" to state.dietType,
                "allergies" to state.allergies,
                "trainingExperience" to state.trainingExperience,
                "trainingGoal" to state.trainingGoal,
                "sessionsPerWeek" to state.sessionsPerWeek,
                "equipmentAccess" to state.equipmentAccess
            )
            repository.saveOnboardingDraft(state.currentStep, dataMap)
        }
    }

    // ── BODY METRICS ──
    fun selectGender(value: String) {
        _uiState.update {
            val resetPregnancy = if (value != "FEMALE") "NONE" else it.pregnancyStatus
            it.copy(gender = value, pregnancyStatus = resetPregnancy, errorMessage = null)
        }
    }

    fun selectPregnancyStatus(value: String) {
        _uiState.update { state ->
            val updated = state.copy(pregnancyStatus = value, errorMessage = null)
            if (value in listOf("PREGNANT", "LACTATING")) {
                updated.copy(
                    goal = "MAINTAIN",
                    generalChoice = "maintain",
                    targetWeightKg = updated.weightKg,
                    weightRateKgPerWeek = 0f
                )
            } else {
                updated
            }
        }
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

    fun selectUnits(value: String) {
        _uiState.update { it.copy(units = value, errorMessage = null) }
    }

    // ── BODY FAT ──
    fun selectBodyFatSource(value: String) {
        _uiState.update { it.copy(bodyFatSource = value, errorMessage = null) }
    }

    fun selectBodyFatPercent(value: Float?) {
        _uiState.update { it.copy(bodyFatPercent = value, errorMessage = null) }
    }

    /** Tính % mỡ cơ thể bằng phương pháp US Navy từ số đo vòng eo/cổ/hông. */
    fun setNavyTapeMeasurements(neckCm: Float?, waistCm: Float?, hipCm: Float?) {
        _uiState.update { state ->
            val computed = computeNavyBodyFat(
                gender = state.gender,
                heightCm = state.heightCm,
                neckCm = neckCm,
                waistCm = waistCm,
                hipCm = hipCm
            )
            state.copy(
                neckCm = neckCm,
                waistCm = waistCm,
                hipCm = hipCm,
                bodyFatPercent = computed ?: state.bodyFatPercent,
                errorMessage = null
            )
        }
    }

    // ── GOAL ──
    fun selectGoalMode(value: String) {
        _uiState.update { state ->
            // Prefill cân nặng mục tiêu = cân nặng hiện tại khi lần đầu chuyển sang chế độ chính xác
            val prefilledTarget = if (value == "precise" && state.targetWeightKg == 65f) state.weightKg else state.targetWeightKg
            state.copy(goalMode = value, targetWeightKg = prefilledTarget, errorMessage = null).let(::syncDerivedGoal)
        }
    }

    fun selectGeneralGoalChoice(value: String) {
        _uiState.update { state ->
            state.copy(generalChoice = value, errorMessage = null).let(::syncDerivedGoal)
        }
    }

    fun setTargetWeightKg(value: Float) {
        _uiState.update { state ->
            state.copy(targetWeightKg = value, errorMessage = null).let(::syncDerivedGoal)
        }
    }

    fun selectWeightRate(value: Float) {
        _uiState.update { it.copy(weightRateKgPerWeek = value, errorMessage = null) }
    }

    /** Đồng bộ `goal` (LOSE_WEIGHT/MAINTAIN/GAIN_WEIGHT) và `weightRateKgPerWeek` theo goalMode hiện tại. */
    private fun syncDerivedGoal(state: OnboardingUiState): OnboardingUiState {
        return if (state.goalMode == "precise") {
            val goal = when {
                state.targetWeightKg < state.weightKg -> "LOSE_WEIGHT"
                state.targetWeightKg > state.weightKg -> "GAIN_WEIGHT"
                else -> "MAINTAIN"
            }
            state.copy(goal = goal)
        } else {
            val (goal, rate) = when (state.generalChoice) {
                "lose_fat" -> "LOSE_WEIGHT" to 0.5f
                "build_muscle" -> "GAIN_WEIGHT" to 0.25f
                else -> "MAINTAIN" to 0f
            }
            state.copy(goal = goal, weightRateKgPerWeek = rate)
        }
    }

    // ── DIET STYLE ──
    fun selectMacroStyle(value: String) {
        _uiState.update { it.copy(macroStyle = value, errorMessage = null) }
    }

    fun selectDietType(value: String) {
        _uiState.update { it.copy(dietType = value, errorMessage = null) }
    }

    // ── ALLERGIES ──
    /** "Không có" loại trừ mọi lựa chọn khác; các dị ứng khác toggle độc lập. */
    fun toggleAllergy(value: String) {
        _uiState.update { state ->
            val current = state.allergies
            val updated = when {
                value == "NONE" -> if (current.contains("NONE")) emptyList() else listOf("NONE")
                current.contains(value) -> current - value
                else -> (current - "NONE") + value
            }
            state.copy(allergies = updated, errorMessage = null)
        }
    }

    // ── TRAINING ──
    fun selectTrainingExperience(value: String) {
        _uiState.update { it.copy(trainingExperience = value, errorMessage = null) }
    }

    fun selectTrainingGoal(value: String) {
        _uiState.update { it.copy(trainingGoal = value, errorMessage = null) }
    }

    fun selectPreferredSplit(value: String?) {
        _uiState.update { it.copy(preferredSplit = value, errorMessage = null) }
    }

    fun selectSessionsPerWeek(value: String) {
        _uiState.update { it.copy(sessionsPerWeek = value, errorMessage = null) }
    }

    fun selectEquipmentAccess(value: String) {
        _uiState.update { it.copy(equipmentAccess = value, errorMessage = null) }
    }

    // ── INJURIES (giữ nguyên) ──
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

    fun setCurrentStep(step: Int) {
        val target = step.coerceIn(0, ONBOARDING_STEP_COUNT - 1)
        _uiState.update { it.copy(currentStep = target) }
        saveDraft()
    }

    /**
     * Gửi toàn bộ dữ liệu lên backend (PATCH /users/me) sau khi hoàn tất Onboarding.
     * activityLevel được suy ra tự động từ sessionsPerWeek (không hỏi trực tiếp nữa).
     */
    fun submit(onSuccess: (() -> Unit)? = null) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = UpdateProfileRequest(
                gender = state.gender,
                pregnancyStatus = if (state.gender == "FEMALE") state.pregnancyStatus else "NONE",
                dateOfBirth = state.dateOfBirth,
                heightCm = state.heightCm,
                weightKg = state.weightKg,
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
                // activityLevel và nhóm buổi tập do backend suy ra từ số buổi tập (BR-03.6)
                trainingDaysPerWeek = state.trainingDaysPerWeek,
                equipmentAccess = state.equipmentAccess,
                injuries = state.injuries,
                injuriesOtherNote = state.injuriesOtherNote.ifBlank { null },
                oneRepMaxSquatKg = state.oneRepMaxSquatKg,
                oneRepMaxBenchKg = state.oneRepMaxBenchKg,
                oneRepMaxDeadliftKg = state.oneRepMaxDeadliftKg,
                macroStyle = state.macroStyle,
                // programType/proteinPreference KHÔNG gửi ở đây — Onboarding không còn hỏi 2 câu này
                // (xem CHANGELOG_Onboarding_v2.md), để backend tự áp default DB (COACHED/MID) cho user mới
                // thay vì App luôn ghi đè bằng giá trị mặc định cứng.
                allergies = state.allergies,
                // Hoàn tất Onboarding: người dùng đã xem màn Tổng kết → áp dụng mục tiêu (BR-04, E1)
                applyTarget = true
            )
            repository.updateProfile(request).onSuccess { profile ->
                viewModelScope.launch {
                    repository.clearOnboardingDraft()
                }
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

/**
 * Công thức US Navy tính % mỡ cơ thể từ vòng eo/cổ (và vòng hông nếu là nữ).
 * Nam:  BF% = 495 / (1.0324 - 0.19077*log10(waist-neck) + 0.15456*log10(height)) - 450
 * Nữ:   BF% = 495 / (1.29579 - 0.35004*log10(waist+hip-neck) + 0.22100*log10(height)) - 450
 */
private fun computeNavyBodyFat(
    gender: String,
    heightCm: Float,
    neckCm: Float?,
    waistCm: Float?,
    hipCm: Float?
): Float? {
    if (neckCm == null || waistCm == null || heightCm <= 0f) return null
    return if (gender == "FEMALE") {
        if (hipCm == null) return null
        val diff = waistCm + hipCm - neckCm
        if (diff <= 0f) return null
        (495.0 / (1.29579 - 0.35004 * log10(diff.toDouble()) + 0.22100 * log10(heightCm.toDouble())) - 450.0).toFloat()
    } else {
        val diff = waistCm - neckCm
        if (diff <= 0f) return null
        (495.0 / (1.0324 - 0.19077 * log10(diff.toDouble()) + 0.15456 * log10(heightCm.toDouble())) - 450.0).toFloat()
    }
}
