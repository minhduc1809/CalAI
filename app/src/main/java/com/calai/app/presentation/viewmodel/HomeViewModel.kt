package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.DailyNutritionSummaryData
import com.calai.app.data.remote.dto.MealResponseDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val dailySummary: DailyNutritionSummaryData? = null,
    val meals: List<MealResponseDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
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
