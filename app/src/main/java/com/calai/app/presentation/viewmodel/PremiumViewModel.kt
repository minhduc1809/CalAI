package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.billing.BillingManager
import com.calai.app.data.billing.PremiumOffer
import com.calai.app.data.billing.PurchaseUiEvent
import com.calai.app.data.remote.CalAIApi
import com.calai.app.data.remote.dto.CreatePaymentOrderRequest
import com.calai.app.data.remote.dto.EntitlementDto
import com.calai.app.data.remote.dto.PaymentOrderDto
import com.calai.app.data.remote.dto.QrPlanDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val POLL_INTERVAL_MS = 5_000L

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billing: BillingManager,
    private val api: CalAIApi
) : ViewModel() {

    val offers: StateFlow<List<PremiumOffer>> = billing.offers
    val entitlement: StateFlow<EntitlementDto?> = billing.entitlement
    val events: StateFlow<PurchaseUiEvent?> = billing.events

    private val _qrPlans = MutableStateFlow<List<QrPlanDto>>(emptyList())
    val qrPlans: StateFlow<List<QrPlanDto>> = _qrPlans.asStateFlow()

    /** Đơn QR đang hiển thị trên hộp thoại; null = không có hộp thoại. */
    private val _order = MutableStateFlow<PaymentOrderDto?>(null)
    val order: StateFlow<PaymentOrderDto?> = _order.asStateFlow()

    private val _qrMessage = MutableStateFlow<String?>(null)
    val qrMessage: StateFlow<String?> = _qrMessage.asStateFlow()

    private var pollJob: Job? = null

    init {
        viewModelScope.launch {
            billing.refreshEntitlement()
            billing.loadOffers()
        }
        viewModelScope.launch {
            _qrPlans.value = runCatching { api.getQrPlans().data }.getOrNull().orEmpty()
        }
    }

    fun buy(activity: android.app.Activity, offer: PremiumOffer) = billing.launchPurchase(activity, offer)
    fun restore() = billing.restorePurchases()
    fun consumeEvent() = billing.consumeEvent()
    fun consumeQrMessage() { _qrMessage.value = null }

    fun startQr(productId: String) {
        viewModelScope.launch {
            val created = runCatching { api.createPaymentOrder(CreatePaymentOrderRequest(productId)).data }
            val order = created.getOrNull()
            if (order == null) {
                _qrMessage.value = "Chưa tạo được mã thanh toán. Hãy thử lại sau."
                return@launch
            }
            _order.value = order
            poll(order.id)
        }
    }

    /** Hỏi server định kỳ; khi admin duyệt đơn thì tải lại entitlement để mở khoá. */
    private fun poll(orderId: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                delay(POLL_INTERVAL_MS)
                val latest = runCatching { api.getPaymentOrder(orderId).data }.getOrNull() ?: continue
                when (latest.status) {
                    "PAID" -> {
                        _order.value = null
                        billing.refreshEntitlement()
                        _qrMessage.value = "Đã nhận thanh toán. Premium đã được kích hoạt!"
                        return@launch
                    }
                    "EXPIRED", "CANCELED" -> {
                        _order.value = null
                        _qrMessage.value = "Mã thanh toán đã hết hạn. Nếu bạn đã chuyển tiền, hãy liên hệ quản trị viên kèm nội dung ${latest.code}."
                        return@launch
                    }
                }
            }
        }
    }

    /** Đóng hộp thoại: dừng hỏi và huỷ đơn chưa trả. */
    fun closeQr() {
        pollJob?.cancel()
        val id = _order.value?.id
        _order.value = null
        if (id != null) viewModelScope.launch { runCatching { api.cancelPaymentOrder(id) } }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
