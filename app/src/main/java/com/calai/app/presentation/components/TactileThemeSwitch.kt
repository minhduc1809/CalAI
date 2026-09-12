package com.calai.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calai.app.presentation.theme.*

/**
 * Nút chuyển đổi giao diện Sáng / Tối dạng khối xúc giác nổi 3D (Part 4.5).
 *
 * Đây là wrapper mỏng quanh component dùng chung [AppToggle] — chỉ thêm phần
 * riêng của theme-switch (kích thước lớn hơn toggle thường + icon mặt trời/mặt
 * trăng bên trong thumb), tránh trùng lặp logic vẽ track/thumb/shadow/glow.
 */
@Composable
fun TactileThemeSwitch(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AppToggle(
        checked = isDarkTheme,
        onCheckedChange = onThemeChanged,
        modifier = modifier,
        width = 66.dp,
        height = 36.dp,
        thumbIcon = { checked ->
            if (checked) {
                DuotoneMoonIcon(
                    size = 15.dp,
                    outlineColor = MaterialTheme.colorScheme.onBackground,
                    accentColor = VividOrangeLight
                )
            } else {
                DuotoneSunIcon(
                    size = 15.dp,
                    outlineColor = TextInkPrimary,
                    accentColor = VividOrange
                )
            }
        }
    )
}
