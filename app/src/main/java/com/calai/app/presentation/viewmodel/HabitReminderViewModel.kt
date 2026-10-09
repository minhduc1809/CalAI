package com.calai.app.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.AttachHabitReminderFoodRequest
import com.calai.app.data.remote.dto.CreateHabitReminderRequest
import com.calai.app.data.remote.dto.HabitReminderDto
import com.calai.app.data.remote.dto.SuggestedMealItemDto
import com.calai.app.data.remote.dto.UpdateHabitReminderRequest
import com.calai.app.domain.repository.CalAIRepository
import com.calai.app.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitReminderListUiState(
    val isLoading: Boolean = false,
    val reminders: List<HabitReminderDto> = emptyList(),
    val errorMessage: String? = null
)

data class HabitReminderEditUiState(
    val reminder: HabitReminderDto? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val suggestions: List<SuggestedMealItemDto> = emptyList(),
    val isLoadingSuggestions: Boolean = false
)

@HiltViewModel
class HabitReminderViewModel @Inject constructor(
    application: Application,
    private val repository: CalAIRepository
) : AndroidViewModel(application) {

    private val _listState = MutableStateFlow(HabitReminderListUiState())
    val listState: StateFlow<HabitReminderListUiState> = _listState.asStateFlow()

    private val _editState = MutableStateFlow(HabitReminderEditUiState())
    val editState: StateFlow<HabitReminderEditUiState> = _editState.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, errorMessage = null)
            repository.getHabitReminders()
                .onSuccess { reminders ->
                    _listState.value = HabitReminderListUiState(reminders = reminders)
                    ReminderScheduler.syncFromServer(getApplication(), reminders)
                }
                .onFailure { e ->
                    _listState.value = _listState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không tải được danh sách nhắc nhở"
                    )
                }
        }
    }

    fun toggleEnabled(reminder: HabitReminderDto, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHabitReminder(reminder.id, UpdateHabitReminderRequest(enabled = enabled))
                .onSuccess { updated -> replaceInList(updated) }
        }
    }

    fun openEdit(reminder: HabitReminderDto) {
        _editState.value = HabitReminderEditUiState(reminder = reminder)
        loadSuggestions(reminder.id)
    }

    fun closeEdit() {
        _editState.value = HabitReminderEditUiState()
    }

    fun saveEdit(
        timeOfDay: String,
        windowStart: String?,
        windowEnd: String?,
        targetCalorieMin: Float?,
        targetCalorieMax: Float?,
        repeatDays: List<Int>,
        advanceNoticeMinutes: Int,
        onDone: () -> Unit
    ) {
        val current = _editState.value.reminder ?: return
        viewModelScope.launch {
            _editState.value = _editState.value.copy(isSaving = true, errorMessage = null)
            repository.updateHabitReminder(
                current.id,
                UpdateHabitReminderRequest(
                    timeOfDay = timeOfDay,
                    windowStart = windowStart,
                    windowEnd = windowEnd,
                    targetCalorieMin = targetCalorieMin,
                    targetCalorieMax = targetCalorieMax,
                    repeatDays = repeatDays,
                    advanceNoticeMinutes = advanceNoticeMinutes
                )
            ).onSuccess { updated ->
                replaceInList(updated)
                _editState.value = _editState.value.copy(reminder = updated, isSaving = false)
                onDone()
            }.onFailure { e ->
                _editState.value = _editState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Lưu thay đổi thất bại"
                )
            }
        }
    }

    fun addFood(name: String, servingSize: String?, calories: Float, protein: Float, carb: Float, fat: Float) {
        val current = _editState.value.reminder ?: return
        viewModelScope.launch {
            repository.addHabitReminderFood(
                current.id,
                AttachHabitReminderFoodRequest(name, servingSize, calories, protein, carb, fat)
            ).onSuccess { updated ->
                replaceInList(updated)
                _editState.value = _editState.value.copy(reminder = updated)
            }
        }
    }

    fun removeFood(foodId: String) {
        val current = _editState.value.reminder ?: return
        viewModelScope.launch {
            repository.removeHabitReminderFood(current.id, foodId).onSuccess { updated ->
                replaceInList(updated)
                _editState.value = _editState.value.copy(reminder = updated)
            }
        }
    }

    private fun loadSuggestions(reminderId: String) {
        viewModelScope.launch {
            _editState.value = _editState.value.copy(isLoadingSuggestions = true)
            repository.getHabitReminderSuggestions(reminderId)
                .onSuccess { response ->
                    _editState.value = _editState.value.copy(
                        isLoadingSuggestions = false,
                        suggestions = response.suggestions
                    )
                }
                .onFailure {
                    _editState.value = _editState.value.copy(isLoadingSuggestions = false)
                }
        }
    }

    fun createCustomReminder(label: String, timeOfDay: String, onDone: (HabitReminderDto) -> Unit) {
        viewModelScope.launch {
            repository.createHabitReminder(CreateHabitReminderRequest(label = label, timeOfDay = timeOfDay))
                .onSuccess { created ->
                    _listState.value = _listState.value.copy(reminders = _listState.value.reminders + created)
                    ReminderScheduler.scheduleFromServer(getApplication(), created)
                    onDone(created)
                }
        }
    }

    fun deleteReminder(reminder: HabitReminderDto) {
        viewModelScope.launch {
            repository.deleteHabitReminder(reminder.id).onSuccess {
                ReminderScheduler.cancelHabitReminder(getApplication(), reminder.id)
                _listState.value = _listState.value.copy(
                    reminders = _listState.value.reminders.filterNot { it.id == reminder.id }
                )
            }
        }
    }

    private fun replaceInList(updated: HabitReminderDto) {
        _listState.value = _listState.value.copy(
            reminders = _listState.value.reminders.map { if (it.id == updated.id) updated else it }
        )
        ReminderScheduler.scheduleFromServer(getApplication(), updated)
    }
}
