package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.*
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditableWorkoutSet(
    val setNumber: Int,
    val reps: Int = 10,
    val weightKg: Float = 50f,
    val rpe: Int? = 8,
    val isCompleted: Boolean = false
)

data class EditableWorkoutExercise(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val sets: List<EditableWorkoutSet> = listOf(
        EditableWorkoutSet(setNumber = 1, reps = 12, weightKg = 40f, rpe = 7),
        EditableWorkoutSet(setNumber = 2, reps = 10, weightKg = 50f, rpe = 8),
        EditableWorkoutSet(setNumber = 3, reps = 8, weightKg = 60f, rpe = 9)
    )
)

data class WorkoutUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val summary: WorkoutSummaryDto? = null,
    val history: List<WorkoutLogDto> = emptyList(),
    val categories: List<WorkoutCategoryInfoDto> = emptyList(),
    val selectedCategory: WorkoutCategory = WorkoutCategory.STRENGTH,
    val workoutName: String = "Buổi tập Thể lực & Tăng cơ",
    val durationMinutes: Int = 45,
    val caloriesBurned: Float = 225f, // MET 5.0 * 60kg * 45/60 = 225
    val rpe: Int = 8,
    val note: String = "",
    val exercises: List<EditableWorkoutExercise> = emptyList(),
    val isRestTimerRunning: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val totalRestSeconds: Int = 90,
    val selectedWorkoutDetail: WorkoutLogDto? = null,
    val userWeightKg: Float = 65f,
    val saveSuccessEvent: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var restTimerJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val userWeight = repository.fetchRemoteProfile().getOrNull()?.weightKg ?: 65f
            val categoriesResult = repository.fetchWorkoutCategories()
            val summaryResult = repository.fetchWorkoutSummary()
            val historyResult = repository.fetchWorkouts()

            val categories = categoriesResult.getOrNull() ?: emptyList()
            val defaultMet = categories.find { it.category == WorkoutCategory.STRENGTH }?.met ?: 5.0f
            val defaultCalo = calculateMetCalories(defaultMet, 45, userWeight)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userWeightKg = userWeight,
                    categories = categories,
                    summary = summaryResult.getOrNull(),
                    history = historyResult.getOrNull() ?: emptyList(),
                    caloriesBurned = defaultCalo
                )
            }
        }
    }

    private fun calculateMetCalories(met: Float, durationMinutes: Int, weightKg: Float): Float {
        val cal = met * weightKg * (durationMinutes.toFloat() / 60f)
        return (Math.round(cal * 10) / 10f)
    }

    fun setCategory(category: WorkoutCategory) {
        val catInfo = _uiState.value.categories.find { it.category == category }
        val met = catInfo?.met ?: 5.0f
        val newCalo = calculateMetCalories(met, _uiState.value.durationMinutes, _uiState.value.userWeightKg)
        val defaultName = when (category) {
            WorkoutCategory.STRENGTH -> "Buổi tập Ngực & Tay sau"
            WorkoutCategory.RUNNING -> "Chạy bộ ngoài trời"
            WorkoutCategory.HIIT -> "Cardio HIIT đốt mỡ 30p"
            WorkoutCategory.CYCLING -> "Đạp xe rèn sức bền"
            WorkoutCategory.SWIMMING -> "Bơi lội giải phóng cơ thể"
            WorkoutCategory.YOGA -> "Yoga & Giãn cơ phục hồi"
            WorkoutCategory.SPORTS -> "Giao lưu Thể thao đối kháng"
            WorkoutCategory.WALKING -> "Đi bộ nhanh thư giãn"
            WorkoutCategory.CARDIO -> "Cardio toàn thân đốt calo"
            WorkoutCategory.OTHER -> "Hoạt động thể chất tự do"
        }

        _uiState.update {
            it.copy(
                selectedCategory = category,
                workoutName = if (it.workoutName.isBlank() || it.workoutName.startsWith("Buổi tập")) defaultName else it.workoutName,
                caloriesBurned = newCalo
            )
        }
    }

    fun setWorkoutName(name: String) {
        _uiState.update { it.copy(workoutName = name) }
    }

    fun setDuration(minutes: Int) {
        val safeMinutes = minutes.coerceIn(1, 720)
        val catInfo = _uiState.value.categories.find { it.category == _uiState.value.selectedCategory }
        val met = catInfo?.met ?: 5.0f
        val newCalo = calculateMetCalories(met, safeMinutes, _uiState.value.userWeightKg)
        _uiState.update { it.copy(durationMinutes = safeMinutes, caloriesBurned = newCalo) }
    }

    fun setCalories(calories: Float) {
        _uiState.update { it.copy(caloriesBurned = calories.coerceAtLeast(0f)) }
    }

    fun setRpe(rpe: Int) {
        _uiState.update { it.copy(rpe = rpe.coerceIn(1, 10)) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun addExercise(name: String) {
        if (name.isBlank()) return
        val newEx = EditableWorkoutExercise(
            name = name.trim(),
            sets = listOf(
                EditableWorkoutSet(setNumber = 1, reps = 12, weightKg = 40f, rpe = 8),
                EditableWorkoutSet(setNumber = 2, reps = 10, weightKg = 45f, rpe = 8),
                EditableWorkoutSet(setNumber = 3, reps = 8, weightKg = 50f, rpe = 9)
            )
        )
        _uiState.update { it.copy(exercises = it.exercises + newEx) }
    }

    fun removeExercise(exerciseId: String) {
        _uiState.update { it.copy(exercises = it.exercises.filterNot { ex -> ex.id == exerciseId }) }
    }

    fun addSet(exerciseId: String) {
        _uiState.update { state ->
            val updated = state.exercises.map { ex ->
                if (ex.id == exerciseId) {
                    val nextSetNumber = (ex.sets.maxOfOrNull { it.setNumber } ?: 0) + 1
                    val lastSet = ex.sets.lastOrNull()
                    val newSet = EditableWorkoutSet(
                        setNumber = nextSetNumber,
                        reps = lastSet?.reps ?: 10,
                        weightKg = lastSet?.weightKg ?: 40f,
                        rpe = lastSet?.rpe ?: 8,
                        isCompleted = false
                    )
                    ex.copy(sets = ex.sets + newSet)
                } else ex
            }
            state.copy(exercises = updated)
        }
    }

    fun removeSet(exerciseId: String, setNumber: Int) {
        _uiState.update { state ->
            val updated = state.exercises.map { ex ->
                if (ex.id == exerciseId) {
                    val remaining = ex.sets.filterNot { it.setNumber == setNumber }
                        .mapIndexed { idx, s -> s.copy(setNumber = idx + 1) }
                    ex.copy(sets = remaining)
                } else ex
            }
            state.copy(exercises = updated)
        }
    }

    fun updateSet(exerciseId: String, setNumber: Int, reps: Int, weightKg: Float, rpe: Int?) {
        _uiState.update { state ->
            val updated = state.exercises.map { ex ->
                if (ex.id == exerciseId) {
                    val newSets = ex.sets.map { s ->
                        if (s.setNumber == setNumber) s.copy(reps = reps, weightKg = weightKg, rpe = rpe) else s
                    }
                    ex.copy(sets = newSets)
                } else ex
            }
            state.copy(exercises = updated)
        }
    }

    fun completeSet(exerciseId: String, setNumber: Int, restSeconds: Int = 90) {
        _uiState.update { state ->
            val updated = state.exercises.map { ex ->
                if (ex.id == exerciseId) {
                    val newSets = ex.sets.map { s ->
                        if (s.setNumber == setNumber) s.copy(isCompleted = !s.isCompleted) else s
                    }
                    ex.copy(sets = newSets)
                } else ex
            }
            state.copy(exercises = updated)
        }
        startRestTimer(restSeconds)
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _uiState.update {
            it.copy(
                isRestTimerRunning = true,
                totalRestSeconds = seconds,
                restSecondsRemaining = seconds
            )
        }
        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restSecondsRemaining > 0) {
                delay(1000L)
                _uiState.update { it.copy(restSecondsRemaining = (it.restSecondsRemaining - 1).coerceAtLeast(0)) }
            }
            _uiState.update { it.copy(isRestTimerRunning = false) }
        }
    }

    fun stopRestTimer() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isRestTimerRunning = false, restSecondsRemaining = 0) }
    }

    fun prefillFromProgram(title: String, exerciseNames: List<String>) {
        val newExercises = exerciseNames.mapIndexed { index, name ->
            EditableWorkoutExercise(
                name = name,
                sets = listOf(
                    EditableWorkoutSet(setNumber = 1, reps = 12, weightKg = 30f, rpe = 7),
                    EditableWorkoutSet(setNumber = 2, reps = 10, weightKg = 40f, rpe = 8),
                    EditableWorkoutSet(setNumber = 3, reps = 8, weightKg = 50f, rpe = 8)
                )
            )
        }
        _uiState.update {
            it.copy(
                workoutName = title,
                selectedCategory = WorkoutCategory.STRENGTH,
                durationMinutes = 60,
                exercises = newExercises
            )
        }
    }

    fun selectWorkoutDetail(workout: WorkoutLogDto?) {
        _uiState.update { it.copy(selectedWorkoutDetail = workout) }
    }

    fun saveWorkout(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.workoutName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên buổi tập") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val exerciseRequests = if (state.selectedCategory == WorkoutCategory.STRENGTH && state.exercises.isNotEmpty()) {
                state.exercises.mapIndexed { idx, ex ->
                    CreateWorkoutExerciseRequest(
                        name = ex.name,
                        order = idx + 1,
                        sets = ex.sets.map { s ->
                            CreateWorkoutSetRequest(
                                setNumber = s.setNumber,
                                reps = s.reps,
                                weightKg = s.weightKg,
                                rpe = s.rpe
                            )
                        }
                    )
                }
            } else null

            val request = CreateWorkoutLogRequest(
                name = state.workoutName.trim(),
                category = state.selectedCategory.name,
                durationMinutes = state.durationMinutes,
                caloriesBurned = state.caloriesBurned,
                rpe = state.rpe,
                note = state.note.ifBlank { null },
                exercises = exerciseRequests
            )

            val result = repository.createWorkoutLog(request)
            if (result.isSuccess) {
                // Reload summary and history
                loadData()
                _uiState.update { it.copy(isSubmitting = false, saveSuccessEvent = true) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể lưu buổi tập, vui lòng thử lại"
                    )
                }
            }
        }
    }

    fun deleteWorkout(id: String) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(id)
            _uiState.update { state ->
                state.copy(
                    history = state.history.filterNot { it.id == id },
                    selectedWorkoutDetail = if (state.selectedWorkoutDetail?.id == id) null else state.selectedWorkoutDetail
                )
            }
        }
    }

    fun resetForm() {
        val defaultMet = _uiState.value.categories.find { it.category == WorkoutCategory.STRENGTH }?.met ?: 5.0f
        val defaultCalo = calculateMetCalories(defaultMet, 45, _uiState.value.userWeightKg)
        _uiState.update {
            it.copy(
                selectedCategory = WorkoutCategory.STRENGTH,
                workoutName = "Buổi tập Ngực & Tay sau",
                durationMinutes = 45,
                caloriesBurned = defaultCalo,
                rpe = 8,
                note = "",
                exercises = listOf(
                    EditableWorkoutExercise(
                        name = "Barbell Bench Press (Đẩy ngực ngang)",
                        sets = listOf(
                            EditableWorkoutSet(1, 12, 50f, 7),
                            EditableWorkoutSet(2, 10, 60f, 8),
                            EditableWorkoutSet(3, 8, 70f, 9)
                        )
                    )
                ),
                errorMessage = null,
                saveSuccessEvent = false
            )
        }
    }
}
