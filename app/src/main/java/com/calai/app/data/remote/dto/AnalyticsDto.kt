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
