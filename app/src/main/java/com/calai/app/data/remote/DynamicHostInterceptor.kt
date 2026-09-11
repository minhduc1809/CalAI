package com.calai.app.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor thông minh tự động chuyển đổi giữa IP Wi-Fi và USB localhost (127.0.0.1)
 * cho máy thật: nếu kết nối qua Wi-Fi thất bại, tự động thử lại qua USB cáp (adb reverse)
 * hoặc ngược lại, giúp trải nghiệm mượt mà không cần sửa code.
 */
@Singleton
class DynamicHostInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalUrl = request.url

        try {
            return chain.proceed(request)
        } catch (e: IOException) {
            val currentHost = originalUrl.host
            val fallbackHost = when (currentHost) {
                CalAIApi.PC_LAN_IP -> CalAIApi.USB_REVERSE_HOST
                CalAIApi.USB_REVERSE_HOST -> CalAIApi.PC_LAN_IP
                else -> null
            }

            if (fallbackHost != null) {
                Log.w(
                    "DynamicHostInterceptor",
                    "Không thể kết nối tới $currentHost: ${e.message}. Đang tự động chuyển sang $fallbackHost..."
                )
                val newUrl = originalUrl.newBuilder()
                    .host(fallbackHost)
                    .build()
                val newRequest = request.newBuilder()
                    .url(newUrl)
                    .build()
                return chain.proceed(newRequest)
            }
            throw e
        }
    }
}
