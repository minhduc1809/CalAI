package com.calai.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.DarkShadow
import com.calai.app.presentation.theme.TextWhite
import com.calai.app.presentation.theme.VividOrange
import com.calai.app.presentation.theme.VividOrangeDark
import com.calai.app.presentation.theme.VividOrangeGlow
import com.calai.app.presentation.theme.WarmShadow

/**
 * CTA chính "tactile" dùng chung cho toàn app (Part 4.1 — CalAI_FINAL_Design_Code_Rules v3):
 * - Pressed: scale ~0.97, translationY 1-2dp, alpha giảm nhẹ, elevation giảm nhẹ,
 *   animation 120-180ms FastOutSlowIn — tactile chứ không skeuomorphic (không bevel/metallic/shadow dày).
 * - Mặc định: gradient 2 tông cùng họ (`VividOrange` → `VividOrangeDark`) + soft shadow + glow rất nhẹ.
 * - Hỗ trợ trạng thái loading (Part 4.8/4.9): khoá `enabled` và hiện spinner khi `isLoading = true`
 *   để chặn duplicate submit.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    gradientColors: List<Color> = listOf(VividOrange, VividOrangeDark),
    glowColor: Color = VividOrangeGlow,
    contentColor: Color = TextWhite,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    height: androidx.compose.ui.unit.Dp = 54.dp,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val isEnabled = enabled && !isLoading
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "app_button_scale"
    )
    val translationY by animateDpAsState(
        targetValue = if (isPressed && isEnabled) 1.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "app_button_translation_y"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed && isEnabled) 3.dp else (if (isDarkTheme) 6.dp else 9.dp),
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "app_button_elevation"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (!isEnabled) 0.5f else if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "app_button_alpha"
    )

    val shadowColor = if (isEnabled) glowColor else (if (isDarkTheme) DarkShadow else WarmShadow)
    val brush = if (isEnabled) {
        Brush.horizontalGradient(gradientColors)
    } else {
        Brush.horizontalGradient(gradientColors.map { it.copy(alpha = 0.35f) })
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY.toPx()
            }
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(brush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                onClick = onClick
            )
            .alpha(contentAlpha),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Box(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = contentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
