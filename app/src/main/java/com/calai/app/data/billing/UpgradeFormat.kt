package com.calai.app.data.billing

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** "2026-11-01T00:00:00.000Z" thành giờ địa phương dạng "01/11 07:00"; không đọc được thì null. */
fun formatResetTime(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val date = parser.parse(iso.take(19)) ?: return null
        SimpleDateFormat("dd/MM HH:mm", Locale("vi", "VN")).format(date)
    } catch (_: Exception) {
        null
    }
}
