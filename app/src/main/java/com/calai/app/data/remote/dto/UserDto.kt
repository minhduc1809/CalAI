package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("heightCm")
    val heightCm: Float? = null,
    @SerializedName("weightKg")
    val weightKg: Float? = null,
    @SerializedName("activityLevel")
    val activityLevel: String? = null,
    @SerializedName("goal")
    val goal: String? = null,
    @SerializedName("targetWeightKg")
    val targetWeightKg: Float? = null,
    @SerializedName("weightRateKgPerWeek")
    val weightRateKgPerWeek: Float? = null,
    @SerializedName("bodyFatPercent")
    val bodyFatPercent: Float? = null,
    @SerializedName("macroStyle")
    val macroStyle: String? = null,
    @SerializedName("bmi")
    val bmi: Float? = null,
    @SerializedName("bmr")
    val bmr: Float? = null,
    @SerializedName("tdee")
    val tdee: Float? = null,
    @SerializedName("targetCalories")
    val targetCalories: Float? = null,
    @SerializedName("targetProtein")
    val targetProtein: Float? = null,
    @SerializedName("targetCarb")
    val targetCarb: Float? = null,
    @SerializedName("targetFat")
    val targetFat: Float? = null,
    @SerializedName("adaptiveExpenditure")
    val adaptiveExpenditure: Float? = null,
    @SerializedName("expenditureStatus")
    val expenditureStatus: String? = null,
    @SerializedName("dailyAiQuota")
    val dailyAiQuota: Int? = null,
    @SerializedName("timezone")
    val timezone: String? = null
)

/** Trạng thái Adaptive Expenditure Engine — GET /users/me/expenditure. */
data class ExpenditureStatusDto(
    @SerializedName("method")
    val method: String, // ADAPTIVE | STATIC_FALLBACK
    @SerializedName("status")
    val status: String, // UPDATING | HOLDING
    @SerializedName("estimatedExpenditure")
    val estimatedExpenditure: Float? = null,
    @SerializedName("staticTdee")
    val staticTdee: Float? = null,
    @SerializedName("windowDays")
    val windowDays: Int = 0,
    @SerializedName("weightLogsCount")
    val weightLogsCount: Int = 0,
    @SerializedName("loggedDaysCount")
    val loggedDaysCount: Int = 0,
    @SerializedName("trendWeightStart")
    val trendWeightStart: Float? = null,
    @SerializedName("trendWeightEnd")
    val trendWeightEnd: Float? = null,
    @SerializedName("avgDailyCaloriesConsumed")
    val avgDailyCaloriesConsumed: Float? = null,
    @SerializedName("message")
    val message: String
)

data class UpdateProfileRequest(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("heightCm")
    val heightCm: Float? = null,
    @SerializedName("weightKg")
    val weightKg: Float? = null,
    @SerializedName("activityLevel")
    val activityLevel: String? = null,
    @SerializedName("goal")
    val goal: String? = null,
    @SerializedName("targetWeightKg")
    val targetWeightKg: Float? = null,
    @SerializedName("weightRateKgPerWeek")
    val weightRateKgPerWeek: Float? = null,
    @SerializedName("bodyFatPercent")
    val bodyFatPercent: Float? = null,
    @SerializedName("macroStyle")
    val macroStyle: String? = null
)
