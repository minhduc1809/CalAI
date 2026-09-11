package com.calai.app.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor thông minh tự động tìm và kết nối tới host khả dụng:
 * 1. 10.0.2.2 (cho Android Emulator)
 * 2. 127.0.0.1 (cho máy thật cắm USB với adb reverse tcp:3000 tcp:3000)
 * 3. 172.16.10.170 (cho máy thật kết nối qua cùng mạng Wi-Fi máy tính)
 * Khi một host kết nối thành công, tự động lưu lại (verifiedHost) để các request sau
 * chạy thẳng không cần retry.
 */
@Singleton
class DynamicHostInterceptor @Inject constructor() : Interceptor {

    private val candidateHosts = listOf(
        CalAIApi.EMULATOR_HOST,       // "10.0.2.2"
        CalAIApi.USB_REVERSE_HOST,    // "127.0.0.1"
        CalAIApi.PC_LAN_IP            // "172.16.10.170"
    )

    @Volatile
    private var verifiedHost: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalUrl = request.url

        // Nếu đã xác minh được host đang hoạt động, chuyển hướng ngay
        val primaryHost = verifiedHost ?: originalUrl.host
        val primaryRequest = if (primaryHost != originalUrl.host) {
            request.newBuilder()
                .url(originalUrl.newBuilder().host(primaryHost).build())
                .build()
        } else {
            request
        }

        try {
            val response = chain.proceed(primaryRequest)
            if (response.isSuccessful || response.code < 500) {
                if (verifiedHost == null) {
                    Log.i("DynamicHostInterceptor", "Đã xác thực host thành công: $primaryHost")
                    verifiedHost = primaryHost
                }
            }
            return response
        } catch (e: IOException) {
            Log.w(
                "DynamicHostInterceptor",
                "Không thể kết nối tới host $primaryHost (${e.message}). Đang tự động dò tìm các host khác..."
            )

            // Thử lần lượt các host còn lại
            for (candidate in candidateHosts) {
                if (candidate == primaryHost) continue
                try {
                    val newUrl = originalUrl.newBuilder().host(candidate).build()
                    val newRequest = request.newBuilder().url(newUrl).build()
                    val response = chain.proceed(newRequest)
                    if (response.isSuccessful || response.code < 500) {
                        Log.i(
                            "DynamicHostInterceptor",
                            ">>> TÌM THẤY KẾT NỐI THÀNH CÔNG: $candidate! Sẽ sử dụng host này."
                        )
                        verifiedHost = candidate
                        return response
                    }
                } catch (candidateError: IOException) {
                    Log.w("DynamicHostInterceptor", "Host $candidate không phản hồi: ${candidateError.message}")
                }
            }

            throw e
        }
    }
}
