package com.calai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.calai.app.data.local.entity.MealEntity
import com.calai.app.data.local.entity.UserEntity
import com.calai.app.data.local.entity.WeightLogEntity

@Database(
    entities = [UserEntity::class, MealEntity::class, WeightLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CalAIDatabase : RoomDatabase() {
    abstract val dao: CalAIDao
}
