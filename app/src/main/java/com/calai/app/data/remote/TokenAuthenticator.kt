package com.calai.app.data.remote

import android.util.Log
import com.calai.app.data.local.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator tự động refresh Access Token khi nhận HTTP 401.
 *
 * Khi bất kỳ request nào trả về 401 Unauthorized:
 * 1. Lấy refresh token từ TokenManager
 * 2. Gọi POST /api/v1/auth/refresh để lấy token mới
 * 3. Lưu token mới vào TokenManager
 * 4. Retry request ban đầu với access token mới
 *
 * Nếu refresh thất bại → clear session, user phải đăng nhập lại.
 * Thread-safe: chỉ 1 thread refresh tại một thời điểm.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRY = 1  // Chỉ retry refresh 1 lần
    }

    private val gson = Gson()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Nếu đã retry refresh rồi mà vẫn 401 → dừng lại, tránh vòng lặp vô hạn
        if (responseCount(response) > MAX_RETRY) {
            Log.w(TAG, "Đã retry refresh $MAX_RETRY lần, vẫn 401. Dừng retry.")
            return null
        }

        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            Log.w(TAG, "Không có refresh token. User cần đăng nhập lại.")
            tokenManager.clear()
            return null
        }

        // Đồng bộ để tránh nhiều thread cùng refresh 1 lúc
        synchronized(this) {
            // Kiểm tra xem thread khác đã refresh thành công chưa
            val currentToken = tokenManager.getAccessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (currentToken != null && currentToken != requestToken) {
                // Token đã được thread khác refresh → retry với token mới
                Log.i(TAG, "Token đã được refresh bởi thread khác. Retry với token mới.")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Thực hiện refresh token
            return try {
                val newAccessToken = refreshAccessToken(response, refreshToken)
                if (newAccessToken != null) {
                    Log.i(TAG, "✅ Token refreshed successfully! Retry request gốc.")
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    Log.w(TAG, "❌ Refresh token thất bại. Clear session.")
                    tokenManager.clear()
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception khi refresh token: ${e.message}", e)
                tokenManager.clear()
                null
            }
        }
    }

    /**
     * Gọi POST /api/v1/auth/refresh trực tiếp bằng OkHttp
     * (không qua Retrofit/CalAIApi để tránh circular dependency với Interceptor)
     */
    private fun refreshAccessToken(originalResponse: Response, refreshToken: String): String? {
        // Xây dựng URL refresh dựa trên host đang hoạt động của request gốc
        val originalUrl = originalResponse.request.url
        val refreshUrl = originalUrl.newBuilder()
            .encodedPath("/api/v1/auth/refresh")
            .build()

        val jsonBody = gson.toJson(mapOf("refreshToken" to refreshToken))
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .post(requestBody)
            .build()

        // Dùng OkHttpClient đơn giản (không có interceptor) để tránh vòng lặp
        // Timeout rõ ràng để đảm bảo request refresh không bao giờ treo vô thời hạn.
        val client = OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val refreshResponse = client.newCall(refreshRequest).execute()
        val body = refreshResponse.body?.string()

        if (refreshResponse.isSuccessful && body != null) {
            try {
                val json = gson.fromJson(body, JsonObject::class.java)
                val data = json.getAsJsonObject("data")
                if (data != null) {
                    val newAccessToken = data.get("accessToken")?.asString
                    val newRefreshToken = data.get("refreshToken")?.asString
                    if (!newAccessToken.isNullOrBlank() && !newRefreshToken.isNullOrBlank()) {
                        tokenManager.saveTokensSync(newAccessToken, newRefreshToken)
                        return newAccessToken
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Parse refresh response lỗi: ${e.message}")
            }
        } else {
            Log.w(TAG, "Refresh API trả về ${refreshResponse.code}: $body")
        }
        return null
    }

    /**
     * Đếm số lần response đã retry (dựa trên response chain)
     */
    private fun responseCount(response: Response): Int {
        var count = 0
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
