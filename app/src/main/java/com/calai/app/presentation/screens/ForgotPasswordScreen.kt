package com.calai.app.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.calai.app.R
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.components.AppTextField

/**
 * Màn "Quên mật khẩu?" — LƯU Ý QUAN TRỌNG: backend hiện KHÔNG có endpoint reset mật khẩu
 * (đã kiểm tra các file auth trong CaIAI-Back — không có "forgot"/"reset-password"). Vì vậy nút
 * "Gửi yêu cầu" KHÔNG được giả vờ gửi email/thành công — chỉ hiển thị thông báo trung thực
 * rằng tính năng đang được phát triển, đúng nguyên tắc "không nói dối người dùng" đã áp dụng
 * cho các fix trước đó trong phiên này (VD "Xóa bộ nhớ đệm", fallback Gemini).
 */
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean = true
) {
    var emailOrUsername by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Nền trang trí pastel dùng chung với các màn Auth (Welcome/Login/Register)
        Image(
            painter = painterResource(id = R.drawable.bg_auth),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_nutriwise),
                contentDescription = "NutriWise",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(160.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Trợ lý dinh dưỡng cá nhân thông minh",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.fox_register),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .heightIn(max = 190.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Thẻ nổi (floating card) — LUÔN sáng bất kể Dark/Light Mode, giống Login/Register
            // (bọc CalAILightColorScheme để mọi MaterialTheme.colorScheme.* bên trong luôn ra token sáng).
            MaterialTheme(colorScheme = com.calai.app.presentation.theme.CalAILightColorScheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(30.dp),
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    )
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Nhập email hoặc tên đăng nhập để nhận hướng dẫn đặt lại mật khẩu",
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppTextField(
                    label = "Email hoặc tên đăng nhập",
                    value = emailOrUsername,
                    onValueChange = { emailOrUsername = it },
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Không có API reset mật khẩu ở backend — KHÔNG giả vờ gửi email thành công.
                // Chỉ hiển thị thông báo trung thực cho người dùng.
                AppButton(
                    text = "Gửi yêu cầu",
                    onClick = {
                        Toast.makeText(
                            context,
                            "Tính năng đặt lại mật khẩu qua email đang được phát triển, vui lòng liên hệ hỗ trợ hoặc đăng nhập lại bằng thông tin đã nhớ.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text("Quay lại đăng nhập", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
            }
        }
    }
}
