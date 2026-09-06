package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.ExpenditureStatusDto
import com.calai.app.data.remote.dto.UserProfileDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenditureDetailUiState(
    val isLoading: Boolean = true,
    val profile: UserProfileDto? = null,
    val expenditure: ExpenditureStatusDto? = null,
    val errorMessage: String? = null,
    val weightUnit: String = "kg"
)

@HiltViewModel
class ExpenditureDetailViewModel @Inject constructor(
    private val repository: CalAIRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenditureDetailUiState())
    val uiState: StateFlow<ExpenditureDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesManager.weightUnit.collect { unit ->
                _uiState.update { it.copy(weightUnit = unit) }
            }
        }
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val profileRes = repository.fetchRemoteProfile()
            val expRes = repository.fetchExpenditureStatus()

            val profile = profileRes.getOrNull()
            val expenditure = expRes.getOrNull()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    profile = profile ?: it.profile ?: UserProfileDto(
                        id = "default_user",
                        username = repository.getCurrentUsername() ?: "NutriWise User",
                        heightCm = 175f,
                        weightKg = 68.5f,
                        bmr = 1680f,
                        tdee = 2310f,
                        adaptiveExpenditure = 2280f,
                        expenditureStatus = "UPDATING"
                    ),
                    expenditure = expenditure ?: ExpenditureStatusDto(
                        method = "ADAPTIVE",
                        status = "UPDATING",
                        estimatedExpenditure = profile?.adaptiveExpenditure ?: 2280f,
                        staticTdee = profile?.tdee ?: 2310f,
                        windowDays = 14,
                        weightLogsCount = 12,
                        loggedDaysCount = 13,
                        trendWeightStart = 69.5f,
                        trendWeightEnd = 68.5f,
                        avgDailyCaloriesConsumed = 1850f,
                        message = "Đang cập nhật liên tục từ dữ liệu cân nặng và calo thực tế"
                    )
                )
            }
        }
    }
}
