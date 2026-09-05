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

    @GET("meals/summary")
    suspend fun getDailySummary(@Query("date") date: String? = null): ApiResponse<DailyNutritionSummaryData>

    @DELETE("meals/{id}")
    suspend fun deleteMeal(@Path("id") mealId: String): ApiResponse<Any?>

    // --- WEIGHT LOGS ---
    @POST("weight-logs")
    suspend fun createWeightLog(@Body request: CreateWeightLogRequest): ApiResponse<WeightLogResponseDto>

    @GET("weight-logs")
    suspend fun getWeightLogs(@Query("limit") limit: Int = 30): ApiResponse<List<WeightLogResponseDto>>

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

    companion object {
        // Mặc định kết nối tới localhost của máy phát triển qua Android Emulator (10.0.2.2)
        // Nếu dùng thiết bị thật qua Wi-Fi LAN, đổi thành IP máy tính (VD: http://192.168.1.x:3000/api/v1/)
        const val BASE_URL = "http://10.0.2.2:3000/api/v1/"
    }
}
