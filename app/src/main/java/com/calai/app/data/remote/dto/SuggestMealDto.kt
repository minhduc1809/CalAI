package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NutritionGapDto(
    @SerializedName("remainingCalories")
    val remainingCalories: Int = 0,
    @SerializedName("remainingProtein")
    val remainingProtein: Int = 0,
    @SerializedName("remainingCarbs")
    val remainingCarbs: Int = 0,
    @SerializedName("remainingFat")
    val remainingFat: Int = 0
)

data class SuggestedMealItemDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("mealType")
    val mealType: String = "Bữa ăn",
    @SerializedName("calories")
    val calories: Int = 0,
    @SerializedName("protein")
    val protein: Int = 0,
    @SerializedName("carbs")
    val carbs: Int = 0,
    @SerializedName("fat")
    val fat: Int = 0,
    @SerializedName("reason")
    val reason: String = "",
    @SerializedName("ingredients")
    val ingredients: List<String> = emptyList()
)

data class SuggestMealResponseDto(
    @SerializedName("nutritionGap")
    val nutritionGap: NutritionGapDto,
    @SerializedName("suggestions")
    val suggestions: List<SuggestedMealItemDto> = emptyList(),
    @SerializedName("advice")
    val advice: String = ""
)
