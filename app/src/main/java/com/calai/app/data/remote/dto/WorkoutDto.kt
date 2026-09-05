package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Phân loại bài tập & hoạt động thể chất
 */
enum class WorkoutCategory(val apiName: String, val displayName: String) {
    @SerializedName("STRENGTH")
    STRENGTH("STRENGTH", "Tập tạ / Kháng lực"),

    @SerializedName("CARDIO")
    CARDIO("CARDIO", "Cardio tổng hợp"),

    @SerializedName("RUNNING")
    RUNNING("RUNNING", "Chạy bộ"),

    @SerializedName("CYCLING")
    CYCLING("CYCLING", "Đạp xe"),

    @SerializedName("SWIMMING")
    SWIMMING("SWIMMING", "Bơi lội"),

    @SerializedName("HIIT")
    HIIT("HIIT", "HIIT / Tabata"),

    @SerializedName("WALKING")
    WALKING("WALKING", "Đi bộ"),

    @SerializedName("YOGA")
    YOGA("YOGA", "Yoga / Giãn cơ"),

    @SerializedName("SPORTS")
    SPORTS("SPORTS", "Thể thao đối kháng"),

    @SerializedName("OTHER")
    OTHER("OTHER", "Vận động khác")
}

/**
 * DTO đại diện cho 1 hiệp tập (Set)
 */
data class WorkoutSetDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("setNumber") val setNumber: Int,
    @SerializedName("reps") val reps: Int,
    @SerializedName("weightKg") val weightKg: Float,
    @SerializedName("rpe") val rpe: Int? = null,
    @SerializedName("isCompleted") val isCompleted: Boolean = true
)

/**
 * DTO đại diện cho 1 bài tập trong buổi
 */
data class WorkoutExerciseDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("order") val order: Int? = 0,
    @SerializedName("sets") val sets: List<WorkoutSetDto> = emptyList()
)

/**
 * DTO bản ghi buổi tập đầy đủ
 */
data class WorkoutLogDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: WorkoutCategory,
    @SerializedName("date") val date: String,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("caloriesBurned") val caloriesBurned: Float,
    @SerializedName("rpe") val rpe: Int? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("exercises") val exercises: List<WorkoutExerciseDto> = emptyList(),
    @SerializedName("totalVolumeKg") val totalVolumeKg: Float? = 0f,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

/**
 * Request tạo buổi tập
 */
data class CreateWorkoutLogRequest(
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("date") val date: String? = null,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("caloriesBurned") val caloriesBurned: Float? = null,
    @SerializedName("rpe") val rpe: Int? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("exercises") val exercises: List<CreateWorkoutExerciseRequest>? = null
)

data class CreateWorkoutExerciseRequest(
    @SerializedName("name") val name: String,
    @SerializedName("order") val order: Int = 0,
    @SerializedName("sets") val sets: List<CreateWorkoutSetRequest>
)

data class CreateWorkoutSetRequest(
    @SerializedName("setNumber") val setNumber: Int,
    @SerializedName("reps") val reps: Int,
    @SerializedName("weightKg") val weightKg: Float,
    @SerializedName("rpe") val rpe: Int? = null
)

/**
 * Request cập nhật buổi tập
 */
data class UpdateWorkoutLogRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("durationMinutes") val durationMinutes: Int? = null,
    @SerializedName("caloriesBurned") val caloriesBurned: Float? = null,
    @SerializedName("rpe") val rpe: Int? = null,
    @SerializedName("note") val note: String? = null
)

/**
 * DTO tổng kết vận động trong ngày
 */
data class WorkoutSummaryDto(
    @SerializedName("date") val date: String,
    @SerializedName("totalActiveCalories") val totalActiveCalories: Int,
    @SerializedName("totalDurationMinutes") val totalDurationMinutes: Int,
    @SerializedName("workoutCount") val workoutCount: Int,
    @SerializedName("categories") val categories: List<String> = emptyList()
)

/**
 * DTO danh mục loại bài tập và hệ số MET
 */
data class WorkoutCategoryInfoDto(
    @SerializedName("category") val category: WorkoutCategory,
    @SerializedName("nameVi") val nameVi: String,
    @SerializedName("met") val met: Float,
    @SerializedName("description") val description: String
)
