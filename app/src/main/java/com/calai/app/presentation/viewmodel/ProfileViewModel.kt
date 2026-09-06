package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.UserProfileDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderSettingsState(
    val breakfastEnabled: Boolean = true,
    val breakfastTime: String = UserPreferencesManager.DEFAULT_BREAKFAST_TIME,
    val lunchEnabled: Boolean = true,
    val lunchTime: String = UserPreferencesManager.DEFAULT_LUNCH_TIME,
    val dinnerEnabled: Boolean = true,
    val dinnerTime: String = UserPreferencesManager.DEFAULT_DINNER_TIME,
    val snackEnabled: Boolean = false,
    val snackTime: String = UserPreferencesManager.DEFAULT_SNACK_TIME,
    val waterEnabled: Boolean = true,
    val waterInterval: Int = UserPreferencesManager.DEFAULT_WATER_INTERVAL_HOURS
)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isChangingPassword: Boolean = false,
    val profile: UserProfileDto? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedOut: Boolean = false,
    val weightUnit: String = "kg",
    val reminderSettings: ReminderSettingsState = ReminderSettingsState()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: CalAIRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(weightUnit = preferencesManager.getWeightUnit()))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadReminderSettings()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesManager.weightUnit.collect { unit ->
                _uiState.update { it.copy(weightUnit = unit) }
            }
        }
    }

    fun setWeightUnit(unit: String) {
        preferencesManager.setWeightUnit(unit)
    }


    fun loadReminderSettings() {
        _uiState.update {
            it.copy(
                reminderSettings = ReminderSettingsState(
                    breakfastEnabled = preferencesManager.isBreakfastReminderEnabled(),
                    breakfastTime = preferencesManager.getBreakfastReminderTime(),
                    lunchEnabled = preferencesManager.isLunchReminderEnabled(),
                    lunchTime = preferencesManager.getLunchReminderTime(),
                    dinnerEnabled = preferencesManager.isDinnerReminderEnabled(),
                    dinnerTime = preferencesManager.getDinnerReminderTime(),
                    snackEnabled = preferencesManager.isSnackReminderEnabled(),
                    snackTime = preferencesManager.getSnackReminderTime(),
                    waterEnabled = preferencesManager.isWaterReminderEnabled(),
                    waterInterval = preferencesManager.getWaterReminderInterval()
                )
            )
        }
    }

    fun updateBreakfastReminder(enabled: Boolean, time: String = UserPreferencesManager.DEFAULT_BREAKFAST_TIME) {
        preferencesManager.setBreakfastReminder(enabled, time)
        loadReminderSettings()
    }

    fun updateLunchReminder(enabled: Boolean, time: String = UserPreferencesManager.DEFAULT_LUNCH_TIME) {
        preferencesManager.setLunchReminder(enabled, time)
        loadReminderSettings()
    }

    fun updateDinnerReminder(enabled: Boolean, time: String = UserPreferencesManager.DEFAULT_DINNER_TIME) {
        preferencesManager.setDinnerReminder(enabled, time)
        loadReminderSettings()
    }

    fun updateSnackReminder(enabled: Boolean, time: String = UserPreferencesManager.DEFAULT_SNACK_TIME) {
        preferencesManager.setSnackReminder(enabled, time)
        loadReminderSettings()
    }

    fun updateWaterReminder(enabled: Boolean, interval: Int = UserPreferencesManager.DEFAULT_WATER_INTERVAL_HOURS) {
        preferencesManager.setWaterReminder(enabled, interval)
        loadReminderSettings()
    }

    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.fetchRemoteProfile()
            result.onSuccess { profile ->
                _uiState.update { it.copy(isLoading = false, profile = profile) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message,
                        // Cung cấp profile mặc định nếu offline / lỗi server
                        profile = it.profile ?: UserProfileDto(
                            id = "default_user",
                            username = repository.getCurrentUsername() ?: "NutriWise User",
                            name = "Nguyễn Minh Đức",
                            heightCm = 175f,
                            weightKg = 68.5f,
                            goal = "LOSE_WEIGHT",
                            activityLevel = "MODERATE",
                            bmi = 22.4f,
                            bmr = 1680f,
                            tdee = 2310f,
                            targetCalories = 1810f,
                            targetProtein = 135f,
                            targetCarb = 200f,
                            targetFat = 50f,
                            dailyAiQuota = 50
                        )
                    )
                }
            }
        }
    }

    fun changePassword(
        oldPass: String,
        newPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (oldPass.isBlank()) {
            onError("Vui lòng nhập mật khẩu hiện tại")
            return
        }
        if (newPass.length < 8) {
            onError("Mật khẩu mới phải có tối thiểu 8 ký tự")
            return
        }
        val hasUpper = newPass.any { it.isUpperCase() }
        val hasLower = newPass.any { it.isLowerCase() }
        val hasDigit = newPass.any { it.isDigit() }
        if (!hasUpper || !hasLower || !hasDigit) {
            onError("Mật khẩu mới cần chứa cả chữ hoa, chữ thường và chữ số")
            return
        }

        _uiState.update { it.copy(isChangingPassword = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.changePassword(oldPass, newPass)
            _uiState.update { it.copy(isChangingPassword = false) }
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu cũ.")
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
            onSuccess()
        }
    }
}
