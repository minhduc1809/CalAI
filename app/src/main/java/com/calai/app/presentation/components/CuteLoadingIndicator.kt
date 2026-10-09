package com.calai.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Hiệu ứng loading "cute" bằng Lottie (CalAI_FINAL_Design_Code_Rules.md Phần 12).
 *
 * CHỈ dùng cho các khoảnh khắc chờ "cảm xúc" — splash, AI đang phân tích, pull-to-refresh,
 * sau khi bấm Save ở bước quan trọng. KHÔNG dùng thay Skeleton cho List/History/Chart đang
 * tải (Phần 12.4) — những màn đó vẫn phải dùng Skeleton mô phỏng đúng layout thật.
 *
 * Dùng đúng 1 animation cố định cho mỗi ngữ cảnh (không đổi tuỳ hứng — Phần 12.3), lấy từ
 * `res/raw/loading_<ten>.json`/`.lottie`.
 */
@Composable
fun CuteLoadingIndicator(
    rawResId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawResId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    Box(modifier = modifier.size(size)) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
        )
    }
}
