package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Kiểm tra xem đã đăng nhập chưa
        if (repository.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(isSuccess = true)
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(
            isLoginMode = !_uiState.value.isLoginMode,
            errorMessage = null
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu")
            return
        }

        if (!state.isLoginMode) {
            val usernameTrimmed = state.username.trim()
            if (usernameTrimmed.length < 3 || usernameTrimmed.length > 30) {
                _uiState.value = state.copy(errorMessage = "Tên đăng nhập phải từ 3 đến 30 ký tự")
                return
            }
            if (!usernameTrimmed.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                _uiState.value = state.copy(errorMessage = "Tên đăng nhập chỉ bao gồm chữ cái, số và dấu gạch dưới")
                return
            }
            if (state.password.length < 8) {
                _uiState.value = state.copy(errorMessage = "Mật khẩu phải có ít nhất 8 ký tự")
                return
            }
            val hasLower = state.password.any { it.isLowerCase() }
            val hasUpper = state.password.any { it.isUpperCase() }
            val hasDigit = state.password.any { it.isDigit() }
            if (!hasLower || !hasUpper || !hasDigit) {
                _uiState.value = state.copy(errorMessage = "Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ HOA và 1 chữ số (VD: Admin@123)")
                return
            }
            if (state.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
                _uiState.value = state.copy(errorMessage = "Địa chỉ email không đúng định dạng")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = if (state.isLoginMode) {
                repository.login(state.username.trim(), state.password)
            } else {
                repository.register(
                    username = state.username.trim(),
                    email = state.email.trim().ifBlank { null },
                    password = state.password,
                    name = state.name.trim().ifBlank { null }
                )
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Có lỗi xảy ra, vui lòng thử lại"
                )
            }
        }
    }
}
