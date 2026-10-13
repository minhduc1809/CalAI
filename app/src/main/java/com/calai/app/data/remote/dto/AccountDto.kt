package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Chính sách quyền riêng tư (BR-18): tách theo mục để hiển thị. */
data class PrivacyPolicyDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("version") val version: String = "",
    @SerializedName("lastUpdated") val lastUpdated: String = "",
    @SerializedName("sections") val sections: List<PolicySectionDto> = emptyList()
)

data class PolicySectionDto(
    @SerializedName("heading") val heading: String = "",
    @SerializedName("content") val content: String = ""
)

data class ReauthRequest(@SerializedName("password") val password: String)

data class ReauthResponse(@SerializedName("reauthToken") val reauthToken: String)

/** Một thiết bị đang đăng nhập (BR-01.3). */
data class SessionDto(
    @SerializedName("id") val id: String,
    @SerializedName("deviceName") val deviceName: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("lastUsedAt") val lastUsedAt: String? = null
)
