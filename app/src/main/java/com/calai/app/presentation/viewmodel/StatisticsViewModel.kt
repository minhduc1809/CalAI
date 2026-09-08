package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.InsightDto
}

data class DayCalorieStat(
    val dayLabel: String,
    val calories: Int
)

data class StatisticsUiState(
    val period: StatsPeriod = StatsPeriod.WEEK,
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
    val weightProgressPercent: Int = 0,
    val weightUnit: String = "kg",
    val insights: List<InsightDto> = emptyList()
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

private fun monthLabelOf(dateIso: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateIso) ?: return dateIso
        val calendar = Calendar.getInstance().apply { time = date }
        "Th${calendar.get(Calendar.MONTH) + 1}"
    } catch (_: Exception) {
        dateIso
    }
}

/**
 * Gộp danh sách điểm theo ngày thành tối đa [maxPoints] bucket (tuần hoặc tháng) để biểu đồ
 * không bị rối khi hiển thị preset dài (quarter/year/all) — backend luôn trả về dữ liệu thô theo ngày.
 */
private fun bucketDailyStats(
    dailyStats: List<com.calai.app.data.remote.dto.DailyStatDto>,
    period: StatsPeriod
): List<DayCalorieStat> {
    if (dailyStats.size <= period.maxPoints || period == StatsPeriod.WEEK) {
        return dailyStats.map { DayCalorieStat(dayLabelOf(it.date), it.calories.toInt()) }
    }

    val bucketSizeDays = when (period) {
        StatsPeriod.MONTH -> 3
        StatsPeriod.QUARTER -> 7
        StatsPeriod.YEAR, StatsPeriod.ALL -> maxOf(30, dailyStats.size / period.maxPoints)
        StatsPeriod.WEEK -> 1
    }

    val buckets = dailyStats.chunked(bucketSizeDays)
    val useMonthLabel = period == StatsPeriod.YEAR || period == StatsPeriod.ALL
    return buckets.map { chunk ->
        val avgCalories = (chunk.sumOf { it.calories.toInt() } / chunk.size)
        val labelSource = chunk.last().date
        DayCalorieStat(
            dayLabel = if (useMonthLabel) monthLabelOf(labelSource) else dayLabelOf(labelSource),
            calories = avgCalories
        )
    }
}

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            // Mục tiêu calo hàng ngày lấy từ hồ sơ dinh dưỡng đã tính (Target Calculation Pipeline)
            val targetCalories = repository.fetchRemoteProfile()
                .getOrNull()?.targetCalories?.toInt() ?: 2200

            // Xu hướng cân nặng thật (EWMA, alpha = 0.1) + tiến độ mục tiêu — thay cho việc tự suy ra từ log thô
            repository.fetchWeightTrend(limit = 60).onSuccess { points ->
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

            // Thống kê calo & macro theo preset thời gian đang chọn (week/month/quarter/year/all)
            repository.fetchNutritionStatistics(preset = period.apiPreset).onSuccess { stats ->
                val bucketed = bucketDailyStats(stats.dailyStats, period)

                val proteinKcal = stats.averages.dailyProtein * 4f
                val carbKcal = stats.averages.dailyCarb * 4f
                val fatKcal = stats.averages.dailyFat * 9f
                val totalKcal = (proteinKcal + carbKcal + fatKcal).coerceAtLeast(1f)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    weeklyStats = bucketed,
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
