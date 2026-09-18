package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HabitReminderFoodDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("servingSize")
    val servingSize: String?,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("protein")
    val protein: Float,
    @SerializedName("carb")
    val carb: Float,
    @SerializedName("fat")
    val fat: Float
)

data class HabitReminderDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String, // BREAKFAST | LUNCH | DINNER | SNACK | WATER | CUSTOM
    @SerializedName("label")
    val label: String,
    @SerializedName("enabled")
    val enabled: Boolean,
    @SerializedName("timeOfDay")
    val timeOfDay: String, // "HH:mm"
    @SerializedName("windowStart")
    val windowStart: String?,
    @SerializedName("windowEnd")
    val windowEnd: String?,
    @SerializedName("targetCalorieMin")
    val targetCalorieMin: Float?,
    @SerializedName("targetCalorieMax")
    val targetCalorieMax: Float?,
    @SerializedName("waterIntervalMinutes")
    val waterIntervalMinutes: Int?,
    @SerializedName("repeatDays")
    val repeatDays: List<Int>, // ISO weekday: 1=Thứ 2 ... 7=Chủ nhật
    @SerializedName("advanceNoticeMinutes")
    val advanceNoticeMinutes: Int,
    @SerializedName("sortOrder")
    val sortOrder: Int,
    @SerializedName("foods")
    val foods: List<HabitReminderFoodDto> = emptyList()
)

data class CreateHabitReminderRequest(
    @SerializedName("label")
    val label: String,
    @SerializedName("timeOfDay")
    val timeOfDay: String,
    @SerializedName("windowStart")
    val windowStart: String? = null,
    @SerializedName("windowEnd")
    val windowEnd: String? = null,
    @SerializedName("targetCalorieMin")
    val targetCalorieMin: Float? = null,
    @SerializedName("targetCalorieMax")
    val targetCalorieMax: Float? = null,
    @SerializedName("repeatDays")
    val repeatDays: List<Int>? = null,
    @SerializedName("advanceNoticeMinutes")
    val advanceNoticeMinutes: Int? = null,
    @SerializedName("enabled")
    val enabled: Boolean? = null
)

data class UpdateHabitReminderRequest(
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("timeOfDay")
    val timeOfDay: String? = null,
    @SerializedName("windowStart")
    val windowStart: String? = null,
    @SerializedName("windowEnd")
    val windowEnd: String? = null,
    @SerializedName("targetCalorieMin")
    val targetCalorieMin: Float? = null,
    @SerializedName("targetCalorieMax")
    val targetCalorieMax: Float? = null,
    @SerializedName("waterIntervalMinutes")
    val waterIntervalMinutes: Int? = null,
    @SerializedName("repeatDays")
    val repeatDays: List<Int>? = null,
    @SerializedName("advanceNoticeMinutes")
    val advanceNoticeMinutes: Int? = null,
    @SerializedName("enabled")
    val enabled: Boolean? = null
)

data class AttachHabitReminderFoodRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("servingSize")
    val servingSize: String? = null,
    @SerializedName("calories")
    val calories: Float,
    @SerializedName("protein")
    val protein: Float,
    @SerializedName("carb")
    val carb: Float,
    @SerializedName("fat")
    val fat: Float
)
