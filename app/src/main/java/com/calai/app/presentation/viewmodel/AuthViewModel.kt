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
    val confirmPassword: String = "",
    val name: String = "",
    /** Đã tích đồng ý Chính sách quyền riêng tư (chỉ cần khi đăng ký). */
    val acceptPrivacy: Boolean = false,
    /** Có idToken Google đang chờ người dùng đồng ý chính sách trước khi tạo tài khoản mới. */
    val pendingGoogleConsent: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    /** true nếu cần hiện Onboarding (profile chưa hoàn tất hoặc vừa đăng ký mới). */
    val needsOnboarding: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Nếu đã đăng nhập (token còn lưu), kiểm tra profile đã đầy đủ chưa
        if (repository.isLoggedIn()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val needsOnboarding = checkProfileIncomplete()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    needsOnboarding = needsOnboarding
                )
            }
        }
    }

    /**
     * Gọi GET /users/me, kiểm tra các trường bắt buộc (heightCm, weightKg, goal, dateOfBirth).
     * Nếu thiếu bất kỳ trường nào → cần Onboarding lại.
     * Nếu API lỗi (mất mạng, token hết hạn) → cho vào Home bình thường (fail-open).
     */
    private suspend fun checkProfileIncomplete(): Boolean {
        return try {
            val result = repository.fetchRemoteProfile()
            result.getOrNull()?.let { profile ->
                profile.heightCm == null ||
                profile.weightKg == null ||
                profile.goal == null ||
                profile.dateOfBirth.isNullOrBlank()
            } ?: false
        } catch (_: Exception) {
            false // Fail-open: không chặn user vào app nếu API lỗi
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

    fun onAcceptPrivacyChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(acceptPrivacy = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
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

    /**
     * Đăng nhập/Đăng ký bằng Google Sign-In. idToken lấy từ Credential Manager phía UI.
     */
    private var pendingGoogleToken: String? = null

    fun loginWithGoogle(idToken: String, acceptPrivacy: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, pendingGoogleConsent = false)
            val result = repository.loginWithGoogle(idToken, acceptPrivacy)
            result.onSuccess {
                pendingGoogleToken = null
                val needsOnboarding = checkProfileIncomplete()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    needsOnboarding = needsOnboarding
                )
            }.onFailure { e ->
                if ((e as? com.calai.app.data.remote.ApiException)?.code == "PRIVACY_CONSENT_REQUIRED") {
                    // Tài khoản Google mới: hỏi đồng ý chính sách rồi gửi lại cùng idToken
                    pendingGoogleToken = idToken
                    _uiState.value = _uiState.value.copy(isLoading = false, pendingGoogleConsent = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Đăng nhập Google thất bại"
                    )
                }
            }
        }
    }

    fun confirmGoogleConsent() {
        pendingGoogleToken?.let { loginWithGoogle(it, acceptPrivacy = true) }
    }

    fun cancelGoogleConsent() {
        pendingGoogleToken = null
        _uiState.value = _uiState.value.copy(pendingGoogleConsent = false)
    }

    fun onGoogleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
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
            if (state.email.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Vui lòng nhập email")
                return
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
                _uiState.value = state.copy(errorMessage = "Địa chỉ email không đúng định dạng")
                return
            }
            if (state.password != state.confirmPassword) {
                _uiState.value = state.copy(errorMessage = "Xác nhận mật khẩu không khớp")
                return
            }
            if (!state.acceptPrivacy) {
                _uiState.value = state.copy(errorMessage = "Bạn cần đồng ý Chính sách quyền riêng tư để tạo tài khoản")
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
                    name = state.name.trim().ifBlank { null },
                    acceptPrivacy = state.acceptPrivacy
                )
            }

            result.onSuccess {
                if (state.isLoginMode) {
                    // Đăng nhập → kiểm tra profile có đầy đủ chưa
                    val needsOnboarding = checkProfileIncomplete()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        needsOnboarding = needsOnboarding
                    )
                } else {
                    // Đăng ký mới → luôn cần Onboarding
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        needsOnboarding = true
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Có lỗi xảy ra, vui lòng thử lại"
                )
            }
        }
    }
}
