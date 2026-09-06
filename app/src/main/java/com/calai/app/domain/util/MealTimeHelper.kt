package com.calai.app.domain.util

import android.content.Context
import android.media.ExifInterface
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object MealTimeHelper {

    /**
     * Tự động xác định loại bữa ăn dựa vào thời gian:
     * - 04:00 - 10:29: Bữa Sáng (BREAKFAST)
     * - 10:30 - 14:29: Bữa Trưa (LUNCH)
     * - 14:30 - 17:29: Bữa Phụ (SNACK)
     * - 17:30 - 21:59: Bữa Tối (DINNER)
     * - 22:00 - 03:59: Bữa Phụ / Đêm (SNACK)
     */
    fun detectMealType(timestamp: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        return when {
            totalMinutes in (4 * 60)..(10 * 60 + 29) -> "BREAKFAST"
            totalMinutes in (10 * 60 + 30)..(14 * 60 + 29) -> "LUNCH"
            totalMinutes in (14 * 60 + 30)..(17 * 60 + 29) -> "SNACK"
            totalMinutes in (17 * 60 + 30)..(21 * 60 + 59) -> "DINNER"
            else -> "SNACK"
        }
    }

    /**
     * Tên tiếng Việt thân thiện của loại bữa ăn
     */
    fun getMealTypeLabel(type: String): String {
        return when (type.uppercase()) {
            "BREAKFAST" -> "Bữa Sáng"
            "LUNCH" -> "Bữa Trưa"
            "DINNER" -> "Bữa Tối"
            "SNACK" -> "Bữa Phụ"
            else -> "Bữa Ăn"
        }
    }

    /**
     * Nhận diện bữa ăn từ Exif Metadata của ảnh (giờ chụp thực tế) hoặc giờ hiện tại nếu ảnh không có metadata.
     * Trả về Pair(mealType, formattedTime) - ví dụ Pair("LUNCH", "12:15")
     */
    fun detectMealTypeFromImage(context: Context, uri: Uri): Pair<String, String> {
        var captureTime = System.currentTimeMillis()

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val dateTimeStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)

                if (!dateTimeStr.isNullOrBlank()) {
                    val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                    val parsedDate = sdf.parse(dateTimeStr)
                    if (parsedDate != null) {
                        captureTime = parsedDate.time
                    }
                }
            }
        } catch (_: Exception) {
            // Không đọc được Exif -> dùng captureTime = System.currentTimeMillis()
        }

        val mealType = detectMealType(captureTime)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val displayTime = timeFormat.format(Date(captureTime))

        return Pair(mealType, displayTime)
    }
}
