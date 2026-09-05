package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatAiRequest(
    @SerializedName("message")
    val message: String
)

data class ChatAiResponseDto(
    @SerializedName("reply")
    val reply: String = "",
    @SerializedName("isFallback")
    val isFallback: Boolean = false
)
