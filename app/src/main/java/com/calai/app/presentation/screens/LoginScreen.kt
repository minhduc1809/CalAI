package com.calai.app.presentation.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.R
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (isNewRegistration: Boolean) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess(uiState.needsOnboarding)
        }
    }

    fun launchGoogleSignIn() {
        coroutineScope.launch {
            try {
                val webClientId = context.getString(R.string.google_web_client_id)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    viewModel.onGoogleSignInError("Không thể lấy thông tin đăng nhập từ Google")
                }
            } catch (e: GetCredentialException) {
                Log.w("LoginScreen", "Google Sign-In bị hủy hoặc lỗi", e)
                viewModel.onGoogleSignInError("Đăng nhập Google thất bại hoặc đã bị hủy")
            } catch (e: Exception) {
                Log.e("LoginScreen", "Lỗi không xác định khi đăng nhập Google", e)
                viewModel.onGoogleSignInError(e.message ?: "Đăng nhập Google thất bại")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Header
            Text(
                text = "NutriWise",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Trợ lý dinh dưỡng cá nhân thông minh",
                fontSize = 14.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Segmented Tab Toggle: Đăng Nhập / Đăng Ký
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (uiState.isLoginMode) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        .clickable { if (!uiState.isLoginMode) viewModel.toggleAuthMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đăng Nhập",
                        fontWeight = if (uiState.isLoginMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (uiState.isLoginMode) TextWhite else TextMuted,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (!uiState.isLoginMode) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        .clickable { if (uiState.isLoginMode) viewModel.toggleAuthMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đăng Ký",
                        fontWeight = if (!uiState.isLoginMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (!uiState.isLoginMode) TextWhite else TextMuted,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Báo lỗi rõ ràng
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { errorMsg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = CrimsonError.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonError)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMsg,
                                color = TextWhite,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Tên đăng nhập
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = { Text("Tên đăng nhập", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nếu đang ở tab Đăng ký: thêm Email và Tên hiển thị
            if (!uiState.isLoginMode) {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Email (không bắt buộc)", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = VividOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Họ và tên", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = VividOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Mật khẩu
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Mật khẩu", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Nút bấm CTA chính: Màu Cam Vivid
            Button(
                onClick = { viewModel.submit() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VividOrange,
                    contentColor = TextWhite
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = TextWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.isLoginMode) "Đăng Nhập" else "Tạo Tài Khoản",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider "hoặc"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "hoặc",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Đăng nhập với Google
            OutlinedButton(
                onClick = { launchGoogleSignIn() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(TextWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        color = MaterialTheme.colorScheme.background,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tiếp tục với Google",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
