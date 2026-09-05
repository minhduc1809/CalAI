package com.calai.app.presentation.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.*
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateTab: (DockTab) -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

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
                    color = if (isDarkTheme) TextWhite else TextInkPrimary,
                    letterSpacing = (-0.5).sp
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                        .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Cài đặt",
                        tint = if (isDarkTheme) TextWhite else TextInkPrimary,
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
                    .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                    .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(24.dp))
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
                            .background(if (isDarkTheme) LavenderGradientStart else Color(0xFFDDD6FE))
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
                            color = if (isDarkTheme) TextWhite else TextInkPrimary
                        )
                        Text(
                            text = "@${profile?.username ?: "calai_user"}",
                            fontSize = 13.sp,
                            color = if (isDarkTheme) TextMuted else TextInkMuted
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
                                text = "🎯 $goalLabel",
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
                color = if (isDarkTheme) TextWhite else TextInkPrimary
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
                    modifier = Modifier.weight(1f)
                )
                BioMetricCard(
                    title = "Cân nặng",
                    value = "${profile?.weightKg ?: 68.5f}",
                    unit = "kg",
                    color = if (isDarkTheme) ProteinGradientStart else PastelProteinLight,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f)
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
                    .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                    .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(24.dp))
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
                            color = if (isDarkTheme) TextWhite else TextInkPrimary
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
                                text = if (isDarkTheme) "🌙 Giao diện tối" else "☀️ Giao diện sáng",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) TextWhite else TextInkPrimary
                            )
                            Text(
                                text = if (isDarkTheme) "Dịu mắt, dễ chịu" else "Sáng rõ, dễ nhìn",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkTheme) TextLightGrey else TextInkSecondary
                            )
                            Text(
                                text = if (isDarkTheme) "Phù hợp khi sử dụng vào ban đêm" else "Thoải mái sử dụng vào ban ngày",
                                fontSize = 11.5.sp,
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
                    .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                    .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(20.dp))
            ) {
                ActionRowItem(icon = Icons.Default.Edit, label = "Chỉnh sửa chỉ số & mục tiêu", isLast = false, isDark = isDarkTheme)
                ActionRowItem(icon = Icons.Default.Notifications, label = "Nhắc nhở bữa ăn & uống nước", isLast = false, isDark = isDarkTheme)
                ActionRowItem(icon = Icons.Default.Lock, label = "Đổi mật khẩu tài khoản", isLast = true, isDark = isDarkTheme)
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
    modifier: Modifier = Modifier
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
            .background(if (isDark) CharcoalSurface else PearlCard)
            .border(1.dp, if (isDark) CharcoalBorder else PearlBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, fontSize = 12.sp, color = if (isDark) TextMuted else TextInkMuted)
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (isDark) TextWhite else TextInkPrimary)
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
            .background(if (isDark) CharcoalDock else Color(0xFFF9F7F2))
            .border(1.dp, if (isDark) CharcoalBorder else PearlBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 11.sp, color = if (isDark) TextMuted else TextInkMuted)
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
    isDark: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if (isDark) TextMuted else TextInkMuted, modifier = Modifier.size(20.dp))
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) TextWhite else TextInkPrimary)
        }
        Icon(
            Icons.Default.ChevronRight,
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
