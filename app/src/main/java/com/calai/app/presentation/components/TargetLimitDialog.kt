package com.calai.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Tiêu đề + lời giải thích cho từng quy tắc an toàn đã giới hạn calo mục tiêu (BR-03.3). */
private fun limitTitle(limit: String) = when (limit) {
    "FLOOR" -> "Bạn đã chạm sàn calo an toàn"
    "DEFICIT_CAP" -> "Mức giảm calo đã được giới hạn"
    "SURPLUS_CAP" -> "Mức tăng calo đã được giới hạn"
    else -> "Mục tiêu đã được giới hạn"
}

private fun limitBody(limit: String, target: Int?) = when (limit) {
    "FLOOR" -> "Với tốc độ giảm cân bạn chọn, lượng calo tính ra thấp hơn mức tối thiểu an toàn cho cơ thể bạn " +
        "(tối thiểu theo giới tính và không thấp hơn năng lượng nghỉ ngơi BMR). Hệ thống giữ mục tiêu ở " +
        (target?.let { "$it kcal/ngày" } ?: "mức tối thiểu đó") +
        ", nên cân nặng sẽ giảm chậm hơn mong muốn. Ăn quá ít kéo dài dễ gây mệt, mất cơ và rối loạn nội tiết."
    "DEFICIT_CAP" -> "Mức thâm hụt bạn chọn vượt quá giới hạn an toàn so với năng lượng bạn tiêu hao mỗi ngày. " +
        "Hệ thống đã hạ mức thâm hụt xuống ngưỡng cho phép nên cân nặng sẽ giảm chậm hơn mong muốn."
    "SURPLUS_CAP" -> "Mức thặng dư bạn chọn vượt quá giới hạn an toàn so với năng lượng bạn tiêu hao mỗi ngày. " +
        "Hệ thống đã hạ mức thặng dư xuống ngưỡng cho phép nên cân nặng sẽ tăng chậm hơn mong muốn."
    else -> "Mục tiêu đã được giới hạn để đảm bảo an toàn cho bạn."
}

/**
 * Hộp thoại giải thích khi calo mục tiêu bị một quy tắc an toàn giới hạn ([limit] = FLOOR / DEFICIT_CAP / SURPLUS_CAP,
 * lấy từ `targetLimitedBy` của hồ sơ). Hiện sau khi lưu mục tiêu và khi người dùng bấm "Vì sao?" ở thẻ mục tiêu.
 */
@Composable
fun TargetLimitDialog(limit: String, targetCalories: Int?, onDismiss: () -> Unit) {
    NwAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(limitTitle(limit), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(limitBody(limit, targetCalories), fontSize = 14.sp, lineHeight = 21.sp)
                Text(
                    "Bạn có thể chọn tốc độ chậm hơn hoặc điều chỉnh mục tiêu cân nặng để có kế hoạch phù hợp hơn.",
                    fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            AppButton(text = "Đã hiểu", onClick = onDismiss, modifier = Modifier.width(120.dp), height = 40.dp)
        }
    )
}
