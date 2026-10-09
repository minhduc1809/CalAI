package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.UserProfileDto
import com.calai.app.data.remote.dto.UpdateProfileRequest
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
    val isChangingPassword: Boolean = false,
    val isUpdatingBiometrics: Boolean = false,
    val isSendingVerificationEmail: Boolean = false,
    val isVerifyingEmail: Boolean = false,
    val profile: UserProfileDto? = null,
    /** Mục tiêu đề xuất sau khi đổi chiều cao/cân nặng; hiện hộp thoại "Áp dụng?" khi khác null (BR-04). */
    val proposedTarget: com.calai.app.data.remote.dto.ProposedTargetDto? = null,
    val isApplyingTarget: Boolean = false,
    val isExporting: Boolean = false,
    /** Kết quả xuất dữ liệu (thành công hoặc lỗi của máy chủ) để màn Cài đặt hiện một lần rồi xoá. */
    val exportMessage: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedOut: Boolean = false,
    val weightUnit: String = "kg",
    val mealStructureMode: String = "TIMELINE"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: CalAIRepository,
    private val preferencesManager: UserPreferencesManager
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
        observePreferences()
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
                _uiState.update { it.copy(profile = updated, proposedTarget = updated.proposedTarget) }
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
                _uiState.update { it.copy(profile = updated, proposedTarget = updated.proposedTarget) }
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Cập nhật cân nặng thất bại")
            }
        }
    }

    /** Người dùng bấm "Áp dụng" trên hộp thoại mục tiêu đề xuất (BR-04, E5). */
    fun applyProposedTarget(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.update { it.copy(isApplyingTarget = true) }
        viewModelScope.launch {
            repository.applyProposedTarget().onSuccess {
                _uiState.update { it.copy(isApplyingTarget = false, proposedTarget = null) }
                loadProfile()
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(isApplyingTarget = false) }
                onError(err.message ?: "Không thể áp dụng mục tiêu mới")
            }
        }
    }

    /** Người dùng chọn "Để sau": giữ nguyên mục tiêu hiện tại. */
    /**
     * Xuất dữ liệu cá nhân (BR-18) vào tệp người dùng đã chọn. Người dùng chọn nơi lưu TRƯỚC, nên lượt xuất trong ngày
     * chỉ bị dùng khi họ thật sự muốn lưu. Nếu xuất lỗi thì xoá tệp rỗng vừa tạo để không để lại file hỏng.
     */
    fun exportData(resolver: android.content.ContentResolver, uri: android.net.Uri) {
        if (_uiState.value.isExporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null) }
            val result = runCatching {
                val stream = resolver.openOutputStream(uri) ?: error("Không mở được nơi lưu tệp")
                stream.use { repository.exportData(it).getOrThrow() }
            }
            result.onSuccess { bytes ->
                val kb = (bytes / 1024).coerceAtLeast(1)
                _uiState.update { it.copy(isExporting = false, exportMessage = "Đã lưu dữ liệu của bạn ($kb KB)") }
            }.onFailure { e ->
                runCatching { android.provider.DocumentsContract.deleteDocument(resolver, uri) }
                _uiState.update {
                    it.copy(isExporting = false, exportMessage = e.message ?: "Không xuất được dữ liệu, vui lòng thử lại")
                }
            }
        }
    }

    fun consumeExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }

    fun dismissProposedTarget() {
        _uiState.update { it.copy(proposedTarget = null) }
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
