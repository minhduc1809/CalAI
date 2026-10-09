package com.calai.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.calai.app.presentation.theme.AppElevation
import com.calai.app.presentation.theme.CtaSolidOrange
import com.calai.app.presentation.theme.ShadowCtaAmbient
import com.calai.app.presentation.theme.ShadowCtaDisabled
import com.calai.app.presentation.theme.ShadowCtaSpot
import com.calai.app.presentation.theme.ShadowLightL2Ambient
import com.calai.app.presentation.theme.ShadowLightL2Spot
import com.calai.app.presentation.theme.ShadowDarkL2Ambient
import com.calai.app.presentation.theme.ShadowDarkL2Spot
import com.calai.app.presentation.theme.TextWhite

/**
 * CTA chính "tactile" dùng chung cho toàn app (Part 4.1 — CalAI_FINAL_Design_Code_Rules v3):
 * - Pressed: scale ~0.97, translationY 1–2dp, elevation giảm nhẹ, animation 120–180ms FastOutSlowIn.
 * - Default: màu đặc duy nhất (`CtaSolidOrange`) + soft colored shadow (L3 level) + top-edge highlight.
 * - Disabled: neutral gray shadow, reduced alpha.
 * - Loading: khoá `enabled`, hiện spinner (chặn duplicate submit, Part 4.8/4.9).
 *
 * Elevation hierarchy:
 *   Pressed  → L3Pressed (4 dp) + ShadowCtaDisabled
 *   Default  → L3 (12 dp) + ShadowCtaAmbient / ShadowCtaSpot
 *   Disabled → L1 (4 dp)  + ShadowCtaDisabled
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    gradientColors: List<Color> = listOf(CtaSolidOrange, CtaSolidOrange),
    glowColor: Color = ShadowCtaAmbient,
    contentColor: Color = TextWhite,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    height: androidx.compose.ui.unit.Dp = 54.dp,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val isEnabled = enabled && !isLoading
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile scale — Part 4.1: 0.96–0.98
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "app_button_scale"
    )
    // Physical press-down: move 2dp toward user
    val translationY by animateDpAsState(
        targetValue = if (isPressed && isEnabled) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "app_button_translation_y"
    )
    // Shadow elevation: L3 default → L3Pressed when down → L1 when disabled
    val shadowElevation by animateDpAsState(
        targetValue = when {
            !isEnabled -> AppElevation.L1
            isPressed  -> AppElevation.L3Pressed
            else       -> AppElevation.L3
        },
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "app_button_elevation"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (!isEnabled) 0.48f else if (isPressed) 0.90f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "app_button_alpha"
    )

    // Shadow color: brand-tinted when active, neutral when disabled
    val ambientShadow = if (isEnabled) ShadowCtaAmbient else ShadowCtaDisabled
    val spotShadow    = if (isEnabled) ShadowCtaSpot    else ShadowCtaDisabled

    val brush = if (isEnabled) {
        Brush.horizontalGradient(gradientColors)
    } else {
        Brush.horizontalGradient(gradientColors.map { it.copy(alpha = 0.38f) })
    }

    // Top-edge highlight: subtle white shimmer along upper border — creates physical surface feel
    val highlightBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isEnabled) 0.30f else 0.12f),
            Color.White.copy(alpha = 0.00f),
        )
    )

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
                ambientColor = ambientShadow,
                spotColor = spotShadow
            )
            .clip(shape)
            .background(brush)
            // Top-edge highlight border drawn OVER the fill
            .border(
                width = 1.dp,
                brush = highlightBrush,
                shape = shape
            )
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

/**
 * Nút phụ (secondary) dùng chung cho toàn app — Part 4.1 / Part 8.
 *
 * Visual specification:
 * - Light solid surface (MaterialTheme.colorScheme.surface)
 * - 1dp border using outline color
 * - Soft neutral shadow (L2 level) so it clearly looks like a physical button
 * - Pressed: scale 0.97, translationY 1.5dp, shadow drops to L2Pressed
 * - Disabled: 0.45 alpha, L1 shadow
 *
 * Use for: "Hủy" buttons, secondary actions, Google sign-in row, etc.
 * Do NOT use for primary CTAs — those use [AppButton] with the brand-colored shadow.
 */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
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
        label = "secondary_button_scale"
    )
    val translationY by animateDpAsState(
        targetValue = if (isPressed && isEnabled) 1.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "secondary_button_translation_y"
    )
    val shadowElevation by animateDpAsState(
        targetValue = when {
            !isEnabled -> AppElevation.L1
            isPressed  -> AppElevation.L2Pressed
            else       -> AppElevation.L2
        },
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "secondary_button_elevation"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (!isEnabled) 0.45f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "secondary_button_alpha"
    )

    val ambientShadow = if (isDarkTheme) ShadowDarkL2Ambient else ShadowLightL2Ambient
    val spotShadow    = if (isDarkTheme) ShadowDarkL2Spot    else ShadowLightL2Spot

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
                ambientColor = ambientShadow,
                spotColor = spotShadow
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            )
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
                color = MaterialTheme.colorScheme.onBackground,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Box(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
