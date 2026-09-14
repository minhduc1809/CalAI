package com.calai.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.calai.app.presentation.theme.VividOrangeSoft

/**
 * Dialog "Thêm mới/Sửa" dùng chung cho toàn app (Part 7/8 — CalAI_FINAL_Design_Code_Rules v3),
 * theo đúng mẫu tham khảo: header riêng "Thêm mới ..." trên dải nền nhạt (VividOrangeSoft),
 * nút thu gọn/phóng to (đổi chiều cao dialog) + nút đóng (X) ở góc phải header, nội dung form
 * cuộn được, và hàng nút hành động (Hủy/Lưu) rõ ràng ở đáy — thay cho `AlertDialog` mặc định
 * (title/text/confirmButton rời rạc trông như form Android/Compose gốc).
 *
 * @param title Tiêu đề, luôn nên bắt đầu bằng "Thêm mới ..." / "Sửa ..." theo đúng mẫu.
 * @param onDismiss Gọi khi bấm nút đóng (X) hoặc bấm ra ngoài dialog.
 * @param actions Hàng nút Hủy/Lưu đặt cuối dialog (dùng `AppButton` cho nút Lưu, `OutlinedButton` cho Hủy).
 * @param content Nội dung form, đã tự cuộn được nếu dài.
 */
@Composable
fun AppFormDialog(
    title: String,
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
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .then(if (isExpanded) Modifier.fillMaxHeight(0.88f) else Modifier)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header — dải nền nhạt, tiêu đề "Thêm mới ...", nút thu/phóng + đóng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VividOrangeSoft)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.OpenInFull,
                        contentDescription = if (isExpanded) "Thu gọn" else "Phóng to",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Nội dung form — cuộn được, tự giãn khi phóng to
            Column(
                modifier = Modifier
                    .weight(1f, fill = isExpanded)
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
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}
