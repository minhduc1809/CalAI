package com.calai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calai.app.domain.model.Meal

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey val mealId: String,
    val userId: String,
    val foodName: String,
    val calories: Float,
    val protein: Float,
    val carb: Float,
    val fat: Float,
    val timestamp: Long,
    val source: String
)

fun MealEntity.toDomain() = Meal(
    mealId = mealId, userId = userId, foodName = foodName, calories = calories,
    protein = protein, carb = carb, fat = fat, timestamp = timestamp, source = source
)

fun Meal.toEntity() = MealEntity(
    mealId = mealId, userId = userId, foodName = foodName, calories = calories,
    protein = protein, carb = carb, fat = fat, timestamp = timestamp, source = source
)
