package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WaterTodayDto(
    @SerializedName("totalMl")
    val totalMl: Int,
    @SerializedName("goalMl")
    val goalMl: Int,
    @SerializedName("glassMl")
    val glassMl: Int
)
