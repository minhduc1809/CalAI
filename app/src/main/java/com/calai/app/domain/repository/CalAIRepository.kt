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
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponseData>
    suspend fun sendVerificationEmail(): Result<Unit>
    suspend fun verifyEmail(code: String): Result<Unit>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>

    // --- Habit Reminders ---
    suspend fun getWaterToday(): Result<WaterTodayDto>
    suspend fun addWaterGlass(): Result<WaterTodayDto>
    suspend fun undoWaterGlass(): Result<WaterTodayDto>

    suspend fun getHabitReminders(): Result<List<HabitReminderDto>>
    suspend fun createHabitReminder(request: CreateHabitReminderRequest): Result<HabitReminderDto>
    suspend fun updateHabitReminder(id: String, request: UpdateHabitReminderRequest): Result<HabitReminderDto>
    suspend fun deleteHabitReminder(id: String): Result<Unit>
    suspend fun addHabitReminderFood(id: String, request: AttachHabitReminderFoodRequest): Result<HabitReminderDto>
    suspend fun removeHabitReminderFood(id: String, foodId: String): Result<HabitReminderDto>
    suspend fun getHabitReminderSuggestions(id: String): Result<SuggestMealResponseDto>
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    fun getCurrentUsername(): String?

    // --- User Profile ---
    suspend fun fetchRemoteProfile(): Result<UserProfileDto>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfileDto>

    /** Áp dụng mục tiêu đề xuất sau khi người dùng bấm "Áp dụng" (BR-04, E5). */
    suspend fun applyProposedTarget(): Result<Unit>
    suspend fun fetchExpenditureStatus(): Result<ExpenditureStatusDto>

    /** BR-02.4: Onboarding draft */
    suspend fun getOnboardingDraft(): Result<OnboardingDraftData?>
    suspend fun saveOnboardingDraft(step: Int, data: Map<String, Any?>): Result<OnboardingDraftData>
    suspend fun clearOnboardingDraft(): Result<Unit>

    // --- Meals Remote & Sync ---
    suspend fun fetchDailySummary(date: String? = null): Result<DailyNutritionSummaryData>

    /** 7 ngày từ startDate (YYYY-MM-DD) với trạng thái đầy đủ của từng ngày, dùng cho Week Strip. */
    suspend fun fetchWeekSummary(startDate: String): Result<List<WeekDaySummaryDto>>

    /** Đánh dấu ngày đã ghi đủ / chưa đủ / để hệ thống quyết định (BR-05.2). */
    suspend fun setDayStatus(date: String, completeness: String): Result<Unit>

    // --- Notifications ---
    suspend fun getNotifications(): Result<NotificationListDto>
    suspend fun markNotificationRead(id: String): Result<Unit>
    suspend fun markAllNotificationsRead(): Result<Unit>
    suspend fun deleteNotification(id: String): Result<Unit>
    suspend fun fetchMealsFromRemote(date: String? = null): Result<List<MealResponseDto>>
    suspend fun createRemoteMeal(request: CreateMealRequest): Result<MealResponseDto>
    suspend fun updateRemoteMeal(
        mealId: String,
        mealType: String? = null,
        date: String? = null,
        items: List<CreateMealItemDto>? = null
    ): Result<MealResponseDto?>
    suspend fun copyRemoteMeal(mealId: String, targetDate: String, mealType: String? = null): Result<MealResponseDto>
    suspend fun deleteRemoteMeal(mealId: String): Result<Unit>
    suspend fun fetchNutritionStatistics(startDate: String? = null, endDate: String? = null, preset: String? = null): Result<NutritionStatisticsData>
    suspend fun fetchInsights(): Result<List<InsightDto>>
    suspend fun fetchWeeklySummary(): Result<WeeklySummaryDto>
    suspend fun regenerateWeeklySummary(): Result<WeeklySummaryDto>
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
    /** Ghi file ZIP dữ liệu cá nhân vào [output]; trả số byte đã ghi. Lỗi (kể cả hết lượt trong ngày) trả Result.failure kèm thông báo của máy chủ. */
    suspend fun exportData(output: java.io.OutputStream): Result<Long>
    suspend fun fetchTodayMealPlan(): Result<TodayMealPlanDto>
    suspend fun fetchMealPlan(days: Int): Result<MealPlanDto>
    suspend fun fetchWorkoutRecommendation(): Result<WorkoutRecommendationData>
    suspend fun fetchExercises(gender: String? = null, level: String? = null): Result<ExerciseListData>
    suspend fun createCustomFood(
        name: String,
        servingSize: String?,
        servingAmount: Float? = null,
        servingUnit: String? = null,
        calories: Float,
        protein: Float = 0f,
        carb: Float = 0f,
        fat: Float = 0f,
        ingredients: List<RecipeIngredientDto>? = null
    ): Result<CustomFoodDto>
    suspend fun lookupBarcode(code: String): Result<BarcodeProductDto?>
    suspend fun fetchCustomFoods(): Result<List<CustomFoodDto>>
    suspend fun deleteCustomFood(id: String): Result<Unit>

    // --- Weight Logs Remote ---
    suspend fun createRemoteWeightLog(weightKg: Float, note: String? = null): Result<WeightLogResponseDto>
    suspend fun fetchRemoteWeightLogs(limit: Int = 30): Result<List<WeightLogResponseDto>>
    suspend fun fetchWeightTrend(limit: Int = 60): Result<List<WeightTrendPointDto>>
    suspend fun fetchWeightProgress(): Result<WeightProgressDto>
    suspend fun updateRemoteWeightLog(logId: String, weightKg: Float? = null, note: String? = null, date: String? = null): Result<WeightLogResponseDto>
    suspend fun deleteRemoteWeightLog(logId: String): Result<Unit>

    // --- AI Food Recognition & Chat Coach ---
    suspend fun recognizeFood(file: File): Result<FoodRecognitionResultDto>
    suspend fun recognizeFoodBase64(base64: String): Result<FoodRecognitionResultDto>
    suspend fun chatAi(message: String): Result<ChatAiResponseDto>
    suspend fun fetchChatQuota(): Result<ChatQuotaInfoDto>
    suspend fun fetchAiQuota(): Result<AiQuotaDto>
    suspend fun fetchChatHistory(): Result<ChatHistoryResponseDto>
    suspend fun clearChatHistory(): Result<Unit>
    suspend fun fetchSuggestMeal(): Result<SuggestMealResponseDto>
    suspend fun scanMenu(file: File, note: String? = null): Result<ScanMenuResponseDto>
    suspend fun scanMenuBase64(base64: String, note: String? = null): Result<ScanMenuResponseDto>

    // --- Workouts & Training Remote ---
    suspend fun fetchWorkoutCategories(): Result<List<WorkoutCategoryInfoDto>>
    suspend fun fetchWorkoutSummary(date: String? = null): Result<WorkoutSummaryDto>
    suspend fun createWorkoutLog(request: CreateWorkoutLogRequest): Result<WorkoutLogDto>
    suspend fun fetchWorkouts(date: String? = null, startDate: String? = null, endDate: String? = null, category: String? = null): Result<List<WorkoutLogDto>>
    suspend fun fetchWorkoutById(id: String): Result<WorkoutLogDto>
    suspend fun updateWorkoutLog(id: String, request: UpdateWorkoutLogRequest): Result<WorkoutLogDto>
    suspend fun deleteWorkoutLog(id: String): Result<Unit>
}
