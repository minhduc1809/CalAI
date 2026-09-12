package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InsightDto(
    @SerializedName("type")
    val type: String,
    @SerializedName("message")
    val message: String
)

data class InsightsData(
    @SerializedName("insights")
    val insights: List<InsightDto>
)

data class WeeklySummaryDto(
    @SerializedName("weekStartDate")
    val weekStartDate: String,
    @SerializedName("weekEndDate")
    val weekEndDate: String,
    @SerializedName("avgCalories")
    val avgCalories: Float?,
    @SerializedName("avgProtein")
    val avgProtein: Float?,
    @SerializedName("avgFat")
    val avgFat: Float?,
    @SerializedName("avgCarb")
    val avgCarb: Float?,
    @SerializedName("weightChangeKg")
    val weightChangeKg: Float?,
    @SerializedName("workoutsCompleted")
    val workoutsCompleted: Int?,
    @SerializedName("highlightText")
    val highlightText: String,
    @SerializedName("isFallback")
    val isFallback: Boolean,
    @SerializedName("generatedAt")
    val generatedAt: String
)
