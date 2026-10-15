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
    // RPE theo bước 0,5 (BR-10.4): máy chủ trả số thực nên KHÔNG được khai báo Int (Gson sẽ lỗi với 8.5)
    @SerializedName("rpe") val rpe: Float? = null,
    @SerializedName("isCompleted") val isCompleted: Boolean = true,
    /** Set khởi động: không tính volume và kỷ lục. */
    @SerializedName("isWarmup") val isWarmup: Boolean = false,
    @SerializedName("durationSec") val durationSec: Int? = null,
    @SerializedName("distanceM") val distanceM: Float? = null
)

/**
 * DTO đại diện cho 1 bài tập trong buổi
 */
data class WorkoutExerciseDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("order") val order: Int? = 0,
    @SerializedName("exerciseSource") val exerciseSource: String? = null,
    @SerializedName("exerciseRefId") val exerciseRefId: String? = null,
    @SerializedName("primaryMuscle") val primaryMuscle: String? = null,
    @SerializedName("restSec") val restSec: Int? = null,
    @SerializedName("exerciseKey") val exerciseKey: String? = null,
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
    @SerializedName("clientRequestId") val clientRequestId: String? = null,
    @SerializedName("startedAt") val startedAt: String? = null,
    @SerializedName("endedAt") val endedAt: String? = null,
    /** Chỉ có ở phản hồi ghi buổi tập: kỷ lục mới và cảnh báo mềm (tạ tăng đột biến). */
    @SerializedName("newRecords") val newRecords: List<NewRecordDto>? = null,
    @SerializedName("warnings") val warnings: List<WeightJumpWarningDto>? = null,
    @SerializedName("deduplicated") val deduplicated: Boolean? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

/** Kỷ lục cá nhân vừa đạt. type: MAX_WEIGHT | MAX_REPS_AT_WEIGHT | EST_1RM | MAX_REPS_BODYWEIGHT. */
data class NewRecordDto(
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: Float,
    @SerializedName("reps") val reps: Int? = null,
    @SerializedName("weightKg") val weightKg: Float? = null,
    @SerializedName("exerciseKey") val exerciseKey: String? = null,
    @SerializedName("exerciseName") val exerciseName: String? = null
)

/** Cảnh báo mềm: tạ gấp hơn 2 lần lần trước (hay nhầm kg và lb). Không chặn việc lưu. */
data class WeightJumpWarningDto(
    @SerializedName("name") val name: String,
    @SerializedName("previousKg") val previousKg: Float,
    @SerializedName("currentKg") val currentKg: Float
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
    /** Mã tạo lúc bấm Lưu: gửi lại bao nhiêu lần máy chủ vẫn chỉ ghi một buổi (BR-10.4). */
    @SerializedName("clientRequestId") val clientRequestId: String? = null,
    @SerializedName("startedAt") val startedAt: String? = null,
    @SerializedName("endedAt") val endedAt: String? = null,
    @SerializedName("exercises") val exercises: List<CreateWorkoutExerciseRequest>? = null
)

data class CreateWorkoutExerciseRequest(
    @SerializedName("name") val name: String,
    @SerializedName("order") val order: Int = 0,
    @SerializedName("exerciseSource") val exerciseSource: String? = null,
    @SerializedName("exerciseId") val exerciseId: String? = null,
    @SerializedName("primaryMuscle") val primaryMuscle: String? = null,
    @SerializedName("restSec") val restSec: Int? = null,
    @SerializedName("sets") val sets: List<CreateWorkoutSetRequest>
)

data class CreateWorkoutSetRequest(
    @SerializedName("setNumber") val setNumber: Int,
    @SerializedName("reps") val reps: Int,
    @SerializedName("weightKg") val weightKg: Float,
    @SerializedName("rpe") val rpe: Float? = null,
    @SerializedName("isWarmup") val isWarmup: Boolean? = null,
    @SerializedName("durationSec") val durationSec: Int? = null,
    @SerializedName("distanceM") val distanceM: Float? = null
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
