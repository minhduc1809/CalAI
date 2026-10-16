package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.DailyNutritionSummaryData
import com.calai.app.data.remote.dto.MealResponseDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Định dạng giờ:phút thực tế lúc log 1 bữa ăn (dùng `createdAt`) để hiển thị trên mỗi dòng ở chế độ Timeline. */
fun formatMealLogTime(createdAt: String?): String? {
    if (createdAt == null) return null
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isoParsers = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    )
    for (parser in isoParsers) {
        val parsed = try { parser.parse(createdAt) } catch (_: Exception) { null }
        if (parsed != null) return timeFormat.format(parsed)
    }
    return null
}

/** 1 nhóm hiển thị trong Nhật ký ăn uống — theo giờ (Timeline) hoặc theo loại bữa (Fixed Meals). */
data class MealGroup(
    val label: String,
    val meals: List<MealResponseDto>
)

data class HomeUiState(
    /** Các ngày trong tuần hiện tại đã có bữa ăn nhưng chưa được tính là đầy đủ (hiện chấm trên Week Strip). */
    val incompleteDates: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val username: String = "",
    val avatar: String? = null,
    val displayName: String? = null,
    val dailySummary: DailyNutritionSummaryData? = null,
    val meals: List<MealResponseDto> = emptyList(),
    val mealStructureMode: String = "TIMELINE",
    val weightText: String? = null,
    val targetWeightText: String? = null,
    val weightProgress: Float? = null,
    val errorMessage: String? = null
) {
    /** Nhóm `meals` theo chế độ hiển thị đang chọn — dữ liệu gốc không đổi, chỉ khác cách nhóm (đúng BRD). */
    val mealGroups: List<MealGroup>
        get() = if (mealStructureMode == "FIXED_MEALS") groupByMealType(meals) else groupByHour(meals)

    private fun groupByMealType(meals: List<MealResponseDto>): List<MealGroup> {
        val order = listOf("BREAKFAST" to "Bữa Sáng", "LUNCH" to "Bữa Trưa", "DINNER" to "Bữa Tối", "SNACK" to "Bữa Phụ")
        return order.mapNotNull { (type, label) ->
            val items = meals.filter { it.mealType == type }
            if (items.isEmpty()) null else MealGroup(label, items)
        }
    }

    private fun groupByHour(meals: List<MealResponseDto>): List<MealGroup> {
        val hourFormat = SimpleDateFormat("HH", Locale.getDefault())
        val isoParsers = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        )
        fun hourLabel(meal: MealResponseDto): String {
            val raw = meal.createdAt ?: return "Không rõ giờ"
            for (parser in isoParsers) {
                val parsed = try { parser.parse(raw) } catch (_: Exception) { null }
                if (parsed != null) return "${hourFormat.format(parsed)}:00"
            }
            return "Không rõ giờ"
        }
        return meals.groupBy(::hourLabel).map { (hour, items) -> MealGroup(hour, items) }
    }
}

/** Trong khoảng này sau lần tải trước, sự kiện quay lại màn hình không tải lại (giảm gọi API trùng). */
private const val FRESH_WINDOW_MS = 3_000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CalAIRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(mealStructureMode = preferencesManager.getMealStructureMode()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Bữa ăn/buổi tập đang chờ gửi hoặc bị từ chối (BR-07.5, BR-10.4). */
    val pendingRequests: StateFlow<List<com.calai.app.data.local.entity.PendingRequestEntity>> =
        repository.observePendingRequests()
            .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    fun retryPending(id: Long) = viewModelScope.launch { repository.retryPendingRequest(id) }
    fun deletePending(id: Long) = viewModelScope.launch { repository.deletePendingRequest(id) }

    // Phải khai báo TRƯỚC khối init: Kotlin khởi tạo theo thứ tự trong lớp; nếu đặt sau, giá trị do loadData() ở init
    // vừa ghi sẽ bị gán lại về 0/null và cơ chế chống tải trùng không hoạt động.
    private var lastLoadAtMs = 0L
    private var lastLoadDate: String? = null

    init {
        viewModelScope.launch {
            preferencesManager.mealStructureMode.collect { mode ->
                _uiState.update { it.copy(mealStructureMode = mode) }
            }
        }
        loadData()
    }

    /** Tải trạng thái "đầy đủ" của tuần hiện tại để Week Strip đánh dấu những ngày còn thiếu bữa. */
    private fun loadWeekStatus() {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance().apply {
            firstDayOfWeek = java.util.Calendar.MONDAY
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        }
        val monday = fmt.format(cal.time)
        val today = fmt.format(java.util.Date())
        viewModelScope.launch {
            repository.fetchWeekSummary(monday).onSuccess { days ->
                _uiState.update {
                    it.copy(
                        incompleteDates = days
                            .filter { d -> d.hasData && !d.isComplete && d.date < today }
                            .map { d -> d.date }
                            .toSet()
                    )
                }
            }
        }
    }

    /**
     * @param onlyIfStale true khi gọi từ sự kiện quay lại màn hình (ON_RESUME): bỏ qua nếu vừa tải cùng ngày trong
     * vài giây trước (lần mở app đầu tiên trước đây tải hai lần liên tiếp: một lần ở khởi tạo, một lần ở ON_RESUME).
     */
    fun loadData(date: String? = null, onlyIfStale: Boolean = false): Boolean {
        val now = android.os.SystemClock.elapsedRealtime()
        // null nghĩa là "hôm nay": quy về cùng khoá với ngày màn hình truyền vào để so sánh đúng
        val key = date ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        if (onlyIfStale && key == lastLoadDate && now - lastLoadAtMs < FRESH_WINDOW_MS) return false
        lastLoadAtMs = now
        lastLoadDate = key
        loadWeekStatus()
        val username = repository.getCurrentUsername() ?: "Người dùng"
        _uiState.update { it.copy(username = username, isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            // Ba lời gọi độc lập: chạy song song thay vì chờ từng cái (tổng thời gian ≈ cái chậm nhất)
            val summaryDeferred = async { repository.fetchDailySummary(date) }
            val mealsDeferred = async { repository.fetchMealsFromRemote(date) }
            val profileDeferred = async { repository.fetchRemoteProfile() }
            val summaryResult = summaryDeferred.await()
            val mealsResult = mealsDeferred.await()

            summaryResult.onSuccess { summaryData ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dailySummary = summaryData,
                        errorMessage = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể kết nối đến máy chủ"
                    )
                }
            }

            mealsResult.onSuccess { mealList ->
                _uiState.update { it.copy(meals = mealList) }
            }

            profileDeferred.await().onSuccess { profile ->
                val current = profile.weightKg
                val target = profile.targetWeightKg
                _uiState.update {
                    it.copy(
                        avatar = profile.avatar,
                        displayName = profile.name,
                        weightText = current?.let { kg -> preferencesManager.formatWeight(kg) },
                        targetWeightText = target?.let { kg -> preferencesManager.formatWeight(kg) },
                        // Độ gần mục tiêu: min/max giữa cân nặng hiện tại và mục tiêu (1.0 = đã đạt)
                        weightProgress = if (current != null && target != null && current > 0f && target > 0f)
                            minOf(current, target) / maxOf(current, target) else null
                    )
                }
            }
        }
        return true
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            val result = repository.deleteRemoteMeal(mealId)
            result.onSuccess {
                loadData()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Không thể xóa bữa ăn")
                }
            }
        }
    }

    fun changeMealType(mealId: String, newMealType: String) {
        viewModelScope.launch {
            repository.updateRemoteMeal(mealId, mealType = newMealType).onSuccess {
                loadData()
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Không thể đổi loại bữa ăn") }
            }
        }
    }

    /** Sửa số lượng 1 món bên trong bữa ăn đã log, hoặc gỡ hẳn (newQuantity <= 0). */
    fun updateMealItemQuantity(meal: com.calai.app.data.remote.dto.MealResponseDto, itemId: String, newQuantity: Float) {
        val updatedItems = if (newQuantity <= 0f) {
            meal.items.filterNot { it.id == itemId }
        } else {
            meal.items.map { if (it.id == itemId) it.copy(quantity = newQuantity) else it }
        }
        submitMealItems(meal.id, updatedItems)
    }

    /** Gỡ hẳn 1 món khỏi bữa ăn đã log (menu 'Sửa món ăn'). */
    fun removeMealItem(meal: com.calai.app.data.remote.dto.MealResponseDto, itemId: String) {
        submitMealItems(meal.id, meal.items.filterNot { it.id == itemId })
    }

    /** True nếu thao tác sẽ làm bữa không còn món nào, tức là xoá cả bữa: màn hình phải hỏi trước (BR-07.4). */
    fun wouldEmptyMeal(meal: com.calai.app.data.remote.dto.MealResponseDto, itemId: String, newQuantity: Float? = null): Boolean =
        (newQuantity == null || newQuantity <= 0f) && meal.items.all { it.id == itemId }

    private fun submitMealItems(mealId: String, items: List<com.calai.app.data.remote.dto.MealItemResponseDto>) {
        // Danh sách rỗng = xoá cả bữa trên máy chủ; chỉ đi đường deleteMeal sau khi người dùng xác nhận
        if (items.isEmpty()) return
        val request = items.map { item ->
            com.calai.app.data.remote.dto.CreateMealItemDto(
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
        viewModelScope.launch {
            repository.updateRemoteMeal(mealId, items = request).onSuccess {
                loadData()
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Không thể sửa món ăn") }
            }
        }
    }

    /**
     * BR-05.2: người dùng bấm "Đã ghi đủ" / "Chưa đủ" cho ngày đang xem; bấm lại lựa chọn đang chọn
     * thì trả về AUTO (để hệ thống tự quyết định). Tải lại ngày để cập nhật trạng thái.
     */
    fun setDayCompleteness(date: String, completeness: String) {
        viewModelScope.launch {
            repository.setDayStatus(date, completeness).onSuccess {
                loadData(date)
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Không thể cập nhật trạng thái ngày") }
            }
        }
    }

    fun copyMeal(mealId: String, targetDate: String) {
        viewModelScope.launch {
            repository.copyRemoteMeal(mealId, targetDate).onSuccess {
                loadData()
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Không thể sao chép bữa ăn") }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }
}
