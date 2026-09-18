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

enum class ForgotPasswordStep {
    ENTER_EMAIL,
    ENTER_CODE
}

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.ENTER_EMAIL,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isResetDone: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(code = value, errorMessage = null)
    }

    fun onNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    /** Bước 1: gửi mã OTP đặt lại mật khẩu tới email đã nhập. */
    fun sendResetCode() {
        val state = _uiState.value
        val email = state.email.trim()
        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Vui lòng nhập email")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = state.copy(errorMessage = "Địa chỉ email không đúng định dạng")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.forgotPassword(email)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = ForgotPasswordStep.ENTER_CODE,
                    infoMessage = "Nếu email tồn tại, mã đặt lại mật khẩu đã được gửi tới $email"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Gửi yêu cầu thất bại, vui lòng thử lại"
                )
            }
        }
    }

    /** Cho phép gửi lại mã mà không mất email đã nhập. */
    fun resendCode() = sendResetCode()

    fun backToEmailStep() {
        _uiState.value = _uiState.value.copy(
            step = ForgotPasswordStep.ENTER_EMAIL,
            code = "",
            newPassword = "",
            confirmPassword = "",
            errorMessage = null,
            infoMessage = null
        )
    }

    /** Bước 2: xác nhận mã OTP + mật khẩu mới. */
    fun resetPassword() {
        val state = _uiState.value
        if (state.code.trim().length != 6) {
            _uiState.value = state.copy(errorMessage = "Mã xác thực phải gồm đúng 6 chữ số")
            return
        }
        if (state.newPassword.length < 8) {
            _uiState.value = state.copy(errorMessage = "Mật khẩu mới phải có ít nhất 8 ký tự")
            return
        }
        val hasLower = state.newPassword.any { it.isLowerCase() }
        val hasUpper = state.newPassword.any { it.isUpperCase() }
        val hasDigit = state.newPassword.any { it.isDigit() }
        if (!hasLower || !hasUpper || !hasDigit) {
            _uiState.value = state.copy(errorMessage = "Mật khẩu mới phải chứa ít nhất 1 chữ thường, 1 chữ HOA và 1 chữ số")
            return
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Xác nhận mật khẩu không khớp")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.resetPassword(
                email = state.email.trim(),
                code = state.code.trim(),
                newPassword = state.newPassword
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isResetDone = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Đặt lại mật khẩu thất bại, vui lòng thử lại"
                )
            }
        }
    }
}
