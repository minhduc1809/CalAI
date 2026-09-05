package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.WeightTrendPointDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class StatsPeriod {
    DAILY,
    WEEKLY
}

data class DayCalorieStat(
    val dayLabel: String,
    val calories: Int
)

data class StatisticsUiState(
    val period: StatsPeriod = StatsPeriod.WEEKLY,
    val isLoading: Boolean = false,
    val weeklyStats: List<DayCalorieStat> = emptyList(),
    val averageCalories: Int = 0,
    val targetCalories: Int = 2200,
    val daysUnderGoal: Int = 0,
    val proteinPercent: Int = 33,
    val carbPercent: Int = 34,
    val fatPercent: Int = 33,
    val weightTrendPoints: List<WeightTrendPointDto> = emptyList(),
    val currentWeight: Float = 0f,
    val startWeight: Float = 0f,
    val targetWeight: Float = 0f,
    val weightChangedKg: Float = 0f,
    val weightProgressPercent: Int = 0
)

/** Thứ trong tuần theo Calendar.DAY_OF_WEEK (SUNDAY = 1 ... SATURDAY = 7), quy ước Việt Nam T2..CN. */
private val VI_DAY_LABELS = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")

private fun dayLabelOf(dateIso: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateIso) ?: return dateIso
        val calendar = Calendar.getInstance().apply { time = date }
        VI_DAY_LABELS[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    } catch (_: Exception) {
        dateIso
    }
}

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
            // Mục tiêu calo hàng ngày lấy từ hồ sơ dinh dưỡng đã tính (Target Calculation Pipeline)
            val targetCalories = repository.fetchRemoteProfile()
                .getOrNull()?.targetCalories?.toInt() ?: 2200

            // Xu hướng cân nặng thật (EWMA, alpha = 0.1) + tiến độ mục tiêu — thay cho việc tự suy ra từ log thô
            repository.fetchWeightTrend(limit = 30).onSuccess { points ->
                _uiState.value = _uiState.value.copy(weightTrendPoints = points)
            }
            repository.fetchWeightProgress().onSuccess { progress ->
                _uiState.value = _uiState.value.copy(
                    currentWeight = progress.currentWeightKg ?: _uiState.value.currentWeight,
                    startWeight = progress.startWeightKg ?: _uiState.value.startWeight,
                    targetWeight = progress.targetWeightKg ?: _uiState.value.targetWeight,
                    weightChangedKg = progress.weightChangedKg,
                    weightProgressPercent = progress.progressPercent
                )
            }

            // Thống kê calo & macro trung bình 7 ngày gần nhất (mặc định của backend khi không truyền khoảng ngày)
            repository.fetchNutritionStatistics().onSuccess { stats ->
                val weekly = stats.dailyStats.map { day ->
                    DayCalorieStat(
                        dayLabel = dayLabelOf(day.date),
                        calories = day.calories.toInt()
                    )
                }

                val proteinKcal = stats.averages.dailyProtein * 4f
                val carbKcal = stats.averages.dailyCarb * 4f
                val fatKcal = stats.averages.dailyFat * 9f
                val totalKcal = (proteinKcal + carbKcal + fatKcal).coerceAtLeast(1f)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    weeklyStats = weekly,
                    averageCalories = stats.averages.dailyCalories.toInt(),
                    targetCalories = targetCalories,
                    daysUnderGoal = stats.dailyStats.count { it.calories <= targetCalories },
                    proteinPercent = ((proteinKcal / totalKcal) * 100).toInt(),
                    carbPercent = ((carbKcal / totalKcal) * 100).toInt(),
                    fatPercent = ((fatKcal / totalKcal) * 100).toInt()
                )
            }
        }
    }
}
