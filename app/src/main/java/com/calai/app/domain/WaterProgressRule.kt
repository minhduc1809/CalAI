package com.calai.app.domain

import java.util.Calendar

/**
 * Nguồn duy nhất cho quy tắc "tiến độ uống nước còn thiếu" — dùng chung ở cả UI (card dashboard)
 * và ReminderWorker (quyết định có bắn thông báo nhắc uống nước hay không). Chỉ 1 công thức để
 * không bị lệch giữa hiển thị và nhắc nhở.
 */
object WaterProgressRule {
    private const val DAY_START_MIN = 7 * 60
    private const val DAY_END_MIN = 22 * 60

    /** Lượng nước kỳ vọng đã uống tại thời điểm hiện tại — tuyến tính từ 07:00 đến 22:00. */
    fun expectedMl(goalMl: Int, now: Calendar = Calendar.getInstance()): Int {
        val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val fraction = ((nowMin - DAY_START_MIN).toFloat() / (DAY_END_MIN - DAY_START_MIN)).coerceIn(0f, 1f)
        return (goalMl * fraction).toInt()
    }

    /** true nếu hiện đang thiếu ít nhất 1 ly (glassMl) so với tiến độ kỳ vọng — chỉ khi đó mới nên nhắc. */
    fun isBehindSchedule(drankMl: Int, goalMl: Int, glassMl: Int, now: Calendar = Calendar.getInstance()): Boolean {
        val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        if (nowMin !in DAY_START_MIN..DAY_END_MIN) return false // ngoài giờ thức thì không nhắc
        if (drankMl >= goalMl) return false
        return expectedMl(goalMl, now) - drankMl >= glassMl
    }
}
