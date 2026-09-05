package com.calai.app.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.calai.app.presentation.theme.*

/**
 * Nút chuyển đổi giao diện Sáng / Tối dạng khối xúc giác nổi 3D (Tactile 3D Switch - Spec 10.5)
 * - Track lõm với viền CharcoalBorder
 * - Núm tròn (Thumb) nổi khối với shadow mềm và viền phát quang VividOrange
 */
@Composable
fun TactileThemeSwitch(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Vị trí offset của thumb di chuyển mượt mà
    val thumbOffset by animateDpAsState(
        targetValue = if (isDarkTheme) 32.dp else 4.dp,
        animationSpec = tween(durationMillis = 350),
        label = "switch_thumb_offset"
    )

    Box(
        modifier = modifier
            .width(66.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDarkTheme) CharcoalCardElevated else PearlCard)
            .border(1.2.dp, if (isDarkTheme) CharcoalBorder else PearlBorder, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onThemeChanged(!isDarkTheme)
            }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Núm tròn xúc giác 3D (Tactile Thumb)
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(28.dp)
                .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = if (isDarkTheme) VividOrangeGlow else Color.Black.copy(alpha = 0.15f))
                .clip(CircleShape)
                .background(
                    brush = if (isDarkTheme) {
                        Brush.verticalGradient(
                            listOf(
                                VividOrange,
                                VividOrangeDark
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color.White,
                                PearlCardElevated
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isDarkTheme) VividOrangeLight else Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = if (isDarkTheme) TextWhite else TextInkPrimary,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
