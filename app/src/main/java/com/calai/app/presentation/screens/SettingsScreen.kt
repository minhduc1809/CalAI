package com.calai.app.presentation.screens

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.components.AppFormDialog
import com.calai.app.presentation.components.AppTextField
import com.calai.app.presentation.components.AppToggle
import com.calai.app.presentation.components.DuotoneMoonIcon
import com.calai.app.presentation.components.DuotoneSunIcon
import com.calai.app.presentation.components.TactileThemeSwitch
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ProfileViewModel
import com.calai.app.presentation.viewmodel.checkinDayName
import com.calai.app.domain.DailyCompletionRule
import com.calai.app.notification.DailyCompletionScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {},
    onOpenGoalSetup: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Chọn nơi lưu tệp ZIP trước (không cần quyền lưu trữ), rồi mới gọi máy chủ để không tốn lượt xuất khi người dùng huỷ
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) viewModel.exportData(context.contentResolver, uri)
    }
    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeExportMessage()
        }
    }

    var showChangePasswordSheet by remember { mutableStateOf(false) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }
    var showMealStructureDialog by remember { mutableStateOf(false) }
    var showCheckinDaySheet by remember { mutableStateOf(false) }
    var showRestDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSessionsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEmailVerifySheet by remember { mutableStateOf(false) }

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

    if (showCheckinDaySheet) {
        CheckinDayModalSheet(
            currentDay = uiState.profile?.checkinDay ?: 1,
            onSelectDay = { day ->
                viewModel.setCheckinDay(day) { _, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showCheckinDaySheet = false }
        )
    }

    if (showMealStructureDialog) {
        MealStructureModalSheet(
            isDarkTheme = isDarkTheme,
            currentMode = uiState.mealStructureMode,
            onSelectMode = { mode ->
                viewModel.setMealStructureMode(mode)
                val modeLabel = if (mode == "FIXED_MEALS") "Bữa cố định" else "Timeline theo giờ"
                Toast.makeText(context, "Đã đổi cách hiển thị sang $modeLabel", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showMealStructureDialog = false }
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

    if (showEmailVerifySheet) {
        EmailVerificationModalSheet(
            isDarkTheme = isDarkTheme,
            email = uiState.profile?.email,
            isSending = uiState.isSendingVerificationEmail,
            isVerifying = uiState.isVerifyingEmail,
            onSendCode = {
                viewModel.sendVerificationEmail(
                    onSuccess = {
                        Toast.makeText(context, "Đã gửi mã xác thực tới email của bạn", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onConfirmCode = { code ->
                viewModel.verifyEmail(
                    code = code,
                    onSuccess = {
                        showEmailVerifySheet = false
                        Toast.makeText(context, "Xác thực email thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onDismiss = { showEmailVerifySheet = false }
        )
    }

    if (showReminderSheet) {
        HabitReminderCenterSheet(
            isDarkTheme = isDarkTheme,
            onDismiss = { showReminderSheet = false }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Về NutriWise",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "Bản quyền © 2026 NutriWise Team. Mọi quyền được bảo lưu.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            .background(MaterialTheme.colorScheme.background)
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
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Cài Đặt Hệ Thống",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Giao diện, nhắc nhở & dữ liệu",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
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
                                .background(if (isDarkTheme) MaterialTheme.colorScheme.surfaceContainerHighest else VividOrangeSoft)
                                .border(1.dp, if (isDarkTheme) MaterialTheme.colorScheme.outline else VividOrange.copy(alpha = 0.3f), CircleShape),
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
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (isDarkTheme) "Tông nền Obsidian bảo vệ mắt ban đêm" else "Tông nền Ivory nhã nhặn, sang trọng",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    isLast = false,
                    onClick = { showReminderSheet = true }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.EventRepeat,
                    title = "Nhắc ghi đủ cuối ngày",
                    subtitle = "Lúc ${uiState.dailyCompletionTime}, hỏi bạn hôm nay đã ghi đủ bữa ăn chưa",
                    checked = uiState.dailyCompletionEnabled,
                    isDark = isDarkTheme,
                    isLast = !uiState.dailyCompletionEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setDailyCompletionEnabled(enabled)
                        DailyCompletionScheduler.sync(context, enabled, uiState.dailyCompletionTime)
                    }
                )
                if (uiState.dailyCompletionEnabled) {
                    SettingsActionRow(
                        icon = Icons.Default.Schedule,
                        title = "Giờ nhắc ghi đủ",
                        subtitle = uiState.dailyCompletionTime,
                        isDark = isDarkTheme,
                        isLast = true,
                        onClick = {
                            val (hour, minute) = DailyCompletionRule.parseTime(uiState.dailyCompletionTime)
                            android.app.TimePickerDialog(
                                context,
                                { _, h, m ->
                                    val time = DailyCompletionRule.formatTime(h, m)
                                    viewModel.setDailyCompletionTime(time)
                                    DailyCompletionScheduler.sync(context, true, time)
                                },
                                hour,
                                minute,
                                true
                            ).show()
                        }
                    )
                }
            }

            // 3b. LOẠI THÔNG BÁO TRONG APP (BR-19.4): tắt loại nào thì máy chủ không tạo loại đó nữa
            SettingsSectionHeader(title = "Loại thông báo trong app", isDark = isDarkTheme)
            SettingsGroupCard(isDark = isDarkTheme) {
                val toggles = com.calai.app.data.sync.SettingsSync.TOGGLEABLE_NOTIFICATIONS
                toggles.forEachIndexed { index, (type, label) ->
                    SettingsSwitchRow(
                        icon = Icons.Default.NotificationsActive,
                        title = label,
                        subtitle = if (type in uiState.disabledNotifications) "Đã tắt" else "Đang bật",
                        checked = type !in uiState.disabledNotifications,
                        isDark = isDarkTheme,
                        isLast = index == toggles.lastIndex,
                        onCheckedChange = { enabled -> viewModel.setNotificationEnabled(type, enabled) }
                    )
                }
            }
            Text(
                "Thông báo về thanh toán luôn được gửi.",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, top = 6.dp)
            )

            // 3c. TẬP LUYỆN: thời gian nghỉ mặc định giữa set
            SettingsSectionHeader(title = "Tập luyện", isDark = isDarkTheme)
            SettingsGroupCard(isDark = isDarkTheme) {
                SettingsActionRow(
                    icon = Icons.Default.Schedule,
                    title = "Nghỉ mặc định giữa set",
                    subtitle = "${uiState.defaultRestSec} giây",
                    isDark = isDarkTheme,
                    isLast = true,
                    onClick = { showRestDialog = true }
                )
            }
            if (showRestDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showRestDialog = false },
                    title = { Text("Nghỉ mặc định giữa set") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(45, 60, 90, 120, 180, 240).forEach { sec ->
                                androidx.compose.material3.TextButton(onClick = {
                                    viewModel.setDefaultRestSec(sec)
                                    showRestDialog = false
                                }) {
                                    Text(if (sec == uiState.defaultRestSec) "$sec giây (đang dùng)" else "$sec giây")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { showRestDialog = false }) { Text("Đóng") }
                    }
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
                    isLast = false,
                    onClick = {
                        showUnitDialog = true
                    }
                )
                val mealModeLabel = if (uiState.mealStructureMode == "FIXED_MEALS") "Bữa cố định (Sáng/Trưa/Tối/Phụ)" else "Timeline theo giờ"
                SettingsActionRow(
                    icon = Icons.Default.Schedule,
                    title = "Hiển thị Nhật ký ăn uống",
                    subtitle = mealModeLabel,
                    isDark = isDarkTheme,
                    isLast = false,
                    onClick = { showMealStructureDialog = true }
                )
                SettingsActionRow(
                    icon = Icons.Default.EventRepeat,
                    title = "Ngày Check-in hằng tuần",
                    subtitle = "${checkinDayName(uiState.profile?.checkinDay)} • Nhìn lại tuần và điều chỉnh mục tiêu",
                    isDark = isDarkTheme,
                    isLast = true,
                    onClick = { showCheckinDaySheet = true }
                )
            }


            // 5. BẢO MẬT & TÀI KHOẢN (Spec 1–4)
            SettingsSectionHeader(title = "Tài khoản & Bảo mật", isDark = isDarkTheme)

            SettingsGroupCard(isDark = isDarkTheme) {
                if (uiState.profile?.email != null) {
                    SettingsActionRow(
                        icon = if (uiState.profile?.isEmailVerified == true) Icons.Default.VerifiedUser else Icons.Default.MarkEmailUnread,
                        title = "Xác thực Email",
                        subtitle = if (uiState.profile?.isEmailVerified == true) "Email đã được xác thực" else "Chưa xác thực — nhấn để xác thực ngay",
                        isDark = isDarkTheme,
                        isLast = false,
                        onClick = { showEmailVerifySheet = true }
                    )
                }
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
                    subtitle = if (uiState.isExporting) "Đang chuẩn bị dữ liệu..." else "Tải toàn bộ dữ liệu của bạn (ZIP gồm JSON và CSV), tối đa 1 lần mỗi ngày",
                    isDark = isDarkTheme,
                    isLast = false,
                    onClick = {
                        if (!uiState.isExporting) {
                            val day = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                            exportLauncher.launch("nutriwise-export-$day.zip")
                        }
                    }
                )
                SettingsActionRow(
                    icon = Icons.Default.CleaningServices,
                    title = "Xóa bộ nhớ đệm (Clear Cache)",
                    subtitle = "Giải phóng dung lượng ảnh quét AI",
                    isDark = isDarkTheme,
                    isLast = true,
                    onClick = {
                        // Xoá thật thư mục cache (ảnh tạm khi quét AI lưu vào context.cacheDir,
                        // xem CameraScanViewModel) — trước đây Toast báo "đã xóa" nhưng
                        // không hề gọi bất kỳ lệnh xóa nào, đánh lừa người dùng.
                        val freedBytes = clearAppCache(context)
                        val freedMb = freedBytes / (1024f * 1024f)
                        val message = if (freedBytes > 0) {
                            "Đã giải phóng %.1f MB bộ nhớ đệm.".format(freedMb)
                        } else {
                            "Bộ nhớ đệm hiện đang trống, không có gì để xóa."
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                    onClick = onOpenPrivacy
                )
            }

            // 7. TÀI KHOẢN & DỮ LIỆU (BR-01.3, BR-01.4)
            SettingsSectionHeader(title = "Tài khoản & Dữ liệu", isDark = isDarkTheme)
            SettingsGroupCard(isDark = isDarkTheme) {
                SettingsActionRow(
                    icon = Icons.Default.Info,
                    title = "Thiết bị đăng nhập",
                    subtitle = "Xem và đăng xuất từng thiết bị",
                    isDark = isDarkTheme,
                    isLast = false,
                    onClick = { showSessionsDialog = true }
                )
                SettingsActionRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Xoá tài khoản",
                    subtitle = "Xoá vĩnh viễn tài khoản và toàn bộ dữ liệu",
                    isDark = isDarkTheme,
                    isLast = true,
                    onClick = { showDeleteDialog = true }
                )
            }
            if (showSessionsDialog) {
                SessionsDialog(viewModel = viewModel, onDismiss = { showSessionsDialog = false })
            }
            if (showDeleteDialog) {
                DeleteAccountDialog(
                    viewModel = viewModel,
                    onDismiss = { showDeleteDialog = false },
                    onDeleted = {
                        showDeleteDialog = false
                        onAccountDeleted()
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
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
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Mật khẩu mới tối thiểu 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số.",
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
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
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
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
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            localError?.let {
                Text(text = it, color = CoralWarning, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            AppButton(
                text = "Cập Nhật Mật Khẩu",
                onClick = {
                    if (newPassword != confirmPassword) {
                        localError = "Mật khẩu xác nhận không khớp"
                        return@AppButton
                    }
                    onConfirm(oldPassword, newPassword)
                },
                enabled = oldPassword.isNotBlank() && newPassword.isNotBlank(),
                isLoading = isLoading,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationModalSheet(
    isDarkTheme: Boolean,
    email: String?,
    isSending: Boolean,
    isVerifying: Boolean,
    onSendCode: () -> Unit,
    onConfirmCode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Xác Thực Email",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Nhấn \"Gửi mã\" để nhận mã OTP 6 chữ số tới ${email ?: "email của bạn"}, sau đó nhập mã vào ô bên dưới.",
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = onSendCode,
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = VividOrange, strokeWidth = 2.dp)
                } else {
                    Text("Gửi mã xác thực", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                }
            }

            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
                label = { Text("Mã OTP (6 chữ số)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = VividOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            AppButton(
                text = "Xác Nhận Mã",
                onClick = { onConfirmCode(code) },
                enabled = code.length == 6,
                isLoading = isVerifying,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

/**
 * Dialog "Thêm mới nhắc nhở" cho nhắc nhở tuỳ chỉnh — dùng chung AppFormDialog/AppTextField
 * (Part 7/8), chọn giờ bằng WheelTimePicker (kéo cuộn) thay cho TimePickerDialog hệ thống.
 */
@Composable
internal fun AddCustomReminderDialog(
    onDismiss: () -> Unit,
    onSave: (label: String, time: String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("08:00") }

    AppFormDialog(
        title = "Thêm mới nhắc nhở",
        onDismiss = onDismiss,
        actions = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Hủy")
            }
            AppButton(
                text = "Lưu",
                onClick = { if (label.isNotBlank()) onSave(label.trim(), time) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = label.isNotBlank(),
                shape = RoundedCornerShape(14.dp)
            )
        }
    ) {
        AppTextField(
            label = "Tên nhắc nhở",
            value = label,
            onValueChange = { label = it },
            placeholder = "VD: Uống thuốc, Tập thể dục..."
        )

        WheelTimePicker(
            timeString = time,
            onTimeChange = { time = it },
            isDarkTheme = false
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
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Chọn đơn vị cân nặng mong muốn để hiển thị trên toàn bộ ứng dụng:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            AppButton(
                text = "Xong",
                onClick = onDismiss,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealStructureModalSheet(
    isDarkTheme: Boolean,
    currentMode: String,
    onSelectMode: (String) -> Unit,
    onDismiss: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Hiển Thị Nhật Ký Ăn Uống",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Chỉ đổi cách nhóm hiển thị — dữ liệu calo/macro đã lưu không thay đổi.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            UnitOptionCard(
                title = "Timeline theo giờ",
                description = "Nhóm bữa ăn theo mốc giờ đã log, không giới hạn số bữa/ngày",
                isSelected = currentMode == "TIMELINE",
                isDark = isDarkTheme,
                onClick = {
                    onSelectMode("TIMELINE")
                    onDismiss()
                }
            )

            UnitOptionCard(
                title = "Bữa cố định",
                description = "Nhóm theo Sáng / Trưa / Tối / Phụ như truyền thống",
                isSelected = currentMode == "FIXED_MEALS",
                isDark = isDarkTheme,
                onClick = {
                    onSelectMode("FIXED_MEALS")
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            AppButton(
                text = "Xong",
                onClick = onDismiss,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(14.dp)
            )
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
            .background(if (isSelected) VividOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) VividOrange else MaterialTheme.colorScheme.outline,
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
                    color = if (isSelected) VividOrange else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = VividOrange,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Xóa thật toàn bộ file trong `context.cacheDir` (ảnh tạm khi quét AI được lưu ở đây,
 * xem `CameraScanViewModel.downscaleAndCompressToJpeg` — `File(context.cacheDir, "scan_*.jpg")`)
 * và `context.externalCacheDir` nếu có. Trả về tổng số byte đã giải phóng thật sự.
 */
private fun clearAppCache(context: android.content.Context): Long {
    fun deleteRecursively(dir: java.io.File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var freed = 0L
        dir.listFiles()?.forEach { file ->
            freed += if (file.isDirectory) {
                val sub = deleteRecursively(file)
                file.delete()
                sub
            } else {
                val size = file.length()
                if (file.delete()) size else 0L
            }
        }
        return freed
    }
    return deleteRecursively(context.cacheDir) + deleteRecursively(context.externalCacheDir)
}



/** Chọn ngày Check-in hằng tuần (BR-06.1). Đổi ngày có hiệu lực ngay, kỳ dữ liệu luôn là 7 ngày trước ngày đó. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinDayModalSheet(
    currentDay: Int,
    onSelectDay: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val days = listOf(1 to "T2", 2 to "T3", 3 to "T4", 4 to "T5", 5 to "T6", 6 to "T7", 7 to "CN")
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Ngày Check-in hằng tuần",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Vào ngày này mỗi tuần, NutriWise nhìn lại 7 ngày vừa qua của bạn và đề xuất điều chỉnh mục tiêu. Chọn ngày bạn hay rảnh để xem lại.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { (day, label) ->
                    val selected = day == currentDay
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) androidx.compose.ui.graphics.Brush.linearGradient(listOf(LavenderGradientStart, LavenderGradientEnd))
                                else androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                                )
                            )
                            .border(
                                1.dp,
                                if (selected) LavenderGradientStart else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectDay(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (selected) TextDeepInk else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = "Đang chọn: ${checkinDayName(currentDay)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            AppButton(
                text = "Xong",
                onClick = onDismiss,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}


/** Dòng cài đặt có công tắc bật/tắt, cùng kiểu với [SettingsActionRow]. */
@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    isDark: Boolean,
    isLast: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 12.dp),
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDark) PastelLavender else VividOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}


/** Danh sách thiết bị đang đăng nhập; đăng xuất từng thiết bị hoặc tất cả (BR-01.3). */
@Composable
private fun SessionsDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    var sessions by remember { mutableStateOf<List<com.calai.app.data.remote.dto.SessionDto>?>(null) }
    var failed by remember { mutableStateOf(false) }
    fun load() = viewModel.loadSessions { result ->
        sessions = result
        failed = result == null
    }
    LaunchedEffect(Unit) { load() }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thiết bị đăng nhập") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when {
                    failed -> Text("Không tải được danh sách. Kiểm tra kết nối rồi thử lại.")
                    sessions == null -> Text("Đang tải…")
                    sessions!!.isEmpty() -> Text("Không có thiết bị nào.")
                    else -> sessions!!.forEach { s ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.deviceName?.takeIf { it.isNotBlank() } ?: "Thiết bị không rõ tên", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Dùng gần nhất: " + (s.lastUsedAt?.take(16)?.replace('T', ' ') ?: "—"),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            androidx.compose.material3.TextButton(onClick = { viewModel.revokeSession(s.id) { load() } }) { Text("Đăng xuất") }
                        }
                    }
                }
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}

/**
 * Xoá tài khoản (BR-01.4). Ba bước: đọc cảnh báo, nhập mật khẩu để xác thực lại, gõ "XOA" để xác nhận cuối.
 * Gói Premium trên Google Play không tự huỷ nên phải nhắc người dùng.
 */
@Composable
private fun DeleteAccountDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit, onDeleted: () -> Unit) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Xoá tài khoản?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Toàn bộ dữ liệu của bạn (bữa ăn, cân nặng, tập luyện, mục tiêu...) sẽ bị xoá vĩnh viễn và không khôi phục được. Bạn nên xuất dữ liệu trước nếu muốn giữ bản sao.")
                Text(
                    "Gói Premium trên Google Play không tự huỷ. Hãy huỷ trong Play Store để không bị trừ tiền. Gói mua qua QR sẽ mất quyền còn lại và không hoàn tiền.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.TextButton(onClick = {
                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/account/subscriptions")))
                }) { Text("Mở trang quản lý gói trên Google Play") }
                androidx.compose.material3.OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Mật khẩu hiện tại") }, singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.material3.OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it }, label = { Text("Gõ XOA để xác nhận") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.5.sp) }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                enabled = !busy && password.isNotBlank() && confirm.trim().uppercase() == "XOA",
                onClick = {
                    busy = true
                    error = null
                    viewModel.deleteAccount(password) { ok, msg ->
                        busy = false
                        if (ok) onDeleted() else error = msg
                    }
                }
            ) { Text(if (busy) "Đang xoá…" else "Xoá vĩnh viễn", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { androidx.compose.material3.TextButton(enabled = !busy, onClick = onDismiss) { Text("Huỷ") } }
    )
}
