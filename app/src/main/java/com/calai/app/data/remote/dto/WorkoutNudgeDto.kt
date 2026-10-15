package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** GET workouts/log-nudge: nhắc "Bạn vừa tập?" khi người tập đều có dấu hiệu quên ghi buổi tập. */
data class WorkoutNudgeDto(
    @SerializedName("show") val show: Boolean = false,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("daysSinceLast") val daysSinceLast: Int? = null,
    @SerializedName("averageGapDays") val averageGapDays: Float? = null,
    @SerializedName("lastWorkout") val lastWorkout: NudgeLastWorkoutDto? = null,
    @SerializedName("message") val message: String? = null
)

data class NudgeLastWorkoutDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("date") val date: String
)
