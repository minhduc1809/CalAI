package com.calai.app.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calai.app.R
import com.calai.app.presentation.theme.NwTheme
import com.calai.app.presentation.theme.ObsidianBackground
import com.calai.app.presentation.theme.PeachDarkTint

/** Nền các màn trước khi vào Home (Chào mừng, Đăng nhập, Quên mật khẩu): luôn là tranh pastel sáng, không theo Dark Mode. */
@Composable
fun AuthBackdrop() {
    Image(
        painter = painterResource(id = R.drawable.bg_auth),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

/** Logo thương hiệu trên nền sáng. */
@Composable
fun AuthLogo(width: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logo_nutriwise),
        contentDescription = "NutriWise",
        contentScale = ContentScale.Fit,
        modifier = modifier.width(width)
    )
}
