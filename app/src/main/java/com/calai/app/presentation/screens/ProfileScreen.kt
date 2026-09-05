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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.DockTab
import com.calai.app.presentation.components.FloatingBottomDock
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateTab: (DockTab) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
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
                    text = "Hồ Sơ & Mục Tiêu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = (-0.5).sp
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CharcoalSurface)
                        .border(1.dp, CharcoalBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Cài đặt",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. USER HERO CARD (BENTO)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
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
                            .clip(CircleShape)
                            .background(PastelLavender)
                            .border(2.dp, VividOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile?.name?.take(1) ?: profile?.username?.take(1) ?: "C").uppercase(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalSurface
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = profile?.name ?: "Người dùng NutriWise",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "@${profile?.username ?: "calai_user"}",
                            fontSize = 13.sp,
                            color = TextMuted
                        )

                        val goalLabel = when (profile?.goal) {
                            "LOSE_WEIGHT" -> "Giảm cân & Siết mỡ"
                            "GAIN_MUSCLE" -> "Tăng cơ nạc"
                            "MAINTAIN" -> "Duy trì vóc dáng"
                            else -> "Ăn uống lành mạnh"
                        }
                        Surface(
                            color = VividOrange.copy(alpha = 0.15f),
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
                color = TextWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BioMetricCard(
                    title = "Chiều cao",
                    value = "${profile?.heightCm?.toInt() ?: 175}",
                    unit = "cm",
                    color = PastelLavender,
                    modifier = Modifier.weight(1f)
                )
                BioMetricCard(
                    title = "Cân nặng",
                    value = "${profile?.weightKg ?: 68.5f}",
                    unit = "kg",
                    color = MintJade,
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
                    color = ButtercupYellow,
                    modifier = Modifier.weight(1f)
                )
                BioMetricCard(
                    title = "Năng lượng TDEE",
                    value = "${profile?.tdee?.toInt() ?: 2310}",
                    unit = "kcal/ngày",
                    color = RoseBlush,
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. NUTRITION TARGET CARD (BENTO)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
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
                            color = TextWhite
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
                            accentColor = MintJade,
                            modifier = Modifier.weight(1f)
                        )
                        MacroBadgePill(
                            label = "Carbs",
                            amount = "${profile?.targetCarb?.toInt() ?: 200}g",
                            accentColor = ButtercupYellow,
                            modifier = Modifier.weight(1f)
                        )
                        MacroBadgePill(
                            label = "Fat",
                            amount = "${profile?.targetFat?.toInt() ?: 50}g",
                            accentColor = RoseBlush,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. AI ENGINE STATUS CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PastelLavender.copy(alpha = 0.12f))
                    .border(1.dp, PastelLavender.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PastelLavender),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ObsidianBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "AI Vision & Coach",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Hạn mức hôm nay: ${profile?.dailyAiQuota ?: 50} / 50 lượt chụp",
                            fontSize = 12.sp,
                            color = TextLightGray
                        )
                    }
                }
            }

            // 6. ACTION ROWS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
            ) {
                ActionRowItem(icon = Icons.Default.Edit, label = "Chỉnh sửa chỉ số & mục tiêu", isLast = false)
                ActionRowItem(icon = Icons.Default.Notifications, label = "Nhắc nhở bữa ăn & uống nước", isLast = false)
                ActionRowItem(icon = Icons.Default.Lock, label = "Đổi mật khẩu tài khoản", isLast = true)
            }

            // 7. LOGOUT BUTTON
            Button(
                onClick = {
                    viewModel.logout(onLogout)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralWarning.copy(alpha = 0.15f),
                    contentColor = CoralWarning
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralWarning.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, fontSize = 12.sp, color = TextMuted)
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Text(text = unit, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun MacroBadgePill(
    label: String,
    amount: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalDock)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = amount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
private fun ActionRowItem(
    icon: ImageVector,
    label: String,
    isLast: Boolean
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
            Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = CharcoalBorder.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}
