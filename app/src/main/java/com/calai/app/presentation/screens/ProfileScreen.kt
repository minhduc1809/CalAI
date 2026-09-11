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
    onLogout: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {},
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

    if (showEditHeightSheet) {
        val currentHeight = (profile?.heightCm?.toInt() ?: 170).coerceIn(100, 230)
        EditBiometricModalSheet(
            title = "Chỉnh sửa Chiều cao",
            subtitle = "Hệ thống sẽ tự động tính toán lại BMI, BMR và TDEE của bạn",
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
            subtitle = "Hệ thống sẽ cập nhật nhật ký cân nặng và tự động điều chỉnh calo mục tiêu",
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

            // 2. USER HERO CARD (BENTO)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar Pastel
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                ambientColor = if (isDarkTheme) DarkShadow else WarmShadow,
                                spotColor = if (isDarkTheme) DarkShadow else WarmShadow
                            )
                            .clip(CircleShape)
                            .background(if (isDarkTheme) LavenderGradientStart else PastelLavenderTrackLight)
                            .border(2.dp, VividOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile?.name?.take(1) ?: profile?.username?.take(1) ?: "C").uppercase(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDeepInk
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = profile?.name ?: "Người dùng NutriWise",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "@${profile?.username ?: "calai_user"}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val goalLabel = when (profile?.goal) {
                            "LOSE_WEIGHT" -> "Giảm cân & Siết mỡ"
                            "GAIN_MUSCLE" -> "Tăng cơ nạc"
                            "MAINTAIN" -> "Duy trì vóc dáng"
                            else -> "Ăn uống lành mạnh"
                        }
                        Surface(
                            color = VividOrangeSoft,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = goalLabel,
                                color = VividOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. BIOLOGICAL BENTO GRID (2x2)
            Text(
                text = "Chỉ số sinh học",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BioMetricCard(
                    title = "Chiều cao",
                    value = "${profile?.heightCm?.toInt() ?: 175}",
                    unit = "cm",
                    color = if (isDarkTheme) LavenderGradientStart else PastelLavenderLight,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    canEdit = true,
                    onClick = { showEditHeightSheet = true }
                )
                val rawWeight = profile?.weightKg ?: 68.5f
                val displayWeight = UserPreferencesManager.formatWeightValueOnly(rawWeight, uiState.weightUnit)

                BioMetricCard(
                    title = "Cân nặng",
                    value = displayWeight,
                    unit = if (uiState.weightUnit == "lb") "lbs" else "kg",
                    color = if (isDarkTheme) ProteinGradientStart else PastelProteinLight,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    canEdit = true,
                    onClick = { showEditWeightSheet = true }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BioMetricCard(
                    title = "Chỉ số BMI",
                    value = String.format("%.1f", profile?.bmi ?: 22.4f),
                    unit = "Bình thường",
                    color = if (isDarkTheme) CarbGradientStart else PastelCarbLight,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
                BioMetricCard(
                    title = "Năng lượng TDEE",
                    value = "${profile?.tdee?.toInt() ?: 2310}",
                    unit = "kcal/ngày",
                    color = if (isDarkTheme) FatGradientStart else PastelFatLight,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
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
                            text = "${profile?.targetCalories?.toInt() ?: 1810} kcal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = VividOrange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroBadgePill(
                            label = "Protein",
                            amount = "${profile?.targetProtein?.toInt() ?: 135}g",
                            accentColor = if (isDarkTheme) ProteinGradientStart else PastelProteinLight,
                            isDark = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                        MacroBadgePill(
                            label = "Carbs",
                            amount = "${profile?.targetCarb?.toInt() ?: 200}g",
                            accentColor = if (isDarkTheme) CarbGradientStart else PastelCarbLight,
                            isDark = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                        MacroBadgePill(
                            label = "Fat",
                            amount = "${profile?.targetFat?.toInt() ?: 50}g",
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
                                text = if (isDarkTheme) "Giao diện tối" else "Giao diện sáng",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (isDarkTheme) "Dịu mắt, dễ chịu" else "Sáng rõ, dễ nhìn",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (isDarkTheme) "Phù hợp khi sử dụng vào ban đêm" else "Thoải mái sử dụng vào ban ngày",
                                fontSize = 11.5.sp,
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
                    color = VividOrange
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

