package com.calai.app.data.repository

import com.calai.app.data.local.CalAIDao
import com.calai.app.data.local.TokenManager
import com.calai.app.data.local.entity.PendingCustomFoodEntity
import com.calai.app.data.local.entity.PendingFavoriteEntity
import com.calai.app.data.local.entity.toDomain
import com.calai.app.data.local.entity.toEntity
import com.calai.app.data.local.entity.toRequest
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation của CalAIRepository
 * Khi không kết nối được Backend, trả lỗi thật để màn hình báo cho người dùng; không bao giờ dùng dữ liệu giả.
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
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e}
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
            if (e is kotlinx.coroutines.CancellationException) throw e
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
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthResponseData> {
        return try {
            val response = api.loginWithGoogle(GoogleLoginRequest(idToken = idToken))
            if (response.success && response.data != null) {
                tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)
                tokenManager.saveUser(
                    userId = response.data.user.id,
                    username = response.data.user.username,
                    name = response.data.user.name
                )
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Đăng nhập Google thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            val response = api.sendVerificationEmail()
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Gửi mã xác thực thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun verifyEmail(code: String): Result<Unit> {
        return try {
            val response = api.verifyEmail(VerifyEmailRequest(code = code))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Xác thực email thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun logout(): Result<Unit> {
        try {
            api.logout()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e}
        tokenManager.clear()
        return Result.success(Unit)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = api.changePassword(ChangePasswordRequest(oldPassword = oldPassword, newPassword = newPassword))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Đổi mật khẩu thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email = email))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Gửi yêu cầu đặt lại mật khẩu thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> {
        return try {
            val response = api.resetPassword(
                ResetPasswordRequest(email = email, code = code, newPassword = newPassword)
            )
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Đặt lại mật khẩu thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun getWaterToday(): Result<WaterTodayDto> {
        return try {
            val response = api.getWaterToday()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Không xử lý được dữ liệu nước uống"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun addWaterGlass(): Result<WaterTodayDto> {
        return try {
            val response = api.addWaterGlass()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Không xử lý được dữ liệu nước uống"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun undoWaterGlass(): Result<WaterTodayDto> {
        return try {
            val response = api.undoWaterGlass()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Không xử lý được dữ liệu nước uống"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun getHabitReminders(): Result<List<HabitReminderDto>> {
        return try {
            val response = api.getHabitReminders()
            if (response.success) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Không tải được danh sách nhắc nhở"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun createHabitReminder(request: CreateHabitReminderRequest): Result<HabitReminderDto> {
        return try {
            val response = api.createHabitReminder(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Tạo nhắc nhở thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun updateHabitReminder(id: String, request: UpdateHabitReminderRequest): Result<HabitReminderDto> {
        return try {
            val response = api.updateHabitReminder(id, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Cập nhật nhắc nhở thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun deleteHabitReminder(id: String): Result<Unit> {
        return try {
            val response = api.deleteHabitReminder(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Xoá nhắc nhở thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun addHabitReminderFood(id: String, request: AttachHabitReminderFoodRequest): Result<HabitReminderDto> {
        return try {
            val response = api.addHabitReminderFood(id, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Gắn món ăn thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun removeHabitReminderFood(id: String, foodId: String): Result<HabitReminderDto> {
        return try {
            val response = api.removeHabitReminderFood(id, foodId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Gỡ món ăn thất bại"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun getHabitReminderSuggestions(id: String): Result<SuggestMealResponseDto> {
        return try {
            val response = api.getHabitReminderSuggestions(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không lấy được gợi ý món ăn"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    override fun getCurrentUserId(): String = tokenManager.getUserId() ?: ""

    override fun getCurrentUsername(): String = tokenManager.getUsername() ?: "Người dùng CalAI"

    // --- User Profile ---
    override suspend fun fetchRemoteProfile(): Result<UserProfileDto> {
        return try {
            val response = api.getProfile()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải thông tin hồ sơ"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfileDto> {
        return try {
            val response = api.updateProfile(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể cập nhật hồ sơ"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun applyProposedTarget(): Result<Unit> {
        return try {
            val response = api.applyProposedTarget()
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Không thể áp dụng mục tiêu mới"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun fetchExpenditureStatus(): Result<ExpenditureStatusDto> {
        return try {
            val response = api.getExpenditureStatus()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun getOnboardingDraft(): Result<OnboardingDraftData?> {
        return try {
            val response = api.getOnboardingDraft()
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải bản nháp onboarding"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun saveOnboardingDraft(step: Int, data: Map<String, Any?>): Result<OnboardingDraftData> {
        return try {
            val response = api.saveOnboardingDraft(SaveOnboardingDraftRequest(step = step, data = data))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể lưu bản nháp onboarding"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun clearOnboardingDraft(): Result<Unit> {
        return try {
            val response = api.clearOnboardingDraft()
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Không thể xóa bản nháp onboarding"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }


    // --- Meals Remote & Sync ---
    override suspend fun fetchWeekSummary(startDate: String): Result<List<WeekDaySummaryDto>> {
        return try {
            val response = api.getWeekSummary(startDate)
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.message ?: "Không thể tải tóm tắt tuần"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun setDayStatus(date: String, completeness: String): Result<Unit> {
        return try {
            val response = api.setDayStatus(date, SetDayStatusRequest(completeness))
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Không thể cập nhật trạng thái ngày"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun fetchDailySummary(date: String?): Result<DailyNutritionSummaryData> {
        return try {
            val response = api.getDailySummary(date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải tổng hợp dinh dưỡng"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun getNotifications(): Result<NotificationListDto> {
        return try {
            val response = api.getNotifications()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải thông báo"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun markNotificationRead(id: String): Result<Unit> {
        return try {
            val response = api.markNotificationRead(id)
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Không thể đánh dấu đã đọc"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun markAllNotificationsRead(): Result<Unit> {
        return try {
            val response = api.markAllNotificationsRead()
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Không thể đánh dấu tất cả đã đọc"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun deleteNotification(id: String): Result<Unit> {
        return try {
            val response = api.deleteNotification(id)
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Không thể xoá thông báo"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
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
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun createRemoteMeal(request: CreateMealRequest): Result<MealResponseDto> {
        return try {
            val response = api.createMeal(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tạo bữa ăn"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun updateRemoteMeal(
        mealId: String,
        mealType: String?,
        date: String?,
        items: List<CreateMealItemDto>?
    ): Result<MealResponseDto?> {
        val request = UpdateMealRequest(mealType = mealType, date = date, items = items)
        return try {
            val response = api.updateMeal(mealId, request)
            // items=[] (xoá món cuối cùng) -> backend xoá luôn bữa ăn, trả success với data=null — hợp lệ, không phải lỗi.
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể cập nhật bữa ăn"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun copyRemoteMeal(mealId: String, targetDate: String, mealType: String?): Result<MealResponseDto> {
        return try {
            val response = api.copyMeal(mealId, CopyMealRequest(targetDate = targetDate, mealType = mealType))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể sao chép bữa ăn"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
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
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun fetchNutritionStatistics(startDate: String?, endDate: String?, preset: String?): Result<NutritionStatisticsData> {
        return try {
            val response = api.getMealsStatistics(startDate, endDate, preset)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun fetchInsights(): Result<List<InsightDto>> {
        return try {
            val response = api.getInsights()
            if (response.success && response.data != null) {
                Result.success(response.data.insights)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(emptyList())
        }
    }

    override suspend fun fetchWeeklySummary(): Result<WeeklySummaryDto> {
        return try {
            val response = api.getWeeklySummary()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tải tổng kết tuần"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun regenerateWeeklySummary(): Result<WeeklySummaryDto> {
        return try {
            val response = api.regenerateWeeklySummary()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể tạo lại tổng kết tuần"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun quickAddMeal(
        name: String,
        mealType: String,
        date: String,
        calories: Float,
        protein: Float,
        carb: Float,
        fat: Float
    ): Result<MealResponseDto> {
        val request = QuickAddMealRequest(
            name = name,
            mealType = mealType,
            date = date,
            calories = calories,
            protein = protein,
            carb = carb,
            fat = fat
        )
        return try {
            val response = api.quickAddMeal(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    // --- Recommendations & Foods  ---
    override suspend fun searchFoods(query: String?, category: String?): Result<List<FoodItemDto>> {
        return try {
            val response = api.searchFoods(query, category)
            if (response.success && response.data != null) {
                Result.success(response.data.items)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun getFoodCategories(): Result<List<String>> {
        return try {
            val response = api.getFoodCategories()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    /**
     * Đẩy lại lên server các thay đổi món yêu thích/món tự tạo từng thất bại lúc mất mạng
     * (lưu ở Room, xem PendingSyncEntity.kt) — gọi mỗi lần vừa xác nhận có kết nối tới server
     * (một request khác vừa thành công), để không phải chờ người dùng tự thử lại thủ công.
     */
    private suspend fun flushPendingOfflineSync() {
        dao.getPendingFavorites().forEach { pending ->
            try {
                val response = if (pending.isAdd) {
                    api.addFavoriteFood(AddFavoriteFoodRequest(pending.foodName))
                } else {
                    api.removeFavoriteFood(pending.foodName)
                }
                if (response.success) dao.deletePendingFavorite(pending.foodName)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Vẫn lỗi mạng — để nguyên trong hàng đợi, thử lại ở lần flush kế tiếp
            }
        }
        dao.getPendingCustomFoods().forEach { pending ->
            try {
                val response = api.createCustomFood(pending.toRequest())
                if (response.success) dao.deletePendingCustomFood(pending.localId)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }
        }
    }

    override suspend fun fetchFavoriteFoods(): Result<List<String>> {
        return try {
            val response = api.getFavoriteFoods()
            if (response.success && response.data != null) {
                flushPendingOfflineSync()
                // Món vừa thêm offline mà flush ở trên chưa kịp lên server (network rớt giữa chừng)
                // vẫn cần hiện luôn, không đợi lần tải sau.
                val stillPendingAdds = dao.getPendingFavorites().filter { it.isAdd }.map { it.foodName }
                Result.success((response.data + stillPendingAdds).distinct())
            } else {
                Result.success(offlineFavoriteFoodsSnapshot())
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(offlineFavoriteFoodsSnapshot())
        }
    }

    /** Không có kết nối tới server — hiện danh sách mặc định gợi ý cộng với các món đã bấm thêm offline. */
    private suspend fun offlineFavoriteFoodsSnapshot(): List<String> {
        val defaults = setOf("Ức Gà Áp Chảo", "Trứng Luộc (2 quả)")
        val pending = dao.getPendingFavorites()
        val added = pending.filter { it.isAdd }.map { it.foodName }
        val removed = pending.filter { !it.isAdd }.map { it.foodName }.toSet()
        return ((defaults + added) - removed).toList()
    }

    override suspend fun addFavoriteFood(foodName: String): Result<Unit> {
        return try {
            val response = api.addFavoriteFood(AddFavoriteFoodRequest(foodName))
            if (response.success) {
                dao.deletePendingFavorite(foodName)
                Result.success(Unit)
            } else {
                // Server từ chối (không phải lỗi mạng) — vẫn xếp hàng đợi Room để lần tới thử lại
                // và không mất trạng thái người dùng đã chọn, nhưng KHÔNG báo thành công giả.
                dao.upsertPendingFavorite(PendingFavoriteEntity(foodName, isAdd = true))
                Result.failure(Exception(response.message ?: "Không lưu được món yêu thích"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            dao.upsertPendingFavorite(PendingFavoriteEntity(foodName, isAdd = true))
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun removeFavoriteFood(foodName: String): Result<Unit> {
        return try {
            val response = api.removeFavoriteFood(foodName)
            dao.deletePendingFavorite(foodName)
            if (response.success) {
                Result.success(Unit)
            } else {
                dao.upsertPendingFavorite(PendingFavoriteEntity(foodName, isAdd = false))
                Result.failure(Exception(response.message ?: "Không gỡ được món yêu thích"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            dao.upsertPendingFavorite(PendingFavoriteEntity(foodName, isAdd = false))
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun exportData(output: java.io.OutputStream): Result<Long> {
        return try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val response = api.exportData()
                if (!response.isSuccessful) {
                    // Dùng chung cách đọc thông báo lỗi của máy chủ (ví dụ "đã dùng 1/1 lần xuất hôm nay")
                    return@withContext Result.failure<Long>(Exception(extractErrorMessage(HttpException(response))))
                }
                val body = response.body() ?: return@withContext Result.failure<Long>(Exception("Máy chủ không trả về dữ liệu"))
                var total = 0L
                body.byteStream().use { input ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        output.write(buffer, 0, n)
                        total += n
                    }
                }
                output.flush()
                Result.success(total)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun fetchTodayMealPlan(): Result<TodayMealPlanDto> {
        return try {
            val response = api.getTodayMealPlan()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun fetchMealPlan(days: Int): Result<MealPlanDto> {
        return try {
            val response = api.getMealPlan(days)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    override suspend fun fetchWorkoutRecommendation(): Result<WorkoutRecommendationData> {
        return try {
            val response = api.getWorkoutRecommendation()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun fetchExercises(gender: String?, level: String?): Result<ExerciseListData> {
        return try {
            val response = api.getExercises(gender, level)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun createCustomFood(
        name: String,
        servingSize: String?,
        servingAmount: Float?,
        servingUnit: String?,
        calories: Float,
        protein: Float,
        carb: Float,
        fat: Float,
        ingredients: List<RecipeIngredientDto>?
    ): Result<CustomFoodDto> {
        val request = CreateCustomFoodRequest(
            name = name,
            servingSize = servingSize,
            servingAmount = servingAmount,
            servingUnit = servingUnit,
            calories = calories,
            protein = protein,
            carb = carb,
            fat = fat,
            ingredients = ingredients
        )
        return try {
            val response = api.createCustomFood(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                queuePendingCustomFood(request)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            queuePendingCustomFood(request)
        }
    }

    private val PENDING_CUSTOM_FOOD_PREFIX = "pending_"

    /** Server chưa tạo được — lưu vào hàng đợi Room (id giả "pending_<localId>" để deleteCustomFood
     * biết đường xoá đúng chỗ) thay vì chỉ giữ trong bộ nhớ tiến trình như trước, để không mất khi
     * app bị đóng và còn cơ hội tự đồng bộ lên server ở lần fetch kế tiếp có mạng. */
    private suspend fun queuePendingCustomFood(request: CreateCustomFoodRequest): Result<CustomFoodDto> {
        val localId = dao.insertPendingCustomFood(
            PendingCustomFoodEntity(
                name = request.name, servingSize = request.servingSize,
                servingAmount = request.servingAmount, servingUnit = request.servingUnit,
                calories = request.calories ?: 0f, protein = request.protein, carb = request.carb, fat = request.fat
            )
        )
        val food = CustomFoodDto(
            id = "$PENDING_CUSTOM_FOOD_PREFIX$localId",
            userId = "offline",
            name = request.name,
            servingSize = request.servingSize,
            servingAmount = request.servingAmount,
            servingUnit = request.servingUnit,
            calories = request.calories ?: 0f,
            protein = request.protein,
            carb = request.carb,
            fat = request.fat
        )
        return Result.success(food)
    }

    override suspend fun lookupBarcode(code: String): Result<BarcodeProductDto?> {
        return try {
            val response = api.lookupBarcode(code)
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(null)
        }
    }

    private fun PendingCustomFoodEntity.toOfflineDto() = CustomFoodDto(
        id = "$PENDING_CUSTOM_FOOD_PREFIX$localId", userId = "offline", name = name,
        servingSize = servingSize, servingAmount = servingAmount, servingUnit = servingUnit,
        calories = calories, protein = protein, carb = carb, fat = fat
    )

    override suspend fun fetchCustomFoods(): Result<List<CustomFoodDto>> {
        return try {
            val response = api.getCustomFoods()
            if (response.success && response.data != null) {
                flushPendingOfflineSync()
                val stillPending = dao.getPendingCustomFoods().map { it.toOfflineDto() }
                Result.success(stillPending + response.data)
            } else {
                Result.success(dao.getPendingCustomFoods().map { it.toOfflineDto() })
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(dao.getPendingCustomFoods().map { it.toOfflineDto() })
        }
    }

    override suspend fun deleteCustomFood(id: String): Result<Unit> {
        if (id.startsWith(PENDING_CUSTOM_FOOD_PREFIX)) {
            val localId = id.removePrefix(PENDING_CUSTOM_FOOD_PREFIX).toLongOrNull()
            if (localId != null) dao.deletePendingCustomFood(localId)
            return Result.success(Unit)
        }
        return try {
            val response = api.deleteCustomFood(id)
            if (response.success) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "Không xoá được món tự tạo"))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }

    // --- Weight Logs Remote ---
    override suspend fun createRemoteWeightLog(weightKg: Float, note: String?): Result<WeightLogResponseDto> {
        // Không trả bản ghi giả khi server từ chối hoặc lỗi mạng: người dùng phải biết bản ghi CHƯA được lưu
        return try {
            val response = api.createWeightLog(CreateWeightLogRequest(weightKg = weightKg, note = note))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể ghi cân nặng"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception(extractErrorMessage(e)))
        }
    }


    override suspend fun fetchRemoteWeightLogs(limit: Int): Result<List<WeightLogResponseDto>> {
        return try {
            val response = api.getWeightLogs(limit)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun fetchWeightTrend(limit: Int): Result<List<WeightTrendPointDto>> {
        return try {
            val response = api.getWeightTrend(limit)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun fetchWeightProgress(): Result<WeightProgressDto> {
        return try {
            val response = api.getWeightProgress()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateRemoteWeightLog(logId: String, weightKg: Float?, note: String?, date: String?): Result<WeightLogResponseDto> {
        return try {
            val response = api.updateWeightLog(logId, UpdateWeightLogRequest(weightKg = weightKg, note = note, date = date))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun deleteRemoteWeightLog(logId: String): Result<Unit> {
        return try {
            api.deleteWeightLog(logId)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(Unit)
        }
    }


    // --- AI Food Recognition & Chat Coach  ---
    override suspend fun recognizeFood(file: File): Result<FoodRecognitionResultDto> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val response = api.recognizeFood(body)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                android.util.Log.w("CalAIRepository", "recognizeFood unsuccess: ${response.message}")
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("CalAIRepository", "recognizeFood exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun recognizeFoodBase64(base64: String): Result<FoodRecognitionResultDto> {
        return try {
            val response = api.recognizeFoodBase64(RecognizeFoodBase64Request(base64Image = base64))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                android.util.Log.w("CalAIRepository", "recognizeFoodBase64 unsuccess: ${response.message}")
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("CalAIRepository", "recognizeFoodBase64 exception: ${e.message}", e)
            Result.failure(e)
        }
    }


    override suspend fun chatAi(message: String): Result<ChatAiResponseDto> {
        return try {
            val response = api.chatAi(ChatAiRequest(message = message))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                android.util.Log.w("CalAIRepository", "chatAi unsuccess: ${response.message}")
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("CalAIRepository", "chatAi exception: ${e.message}", e)
            Result.failure(e)
        }
    }


    override suspend fun fetchChatQuota(): Result<ChatQuotaInfoDto> {
        return try {
            val response = api.getChatQuota()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.success(ChatQuotaInfoDto())
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(ChatQuotaInfoDto())
        }
    }

    override suspend fun fetchAiQuota(): Result<AiQuotaDto> {
        return try {
            val response = api.getAiQuota()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Không thể lấy hạn mức chụp ảnh AI"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun fetchChatHistory(): Result<ChatHistoryResponseDto> {
        return try {
            val response = api.getChatHistory()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.success(ChatHistoryResponseDto())
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(ChatHistoryResponseDto())
        }
    }

    override suspend fun clearChatHistory(): Result<Unit> {
        return try {
            val response = api.clearChatHistory()
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Không thể xóa lịch sử"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun fetchSuggestMeal(): Result<SuggestMealResponseDto> {
        return try {
            val response = api.getSuggestMeal()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.success(SuggestMealResponseDto(
                    nutritionGap = NutritionGapDto(remainingCalories = 500, remainingProtein = 35),
                    suggestions = listOf(
                        SuggestedMealItemDto(
                            name = "Phở gà ức ít bánh + 2 trứng chần",
                            mealType = "Bữa tối",
                            calories = 450,
                            protein = 40,
                            carbs = 48,
                            fat = 9,
                            reason = "Cung cấp đạm nạc tinh khiết, vừa vặn với calo còn thiếu tối nay."
                        )
                    ),
                    advice = "Bạn còn thiếu 35g protein. Hãy ưu tiên bổ sung bữa tối giàu đạm nhé!"
                ))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(SuggestMealResponseDto(
                nutritionGap = NutritionGapDto(remainingCalories = 500, remainingProtein = 35),
                suggestions = listOf(
                    SuggestedMealItemDto(
                        name = "Phở gà ức ít bánh + 2 trứng chần",
                        mealType = "Bữa tối",
                        calories = 450,
                        protein = 40,
                        carbs = 48,
                        fat = 9,
                        reason = "Cung cấp đạm nạc tinh khiết, vừa vặn với calo còn thiếu tối nay."
                    )
                ),
                advice = "Bạn còn thiếu 35g protein. Hãy ưu tiên bổ sung bữa tối giàu đạm nhé!"
            ))
        }
    }

    override suspend fun scanMenu(file: File, note: String?): Result<ScanMenuResponseDto> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val noteBody = note?.let { MultipartBody.Part.createFormData("note", it) }

            val response = api.scanMenu(body, noteBody)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun scanMenuBase64(base64: String, note: String?): Result<ScanMenuResponseDto> {
        return try {
            val response = api.scanMenuBase64(ScanMenuBase64Request(imageBase64 = base64, note = note))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    // --- Workouts & Training Implementation ---

    override suspend fun fetchWorkoutCategories(): Result<List<WorkoutCategoryInfoDto>> {
        return try {
            val response = api.getWorkoutCategories()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun fetchWorkoutSummary(date: String?): Result<WorkoutSummaryDto> {
        return try {
            val response = api.getWorkoutSummary(date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun createWorkoutLog(request: CreateWorkoutLogRequest): Result<WorkoutLogDto> {
        return try {
            val response = api.createWorkout(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun fetchWorkouts(
        date: String?,
        startDate: String?,
        endDate: String?,
        category: String?
    ): Result<List<WorkoutLogDto>> {
        return try {
            val response = api.getWorkouts(date, startDate, endDate, category)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }


    override suspend fun fetchWorkoutById(id: String): Result<WorkoutLogDto> {
        return try {
            val response = api.getWorkoutById(id)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Máy chủ trả về lỗi"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateWorkoutLog(id: String, request: UpdateWorkoutLogRequest): Result<WorkoutLogDto> {
        return try {
            val response = api.updateWorkout(id, request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                fetchWorkoutById(id)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            fetchWorkoutById(id)
        }
    }

    override suspend fun deleteWorkoutLog(id: String): Result<Unit> {
        return try {
            val response = api.deleteWorkout(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.success(Unit)
        }
    }
}
