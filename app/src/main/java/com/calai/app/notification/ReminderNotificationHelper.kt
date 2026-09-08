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

enum class ReminderType(val channelId: String, val notificationId: Int) {
    BREAKFAST("meal_reminders", 1001),
    LUNCH("meal_reminders", 1002),
    DINNER("meal_reminders", 1003),
    SNACK("meal_reminders", 1004),
    WATER("water_reminders", 1005)
}

/**
 * Tạo notification channel + hiển thị nhắc nhở bữa ăn/uống nước thật.
 * Dùng chung 1 icon launcher làm small icon vì app chưa có bộ icon monochrome riêng cho notification.
 */
object ReminderNotificationHelper {

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
    }

    fun show(context: Context, type: ReminderType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val (title, message) = when (type) {
            ReminderType.BREAKFAST -> "Đến giờ ăn sáng rồi!" to "Đừng quên ghi lại bữa sáng để theo dõi mục tiêu dinh dưỡng nhé."
            ReminderType.LUNCH -> "Đến giờ ăn trưa rồi!" to "Ghi lại bữa trưa để CalAI tính đúng calo còn lại trong ngày."
            ReminderType.DINNER -> "Đến giờ ăn tối rồi!" to "Đừng quên ghi lại bữa tối nhé."
            ReminderType.SNACK -> "Bữa phụ của bạn đây" to "Ghi lại bữa ăn nhẹ để không bỏ sót calo đã nạp."
            ReminderType.WATER -> "Uống nước thôi!" to "Đã đến chu kỳ nhắc uống nước bạn đặt trong Cài đặt."
        }

        val notification = NotificationCompat.Builder(context, type.channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(type.notificationId, notification)
    }
}
