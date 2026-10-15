package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.ApiException
import com.calai.app.data.remote.dto.CheckInCoachingDto
import com.calai.app.data.remote.dto.CheckInDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Kết quả sau khi người dùng xử lý (hoặc đã xử lý trước đó) Check-in. */
enum class CheckinOutcome {
    ACCEPTED,
    DECLINED,
    SNOOZED,
    ACKNOWLEDGED,
    /** Mục tiêu đã đổi nên đề xuất hết giá trị (409), hoặc Check-in đã sang kỳ mới. */
    EXPIRED,
    /** Check-in của kỳ này đã được xử lý từ trước, không còn gì để làm. */
    ALREADY_HANDLED
}

/** Màn Check-in luôn ở đúng một trạng thái; mỗi trạng thái có giao diện riêng. */
sealed interface CheckinPhase {
    data object Loading : CheckinPhase
    data class Error(val message: String) : CheckinPhase

    /** Hồ sơ chưa đủ để có mục tiêu (PROFILE_INCOMPLETE). */
    data object NeedProfile : CheckinPhase

    /** Hết hạn mức Check-in của gói Free (QUOTA_EXCEEDED). */
    data class QuotaExceeded(val message: String) : CheckinPhase

    /** Có đề xuất (kể cả đề xuất "giữ nguyên"), chờ người dùng quyết định. */
    data class Proposal(val checkIn: CheckInDto, val coaching: CheckInCoachingDto?) : CheckinPhase

    /** Chưa đủ dữ liệu để đề xuất; chỉ có thể bấm "Đã hiểu". */
    data class NeedMoreData(val checkIn: CheckInDto, val coaching: CheckInCoachingDto?) : CheckinPhase

    data class Done(val outcome: CheckinOutcome, val message: String, val checkIn: CheckInDto? = null) : CheckinPhase

    /** Tài khoản chưa có đủ một tuần trọn vẹn: Check-in đầu tiên sẽ có vào [firstDate] (YYYY-MM-DD). */
    data class TooEarly(val firstDate: String?) : CheckinPhase
}

data class CheckinUiState(
    val phase: CheckinPhase = CheckinPhase.Loading,
    val isSubmitting: Boolean = false,
    /** Tâm trạng tuần qua (tuỳ chọn): GREAT | GOOD | OKAY | BAD | TERRIBLE. */
    val mood: String? = null,
    /** Lỗi khi gửi phản hồi nhưng vẫn còn ở màn đề xuất (mất mạng...). */
    val actionError: String? = null,
    val history: List<CheckInDto> = emptyList(),
    /** Ngày Check-in kế tiếp (YYYY-MM-DD) theo ngày người dùng đã chọn; null khi chưa biết. */
    val nextCheckinDate: String? = null
)

@HiltViewModel
class CheckinViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckinUiState())
    val uiState: StateFlow<CheckinUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Lấy Check-in đang chờ; chưa có thì tạo Check-in của kỳ hiện tại (backend chống tạo trùng). */
    fun load() {
        _uiState.update { it.copy(phase = CheckinPhase.Loading, actionError = null) }
        viewModelScope.launch {
            val pending = repository.fetchPendingCheckin()
            pending.getOrNull()?.nextCheckinDate?.let { next ->
                _uiState.update { it.copy(nextCheckinDate = next) }
            }
            val pendingCheckIn = pending.getOrNull()?.checkIn
            if (pendingCheckIn != null) {
                showCheckIn(pendingCheckIn, pending.getOrNull()?.coachingContent)
            } else if (pending.isFailure) {
                _uiState.update { it.copy(phase = phaseForError(pending.exceptionOrNull())) }
            } else {
                repository.generateCheckin().fold(
                    onSuccess = { generated ->
                        val created = generated.checkIn
                        generated.nextCheckinDate?.let { next ->
                            if (generated.tooEarly != true) _uiState.update { it.copy(nextCheckinDate = next) }
                        }
                        if (created == null && generated.tooEarly == true) {
                            _uiState.update { it.copy(phase = CheckinPhase.TooEarly(generated.nextCheckinDate)) }
                        } else if (created == null) {
                            _uiState.update { it.copy(phase = CheckinPhase.Error("Chưa tạo được Check-in cho tuần này")) }
                        } else {
                            showCheckIn(created, generated.coachingContent)
                        }
                    },
                    onFailure = { e -> _uiState.update { it.copy(phase = phaseForError(e)) } }
                )
            }
            loadHistory()
        }
    }

    private fun showCheckIn(checkIn: CheckInDto, coaching: CheckInCoachingDto?) {
        val phase = when (checkIn.status) {
            "PENDING" -> CheckinPhase.Proposal(checkIn, coaching)
            "INSUFFICIENT_DATA" -> CheckinPhase.NeedMoreData(checkIn, coaching)
            "SNOOZED" -> CheckinPhase.Done(
                CheckinOutcome.SNOOZED,
                "Bạn đã hoãn Check-in này. Nó sẽ quay lại sau 24 giờ kể từ lúc hoãn.",
                checkIn
            )
            else -> CheckinPhase.Done(
                CheckinOutcome.ALREADY_HANDLED,
                "Check-in tuần này đã được xử lý. Lần tới sẽ có Check-in mới vào đầu tuần.",
                checkIn
            )
        }
        _uiState.update { it.copy(phase = phase) }
    }

    private fun phaseForError(e: Throwable?): CheckinPhase = when {
        e is ApiException && e.isQuotaExceeded -> CheckinPhase.QuotaExceeded(e.message)
        e is ApiException && e.isProfileIncomplete -> CheckinPhase.NeedProfile
        else -> CheckinPhase.Error(e?.message ?: "Không thể tải Check-in")
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.fetchCheckinHistory(limit = 8).onSuccess { list ->
                // Check-in đang xem đã nằm ở phần trên, lịch sử chỉ liệt kê các tuần đã có kết quả
                val finished = setOf("ACCEPTED", "DECLINED", "EXPIRED", "ACKNOWLEDGED")
                _uiState.update { it.copy(history = list.filter { c -> c.status in finished }) }
            }
        }
    }

    fun selectMood(mood: String) {
        _uiState.update { it.copy(mood = if (it.mood == mood) null else mood) }
    }

    fun accept() = respond("ACCEPT")
    fun decline() = respond("DECLINE")
    fun snooze() = respond("SNOOZE")
    fun acknowledge() = respond("ACKNOWLEDGE")

    private fun respond(action: String) {
        val state = _uiState.value
        val checkIn = when (val p = state.phase) {
            is CheckinPhase.Proposal -> p.checkIn
            is CheckinPhase.NeedMoreData -> p.checkIn
            else -> return
        }
        if (state.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, actionError = null) }
        viewModelScope.launch {
            repository.respondToCheckin(checkIn.id, action, state.mood).fold(
                onSuccess = { updated ->
                    val done = when (action) {
                        "ACCEPT" -> CheckinPhase.Done(
                            CheckinOutcome.ACCEPTED,
                            "Mục tiêu mới đã được áp dụng: ${checkIn.proposedCalorieTarget} kcal mỗi ngày.",
                            updated
                        )
                        "DECLINE" -> CheckinPhase.Done(
                            CheckinOutcome.DECLINED,
                            "Mình giữ nguyên mục tiêu ${checkIn.currentCalorieTarget} kcal mỗi ngày nhé.",
                            updated
                        )
                        "SNOOZE" -> CheckinPhase.Done(
                            CheckinOutcome.SNOOZED,
                            "Mình sẽ nhắc lại sau 24 giờ.",
                            updated
                        )
                        else -> CheckinPhase.Done(CheckinOutcome.ACKNOWLEDGED, "Cảm ơn bạn, hẹn gặp lại ở Check-in sau.", updated)
                    }
                    _uiState.update { it.copy(isSubmitting = false, phase = done) }
                    loadHistory()
                },
                onFailure = { e ->
                    if (e is ApiException && e.status == 409) {
                        // Mục tiêu đã đổi kể từ lúc tạo đề xuất: đề xuất hết giá trị
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                phase = CheckinPhase.Done(CheckinOutcome.EXPIRED, e.message, checkIn)
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isSubmitting = false, actionError = e.message ?: "Không thể gửi phản hồi")
                        }
                    }
                }
            )
        }
    }
}

/**
 * Nạp Check-in đang chờ để Trang chủ hiện thẻ nhắc. Chỉ đọc, không tạo Check-in mới
 * (việc tạo diễn ra khi người dùng mở màn Check-in).
 */
@HiltViewModel
class CheckinBannerViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _pending = MutableStateFlow<CheckInDto?>(null)
    val pending: StateFlow<CheckInDto?> = _pending.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            // Lỗi (mất mạng, hết hạn mức...) thì không hiện thẻ, không làm phiền người dùng
            var checkIn = repository.fetchPendingCheckin().getOrNull()?.checkIn
            if (checkIn == null) {
                // BR-06.3: Check-in được tạo khi người dùng mở app sau ngày Check-in. Backend chống tạo trùng và
                // trả lại Check-in đã có của kỳ này, nên chỉ hiện thẻ khi nó còn chờ xử lý.
                val generated = repository.generateCheckin().getOrNull()?.checkIn
                checkIn = generated?.takeIf { it.status == "PENDING" || it.status == "INSUFFICIENT_DATA" }
            }
            _pending.value = checkIn
        }
    }
}
