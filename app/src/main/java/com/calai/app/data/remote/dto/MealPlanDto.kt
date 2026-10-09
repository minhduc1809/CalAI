package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Giá trị dinh dưỡng của MỘT phần món trong kho (để ghi vào nhật ký đúng quy ước calo-trên-1-quantity). */
data class MealPlanUnitDto(
    @SerializedName("calories") val calories: Float = 0f,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carb") val carb: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f
)

data class MealPlanItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("servingSize") val servingSize: String = "",
    @SerializedName("quantity") val quantity: Float = 1f,
    @SerializedName("unit") val unit: MealPlanUnitDto = MealPlanUnitDto(),
    @SerializedName("calories") val calories: Float = 0f,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carb") val carb: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f,
    // Món này đã được ghi vào nhật ký hôm nay chưa (chỉ có ở thực đơn hôm nay)
    @SerializedName("logged") val logged: Boolean = false
)

/** Một bữa trong thực đơn. status: PLANNED | PARTIAL | LOGGED (chỉ có ở thực đơn hôm nay). */
data class MealPlanSlotDto(
    @SerializedName("mealType") val mealType: String,
    @SerializedName("budgetCalories") val budgetCalories: Int = 0,
    @SerializedName("items") val items: List<MealPlanItemDto> = emptyList(),
    @SerializedName("calories") val calories: Float = 0f,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carb") val carb: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f,
    @SerializedName("withinTolerance") val withinTolerance: Boolean = true,
    @SerializedName("status") val status: String? = null,
    @SerializedName("consumedCalories") val consumedCalories: Int = 0,
    // Âm khi đã ăn vượt ngân sách của bữa
    @SerializedName("remainingCalories") val remainingCalories: Int = 0,
    @SerializedName("suggestionForRemaining") val suggestionForRemaining: MealPlanSlotDto? = null
)

data class MealPlanTotalsDto(
    @SerializedName("calories") val calories: Float = 0f,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carb") val carb: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f
)

data class MacroTargetsDto(
    @SerializedName("calories") val calories: Float? = null,
    @SerializedName("protein") val protein: Float? = null,
    @SerializedName("carb") val carb: Float? = null,
    @SerializedName("fat") val fat: Float? = null
)

/** GET meal-plan/today */
data class TodayMealPlanDto(
    @SerializedName("date") val date: String,
    @SerializedName("targetCalories") val targetCalories: Float? = null,
    @SerializedName("macroTargets") val macroTargets: MacroTargetsDto? = null,
    @SerializedName("meals") val meals: List<MealPlanSlotDto> = emptyList(),
    @SerializedName("totals") val totals: MealPlanTotalsDto = MealPlanTotalsDto(),
    @SerializedName("limitedChoices") val limitedChoices: Boolean = false,
    @SerializedName("message") val message: String? = null
)

/** Một ngày trong kế hoạch. locked = true thì không có nội dung (Free xem trước 7 ngày đầu của 30 ngày). */
data class MealPlanDayDto(
    @SerializedName("dayNumber") val dayNumber: Int,
    @SerializedName("date") val date: String,
    @SerializedName("locked") val locked: Boolean = false,
    @SerializedName("slots") val slots: List<MealPlanSlotDto> = emptyList(),
    @SerializedName("totals") val totals: MealPlanTotalsDto = MealPlanTotalsDto()
)

/** GET meal-plan?days=7|30 */
data class MealPlanDto(
    @SerializedName("startDate") val startDate: String = "",
    @SerializedName("targetCalories") val targetCalories: Float? = null,
    @SerializedName("macroTargets") val macroTargets: MacroTargetsDto? = null,
    @SerializedName("days") val days: List<MealPlanDayDto> = emptyList(),
    @SerializedName("previewDays") val previewDays: Int? = null,
    @SerializedName("lockedFromDay") val lockedFromDay: Int? = null,
    @SerializedName("limitedChoices") val limitedChoices: Boolean = false,
    @SerializedName("message") val message: String? = null
)
