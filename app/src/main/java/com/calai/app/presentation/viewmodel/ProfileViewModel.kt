package com.calai.app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.UserProfileDto
import com.calai.app.data.remote.dto.UpdateProfileRequest
import com.calai.app.domain.repository.CalAIRepository
import com.calai.app.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val isUpdatingBiometrics: Boolean = false,
    val isSendingVerificationEmail: Boolean = false,
    val isVerifyingEmail: Boolean = false,
    val profile: UserProfileDto? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedOut: Boolean = false,
    val weightUnit: String = "kg",
    val mealStructureMode: String = "TIMELINE",
    val reminderSettings: ReminderSettingsState = ReminderSettingsState()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: CalAIRepository,
    private val preferencesManager: UserPreferencesManager,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            weightUnit = preferencesManager.getWeightUnit(),
            mealStructureMode = preferencesManager.getMealStructureMode()
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadReminderSettings()
        observePreferences()
        ReminderScheduler.scheduleAll(appContext, preferencesManager)
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesManager.weightUnit.collect { unit ->
                _uiState.update { it.copy(weightUnit = unit) }
            }
        }
        viewModelScope.launch {
            preferencesManager.mealStructureMode.collect { mode ->
                _uiState.update { it.copy(mealStructureMode = mode) }
            }
        }
    }

    fun setWeightUnit(unit: String) {
        preferencesManager.setWeightUnit(unit)
    }

    fun setMealStructureMode(mode: String) {
        preferencesManager.setMealStructureMode(mode)
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
        ReminderScheduler.scheduleAll(appContext, preferencesManager)
    }

    fun updateLunchReminder(enabled: Boolean, time: String = UserPreferencesManager.DEFAULT_LUNCH_TIME) {
        preferencesManager.setLunchReminder(enabled, time)
        loadReminderSettings()
        ReminderScheduler.scheduleAll(appContext, preferencesManager)
    }

    fun updateDinnerReminder(enabled: Boolean, time: String = UserPreferencesManager.DEFAULT_DINNER_TIME) {
        preferencesManager.setDinnerReminder(enabled, time)
        loadReminderSettings()
        ReminderScheduler.scheduleAll(appContext, preferencesManager)
    }

    fun updateSnackReminder(enabled: Boolean, time: String = UserPreferencesManager.DEFAULT_SNACK_TIME) {
        preferencesManager.setSnackReminder(enabled, time)
        loadReminderSettings()
        ReminderScheduler.scheduleAll(appContext, preferencesManager)
    }

    fun updateWaterReminder(enabled: Boolean, interval: Int = UserPreferencesManager.DEFAULT_WATER_INTERVAL_HOURS) {
        preferencesManager.setWaterReminder(enabled, interval)
        loadReminderSettings()
        ReminderScheduler.scheduleAll(appContext, preferencesManager)
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
                        errorMessage = err.message ?: "Không thể kết nối đến máy chủ"
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

    fun updateHeight(
        heightCm: Float,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.update { it.copy(isUpdatingBiometrics = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.updateProfile(UpdateProfileRequest(heightCm = heightCm))
            _uiState.update { it.copy(isUpdatingBiometrics = false) }
            result.onSuccess { updated ->
                _uiState.update { it.copy(profile = updated) }
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Cập nhật chiều cao thất bại")
            }
        }
    }

    fun updateWeight(
        weightKg: Float,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.update { it.copy(isUpdatingBiometrics = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.updateProfile(UpdateProfileRequest(weightKg = weightKg))
            _uiState.update { it.copy(isUpdatingBiometrics = false) }
            result.onSuccess { updated ->
                _uiState.update { it.copy(profile = updated) }
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Cập nhật cân nặng thất bại")
            }
        }
    }

    fun sendVerificationEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.update { it.copy(isSendingVerificationEmail = true) }
        viewModelScope.launch {
            val result = repository.sendVerificationEmail()
            _uiState.update { it.copy(isSendingVerificationEmail = false) }
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Gửi mã xác thực thất bại")
            }
        }
    }

    fun verifyEmail(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.update { it.copy(isVerifyingEmail = true) }
        viewModelScope.launch {
            val result = repository.verifyEmail(code)
            _uiState.update { it.copy(isVerifyingEmail = false) }
            result.onSuccess {
                loadProfile()
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Mã xác thực không chính xác")
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
