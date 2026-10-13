package com.calai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MenuItemDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: String? = null,
    @SerializedName("estimatedCalories")
    val estimatedCalories: Int = 0,
    @SerializedName("protein")
    val protein: Int = 0,
    @SerializedName("carbs")
    val carbs: Int = 0,
    @SerializedName("fat")
    val fat: Int = 0,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("isRecommended")
    val isRecommended: Boolean = false,
    @SerializedName("recommendationReason")
    val recommendationReason: String? = null,
    /** Cảnh báo đỏ khi món có thể chứa chất dị ứng hoặc không hợp chế độ ăn của người dùng (BR-11.3). */
    @SerializedName("warning")
    val warning: String? = null,
    /** AI_MATCHED: số liệu từ kho món. AI_ESTIMATE: ước tính, phải hiện nhãn "Ước tính". */
    @SerializedName("sourceType")
    val sourceType: String? = null,
    @SerializedName("sourceId")
    val sourceId: String? = null,
    @SerializedName("isEstimate")
    val isEstimate: Boolean = false,
    /** Khẩu phần mà số liệu dinh dưỡng áp dụng (khẩu phần chuẩn của kho, có thể khác quán). */
    @SerializedName("servingSize")
    val servingSize: String? = null,
    /** false khi chưa có số liệu cho món này (các trường calo/macro là 0, không được hiển thị hay lưu). */
    @SerializedName("nutritionAvailable")
    val nutritionAvailable: Boolean? = null
)

data class ScanMenuResponseDto(
    @SerializedName("restaurantName")
    val restaurantName: String? = null,
    @SerializedName("items")
    val items: List<MenuItemDto> = emptyList(),
    @SerializedName("recommendedItems")
    val recommendedItems: List<MenuItemDto> = emptyList(),
    @SerializedName("summaryAdvice")
    val summaryAdvice: String = "",
    /** Cảnh báo chung luôn hiển thị: thành phần thực tế tuỳ cách chế biến của quán. */
    @SerializedName("disclaimer")
    val disclaimer: String? = null
)

data class ScanMenuBase64Request(
    @SerializedName("imageBase64")
    val imageBase64: String,
    @SerializedName("note")
    val note: String? = null
)
