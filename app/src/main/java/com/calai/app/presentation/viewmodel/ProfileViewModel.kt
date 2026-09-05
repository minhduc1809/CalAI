package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.UpdateProfileRequest
import com.calai.app.data.remote.dto.UserProfileDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfileDto? = null,
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
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

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
            onSuccess()
        }
    }
}
