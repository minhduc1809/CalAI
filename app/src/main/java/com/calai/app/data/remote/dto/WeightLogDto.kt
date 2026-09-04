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
