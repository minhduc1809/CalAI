package com.calai.app.domain.model

/**
 * Model đại diện cho thông tin người dùng
 */
data class User(
    val userId: String,
    val name: String,
    val age: Int,
    val height: Float, // cm
    val weight: Float, // kg
    val gender: String,
    val goal: String // Ví dụ: "Giảm cân", "Tăng cơ", "Duy trì"
)
