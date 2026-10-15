package com.calai.app.domain

import java.util.Calendar

/**
 * Quy tắc nhắc "ghi đủ hôm nay" lúc 21:00 (BR-14, H2). Một nguồn duy nhất cho worker quyết định có bắn hay không
 * và cho việc tính giờ bắn kế tiếp, để kiểm thử được mà không cần Android.
 */
object DailyCompletionRule {
    const val DEFAULT_TIME = "21:00"

    /**
     * Chỉ nhắc khi hôm nay đã có ít nhất một bữa VÀ ngày chưa được đánh dấu (AUTO hoặc chưa có trạng thái).
     * Đã bấm "Đủ rồi" (COMPLETE) hay "Chưa đủ" (INCOMPLETE) rồi thì không làm phiền nữa.
     */
    fun shouldRemind(mealsCount: Int, completeness: String?): Boolean =
        mealsCount >= 1 && (completeness == null || completeness == "AUTO")

    /** "HH:mm" → (giờ, phút); chuỗi sai trả về giờ mặc định 21:00 thay vì làm hỏng lịch nhắc. */
    fun parseTime(time: String?): Pair<Int, Int> {
        val parts = (time ?: DEFAULT_TIME).split(":").mapNotNull { it.trim().toIntOrNull() }
        val hour = parts.getOrNull(0)
        val minute = parts.getOrNull(1)
        if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) return 21 to 0
        return hour to minute
    }

    fun formatTime(hour: Int, minute: Int): String = String.format("%02d:%02d", hour, minute)

    /** Số ms từ [now] tới lần "HH:mm" gần nhất trong tương lai (hôm nay hoặc mai). */
    fun delayUntilNext(time: String?, now: Calendar = Calendar.getInstance()): Long {
        val (hour, minute) = parseTime(time)
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis - now.timeInMillis
    }
}
