package com.calai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calai.app.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val age: Int,
    val height: Float,
    val weight: Float,
    val gender: String,
    val goal: String
)

// Extension function để chuyển đổi từ Entity sang Domain Model
fun UserEntity.toDomain() = User(
    userId = userId, name = name, age = age, height = height, weight = weight, gender = gender, goal = goal
)

// Extension function để chuyển đổi từ Domain Model sang Entity
fun User.toEntity() = UserEntity(
    userId = userId, name = name, age = age, height = height, weight = weight, gender = gender, goal = goal
)
