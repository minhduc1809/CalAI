package com.calai.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.calai.app.presentation.theme.CtaSolidOrange
import com.calai.app.presentation.theme.VividOrange
import com.calai.app.presentation.theme.VividOrangeLight

/**
 * Nút trợ lý AI dạng linh vật chatbot (đầu robot có ăng-ten, hai mắt) nổi ở góc dưới, nhấp nhô nhẹ lên xuống
 * để gợi ý "bấm vào để hỏi". Chuyển động chậm (~2,4 giây/nhịp, biên độ 5dp) để không gây xao nhãng.
 */
@Composable
fun ChatbotFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "chatbot-bob")
    val bob by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 3600
                1f at 0
                1f at 3300
                0.1f at 3420
                1f at 3540
            },
            RepeatMode.Restart
        ),
        label = "blink"
    )

    Box(
        modifier = modifier
            .offset(y = bob.dp)
            .size(50.dp)
            .shadow(10.dp, CircleShape, ambientColor = CtaSolidOrange.copy(alpha = 0.35f), spotColor = CtaSolidOrange.copy(alpha = 0.45f))
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(VividOrangeLight, VividOrange)))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .semantics { contentDescription = "Mở trợ lý dinh dưỡng AI" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(29.dp)) {
            val w = size.width
            val h = size.height
            // ăng-ten
            drawLine(Color.White, Offset(w / 2, h * 0.08f), Offset(w / 2, h * 0.22f), strokeWidth = w * 0.06f)
            drawCircle(Color.White, radius = w * 0.07f, center = Offset(w / 2, h * 0.07f))
            // đầu
            drawRoundRect(Color.White, topLeft = Offset(w * 0.08f, h * 0.22f), size = Size(w * 0.84f, h * 0.66f), cornerRadius = CornerRadius(w * 0.24f))
            // tai
            drawRoundRect(Color.White, topLeft = Offset(0f, h * 0.42f), size = Size(w * 0.08f, h * 0.26f), cornerRadius = CornerRadius(w * 0.04f))
            drawRoundRect(Color.White, topLeft = Offset(w * 0.92f, h * 0.42f), size = Size(w * 0.08f, h * 0.26f), cornerRadius = CornerRadius(w * 0.04f))
            // mắt (thỉnh thoảng chớp)
            val eyeH = h * 0.16f * blink
            val eyeY = h * 0.48f - eyeH / 2
            drawRoundRect(CtaSolidOrange, topLeft = Offset(w * 0.27f, eyeY), size = Size(w * 0.13f, eyeH), cornerRadius = CornerRadius(w * 0.065f))
            drawRoundRect(CtaSolidOrange, topLeft = Offset(w * 0.60f, eyeY), size = Size(w * 0.13f, eyeH), cornerRadius = CornerRadius(w * 0.065f))
            // miệng cười
            val mouth = Path().apply {
                moveTo(w * 0.36f, h * 0.67f)
                quadraticBezierTo(w * 0.5f, h * 0.77f, w * 0.64f, h * 0.67f)
            }
            drawPath(mouth, CtaSolidOrange, style = Stroke(width = w * 0.05f, cap = StrokeCap.Round))
        }
    }
}
