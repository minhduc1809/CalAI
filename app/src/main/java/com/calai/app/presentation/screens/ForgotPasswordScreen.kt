package com.calai.app.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.R
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.components.AppTextField
import com.calai.app.presentation.theme.VividOrange
import com.calai.app.presentation.viewmodel.ForgotPasswordStep
import com.calai.app.presentation.viewmodel.ForgotPasswordViewModel

/**
 * Màn "Quên mật khẩu?" — luồng đặt lại mật khẩu thật qua mã OTP gửi email
 * (POST auth/forgot-password + auth/reset-password ở backend, thêm cùng đợt với màn này).
 * Bước 1: nhập email, gửi mã OTP. Bước 2: nhập mã 6 số + mật khẩu mới.
 */
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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

            Spacer(modifier = Modifier.height(24.dp))

            // Thẻ nổi (floating card) — LUÔN sáng bất kể Dark/Light Mode, giống Login/Register
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
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isResetDone) {
                        ResetDoneContent(onBack = onBack)
                    } else when (uiState.step) {
                        ForgotPasswordStep.ENTER_EMAIL -> EnterEmailContent(
                            email = uiState.email,
                            isLoading = uiState.isLoading,
                            onEmailChange = viewModel::onEmailChange,
                            onSubmit = viewModel::sendResetCode,
                            onBack = onBack
                        )
                        ForgotPasswordStep.ENTER_CODE -> EnterCodeContent(
                            email = uiState.email,
                            code = uiState.code,
                            newPassword = uiState.newPassword,
                            confirmPassword = uiState.confirmPassword,
                            isLoading = uiState.isLoading,
                            infoMessage = uiState.infoMessage,
                            onCodeChange = viewModel::onCodeChange,
                            onNewPasswordChange = viewModel::onNewPasswordChange,
                            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                            onSubmit = viewModel::resetPassword,
                            onResend = viewModel::resendCode,
                            onChangeEmail = viewModel::backToEmailStep
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnterEmailContent(
    email: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Text(
        text = "Quên mật khẩu?",
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Nhập email đã đăng ký, chúng tôi sẽ gửi mã đặt lại mật khẩu tới email đó",
        fontSize = 13.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    Spacer(modifier = Modifier.height(24.dp))

    AppTextField(
        label = "Email",
        value = email,
        onValueChange = onEmailChange,
        placeholder = "you@example.com",
        keyboardType = KeyboardType.Email,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    AppButton(
        text = "Gửi mã xác thực",
        onClick = onSubmit,
        enabled = !isLoading,
        isLoading = isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = onBack,
        enabled = !isLoading,
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

@Composable
private fun EnterCodeContent(
    email: String,
    code: String,
    newPassword: String,
    confirmPassword: String,
    isLoading: Boolean,
    infoMessage: String?,
    onCodeChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit,
    onChangeEmail: () -> Unit
) {
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Text(
        text = "Nhập mã xác thực",
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = infoMessage ?: "Nhập mã 6 số đã gửi tới $email và đặt mật khẩu mới",
        fontSize = 13.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    Spacer(modifier = Modifier.height(20.dp))

    AppTextField(
        label = "Mã xác thực (6 số)",
        value = code,
        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) onCodeChange(it) },
        placeholder = "123456",
        keyboardType = KeyboardType.NumberPassword,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        label = { Text("Mật khẩu mới", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingIcon = {
            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                Icon(
                    imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = VividOrange,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Xác nhận mật khẩu mới", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingIcon = {
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                Icon(
                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = VividOrange,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    )

    Spacer(modifier = Modifier.height(24.dp))

    AppButton(
        text = "Đặt lại mật khẩu",
        onClick = onSubmit,
        enabled = !isLoading,
        isLoading = isLoading
    )

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(onClick = onResend, enabled = !isLoading) {
        Text("Gửi lại mã", color = VividOrange, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }

    TextButton(onClick = onChangeEmail, enabled = !isLoading) {
        Text("Đổi email khác", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun ResetDoneContent(onBack: () -> Unit) {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = VividOrange,
        modifier = Modifier.size(56.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Đặt lại mật khẩu thành công",
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Vui lòng đăng nhập lại bằng mật khẩu mới",
        fontSize = 13.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(24.dp))
    AppButton(text = "Về trang đăng nhập", onClick = onBack)
}
