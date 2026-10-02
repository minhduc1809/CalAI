package com.calai.app.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.calai.app.domain.WaterProgressRule
import com.calai.app.domain.repository.CalAIRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CalAIRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_HABIT_ID = "habit_reminder_id"
        const val KEY_HABIT_CHANNEL = "habit_reminder_channel"
        const val KEY_HABIT_TITLE = "habit_reminder_title"
        const val KEY_HABIT_MESSAGE = "habit_reminder_message"
        /** Danh sách thứ ISO (1=Thứ 2..7=Chủ nhật) được phép bắn, phân cách bởi dấu phẩy. Rỗng/không có = mọi ngày. */
        const val KEY_REPEAT_DAYS = "habit_reminder_repeat_days"
        /** true nếu đây là nhắc uống nước — cần kiểm tra tiến độ thật trước khi bắn (không nhắc nếu đã đủ). */
        const val KEY_IS_WATER = "habit_reminder_is_water"
    }

    override suspend fun doWork(): Result {
        ReminderNotificationHelper.ensureChannels(applicationContext)

        val habitId = inputData.getString(KEY_HABIT_ID) ?: return Result.failure()
        val repeatDaysCsv = inputData.getString(KEY_REPEAT_DAYS)
        if (!isTodayAllowed(repeatDaysCsv)) {
            return Result.success()
        }

        if (inputData.getBoolean(KEY_IS_WATER, false) && !isBehindWaterSchedule()) {
            return Result.success()
        }

        val channelId = inputData.getString(KEY_HABIT_CHANNEL) ?: ReminderNotificationHelper.CUSTOM_REMINDER_CHANNEL_ID
        val title = inputData.getString(KEY_HABIT_TITLE) ?: "Nhắc nhở"
        val message = inputData.getString(KEY_HABIT_MESSAGE) ?: "Đến giờ nhắc nhở bạn đã đặt."
        ReminderNotificationHelper.showHabitReminder(applicationContext, habitId, channelId, title, message)
        return Result.success()
    }

    /** Chỉ nhắc uống nước khi thực sự thiếu so với tiến độ ngày — tránh spam khi đã uống đủ. */
    private suspend fun isBehindWaterSchedule(): Boolean {
        val today = repository.getWaterToday().getOrNull() ?: return true // lỗi mạng: vẫn nhắc, an toàn hơn im lặng
        return WaterProgressRule.isBehindSchedule(today.totalMl, today.goalMl, today.glassMl, Calendar.getInstance())
    }

    private fun isTodayAllowed(repeatDaysCsv: String?): Boolean {
        if (repeatDaysCsv.isNullOrBlank()) return true
        val allowedDays = repeatDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (allowedDays.isEmpty()) return true

        // Calendar.DAY_OF_WEEK: Chủ nhật=1..Thứ 7=7 → quy đổi sang ISO: Thứ 2=1..Chủ nhật=7
        val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val isoDay = if (calendarDay == Calendar.SUNDAY) 7 else calendarDay - 1
        return allowedDays.contains(isoDay)
    }
}
