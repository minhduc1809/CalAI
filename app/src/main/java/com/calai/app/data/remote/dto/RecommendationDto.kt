package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// --- DIET RECOMMENDATION (GET recommendations/diet) ---

data class VietnameseMealItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("serving") val serving: String,
    @SerializedName("calories") val calories: Float,
    @SerializedName("protein") val protein: Float,
    @SerializedName("carb") val carb: Float,
    @SerializedName("fat") val fat: Float,
    @SerializedName("note") val note: String? = null
)

data class MealBlockDto(
    @SerializedName("title") val title: String,
    @SerializedName("items") val items: List<VietnameseMealItemDto> = emptyList(),
    @SerializedName("totalCalories") val totalCalories: Float = 0f
)

data class DietMealsDto(
    @SerializedName("breakfast") val breakfast: MealBlockDto? = null,
    @SerializedName("lunch") val lunch: MealBlockDto? = null,
    @SerializedName("dinner") val dinner: MealBlockDto? = null,
    @SerializedName("snack") val snack: MealBlockDto? = null
)

data class MacroRatioDto(
    @SerializedName("proteinPercent") val proteinPercent: Int,
    @SerializedName("carbPercent") val carbPercent: Int,
    @SerializedName("fatPercent") val fatPercent: Int
)

data class VietnameseDietPlanDto(
    @SerializedName("id") val id: String,
    @SerializedName("goal") val goal: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("targetCalo") val targetCalo: Float,
    @SerializedName("macroRatio") val macroRatio: MacroRatioDto,
    @SerializedName("meals") val meals: DietMealsDto
)

data class DietOptionDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("targetCalo") val targetCalo: Float,
    @SerializedName("description") val description: String
)

data class UserDietTargetDto(
    @SerializedName("goal") val goal: String? = null,
    @SerializedName("targetCalories") val targetCalories: Float? = null,
    @SerializedName("targetProtein") val targetProtein: Float? = null,
    @SerializedName("targetCarb") val targetCarb: Float? = null,
    @SerializedName("targetFat") val targetFat: Float? = null
)

data class DietRecommendationData(
    @SerializedName("userTarget") val userTarget: UserDietTargetDto,
    @SerializedName("recommendedPlan") val recommendedPlan: VietnameseDietPlanDto,
    @SerializedName("availableOptions") val availableOptions: List<DietOptionDto> = emptyList()
)

// --- WORKOUT RECOMMENDATION (GET recommendations/workout) ---

data class WorkoutExerciseItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("targetMuscle") val targetMuscle: String,
    @SerializedName("sets") val sets: Int,
    @SerializedName("repsOrDuration") val repsOrDuration: String,
    @SerializedName("restSeconds") val restSeconds: Int,
    @SerializedName("caloriesBurnedEstimate") val caloriesBurnedEstimate: Float,
    @SerializedName("instructions") val instructions: String
)

data class DayWorkoutPlanDto(
    @SerializedName("dayName") val dayName: String,
    @SerializedName("focus") val focus: String,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int,
    @SerializedName("exercises") val exercises: List<WorkoutExerciseItemDto> = emptyList()
)

data class WorkoutTemplatePlanDto(
    @SerializedName("id") val id: String,
    @SerializedName("goal") val goal: String,
    @SerializedName("level") val level: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("suitableForBmi") val suitableForBmi: String,
    @SerializedName("weeklySchedule") val weeklySchedule: List<DayWorkoutPlanDto> = emptyList()
)

data class WorkoutOptionDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("goal") val goal: String,
    @SerializedName("level") val level: String,
    @SerializedName("suitableForBmi") val suitableForBmi: String
)

data class UserWorkoutProfileDto(
    @SerializedName("bmi") val bmi: Float? = null,
    @SerializedName("goal") val goal: String? = null,
    @SerializedName("activityLevel") val activityLevel: String? = null
)

data class WorkoutRecommendationData(
    @SerializedName("userProfile") val userProfile: UserWorkoutProfileDto,
    @SerializedName("recommendedWorkout") val recommendedWorkout: WorkoutTemplatePlanDto,
    @SerializedName("allWorkoutPlans") val allWorkoutPlans: List<WorkoutOptionDto> = emptyList()
)

// --- EXERCISE LIBRARY (GET recommendations/exercises) ---

data class ExerciseInstructionsDto(
    @SerializedName("preparation") val preparation: String,
    @SerializedName("execution") val execution: String,
    @SerializedName("commonMistakes") val commonMistakes: String,
    @SerializedName("breathing") val breathing: String
)

data class ExerciseGuideDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("genderTarget") val genderTarget: String,
    @SerializedName("level") val level: String,
    @SerializedName("targetMuscle") val targetMuscle: String,
    @SerializedName("equipment") val equipment: String,
    @SerializedName("sets") val sets: Int,
    @SerializedName("repsOrDuration") val repsOrDuration: String,
    @SerializedName("restSeconds") val restSeconds: Int,
    @SerializedName("caloriesBurnedEstimate") val caloriesBurnedEstimate: Float,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("instructions") val instructions: ExerciseInstructionsDto
)

data class LevelsSummaryDto(
    @SerializedName("beginner") val beginner: Int = 0,
    @SerializedName("intermediate") val intermediate: Int = 0,
    @SerializedName("advanced") val advanced: Int = 0
)

// --- MONTHLY DIET (GET recommendations/diet/monthly) ---

data class MacroSummaryDto(
    @SerializedName("proteinGrams") val proteinGrams: Float,
    @SerializedName("carbGrams") val carbGrams: Float,
    @SerializedName("fatGrams") val fatGrams: Float,
    @SerializedName("proteinRatio") val proteinRatio: Int,
    @SerializedName("carbRatio") val carbRatio: Int,
    @SerializedName("fatRatio") val fatRatio: Int
)

data class MonthDietPlanItemDto(
    @SerializedName("dayNumber") val dayNumber: Int,
    @SerializedName("dayTitle") val dayTitle: String,
    @SerializedName("goal") val goal: String,
    @SerializedName("experienceLevel") val experienceLevel: String,
    @SerializedName("suitableForWho") val suitableForWho: String,
    @SerializedName("phaseName") val phaseName: String,
    @SerializedName("focusMessage") val focusMessage: String,
    @SerializedName("targetCalories") val targetCalories: Float,
    @SerializedName("macroSummary") val macroSummary: MacroSummaryDto,
    @SerializedName("meals") val meals: DietMealsDto
)

data class MonthlyDietData(
    @SerializedName("goal") val goal: String,
    @SerializedName("experienceLevel") val experienceLevel: String,
    @SerializedName("totalDays") val totalDays: Int? = null,
    @SerializedName("monthlyPlans") val monthlyPlans: List<MonthDietPlanItemDto>? = null,
    @SerializedName("dayPlan") val dayPlan: MonthDietPlanItemDto? = null
)

data class ExerciseListData(
    @SerializedName("gender") val gender: String,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("filteredCount") val filteredCount: Int,
    @SerializedName("levelsSummary") val levelsSummary: LevelsSummaryDto,
    @SerializedName("exercises") val exercises: List<ExerciseGuideDto> = emptyList()
)
