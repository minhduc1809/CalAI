package com.calai.app.domain.model

/**
 * Model đại diện cho một bữa ăn/món ăn
 */
data class Meal(
    val mealId: String,
    val userId: String,
    val foodName: String,
    val calories: Float,
    val protein: Float,
    val carb: Float,
    val fat: Float,
    val timestamp: Long,
    val source: String // Ví dụ: "AI", "Manual"
)
