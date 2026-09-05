package com.calai.app.data.remote

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

    // --- USERS ---
    @GET("users/me")
    suspend fun getProfile(): ApiResponse<UserProfileDto>

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserProfileDto>

    // --- MEALS ---
    @POST("meals")
    suspend fun createMeal(@Body request: CreateMealRequest): ApiResponse<MealResponseDto>

    @GET("meals")
    suspend fun getMeals(@Query("date") date: String? = null): ApiResponse<List<MealResponseDto>>

    @POST("meals/quick-add")
    suspend fun quickAddMeal(@Body request: QuickAddMealRequest): ApiResponse<MealResponseDto>

    @GET("meals/summary")
    suspend fun getDailySummary(@Query("date") date: String? = null): ApiResponse<DailyNutritionSummaryData>

    @GET("meals/statistics")
    suspend fun getMealsStatistics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<NutritionStatisticsData>

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

    @POST("recommendations/favorites")
    suspend fun addFavoriteFood(@Body request: AddFavoriteFoodRequest): ApiResponse<Any?>

    @GET("recommendations/favorites")
    suspend fun getFavoriteFoods(): ApiResponse<List<String>>

    @DELETE("recommendations/favorites/{foodName}")
    suspend fun removeFavoriteFood(@Path("foodName") foodName: String): ApiResponse<Any?>

    @GET("recommendations/diet")
    suspend fun getDietRecommendation(): ApiResponse<DietRecommendationData>

    @GET("recommendations/workout")
    suspend fun getWorkoutRecommendation(): ApiResponse<WorkoutRecommendationData>

    @GET("recommendations/exercises")
    suspend fun getExercises(
        @Query("gender") gender: String? = null,
        @Query("level") level: String? = null
    ): ApiResponse<ExerciseListData>

    @GET("recommendations/diet/monthly")
    suspend fun getMonthlyDiet(
        @Query("goal") goal: String? = null,
        @Query("level") level: String? = null
    ): ApiResponse<MonthlyDietData>

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

    companion object {
        // Mặc định kết nối tới localhost của máy phát triển qua Android Emulator (10.0.2.2)
        // Nếu dùng thiết bị thật qua Wi-Fi LAN, đổi thành IP máy tính (VD: http://192.168.1.x:3000/api/v1/)
        const val BASE_URL = "http://10.0.2.2:3000/api/v1/"
    }
}
