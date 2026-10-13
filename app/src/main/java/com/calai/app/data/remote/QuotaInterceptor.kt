package com.calai.app.data.remote

import com.calai.app.data.billing.UpgradeEvents
import com.calai.app.data.billing.UpgradePrompt
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bắt lỗi hết hạn mức (429 QUOTA_EXCEEDED) và cần Premium (402 PREMIUM_REQUIRED) ở tầng mạng để mọi màn đều hiển thị
 * lời mời nâng cấp đúng ngữ cảnh mà không phải sửa từng màn (BR-16.9). Chỉ đọc bản xem trước của thân phản hồi, không
 * tiêu thụ nó; lỗi đọc thì bỏ qua.
 */
@Singleton
class QuotaInterceptor @Inject constructor(
    private val events: UpgradeEvents
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code != 429 && response.code != 402) return response
        try {
            val body = response.peekBody(8 * 1024).string()
            val json = JsonParser.parseString(body).asJsonObject
            val code = json.get("code")?.takeIf { it.isJsonPrimitive }?.asString ?: return response
            if (code != "QUOTA_EXCEEDED" && code != "PREMIUM_REQUIRED") return response
            fun str(k: String) = json.get(k)?.takeIf { it.isJsonPrimitive }?.asString
            fun int(k: String) = json.get(k)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asInt
            events.emit(
                UpgradePrompt(
                    code = code,
                    feature = str("feature"),
                    message = str("message") ?: "Tính năng này cần gói Premium",
                    limit = int("limit"),
                    used = int("used"),
                    resetsAt = str("resetsAt"),
                    upgrade = json.get("upgrade")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: (code == "PREMIUM_REQUIRED")
                )
            )
        } catch (_: Exception) {
            // Không đọc được thì để lỗi đi tiếp như bình thường
        }
        return response
    }
}
