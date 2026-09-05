package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.WeightLogResponseDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatsPeriod {
    DAILY,
    WEEKLY
}

data class DayCalorieStat(
    val dayLabel: String,
    val calories: Int,
    val target: Int = 2200
)

data class StatisticsUiState(
    val period: StatsPeriod = StatsPeriod.WEEKLY,
    val isLoading: Boolean = false,
    val weeklyStats: List<DayCalorieStat> = emptyList(),
    val averageCalories: Int = 1820,
    val daysUnderGoal: Int = 5,
    val weightLogs: List<WeightLogResponseDto> = emptyList(),
    val currentWeight: Float = 69.5f,
    val targetWeight: Float = 65.0f,
    val startWeight: Float = 72.0f
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun setPeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
    }

    private fun loadStatistics() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            // Tải lịch sử cân nặng từ API
            repository.fetchRemoteWeightLogs(limit = 14).onSuccess { logs ->
                val latest = logs.firstOrNull()?.weightKg ?: 69.5f
                val earliest = logs.lastOrNull()?.weightKg ?: 72.0f
                _uiState.value = _uiState.value.copy(
                    weightLogs = logs,
                    currentWeight = latest,
                    startWeight = earliest
                )
            }

            // Dữ liệu calo tuần mẫu thực tế
            val mockWeekly = listOf(
                DayCalorieStat("T2", 1750),
                DayCalorieStat("T3", 1920),
                DayCalorieStat("T4", 1680),
                DayCalorieStat("T5", 1850),
                DayCalorieStat("T6", 2100),
                DayCalorieStat("T7", 1790),
                DayCalorieStat("CN", 1650)
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                weeklyStats = mockWeekly,
                averageCalories = (mockWeekly.sumOf { it.calories } / mockWeekly.size)
            )
        }
    }
}
