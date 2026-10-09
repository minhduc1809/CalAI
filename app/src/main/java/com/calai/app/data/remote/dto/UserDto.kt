package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProposedTargetDiffDto(
    @SerializedName("calories") val calories: Float = 0f,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carb") val carb: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f
)

data class ProposedTargetDto(
    @SerializedName("calories") val calories: Float,
    @SerializedName("protein") val protein: Float? = null,
    @SerializedName("carb") val carb: Float? = null,
    @SerializedName("fat") val fat: Float? = null,
    @SerializedName("diff") val diff: ProposedTargetDiffDto = ProposedTargetDiffDto()
)

data class UserProfileDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("isEmailVerified")
    val isEmailVerified: Boolean = false,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("heightCm")
    val heightCm: Float? = null,
    @SerializedName("weightKg")
    val weightKg: Float? = null,
    @SerializedName("activityLevel")
    val activityLevel: String? = null,
    @SerializedName("goal")
    val goal: String? = null,
    @SerializedName("targetWeightKg")
    val targetWeightKg: Float? = null,
    @SerializedName("weightRateKgPerWeek")
    val weightRateKgPerWeek: Float? = null,
    @SerializedName("weightRatePercent")
    val weightRatePercent: Float? = null,
    @SerializedName("pregnancyStatus")
    val pregnancyStatus: String? = null,
    @SerializedName("bodyFatPercent")
    val bodyFatPercent: Float? = null,
    @SerializedName("macroStyle")
    val macroStyle: String? = null,
    @SerializedName("bmi")
    val bmi: Float? = null,
    @SerializedName("bmr")
    val bmr: Float? = null,
    @SerializedName("tdee")
    val tdee: Float? = null,
    @SerializedName("targetCalories")
    val targetCalories: Float? = null,
    @SerializedName("targetProtein")
    val targetProtein: Float? = null,
    @SerializedName("targetCarb")
    val targetCarb: Float? = null,
    @SerializedName("targetFat")
    val targetFat: Float? = null,
    @SerializedName("adaptiveExpenditure")
    val adaptiveExpenditure: Float? = null,
    @SerializedName("expenditureStatus")
    val expenditureStatus: String? = null,
    @SerializedName("dailyAiQuota")
    val dailyAiQuota: Int? = null,
    @SerializedName("timezone")
    val timezone: String? = null,
    /** Quy tắc an toàn đã giới hạn mục tiêu calo: FLOOR | DEFICIT_CAP | SURPLUS_CAP | null (BR-03.3). */
    @SerializedName("targetLimitedBy")
    val targetLimitedBy: String? = null,
    /** true nếu lần cập nhật này đã áp dụng mục tiêu mới (Onboarding, đổi mục tiêu). */
    @SerializedName("targetApplied")
    val targetApplied: Boolean? = null,
    /** Mục tiêu đề xuất khi hồ sơ đổi nhưng chưa áp dụng (BR-04); null nếu không có gì khác. */
    @SerializedName("proposedTarget")
    val proposedTarget: ProposedTargetDto? = null,
    @SerializedName("sleepHours")
    val sleepHours: Float? = null,
    @SerializedName("stressLevel")
    val stressLevel: String? = null,
    @SerializedName("takesSupplements")
    val takesSupplements: Boolean? = null,
    @SerializedName("dietType")
    val dietType: String? = null,
    @SerializedName("mealsPerDay")
    val mealsPerDay: Int? = null,
    @SerializedName("cookTimeMinutes")
    val cookTimeMinutes: Int? = null,
    @SerializedName("foodBudgetLevel")
    val foodBudgetLevel: String? = null,
    @SerializedName("trainingExperience")
    val trainingExperience: String? = null,
    @SerializedName("trainingGoal")
    val trainingGoal: String? = null,
    @SerializedName("sessionsPerWeek")
    val sessionsPerWeek: String? = null,
    @SerializedName("equipmentAccess")
    val equipmentAccess: String? = null,
    @SerializedName("injuries")
    val injuries: List<String>? = null,
    @SerializedName("injuriesOtherNote")
    val injuriesOtherNote: String? = null,
    @SerializedName("oneRepMaxSquatKg")
    val oneRepMaxSquatKg: Float? = null,
    @SerializedName("oneRepMaxBenchKg")
    val oneRepMaxBenchKg: Float? = null,
    @SerializedName("oneRepMaxDeadliftKg")
    val oneRepMaxDeadliftKg: Float? = null,
    @SerializedName("programType")
    val programType: String? = null,
    @SerializedName("proteinPreference")
    val proteinPreference: String? = null,
    @SerializedName("isIntermittentFasting")
    val isIntermittentFasting: Boolean? = null,
    @SerializedName("ifWindowStart")
    val ifWindowStart: String? = null,
    @SerializedName("ifWindowEnd")
    val ifWindowEnd: String? = null
)

/** Trạng thái Adaptive Expenditure Engine — GET /users/me/expenditure. */
data class ExpenditureStatusDto(
    @SerializedName("method")
    val method: String, // ADAPTIVE | STATIC_FALLBACK
    @SerializedName("status")
    val status: String, // UPDATING | HOLDING
    @SerializedName("estimatedExpenditure")
    val estimatedExpenditure: Float? = null,
    @SerializedName("staticTdee")
    val staticTdee: Float? = null,
    @SerializedName("windowDays")
    val windowDays: Int = 0,
    @SerializedName("weightLogsCount")
    val weightLogsCount: Int = 0,
    @SerializedName("loggedDaysCount")
    val loggedDaysCount: Int = 0,
    @SerializedName("trendWeightStart")
    val trendWeightStart: Float? = null,
    @SerializedName("trendWeightEnd")
    val trendWeightEnd: Float? = null,
    @SerializedName("avgDailyCaloriesConsumed")
    val avgDailyCaloriesConsumed: Float? = null,
    @SerializedName("message")
    val message: String
)

data class UpdateProfileRequest(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("heightCm")
    val heightCm: Float? = null,
    @SerializedName("weightKg")
    val weightKg: Float? = null,
    @SerializedName("activityLevel")
    val activityLevel: String? = null,
    @SerializedName("goal")
    val goal: String? = null,
    @SerializedName("targetWeightKg")
    val targetWeightKg: Float? = null,
    @SerializedName("weightRateKgPerWeek")
    val weightRateKgPerWeek: Float? = null,
    @SerializedName("bodyFatPercent")
    val bodyFatPercent: Float? = null,
    @SerializedName("macroStyle")
    val macroStyle: String? = null,
    @SerializedName("sleepHours")
    val sleepHours: Float? = null,
    @SerializedName("stressLevel")
    val stressLevel: String? = null,
    @SerializedName("takesSupplements")
    val takesSupplements: Boolean? = null,
    @SerializedName("dietType")
    val dietType: String? = null,
    @SerializedName("mealsPerDay")
    val mealsPerDay: Int? = null,
    @SerializedName("cookTimeMinutes")
    val cookTimeMinutes: Int? = null,
    @SerializedName("foodBudgetLevel")
    val foodBudgetLevel: String? = null,
    @SerializedName("trainingExperience")
    val trainingExperience: String? = null,
    @SerializedName("trainingGoal")
    val trainingGoal: String? = null,
    @SerializedName("sessionsPerWeek")
    val sessionsPerWeek: String? = null,
    /** Số buổi tập mỗi tuần (0–7). Backend suy ra activityLevel và sessionsPerWeek từ giá trị này (BR-03.6). */
    @SerializedName("trainingDaysPerWeek")
    val trainingDaysPerWeek: Int? = null,
    @SerializedName("equipmentAccess")
    val equipmentAccess: String? = null,
    @SerializedName("injuries")
    val injuries: List<String>? = null,
    @SerializedName("injuriesOtherNote")
    val injuriesOtherNote: String? = null,
    @SerializedName("oneRepMaxSquatKg")
    val oneRepMaxSquatKg: Float? = null,
    @SerializedName("oneRepMaxBenchKg")
    val oneRepMaxBenchKg: Float? = null,
    @SerializedName("oneRepMaxDeadliftKg")
    val oneRepMaxDeadliftKg: Float? = null,
    @SerializedName("programType")
    val programType: String? = null,
    @SerializedName("proteinPreference")
    val proteinPreference: String? = null,
    @SerializedName("isIntermittentFasting")
    val isIntermittentFasting: Boolean? = null,
    @SerializedName("ifWindowStart")
    val ifWindowStart: String? = null,
    @SerializedName("ifWindowEnd")
    val ifWindowEnd: String? = null,
    @SerializedName("allergies")
    val allergies: List<String>? = null,
    @SerializedName("pregnancyStatus")
    val pregnancyStatus: String? = null,
    @SerializedName("weightRatePercent")
    val weightRatePercent: Float? = null,
    /**
     * true = người dùng đã xác nhận áp dụng mục tiêu tính lại (BR-04: Onboarding, đổi mục tiêu).
     * Không truyền thì backend chỉ trả proposedTarget và giữ nguyên mục tiêu đang dùng.
     */
    @SerializedName("applyTarget")
    val applyTarget: Boolean? = null
)

data class OnboardingDraftData(
    @SerializedName("step")
    val step: Int,
    @SerializedName("data")
    val data: Map<String, Any?>? = null
)

data class SaveOnboardingDraftRequest(
    @SerializedName("step")
    val step: Int,
    @SerializedName("data")
    val data: Map<String, Any?>
)

