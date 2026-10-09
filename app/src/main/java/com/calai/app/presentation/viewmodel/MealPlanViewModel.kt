package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CreateMealRequest
import com.calai.app.data.remote.dto.MealPlanDto
import com.calai.app.data.remote.dto.MealPlanSlotDto
import com.calai.app.data.remote.dto.TodayMealPlanDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MealPlanUiState(
    val isLoadingToday: Boolean = true,
    val today: TodayMealPlanDto? = null,
    val todayError: String? = null,
    // true khi hồ sơ chưa có mục tiêu: màn hình mời hoàn tất hồ sơ thay vì báo lỗi chung
    val needsProfile: Boolean = false,
    val planDays: Int = 7,
    val isLoadingPlan: Boolean = false,
    val plan: MealPlanDto? = null,
    val planError: String? = null,
    val selectedDayIndex: Int = 0,
    val loggingMealType: String? = null,
    val message: String? = null
)

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    init {
        refreshToday()
        loadPlan(7)
    }

    private fun isProfileIncomplete(message: String?) = message?.contains("hoàn tất hồ sơ", ignoreCase = true) == true

    fun refreshToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingToday = true, todayError = null) }
            repository.fetchTodayMealPlan().onSuccess { data ->
                _uiState.update { it.copy(isLoadingToday = false, today = data, needsProfile = false) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoadingToday = false,
                        needsProfile = isProfileIncomplete(e.message),
                        todayError = e.message ?: "Không tải được thực đơn hôm nay"
                    )
                }
            }
        }
    }

    fun loadPlan(days: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(planDays = days, isLoadingPlan = true, planError = null, selectedDayIndex = 0) }
            repository.fetchMealPlan(days).onSuccess { data ->
                _uiState.update { it.copy(isLoadingPlan = false, plan = data) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingPlan = false, plan = null, planError = e.message ?: "Không tải được kế hoạch") }
            }
        }
    }

    fun selectDay(index: Int) = _uiState.update { it.copy(selectedDayIndex = index) }
    fun consumeMessage() = _uiState.update { it.copy(message = null) }

    /**
     * Ghi các món chưa ăn của một bữa vào nhật ký. Mỗi món ghi với calo trên 1 phần × số phần, đúng quy ước
     * của nhật ký; nguồn là "diet_plan". Chỉ ghi cho ngày hôm nay (người dùng thực sự ăn món đó).
     */
    fun logSlot(date: String, slot: MealPlanSlotDto) {
        val items = slot.items.filter { !it.logged }
        if (items.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(loggingMealType = slot.mealType) }
            val request = CreateMealRequest(
                mealType = slot.mealType,
                date = date,
                items = items.map {
                    CreateMealItemDto(
                        name = it.name,
                        servingSize = it.servingSize,
                        quantity = it.quantity,
                        calories = it.unit.calories,
                        protein = it.unit.protein,
                        carb = it.unit.carb,
                        fat = it.unit.fat,
                        source = "diet_plan"
                    )
                }
            )
            repository.createRemoteMeal(request).onSuccess {
                _uiState.update { it.copy(loggingMealType = null, message = "Đã ghi vào nhật ký") }
                refreshToday()
            }.onFailure { e ->
                _uiState.update { it.copy(loggingMealType = null, message = e.message ?: "Không ghi được vào nhật ký") }
            }
        }
    }
}
