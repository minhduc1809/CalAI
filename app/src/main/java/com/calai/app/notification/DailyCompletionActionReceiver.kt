package com.calai.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/** Nhận bấm nút "Đủ rồi" / "Chưa đủ" trên thông báo, đóng thông báo và giao việc gọi máy chủ cho WorkManager. */
class DailyCompletionActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val date = intent.getStringExtra(ReminderNotificationHelper.EXTRA_DATE) ?: return
        val completeness = when (intent.action) {
            ReminderNotificationHelper.ACTION_DAY_COMPLETE -> "COMPLETE"
            ReminderNotificationHelper.ACTION_DAY_INCOMPLETE -> "INCOMPLETE"
            else -> return
        }
        NotificationManagerCompat.from(context).cancel(ReminderNotificationHelper.DAILY_COMPLETION_NOTIFICATION_ID)
        DailyCompletionActionWorker.enqueue(context, date, completeness)
    }
}
