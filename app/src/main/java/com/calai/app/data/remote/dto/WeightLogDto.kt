package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateWeightLogRequest(
    @SerializedName("weightKg")
    val weightKg: Float,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("date")
    val date: String? = null
)

data class WeightLogResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("weightKg")
    val weightKg: Float,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("date")
    val date: String
)

/** Một điểm trên đường Trend Weight (EWMA, alpha = 0.1) do backend tính sẵn. */
data class WeightTrendPointDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("loggedWeight")
    val loggedWeight: Float,
    @SerializedName("trendWeight")
    val trendWeight: Float,
    @SerializedName("note")
    val note: String? = null
)

/** Tiến độ hoàn thành mục tiêu cân nặng: mốc bắt đầu vs hiện tại vs mục tiêu. */
data class WeightProgressDto(
    @SerializedName("goal")
    val goal: String? = null,
    @SerializedName("startWeightKg")
    val startWeightKg: Float? = null,
    @SerializedName("currentWeightKg")
    val currentWeightKg: Float? = null,
    @SerializedName("targetWeightKg")
    val targetWeightKg: Float? = null,
    @SerializedName("weightChangedKg")
    val weightChangedKg: Float = 0f,
    @SerializedName("remainingToGoalKg")
    val remainingToGoalKg: Float = 0f,
    @SerializedName("progressPercent")
    val progressPercent: Int = 0
)
