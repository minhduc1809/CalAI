package com.calai.app.presentation.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.DarkShadow
import com.calai.app.presentation.theme.PearlBorder
import com.calai.app.presentation.theme.WarmShadow
import kotlin.math.PI
import kotlin.math.sin

/** Toàn bộ số liệu lấy từ BE; null = chưa có dữ liệu thật (hiển thị "--", không điền số giả). */
data class DashboardStats(
    val weightText: String?, val targetWeightText: String?, val weightProgress: Float?,
    val proteinG: Int?, val proteinTarget: Int?,
    val carbG: Int?, val carbTarget: Int?,
    val fatG: Int?, val fatTarget: Int?
)

private val TileShape = RoundedCornerShape(22.dp)
private val TileHeight = 156.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TileFrame(
    bg: Color,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    // Shadow mềm, màu nhuốm tông nền (không đen) — để tile có cảm giác "nổi nhẹ" thay vì phẳng
    // tuyệt đối, đúng tinh thần premium card (floating slightly above background).
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    Box(
        modifier = modifier
            .height(TileHeight)
            .shadow(elevation = 4.dp, shape = TileShape, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(TileShape)
            .background(bg)
            .then(if (onClick != null) Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick) else Modifier),
        content = content
    )
}

/**
 * Bố cục chi tiết của 1 thẻ: hàng đầu [icon tròn + tên ..... % tiến độ], giữa là số to kèm đơn vị nhỏ,
 * dưới cùng là dòng mục tiêu. Sóng nằm dải dưới cùng nên không đè chữ.
 */
@Composable
private fun TileTexts(
    title: String,
    icon: ImageVector,
    valueText: String,
    subText: String,
    pillText: String,
    ink: Color,
    accent: Color,
    dark: Boolean,
    modifier: Modifier = Modifier
) {
    val rawNumber = valueText.substringBefore(" ")
    val unit = if (valueText.contains(" ")) valueText.substringAfter(" ") else ""
    val targetFloat = rawNumber.toFloatOrNull()
    val hasDecimal = rawNumber.contains(".")
    val animated by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetFloat ?: 0f,
        animationSpec = androidx.compose.animation.core.tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "tile_count_up"
    )
    // Hiệu ứng "chạy số" đếm từ 0 lên giá trị thật mỗi khi dữ liệu BE thay đổi — chỉ áp dụng khi
    // parse được số (không áp cho "--" lúc chưa có dữ liệu, tránh hiểu nhầm là số 0 thật).
    val number = if (targetFloat != null) {
        if (hasDecimal) "%.1f".format(animated) else "${animated.toInt()}"
    } else rawNumber

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(accent.copy(alpha = 0.32f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = ink.copy(alpha = 0.85f), modifier = Modifier.size(16.dp)) }
            Text(
                title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ink.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f), maxLines = 1
            )
            Box(
                modifier = Modifier.clip(RoundedCornerShape(999.dp))
                    .background(if (dark) accent.copy(alpha = 0.18f) else Color.White)
                    .border(1.dp, accent.copy(alpha = if (dark) 0f else 0.9f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) { Text(pillText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (dark) ink else accent) }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(number, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = ink, maxLines = 1, letterSpacing = (-1).sp)
            if (unit.isNotEmpty()) {
                Text(
                    unit, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ink.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
                )
            }
        }
        Text(
            subText, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = ink.copy(alpha = 0.65f),
            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DashboardStatTiles(stats: DashboardStats, isDarkTheme: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroGaugeTile(
                "Protein", TileArt.PROTEIN, stats.proteinG?.let { "$it g" },
                stats.proteinTarget?.let { "Mục tiêu $it g" }, ratioOf(stats.proteinG, stats.proteinTarget),
                Color(0xFF7FBF98), Color(0xFFE8F4EC), Color(0xFF2E4A3B), isDarkTheme, Modifier.weight(1f)
            )
            MacroGaugeTile(
                "Carbs", TileArt.CARBS, stats.carbG?.let { "$it g" },
                stats.carbTarget?.let { "Mục tiêu $it g" }, ratioOf(stats.carbG, stats.carbTarget),
                Color(0xFFEDB56B), Color(0xFFFCF1DF), Color(0xFF5A4325), isDarkTheme, Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroGaugeTile(
                "Chất béo", TileArt.FAT, stats.fatG?.let { "$it g" },
                stats.fatTarget?.let { "Mục tiêu $it g" }, ratioOf(stats.fatG, stats.fatTarget),
                Color(0xFFEE9DB0), Color(0xFFFBE9EE), Color(0xFF5A2E3C), isDarkTheme, Modifier.weight(1f)
            )
            MacroGaugeTile(
                "Cân nặng", TileArt.WEIGHT, stats.weightText,
                stats.targetWeightText?.let { "Mục tiêu $it" } ?: "Chưa đặt mục tiêu", stats.weightProgress ?: 0f,
                Color(0xFF9E8EE0), Color(0xFFEEEAFB), Color(0xFF3A3265), isDarkTheme, Modifier.weight(1f)
            )
        }
    }
}

private fun ratioOf(value: Int?, target: Int?): Float =
    if (value != null && target != null && target > 0) (value.toFloat() / target).coerceIn(0f, 1f) else 0f

// ── Macro (Protein / Carbs / Chất béo): cung bán nguyệt + icon, % theo mục tiêu thật ──

enum class TileArt { PROTEIN, CARBS, FAT, WEIGHT }

/** Mỗi thẻ có "tính cách sóng" riêng: biên độ, tần số và tốc độ trôi khác nhau. */
private data class WaveStyle(val amp: Float, val freq: Float, val periodMs: Int, val bubbles: Boolean = false)

private fun waveStyleOf(art: TileArt) = when (art) {
    TileArt.PROTEIN -> WaveStyle(amp = 0.055f, freq = 1.4f, periodMs = 4200)
    TileArt.CARBS -> WaveStyle(amp = 0.075f, freq = 0.9f, periodMs = 5600)
    TileArt.FAT -> WaveStyle(amp = 0.04f, freq = 1.8f, periodMs = 3600, bubbles = true)
    TileArt.WEIGHT -> WaveStyle(amp = 0.05f, freq = 1.1f, periodMs = 4800)
}

@Composable
private fun MacroGaugeTile(
    title: String,
    art: TileArt,
    valueText: String?,
    subText: String?,
    ratio: Float,
    accent: Color,
    lightBg: Color,
    lightInk: Color,
    dark: Boolean,
    modifier: Modifier
) {
    val progress by animateFloatAsState(ratio, tween(1100), label = "wave_level_$title")
    val infinite = rememberInfiniteTransition(label = "wave_$title")
    val style = waveStyleOf(art)
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(style.periodMs, easing = LinearEasing)),
        label = "wave_phase_$title"
    )
    val bubblePhase by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "bubble_phase_$title"
    )
    // Mực sóng: luôn còn một dải sóng mỏng ở đáy (trang trí), phần dâng lên tỉ lệ thuận với % thật.
    val level = 0.13f + 0.25f * progress

    TileFrame(if (dark) accent.copy(alpha = 0.14f) else lightBg, modifier, isDarkTheme = dark) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun layer(top: Float, amp: Float, freq: Float, ph: Float, color: Color) {
                val path = Path().apply {
                    moveTo(0f, h)
                    var x = 0f
                    while (x <= w) {
                        lineTo(x, top + amp * sin(2.0 * PI * x / w * freq + ph).toFloat())
                        x += 3f
                    }
                    lineTo(w, h)
                    close()
                }
                drawPath(path, color)
            }
            val top = h * (1f - level)
            val amp = h * style.amp
            layer(top - amp * 0.4f, amp * 1.2f, style.freq * 0.8f, -phase + 1.3f, accent.copy(alpha = 0.18f))
            layer(top + amp * 0.3f, amp, style.freq, phase, accent.copy(alpha = 0.30f))
            layer(top + amp * 1.1f, amp * 0.8f, style.freq * 1.3f, -phase * 2f + 3.1f, accent.copy(alpha = 0.42f))

            if (style.bubbles) {
                repeat(3) { i ->
                    val t = (bubblePhase + i / 3f) % 1f
                    val bx = w * (0.62f + 0.13f * i) + 5.dp.toPx() * sin(2.0 * PI * (t + i)).toFloat()
                    val by = h - (h - top) * t
                    drawCircle(Color.White.copy(alpha = 0.55f * (1f - t)), (3f + 2f * i).dp.toPx(), Offset(bx, by))
                }
            }
        }
        TileTexts(
            title = title,
            icon = when (art) {
                TileArt.PROTEIN -> Icons.Default.EggAlt
                TileArt.CARBS -> Icons.Default.BakeryDining
                TileArt.FAT -> Icons.Default.WaterDrop
                TileArt.WEIGHT -> Icons.Default.MonitorWeight
            },
            valueText = valueText ?: "--",
            subText = subText ?: "Đang tải...",
            pillText = if (valueText != null) "${(ratio * 100).toInt()}%" else "--",
            ink = if (dark) Color.White else lightInk,
            accent = accent,
            dark = dark
        )
    }
}

// ── Hero Calories: cung bán nguyệt lớn, số kcal còn lại ở giữa ─────────────

@Composable
fun CalorieHeroCard(
    consumed: Int?,
    target: Int?,
    remaining: Int?,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = Color(0xFFFF6B3D)
    val ratio = if (consumed != null && target != null && target > 0) (consumed.toFloat() / target).coerceIn(0f, 1f) else 0f
    val progress by animateFloatAsState(ratio, tween(900), label = "hero_cal_p")
    val ink = if (isDarkTheme) Color.White else Color(0xFF14151C)
    val muted = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color(0xFF7B7F92)
    val shape = RoundedCornerShape(28.dp)
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    fun fmt(v: Int?) = v?.let { "%,d".format(java.util.Locale("vi", "VN"), it) } ?: "--"
    // Hiệu ứng "chạy số" — kcal còn lại đếm từ 0 lên giá trị thật mỗi khi đổi ngày/log bữa mới.
    val animatedRemaining by animateFloatAsState(
        targetValue = remaining?.toFloat() ?: 0f,
        animationSpec = tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "hero_remaining_count_up"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // Ambient glow orb — rất nhẹ, blur lớn, chỉ tạo chiều sâu phía sau hero card, không che nội dung.
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-40).dp)
                .blur(70.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = if (isDarkTheme) 0.10f else 0.07f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 10.dp, shape = shape, ambientColor = shadowColor, spotColor = shadowColor)
                .clip(shape)
                .background(if (isDarkTheme) androidx.compose.material3.MaterialTheme.colorScheme.surface else Color.White)
                .border(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.06f) else PearlBorder, shape)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(9.dp).clip(androidx.compose.foundation.shape.CircleShape).background(accent))
                Text("Calories", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ink)
            }
            Box(
                Modifier.clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.16f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Đã nạp: ${consumed ?: "--"} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(150.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 20.dp.toPx()
                val radius = minOf((size.width - stroke) / 2f, size.height - stroke)
                val center = Offset(size.width / 2f, size.height - stroke / 2f)
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)
                drawArc(
                    color = accent.copy(alpha = 0.12f), startAngle = 180f, sweepAngle = 180f, useCenter = false,
                    topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
                )
                if (progress > 0.005f) {
                    drawArc(
                        color = accent, startAngle = 180f, sweepAngle = 180f * progress, useCenter = false,
                        topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 26.dp)) {
                if (target == null && consumed != null) {
                    // Chưa có mục tiêu thật: không vẽ số giả, mời hoàn tất hồ sơ
                    Text("Chưa có mục tiêu", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ink)
                    Text("Hoàn tất hồ sơ để có mục tiêu hằng ngày", fontSize = 13.sp, color = muted)
                } else {
                    // Ăn vượt: hiện phần vượt (số dương), nhãn trung tính, không phán xét
                    val over = remaining != null && remaining < 0
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (remaining != null) fmt(kotlin.math.abs(animatedRemaining.toInt())) else "--",
                            fontSize = 48.sp, fontWeight = FontWeight.Black, color = ink, letterSpacing = (-1).sp
                        )
                        Text(" kcal", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = muted, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Text(if (over) "Vượt mục tiêu hôm nay" else "Còn lại hôm nay", fontSize = 14.sp, color = muted)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0 kcal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = muted)
            Text("Mục tiêu: ${target ?: "--"} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ink.copy(alpha = 0.75f))
        }
        }
    }
}
