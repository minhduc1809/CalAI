package com.calai.app.data.remote.dto

data class FoodItemRecognitionDto(
    val name: String,
    val servingSize: String? = null,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carb: Double = 0.0,
    val fat: Double = 0.0
)

data class FoodRecognitionResultDto(
    val foodName: String = "",
    val confidenceScore: Double = 0.0,
    val servingSize: String = "",
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarb: Double = 0.0,
    val totalFat: Double = 0.0,
    val items: List<FoodItemRecognitionDto> = emptyList(),
    val healthTip: String = "",
    val isFallback: Boolean = false,
    val usedQuotaType: String? = null,
    val freeRemaining: Int? = null,
    val purchasedCredits: Int? = null,
    val totalRemaining: Int? = null,
    val remainingDailyQuota: Int? = null,
    val dailyLimit: Int? = null
)

data class RecognizeFoodBase64Request(
    val base64Image: String,
    val mimeType: String = "image/jpeg"
)
