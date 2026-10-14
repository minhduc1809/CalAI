package com.calai.app.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateTab: (DockTab) -> Unit,
    onOpenPremium: () -> Unit = {},
    onLogout: () -> Unit,
    isDarkTheme: Boolean = true,
    themeMode: String = "LIGHT",
    onThemeModeChange: (String) -> Unit = {},
    onOpenGoalSetup: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    var showChangePasswordSheet by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showEditHeightSheet by remember { mutableStateOf(false) }
    var showEditWeightSheet by remember { mutableStateOf(false) }
    var showAvatarSheet by remember { mutableStateOf(false) }
    var showEditInfo by remember { mutableStateOf(false) }
    val avatarPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) viewModel.uploadAvatar(context, uri) { _, message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (showAvatarSheet) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAvatarSheet = false },
            title = { Text("Ảnh đại diện", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    androidx.compose.material3.TextButton(onClick = { showAvatarSheet = false; avatarPicker.launch("image/*") }) {
                        Text("Chọn ảnh từ thư viện", color = MaterialTheme.colorScheme.onBackground)
                    }
                    if (profile?.avatar != null) {
                        androidx.compose.material3.TextButton(onClick = {
                            showAvatarSheet = false
                            viewModel.removeAvatar { _, message -> android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show() }
                        }) { Text("Xoá ảnh hiện tại", color = MaterialTheme.colorScheme.error) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showAvatarSheet = false }) { Text("Đóng", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showEditInfo) {
        EditBasicInfoDialog(
            initialName = profile?.name.orEmpty(),
            initialGender = profile?.gender,
            initialDob = profile?.dateOfBirth?.take(10),
            saving = uiState.isUpdatingBiometrics,
            onDismiss = { showEditInfo = false },
            onSave = { name, gender, dob ->
                viewModel.saveBasicInfo(name, gender, dob) { ok, message ->
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                    if (ok) showEditInfo = false
                }
            }
        )
    }

    // BR-04: đổi hồ sơ không tự đổi mục tiêu; hỏi người dùng có muốn áp dụng mục tiêu đề xuất không
    uiState.proposedTarget?.let { proposed ->
        val diff = proposed.diff.calories.toInt()
        val diffText = when {
            diff > 0 -> "tăng $diff kcal so với hiện tại"
            diff < 0 -> "giảm ${-diff} kcal so với hiện tại"
            else -> "giữ nguyên calo, chỉ đổi macro"
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissProposedTarget() },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Cập nhật mục tiêu?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            },
            text = {
                Text(
                    "Hồ sơ của bạn đã thay đổi. Mục tiêu đề xuất mới là ${proposed.calories.toInt()} kcal/ngày " +
                        "(P ${proposed.protein?.toInt() ?: 0}g · C ${proposed.carb?.toInt() ?: 0}g · F ${proposed.fat?.toInt() ?: 0}g), $diffText. " +
                        "Bạn có muốn áp dụng không? Nếu để sau, mục tiêu hiện tại được giữ nguyên.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                AppButton(
                    text = "Áp dụng",
                    onClick = {
                        viewModel.applyProposedTarget(
                            onSuccess = {
                                Toast.makeText(context, "Đã cập nhật mục tiêu mới!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier.width(120.dp),
                    isLoading = uiState.isApplyingTarget,
                    height = 40.dp,
                    shape = RoundedCornerShape(10.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissProposedTarget() }) {
                    Text("Để sau", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showEditHeightSheet) {
        val currentHeight = (profile?.heightCm?.toInt() ?: 170).coerceIn(100, 230)
        EditBiometricModalSheet(
            title = "Chỉnh sửa Chiều cao",
            subtitle = "Hệ thống sẽ tính lại BMI, BMR và TDEE. Nếu mục tiêu calo cần đổi, bạn sẽ được hỏi trước khi áp dụng",
            initialValue = currentHeight,
            range = 100..230,
            unit = "cm",
            isLoading = uiState.isUpdatingBiometrics,
            isDarkTheme = isDarkTheme,
            onDismiss = { showEditHeightSheet = false },
            onSave = { newHeight ->
                viewModel.updateHeight(
                    heightCm = newHeight.toFloat(),
                    onSuccess = {
                        showEditHeightSheet = false
                        Toast.makeText(context, "Cập nhật chiều cao thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showEditWeightSheet) {
        val isLb = uiState.weightUnit == "lb"
        val rawWeight = profile?.weightKg ?: 65f
        val currentWeight = if (isLb) {
            UserPreferencesManager.convertKg(rawWeight, "lb").toInt().coerceIn(66, 440)
        } else {
            rawWeight.toInt().coerceIn(30, 200)
        }
        val range = if (isLb) 66..440 else 30..200
        val unitLabel = if (isLb) "lbs" else "kg"

        EditBiometricModalSheet(
            title = "Chỉnh sửa Cân nặng",
            subtitle = "Hệ thống sẽ cập nhật nhật ký cân nặng. Nếu mục tiêu calo cần đổi, bạn sẽ được hỏi trước khi áp dụng",
            initialValue = currentWeight,
            range = range,
            unit = unitLabel,
            isLoading = uiState.isUpdatingBiometrics,
            isDarkTheme = isDarkTheme,
            onDismiss = { showEditWeightSheet = false },
            onSave = { newWeight ->
                val weightKg = if (isLb) {
                    UserPreferencesManager.convertToKg(newWeight.toFloat(), "lb")
                } else {
                    newWeight.toFloat()
                }
                viewModel.updateWeight(
                    weightKg = weightKg,
                    onSuccess = {
                        showEditWeightSheet = false
                        Toast.makeText(context, "Cập nhật cân nặng thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showChangePasswordSheet) {
        ChangePasswordModalSheet(
            isDarkTheme = isDarkTheme,
            isLoading = uiState.isChangingPassword,
            onDismiss = { showChangePasswordSheet = false },
            onConfirm = { oldPass, newPass ->
                viewModel.changePassword(
                    oldPass = oldPass,
                    newPass = newPass,
                    onSuccess = {
                        showChangePasswordSheet = false
                        Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showReminderSheet) {
        HabitReminderCenterSheet(
            isDarkTheme = isDarkTheme,
            onDismiss = { showReminderSheet = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 28.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. TOP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hồ Sơ & Cài Đặt",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { onOpenSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Cài đặt",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. HERO: ảnh đại diện lớn, tên, mục tiêu và 3 chỉ số cơ thể trong cùng một mảng peach
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .background(heroBrush())
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(30.dp))
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(VividOrangeLight.copy(alpha = if (NwTheme.isDark) 0.14f else 0.28f), size.width * 0.34f, androidx.compose.ui.geometry.Offset(size.width * 0.98f, 0f))
                }
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Ảnh đại diện: bấm để đổi / xoá
                        Box(modifier = Modifier.size(92.dp).clickable { showAvatarSheet = true }) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(3.dp, VividOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                com.calai.app.presentation.components.UserAvatar(
                                    avatar = profile?.avatar,
                                    displayName = profile?.name ?: profile?.username,
                                    size = 84.dp,
                                    fontSize = 34
                                )
                                if (uiState.isUpdatingAvatar) {
                                    androidx.compose.material3.CircularProgressIndicator(color = PrimaryStrong, strokeWidth = 3.dp, modifier = Modifier.size(30.dp))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(VividOrange)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Đổi ảnh đại diện", tint = OnPrimary, modifier = Modifier.size(15.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = profile?.name ?: "Người dùng NutriWise",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 2
                            )
                            Text(
                                text = "@${profile?.username ?: ""}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val goalLabel = when (profile?.goal) {
                                "LOSE_WEIGHT" -> "Giảm cân"
                                "GAIN_WEIGHT", "GAIN_MUSCLE" -> "Tăng cân"
                                "MAINTAIN" -> "Giữ cân"
                                else -> "Ăn uống lành mạnh"
                            }
                            Text(
                                text = goalLabel,
                                color = OnPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VividOrange)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val rawWeight = profile?.weightKg
                        BodyStat(
                            label = "Chiều cao",
                            value = profile?.heightCm?.toInt()?.toString() ?: "—",
                            unit = "cm",
                            modifier = Modifier.weight(1f),
                            onClick = { showEditHeightSheet = true }
                        )
                        StatDivider()
                        BodyStat(
                            label = "Cân nặng",
                            value = rawWeight?.let { UserPreferencesManager.formatWeightValueOnly(it, uiState.weightUnit) } ?: "—",
                            unit = if (uiState.weightUnit == "lb") "lbs" else "kg",
                            modifier = Modifier.weight(1f),
                            onClick = { showEditWeightSheet = true }
                        )
                        StatDivider()
                        val bmi = profile?.bmi
                        BodyStat(
                            label = "BMI",
                            value = bmi?.let { String.format("%.1f", it) } ?: "—",
                            unit = when {
                                bmi == null -> ""
                                bmi < 18.5f -> "Thiếu cân"
                                bmi < 25f -> "Bình thường"
                                bmi < 30f -> "Thừa cân"
                                else -> "Béo phì"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Chỉnh sửa thông tin cá nhân",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryStrong,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showEditInfo = true }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }
            }

            // 4. NUTRITION TARGET CARD (BENTO)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkTheme) 8.dp else 10.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = if (isDarkTheme) DarkShadow else WarmShadow,
                        spotColor = if (isDarkTheme) DarkShadow else WarmShadow
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mục tiêu calo & dinh dưỡng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${profile?.targetCalories?.toInt()?.toString() ?: "—"} kcal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryStrong
                        )
                    }

                    profile?.tdee?.let {
                        Text(
                            text = "Năng lượng tiêu hao mỗi ngày (TDEE): ${it.toInt()} kcal",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroBadgePill(
                            label = "Protein",
                            amount = "${profile?.targetProtein?.toInt()?.toString() ?: "—"}g",
                            accentColor = if (isDarkTheme) ProteinGradientStart else PastelProteinLight,
                            isDark = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                        MacroBadgePill(
                            label = "Carbs",
                            amount = "${profile?.targetCarb?.toInt()?.toString() ?: "—"}g",
                            accentColor = if (isDarkTheme) CarbGradientStart else PastelCarbLight,
                            isDark = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                        MacroBadgePill(
                            label = "Fat",
                            amount = "${profile?.targetFat?.toInt()?.toString() ?: "—"}g",
                            accentColor = if (isDarkTheme) FatGradientStart else PastelFatLight,
                            isDark = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. CÀI ĐẶT GIAO DIỆN (LIGHT / DARK THEME SWITCH) - Spec 10.6
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkTheme) 8.dp else 10.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = if (isDarkTheme) DarkShadow else WarmShadow,
                        spotColor = if (isDarkTheme) DarkShadow else WarmShadow
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Giao diện",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    com.calai.app.presentation.components.ThemeModeSelector(mode = themeMode, onModeChange = onThemeModeChange)
                }
            }

            // 6. ACTION ROWS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkTheme) 6.dp else 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = if (isDarkTheme) DarkShadow else WarmShadow,
                        spotColor = if (isDarkTheme) DarkShadow else WarmShadow
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
                ActionRowItem(icon = Icons.Default.Star, label = "Gói Premium", isLast = false, isDark = isDarkTheme, onClick = onOpenPremium)
                ActionRowItem(icon = Icons.Default.Edit, label = "Chỉnh sửa chỉ số & mục tiêu", isLast = false, isDark = isDarkTheme, onClick = onOpenGoalSetup)
                ActionRowItem(icon = Icons.Default.Notifications, label = "Nhắc nhở bữa ăn & uống nước", isLast = false, isDark = isDarkTheme, onClick = { showReminderSheet = true })
                ActionRowItem(icon = Icons.Default.Lock, label = "Đổi mật khẩu tài khoản", isLast = false, isDark = isDarkTheme, onClick = { showChangePasswordSheet = true })
                ActionRowItem(icon = Icons.Default.Settings, label = "Cài đặt hệ thống & Giao diện", isLast = true, isDark = isDarkTheme, onClick = onOpenSettings)
            }

            // 7. LOGOUT BUTTON
            Button(
                onClick = {
                    viewModel.logout(onLogout)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = CoralWarning.copy(alpha = 0.25f),
                        spotColor = CoralWarning.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralWarning.copy(alpha = 0.15f),
                    contentColor = CoralWarning
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralWarning.copy(alpha = 0.4f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đăng Xuất",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Floating Bottom Dock
        FloatingBottomDock(
            currentTab = DockTab.PROFILE,
            onTabSelected = onNavigateTab,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun BioMetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    isDark: Boolean = true,
    modifier: Modifier = Modifier,
    canEdit: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 6.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isDark) DarkShadow else WarmShadow,
                spotColor = if (isDark) DarkShadow else WarmShadow
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (canEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa $title",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = unit, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun MacroBadgePill(
    label: String,
    amount: String,
    accentColor: Color,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = amount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
private fun ActionRowItem(
    icon: ImageVector,
    label: String,
    isLast: Boolean,
    isDark: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = (MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = (MaterialTheme.colorScheme.outline).copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBiometricModalSheet(
    title: String,
    subtitle: String,
    initialValue: Int,
    range: IntRange,
    unit: String,
    isLoading: Boolean,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var selectedValue by remember(initialValue) { mutableIntStateOf(initialValue) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Hiển thị số lớn ở giữa
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$selectedValue",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryStrong
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = unit,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // WheelPicker3D
            WheelPicker3D(
                value = selectedValue,
                onValueChange = { selectedValue = it },
                range = range,
                modifier = Modifier.fillMaxWidth(0.55f),
                isDarkTheme = isDarkTheme
            )

            Spacer(Modifier.height(8.dp))

            // Nút Lưu thay đổi
            Button(
                onClick = { onSave(selectedValue) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VividOrange,
                    contentColor = TextDeepInk
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = TextDeepInk,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Lưu thay đổi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}



/** Sửa họ tên, giới tính và ngày sinh. Kiểm tra cơ bản ở máy; máy chủ kiểm tra lại (tuổi 13–100). */
@Composable
private fun EditBasicInfoDialog(
    initialName: String,
    initialGender: String?,
    initialDob: String?,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?) -> Unit,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var gender by remember { mutableStateOf(initialGender) }
    var dob by remember { mutableStateOf(initialDob.orEmpty()) }
    val nameError = if (name.isBlank()) "Nhập họ tên" else if (name.trim().length > 60) "Tối đa 60 ký tự" else null

    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Thông tin cá nhân", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Giới tính", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MALE" to "Nam", "FEMALE" to "Nữ", "OTHER" to "Khác").forEach { (value, label) ->
                        androidx.compose.material3.FilterChip(
                            selected = gender == value,
                            onClick = { gender = value },
                            label = { Text(label) }
                        )
                    }
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        val cal = java.util.Calendar.getInstance()
                        dob.takeIf { it.length == 10 }?.split("-")?.takeIf { it.size == 3 }?.let {
                            cal.set(it[0].toInt(), it[1].toInt() - 1, it[2].toInt())
                        } ?: cal.set(2000, 0, 1)
                        android.app.DatePickerDialog(context, { _, y, m, d -> dob = "%04d-%02d-%02d".format(y, m + 1, d) },
                            cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)
                        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (dob.isBlank()) "Chọn ngày sinh" else "Ngày sinh: ${dob.split("-").reversed().joinToString("/")}") }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { onSave(name, gender, dob.takeIf { it.length == 10 }) },
                enabled = !saving && nameError == null,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = VividOrange)
            ) { Text(if (saving) "Đang lưu..." else "Lưu", color = OnPrimary) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss, enabled = !saving) { Text("Hủy", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}


@Composable
private fun StatDivider() {
    Box(Modifier.width(1.dp).height(34.dp).background(MaterialTheme.colorScheme.outline))
}

@Composable
private fun BodyStat(label: String, value: String, unit: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        if (unit.isNotEmpty()) Text(unit, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}
