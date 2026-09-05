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

data class CreateCustomFoodRequest(
    @SerializedName("name") val name: String,
    @SerializedName("servingSize") val servingSize: String? = null,
    @SerializedName("calories") val calories: Float,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carb") val carb: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f
)

data class CustomFoodDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("servingSize") val servingSize: String? = null,
    @SerializedName("calories") val calories: Float,
    @SerializedName("protein") val protein: Float = 0f,
    @SerializedName("carb") val carb: Float = 0f,
    @SerializedName("fat") val fat: Float = 0f
)

/** Chuyển CustomFoodDto sang FoodItemDto để tái dùng chung luồng chọn món trong AddMealScreen. */
fun CustomFoodDto.toFoodItemDto() = FoodItemDto(
    name = name,
    category = "Món của tôi",
    servingSize = servingSize ?: "1 phần",
    calories = calories,
    protein = protein,
    carb = carb,
    fat = fat
)
