package com.calai.app.data.sync

import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.CalAIApi
import com.calai.app.data.remote.dto.UpdateSettingsRequest
import com.calai.app.data.remote.dto.UserSettingsDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cài đặt cá nhân đồng bộ qua server (BR-19.4). Server là nguồn chính, SharedPreferences chỉ là bộ nhớ đệm.
 * Lần đầu sau cập nhật (server chưa `initialized`): đẩy cài đặt đang có trên máy lên một lần rồi đánh dấu.
 * Giao diện (theme, kiểu hiển thị bữa ăn) chỉ lưu trên máy nên không đồng bộ.
 */
@Singleton
class SettingsSync @Inject constructor(
    private val api: CalAIApi,
    private val prefs: UserPreferencesManager
) {
    /** Loại thông báo đang bị tắt (true = bật). Chỉ chứa loại đã tắt, như server. */
    private val _disabledNotifications = MutableStateFlow<Set<String>>(emptySet())
    val disabledNotifications: StateFlow<Set<String>> = _disabledNotifications.asStateFlow()

    /** Lấy cài đặt từ server rồi áp lên máy. Trả null nếu không gọi được (giữ cài đặt cũ trên máy). */
    suspend fun sync(): UserSettingsDto? {
        val remote = try {
            val r = api.getSettings()
            if (r.success) r.data else null
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            null
        } ?: return null

        if (!remote.initialized) {
            // Di trú một lần: giá trị đang dùng trên máy trở thành giá trị trên server
            val push = UpdateSettingsRequest(
                weightUnit = prefs.getWeightUnit(),
                heightUnit = prefs.getHeightUnit(),
                energyUnit = prefs.getEnergyUnit(),
                // "OFF" = tắt nhắc (JSON không gửi null)
                dailyCompletionReminder = if (prefs.isDailyCompletionEnabled()) prefs.getDailyCompletionTime() else "OFF",
                defaultRestSec = prefs.getDefaultRestSec(),
                initialized = true
            )
            return pushRequest(push) ?: remote
        }
        apply(remote)
        return remote
    }

    /** Đẩy một thay đổi lên server; lỗi mạng thì bỏ qua (lần mở app sau server vẫn là nguồn chính). */
    suspend fun push(request: UpdateSettingsRequest): UserSettingsDto? = pushRequest(request)

    private suspend fun pushRequest(request: UpdateSettingsRequest): UserSettingsDto? {
        return try {
            val r = api.updateSettings(request)
            val data = if (r.success) r.data else null
            data?.let(::apply)
            data
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            null
        }
    }

    /** Áp giá trị server lên SharedPreferences. */
    private fun apply(s: UserSettingsDto) {
        if (s.weightUnit.isNotBlank() && prefs.getWeightUnit() != s.weightUnit) prefs.setWeightUnit(s.weightUnit)
        if (s.heightUnit.isNotBlank() && prefs.getHeightUnit() != s.heightUnit) prefs.setHeightUnit(s.heightUnit)
        if (s.energyUnit.isNotBlank() && prefs.getEnergyUnit() != s.energyUnit) prefs.setEnergyUnit(s.energyUnit)
        if (s.defaultRestSec in 15..600) prefs.setDefaultRestSec(s.defaultRestSec)
        val reminder = s.dailyCompletionReminder
        if (reminder == null) {
            prefs.setDailyCompletionEnabled(false)
        } else {
            prefs.setDailyCompletionEnabled(true)
            prefs.setDailyCompletionTime(reminder)
        }
        _disabledNotifications.value = s.notificationPrefs.filterValues { !it }.keys
    }

    companion object {
        /** Loại thông báo người dùng được tắt/bật, kèm tên hiển thị (BR-19.4). */
        val TOGGLEABLE_NOTIFICATIONS: List<Pair<String, String>> = listOf(
            "CHECKIN_READY" to "Check-in tuần",
            "DAY_COMPLETED" to "Ngày ghi đủ",
            "WEEKLY_SUMMARY_READY" to "Tổng kết tuần",
            "GOAL_REACHED" to "Đạt cân nặng mục tiêu",
            "FOOD_SUBMISSION_RESULT" to "Kết quả duyệt món gửi"
        )
    }
}
