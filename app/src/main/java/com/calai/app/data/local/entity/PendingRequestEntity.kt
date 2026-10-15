package com.calai.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Hàng đợi ghi bữa ăn / buổi tập khi mất mạng (BR-07.5, BR-10.4). Mỗi dòng giữ nguyên nội dung yêu cầu (JSON) cùng
 * `clientRequestId` do app tạo lúc bấm Lưu; gửi lại bao nhiêu lần máy chủ vẫn chỉ ghi một bản (idempotent).
 *
 * Trạng thái: PENDING_SYNC (đang chờ gửi) → xoá khỏi bảng khi gửi xong; FAILED khi máy chủ từ chối hẳn (4xx) hoặc
 * thử quá số lần cho phép, để người dùng xem lý do và xoá hoặc thử lại.
 */
@Entity(
    tableName = "pending_requests",
    indices = [Index(value = ["clientRequestId"], unique = true)]
)
data class PendingRequestEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    /** "MEAL" hoặc "WORKOUT". */
    val kind: String,
    val clientRequestId: String,
    /** JSON của CreateMealRequest hoặc CreateWorkoutLogRequest. */
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
    val status: String = STATUS_PENDING,
    val lastError: String? = null
) {
    companion object {
        const val KIND_MEAL = "MEAL"
        const val KIND_WORKOUT = "WORKOUT"
        const val STATUS_PENDING = "PENDING_SYNC"
        const val STATUS_FAILED = "FAILED"
    }
}
