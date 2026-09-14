package com.calai.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TYPE = "reminder_type"
        const val KEY_CUSTOM_ID = "custom_reminder_id"
        const val KEY_CUSTOM_LABEL = "custom_reminder_label"
        const val TYPE_CUSTOM = "CUSTOM"
    }

    override suspend fun doWork(): Result {
        val typeName = inputData.getString(KEY_TYPE) ?: return Result.failure()
        ReminderNotificationHelper.ensureChannels(applicationContext)

        if (typeName == TYPE_CUSTOM) {
            val id = inputData.getString(KEY_CUSTOM_ID) ?: return Result.failure()
            val label = inputData.getString(KEY_CUSTOM_LABEL) ?: return Result.failure()
            ReminderNotificationHelper.showCustom(applicationContext, id, label)
            return Result.success()
        }

        val type = runCatching { ReminderType.valueOf(typeName) }.getOrNull() ?: return Result.failure()
        ReminderNotificationHelper.show(applicationContext, type)
        return Result.success()
    }
}
