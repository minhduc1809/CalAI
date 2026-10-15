package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.ActivityCheckDto
import com.calai.app.data.remote.dto.CustomExerciseDto
import com.calai.app.data.remote.dto.NewRecordDto
import com.calai.app.data.remote.dto.RecordsDto
import com.calai.app.data.remote.dto.RoutineDto
import com.calai.app.data.remote.dto.SetsPerMuscleDto
import com.calai.app.data.remote.dto.VolumeStatsDto
import com.calai.app.data.remote.dto.WeightJumpWarningDto
import com.calai.app.data.remote.dto.WorkoutCategoryInfoDto
import com.calai.app.data.remote.dto.WorkoutHistoryDto
import com.calai.app.data.remote.dto.WorkoutLogDto
import com.calai.app.data.remote.dto.WorkoutSummaryDto
import com.calai.app.data.remote.dto.WorkoutTemplateDto
import com.calai.app.data.workout.DraftExercise
import com.calai.app.data.workout.DraftSet
import com.calai.app.data.workout.WorkoutDraft
import com.calai.app.data.workout.WorkoutDraftStore
import com.calai.app.data.workout.WorkoutSessionLogic
import com.calai.app.data.workout.isAbandoned
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Tóm tắt hiện ngay sau khi lưu buổi tập: thời gian, volume, calo, kỷ lục mới và cảnh báo (BR-10.4). */
data class SaveSummary(
    val workoutName: String,
    val durationMinutes: Int,
    val volumeKg: Float,
    val calories: Float?,
    /** true khi mất mạng: buổi tập đã lưu trên máy và sẽ tự gửi khi có mạng. */
    val queued: Boolean,
    val newRecords: List<NewRecordDto>,
    val warnings: List<WeightJumpWarningDto>,
    val savedWorkoutId: String?
)

/** Phiên dở lúc mở màn Ghi tập: tiếp tục bình thường, hay đã quá 12 giờ nên hỏi lưu hay huỷ. */
enum class ResumeState { NONE, RESUME, ABANDONED }

data class WorkoutUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val summary: WorkoutSummaryDto? = null,
    val history: List<WorkoutLogDto> = emptyList(),
    val categories: List<WorkoutCategoryInfoDto> = emptyList(),
    val userWeightKg: Float = 65f,
    val weightUnit: String = "kg",
    val draft: WorkoutDraft? = null,
    val resume: ResumeState = ResumeState.NONE,
    val isRestTimerRunning: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val totalRestSeconds: Int = 90,
    val selectedWorkoutDetail: WorkoutLogDto? = null,
    val saveSummary: SaveSummary? = null,
    val routines: List<RoutineDto> = emptyList(),
    val customExercises: List<CustomExerciseDto> = emptyList(),
    val library: List<com.calai.app.data.remote.dto.ExerciseGuideDto> = emptyList(),
    val lastTemplate: WorkoutTemplateDto? = null,
    val activityCheck: ActivityCheckDto? = null,
    val monthHistory: WorkoutHistoryDto? = null,
    val records: RecordsDto? = null,
    val volume: VolumeStatsDto? = null,
    val setsPerMuscle: SetsPerMuscleDto? = null,
    val errorMessage: String? = null
) {
    /** Calo ước tính hiển thị khi đang soạn: tự nhập thì lấy số đó, không thì theo MET net (BR-10.5). */
    fun estimatedCalories(nowMs: Long): Float {
        val d = draft ?: return 0f
        d.caloriesOverride?.let { return it }
        val met = categories.find { it.category.name == d.category }?.met ?: 4.0f
        return WorkoutSessionLogic.netCalories(met, WorkoutSessionLogic.effectiveDuration(d, nowMs), userWeightKg)
    }
}

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: CalAIRepository,
    private val draftStore: WorkoutDraftStore,
    private val preferences: UserPreferencesManager
) : ViewModel() {

    private val _ui = MutableStateFlow(
        WorkoutUiState(weightUnit = preferences.getWeightUnit())
    )

    /** Trạng thái màn hình = dữ liệu tải về + phiên tập đang soạn (dùng chung giữa Hub và màn Ghi tập). */
    val uiState: StateFlow<WorkoutUiState> =
        combine(_ui, draftStore.draft) { ui, draft -> ui.copy(draft = draft) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, _ui.value)

    private var restTimerJob: Job? = null

    init {
        refreshResumeState()
        loadData()
    }

    // ── Tải dữ liệu ──

    fun loadLibrary() {
        if (_ui.value.library.isNotEmpty()) return
        viewModelScope.launch {
            repository.fetchExercises().getOrNull()?.let { d -> _ui.update { it.copy(library = d.exercises) } }
        }
    }

    fun loadData() {
        _ui.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val userWeight = repository.fetchRemoteProfile().getOrNull()?.weightKg ?: 65f
            val categories = repository.fetchWorkoutCategories().getOrNull() ?: emptyList()
            val summary = repository.fetchWorkoutSummary().getOrNull()
            val history = repository.fetchWorkouts().getOrNull() ?: emptyList()
            _ui.update {
                it.copy(
                    isLoading = false,
                    userWeightKg = userWeight,
                    categories = categories,
                    summary = summary,
                    history = history
                )
            }
        }
        viewModelScope.launch {
            val routines = repository.fetchWorkoutRoutines().getOrNull() ?: emptyList()
            val custom = repository.fetchCustomExercises().getOrNull() ?: emptyList()
            val template = repository.fetchLastWorkoutTemplate().getOrNull()?.template
            _ui.update { it.copy(routines = routines, customExercises = custom, lastTemplate = template) }
        }
    }

    /** Đối chiếu số buổi tập thực tế (BR-10.7): chỉ đề xuất, người dùng quyết định. */
    fun loadActivityCheck() {
        viewModelScope.launch {
            val check = repository.fetchActivityCheck().getOrNull()
            _ui.update { it.copy(activityCheck = check?.takeIf { c -> c.eligible && c.mismatch == true }) }
        }
    }

    fun dismissActivityCheck() {
        _ui.update { it.copy(activityCheck = null) }
        viewModelScope.launch { repository.markActivityCheckHandled() }
    }

    /** Đồng ý đề xuất: cập nhật số buổi tập khai báo, mục tiêu mới chỉ là đề xuất (BR-04), không tự áp. */
    fun acceptActivityCheck(onDone: (Boolean, String) -> Unit) {
        val suggested = _ui.value.activityCheck?.suggestedTrainingDays ?: return
        viewModelScope.launch {
            repository.updateProfile(com.calai.app.data.remote.dto.UpdateProfileRequest(trainingDaysPerWeek = suggested))
                .onSuccess {
                    repository.markActivityCheckHandled()
                    _ui.update { it.copy(activityCheck = null) }
                    onDone(true, "Đã cập nhật số buổi tập: $suggested buổi/tuần")
                }
                .onFailure { onDone(false, it.message ?: "Không thể cập nhật hồ sơ") }
        }
    }

    fun loadMonth(month: String?) {
        viewModelScope.launch {
            val h = repository.fetchWorkoutHistory(month).getOrNull()
            if (h != null) _ui.update { it.copy(monthHistory = h) }
        }
    }

    fun loadProgress() {
        viewModelScope.launch {
            val records = repository.fetchWorkoutRecords().getOrNull()
            val volume = repository.fetchVolumeStats(weeks = 8).getOrNull()
            val sets = repository.fetchSetsPerMuscle(weeks = 1).getOrNull()
            _ui.update { it.copy(records = records, volume = volume, setsPerMuscle = sets) }
        }
    }

    // ── Bắt đầu / tiếp tục phiên ──

    private fun refreshResumeState() {
        val d = draftStore.draft.value
        val state = when {
            d == null || !d.hasContent -> ResumeState.NONE
            d.isAbandoned() -> ResumeState.ABANDONED
            else -> ResumeState.RESUME
        }
        _ui.update { it.copy(resume = state) }
    }

    /** Có phiên dở đáng giữ không (Hub dùng để hỏi trước khi thay bằng phiên mới). */
    fun hasUnfinishedSession(): Boolean = draftStore.draft.value?.hasContent == true

    /** Vào màn Ghi tập: chưa có phiên thì mở phiên trống; có phiên dở thì giữ nguyên và hiện hộp thoại tiếp tục. */
    fun ensureDraft() {
        if (draftStore.draft.value == null) startBlank()
        refreshResumeState()
    }

    fun acknowledgeResume() {
        _ui.update { it.copy(resume = ResumeState.NONE) }
    }

    fun startBlank(category: String = "STRENGTH") {
        draftStore.start(WorkoutDraft(category = category, name = defaultName(category)))
        _ui.update { it.copy(resume = ResumeState.NONE, saveSummary = null, errorMessage = null) }
    }

    fun startFromTemplate(template: WorkoutTemplateDto) {
        draftStore.start(
            WorkoutDraft(
                category = template.category,
                name = template.name,
                durationMinutes = template.durationMinutes ?: 45,
                exercises = template.exercises.map { WorkoutSessionLogic.exerciseFromTemplate(it) }
            )
        )
        _ui.update { it.copy(resume = ResumeState.NONE, saveSummary = null, errorMessage = null) }
    }

    fun startFromRoutine(routine: RoutineDto) {
        draftStore.start(
            WorkoutDraft(
                category = routine.category,
                name = routine.name,
                exercises = routine.items.map {
                    WorkoutSessionLogic.exerciseFromTemplate(
                        com.calai.app.data.remote.dto.TemplateExerciseDto(
                            it.name, it.exerciseSource, it.exerciseId, it.primaryMuscle, it.restSec, it.sets
                        )
                    )
                }
            )
        )
        _ui.update { it.copy(resume = ResumeState.NONE, saveSummary = null, errorMessage = null) }
    }

    /** Bắt đầu từ ngày tập của lộ trình gợi ý: mỗi bài một set trống, số liệu điền sẵn từ lần tập trước nếu có. */
    fun startFromProgram(title: String, exerciseNames: List<String>) {
        draftStore.start(WorkoutDraft(category = "STRENGTH", name = title, durationMinutes = 60))
        exerciseNames.forEach { addExercise(it) }
        _ui.update { it.copy(resume = ResumeState.NONE, saveSummary = null, errorMessage = null) }
    }

    fun startFromExercise(name: String, source: String? = null, refId: String? = null, muscle: String? = null) {
        draftStore.start(WorkoutDraft(category = "STRENGTH", name = "Buổi tập $name"))
        addExercise(name, source, refId, muscle)
        _ui.update { it.copy(resume = ResumeState.NONE, saveSummary = null, errorMessage = null) }
    }

    fun discardDraft() {
        stopRestTimer()
        draftStore.clear()
        _ui.update { it.copy(resume = ResumeState.NONE) }
    }

    private fun defaultName(category: String) = when (category) {
        "STRENGTH" -> "Buổi tập tạ"
        "RUNNING" -> "Chạy bộ"
        "HIIT" -> "HIIT"
        "CYCLING" -> "Đạp xe"
        "SWIMMING" -> "Bơi lội"
        "YOGA" -> "Yoga & giãn cơ"
        "SPORTS" -> "Thể thao"
        "WALKING" -> "Đi bộ"
        "CARDIO" -> "Cardio"
        else -> "Hoạt động thể chất"
    }

    // ── Soạn phiên ──

    fun setCategory(category: String) {
        draftStore.update { d ->
            val wasDefault = d.name.isBlank() || d.name == defaultName(d.category)
            d.copy(category = category, name = if (wasDefault) defaultName(category) else d.name, caloriesOverride = null)
        }
    }

    fun setName(name: String) = draftStore.update { it.copy(name = name) }

    fun setDuration(minutes: Int) =
        draftStore.update { it.copy(durationMinutes = minutes.coerceIn(1, 600), durationManual = true, caloriesOverride = it.caloriesOverride) }

    fun setCalories(calories: Float?) = draftStore.update { it.copy(caloriesOverride = calories?.coerceAtLeast(0f)) }
    fun setRpe(rpe: Int) = draftStore.update { it.copy(rpe = rpe.coerceIn(1, 10)) }
    fun setNote(note: String) = draftStore.update { it.copy(note = note) }

    /**
     * Thêm bài vào phiên với một set trống rồi điền sẵn số lần và tạ từ lần tập gần nhất của bài (BR-10.4).
     * Lịch sử tải sau nên người dùng không phải chờ.
     */
    fun addExercise(name: String, source: String? = null, refId: String? = null, muscle: String? = null) {
        val clean = name.trim()
        if (clean.isBlank()) return
        val ex = DraftExercise(name = clean, source = source, refId = refId, primaryMuscle = muscle, restSec = preferences.getDefaultRestSec(), sets = listOf(DraftSet()))
        draftStore.update { it.copy(exercises = it.exercises + ex) }
        val key = WorkoutSessionLogic.exerciseKey(ex)
        viewModelScope.launch {
            val last = repository.fetchLastSets(key).getOrNull() ?: return@launch
            if (last.sets.isEmpty()) return@launch
            draftStore.update { d ->
                d.copy(
                    exercises = d.exercises.map { e ->
                        // Chỉ điền khi người dùng chưa đụng tới bài này (chưa đánh dấu set nào)
                        if (e.id == ex.id && e.sets.none { it.isCompleted }) {
                            e.copy(sets = WorkoutSessionLogic.prefillSets(last.sets), lastMaxKg = WorkoutSessionLogic.lastMaxKg(last.sets))
                        } else e
                    }
                )
            }
        }
    }

    fun removeExercise(exerciseId: String) =
        draftStore.update { it.copy(exercises = it.exercises.filterNot { e -> e.id == exerciseId }) }

    fun setRest(exerciseId: String, seconds: Int) = mapExercise(exerciseId) { it.copy(restSec = seconds.coerceIn(0, 900)) }

    private fun mapExercise(exerciseId: String, f: (DraftExercise) -> DraftExercise) =
        draftStore.update { d -> d.copy(exercises = d.exercises.map { if (it.id == exerciseId) f(it) else it }) }

    private fun mapSet(exerciseId: String, setId: String, f: (DraftSet) -> DraftSet) =
        mapExercise(exerciseId) { ex -> ex.copy(sets = ex.sets.map { if (it.id == setId) f(it) else it }) }

    fun addSet(exerciseId: String) = mapExercise(exerciseId) { ex ->
        val last = ex.sets.lastOrNull()
        ex.copy(sets = ex.sets + DraftSet(reps = last?.reps ?: 10, weightKg = last?.weightKg ?: 0f, rpe = last?.rpe))
    }

    fun removeSet(exerciseId: String, setId: String) =
        mapExercise(exerciseId) { ex -> ex.copy(sets = ex.sets.filterNot { it.id == setId }) }

    fun setReps(exerciseId: String, setId: String, reps: Int) =
        mapSet(exerciseId, setId) { it.copy(reps = WorkoutSessionLogic.clampReps(reps)) }

    /** `weightKg` luôn là kg; màn hình đổi từ lb trước khi gọi. */
    fun setWeight(exerciseId: String, setId: String, weightKg: Float) =
        mapSet(exerciseId, setId) { it.copy(weightKg = WorkoutSessionLogic.clampWeight(weightKg)) }

    fun setSetRpe(exerciseId: String, setId: String, rpe: Float?) =
        mapSet(exerciseId, setId) { it.copy(rpe = rpe?.let { v -> WorkoutSessionLogic.clampRpe(v) }) }

    fun toggleWarmup(exerciseId: String, setId: String) =
        mapSet(exerciseId, setId) { it.copy(isWarmup = !it.isWarmup) }

    /** Đánh dấu set đã làm (hoặc bỏ đánh dấu) và bật đồng hồ nghỉ theo cài đặt của bài. */
    fun toggleSetDone(exerciseId: String, setId: String) {
        var startRest: Int? = null
        draftStore.update { d ->
            d.copy(
                exercises = d.exercises.map { ex ->
                    if (ex.id != exerciseId) ex
                    else ex.copy(
                        sets = ex.sets.map { s ->
                            if (s.id != setId) s
                            else {
                                val done = !s.isCompleted
                                if (done && ex.restSec > 0) startRest = ex.restSec
                                s.copy(isCompleted = done)
                            }
                        }
                    )
                }
            )
        }
        startRest?.let { startRestTimer(it) }
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _ui.update { it.copy(isRestTimerRunning = true, totalRestSeconds = seconds, restSecondsRemaining = seconds) }
        restTimerJob = viewModelScope.launch {
            while (_ui.value.restSecondsRemaining > 0) {
                delay(1000L)
                _ui.update { it.copy(restSecondsRemaining = (it.restSecondsRemaining - 1).coerceAtLeast(0)) }
            }
            _ui.update { it.copy(isRestTimerRunning = false) }
        }
    }

    fun stopRestTimer() {
        restTimerJob?.cancel()
        _ui.update { it.copy(isRestTimerRunning = false, restSecondsRemaining = 0) }
    }

    // ── Lưu ──

    /**
     * Lưu phiên tập. Dùng `clientRequestId` của phiên nên bấm lưu nhiều lần hay mất mạng gửi lại đều chỉ ghi một buổi;
     * mất mạng thì buổi tập vào hàng đợi trên máy và tự gửi khi có mạng.
     */
    fun save() {
        val draft = draftStore.draft.value ?: return
        WorkoutSessionLogic.validate(draft)?.let { msg ->
            _ui.update { it.copy(errorMessage = msg) }
            return
        }
        if (_ui.value.isSubmitting) return
        _ui.update { it.copy(isSubmitting = true, errorMessage = null) }
        val now = System.currentTimeMillis()
        val request = WorkoutSessionLogic.toRequest(draft, now)
        viewModelScope.launch {
            repository.createWorkoutLog(request).fold(
                onSuccess = { saved ->
                    val queued = saved.id.startsWith("pending:")
                    stopRestTimer()
                    draftStore.clear()
                    _ui.update {
                        it.copy(
                            isSubmitting = false,
                            saveSummary = SaveSummary(
                                workoutName = request.name,
                                durationMinutes = request.durationMinutes,
                                volumeKg = saved.totalVolumeKg ?: WorkoutSessionLogic.volumeKg(draft),
                                calories = if (queued) request.caloriesBurned else saved.caloriesBurned,
                                queued = queued,
                                newRecords = saved.newRecords ?: emptyList(),
                                warnings = saved.warnings ?: emptyList(),
                                savedWorkoutId = if (queued) null else saved.id
                            )
                        )
                    }
                    if (!queued) loadData()
                },
                onFailure = { e ->
                    _ui.update { it.copy(isSubmitting = false, errorMessage = e.message ?: "Không thể lưu buổi tập, vui lòng thử lại") }
                }
            )
        }
    }

    fun dismissSummary() = _ui.update { it.copy(saveSummary = null) }

    fun saveSummaryAsRoutine(name: String?, onDone: (Boolean, String) -> Unit) {
        val id = _ui.value.saveSummary?.savedWorkoutId ?: return onDone(false, "Buổi tập chưa được lưu lên máy chủ")
        viewModelScope.launch {
            repository.createRoutineFromWorkout(id, name)
                .onSuccess {
                    loadData()
                    onDone(true, "Đã lưu thành buổi tập mẫu")
                }
                .onFailure { onDone(false, it.message ?: "Không thể lưu mẫu") }
        }
    }

    fun deleteRoutine(id: String) {
        viewModelScope.launch {
            repository.deleteWorkoutRoutine(id)
            _ui.update { it.copy(routines = it.routines.filterNot { r -> r.id == id }) }
        }
    }

    fun createCustomExercise(name: String, muscle: String, onDone: (CustomExerciseDto?) -> Unit) {
        viewModelScope.launch {
            repository.createCustomExercise(name, muscle)
                .onSuccess { ex ->
                    _ui.update { it.copy(customExercises = it.customExercises + ex) }
                    onDone(ex)
                }
                .onFailure { e ->
                    _ui.update { it.copy(errorMessage = e.message) }
                    onDone(null)
                }
        }
    }

    // ── Lịch sử ──

    fun selectWorkoutDetail(workout: WorkoutLogDto?) = _ui.update { it.copy(selectedWorkoutDetail = workout) }

    fun openWorkoutById(id: String) {
        viewModelScope.launch {
            repository.fetchWorkoutById(id).onSuccess { w -> _ui.update { it.copy(selectedWorkoutDetail = w) } }
        }
    }

    fun deleteWorkout(id: String) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(id)
            _ui.update { state ->
                state.copy(
                    history = state.history.filterNot { it.id == id },
                    selectedWorkoutDetail = if (state.selectedWorkoutDetail?.id == id) null else state.selectedWorkoutDetail
                )
            }
            loadData()
            loadProgress()
        }
    }

    fun clearError() = _ui.update { it.copy(errorMessage = null) }
}
