package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String, // MEAL_REMINDER | WATER_REMINDER | GOAL_ACHIEVED | GOAL_EXCEEDED | AI_MESSAGE
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("isRead")
    val isRead: Boolean,
    @SerializedName("createdAt")
    val createdAt: String
)

data class NotificationListDto(
    @SerializedName("items")
    val items: List<NotificationDto>,
    @SerializedName("unreadCount")
    val unreadCount: Int
)
