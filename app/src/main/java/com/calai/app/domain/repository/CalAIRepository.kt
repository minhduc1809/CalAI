package com.calai.app.domain.repository

import com.calai.app.domain.model.Meal
import com.calai.app.domain.model.User
import com.calai.app.domain.model.WeightLog
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository định nghĩa các phương thức thao tác dữ liệu
 * Tuân thủ Clean Architecture: Tầng Domain không phụ thuộc vào Room hay Retrofit
 */
interface CalAIRepository {
    // Thao tác với User
    fun getUser(userId: String): Flow<User?>
    suspend fun saveUser(user: User)

    // Thao tác với Meal
    fun getMeals(userId: String): Flow<List<Meal>>
    suspend fun insertMeal(meal: Meal)
    suspend fun deleteMeal(meal: Meal)

    // Thao tác với WeightLog
    fun getWeightLogs(userId: String): Flow<List<WeightLog>>
    suspend fun insertWeightLog(log: WeightLog)
}
