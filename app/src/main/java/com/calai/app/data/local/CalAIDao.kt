package com.calai.app.data.local

import androidx.room.*
import com.calai.app.data.local.entity.MealEntity
import com.calai.app.data.local.entity.PendingCustomFoodEntity
import com.calai.app.data.local.entity.PendingFavoriteEntity
import com.calai.app.data.local.entity.PendingRequestEntity
import com.calai.app.data.local.entity.UserEntity
import com.calai.app.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalAIDao {
    // ── Hàng đợi ghi bữa ăn / buổi tập khi mất mạng ──
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPendingRequest(entity: PendingRequestEntity): Long

    @Query("SELECT * FROM pending_requests WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getPendingRequests(status: String): List<PendingRequestEntity>

    @Query("SELECT * FROM pending_requests ORDER BY createdAt ASC")
    fun observePendingRequests(): Flow<List<PendingRequestEntity>>

    @Update
    suspend fun updatePendingRequest(entity: PendingRequestEntity)

    @Query("DELETE FROM pending_requests WHERE localId = :localId")
    suspend fun deletePendingRequest(localId: Long)

    @Query("UPDATE pending_requests SET status = 'PENDING_SYNC', attempts = 0, lastError = NULL WHERE localId = :localId")
    suspend fun resetPendingRequest(localId: Long)

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

    @Query("SELECT * FROM pending_favorite_actions")
    suspend fun getPendingFavorites(): List<PendingFavoriteEntity>

    @Upsert
    suspend fun upsertPendingFavorite(entity: PendingFavoriteEntity)

    @Query("DELETE FROM pending_favorite_actions WHERE foodName = :foodName")
    suspend fun deletePendingFavorite(foodName: String)

    @Query("SELECT * FROM pending_custom_foods ORDER BY createdAt ASC")
    suspend fun getPendingCustomFoods(): List<PendingCustomFoodEntity>

    @Insert
    suspend fun insertPendingCustomFood(entity: PendingCustomFoodEntity): Long

    @Query("DELETE FROM pending_custom_foods WHERE localId = :localId")
    suspend fun deletePendingCustomFood(localId: Long)
}
