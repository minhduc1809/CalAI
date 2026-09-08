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
    val currentTier: String = "FREE", // FREE, PLUS, PRO, MAX
    @SerializedName("tierName")
    val tierName: String = "Bản Miễn Phí",
    @SerializedName("remainingPercent")
    val remainingPercent: Int = 100,
    @SerializedName("status")
    val status: String = "COMFORTABLE", // COMFORTABLE, GOOD, LOW, EXHAUSTED
    @SerializedName("statusMessage")
    val statusMessage: String = "Hạn mức trò chuyện dồi dào",
    @SerializedName("resetsAt")
    val resetsAt: String? = null
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

data class ChatPlanDto(
    @SerializedName("id")
    val id: String, // PLUS, PRO, MAX
    @SerializedName("name")
    val name: String,
    @SerializedName("priceVnd")
    val priceVnd: Long,
    @SerializedName("description")
    val description: String,
    @SerializedName("isPopular")
    val isPopular: Boolean = false,
    @SerializedName("bestValue")
    val bestValue: Boolean = false
)

data class PurchaseChatPlanRequest(
    @SerializedName("packageId")
    val packageId: String // PLUS, PRO, MAX
)
