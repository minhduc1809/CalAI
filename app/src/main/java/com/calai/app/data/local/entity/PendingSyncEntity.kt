package com.calai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calai.app.data.remote.dto.CreateCustomFoodRequest

/**
 * Hàng đợi các thay đổi món yêu thích chưa lên được server (mạng lỗi khi bấm). Trước đây các
 * lượt này chỉ nằm trong 1 mutableSet trong bộ nhớ tiến trình — mất hẳn khi mạng có lại nếu app
 * đã bị đóng, không bao giờ đồng bộ lên server. Giờ lưu Room để còn cơ hội gửi lại lần sau.
 */
@Entity(tableName = "pending_favorite_actions")
data class PendingFavoriteEntity(
    @PrimaryKey val foodName: String,
    val isAdd: Boolean, // true = thêm yêu thích, false = gỡ yêu thích
    val createdAt: Long = System.currentTimeMillis()
)

/** Món tự tạo mà request tạo lên server thất bại — giữ lại để gửi lại khi có mạng. */
@Entity(tableName = "pending_custom_foods")
data class PendingCustomFoodEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val name: String,
    val servingSize: String?,
    val servingAmount: Float?,
    val servingUnit: String?,
    val calories: Float,
    val protein: Float,
    val carb: Float,
    val fat: Float,
    val createdAt: Long = System.currentTimeMillis()
)

fun PendingCustomFoodEntity.toRequest() = CreateCustomFoodRequest(
    name = name,
    servingSize = servingSize,
    servingAmount = servingAmount,
    servingUnit = servingUnit,
    calories = calories,
    protein = protein,
    carb = carb,
    fat = fat
)
