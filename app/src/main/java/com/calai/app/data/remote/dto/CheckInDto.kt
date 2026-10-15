package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Check-in tuần (BR-06) ──────────────────────────────────────────────────

/**
 * Một Check-in. Trạng thái (status):
 * PENDING (chờ quyết định) · SNOOZED (để sau, tự quay lại sau 24 giờ) · ACCEPTED · DECLINED ·
 * EXPIRED (sang kỳ mới hoặc mục tiêu đã đổi) · INSUFFICIENT_DATA (thiếu dữ liệu, chỉ có "Đã hiểu") · ACKNOWLEDGED.
 * Lý do điều chỉnh (adjustmentReason): EXPENDITURE_CHANGE · GOAL_CHANGE · SMOOTHING · NO_CHANGE.
 *
 * Gson bỏ qua giá trị mặc định của Kotlin nên các trường có thể thiếu đều khai báo nullable.
 */
data class CheckInDto(
    @SerializedName("id") val id: String,
    @SerializedName("weekNumber") val weekNumber: Int? = null,
    @SerializedName("weekStartDate") val weekStartDate: String? = null,
    @SerializedName("weekEndDate") val weekEndDate: String? = null,
    @SerializedName("status") val status: String,

    // Tuần vừa qua
    @SerializedName("avgDailyCalories") val avgDailyCalories: Double? = null,
    @SerializedName("avgDailyProtein") val avgDailyProtein: Double? = null,
    @SerializedName("avgDailyCarb") val avgDailyCarb: Double? = null,
    @SerializedName("avgDailyFat") val avgDailyFat: Double? = null,
    @SerializedName("weightAtCheckin") val weightAtCheckin: Double? = null,
    @SerializedName("compliancePct") val compliancePct: Double? = null,

    // Mục tiêu hiện tại và đề xuất
    @SerializedName("currentCalorieTarget") val currentCalorieTarget: Int = 0,
    @SerializedName("proposedCalorieTarget") val proposedCalorieTarget: Int = 0,
    @SerializedName("currentProteinTarget") val currentProteinTarget: Double? = null,
    @SerializedName("proposedProteinTarget") val proposedProteinTarget: Double? = null,
    @SerializedName("currentCarbTarget") val currentCarbTarget: Double? = null,
    @SerializedName("proposedCarbTarget") val proposedCarbTarget: Double? = null,
    @SerializedName("currentFatTarget") val currentFatTarget: Double? = null,
    @SerializedName("proposedFatTarget") val proposedFatTarget: Double? = null,

    @SerializedName("newExpenditure") val newExpenditure: Double? = null,
    @SerializedName("adjustmentReason") val adjustmentReason: String? = null,
    @SerializedName("goalProgressPct") val goalProgressPct: Double? = null,

    @SerializedName("mood") val mood: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("processedAt") val processedAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

/** Nội dung giải thích do backend sinh theo lý do điều chỉnh, giọng không phán xét. */
data class CheckInCoachingDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("complianceMessage") val complianceMessage: String? = null,
    @SerializedName("callToAction") val callToAction: String? = null,
    /** Việc cần làm cụ thể khi chưa đủ dữ liệu để đề xuất (chỉ có với INSUFFICIENT_DATA). */
    @SerializedName("todo") val todo: List<String>? = null
)

/** Dữ liệu của GET /checkins/pending, POST /checkins/generate và GET /checkins/{id}. */
data class CheckInWithCoachingDto(
    @SerializedName("checkIn") val checkIn: CheckInDto? = null,
    @SerializedName("coachingContent") val coachingContent: CheckInCoachingDto? = null,
    /** Ngày Check-in kế tiếp (YYYY-MM-DD) và ngày đã chọn, để hiện "Check-in tiếp theo". */
    @SerializedName("nextCheckinDate") val nextCheckinDate: String? = null,
    @SerializedName("checkinDay") val checkinDay: Int? = null,
    /** true khi tài khoản chưa có đủ một tuần trọn vẹn nên chưa có Check-in đầu tiên. */
    @SerializedName("tooEarly") val tooEarly: Boolean? = null
)

data class GenerateCheckinRequest(
    @SerializedName("mood") val mood: String? = null,
    @SerializedName("note") val note: String? = null
)

/** action: ACCEPT | DECLINE | SNOOZE | ACKNOWLEDGE. */
data class RespondCheckinRequest(
    @SerializedName("action") val action: String,
    @SerializedName("mood") val mood: String? = null,
    @SerializedName("note") val note: String? = null
)
