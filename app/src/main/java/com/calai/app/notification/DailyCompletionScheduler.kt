package com.calai.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.domain.DailyCompletionRule
import java.util.concurrent.TimeUnit

/** Lên lịch nhắc "ghi đủ hôm nay" hằng ngày theo giờ đã chọn (mặc định 21:00); tắt thì huỷ lịch. */
object DailyCompletionScheduler {
    const val UNIQUE_NAME = "daily_completion_reminder"

    fun sync(context: Context, prefs: UserPreferencesManager) =
        sync(context, prefs.isDailyCompletionEnabled(), prefs.getDailyCompletionTime())

    fun sync(context: Context, enabled: Boolean, time: String) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(UNIQUE_NAME)
            return
        }
        ReminderNotificationHelper.ensureChannels(context)
        val request = PeriodicWorkRequestBuilder<DailyCompletionWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(DailyCompletionRule.delayUntilNext(time), TimeUnit.MILLISECONDS)
            .build()
        // UPDATE: đổi giờ có hiệu lực ngay, không tạo thêm lịch thứ hai
        workManager.enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}
