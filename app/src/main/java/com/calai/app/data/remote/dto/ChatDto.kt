package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatAiRequest(
    @SerializedName("message")
    val message: String
)

data class ChatQuotaInfoDto(
    @SerializedName("hasQuota")
    val hasQuota: Boolean = true,
    @SerializedName("currentTier")
    val currentTier: String = "FREE", // FREE | PREMIUM
    @SerializedName("tierName")
    val tierName: String = "Gói Miễn phí",
    @SerializedName("remainingPercent")
    val remainingPercent: Int = 100,
    @SerializedName("status")
    val status: String = "COMFORTABLE", // COMFORTABLE, GOOD, LOW, EXHAUSTED
    @SerializedName("statusMessage")
    val statusMessage: String = "Hạn mức trò chuyện dồi dào",
    @SerializedName("resetsAt")
    val resetsAt: String? = null,
    // Free đếm theo tin nhắn (MESSAGES), Premium đếm theo token (TOKENS)
    @SerializedName("unit")
    val unit: String = "MESSAGES",
    @SerializedName("limit")
    val limit: Int = 0,
    @SerializedName("used")
    val used: Int = 0
)

data class ChatAiResponseDto(
    @SerializedName("reply")
    val reply: String = "",
    @SerializedName("quota")
    val quota: ChatQuotaInfoDto? = null,
    @SerializedName("isFallback")
    val isFallback: Boolean = false
)

data class ChatMessageDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("role")
    val role: String, // "user" hoặc "assistant"
    @SerializedName("content")
    val content: String,
    @SerializedName("createdAt")
    val createdAt: String
)

data class ChatHistoryResponseDto(
    @SerializedName("messages")
    val messages: List<ChatMessageDto> = emptyList(),
    @SerializedName("totalMessages")
    val totalMessages: Int = 0,
    @SerializedName("quota")
    val quota: ChatQuotaInfoDto? = null
)

data class AiQuotaDto(
    @SerializedName("feature")
    val feature: String = "food_recognition",
    @SerializedName("dailyFreeLimit")
    val dailyFreeLimit: Int = 5,
    @SerializedName("freeUsedToday")
    val freeUsedToday: Int = 0,
    @SerializedName("freeRemaining")
    val freeRemaining: Int = 5,
    @SerializedName("purchasedCredits")
    val purchasedCredits: Int = 0,
    @SerializedName("totalRemaining")
    val totalRemaining: Int = 5,
    @SerializedName("resetsAt")
    val resetsAt: String? = null
)
