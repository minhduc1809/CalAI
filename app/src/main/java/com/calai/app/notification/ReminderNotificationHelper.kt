package com.calai.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calai.app.R

/**
 * Tạo notification channel + hiển thị nhắc nhở bữa ăn/uống nước thật (đồng bộ từ server qua
 * habit-reminders API — xem ReminderScheduler). Dùng chung 1 icon launcher làm small icon vì app
 * chưa có bộ icon monochrome riêng cho notification.
 */
object ReminderNotificationHelper {

    const val CUSTOM_REMINDER_CHANNEL_ID = "custom_reminders"
    const val DAILY_COMPLETION_CHANNEL_ID = "daily_completion"
    const val DAILY_COMPLETION_NOTIFICATION_ID = 3_000_001
    const val ACTION_DAY_COMPLETE = "com.calai.app.action.DAY_COMPLETE"
    const val ACTION_DAY_INCOMPLETE = "com.calai.app.action.DAY_INCOMPLETE"
    const val EXTRA_DATE = "date"

    /** notificationId ổn định theo id của HabitReminder (hash để không đụng nhau giữa các nhắc nhở). */
    fun customNotificationId(reminderId: String): Int = 2_000_000 + (reminderId.hashCode() and 0x7FFFFFF)

    /**
     * Hiển thị notification cho 1 Habit Reminder đồng bộ từ server (bao gồm cả 5 loại mặc định
     * và nhắc tuỳ chỉnh) — title/message đã được ReminderScheduler dựng sẵn theo đúng dữ liệu
     * thật (mục tiêu calo, món ăn đã gắn) thay vì chuỗi tĩnh cố định.
     */
    fun showHabitReminder(context: Context, reminderId: String, channelId: String, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(customNotificationId(reminderId), notification)
    }

    /** Thông báo 21:00 "hôm nay bạn đã ghi đủ chưa?" với hai nút thao tác nhanh (BR-14). */
    fun showDailyCompletion(context: Context, date: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        fun actionIntent(action: String, requestCode: Int) = android.app.PendingIntent.getBroadcast(
            context,
            requestCode,
            android.content.Intent(context, DailyCompletionActionReceiver::class.java)
                .setAction(action)
                .putExtra(EXTRA_DATE, date),
            flags
        )
        val openApp = android.app.PendingIntent.getActivity(
            context,
            DAILY_COMPLETION_NOTIFICATION_ID,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            flags
        )
        val text = "Cho NutriWise biết để tính toán chính xác hơn. Ngày ghi chưa đủ sẽ không ảnh hưởng mục tiêu của bạn."
        val notification = NotificationCompat.Builder(context, DAILY_COMPLETION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hôm nay bạn đã ghi đủ bữa ăn chưa?")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openApp)
            .addAction(0, "Đủ rồi", actionIntent(ACTION_DAY_COMPLETE, 1))
            .addAction(0, "Chưa đủ", actionIntent(ACTION_DAY_INCOMPLETE, 2))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(DAILY_COMPLETION_NOTIFICATION_ID, notification)
    }

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                "meal_reminders",
                "Nhắc nhở bữa ăn",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nhắc ghi lại bữa Sáng/Trưa/Tối/Phụ đúng giờ đã đặt"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                "water_reminders",
                "Nhắc uống nước",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Nhắc uống nước định kỳ theo chu kỳ giờ đã đặt"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                DAILY_COMPLETION_CHANNEL_ID,
                "Nhắc ghi đủ cuối ngày",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nhắc xác nhận bạn đã ghi đủ bữa ăn trong ngày"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CUSTOM_REMINDER_CHANNEL_ID,
                "Nhắc nhở tuỳ chỉnh",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nhắc nhở do bạn tự tạo"
            }
        )
    }
}
