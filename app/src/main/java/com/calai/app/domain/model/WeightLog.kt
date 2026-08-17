package com.calai.app.domain.model

/**
 * Model đại diện cho lịch sử ghi chép cân nặng
 */
data class WeightLog(
    val logId: String,
    val userId: String,
    val weight: Float,
    val date: Long
)
