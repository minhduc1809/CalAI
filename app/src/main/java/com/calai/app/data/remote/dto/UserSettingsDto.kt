package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Cài đặt cá nhân đồng bộ trên server (BR-19.4). */
data class UserSettingsDto(
    @SerializedName("weightUnit") val weightUnit: String = "kg",
    @SerializedName("heightUnit") val heightUnit: String = "cm",
    @SerializedName("energyUnit") val energyUnit: String = "kcal",
    @SerializedName("timezone") val timezone: String? = null,
    @SerializedName("checkinDay") val checkinDay: Int? = null,
    /** "HH:mm" hoặc null khi tắt. */
    @SerializedName("dailyCompletionReminder") val dailyCompletionReminder: String? = null,
    @SerializedName("defaultRestSec") val defaultRestSec: Int = 90,
    /** Chỉ chứa loại thông báo đã tắt (giá trị false). */
    @SerializedName("notificationPrefs") val notificationPrefs: Map<String, Boolean> = emptyMap(),
    @SerializedName("initialized") val initialized: Boolean = false
)

/** Cập nhật cài đặt; trường null không được gửi. "OFF" ở giờ nhắc nghĩa là tắt nhắc. */
data class UpdateSettingsRequest(
    @SerializedName("weightUnit") val weightUnit: String? = null,
    @SerializedName("heightUnit") val heightUnit: String? = null,
    @SerializedName("energyUnit") val energyUnit: String? = null,
    @SerializedName("dailyCompletionReminder") val dailyCompletionReminder: String? = null,
    @SerializedName("defaultRestSec") val defaultRestSec: Int? = null,
    @SerializedName("notificationPrefs") val notificationPrefs: Map<String, Boolean>? = null,
    @SerializedName("initialized") val initialized: Boolean? = null
)
