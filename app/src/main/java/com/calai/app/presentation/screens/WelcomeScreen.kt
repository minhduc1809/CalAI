package com.calai.app.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.R
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.theme.CalAILightColorScheme
import com.calai.app.presentation.theme.TextInkPrimary
import com.calai.app.presentation.theme.TextInkSecondary

/**
 * Màn Welcome — màn ĐẦU TIÊN khi mở app (trước cả Đăng Nhập/Đăng Ký), giới thiệu
 * thương hiệu + linh vật cáo trước khi vào luồng xác thực. Dùng chung nền pastel
 * (bg_auth) với Login/Register/Forgot Password. Luôn hiển thị sáng cố định, không
 * theo Dark/Light Mode của app (giống các màn Auth khác).
 */
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    MaterialTheme(colorScheme = CalAILightColorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg_auth),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Logo thương hiệu (đã tách nền trong suốt)
                Image(
                    painter = painterResource(id = R.drawable.logo_nutriwise),
                    contentDescription = "NutriWise",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(220.dp)
                )

                // Ảnh minh hoạ hero (linh vật cáo)
                Image(
                    painter = painterResource(id = R.drawable.fox_login),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .heightIn(max = 340.dp)
                        .weight(1f, fill = false)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // CTA chính — gradient cam/đỏ ấm, tactile (AppButton dùng chung)
                    AppButton(
                        text = "Bắt đầu ngay  →",
                        onClick = onGetStarted,
                        height = 58.dp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Page indicator — màn Welcome là bước đầu của luồng giới thiệu
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == 0) 9.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == 0) TextInkPrimary
                                        else TextInkSecondary.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
