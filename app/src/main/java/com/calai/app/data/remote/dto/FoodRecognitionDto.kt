package com.calai.app.data.remote.dto

/** Món gần nhất trong kho để người dùng đổi khi AI nhận sai (một khẩu phần chuẩn của kho). */
data class FoodAlternativeDto(
    val name: String,
    val servingSize: String? = null,
    val sourceId: String? = null,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carb: Double = 0.0,
    val fat: Double = 0.0
)

data class FoodItemRecognitionDto(
    val name: String,
    val servingSize: String? = null,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carb: Double = 0.0,
    val fat: Double = 0.0,
    /** AI_MATCHED: số liệu lấy từ kho món. AI_ESTIMATE: ước tính, phải hiện nhãn "Ước tính". */
    val sourceType: String? = null,
    val sourceId: String? = null,
    val isEstimate: Boolean = false,
    val confidence: Double? = null,
    val confidenceLevel: String? = null,
    val atwaterAdjusted: Boolean = false,
    val alternatives: List<FoodAlternativeDto> = emptyList()
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
    /** Món AI thấy nhưng không ước tính được số liệu: người dùng tự tìm trong kho món. */
    val unrecognized: List<String> = emptyList(),
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
