package com.calai.app.domain.repository

import com.calai.app.data.remote.dto.*
import com.calai.app.domain.model.Meal
import com.calai.app.domain.model.User
import com.calai.app.domain.model.WeightLog
import kotlinx.coroutines.flow.Flow
import java.io.File

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
    suspend fun updateRemoteMeal(mealId: String, mealType: String? = null, date: String? = null): Result<MealResponseDto>
    suspend fun copyRemoteMeal(mealId: String, targetDate: String, mealType: String? = null): Result<MealResponseDto>
    suspend fun deleteRemoteMeal(mealId: String): Result<Unit>
    suspend fun fetchNutritionStatistics(startDate: String? = null, endDate: String? = null): Result<NutritionStatisticsData>
    suspend fun quickAddMeal(
        name: String,
        mealType: String,
        date: String,
        calories: Float,
        protein: Float = 0f,
        carb: Float = 0f,
        fat: Float = 0f
    ): Result<MealResponseDto>

    // --- Food Database & Recommendations ---
    suspend fun searchFoods(query: String? = null, category: String? = null): Result<List<FoodItemDto>>
    suspend fun getFoodCategories(): Result<List<String>>
    suspend fun fetchFavoriteFoods(): Result<List<String>>
    suspend fun addFavoriteFood(foodName: String): Result<Unit>
    suspend fun removeFavoriteFood(foodName: String): Result<Unit>
    suspend fun fetchDietRecommendation(): Result<DietRecommendationData>
    suspend fun fetchWorkoutRecommendation(): Result<WorkoutRecommendationData>
    suspend fun fetchExercises(gender: String? = null, level: String? = null): Result<ExerciseListData>

    // --- Weight Logs Remote ---
    suspend fun createRemoteWeightLog(weightKg: Float, note: String? = null): Result<WeightLogResponseDto>
    suspend fun fetchRemoteWeightLogs(limit: Int = 30): Result<List<WeightLogResponseDto>>
    suspend fun fetchWeightTrend(limit: Int = 60): Result<List<WeightTrendPointDto>>
    suspend fun fetchWeightProgress(): Result<WeightProgressDto>

    // --- AI Food Recognition & Chat Coach ---
    suspend fun recognizeFood(file: File): Result<FoodRecognitionResultDto>
    suspend fun recognizeFoodBase64(base64: String): Result<FoodRecognitionResultDto>
    suspend fun chatAi(message: String): Result<ChatAiResponseDto>
}
