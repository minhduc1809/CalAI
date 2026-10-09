package com.calai.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.calai.app.presentation.theme.AppElevation
import com.calai.app.presentation.theme.ShadowDarkL4Ambient
import com.calai.app.presentation.theme.ShadowDarkL4Spot
import com.calai.app.presentation.theme.ShadowLightL4Ambient
import com.calai.app.presentation.theme.ShadowLightL4Spot
import com.calai.app.presentation.theme.TextInkSecondary
import com.calai.app.presentation.theme.VividOrangeSoft

/**
 * Dialog "Thêm mới/Sửa" dùng chung cho toàn app (Part 7/8 — CalAI_FINAL_Design_Code_Rules v3),
 * theo đúng mẫu tham khảo: header riêng "Thêm mới ..." trên dải nền nhạt (VividOrangeSoft),
 * nút đóng (X) ở góc phải header, nội dung form cuộn được, và hàng nút hành động (Hủy/Lưu)
 * rõ ràng ở đáy — thay cho `AlertDialog` mặc định (title/text/confirmButton rời rạc trông như
 * form Android/Compose gốc).
 *
 * @param title Tiêu đề, luôn nên bắt đầu bằng "Thêm mới ..." / "Sửa ..." theo đúng mẫu.
 * @param subtitle Dòng phụ mô tả ngắn mục đích form (VD "Tùy chỉnh khẩu phần & giá trị dinh dưỡng"), tùy chọn.
 * @param fullScreen Form có nhiều field/phần → chiếm toàn màn hình thay vì popup nhỏ giữa màn hình
 *   (form ngắn 1-2 field thì để false, giữ dạng popup gọn).
 * @param onDismiss Gọi khi bấm nút đóng (X) hoặc bấm ra ngoài dialog.
 * @param actions Hàng nút Hủy/Lưu đặt cuối dialog (dùng `AppButton` cho nút Lưu, `OutlinedButton` cho Hủy).
 * @param content Nội dung form, đã tự cuộn được nếu dài.
 */
@Composable
fun AppFormDialog(
    title: String,
    subtitle: String? = null,
    fullScreen: Boolean = false,
    onDismiss: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = if (fullScreen) {
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
            } else {
                val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
                val dialogAmbient = if (isDarkTheme) ShadowDarkL4Ambient else ShadowLightL4Ambient
                val dialogSpot    = if (isDarkTheme) ShadowDarkL4Spot    else ShadowLightL4Spot
                val shape = RoundedCornerShape(24.dp)
                // Top-edge highlight brush — subtle white shimmer on upper/left edges
                val highlightBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDarkTheme) 0.10f else 0.80f),
                        Color.White.copy(alpha = 0.00f)
                    )
                )
                Modifier
                    .fillMaxWidth(0.94f)
                    .then(if (isExpanded) Modifier.fillMaxHeight(0.88f) else Modifier)
                    .shadow(
                        elevation = AppElevation.L4,
                        shape = shape,
                        ambientColor = dialogAmbient,
                        spotColor = dialogSpot
                    )
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        brush = highlightBrush,
                        shape = shape
                    )
            }
        ) {
            if (fullScreen) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            }
            // Header — dải nền nhạt, tiêu đề "Thêm mới ...", nút thu/phóng (chỉ khi không full màn) + đóng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VividOrangeSoft)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextInkSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                if (!fullScreen) {
                    IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.OpenInFull,
                            contentDescription = if (isExpanded) "Thu gọn" else "Phóng to",
                            tint = TextInkSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = TextInkSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Nội dung form — cuộn được, tự giãn khi phóng to / full màn
            Column(
                modifier = Modifier
                    .weight(1f, fill = isExpanded || fullScreen)
                    .verticalScroll(rememberScrollState())
                    .animateContentSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Hàng nút hành động — Hủy (trái) / Lưu (phải), sạch sẽ đúng mẫu tham khảo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .then(if (fullScreen) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}
