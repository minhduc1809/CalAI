package com.calai.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calai_auth_prefs", Context.MODE_PRIVATE)

    /**
     * Đếm số lần session bị coi là hết hạn cưỡng bức (token invalid, refresh thất bại...).
     * MainActivity lắng nghe flow này để tự điều hướng về màn Login — không dùng để tính
     * toán gì khác ngoài việc kích hoạt navigation, giá trị tăng dần chỉ để LaunchedEffect
     * phát hiện thay đổi kể cả khi nhiều lần liên tiếp.
     */
    private val _sessionExpiredEvent = MutableStateFlow(0)
    val sessionExpiredEvent: StateFlow<Int> = _sessionExpiredEvent.asStateFlow()

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_NAME = "user_name"
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    /**
     * Lưu token đồng bộ (blocking) — dùng cho TokenAuthenticator
     * để đảm bảo token mới được persist trước khi retry request.
     */
    fun saveTokensSync(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .commit()
    }

    fun saveUser(userId: String, username: String, name: String? = null) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun isLoggedIn(): Boolean {
        val token = getAccessToken()
        if (token == "mock_access_token") {
            clear()
            return false
        }
        return !token.isNullOrBlank()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    /**
     * Gọi khi phát hiện session không còn hợp lệ (401 mà refresh token thất bại/không có/
     * đã hết lượt retry) — xóa token cục bộ VÀ báo cho UI (MainActivity) biết để tự điều
     * hướng về Login, thay vì chỉ âm thầm clear() khiến app vẫn "tưởng" đang đăng nhập.
     */
    fun notifySessionExpired() {
        clear()
        _sessionExpiredEvent.value = _sessionExpiredEvent.value + 1
    }
}
