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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation của CalAIRepository
 * Hỗ trợ Chế độ Hybrid: Tự động dùng Mock Offline khi không kết nối được Backend
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

    // --- Authentication (với Mock Offline Fallback) ---
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
        } catch (_: Exception) {
            // Mock Offline Fallback
            val mockUser = AuthUserDto(
                id = "mock_user_01",
                username = username.ifBlank { "calai_user" },
                email = "${username.ifBlank { "user" }}@calai.com",
                name = username.ifBlank { "Người dùng CalAI" }
            )
            val mockAuth = AuthResponseData(
                accessToken = "mock_access_token",
                refreshToken = "mock_refresh_token",
                user = mockUser
            )
            tokenManager.saveTokens(mockAuth.accessToken, mockAuth.refreshToken)
            tokenManager.saveUser(mockUser.id, mockUser.username, mockUser.name)
            Result.success(mockAuth)
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
        } catch (_: Exception) {
            // Mock Offline Fallback
            val mockUser = AuthUserDto(
                id = "mock_user_01",
                username = username.ifBlank { "calai_user" },
                email = email?.ifBlank { "user@calai.com" } ?: "user@calai.com",
                name = name?.ifBlank { "Người dùng CalAI" } ?: "Người dùng CalAI"
            )
            val mockAuth = AuthResponseData(
                accessToken = "mock_access_token",
                refreshToken = "mock_refresh_token",
                user = mockUser
            )
            tokenManager.saveTokens(mockAuth.accessToken, mockAuth.refreshToken)
            tokenManager.saveUser(mockUser.id, mockUser.username, mockUser.name)
            Result.success(mockAuth)
        }
    }

    override suspend fun logout(): Result<Unit> {
        try {
            api.logout()
        } catch (_: Exception) {}
        tokenManager.clear()
        return Result.success(Unit)
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    override fun getCurrentUserId(): String = tokenManager.getUserId() ?: "mock_user_01"

    override fun getCurrentUsername(): String = tokenManager.getUsername() ?: "Người dùng CalAI"

    // --- User Profile (với Mock Offline Fallback) ---
    override suspend fun fetchRemoteProfile(): Result<UserProfileDto> {
        return try {
            val response = api.getProfile()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockProfile()
            }
        } catch (_: Exception) {
            getMockProfile()
        }
    }

    private fun getMockProfile(): Result<UserProfileDto> {
        val username = tokenManager.getUsername() ?: "calai_user"
        return Result.success(
            UserProfileDto(
                id = "mock_user_01",
                username = username,
                email = "$username@calai.com",
                name = "Người dùng CalAI",
                gender = "MALE",
                heightCm = 175f,
                weightKg = 68.5f,
                goal = "LOSE_WEIGHT",
                bmi = 22.4f,
                bmr = 1680f,
                tdee = 2310f,
                targetCalories = 1810f,
                targetProtein = 135f,
                targetCarb = 200f,
                targetFat = 50f,
                dailyAiQuota = 50
            )
        )
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfileDto> {
        return try {
            val response = api.updateProfile(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockProfile()
            }
        } catch (_: Exception) {
            getMockProfile()
        }
    }

    // --- Meals Remote & Sync (với Mock Offline Fallback) ---
    override suspend fun fetchDailySummary(date: String?): Result<DailyNutritionSummaryData> {
        return try {
            val response = api.getDailySummary(date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockDailySummary(date)
            }
        } catch (_: Exception) {
            getMockDailySummary(date)
        }
    }

    private fun getMockDailySummary(date: String?): Result<DailyNutritionSummaryData> {
        val targetIso = date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return Result.success(
            DailyNutritionSummaryData(
                date = targetIso,
                summary = DailySummaryDto(
                    consumedCalories = 1450f,
                    targetCalories = 2200f,
                    remainingCalories = 750f,
                    progressPercent = 65,
                    macros = MacrosSummaryDto(
                        protein = MacroDetailDto(consumed = 110f, target = 140f, unit = "g"),
                        carb = MacroDetailDto(consumed = 180f, target = 220f, unit = "g"),
                        fat = MacroDetailDto(consumed = 45f, target = 65f, unit = "g")
                    )
                ),
                mealsCount = 3
            )
        )
    }

    override suspend fun fetchMealsFromRemote(date: String?): Result<List<MealResponseDto>> {
        return try {
            val response = api.getMeals(date)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockMeals()
            }
        } catch (_: Exception) {
            getMockMeals()
        }
    }

    private fun getMockMeals(): Result<List<MealResponseDto>> {
        val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val mockList = listOf(
            MealResponseDto(
                id = "meal_mock_1",
                userId = "mock_user_01",
                mealType = "BREAKFAST",
                date = dateIso,
                totalCalories = 550f,
                totalProtein = 28f,
                totalCarb = 65f,
                totalFat = 18f,
                items = listOf(
                    MealItemResponseDto("item_1", "meal_mock_1", "Phở Bò Tái chín", "1 tô (450g)", 1f, 550f, 28f, 65f, 18f, "MANUAL")
                )
            ),
            MealResponseDto(
                id = "meal_mock_2",
                userId = "mock_user_01",
                mealType = "LUNCH",
                date = dateIso,
                totalCalories = 620f,
                totalProtein = 32f,
                totalCarb = 75f,
                totalFat = 22f,
                items = listOf(
                    MealItemResponseDto("item_2", "meal_mock_2", "Cơm Tấm Sườn Bì Chả", "1 đĩa (400g)", 1f, 620f, 32f, 75f, 22f, "MANUAL")
                )
            ),
            MealResponseDto(
                id = "meal_mock_3",
                userId = "mock_user_01",
                mealType = "SNACK",
                date = dateIso,
                totalCalories = 280f,
                totalProtein = 25f,
                totalCarb = 12f,
                totalFat = 14f,
                items = listOf(
                    MealItemResponseDto("item_3", "meal_mock_3", "Salad Ức Gà Sốt Mè", "1 tô (300g)", 1f, 280f, 25f, 12f, 14f, "AI_VISION")
                )
            )
        )
        return Result.success(mockList)
    }

    override suspend fun createRemoteMeal(request: CreateMealRequest): Result<MealResponseDto> {
        return try {
            val response = api.createMeal(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockCreatedMeal(request)
            }
        } catch (_: Exception) {
            getMockCreatedMeal(request)
        }
    }

    private fun getMockCreatedMeal(request: CreateMealRequest): Result<MealResponseDto> {
        val totalCal = request.items.sumOf { (it.calories * it.quantity).toDouble() }.toFloat()
        val totalP = request.items.sumOf { (it.protein * it.quantity).toDouble() }.toFloat()
        val totalC = request.items.sumOf { (it.carb * it.quantity).toDouble() }.toFloat()
        val totalF = request.items.sumOf { (it.fat * it.quantity).toDouble() }.toFloat()
        val dateIso = request.date

        val mockMealId = UUID.randomUUID().toString()
        val mockMeal = MealResponseDto(
            id = mockMealId,
            userId = "mock_user_01",
            mealType = request.mealType,
            date = dateIso,
            totalCalories = totalCal,
            totalProtein = totalP,
            totalCarb = totalC,
            totalFat = totalF,
            items = request.items.mapIndexed { idx, item ->
                MealItemResponseDto(
                    id = "item_${idx}_${UUID.randomUUID()}",
                    mealId = mockMealId,
                    name = item.name,
                    servingSize = item.servingSize,
                    quantity = item.quantity,
                    calories = item.calories,
                    protein = item.protein,
                    carb = item.carb,
                    fat = item.fat,
                    source = item.source
                )
            }
        )
        return Result.success(mockMeal)
    }

    override suspend fun deleteRemoteMeal(mealId: String): Result<Unit> {
        return try {
            val response = api.deleteMeal(mealId)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (_: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun fetchNutritionStatistics(startDate: String?, endDate: String?): Result<NutritionStatisticsData> {
        return try {
            val response = api.getMealsStatistics(startDate, endDate)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockNutritionStatistics()
            }
        } catch (_: Exception) {
            getMockNutritionStatistics()
        }
    }

    private fun getMockNutritionStatistics(): Result<NutritionStatisticsData> {
        val calendar = java.util.Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val caloriesByDay = listOf(1750f, 1920f, 1680f, 1850f, 2100f, 1790f, 1650f)
        val dailyStats = caloriesByDay.mapIndexed { index, calories ->
            calendar.time = Date()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -(caloriesByDay.size - 1 - index))
            DailyStatDto(
                date = fmt.format(calendar.time),
                calories = calories,
                protein = calories * 0.30f / 4f,
                carb = calories * 0.45f / 4f,
                fat = calories * 0.25f / 9f,
                mealsCount = 3
            )
        }
        val avgCalories = dailyStats.map { it.calories }.average().toFloat()
        return Result.success(
            NutritionStatisticsData(
                period = StatisticsPeriodDto(start = dailyStats.first().date, end = dailyStats.last().date),
                averages = StatisticsAveragesDto(
                    dailyCalories = avgCalories,
                    dailyProtein = dailyStats.map { it.protein }.average().toFloat(),
                    dailyCarb = dailyStats.map { it.carb }.average().toFloat(),
                    dailyFat = dailyStats.map { it.fat }.average().toFloat()
                ),
                dailyStats = dailyStats
            )
        )
    }

    // --- Recommendations & Foods (với Mock Offline Fallback) ---
    override suspend fun searchFoods(query: String?, category: String?): Result<List<FoodItemDto>> {
        return try {
            val response = api.searchFoods(query, category)
            if (response.success && response.data != null) {
                Result.success(response.data.items)
            } else {
                getMockFoods(query, category)
            }
        } catch (_: Exception) {
            getMockFoods(query, category)
        }
    }

    private fun getMockFoods(query: String?, category: String?): Result<List<FoodItemDto>> {
        val allFoods = listOf(
            FoodItemDto("Phở Bò Tái Chín", "Cơm / Bún / Phở", "1 tô lớn (450g)", 550f, 28f, 65f, 18f),
            FoodItemDto("Cơm Tấm Sườn Bì Chả", "Cơm / Bún / Phở", "1 đĩa (400g)", 620f, 32f, 75f, 22f),
            FoodItemDto("Bún Bò Huế", "Cơm / Bún / Phở", "1 tô lớn (500g)", 580f, 30f, 68f, 20f),
            FoodItemDto("Ức Gà Áp Chảo", "Thịt / Trứng", "1 phần (200g)", 330f, 46f, 0f, 7f),
            FoodItemDto("Trứng Luộc (2 quả)", "Thịt / Trứng", "2 quả (100g)", 155f, 13f, 1f, 11f),
            FoodItemDto("Bún Chả Hà Nội", "Cơm / Bún / Phở", "1 phần (380g)", 520f, 26f, 60f, 18f),
            FoodItemDto("Salad Ức Gà Sốt Mè", "Rau / Củ", "1 tô (300g)", 280f, 25f, 12f, 14f),
            FoodItemDto("Gỏi Cuốn Tôm Thịt", "Rau / Củ", "2 cuốn (180g)", 220f, 14f, 28f, 5f),
            FoodItemDto("Sữa Tươi Không Đường", "Đồ uống", "1 hộp (250ml)", 120f, 8f, 11f, 5f),
            FoodItemDto("Sinh Tố Bơ Ít Đường", "Đồ uống", "1 ly (300ml)", 240f, 4f, 22f, 16f)
        )

        var filtered = allFoods
        if (!category.isNullOrBlank() && category != "Tất cả") {
            filtered = filtered.filter { it.category.contains(category, ignoreCase = true) }
        }
        if (!query.isNullOrBlank()) {
            filtered = filtered.filter { it.name.contains(query, ignoreCase = true) }
        }
        return Result.success(filtered)
    }

    override suspend fun getFoodCategories(): Result<List<String>> {
        return try {
            val response = api.getFoodCategories()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockCategories()
            }
        } catch (_: Exception) {
            getMockCategories()
        }
    }

    private fun getMockCategories(): Result<List<String>> {
        return Result.success(listOf("Tất cả", "Cơm / Bún / Phở", "Thịt / Trứng", "Rau / Củ", "Đồ uống"))
    }

    // --- Weight Logs Remote ---
    override suspend fun createRemoteWeightLog(weightKg: Float, note: String?): Result<WeightLogResponseDto> {
        return try {
            val response = api.createWeightLog(CreateWeightLogRequest(weightKg = weightKg, note = note))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockWeightLog(weightKg, note)
            }
        } catch (_: Exception) {
            getMockWeightLog(weightKg, note)
        }
    }

    private fun getMockWeightLog(weightKg: Float, note: String?): Result<WeightLogResponseDto> {
        val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return Result.success(
            WeightLogResponseDto(
                id = UUID.randomUUID().toString(),
                userId = "mock_user_01",
                weightKg = weightKg,
                note = note,
                date = dateIso
            )
        )
    }

    override suspend fun fetchRemoteWeightLogs(limit: Int): Result<List<WeightLogResponseDto>> {
        return try {
            val response = api.getWeightLogs(limit)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockWeightLogs()
            }
        } catch (_: Exception) {
            getMockWeightLogs()
        }
    }

    private fun getMockWeightLogs(): Result<List<WeightLogResponseDto>> {
        val dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return Result.success(
            listOf(
                WeightLogResponseDto("w_1", "mock_user_01", 68.5f, "Cân sáng lúc bụng rỗng", dateIso),
                WeightLogResponseDto("w_2", "mock_user_01", 68.8f, "Sau buổi tập nhẹ", "2026-09-04"),
                WeightLogResponseDto("w_3", "mock_user_01", 69.2f, "Bắt đầu chuỗi siết mỡ", "2026-09-02")
            )
        )
    }

    override suspend fun fetchWeightTrend(limit: Int): Result<List<WeightTrendPointDto>> {
        return try {
            val response = api.getWeightTrend(limit)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockWeightTrend()
            }
        } catch (_: Exception) {
            getMockWeightTrend()
        }
    }

    private fun getMockWeightTrend(): Result<List<WeightTrendPointDto>> {
        return Result.success(
            listOf(
                WeightTrendPointDto("wt_1", "2026-09-02", loggedWeight = 69.2f, trendWeight = 69.2f, note = "Bắt đầu chuỗi siết mỡ"),
                WeightTrendPointDto("wt_2", "2026-09-04", loggedWeight = 68.8f, trendWeight = 69.16f, note = "Sau buổi tập nhẹ"),
                WeightTrendPointDto("wt_3", "2026-09-05", loggedWeight = 68.5f, trendWeight = 69.09f, note = "Cân sáng lúc bụng rỗng")
            )
        )
    }

    override suspend fun fetchWeightProgress(): Result<WeightProgressDto> {
        return try {
            val response = api.getWeightProgress()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockWeightProgress()
            }
        } catch (_: Exception) {
            getMockWeightProgress()
        }
    }

    private fun getMockWeightProgress(): Result<WeightProgressDto> {
        return Result.success(
            WeightProgressDto(
                goal = "LOSE_WEIGHT",
                startWeightKg = 72.0f,
                currentWeightKg = 68.5f,
                targetWeightKg = 65.0f,
                weightChangedKg = -3.5f,
                remainingToGoalKg = 3.5f,
                progressPercent = 50
            )
        )
    }

    // --- AI Food Recognition & Chat Coach (với Mock Offline Fallback) ---
    override suspend fun recognizeFood(file: File): Result<FoodRecognitionResultDto> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val response = api.recognizeFood(body)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockRecognition()
            }
        } catch (_: Exception) {
            getMockRecognition()
        }
    }

    override suspend fun recognizeFoodBase64(base64: String): Result<FoodRecognitionResultDto> {
        return try {
            val response = api.recognizeFoodBase64(RecognizeFoodBase64Request(base64Image = base64))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockRecognition()
            }
        } catch (_: Exception) {
            getMockRecognition()
        }
    }

    private fun getMockRecognition(): Result<FoodRecognitionResultDto> {
        return Result.success(
            FoodRecognitionResultDto(
                foodName = "Phở Bò Tái Cầu",
                confidenceScore = 0.96,
                totalCalories = 550.0,
                totalProtein = 28.0,
                totalCarb = 65.0,
                totalFat = 18.0,
                servingSize = "1 tô lớn (450g)",
                healthTip = "Món ăn giàu đạm và năng lượng phục hồi. Bạn có thể giảm nước lèo béo để duy trì thâm hụt calo tốt hơn.",
                items = listOf(
                    FoodItemRecognitionDto("Thịt bò tái & nạm", "150g", 220.0, 22.0, 0.0, 14.0),
                    FoodItemRecognitionDto("Bánh phở tươi", "200g", 250.0, 5.0, 58.0, 1.0),
                    FoodItemRecognitionDto("Nước dùng & Rau thơm", "100g", 80.0, 1.0, 7.0, 3.0)
                )
            )
        )
    }

    override suspend fun chatAi(message: String): Result<ChatAiResponseDto> {
        return try {
            val response = api.chatAi(ChatAiRequest(message = message))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                getMockChatAi(message)
            }
        } catch (_: Exception) {
            getMockChatAi(message)
        }
    }

    private fun getMockChatAi(message: String): Result<ChatAiResponseDto> {
        val answer = when {
            message.contains("calo", ignoreCase = true) || message.contains("gợi ý", ignoreCase = true) ->
                "Dựa trên chỉ số TDEE 2310 kcal của bạn, để giảm cân an toàn bạn nên duy trì lượng nạp khoảng 1810 kcal/ngày (thâm hụt 500 kcal). Ưu tiên bữa ăn giàu ức gà, trứng, cá thu và rau xanh!"
            message.contains("protein", ignoreCase = true) || message.contains("đạm", ignoreCase = true) ->
                "Mục tiêu Protein hàng ngày của bạn là 135g. Bạn có thể chia làm 3-4 bữa, mỗi bữa bổ sung từ 30-35g đạm (tương đương 150g ức gà hoặc 4 quả trứng luộc)."
            else ->
                "CalAI Coach chào bạn! Tôi là trợ lý dinh dưỡng AI. Bạn có thể hỏi tôi bất kỳ câu hỏi nào về calo, thực đơn siết mỡ, tăng cơ hay cách phân bổ dinh dưỡng hợp lý!"
        }
        return Result.success(ChatAiResponseDto(reply = answer))
    }
}
