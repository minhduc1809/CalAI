package com.calai.app.domain.repository

import com.calai.app.data.remote.dto.*
import com.calai.app.domain.model.Meal
import com.calai.app.domain.model.User
import com.calai.app.domain.model.WeightLog
import kotlinx.coroutines.flow.Flow

/**
 * Interface Repository định nghĩa các phương thức thao tác dữ liệu
 * Kết hợp Local Cache (Room) và Remote API (NestJS Backend)
 */
interface CalAIRepository {
    // --- Local Database (Offline First) ---
    fun getUser(userId: String): Flow<User?>
    suspend fun saveUser(user: User)

    fun getMeals(userId: String): Flow<List<Meal>>
    suspend fun insertMeal(meal: Meal)
    suspend fun deleteMeal(meal: Meal)

    fun getWeightLogs(userId: String): Flow<List<WeightLog>>
    suspend fun insertWeightLog(log: WeightLog)

    // --- Authentication ---
    suspend fun login(username: String, password: String): Result<AuthResponseData>
    suspend fun register(username: String, email: String?, password: String, name: String?): Result<AuthResponseData>
    suspend fun logout(): Result<Unit>
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    fun getCurrentUsername(): String?

    // --- User Profile ---
    suspend fun fetchRemoteProfile(): Result<UserProfileDto>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfileDto>

    // --- Meals Remote & Sync ---
    suspend fun fetchDailySummary(date: String? = null): Result<DailyNutritionSummaryData>
    suspend fun fetchMealsFromRemote(date: String? = null): Result<List<MealResponseDto>>
    suspend fun createRemoteMeal(request: CreateMealRequest): Result<MealResponseDto>
    suspend fun deleteRemoteMeal(mealId: String): Result<Unit>

    // --- Food Database & Recommendations ---
    suspend fun searchFoods(query: String? = null, category: String? = null): Result<List<FoodItemDto>>
    suspend fun getFoodCategories(): Result<List<String>>

    // --- Weight Logs Remote ---
    suspend fun createRemoteWeightLog(weightKg: Float, note: String? = null): Result<WeightLogResponseDto>
    suspend fun fetchRemoteWeightLogs(limit: Int = 30): Result<List<WeightLogResponseDto>>
}
