package com.calai.app.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.DockTab
import com.calai.app.presentation.components.FloatingBottomDock
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.StatisticsUiState
import com.calai.app.presentation.viewmodel.StatisticsViewModel
import com.calai.app.presentation.viewmodel.StatsPeriod

@Composable
fun StatisticsScreen(
    onNavigateTab: (DockTab) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                .padding(top = 32.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Tiêu đề
            Text(
                text = "Phân Tích & Xu Hướng",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                letterSpacing = (-0.5).sp
            )

            // Card Xanh Mint Banner (Đúng góc trên ảnh mẫu)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(PastelMint)
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "Phân tích dinh dưỡng tuần này",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDeepInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Theo dõi quy luật calo, nhận diện thói quen để kiểm soát vóc dáng bền vững.",
                        fontSize = 13.sp,
                        color = TextDeepInk.copy(alpha = 0.75f),
                        lineHeight = 18.sp
                    )
                }
            }

            // Segmented Pill Toggle: [ Theo Ngày ] [ Theo Tuần ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CharcoalSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (uiState.period == StatsPeriod.DAILY) VividOrange else Color.Transparent)
                        .clickable { viewModel.setPeriod(StatsPeriod.DAILY) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Theo Ngày",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.period == StatsPeriod.DAILY) TextWhite else TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (uiState.period == StatsPeriod.WEEKLY) VividOrange else Color.Transparent)
                        .clickable { viewModel.setPeriod(StatsPeriod.WEEKLY) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Theo Tuần",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.period == StatsPeriod.WEEKLY) TextWhite else TextMuted
                    )
                }
            }

            // Thẻ Calorie Trends (Màu Pastel Lavender chuẩn ảnh mẫu)
            CalorieTrendsCard(uiState = uiState)

            // Thẻ Macro Distribution
            MacroDistributionCard(uiState = uiState)

            // Thẻ Xu Hướng Cân Nặng (EWMA Trend)
            WeightTrendCard(uiState = uiState)
        }

        // Thanh Dock nổi ở đáy
        FloatingBottomDock(
            currentTab = DockTab.STATISTICS,
            onTabSelected = onNavigateTab,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CalorieTrendsCard(uiState: StatisticsUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PastelLavender)
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = "Xu hướng Calo (Calorie Trends)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDeepInk
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Trung bình: ${uiState.averageCalories} kcal/ngày · Mục tiêu ${uiState.targetCalories} kcal",
                fontSize = 13.sp,
                color = TextDeepInk.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Biểu đồ đường cong calo thực tế (GET /meals/statistics)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                if (uiState.weeklyStats.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val calories = uiState.weeklyStats.map { it.calories }
                        val minCal = minOf(calories.min(), uiState.targetCalories)
                        val maxCal = maxOf(calories.max(), uiState.targetCalories)
                        val range = (maxCal - minCal).takeIf { it > 0 } ?: 1

                        // Trục dọc đảo chiều (calo cao -> gần đỉnh), chừa lề trên/dưới 15%
                        fun yFor(value: Int): Float {
                            val t = (value - minCal).toFloat() / range
                            return height * (0.85f - t * 0.70f)
                        }

                        // Đường mục tiêu đứt nét — đúng vị trí Target Calories thật của người dùng
                        val targetY = yFor(uiState.targetCalories)
                        drawLine(
                            color = TextDeepInk.copy(alpha = 0.25f),
                            start = Offset(0f, targetY),
                            end = Offset(width, targetY),
                            strokeWidth = 2.dp.toPx()
                        )

                        // Vẽ đường cong calo các ngày từ dữ liệu thật
                        val n = uiState.weeklyStats.size
                        val points = uiState.weeklyStats.mapIndexed { index, day ->
                            val x = if (n == 1) width / 2f else width * index / (n - 1).toFloat()
                            Offset(x, yFor(day.calories))
                        }

                        val path = Path()
                        path.moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val midX = (prev.x + curr.x) / 2
                            path.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }

                        drawPath(
                            path = path,
                            color = TextDeepInk,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Vẽ các điểm mút
                        points.forEach { pt ->
                            drawCircle(color = TextDeepInk, radius = 4.dp.toPx(), center = pt)
                        }
                    }
                }
            }

            // Nhãn thứ — lấy đúng thứ thật trong tuần của từng ngày dữ liệu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                uiState.weeklyStats.forEach { day ->
                    Text(day.dayLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDeepInk.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TextDeepInk)
                )
                Text(
                    text = "${uiState.daysUnderGoal} ngày đạt dưới calo mục tiêu",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDeepInk
                )
            }
        }
    }
}

@Composable
private fun MacroDistributionCard(uiState: StatisticsUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CharcoalSurface)
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = "Phân bổ nhóm chất dinh dưỡng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Duy trì tỉ lệ đạm cao giúp bảo vệ khối cơ bắp khi thâm hụt calo.",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroSharePill("Đạm (Protein)", "${uiState.proteinPercent}%", PastelMint, Modifier.weight(1f))
                MacroSharePill("Carb", "${uiState.carbPercent}%", PastelButtercup, Modifier.weight(1f))
                MacroSharePill("Chất béo", "${uiState.fatPercent}%", PastelRose, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MacroSharePill(label: String, percent: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(percent, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextDeepInk)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDeepInk.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun WeightTrendCard(uiState: StatisticsUiState) {
    val diff = uiState.weightChangedKg
    val diffSign = if (diff <= 0) "" else "+"
    val diffFormatted = String.format("%.1f", diff)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CharcoalSurface)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Xu hướng cân nặng EWMA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Làm mịn biến động nước cơ thể",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Surface(
                    color = if (diff <= 0) EmeraldSuccess.copy(alpha = 0.15f) else CoralWarning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$diffSign$diffFormatted kg",
                        color = if (diff <= 0) EmeraldSuccess else CoralWarning,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sparkline đường Trend Weight thật (EWMA, alpha = 0.1) từ GET /weight-logs/trend
            if (uiState.weightTrendPoints.size >= 2) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val values = uiState.weightTrendPoints.map { it.trendWeight }
                    val minV = values.min()
                    val maxV = values.max()
                    val range = (maxV - minV).takeIf { it > 0f } ?: 1f
                    val n = values.size

                    val points = values.mapIndexed { index, v ->
                        val x = width * index / (n - 1).toFloat()
                        val y = height * (0.9f - ((v - minV) / range) * 0.8f)
                        Offset(x, y)
                    }

                    val path = Path()
                    path.moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val midX = (prev.x + curr.x) / 2
                        path.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }

                    drawPath(path = path, color = VividOrange, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    drawCircle(color = VividOrange, radius = 4.dp.toPx(), center = points.last())
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bắt đầu", fontSize = 11.sp, color = TextMuted)
                    Text("${uiState.startWeight} kg", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hiện tại", fontSize = 11.sp, color = TextMuted)
                    Text("${uiState.currentWeight} kg", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = VividOrange)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mục tiêu", fontSize = 11.sp, color = TextMuted)
                    Text("${uiState.targetWeight} kg", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PastelLavender)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Đã hoàn thành ${uiState.weightProgressPercent}% mục tiêu cân nặng",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
