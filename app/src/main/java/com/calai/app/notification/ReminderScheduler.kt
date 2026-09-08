package com.calai.app.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.calai.app.data.local.UserPreferencesManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Biến tuỳ chọn nhắc nhở đã lưu trong UserPreferencesManager thành lịch WorkManager thật.
 * Mỗi loại nhắc là 1 unique periodic work 24h (hoặc theo interval riêng cho nước), initial delay
 * được tính để lần bắn đầu tiên đúng vào giờ đã đặt — gọi lại scheduleAll() bất cứ khi nào người
 * dùng đổi tuỳ chọn để initial delay được tính lại từ thời điểm hiện tại.
 */
object ReminderScheduler {

    private fun uniqueName(type: ReminderType) = "reminder_${type.name.lowercase()}"

    fun scheduleAll(context: Context, prefs: UserPreferencesManager) {
        ReminderNotificationHelper.ensureChannels(context)

        scheduleMeal(context, ReminderType.BREAKFAST, prefs.isBreakfastReminderEnabled(), prefs.getBreakfastReminderTime())
        scheduleMeal(context, ReminderType.LUNCH, prefs.isLunchReminderEnabled(), prefs.getLunchReminderTime())
        scheduleMeal(context, ReminderType.DINNER, prefs.isDinnerReminderEnabled(), prefs.getDinnerReminderTime())
        scheduleMeal(context, ReminderType.SNACK, prefs.isSnackReminderEnabled(), prefs.getSnackReminderTime())
        scheduleWater(context, prefs.isWaterReminderEnabled(), prefs.getWaterReminderInterval())
    }

    private fun scheduleMeal(context: Context, type: ReminderType, enabled: Boolean, time: String) {
        val workManager = WorkManager.getInstance(context)
        val name = uniqueName(type)
        if (!enabled) {
            workManager.cancelUniqueWork(name)
            return
        }

        val initialDelayMs = delayUntilNextOccurrence(time)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putString(ReminderWorker.KEY_TYPE, type.name).build())
            .build()

        workManager.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun scheduleWater(context: Context, enabled: Boolean, intervalHours: Int) {
        val workManager = WorkManager.getInstance(context)
        val name = uniqueName(ReminderType.WATER)
        if (!enabled) {
            workManager.cancelUniqueWork(name)
            return
        }

        val safeIntervalHours = intervalHours.coerceAtLeast(1)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(safeIntervalHours.toLong(), TimeUnit.HOURS)
            .setInputData(Data.Builder().putString(ReminderWorker.KEY_TYPE, ReminderType.WATER.name).build())
            .build()

        workManager.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.UPDATE, request)
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
