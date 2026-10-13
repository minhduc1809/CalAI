package com.calai.app.data.remote

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.HttpException

/**
 * Lỗi từ máy chủ kèm mã máy đọc được (QUOTA_EXCEEDED, PROFILE_INCOMPLETE, AI_UNAVAILABLE...) và các trường chi tiết
 * (feature, limit, resetsAt...). Màn hình xử lý theo [code] thay vì dò chữ trong [message].
 */
class ApiException(
    override val message: String,
    val status: Int,
    val code: String? = null,
    val details: JsonObject? = null
) : Exception(message) {

    val isQuotaExceeded: Boolean get() = code == "QUOTA_EXCEEDED"
    val isProfileIncomplete: Boolean get() = code == "PROFILE_INCOMPLETE"

    companion object {
        /** Đọc thân lỗi của [HttpException]; lỗi khác (mất mạng...) giữ nguyên thông báo gốc. */
        fun from(e: Throwable, fallback: String = "Có lỗi xảy ra"): Throwable {
            if (e !is HttpException) return e
            val body = try {
                e.response()?.errorBody()?.string()
            } catch (_: Exception) {
                null
            }
            if (body.isNullOrBlank()) return ApiException(e.message() ?: fallback, e.code())
            return try {
                val json = JsonParser.parseString(body).asJsonObject
                val message = json.get("message")?.let { el ->
                    when {
                        el.isJsonArray -> el.asJsonArray.joinToString("\n") { it.asString }
                        el.isJsonPrimitive -> el.asString
                        else -> null
                    }
                } ?: fallback
                ApiException(
                    message = message,
                    status = e.code(),
                    code = json.get("code")?.takeIf { it.isJsonPrimitive }?.asString,
                    details = json.get("details")?.takeIf { it.isJsonObject }?.asJsonObject
                )
            } catch (_: Exception) {
                ApiException(fallback, e.code())
            }
        }
    }
}
