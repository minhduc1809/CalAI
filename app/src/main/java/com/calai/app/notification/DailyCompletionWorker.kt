package com.calai.app.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.calai.app.data.local.CalAIDao
import com.calai.app.data.local.TokenManager
import com.calai.app.domain.DailyCompletionRule
import com.calai.app.domain.repository.CalAIRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun todayDateKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

/**
 * Nhắc 21:00 "hôm nay bạn đã ghi đủ chưa?" (BR-14). Chỉ bắn khi hôm nay đã có ít nhất một bữa và ngày chưa được
 * đánh dấu. Ưu tiên dữ liệu mới nhất từ máy chủ; mất mạng thì dùng dữ liệu bữa ăn lưu trên máy (trạng thái đã
 * đánh dấu chưa biết nên vẫn nhắc, vì nhắc thừa một lần nhẹ hơn là bỏ lỡ).
 */
@HiltWorker
class DailyCompletionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CalAIRepository,
    private val tokenManager: TokenManager,
    private val dao: CalAIDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!tokenManager.isLoggedIn()) return Result.success()
        val today = todayDateKey()

        val summary = repository.fetchDailySummary(today).getOrNull()
        val remind = if (summary != null) {
            DailyCompletionRule.shouldRemind(summary.mealsCount, summary.logStatus?.completeness)
        } else {
            DailyCompletionRule.shouldRemind(localMealsToday(), null)
        }
        if (remind) {
            ReminderNotificationHelper.ensureChannels(applicationContext)
            ReminderNotificationHelper.showDailyCompletion(applicationContext, today)
        }
        return Result.success()
    }

    private suspend fun localMealsToday(): Int {
        val userId = tokenManager.getUserId() ?: return 0
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return try {
            dao.getMeals(userId).first().count { it.timestamp >= start }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            0
        }
    }
}

/** Gửi lựa chọn "Đủ rồi"/"Chưa đủ" lên máy chủ, tự thử lại khi mất mạng. */
@HiltWorker
class DailyCompletionActionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CalAIRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_DATE = "date"
        const val KEY_COMPLETENESS = "completeness"
        private const val MAX_ATTEMPTS = 6

        fun enqueue(context: Context, date: String, completeness: String) {
            val request = OneTimeWorkRequestBuilder<DailyCompletionActionWorker>()
                .setInputData(Data.Builder().putString(KEY_DATE, date).putString(KEY_COMPLETENESS, completeness).build())
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result {
        val date = inputData.getString(KEY_DATE) ?: return Result.failure()
        val completeness = inputData.getString(KEY_COMPLETENESS) ?: return Result.failure()
        return repository.setDayStatus(date, completeness).fold(
            onSuccess = { Result.success() },
            onFailure = { if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure() }
        )
    }
}
