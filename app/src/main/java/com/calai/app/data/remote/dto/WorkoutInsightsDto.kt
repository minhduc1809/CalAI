package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Lịch sử, thống kê, kỷ lục (BR-10.8) ──

data class HistoryDayDto(
    @SerializedName("date") val date: String,
    @SerializedName("sessions") val sessions: Int = 0,
    @SerializedName("minutes") val minutes: Int = 0,
    @SerializedName("calories") val calories: Int = 0
)

data class HistorySessionDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("date") val date: String,
    @SerializedName("durationMinutes") val durationMinutes: Int = 0,
    @SerializedName("caloriesBurned") val caloriesBurned: Float = 0f,
    @SerializedName("exerciseCount") val exerciseCount: Int = 0,
    @SerializedName("setCount") val setCount: Int = 0
)

data class WorkoutHistoryDto(
    @SerializedName("month") val month: String,
    @SerializedName("days") val days: List<HistoryDayDto> = emptyList(),
    @SerializedName("sessions") val sessions: List<HistorySessionDto> = emptyList()
)

/** Một set của lần tập gần nhất, dùng để điền sẵn số lần và tạ. */
data class LastSetDto(
    @SerializedName("setNumber") val setNumber: Int = 1,
    @SerializedName("reps") val reps: Int = 0,
    @SerializedName("weightKg") val weightKg: Float = 0f,
    @SerializedName("isWarmup") val isWarmup: Boolean = false
)

data class LastSetsDto(
    @SerializedName("key") val key: String = "",
    @SerializedName("date") val date: String? = null,
    @SerializedName("sets") val sets: List<LastSetDto> = emptyList()
)

data class VolumeWeekDto(
    @SerializedName("weekStart") val weekStart: String,
    @SerializedName("volume") val volume: Float = 0f,
    @SerializedName("sets") val sets: Int = 0
)

data class VolumeStatsDto(
    @SerializedName("weeks") val weeks: Int = 0,
    @SerializedName("items") val items: List<VolumeWeekDto> = emptyList()
)

data class MuscleSetsDto(
    @SerializedName("muscle") val muscle: String,
    @SerializedName("sets") val sets: Int = 0
)

data class SetsPerMuscleDto(
    @SerializedName("weeks") val weeks: Int = 0,
    @SerializedName("items") val items: List<MuscleSetsDto> = emptyList()
)

data class RecordItemDto(
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: Float = 0f,
    @SerializedName("reps") val reps: Int? = null,
    @SerializedName("weightKg") val weightKg: Float? = null,
    @SerializedName("workoutId") val workoutId: String? = null,
    @SerializedName("achievedAt") val achievedAt: String? = null
)

data class ExerciseRecordsDto(
    @SerializedName("exerciseKey") val exerciseKey: String,
    @SerializedName("exerciseName") val exerciseName: String,
    @SerializedName("records") val records: List<RecordItemDto> = emptyList()
)

data class RecordsDto(
    @SerializedName("items") val items: List<ExerciseRecordsDto> = emptyList()
)

// ── Buổi tập mẫu, lặp lại buổi gần nhất, bài tự tạo (BR-10.3, BR-10.6) ──

data class TemplateExerciseDto(
    @SerializedName("name") val name: String,
    @SerializedName("exerciseSource") val exerciseSource: String? = null,
    @SerializedName("exerciseId") val exerciseId: String? = null,
    @SerializedName("primaryMuscle") val primaryMuscle: String? = null,
    @SerializedName("restSec") val restSec: Int? = null,
    @SerializedName("sets") val sets: List<LastSetDto> = emptyList()
)

data class WorkoutTemplateDto(
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("durationMinutes") val durationMinutes: Int? = null,
    @SerializedName("exercises") val exercises: List<TemplateExerciseDto> = emptyList()
)

data class LastTemplateDto(
    @SerializedName("template") val template: WorkoutTemplateDto? = null
)

data class RoutineItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("exerciseSource") val exerciseSource: String? = null,
    @SerializedName("exerciseId") val exerciseId: String? = null,
    @SerializedName("primaryMuscle") val primaryMuscle: String? = null,
    @SerializedName("restSec") val restSec: Int? = null,
    @SerializedName("sets") val sets: List<LastSetDto> = emptyList()
)

data class RoutineDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String = "STRENGTH",
    @SerializedName("items") val items: List<RoutineItemDto> = emptyList()
)

data class RoutineFromWorkoutRequest(
    @SerializedName("name") val name: String? = null
)

data class CustomExerciseDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("primaryMuscle") val primaryMuscle: String = "",
    @SerializedName("equipment") val equipment: String? = null,
    @SerializedName("kind") val kind: String? = null
)

data class CreateCustomExerciseRequest(
    @SerializedName("name") val name: String,
    @SerializedName("primaryMuscle") val primaryMuscle: String
)

// ── Đối chiếu số buổi tập thực tế (BR-10.7) ──

data class ActivityCheckDto(
    @SerializedName("eligible") val eligible: Boolean = false,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("mismatch") val mismatch: Boolean? = null,
    @SerializedName("declaredDays") val declaredDays: Int? = null,
    @SerializedName("averagePerWeek") val averagePerWeek: Float? = null,
    @SerializedName("weeklyCounts") val weeklyCounts: List<Int>? = null,
    @SerializedName("suggestedTrainingDays") val suggestedTrainingDays: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("note") val note: String? = null
)
