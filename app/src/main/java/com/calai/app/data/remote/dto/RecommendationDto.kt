package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

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

data class ExerciseListData(
    @SerializedName("gender") val gender: String,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("filteredCount") val filteredCount: Int,
    @SerializedName("levelsSummary") val levelsSummary: LevelsSummaryDto,
    @SerializedName("exercises") val exercises: List<ExerciseGuideDto> = emptyList()
)
