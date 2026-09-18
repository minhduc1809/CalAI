package com.calai.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_HABIT_ID = "habit_reminder_id"
        const val KEY_HABIT_CHANNEL = "habit_reminder_channel"
        const val KEY_HABIT_TITLE = "habit_reminder_title"
        const val KEY_HABIT_MESSAGE = "habit_reminder_message"
        /** Danh sách thứ ISO (1=Thứ 2..7=Chủ nhật) được phép bắn, phân cách bởi dấu phẩy. Rỗng/không có = mọi ngày. */
        const val KEY_REPEAT_DAYS = "habit_reminder_repeat_days"
    }

    override suspend fun doWork(): Result {
        ReminderNotificationHelper.ensureChannels(applicationContext)

        val habitId = inputData.getString(KEY_HABIT_ID) ?: return Result.failure()
        val repeatDaysCsv = inputData.getString(KEY_REPEAT_DAYS)
        if (!isTodayAllowed(repeatDaysCsv)) {
            return Result.success()
        }

        val channelId = inputData.getString(KEY_HABIT_CHANNEL) ?: ReminderNotificationHelper.CUSTOM_REMINDER_CHANNEL_ID
        val title = inputData.getString(KEY_HABIT_TITLE) ?: "Nhắc nhở"
        val message = inputData.getString(KEY_HABIT_MESSAGE) ?: "Đến giờ nhắc nhở bạn đã đặt."
        ReminderNotificationHelper.showHabitReminder(applicationContext, habitId, channelId, title, message)
        return Result.success()
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
