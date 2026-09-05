package com.calai.app.data.repository

import com.calai.app.data.local.CalAIDao
import com.calai.app.data.local.TokenManager
import com.calai.app.data.local.entity.toDomain
import com.calai.app.data.local.entity.toEntity
import com.calai.app.data.remote.CalAIApi
import com.calai.app.data.remote.dto.*
import com.calai.app.domain.model.Meal
import com.calai.app.domain.model.User
import com.calai.app.domain.model.WeightLog
import com.calai.app.domain.repository.CalAIRepository
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

/**
 * Implementation của CalAIRepository
 * Kết nối Database cục bộ (Room) và REST API (NestJS backend)
 */
class CalAIRepositoryImpl @Inject constructor(
    private val dao: CalAIDao,
    private val api: CalAIApi,
    private val tokenManager: TokenManager
) : CalAIRepository {

    private fun extractErrorMessage(e: Throwable): String {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                try {
                    val json = JsonParser.parseString(errorBody).asJsonObject
                    if (json.has("message")) {
                        val msgElem = json.get("message")
                        if (msgElem.isJsonArray) {
                            return msgElem.asJsonArray.joinToString("\n") { it.asString }
                        } else if (msgElem.isJsonPrimitive) {
                            return msgElem.asString
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return e.localizedMessage ?: "Có lỗi xảy ra"
    }

    // --- Local Database ---
    override fun getUser(userId: String): Flow<User?> {
        return dao.getUser(userId).map { it?.toDomain() }
    }

    override suspend fun saveUser(user: User) {
        dao.saveUser(user.toEntity())
    }

    override fun getMeals(userId: String): Flow<List<Meal>> {
        return dao.getMeals(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMeal(meal: Meal) {
        dao.insertMeal(meal.toEntity())
    }

    override suspend fun deleteMeal(meal: Meal) {
        dao.deleteMeal(meal.toEntity())
    }

    override fun getWeightLogs(userId: String): Flow<List<WeightLog>> {
        return dao.getWeightLogs(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertWeightLog(log: WeightLog) {
        dao.insertWeightLog(log.toEntity())
    }

    // --- Authentication ---
    override suspend fun login(username: String, password: String): Result<AuthResponseData> {
        return try {
            val response = api.login(LoginRequest(username = username.trim(), password = password))
            if (response.success && response.data != null) {
                tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)
                tokenManager.saveUser(
                    userId = response.data.user.id,
                    username = response.data.user.username,
                    name = response.data.user.name
                )
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Đăng nhập thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun register(
        username: String,
        email: String?,
        password: String,
        name: String?
    ): Result<AuthResponseData> {
        return try {
            val response = api.register(
                RegisterRequest(
                    username = username.trim(),
                    email = if (email.isNullOrBlank()) null else email.trim(),
                    password = password,
                    name = if (name.isNullOrBlank()) null else name.trim()
                )
            )
            if (response.success && response.data != null) {
                tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)
                tokenManager.saveUser(
                    userId = response.data.user.id,
                    username = response.data.user.username,
                    name = response.data.user.name
                )
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Đăng ký thất bại"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            try {
                api.logout()
            } catch (_: Exception) {}
            tokenManager.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    override fun getCurrentUserId(): String? = tokenManager.getUserId()

    override fun getCurrentUsername(): String? = tokenManager.getUsername()

    // --- User Profile ---
    override suspend fun fetchRemoteProfile(): Result<UserProfileDto> {
        return try {
            val response = api.getProfile()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể lấy thông tin cá nhân"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfileDto> {
        return try {
            val response = api.updateProfile(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể cập nhật thông tin"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    // --- Meals Remote & Sync ---
    override suspend fun fetchDailySummary(date: String?): Result<DailyNutritionSummaryData> {
        return try {
            val response = api.getDailySummary(date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải tổng hợp dinh dưỡng"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun fetchMealsFromRemote(date: String?): Result<List<MealResponseDto>> {
        return try {
            val response = api.getMeals(date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải danh sách bữa ăn"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun createRemoteMeal(request: CreateMealRequest): Result<MealResponseDto> {
        return try {
            val response = api.createMeal(request)
            if (response.success && response.data != null) {
                val userId = tokenManager.getUserId() ?: ""
                for (item in response.data.items) {
                    val localMeal = Meal(
                        mealId = item.id,
                        userId = userId,
                        foodName = item.name,
                        calories = item.calories,
                        protein = item.protein,
                        carb = item.carb,
                        fat = item.fat,
                        timestamp = System.currentTimeMillis(),
                        source = item.source
                    )
                    dao.insertMeal(localMeal.toEntity())
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tạo bữa ăn"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun deleteRemoteMeal(mealId: String): Result<Unit> {
        return try {
            val response = api.deleteMeal(mealId)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Không thể xóa bữa ăn"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    // --- Recommendations & Foods ---
    override suspend fun searchFoods(query: String?, category: String?): Result<List<FoodItemDto>> {
        return try {
            val response = api.searchFoods(query, category)
            if (response.success && response.data != null) {
                Result.success(response.data.items)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tra cứu món ăn"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun getFoodCategories(): Result<List<String>> {
        return try {
            val response = api.getFoodCategories()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải danh mục món ăn"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    // --- Weight Logs Remote ---
    override suspend fun createRemoteWeightLog(weightKg: Float, note: String?): Result<WeightLogResponseDto> {
        return try {
            val response = api.createWeightLog(CreateWeightLogRequest(weightKg = weightKg, note = note))
            if (response.success && response.data != null) {
                val userId = tokenManager.getUserId() ?: ""
                val localLog = WeightLog(
                    logId = response.data.id,
                    userId = userId,
                    weight = response.data.weightKg,
                    date = System.currentTimeMillis()
                )
                dao.insertWeightLog(localLog.toEntity())
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể lưu cân nặng"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun fetchRemoteWeightLogs(limit: Int): Result<List<WeightLogResponseDto>> {
        return try {
            val response = api.getWeightLogs(limit)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải lịch sử cân nặng"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    // --- AI Food Recognition ---
    override suspend fun recognizeFood(file: File): Result<FoodRecognitionResultDto> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val response = api.recognizeFood(body)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể nhận diện món ăn"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun recognizeFoodBase64(base64: String): Result<FoodRecognitionResultDto> {
        return try {
            val response = api.recognizeFoodBase64(RecognizeFoodBase64Request(base64Image = base64))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể nhận diện món ăn"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun chatAi(message: String): Result<ChatAiResponseDto> {
        return try {
            val response = api.chatAi(ChatAiRequest(message = message))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể nhận phản hồi từ AI Coach"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }
}
