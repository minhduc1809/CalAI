package com.calai.app.data.local

import androidx.room.*
import com.calai.app.data.local.entity.MealEntity
import com.calai.app.data.local.entity.UserEntity
import com.calai.app.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalAIDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUser(userId: String): Flow<UserEntity?>

    @Upsert
    suspend fun saveUser(user: UserEntity)

    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY timestamp DESC")
    fun getMeals(userId: String): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("SELECT * FROM weight_logs WHERE userId = :userId ORDER BY date DESC")
    fun getWeightLogs(userId: String): Flow<List<WeightLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLogEntity)
}
