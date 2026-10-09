package com.calai.app.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.calai.app.data.local.TokenManager
import com.calai.app.data.remote.CalAIApi
import com.calai.app.data.remote.dto.EntitlementDto
import com.calai.app.data.remote.dto.VerifyPurchaseRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/** Một gói bán, giá lấy từ Google Play (không viết cứng, BR-16.9). */
data class PremiumOffer(
    val productId: String,
    val title: String,
    val formattedPrice: String,
    val billingPeriod: String,
    val hasFreeTrial: Boolean,
    internal val details: ProductDetails,
    internal val offerToken: String
)

sealed interface PurchaseUiEvent {
    object Verified : PurchaseUiEvent
    object Pending : PurchaseUiEvent
    object Canceled : PurchaseUiEvent
    data class Error(val message: String) : PurchaseUiEvent
}

/**
 * BR-16.9: bọc BillingClient. Mọi quyền Premium chỉ được mở sau khi server xác minh với Google
 * (POST billing/verify); app không tự quyết định.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CalAIApi,
    private val tokenManager: TokenManager
) {
    companion object {
        val PRODUCT_IDS = listOf("premium_monthly", "premium_yearly")
        private const val MAX_VERIFY_ATTEMPTS = 3
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _offers = MutableStateFlow<List<PremiumOffer>>(emptyList())
    val offers: StateFlow<List<PremiumOffer>> = _offers.asStateFlow()

    private val _entitlement = MutableStateFlow<EntitlementDto?>(null)
    val entitlement: StateFlow<EntitlementDto?> = _entitlement.asStateFlow()

    private val _events = MutableStateFlow<PurchaseUiEvent?>(null)
    val events: StateFlow<PurchaseUiEvent?> = _events.asStateFlow()

    private val listener = PurchasesUpdatedListener { result, purchases ->
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                _events.value = PurchaseUiEvent.Canceled // người dùng huỷ: không gọi backend
            else ->
                _events.value = PurchaseUiEvent.Error("Không hoàn tất được giao dịch (${result.responseCode})")
        }
    }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(listener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    /** SHA-256 của userId, khớp với obfuscatedAccountId mà backend kiểm tra (BR-16.4). */
    private fun obfuscatedAccountId(): String {
        val userId = tokenManager.getUserId().orEmpty()
        return MessageDigest.getInstance("SHA-256").digest(userId.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private suspend fun ensureConnected(): Boolean {
        if (client.isReady) return true
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) cont.resumeWith(Result.success(result.responseCode == BillingClient.BillingResponseCode.OK))
                }
                override fun onBillingServiceDisconnected() { /* sẽ tự kết nối lại ở lần gọi sau */ }
            })
        }
    }

    fun consumeEvent() { _events.value = null }

    /** Tải entitlement từ server. Lỗi mạng thì giữ bản cũ. */
    suspend fun refreshEntitlement() {
        runCatching { api.getEntitlement().data }.getOrNull()?.let { _entitlement.value = it }
    }

    suspend fun loadOffers() {
        if (!ensureConnected()) return
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(PRODUCT_IDS.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it).setProductType(BillingClient.ProductType.SUBS).build()
            }).build()
        val details = client.queryProductDetails(params).productDetailsList.orEmpty()
        _offers.value = details.mapNotNull { d ->
            val offer = d.subscriptionOfferDetails?.firstOrNull { o ->
                o.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
            } ?: d.subscriptionOfferDetails?.firstOrNull() ?: return@mapNotNull null
            val paid = offer.pricingPhases.pricingPhaseList.last()
            PremiumOffer(
                productId = d.productId,
                title = d.name,
                formattedPrice = paid.formattedPrice,
                billingPeriod = paid.billingPeriod,
                hasFreeTrial = offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L },
                details = d,
                offerToken = offer.offerToken
            )
        }.sortedBy { PRODUCT_IDS.indexOf(it.productId) }
    }

    fun launchPurchase(activity: Activity, offer: PremiumOffer) {
        scope.launch {
            if (!ensureConnected()) {
                _events.value = PurchaseUiEvent.Error("Không kết nối được Google Play")
                return@launch
            }
            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(offer.details)
                            .setOfferToken(offer.offerToken)
                            .build()
                    )
                )
                .setObfuscatedAccountId(obfuscatedAccountId())
                .build()
            client.launchBillingFlow(activity, params)
        }
    }

    /** Mở app hoặc bấm "Khôi phục giao dịch": gửi lại mọi token còn hiệu lực lên server (idempotent). */
    fun restorePurchases() {
        scope.launch {
            if (!ensureConnected()) return@launch
            val result = client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
            )
            result.purchasesList.forEach { handlePurchase(it, silent = true) }
            refreshEntitlement()
        }
    }

    private fun handlePurchase(purchase: Purchase, silent: Boolean = false) {
        scope.launch {
            if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                if (!silent) _events.value = PurchaseUiEvent.Pending
                return@launch
            }
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return@launch
            val productId = purchase.products.firstOrNull() ?: return@launch

            // Không cấp quyền khi chưa xác minh được: thử lại theo backoff, lỗi hẳn thì chờ lần mở app sau
            var attempt = 0
            while (attempt < MAX_VERIFY_ATTEMPTS) {
                val response = runCatching {
                    api.verifyPurchase(VerifyPurchaseRequest(purchase.purchaseToken, productId))
                }
                val data = response.getOrNull()?.data
                if (data != null) {
                    _entitlement.value = data
                    if (!silent) _events.value = PurchaseUiEvent.Verified
                    return@launch
                }
                val http = (response.exceptionOrNull() as? retrofit2.HttpException)?.code()
                if (http == 409) {
                    if (!silent) _events.value = PurchaseUiEvent.Error(
                        "Gói này đã gắn với tài khoản khác, vui lòng đăng nhập đúng tài khoản."
                    )
                    return@launch
                }
                if (http == 400) {
                    if (!silent) _events.value = PurchaseUiEvent.Error("Giao dịch không hợp lệ.")
                    return@launch
                }
                attempt++
                delay(1000L * (1 shl attempt))
            }
            if (!silent) _events.value = PurchaseUiEvent.Error(
                "Chưa xác minh được giao dịch, hãy mở lại app, hệ thống sẽ tự thử lại."
            )
        }
    }
}
