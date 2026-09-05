package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateMealItemDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("servingSize")
    val servingSize: String? = null,
    @SerializedName("quantity")
    val quantity: Float = 1f,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("protein")
    val protein: Float = 0f,
    @SerializedName("carb")
    val carb: Float = 0f,
    @SerializedName("fat")
    val fat: Float = 0f,
    @SerializedName("source")
    val source: String = "manual"
)

data class CreateMealRequest(
    @SerializedName("mealType")
    val mealType: String, // BREAKFAST, LUNCH, DINNER, SNACK
    @SerializedName("date")
    val date: String, // YYYY-MM-DD
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("items")
    val items: List<CreateMealItemDto>
)

data class QuickAddMealRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("mealType")
    val mealType: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("protein")
    val protein: Float = 0f,
    @SerializedName("carb")
    val carb: Float = 0f,
    @SerializedName("fat")
    val fat: Float = 0f
)

data class MealItemResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("mealId")
    val mealId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("servingSize")
    val servingSize: String? = null,
    @SerializedName("quantity")
    val quantity: Float,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("protein")
    val protein: Float,
    @SerializedName("carb")
    val carb: Float,
    @SerializedName("fat")
    val fat: Float,
    @SerializedName("source")
    val source: String
)

data class MealResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("mealType")
    val mealType: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("totalCalories")
    val totalCalories: Float,
    @SerializedName("totalProtein")
    val totalProtein: Float,
    @SerializedName("totalCarb")
    val totalCarb: Float,
    @SerializedName("totalFat")
    val totalFat: Float,
    @SerializedName("items")
    val items: List<MealItemResponseDto> = emptyList(),
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class MacroDetailDto(
    @SerializedName("consumed")
    val consumed: Float,
    @SerializedName("target")
    val target: Float,
    @SerializedName("unit")
    val unit: String
)

data class MacrosSummaryDto(
    @SerializedName("protein")
    val protein: MacroDetailDto,
    @SerializedName("carb")
    val carb: MacroDetailDto,
    @SerializedName("fat")
    val fat: MacroDetailDto
)

data class DailySummaryDto(
    @SerializedName("consumedCalories")
    val consumedCalories: Float,
    @SerializedName("targetCalories")
    val targetCalories: Float,
    @SerializedName("remainingCalories")
    val remainingCalories: Float,
    @SerializedName("progressPercent")
    val progressPercent: Int,
    @SerializedName("macros")
    val macros: MacrosSummaryDto
)

data class DailyNutritionSummaryData(
    @SerializedName("date")
    val date: String,
    @SerializedName("summary")
    val summary: DailySummaryDto,
    @SerializedName("mealsCount")
    val mealsCount: Int,
    @SerializedName("meals")
    val meals: List<MealResponseDto> = emptyList()
)

data class StatisticsPeriodDto(
    @SerializedName("start")
    val start: String,
    @SerializedName("end")
    val end: String
)

data class StatisticsAveragesDto(
    @SerializedName("dailyCalories")
    val dailyCalories: Float,
    @SerializedName("dailyProtein")
    val dailyProtein: Float,
    @SerializedName("dailyCarb")
    val dailyCarb: Float,
    @SerializedName("dailyFat")
    val dailyFat: Float
)

data class DailyStatDto(
    @SerializedName("date")
    val date: String,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("protein")
    val protein: Float,
    @SerializedName("carb")
    val carb: Float,
    @SerializedName("fat")
    val fat: Float,
    @SerializedName("mealsCount")
    val mealsCount: Int
)

/** Thống kê dinh dưỡng theo dải ngày — trả về từ GET /meals/statistics. */
data class NutritionStatisticsData(
    @SerializedName("period")
    val period: StatisticsPeriodDto,
    @SerializedName("averages")
    val averages: StatisticsAveragesDto,
    @SerializedName("dailyStats")
    val dailyStats: List<DailyStatDto>
)
