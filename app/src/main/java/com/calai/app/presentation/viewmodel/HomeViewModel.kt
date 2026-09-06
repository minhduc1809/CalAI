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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
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
    val isLoading: Boolean = false,
    val username: String = "",
    val dailySummary: DailyNutritionSummaryData? = null,
    val meals: List<MealResponseDto> = emptyList(),
    val mealStructureMode: String = "TIMELINE",
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CalAIRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(mealStructureMode = preferencesManager.getMealStructureMode()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.mealStructureMode.collect { mode ->
                _uiState.update { it.copy(mealStructureMode = mode) }
            }
        }
        loadData()
    }

    fun loadData(date: String? = null) {
        val username = repository.getCurrentUsername() ?: "Người dùng"
        _uiState.update { it.copy(username = username, isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val summaryResult = repository.fetchDailySummary(date)
            val mealsResult = repository.fetchMealsFromRemote(date)

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
        }
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
