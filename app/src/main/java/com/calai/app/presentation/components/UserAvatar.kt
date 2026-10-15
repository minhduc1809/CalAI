package com.calai.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.calai.app.data.remote.CalAIApi

/** Đường dẫn tương đối của máy chủ (/uploads/...) thành địa chỉ đầy đủ; host sẽ được dò tự động khi tải ảnh. */
fun avatarUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    val root = CalAIApi.BASE_URL.substringBefore("/api/")
    return root.trimEnd('/') + "/" + path.trimStart('/')
}

/** Ảnh đại diện tròn; chưa có ảnh (hoặc tải lỗi) thì hiện chữ cái đầu của tên. */
@Composable
fun UserAvatar(
    avatar: String?,
    displayName: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    fontSize: Int = 24,
) {
    val initial = (displayName?.trim()?.firstOrNull()?.toString() ?: "N").uppercase()
    val url = avatarUrl(avatar)
    Box(modifier = modifier.size(size).clip(CircleShape), contentAlignment = Alignment.Center) {
        val fallback: @Composable () -> Unit = {
            Text(text = initial, fontSize = fontSize.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
        }
        if (url == null) fallback() else SubcomposeAsyncImage(
            model = url,
            contentDescription = "Ảnh đại diện",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { fallback() },
            error = { fallback() },
        )
    }
}
