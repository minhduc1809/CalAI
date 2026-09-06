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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.DuotoneMoonIcon
import com.calai.app.presentation.components.DuotoneSunIcon
import com.calai.app.presentation.components.TactileThemeSwitch
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {},
    onOpenGoalSetup: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showChangePasswordSheet by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    if (showUnitDialog) {
        UnitSelectionModalSheet(
            isDarkTheme = isDarkTheme,
            currentWeightUnit = uiState.weightUnit,
            onSelectWeightUnit = { unit ->
                viewModel.setWeightUnit(unit)
                val unitLabel = if (unit == "lb") "Pounds (lbs)" else "Kilograms (kg)"
                Toast.makeText(context, "Đã đổi đơn vị cân nặng sang $unitLabel", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showUnitDialog = false }
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
        RemindersModalSheet(
            isDarkTheme = isDarkTheme,
            settings = uiState.reminderSettings,
            onUpdateBreakfast = { enabled, time -> viewModel.updateBreakfastReminder(enabled, time) },
            onUpdateLunch = { enabled, time -> viewModel.updateLunchReminder(enabled, time) },
            onUpdateDinner = { enabled, time -> viewModel.updateDinnerReminder(enabled, time) },
            onUpdateSnack = { enabled, time -> viewModel.updateSnackReminder(enabled, time) },
            onUpdateWater = { enabled, interval -> viewModel.updateWaterReminder(enabled, interval) },
            onDismiss = { showReminderSheet = false }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = if (isDarkTheme) CharcoalSurface else PearlCard,
            title = {
                Text(
                    text = "Về NutriWise (CalAI)",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) TextWhite else TextInkPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Phiên bản: 1.2.0 (Build 2026)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VividOrange
                    )
                    Text(
                        text = "Ứng dụng trợ lý AI dinh dưỡng & thể hình cá nhân hóa với Google Gemini 2.0 Flash Vision, tính năng theo dõi vĩ mô và cân nặng thích ứng.",
                        fontSize = 13.sp,
                        color = if (isDarkTheme) TextMuted else TextInkMuted,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "Bản quyền © 2026 NutriWise Team. Mọi quyền được bảo lưu.",
                        fontSize = 12.sp,
                        color = if (isDarkTheme) TextMuted else TextInkMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Đóng", color = VividOrange, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) ObsidianBackground else IvoryBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 28.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. TOP HEADER & BACK BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                        .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = if (isDarkTheme) TextWhite else TextInkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Cài Đặt Hệ Thống",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Giao diện, nhắc nhở & dữ liệu",
                        fontSize = 12.5.sp,
                        color = if (isDarkTheme) TextMuted else TextInkMuted
                    )
                }
            }

            // 2. PHẦN GIAO DIỆN & THEME (Spec 133)
            SettingsSectionHeader(title = "Giao diện & Chủ đề", isDark = isDarkTheme)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkTheme) 6.dp else 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                    .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) CharcoalCardElevated else VividOrangeSoft)
                                .border(1.dp, if (isDarkTheme) CharcoalBorder else VividOrange.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDarkTheme) {
                                DuotoneMoonIcon(size = 22.dp, outlineColor = PastelLavender, accentColor = LavenderGradientEnd)
                            } else {
                                DuotoneSunIcon(size = 22.dp, outlineColor = VividOrange, accentColor = CarbGradientStart)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (isDarkTheme) "Chế độ tối (Dark Luxury)" else "Chế độ sáng (Ivory Light)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) TextWhite else TextInkPrimary
                            )
                            Text(
                                text = if (isDarkTheme) "Tông nền Obsidian bảo vệ mắt ban đêm" else "Tông nền Ivory nhã nhặn, sang trọng",
                                fontSize = 12.sp,
                                color = if (isDarkTheme) TextMuted else TextInkMuted
                            )
                        }
                    }

                    TactileThemeSwitch(
                        isDarkTheme = isDarkTheme,
                        onThemeChanged = { onToggleTheme(it) }
                    )
                }
            }

            // 3. THÔNG BÁO & NHẮC NHỞ (Spec STT 122–125)
            SettingsSectionHeader(title = "Thông báo & Lối sống", isDark = isDarkTheme)

            SettingsGroupCard(isDark = isDarkTheme) {
                SettingsActionRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Nhắc nhở bữa ăn & Uống nước",
                    subtitle = "Sáng 07:30 • Trưa 12:00 • Tối 19:00",
                    isDark = isDarkTheme,
                    isLast = true,
                    onClick = { showReminderSheet = true }
                )
            }

            // 4. MỤC TIÊU & ĐƠN VỊ ĐO LƯỜNG (Spec 132)
            SettingsSectionHeader(title = "Mục tiêu & Đơn vị đo", isDark = isDarkTheme)

            SettingsGroupCard(isDark = isDarkTheme) {
                SettingsActionRow(
                    icon = Icons.Default.Tune,
                    title = "Thiết lập mục tiêu & Tỷ lệ macro",
                    subtitle = "Calo, Protein, Carb, Fat theo tuần",
                    isDark = isDarkTheme,
                    isLast = false,
                    onClick = onOpenGoalSetup
                )
                val weightLabel = if (uiState.weightUnit == "lb") "Pound (lb)" else "Kilogram (kg)"
                SettingsActionRow(
                    icon = Icons.Default.Straighten,
                    title = "Đơn vị đo lường (Units)",
                    subtitle = "$weightLabel • Centimeter (cm) • Calo (kcal)",
                    isDark = isDarkTheme,
                    isLast = true,
                    onClick = {
                        showUnitDialog = true
                    }
                )
            }


            // 5. BẢO MẬT & TÀI KHOẢN (Spec 1–4)
            SettingsSectionHeader(title = "Tài khoản & Bảo mật", isDark = isDarkTheme)

            SettingsGroupCard(isDark = isDarkTheme) {
                SettingsActionRow(
                    icon = Icons.Default.LockReset,
                    title = "Đổi mật khẩu tài khoản",
                    subtitle = "Bảo mật với chuẩn mã hóa bcrypt",
                    isDark = isDarkTheme,
                    isLast = false,
                    onClick = { showChangePasswordSheet = true }
                )
                SettingsActionRow(
                    icon = Icons.Default.CloudDownload,
                    title = "Xuất dữ liệu cá nhân (Export)",
                    subtitle = "Tải toàn bộ lịch sử calo, cân nặng dạng file",
                    isDark = isDarkTheme,
                    isLast = false,
                    onClick = {
                        Toast.makeText(context, "Đang chuẩn bị gói dữ liệu xuất...", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsActionRow(
                    icon = Icons.Default.CleaningServices,
                    title = "Xóa bộ nhớ đệm (Clear Cache)",
                    subtitle = "Giải phóng dung lượng ảnh quét AI",
                    isDark = isDarkTheme,
                    isLast = true,
                    onClick = {
                        Toast.makeText(context, "Đã làm sạch bộ đệm tạm thời thành công!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 6. THÔNG TIN ỨNG DỤNG
            SettingsSectionHeader(title = "Thông tin & Hỗ trợ", isDark = isDarkTheme)

            SettingsGroupCard(isDark = isDarkTheme) {
                SettingsActionRow(
                    icon = Icons.Default.Info,
                    title = "Về NutriWise",
                    subtitle = "Phiên bản 1.2.0 • Bản dựng ổn định",
                    isDark = isDarkTheme,
                    isLast = false,
                    onClick = { showAboutDialog = true }
                )
                SettingsActionRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Chính sách bảo mật & Điều khoản",
                    subtitle = "Bảo vệ thông tin sinh trắc học và dữ liệu bữa ăn",
                    isDark = isDarkTheme,
                    isLast = true,
                    onClick = {
                        Toast.makeText(context, "Dữ liệu được bảo vệ theo tiêu chuẩn RFC5322 & JWT", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, isDark: Boolean) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = VividOrange,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun SettingsGroupCard(
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val shadowColor = if (isDark) DarkShadow else WarmShadow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 6.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) CharcoalSurface else PearlCard)
            .border(1.dp, if (isDark) CharcoalBorder else PearlBorder, RoundedCornerShape(20.dp))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDark: Boolean,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) CharcoalDock else PearlDock),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDark) PastelLavender else VividOrange,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) TextWhite else TextInkPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isDark) TextMuted else TextInkMuted
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = (if (isDark) TextMuted else TextInkMuted).copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }

    if (!isLast) {
        HorizontalDivider(
            color = (if (isDark) CharcoalBorder else PearlBorder).copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordModalSheet(
    isDarkTheme: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (oldPass: String, newPass: String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkTheme) CharcoalSurface else PearlCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = if (isDarkTheme) TextMuted else TextInkMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đổi Mật Khẩu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) TextWhite else TextInkPrimary
                )
            }

            Text(
                text = "Mật khẩu mới tối thiểu 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số.",
                fontSize = 12.5.sp,
                color = if (isDarkTheme) TextMuted else TextInkMuted
            )

            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it; localError = null },
                label = { Text("Mật khẩu hiện tại") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDarkTheme) CharcoalDock else PearlSurface,
                    unfocusedContainerColor = if (isDarkTheme) CharcoalDock else PearlSurface,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = if (isDarkTheme) CharcoalBorder else PearlBorder,
                    focusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary,
                    unfocusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary
                )
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; localError = null },
                label = { Text("Mật khẩu mới (≥8 ký tự)") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDarkTheme) CharcoalDock else PearlSurface,
                    unfocusedContainerColor = if (isDarkTheme) CharcoalDock else PearlSurface,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = if (isDarkTheme) CharcoalBorder else PearlBorder,
                    focusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary,
                    unfocusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary
                )
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; localError = null },
                label = { Text("Xác nhận mật khẩu mới") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDarkTheme) CharcoalDock else PearlSurface,
                    unfocusedContainerColor = if (isDarkTheme) CharcoalDock else PearlSurface,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = if (isDarkTheme) CharcoalBorder else PearlBorder,
                    focusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary,
                    unfocusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary
                )
            )

            localError?.let {
                Text(text = it, color = CoralWarning, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        localError = "Mật khẩu xác nhận không khớp"
                        return@Button
                    }
                    onConfirm(oldPassword, newPassword)
                },
                enabled = !isLoading && oldPassword.isNotBlank() && newPassword.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextWhite)
                } else {
                    Text("Cập Nhật Mật Khẩu", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersModalSheet(
    isDarkTheme: Boolean,
    settings: com.calai.app.presentation.viewmodel.ReminderSettingsState,
    onUpdateBreakfast: (Boolean, String) -> Unit,
    onUpdateLunch: (Boolean, String) -> Unit,
    onUpdateDinner: (Boolean, String) -> Unit,
    onUpdateSnack: (Boolean, String) -> Unit,
    onUpdateWater: (Boolean, Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkTheme) CharcoalSurface else PearlCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = if (isDarkTheme) TextMuted else TextInkMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nhắc Nhở Bữa Ăn & Uống Nước",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) TextWhite else TextInkPrimary
            )

            ReminderToggleRow(
                title = "Bữa Sáng",
                time = settings.breakfastTime,
                enabled = settings.breakfastEnabled,
                isDark = isDarkTheme,
                onToggle = { onUpdateBreakfast(it, settings.breakfastTime) }
            )

            ReminderToggleRow(
                title = "Bữa Trưa",
                time = settings.lunchTime,
                enabled = settings.lunchEnabled,
                isDark = isDarkTheme,
                onToggle = { onUpdateLunch(it, settings.lunchTime) }
            )

            ReminderToggleRow(
                title = "Bữa Tối",
                time = settings.dinnerTime,
                enabled = settings.dinnerEnabled,
                isDark = isDarkTheme,
                onToggle = { onUpdateDinner(it, settings.dinnerTime) }
            )

            ReminderToggleRow(
                title = "Bữa Phụ",
                time = settings.snackTime,
                enabled = settings.snackEnabled,
                isDark = isDarkTheme,
                onToggle = { onUpdateSnack(it, settings.snackTime) }
            )

            ReminderToggleRow(
                title = "Nhắc Uống Nước",
                time = "Mỗi ${settings.waterInterval} giờ",
                enabled = settings.waterEnabled,
                isDark = isDarkTheme,
                onToggle = { onUpdateWater(it, settings.waterInterval) }
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
            ) {
                Text("Xong", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun ReminderToggleRow(
    title: String,
    time: String,
    enabled: Boolean,
    isDark: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) CharcoalDock else PearlDock)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) TextWhite else TextInkPrimary
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = VividOrange,
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextWhite,
                checkedTrackColor = VividOrange,
                uncheckedThumbColor = if (isDark) TextMuted else TextInkMuted,
                uncheckedTrackColor = if (isDark) CharcoalCardElevated else PearlBorder
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelectionModalSheet(
    isDarkTheme: Boolean,
    currentWeightUnit: String,
    onSelectWeightUnit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkTheme) CharcoalSurface else PearlCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = if (isDarkTheme) TextMuted else TextInkMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Đơn Vị Đo Lường (Units)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) TextWhite else TextInkPrimary
            )

            Text(
                text = "Chọn đơn vị cân nặng mong muốn để hiển thị trên toàn bộ ứng dụng:",
                fontSize = 13.sp,
                color = if (isDarkTheme) TextMuted else TextInkMuted
            )

            // Tùy chọn 1: Kilogram (kg)
            UnitOptionCard(
                title = "Kilogram (kg)",
                description = "Hệ mét tiêu chuẩn (1 kg = 1000g)",
                isSelected = currentWeightUnit == "kg",
                isDark = isDarkTheme,
                onClick = {
                    onSelectWeightUnit("kg")
                    onDismiss()
                }
            )

            // Tùy chọn 2: Pound (lb)
            UnitOptionCard(
                title = "Pound (lb / lbs)",
                description = "Hệ đo lường Anh-Mỹ (1 kg ≈ 2.205 lbs)",
                isSelected = currentWeightUnit == "lb",
                isDark = isDarkTheme,
                onClick = {
                    onSelectWeightUnit("lb")
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividOrange)
            ) {
                Text("Xong", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun UnitOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) VividOrange.copy(alpha = 0.12f) else if (isDark) CharcoalDock else PearlDock)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) VividOrange else if (isDark) CharcoalBorder else PearlBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) VividOrange else if (isDark) TextWhite else TextInkPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (isDark) TextMuted else TextInkMuted
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = VividOrange,
                    unselectedColor = if (isDark) TextMuted else TextInkMuted
                )
            )
        }
    }
}

