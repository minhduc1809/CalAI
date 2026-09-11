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
    }

    override suspend fun doWork(): Result {
        val typeName = inputData.getString(KEY_TYPE) ?: return Result.failure()
        val type = runCatching { ReminderType.valueOf(typeName) }.getOrNull() ?: return Result.failure()
        ReminderNotificationHelper.ensureChannels(applicationContext)
        ReminderNotificationHelper.show(applicationContext, type)
        return Result.success()
    }
}
