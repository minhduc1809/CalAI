package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FoodItemDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("servingSize")
    val servingSize: String,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("protein")
    val protein: Float,
    @SerializedName("carb")
    val carb: Float,
    @SerializedName("fat")
    val fat: Float,
    @SerializedName("note")
    val note: String? = null
)

data class FoodSearchResultData(
    @SerializedName("total")
    val total: Int,
    @SerializedName("items")
    val items: List<FoodItemDto>
)

data class AddFavoriteFoodRequest(
    @SerializedName("foodName")
    val foodName: String
)
