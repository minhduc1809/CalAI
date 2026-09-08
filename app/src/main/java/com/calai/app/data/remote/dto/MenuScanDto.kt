package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MenuItemDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: String? = null,
    @SerializedName("estimatedCalories")
    val estimatedCalories: Int = 0,
    @SerializedName("protein")
    val protein: Int = 0,
    @SerializedName("carbs")
    val carbs: Int = 0,
    @SerializedName("fat")
    val fat: Int = 0,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("isRecommended")
    val isRecommended: Boolean = false,
    @SerializedName("recommendationReason")
    val recommendationReason: String? = null
)

data class ScanMenuResponseDto(
    @SerializedName("restaurantName")
    val restaurantName: String? = null,
    @SerializedName("items")
    val items: List<MenuItemDto> = emptyList(),
    @SerializedName("recommendedItems")
    val recommendedItems: List<MenuItemDto> = emptyList(),
    @SerializedName("summaryAdvice")
    val summaryAdvice: String = ""
)

data class ScanMenuBase64Request(
    @SerializedName("imageBase64")
    val imageBase64: String,
    @SerializedName("note")
    val note: String? = null
)
