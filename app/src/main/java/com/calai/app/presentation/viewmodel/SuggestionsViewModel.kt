package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.DietRecommendationData
import com.calai.app.data.remote.dto.ExerciseGuideDto
import com.calai.app.data.remote.dto.MonthlyDietData
import com.calai.app.data.remote.dto.WorkoutRecommendationData
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuggestionsUiState(
    val isLoading: Boolean = true,
    val diet: DietRecommendationData? = null,
    val monthlyDiet: MonthlyDietData? = null,
    val showMonthlyDiet: Boolean = false,
    val selectedDayNumber: Int = 1,
    val workout: WorkoutRecommendationData? = null,
    val exercises: List<ExerciseGuideDto> = emptyList(),
    val selectedGender: String = "MALE",
    val selectedLevel: String? = null, // null = Tất cả
    val expandedExerciseId: String? = null,
    val expandedDayName: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuggestionsUiState())
    val uiState: StateFlow<SuggestionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val genderFromProfile = repository.fetchRemoteProfile().getOrNull()?.gender ?: "MALE"
            _uiState.update { it.copy(selectedGender = genderFromProfile) }
            loadAll()
        }
    }

    private fun loadAll() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val dietResult = repository.fetchDietRecommendation()
            val workoutResult = repository.fetchWorkoutRecommendation()
            val exercisesResult = repository.fetchExercises(_uiState.value.selectedGender, _uiState.value.selectedLevel)
            val monthlyResult = repository.fetchMonthlyDiet()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    diet = dietResult.getOrNull() ?: it.diet,
                    workout = workoutResult.getOrNull() ?: it.workout,
                    exercises = exercisesResult.getOrNull()?.exercises ?: it.exercises,
                    monthlyDiet = monthlyResult.getOrNull() ?: it.monthlyDiet,
                    errorMessage = if (dietResult.isFailure && workoutResult.isFailure) "Không thể tải gợi ý, vui lòng thử lại" else null
                )
            }
        }
    }

    fun toggleMonthlyView() {
        _uiState.update { it.copy(showMonthlyDiet = !it.showMonthlyDiet) }
    }

    fun selectDay(dayNumber: Int) {
        _uiState.update { it.copy(selectedDayNumber = dayNumber) }
    }

    fun selectGender(gender: String) {
        if (gender == _uiState.value.selectedGender) return
        _uiState.update { it.copy(selectedGender = gender) }
        reloadExercises()
    }

    fun selectLevel(level: String?) {
        if (level == _uiState.value.selectedLevel) return
        _uiState.update { it.copy(selectedLevel = level) }
        reloadExercises()
    }

    private fun reloadExercises() {
        viewModelScope.launch {
            repository.fetchExercises(_uiState.value.selectedGender, _uiState.value.selectedLevel).onSuccess { data ->
                _uiState.update { it.copy(exercises = data.exercises) }
            }
        }
    }

    fun toggleExerciseExpand(exerciseId: String) {
        _uiState.update {
            it.copy(expandedExerciseId = if (it.expandedExerciseId == exerciseId) null else exerciseId)
        }
    }

    fun toggleDayExpand(dayName: String) {
        _uiState.update {
            it.copy(expandedDayName = if (it.expandedDayName == dayName) null else dayName)
        }
    }
}
