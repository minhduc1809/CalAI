package com.calai.app.data.billing

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lời mời nâng cấp theo ngữ cảnh (BR-16.9): máy chủ trả 429 QUOTA_EXCEEDED hoặc 402 PREMIUM_REQUIRED thì app hiện hộp
 * thoại "Xem gói Premium" ở bất kỳ màn nào. Không bao giờ hiện lúc mở app.
 */
data class UpgradePrompt(
    val code: String,
    val feature: String?,
    val message: String,
    val limit: Int?,
    val used: Int?,
    /** ISO 8601, thời điểm hạn mức được làm mới. */
    val resetsAt: String?,
    /** false khi người dùng đã là Premium (hết hạn mức Premium thì không mời nâng cấp). */
    val upgrade: Boolean
)

@Singleton
class UpgradeEvents @Inject constructor() {
    private val _events = MutableSharedFlow<UpgradePrompt>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<UpgradePrompt> = _events.asSharedFlow()

    /** Các tính năng đã có giao diện riêng khi hết lượt, không hiện thêm hộp thoại chung. */
    private val handledInScreen = setOf("WEEKLY_CHECKIN", "BARCODE")

    fun emit(prompt: UpgradePrompt) {
        if (prompt.feature in handledInScreen) return
        _events.tryEmit(prompt)
    }
}
