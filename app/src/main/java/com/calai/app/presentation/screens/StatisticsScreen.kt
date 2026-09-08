package com.calai.app.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
<<<<<<< HEAD
=======

>>>>>>> ef4ce2c1a328dabd7c45f614d2da5cc1a5e4487a
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.InsightDto
import com.calai.app.presentation.components.DockTab
import com.calai.app.presentation.components.FloatingBottomDock
import com.calai.app.presentation.components.MacroDonutChart
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.StatisticsUiState
import com.calai.app.presentation.viewmodel.StatisticsViewModel
import com.calai.app.presentation.viewmodel.StatsPeriod

@Composable
fun StatisticsScreen(
    onNavigateTab: (DockTab) -> Unit,
    onNavigateToWeightHistory: () -> Unit = {},
    isDarkTheme: Boolean = true,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                .padding(top = 32.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Tiêu đề
            Text(
                text = "Phân Tích & Xu Hướng",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) TextWhite else TextInkPrimary,
                letterSpacing = (-0.5).sp
            )

            // Card Xanh Mint Banner với Shadow nổi khối
            val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkTheme) 4.dp else 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (isDarkTheme) PastelMint else ProteinGradientStartLight)
                    .border(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
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

            // Segmented Pill Toggle: 5 preset thời gian (Tuần/Tháng/Quý/Năm/Tất cả)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .shadow(
                        elevation = if (isDarkTheme) 2.dp else 4.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDarkTheme) CharcoalSurface else PearlCard)
                    .border(1.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                StatsPeriod.values().forEach { period ->
                    val isSelected = uiState.period == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) VividOrange else Color.Transparent)
                            .clickable { viewModel.setPeriod(period) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TextWhite else if (isDarkTheme) TextMuted else TextInkMuted
                        )
                    }
                }
            }

            // Thẻ Insight tự động (Plateau Detection / Goal Deviation) — chỉ hiện khi có insight
            if (uiState.insights.isNotEmpty()) {
                InsightCard(insights = uiState.insights, isDarkTheme = isDarkTheme)
            }

            // Thẻ Calorie Trends (Màu Pastel Lavender chuẩn ảnh mẫu)
            CalorieTrendsCard(uiState = uiState, isDarkTheme = isDarkTheme)

            // Thẻ Macro Distribution
            MacroDistributionCard(uiState = uiState, isDarkTheme = isDarkTheme)

            // Thẻ Xu Hướng Cân Nặng (EWMA Trend)
            WeightTrendCard(uiState = uiState, isDarkTheme = isDarkTheme, onNavigateToHistory = onNavigateToWeightHistory)
        }

        // Thanh Dock nổi ở đáy
        FloatingBottomDock(
            currentTab = DockTab.STATISTICS,
            onTabSelected = onNavigateTab,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun InsightCard(insights: List<InsightDto>, isDarkTheme: Boolean = true) {
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 4.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDarkTheme) PastelButtercup.copy(alpha = 0.18f) else CarbGradientStartLight.copy(alpha = 0.35f))
            .border(1.dp, if (isDarkTheme) PastelButtercup.copy(alpha = 0.35f) else CarbGradientStartLight, RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        insights.forEach { insight ->
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    imageVector = if (insight.type == "PLATEAU") Icons.AutoMirrored.Filled.TrendingFlat else Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = if (isDarkTheme) TextWhite else TextInkPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = insight.message,
                    fontSize = 12.8.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 17.sp,
                    color = if (isDarkTheme) TextWhite else TextInkPrimary
                )
            }
        }
    }
}

@Composable
private fun CalorieTrendsCard(uiState: StatisticsUiState, isDarkTheme: Boolean = true) {
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 6.dp else 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDarkTheme) PastelLavender else LavenderGradientStartLight)
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.65f), Color.Transparent)
                ),
                shape = RoundedCornerShape(24.dp)
            )
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

            // Biểu đồ đường cong calo thực tế (GET /meals/statistics) — animate vẽ dần khi đổi preset/dữ liệu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                if (uiState.weeklyStats.isNotEmpty()) {
                    val drawProgress = remember { Animatable(0f) }
                    LaunchedEffect(uiState.weeklyStats) {
                        drawProgress.snapTo(0f)
                        drawProgress.animateTo(1f, animationSpec = tween(durationMillis = 700, easing = LinearEasing))
                    }

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

                        // Cắt path theo tiến độ animate (đường "tự vẽ" từ trái sang phải)
                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(path, false)
                        val animatedPath = Path()
                        pathMeasure.getSegment(0f, pathMeasure.length * drawProgress.value, animatedPath, true)

                        // Gradient fill phía dưới đường cong
                        val fillPath = Path().apply {
                            addPath(animatedPath)
                            lineTo(width * drawProgress.value, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(TextDeepInk.copy(alpha = 0.20f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        drawPath(
                            path = animatedPath,
                            color = TextDeepInk,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Vẽ các điểm mút đã "lộ ra" theo tiến độ animate
                        val visibleCount = (points.size * drawProgress.value).toInt().coerceIn(0, points.size)
                        points.take(visibleCount).forEach { pt ->
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
private fun MacroDistributionCard(uiState: StatisticsUiState, isDarkTheme: Boolean = true) {
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 4.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDarkTheme) CharcoalSurface else PearlCard)
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White,
                        if (isDarkTheme) CharcoalBorder else PearlBorder
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = "Phân bổ nhóm chất dinh dưỡng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) TextWhite else TextInkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Duy trì tỉ lệ đạm cao giúp bảo vệ khối cơ bắp khi thâm hụt calo.",
                fontSize = 12.sp,
                color = if (isDarkTheme) TextMuted else TextInkMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                MacroDonutChart(
                    proteinPercent = uiState.proteinPercent,
                    carbPercent = uiState.carbPercent,
                    fatPercent = uiState.fatPercent,
                    centerLabel = "${uiState.averageCalories}",
                    isDarkTheme = isDarkTheme
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MacroLegendRow("Đạm (Protein)", "${uiState.proteinPercent}%", if (isDarkTheme) PastelMint else ProteinGradientStartLight, isDarkTheme)
                    MacroLegendRow("Carb", "${uiState.carbPercent}%", if (isDarkTheme) PastelButtercup else CarbGradientStartLight, isDarkTheme)
                    MacroLegendRow("Chất béo", "${uiState.fatPercent}%", if (isDarkTheme) PastelRose else FatGradientStartLight, isDarkTheme)
                }
            }
        }
    }
}

@Composable
private fun MacroLegendRow(label: String, percent: String, dotColor: Color, isDarkTheme: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) TextMuted else TextInkMuted
        )
        Text(
            text = percent,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDarkTheme) TextWhite else TextInkPrimary
        )
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
private fun WeightTrendCard(uiState: StatisticsUiState, isDarkTheme: Boolean = true, onNavigateToHistory: () -> Unit = {}) {
    val unitLabel = if (uiState.weightUnit == "lb") "lbs" else "kg"
    val diff = UserPreferencesManager.convertKg(uiState.weightChangedKg, uiState.weightUnit)
    val diffSign = if (diff <= 0) "" else "+"
    val diffFormatted = String.format(java.util.Locale.US, "%.1f", diff)
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

    val startWeightDisplay = UserPreferencesManager.formatWeight(uiState.startWeight, uiState.weightUnit)
    val currentWeightDisplay = UserPreferencesManager.formatWeight(uiState.currentWeight, uiState.weightUnit)
    val targetWeightDisplay = UserPreferencesManager.formatWeight(uiState.targetWeight, uiState.weightUnit)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDarkTheme) 4.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDarkTheme) CharcoalSurface else PearlCard)
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White,
                        if (isDarkTheme) CharcoalBorder else PearlBorder
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
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
                        color = if (isDarkTheme) TextWhite else TextInkPrimary
                    )
                    Text(
                        text = "Làm mịn biến động nước cơ thể",
                        fontSize = 12.sp,
                        color = if (isDarkTheme) TextMuted else TextInkMuted
                    )
                }
                Surface(
                    color = if (diff <= 0) EmeraldSuccess.copy(alpha = 0.15f) else CoralWarning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$diffSign$diffFormatted $unitLabel",
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
                    Text("Bắt đầu", fontSize = 11.sp, color = if (isDarkTheme) TextMuted else TextInkMuted)
                    Text(
                        startWeightDisplay,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hiện tại", fontSize = 11.sp, color = if (isDarkTheme) TextMuted else TextInkMuted)
                    Text(currentWeightDisplay, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = VividOrange)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mục tiêu", fontSize = 11.sp, color = if (isDarkTheme) TextMuted else TextInkMuted)
                    Text(targetWeightDisplay, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PastelLavender)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Đã hoàn thành ${uiState.weightProgressPercent}% mục tiêu cân nặng",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkTheme) TextMuted else TextInkMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Xem lịch sử →",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = VividOrange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToHistory),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
