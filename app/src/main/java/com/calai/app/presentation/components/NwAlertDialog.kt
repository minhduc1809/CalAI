package com.calai.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.calai.app.presentation.theme.NwTheme

/**
 * Hộp thoại dùng chung toàn app: nền trắng (sáng) / nền tối (tối), bo 28dp, tiêu đề đậm,
 * nội dung thoáng, nút xếp hàng bên phải. Thay cho AlertDialog mặc định để mọi hộp thoại đồng nhất.
 * Tham số `containerColor` được giữ để các chỗ gọi cũ biên dịch được nhưng không còn tác dụng.
 */
@Composable
fun NwAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") containerColor: Color = Color.Unspecified,
    @Suppress("UNUSED_PARAMETER") shape: androidx.compose.ui.graphics.Shape? = null,
) {
    val bg = if (NwTheme.isDark) MaterialTheme.colorScheme.surface else Color.White
    val onBg = MaterialTheme.colorScheme.onSurface
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(bg)
                .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompositionLocalProvider(LocalContentColor provides onBg) {
                if (icon != null) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { icon() }
                if (title != null) {
                    ProvideTextStyle(TextStyle(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = onBg)) {
                        Box(Modifier.padding(end = 8.dp)) { title() }
                    }
                }
                if (text != null) {
                    ProvideTextStyle(TextStyle(fontSize = 14.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        Box(Modifier.padding(end = 8.dp).verticalScroll(rememberScrollState())) { text() }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}
