package com.calai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calai.app.domain.model.WeightLog

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey val logId: String,
    val userId: String,
    val weight: Float,
    val date: Long
)

fun WeightLogEntity.toDomain() = WeightLog(
    logId = logId, userId = userId, weight = weight, date = date
)

fun WeightLog.toEntity() = WeightLogEntity(
    logId = logId, userId = userId, weight = weight, date = date
)
