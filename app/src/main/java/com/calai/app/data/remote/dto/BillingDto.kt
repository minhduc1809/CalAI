package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlanLimitsDto(
    @SerializedName("aiPhotoPerDay") val aiPhotoPerDay: Int = 5,
    @SerializedName("chatTokensPerDay") val chatTokensPerDay: Int = 50000
)

data class UsageTodayDto(
    @SerializedName("aiPhoto") val aiPhoto: Int = 0,
    @SerializedName("chatTokens") val chatTokens: Int = 0
)

/** GET me/entitlement (BR-15.3). plan: FREE | PREMIUM; banner: null | GRACE_PERIOD | ON_HOLD. */
data class EntitlementDto(
    @SerializedName("plan") val plan: String = "FREE",
    @SerializedName("status") val status: String? = null,
    @SerializedName("expiryTime") val expiryTime: String? = null,
    @SerializedName("autoRenewing") val autoRenewing: Boolean = false,
    @SerializedName("isTrial") val isTrial: Boolean = false,
    @SerializedName("productId") val productId: String? = null,
    @SerializedName("banner") val banner: String? = null,
    @SerializedName("limits") val limits: PlanLimitsDto = PlanLimitsDto(),
    @SerializedName("usageToday") val usageToday: UsageTodayDto = UsageTodayDto()
) {
    val isPremium: Boolean get() = plan == "PREMIUM"
}

data class VerifyPurchaseRequest(
    @SerializedName("purchaseToken") val purchaseToken: String,
    @SerializedName("productId") val productId: String
)

/** Gói bán qua QR ngân hàng (GET payments/plans). */
data class QrPlanDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("days") val days: Int
)

data class CreatePaymentOrderRequest(
    @SerializedName("productId") val productId: String
)

/** Đơn thanh toán QR. status: PENDING | PAID | EXPIRED | CANCELED. */
data class PaymentOrderDto(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("status") val status: String,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("transferContent") val transferContent: String = "",
    @SerializedName("bankName") val bankName: String = "",
    @SerializedName("accountNo") val accountNo: String = "",
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("qrUrl") val qrUrl: String = ""
)
