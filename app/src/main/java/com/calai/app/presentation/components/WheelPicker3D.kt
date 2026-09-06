package com.calai.app.presentation.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.*
import kotlin.math.abs

/**
 * WheelPicker3D — Bộ chọn số dạng cuộn dọc 3D (kiểu iOS NumberPicker / hình trụ nhìn nghiêng).
 *
 * Hiệu ứng thị giác "hình trụ":
 * - Số ở chính giữa: to nhất, rõ nét nhất (alpha 1.0, scale 1.0, rotationX 0°).
 * - Càng xa tâm: nhỏ dần (scale), mờ dần (alpha), nghiêng theo rotationX + cameraDistance
 *   tạo phối cảnh 3D thật (không phải scale phẳng). Khoảng cách dọc co lại khi xa tâm
 *   (foreshortening) nhờ translationY kéo về phía trung tâm.
 * - Chỉ hiển thị ~5 số quanh giá trị trung tâm cùng lúc, các số xa hơn mờ hẳn.
 *
 * Hành vi kéo:
 * - Giai đoạn đang giữ tay: cuộn bám sát 1:1 theo vị trí ngón tay (LazyColumn mặc định).
 * - Giai đoạn sau khi thả tay: quán tính (fling) + snap về đúng số nguyên gần nhất
 *   qua rememberSnapFlingBehavior.
 * - Haptic feedback nhẹ (CLOCK_TICK) mỗi khi giá trị thay đổi 1 đơn vị.
 * - Giới hạn range hợp lệ: không cuộn vượt quá min/max.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker3D(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    formatLabel: (Int) -> String = { it.toString() },
    itemHeight: Dp = 56.dp,
    visibleItemCount: Int = 5,
    isDarkTheme: Boolean = true
) {
    val items = remember(range) { range.toList() }
    val initialIndex = remember(value, range) {
        (value - range.first).coerceIn(0, items.lastIndex)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val view = LocalView.current
    val density = LocalDensity.current

    val halfVisible = visibleItemCount / 2
    val totalHeight = itemHeight * visibleItemCount

    // ── Xác định item ở chính giữa viewport ──
    val centerItemIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewportCenter =
                (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - viewportCenter)
            }?.index ?: initialIndex
        }
    }

    // ── Emit giá trị + haptic khi center thay đổi ──
    var lastEmittedIndex by remember { mutableIntStateOf(initialIndex) }
    LaunchedEffect(centerItemIndex) {
        if (centerItemIndex != lastEmittedIndex && centerItemIndex in items.indices) {
            lastEmittedIndex = centerItemIndex
            onValueChange(items[centerItemIndex])
            try {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            } catch (_: Exception) { /* thiết bị không hỗ trợ */ }
        }
    }

    // ── Đồng bộ khi value bên ngoài thay đổi (không phải do cuộn) ──
    LaunchedEffect(value) {
        val target = (value - range.first).coerceIn(0, items.lastIndex)
        if (!listState.isScrollInProgress && centerItemIndex != target) {
            listState.scrollToItem(target)
        }
    }

    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textMuted = if (isDarkTheme) TextMuted else TextInkMuted
    val highlightBg = if (isDarkTheme) CharcoalCardElevated else PearlBorder.copy(alpha = 0.35f)
    val highlightBorder = if (isDarkTheme) CharcoalBorder else PearlBorder

    Box(
        modifier = modifier.height(totalHeight),
        contentAlignment = Alignment.Center
    ) {
        // ── Khung highlight ở vị trí trung tâm ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight + 2.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(highlightBg)
        )

        // ── Danh sách cuộn dọc ──
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight),
            contentPadding = PaddingValues(vertical = itemHeight * halfVisible),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val isCenter = index == centerItemIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            // Đọc layoutInfo trong graphicsLayer lambda → chỉ redraw, không recompose
                            val info = listState.layoutInfo
                            val viewportCenter =
                                (info.viewportStartOffset + info.viewportEndOffset) / 2f
                            val itemInfo =
                                info.visibleItemsInfo.firstOrNull { it.index == index }

                            if (itemInfo != null) {
                                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                // Khoảng cách chuẩn hóa: 0 = chính giữa, ±1 = 1 item slot
                                val dist =
                                    (itemCenter - viewportCenter) / itemInfo.size.toFloat()
                                val absDist = abs(dist)

                                // 1) Xoay quanh trục X → hiệu ứng hình trụ nhìn nghiêng
                                //    Dấu âm: item phía trên (dist < 0) quay ngửa ra phía trước,
                                //    item phía dưới (dist > 0) quay úp vào phía sau.
                                rotationX = dist * -25f

                                // 2) Camera distance tạo chiều sâu phối cảnh thực
                                cameraDistance = 12f * density.density

                                // 3) Thu nhỏ dần khi xa tâm
                                val s = (1f - absDist * 0.12f).coerceIn(0.55f, 1f)
                                scaleX = s
                                scaleY = s

                                // 4) Mờ dần khi xa tâm
                                alpha = (1f - absDist * 0.28f).coerceIn(0f, 1f)

                                // 5) Kéo về phía tâm → foreshortening (nén khoảng cách dọc)
                                translationY = -dist * 4.5f * density.density
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatLabel(items[index]),
                        fontSize = if (isCenter) 28.sp else 21.sp,
                        fontWeight = if (isCenter) FontWeight.Black else FontWeight.Normal,
                        color = if (isCenter) textPrimary else textMuted.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
