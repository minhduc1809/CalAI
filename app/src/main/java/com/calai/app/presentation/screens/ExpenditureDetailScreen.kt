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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ExpenditureDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenditureDetailScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: ExpenditureDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val expenditure = uiState.expenditure
    val profile = uiState.profile

    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    val isUpdating = expenditure?.status == "UPDATING"

    val currentExpenditure = expenditure?.estimatedExpenditure?.toInt()
        ?: profile?.adaptiveExpenditure?.toInt()
        ?: profile?.tdee?.toInt()
        ?: 2280

    val staticTdee = expenditure?.staticTdee?.toInt()
        ?: profile?.tdee?.toInt()
        ?: 2310

    val bmr = profile?.bmr?.toInt() ?: 1680

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
            // 1. TOP HEADER & BACK
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
                        text = "Chi Tiết Tiêu Hao (Expenditure)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Thuật toán ước tính trao đổi chất thích ứng",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. HERO CARD - EXPENDITURE NỔI BẬT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkTheme) 8.dp else 12.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (isDarkTheme) listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
                            else listOf(PearlCard, IvoryBackground)
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(26.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusColor = if (isUpdating) PastelMint else PastelLavender
                        Surface(
                            color = statusColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Text(
                                    text = if (isUpdating) "ĐANG CẬP NHẬT (UPDATING)" else "ĐANG GIỮ (HOLDING)",
                                    color = statusColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (expenditure?.method == "ADAPTIVE") "Thuật toán Thích ứng" else "Công thức Tĩnh",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VividOrange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Năng lượng tiêu hao thực tế",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$currentExpenditure",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = VividOrange,
                                    letterSpacing = (-1).sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "kcal / ngày",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // So sánh với TDEE tĩnh ban đầu
                        Column(horizontalAlignment = Alignment.End) {
                            val diff = currentExpenditure - staticTdee
                            val diffText = if (diff >= 0) "+$diff" else "$diff"
                            Text(
                                text = "TDEE ước tính tĩnh",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$staticTdee kcal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "($diffText kcal/ngày)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (diff >= 0) PastelMint else CoralWarning
                            )
                        }
                    }

                    HorizontalDivider(
                        color = (MaterialTheme.colorScheme.outline).copy(alpha = 0.5f),
                        thickness = 1.dp
                    )

                    Text(
                        text = expenditure?.message ?: "Expenditure được tính toán tự động qua hồi quy năng lượng giữa cân nặng thực tế và calo bạn đã nạp.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // 3. CÁC THÀNH TỐ CỦA NĂNG LƯỢNG TIÊU HAO (The 4 Components of TDEE)
            Text(
                text = "Phân tích 4 thành tố tiêu hao năng lượng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // 4 Bento Cards cho BMR, NEAT, TEF, EAT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnergyComponentCard(
                    title = "BMR (Chuyển hóa cơ bản)",
                    percent = "60 - 70%",
                    calories = "${bmr} kcal",
                    desc = "Duy trì hoạt động tim, não, phổi và tế bào khi nghỉ ngơi.",
                    accentColor = ProteinGradientStart,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )

                EnergyComponentCard(
                    title = "NEAT (Vận động tự nhiên)",
                    percent = "15 - 20%",
                    calories = "~${(currentExpenditure * 0.18f).toInt()} kcal",
                    desc = "Đi bộ, làm việc, dọn dẹp, sinh hoạt không phải tập thể thao.",
                    accentColor = CarbGradientStart,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnergyComponentCard(
                    title = "TEF (Hiệu ứng nhiệt thức ăn)",
                    percent = "~10%",
                    calories = "~${(currentExpenditure * 0.10f).toInt()} kcal",
                    desc = "Năng lượng cơ thể dùng để tiêu hóa, hấp thu thức ăn (nhất là Protein).",
                    accentColor = FatGradientStart,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )

                EnergyComponentCard(
                    title = "EAT (Tập luyện thể thao)",
                    percent = "5 - 15%",
                    calories = "~${(currentExpenditure * 0.10f).toInt()} kcal",
                    desc = "Calo đốt cháy trực tiếp qua các buổi tập gym, cardio, chạy bộ.",
                    accentColor = PastelLavender,
                    isDark = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. THÔNG SỐ HỒI QUY THỰC TẾ (Regression Window)
            Text(
                text = "Dữ liệu thực tế trong cửa sổ hồi quy",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

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
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    RegressionDataRow(
                        label = "Cửa sổ theo dõi hồi quy",
                        value = "${expenditure?.windowDays ?: 14} ngày gần nhất",
                        isDark = isDarkTheme
                    )
                    RegressionDataRow(
                        label = "Số ngày đã ghi log cân nặng",
                        value = "${expenditure?.weightLogsCount ?: 0} ngày",
                        isDark = isDarkTheme
                    )
                    RegressionDataRow(
                        label = "Số ngày đã ghi log bữa ăn",
                        value = "${expenditure?.loggedDaysCount ?: 0} ngày",
                        isDark = isDarkTheme
                    )
                    RegressionDataRow(
                        label = "Calo nạp trung bình mỗi ngày",
                        value = "${expenditure?.avgDailyCaloriesConsumed?.toInt() ?: 1850} kcal/ngày",
                        isDark = isDarkTheme
                    )
                    if (expenditure?.trendWeightStart != null && expenditure.trendWeightEnd != null) {
                        val startStr = UserPreferencesManager.formatWeight(expenditure.trendWeightStart, uiState.weightUnit)
                        val endStr = UserPreferencesManager.formatWeight(expenditure.trendWeightEnd, uiState.weightUnit)

                        RegressionDataRow(
                            label = "Biến động cân nặng xu hướng",
                            value = "$startStr → $endStr",
                            isDark = isDarkTheme
                        )
                    }
                }
            }

            // 5. CẨM NANG: CÁCH HIỂU TRẠNG THÁI UPDATING & HOLDING
            Text(
                text = "Cách hoạt động của hệ thống thích ứng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            StatusExplanationCard(
                icon = Icons.Default.Autorenew,
                title = "Trạng thái ĐANG CẬP NHẬT (UPDATING)",
                description = "Khi bạn log cân nặng và bữa ăn đều đặn, thuật toán sẽ tự động điều chỉnh TDEE mỗi tuần dựa trên tốc độ tăng/giảm cân thực tế so với calo nạp vào.",
                accentColor = PastelMint,
                isDark = isDarkTheme
            )

            StatusExplanationCard(
                icon = Icons.Default.PauseCircleOutline,
                title = "Trạng thái ĐANG GIỮ (HOLDING)",
                description = "Khi thiếu dữ liệu cân nặng hoặc bữa ăn quá 4 ngày, hệ thống sẽ tạm ngưng điều chỉnh và giữ mức Expenditure ổn định gần nhất để bảo vệ bạn khỏi việc cắt giảm calo sai lệch.",
                accentColor = PastelLavender,
                isDark = isDarkTheme
            )
        }
    }
}

@Composable
private fun EnergyComponentCard(
    title: String,
    percent: String,
    calories: String,
    desc: String,
    accentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 4.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isDark) DarkShadow else WarmShadow,
                spotColor = if (isDark) DarkShadow else WarmShadow
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = percent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = calories,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = desc,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun RegressionDataRow(
    label: String,
    value: String,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun StatusExplanationCard(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = description,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
