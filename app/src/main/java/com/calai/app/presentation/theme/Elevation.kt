package com.calai.app.presentation.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized elevation dp values for the CalAI Soft-Elevation Design System.
 *
 * Hierarchy:
 * ─────────────────────────────────────────────────────────────
 * L0  Page background            No shadow
 * L1  Inputs / small controls    Very subtle — 4 dp
 * L2  Standard cards             Soft visible — 8 dp
 * L3  Primary buttons / CTA      Slightly stronger — 12 dp
 * L4  Modals / bottom sheets     Strongest (still diffused) — 20 dp
 * ─────────────────────────────────────────────────────────────
 *
 * Shadow color is controlled by the L1–L4 tokens in Color.kt.
 * For brand-colored CTA buttons, use [ShadowCtaAmbient] / [ShadowCtaSpot] directly.
 */
object AppElevation {
    val L0: Dp = 0.dp
    val L1: Dp = 4.dp
    val L2: Dp = 8.dp
    val L3: Dp = 12.dp
    val L4: Dp = 20.dp

    // Pressed states — reduce by ~60% to create physical press-down feel
    val L1Pressed: Dp = 1.dp
    val L2Pressed: Dp = 3.dp
    val L3Pressed: Dp = 4.dp
    val L4Pressed: Dp = 8.dp
}

/**
 * Applies a soft ambient shadow matching the given elevation [level] (1–4) and [isDarkTheme].
 * Uses the graduated [ShadowDarkL*] / [ShadowLightL*] tokens from Color.kt.
 *
 * Example usage:
 * ```
 * Modifier.appElevation(level = 2, isDarkTheme = isDarkTheme, shape = RoundedCornerShape(20.dp))
 * ```
 *
 * @param level  1 = input/small, 2 = card, 3 = button (neutral), 4 = modal/dock.
 *               For primary CTA buttons prefer explicit [ShadowCtaAmbient]/[ShadowCtaSpot].
 */
fun Modifier.appElevation(
    level: Int,
    isDarkTheme: Boolean,
    shape: Shape = RectangleShape,
    clip: Boolean = false,
): Modifier {
    val (elevation, ambient, spot) = when (level) {
        1 -> if (isDarkTheme)
            Triple(AppElevation.L1, ShadowDarkL1Ambient, ShadowDarkL1Spot)
        else
            Triple(AppElevation.L1, ShadowLightL1Ambient, ShadowLightL1Spot)
        2 -> if (isDarkTheme)
            Triple(AppElevation.L2, ShadowDarkL2Ambient, ShadowDarkL2Spot)
        else
            Triple(AppElevation.L2, ShadowLightL2Ambient, ShadowLightL2Spot)
        3 -> if (isDarkTheme)
            Triple(AppElevation.L3, ShadowDarkL3Ambient, ShadowDarkL3Spot)
        else
            Triple(AppElevation.L3, ShadowLightL3Ambient, ShadowLightL3Spot)
        4 -> if (isDarkTheme)
            Triple(AppElevation.L4, ShadowDarkL4Ambient, ShadowDarkL4Spot)
        else
            Triple(AppElevation.L4, ShadowLightL4Ambient, ShadowLightL4Spot)
        else -> Triple(AppElevation.L0, ShadowDarkL1Ambient, ShadowDarkL1Spot)
    }
    return this.shadow(
        elevation = elevation,
        shape = shape,
        clip = clip,
        ambientColor = ambient,
        spotColor = spot,
    )
}
