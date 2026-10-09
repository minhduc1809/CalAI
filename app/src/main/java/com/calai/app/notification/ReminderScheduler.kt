package com.calai.app.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.calai.app.data.remote.dto.HabitReminderDto
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Biến danh sách HabitReminder lấy từ server (habit-reminders API) thành lịch WorkManager thật.
 * Mỗi nhắc nhở là 1 unique periodic work theo id — 24h cho bữa ăn (initial delay tính đúng giờ báo,
 * đã trừ "thời gian báo trước" nếu có), hoặc theo interval phút riêng cho Nước. Gọi syncFromServer()
 * mỗi khi tải lại danh sách; scheduleFromServer() cho 1 bản ghi vừa sửa/tạo.
 */
object ReminderScheduler {

    private fun uniqueHabitName(id: String) = "habit_reminder_$id"

    fun syncFromServer(context: Context, reminders: List<HabitReminderDto>) {
        ReminderNotificationHelper.ensureChannels(context)
        reminders.forEach { scheduleFromServer(context, it) }
    }

    fun scheduleFromServer(context: Context, reminder: HabitReminderDto) {
        val workManager = WorkManager.getInstance(context)
        val name = uniqueHabitName(reminder.id)

        if (!reminder.enabled) {
            workManager.cancelUniqueWork(name)
            return
        }

        val channelId = when (reminder.type) {
            "WATER" -> "water_reminders"
            "CUSTOM" -> ReminderNotificationHelper.CUSTOM_REMINDER_CHANNEL_ID
            else -> "meal_reminders"
        }
        val (title, message) = buildContent(reminder)
        val repeatDaysCsv = reminder.repeatDays.joinToString(",")

        val inputData = Data.Builder()
            .putString(ReminderWorker.KEY_HABIT_ID, reminder.id)
            .putString(ReminderWorker.KEY_HABIT_CHANNEL, channelId)
            .putString(ReminderWorker.KEY_HABIT_TITLE, title)
            .putString(ReminderWorker.KEY_HABIT_MESSAGE, message)
            .putString(ReminderWorker.KEY_REPEAT_DAYS, repeatDaysCsv)
            .putBoolean(ReminderWorker.KEY_IS_WATER, reminder.type == "WATER")
            .build()

        val request = if (reminder.type == "WATER") {
            val intervalMinutes = (reminder.waterIntervalMinutes ?: 120).coerceAtLeast(15)
            PeriodicWorkRequestBuilder<ReminderWorker>(intervalMinutes.toLong(), TimeUnit.MINUTES)
                .setInputData(inputData)
                .build()
        } else {
            val alarmTime = shiftTimeEarlier(reminder.timeOfDay, reminder.advanceNoticeMinutes)
            val initialDelayMs = delayUntilNextOccurrence(alarmTime)
            PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()
        }

        workManager.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancelHabitReminder(context: Context, id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueHabitName(id))
    }

    private fun buildContent(reminder: HabitReminderDto): Pair<String, String> {
        if (reminder.type == "WATER") {
            return "Uống nước thôi!" to "Đã đến chu kỳ nhắc uống nước bạn đặt."
        }

        val title = when (reminder.type) {
            "BREAKFAST" -> "Đến giờ ăn sáng rồi!"
            "LUNCH" -> "Đến giờ ăn trưa rồi!"
            "DINNER" -> "Đến giờ ăn tối rồi!"
            "SNACK" -> "${reminder.label} của bạn đây"
            else -> reminder.label
        }

        val foodNote = reminder.foods.firstOrNull()?.let { " Gợi ý: ${it.name}." } ?: ""
        val advanceNote = if (reminder.advanceNoticeMinutes > 0) {
            " Còn ${reminder.advanceNoticeMinutes} phút nữa tới giờ ăn (${reminder.timeOfDay})."
        } else ""
        val message = "Đừng quên ghi lại ${reminder.label.lowercase()} để CalAI tính đúng calo còn lại trong ngày.$advanceNote$foodNote"

        return title to message
    }

    /** Lùi 1 mốc "HH:mm" về sớm hơn N phút (dùng cho "Thời gian báo trước"). */
    private fun shiftTimeEarlier(time: String, minutesEarlier: Int): String {
        if (minutesEarlier <= 0) return time
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        val hour = parts.getOrElse(0) { 12 }
        val minute = parts.getOrElse(1) { 0 }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            add(Calendar.MINUTE, -minutesEarlier)
        }
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    /** Tính số ms từ hiện tại tới lần "HH:mm" gần nhất trong tương lai (hôm nay hoặc mai). */
    private fun delayUntilNextOccurrence(time: String): Long {
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        val hour = parts.getOrElse(0) { 12 }
        val minute = parts.getOrElse(1) { 0 }

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
