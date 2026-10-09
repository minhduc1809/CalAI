package com.calai.app.data.remote

import android.os.Build
import com.calai.app.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * Interface Retrofit kết nối tới backend CalAI (NestJS)
 */
interface CalAIApi {

    // --- AUTH ---
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResponseData>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponseData>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<RefreshTokenResponseData>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Any?>

    @PATCH("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<Any?>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): ApiResponse<AuthResponseData>

    @POST("auth/send-verification-email")
    suspend fun sendVerificationEmail(): ApiResponse<Any?>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): ApiResponse<Any?>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiResponse<Any?>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiResponse<Any?>

    // --- USERS ---
    @GET("users/me")
    suspend fun getProfile(): ApiResponse<UserProfileDto>

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserProfileDto>

    /** BR-04 (E5): áp dụng mục tiêu tính lại từ hồ sơ hiện tại sau khi người dùng xác nhận. */
    @POST("users/me/target/apply")
    suspend fun applyProposedTarget(): ApiResponse<Any?>

    @GET("users/me/expenditure")
    suspend fun getExpenditureStatus(): ApiResponse<ExpenditureStatusDto>

    // --- MEALS ---
    @POST("meals")
    suspend fun createMeal(@Body request: CreateMealRequest): ApiResponse<MealResponseDto>

    @GET("meals")
    suspend fun getMeals(@Query("date") date: String? = null): ApiResponse<List<MealResponseDto>>

    @POST("meals/quick-add")
    suspend fun quickAddMeal(@Body request: QuickAddMealRequest): ApiResponse<MealResponseDto>

    @GET("meals/summary")
    suspend fun getDailySummary(@Query("date") date: String? = null): ApiResponse<DailyNutritionSummaryData>

    /** BR-05.2: người dùng đánh dấu ngày "Đã ghi đủ" / "Chưa đủ" / để hệ thống quyết định (AUTO). */
    @PUT("daily-status/{date}")
    suspend fun setDayStatus(
        @Path("date") date: String,
        @Body request: SetDayStatusRequest
    ): ApiResponse<Any?>

    @GET("meals/week-summary")
    suspend fun getWeekSummary(@Query("startDate") startDate: String): ApiResponse<List<WeekDaySummaryDto>>

    // --- NOTIFICATIONS ---
    @GET("notifications")
    suspend fun getNotifications(): ApiResponse<NotificationListDto>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): ApiResponse<NotificationDto>

    @PATCH("notifications/read-all")
    suspend fun markAllNotificationsRead(): ApiResponse<Any?>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): ApiResponse<Any?>

    @GET("meals/statistics")
    suspend fun getMealsStatistics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("preset") preset: String? = null
    ): ApiResponse<NutritionStatisticsData>

    @GET("analytics/insights")
    suspend fun getInsights(): ApiResponse<InsightsData>

    @GET("analytics/weekly-summary")
    suspend fun getWeeklySummary(): ApiResponse<WeeklySummaryDto>

    @POST("analytics/weekly-summary/regenerate")
    suspend fun regenerateWeeklySummary(): ApiResponse<WeeklySummaryDto>

    @PATCH("meals/{id}")
    suspend fun updateMeal(@Path("id") mealId: String, @Body request: UpdateMealRequest): ApiResponse<MealResponseDto>

    @POST("meals/{id}/copy")
    suspend fun copyMeal(@Path("id") mealId: String, @Body request: CopyMealRequest): ApiResponse<MealResponseDto>

    @DELETE("meals/{id}")
    suspend fun deleteMeal(@Path("id") mealId: String): ApiResponse<Any?>

    // --- WEIGHT LOGS ---
    @POST("weight-logs")
    suspend fun createWeightLog(@Body request: CreateWeightLogRequest): ApiResponse<WeightLogResponseDto>

    @GET("weight-logs")
    suspend fun getWeightLogs(@Query("limit") limit: Int = 30): ApiResponse<List<WeightLogResponseDto>>

    @GET("weight-logs/trend")
    suspend fun getWeightTrend(@Query("limit") limit: Int = 60): ApiResponse<List<WeightTrendPointDto>>

    @GET("weight-logs/progress")
    suspend fun getWeightProgress(): ApiResponse<WeightProgressDto>

    @PATCH("weight-logs/{id}")
    suspend fun updateWeightLog(@Path("id") logId: String, @Body request: UpdateWeightLogRequest): ApiResponse<WeightLogResponseDto>

    @DELETE("weight-logs/{id}")
    suspend fun deleteWeightLog(@Path("id") logId: String): ApiResponse<Any?>

    // --- RECOMMENDATIONS & FOODS ---
    @GET("recommendations/foods")
    suspend fun searchFoods(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null
    ): ApiResponse<FoodSearchResultData>

    @GET("recommendations/foods/categories")
    suspend fun getFoodCategories(): ApiResponse<List<String>>

    @GET("recommendations/barcode/{code}")
    suspend fun lookupBarcode(@Path("code") code: String): ApiResponse<BarcodeProductDto?>

    @POST("recommendations/favorites")
    suspend fun addFavoriteFood(@Body request: AddFavoriteFoodRequest): ApiResponse<Any?>

    @GET("recommendations/favorites")
    suspend fun getFavoriteFoods(): ApiResponse<List<String>>

    @DELETE("recommendations/favorites/{foodName}")
    suspend fun removeFavoriteFood(@Path("foodName") foodName: String): ApiResponse<Any?>

    @GET("meal-plan/today")
    suspend fun getTodayMealPlan(): ApiResponse<TodayMealPlanDto>

    @GET("meal-plan")
    suspend fun getMealPlan(@Query("days") days: Int): ApiResponse<MealPlanDto>

    @GET("recommendations/workout")
    suspend fun getWorkoutRecommendation(): ApiResponse<WorkoutRecommendationData>

    @GET("recommendations/exercises")
    suspend fun getExercises(
        @Query("gender") gender: String? = null,
        @Query("level") level: String? = null
    ): ApiResponse<ExerciseListData>

    @POST("recommendations/custom-foods")
    suspend fun createCustomFood(@Body request: CreateCustomFoodRequest): ApiResponse<CustomFoodDto>

    @GET("recommendations/custom-foods")
    suspend fun getCustomFoods(): ApiResponse<List<CustomFoodDto>>

    @DELETE("recommendations/custom-foods/{id}")
    suspend fun deleteCustomFood(@Path("id") id: String): ApiResponse<Any?>

    // --- WORKOUTS & TRAINING ---
    @GET("workouts/categories")
    suspend fun getWorkoutCategories(): ApiResponse<List<WorkoutCategoryInfoDto>>

    @GET("workouts/summary")
    suspend fun getWorkoutSummary(
        @Query("date") date: String? = null
    ): ApiResponse<WorkoutSummaryDto>

    @POST("workouts")
    suspend fun createWorkout(
        @Body request: CreateWorkoutLogRequest
    ): ApiResponse<WorkoutLogDto>

    @GET("workouts")
    suspend fun getWorkouts(
        @Query("date") date: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("category") category: String? = null
    ): ApiResponse<List<WorkoutLogDto>>

    @GET("workouts/{id}")
    suspend fun getWorkoutById(
        @Path("id") id: String
    ): ApiResponse<WorkoutLogDto>

    @PATCH("workouts/{id}")
    suspend fun updateWorkout(
        @Path("id") id: String,
        @Body request: UpdateWorkoutLogRequest
    ): ApiResponse<WorkoutLogDto>

    @DELETE("workouts/{id}")
    suspend fun deleteWorkout(
        @Path("id") id: String
    ): ApiResponse<Any?>

    // --- AI ENGINE ---
    @Multipart
    @POST("ai/recognize-food")
    suspend fun recognizeFood(
        @Part image: MultipartBody.Part
    ): ApiResponse<FoodRecognitionResultDto>

    @POST("ai/recognize-food-base64")
    suspend fun recognizeFoodBase64(
        @Body request: RecognizeFoodBase64Request
    ): ApiResponse<FoodRecognitionResultDto>

    @POST("ai/chat")
    suspend fun chatAi(
        @Body request: ChatAiRequest
    ): ApiResponse<ChatAiResponseDto>

    @GET("payments/plans")
    suspend fun getQrPlans(): ApiResponse<List<QrPlanDto>>

    @POST("payments/orders")
    suspend fun createPaymentOrder(
        @Body request: CreatePaymentOrderRequest
    ): ApiResponse<PaymentOrderDto>

    @GET("payments/orders/{id}")
    suspend fun getPaymentOrder(@Path("id") id: String): ApiResponse<PaymentOrderDto>

    @POST("payments/orders/{id}/cancel")
    suspend fun cancelPaymentOrder(@Path("id") id: String): ApiResponse<PaymentOrderDto>

    /** Xuất toàn bộ dữ liệu cá nhân (BR-18): trả thẳng file ZIP, không bọc JSON. Tối đa 1 lần/ngày. */
    @Streaming
    @POST("me/export")
    suspend fun exportData(): retrofit2.Response<okhttp3.ResponseBody>

    @GET("me/entitlement")
    suspend fun getEntitlement(): ApiResponse<EntitlementDto>

    @POST("billing/verify")
    suspend fun verifyPurchase(
        @Body request: VerifyPurchaseRequest
    ): ApiResponse<EntitlementDto>

    @GET("ai/chat/quota")
    suspend fun getChatQuota(): ApiResponse<ChatQuotaInfoDto>

    @GET("ai/quota")
    suspend fun getAiQuota(): ApiResponse<AiQuotaDto>

    @GET("ai/chat/history")
    suspend fun getChatHistory(): ApiResponse<ChatHistoryResponseDto>

    @DELETE("ai/chat/history")
    suspend fun clearChatHistory(): ApiResponse<Any?>

    @GET("ai/suggest-meal")
    suspend fun getSuggestMeal(): ApiResponse<SuggestMealResponseDto>

    // --- WATER LOGS ---
    @GET("water-logs/today")
    suspend fun getWaterToday(): ApiResponse<WaterTodayDto>

    @POST("water-logs")
    suspend fun addWaterGlass(): ApiResponse<WaterTodayDto>

    @DELETE("water-logs/last")
    suspend fun undoWaterGlass(): ApiResponse<WaterTodayDto>

    // --- HABIT REMINDERS ---
    @GET("habit-reminders")
    suspend fun getHabitReminders(): ApiResponse<List<HabitReminderDto>>

    @POST("habit-reminders")
    suspend fun createHabitReminder(@Body request: CreateHabitReminderRequest): ApiResponse<HabitReminderDto>

    @PATCH("habit-reminders/{id}")
    suspend fun updateHabitReminder(
        @Path("id") id: String,
        @Body request: UpdateHabitReminderRequest
    ): ApiResponse<HabitReminderDto>

    @DELETE("habit-reminders/{id}")
    suspend fun deleteHabitReminder(@Path("id") id: String): ApiResponse<Any?>

    @POST("habit-reminders/{id}/foods")
    suspend fun addHabitReminderFood(
        @Path("id") id: String,
        @Body request: AttachHabitReminderFoodRequest
    ): ApiResponse<HabitReminderDto>

    @DELETE("habit-reminders/{id}/foods/{foodId}")
    suspend fun removeHabitReminderFood(
        @Path("id") id: String,
        @Path("foodId") foodId: String
    ): ApiResponse<HabitReminderDto>

    @GET("habit-reminders/{id}/suggestions")
    suspend fun getHabitReminderSuggestions(@Path("id") id: String): ApiResponse<SuggestMealResponseDto>

    @Multipart
    @POST("ai/scan-menu")
    suspend fun scanMenu(
        @Part image: MultipartBody.Part,
        @Part note: MultipartBody.Part? = null
    ): ApiResponse<ScanMenuResponseDto>

    @POST("ai/scan-menu-base64")
    suspend fun scanMenuBase64(
        @Body request: ScanMenuBase64Request
    ): ApiResponse<ScanMenuResponseDto>

    companion object {
        val isEmulator: Boolean
            get() = (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || Build.PRODUCT.contains("sdk")
                    || Build.HARDWARE.contains("goldfish")
                    || Build.HARDWARE.contains("ranchu"))

        const val EMULATOR_HOST = "10.0.2.2"
        const val PC_LAN_IP = "172.16.10.170"
        const val USB_REVERSE_HOST = "127.0.0.1"

        /**
         * Mặc định sử dụng 10.0.2.2 (cho Android Emulator).
         * Khi chạy trên máy thật (qua cáp USB với adb reverse hoặc qua Wi-Fi),
         * DynamicHostInterceptor sẽ tự động chuyển hướng sang 127.0.0.1 hoặc 172.16.10.170.
         */
        const val BASE_URL = "http://$EMULATOR_HOST:3000/api/v1/"
    }
}
