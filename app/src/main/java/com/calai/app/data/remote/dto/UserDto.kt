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
    @SerializedName("dailyAiQuota")
    val dailyAiQuota: Int? = null,
    @SerializedName("timezone")
    val timezone: String? = null
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
    val goal: String? = null
)
