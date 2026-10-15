package com.calai.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.calai.app.data.local.CalAIDao
import com.calai.app.data.local.entity.PendingRequestEntity
import com.calai.app.data.remote.ApiException
import com.calai.app.data.remote.CalAIApi
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CreateMealRequest
import com.calai.app.data.remote.dto.CreateWorkoutLogRequest
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tổng calo/macro tạm tính của một bữa đang chờ gửi (calo mỗi món tính trên MỘT đơn vị `quantity`, như máy chủ). */
data class PendingMealTotals(val calories: Float, val protein: Float, val carb: Float, val fat: Float) {
    companion object {
        fun of(items: List<CreateMealItemDto>): PendingMealTotals {
            var c = 0f; var p = 0f; var cb = 0f; var f = 0f
            for (it in items) {
                val q = if (it.quantity > 0f) it.quantity else 1f
                c += it.calories * q; p += it.protein * q; cb += it.carb * q; f += it.fat * q
            }
            return PendingMealTotals(c, p, cb, f)
        }
    }
}

/**
 * Tổng calo/macro tạm tính của các bữa ăn ĐANG CHỜ GỬI trong ngày `dateIso` (yyyy-MM-dd), để Trang chủ cộng vào "Đã ăn"
 * ngay cả khi chưa lên được máy chủ (BR-07.5). Bữa đã bị từ chối (FAILED) không tính vì sẽ không bao giờ được ghi.
 */
fun pendingMealTotalsFor(items: List<PendingRequestEntity>, dateIso: String): PendingMealTotals {
    val gson = Gson()
    var c = 0f; var p = 0f; var cb = 0f; var f = 0f
    for (item in items) {
        if (item.kind != PendingRequestEntity.KIND_MEAL || item.status != PendingRequestEntity.STATUS_PENDING) continue
        val req = try {
            gson.fromJson(item.payload, com.calai.app.data.remote.dto.CreateMealRequest::class.java)
        } catch (_: Exception) {
            null
        } ?: continue
        if (req.date.take(10) != dateIso) continue
        val t = PendingMealTotals.of(req.items)
        c += t.calories; p += t.protein; cb += t.carb; f += t.fat
    }
    return PendingMealTotals(c, p, cb, f)
}

/** Phân loại lỗi khi gửi một yêu cầu trong hàng đợi. */
sealed interface SendOutcome {
    data object Sent : SendOutcome
    /** Máy chủ từ chối hẳn (4xx như ngày tương lai, số liệu vượt giới hạn): thử lại vô ích. */
    data class Rejected(val message: String) : SendOutcome
    /** Mất mạng hoặc máy chủ tạm lỗi: giữ lại và thử sau. */
    data class Retry(val message: String?) : SendOutcome
}

/** Quyết định một lỗi là nên thử lại hay từ chối hẳn. Hàm thuần để unit test. */
fun classifyFailure(e: Throwable): SendOutcome {
    if (e is HttpException) {
        val code = e.code()
        // 408 hết giờ và 429 quá nhiều yêu cầu là tạm thời; 401 do phiên hết hạn sẽ được làm mới nên cũng thử lại
        if (code == 408 || code == 429 || code == 401 || code >= 500) return SendOutcome.Retry("HTTP $code")
        return SendOutcome.Rejected((ApiException.from(e) as? ApiException)?.message ?: "Máy chủ từ chối (HTTP $code)")
    }
    if (e is IOException) return SendOutcome.Retry(e.message)
    // Lỗi không phải mạng (ví dụ đọc phản hồi hỏng) thì thử lại cũng vô ích: báo ngay
    return SendOutcome.Rejected(e.message ?: "Lỗi không xác định")
}

/** Lỗi này có đáng đưa vào hàng đợi (thay vì báo lỗi ngay) không? */
fun shouldQueue(e: Throwable): Boolean = classifyFailure(e) is SendOutcome.Retry

/** Số lần thử tối đa trước khi đánh dấu FAILED để không lặp vô hạn. */
const val MAX_SYNC_ATTEMPTS = 12

@Singleton
class PendingSyncProcessor @Inject constructor(
    private val dao: CalAIDao,
    private val api: CalAIApi
) {
    private val gson = Gson()

    /** Gửi một yêu cầu trong hàng đợi lên máy chủ. */
    suspend fun send(item: PendingRequestEntity): SendOutcome {
        return try {
            when (item.kind) {
                PendingRequestEntity.KIND_MEAL ->
                    api.createMeal(gson.fromJson(item.payload, CreateMealRequest::class.java))
                PendingRequestEntity.KIND_WORKOUT ->
                    api.createWorkout(gson.fromJson(item.payload, CreateWorkoutLogRequest::class.java))
                else -> return SendOutcome.Rejected("Loại yêu cầu không hỗ trợ: ${item.kind}")
            }
            SendOutcome.Sent
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            classifyFailure(e)
        }
    }

    /**
     * Gửi mọi yêu cầu đang chờ theo thứ tự thời gian. Trả true nếu còn yêu cầu cần thử lại sau (để WorkManager đặt lại
     * lịch). Gửi xong thì xoá; bị từ chối hoặc quá số lần thử thì đánh dấu FAILED kèm lý do.
     */
    suspend fun processAll(): Boolean {
        var needsRetry = false
        for (item in dao.getPendingRequests(PendingRequestEntity.STATUS_PENDING)) {
            when (val outcome = send(item)) {
                SendOutcome.Sent -> dao.deletePendingRequest(item.localId)
                is SendOutcome.Rejected -> dao.updatePendingRequest(
                    item.copy(status = PendingRequestEntity.STATUS_FAILED, lastError = outcome.message, attempts = item.attempts + 1)
                )
                is SendOutcome.Retry -> {
                    val attempts = item.attempts + 1
                    if (attempts >= MAX_SYNC_ATTEMPTS) {
                        dao.updatePendingRequest(
                            item.copy(
                                status = PendingRequestEntity.STATUS_FAILED,
                                lastError = outcome.message ?: "Không gửi được sau nhiều lần thử",
                                attempts = attempts
                            )
                        )
                    } else {
                        dao.updatePendingRequest(item.copy(attempts = attempts, lastError = outcome.message))
                        needsRetry = true
                    }
                }
            }
        }
        return needsRetry
    }
}

@HiltWorker
class PendingSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val processor: PendingSyncProcessor
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = if (processor.processAll()) Result.retry() else Result.success()
}

object PendingSyncScheduler {
    private const val UNIQUE_NAME = "pending_sync"

    /** Đặt lịch gửi hàng đợi khi có mạng, thử lại theo cấp số nhân. Gọi nhiều lần vẫn chỉ có một lịch. */
    fun schedule(context: Context) {
        val request = OneTimeWorkRequestBuilder<PendingSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
