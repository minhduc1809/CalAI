package com.calai.app.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calai.app.presentation.theme.VividOrange
import com.calai.app.presentation.theme.VividOrangeDark
import com.calai.app.presentation.theme.VividOrangeGlow
import com.calai.app.presentation.theme.VividOrangeLight

/**
 * Toggle "nổi khối" dùng chung cho toàn app (Part 4.5 — CalAI_FINAL_Design_Code_Rules v3):
 * - Track dùng `surfaceContainerHighest` (~CharcoalCardElevated ở Dark) + viền inset `outline` (~CharcoalBorder).
 * - Thumb: gradient nổi + drop shadow mềm + vòng glow accent mỏng khi active.
 * - `activeColor` cho phép mỗi màn dùng đúng semantic color của nó (VividOrange mặc định).
 * - `thumbIcon` là slot tuỳ chọn để vẽ icon bên trong thumb (vd. sun/moon của theme switch),
 *   nhận `checked` hiện tại để tự chọn icon phù hợp.
 *
 * Đây là component thay thế cho các bản tactile-switch trùng lặp trước đó
 * (`TactileThemeSwitch`, `ReminderTactileSwitch`) để tránh trùng lặp logic vẽ toggle (Part 1.1/11.2).
 */
@Composable
fun AppToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = VividOrange,
    activeColorDark: Color = VividOrangeDark,
    activeColorLight: Color = VividOrangeLight,
    activeGlowColor: Color = VividOrangeGlow,
    width: Dp = 54.dp,
    height: Dp = 30.dp,
    thumbIcon: (@Composable (checked: Boolean) -> Unit)? = null,
) {
    val trackPadding = 3.dp
    val thumbSize = height - trackPadding * 2
    val maxOffset = width - height + trackPadding

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) maxOffset else trackPadding,
        animationSpec = tween(durationMillis = 300),
        label = "app_toggle_thumb_offset"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(height / 2))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(thumbSize)
                .shadow(
                    elevation = if (checked) 5.dp else 3.dp,
                    shape = CircleShape,
                    ambientColor = if (checked) activeGlowColor else Color.Black.copy(alpha = 0.15f),
                    spotColor = if (checked) activeGlowColor else Color.Black.copy(alpha = 0.15f)
                )
                .clip(CircleShape)
                .background(
                    brush = if (checked) {
                        Brush.verticalGradient(listOf(activeColor, activeColorDark))
                    } else {
                        Brush.verticalGradient(
                            listOf(Color.White, MaterialTheme.colorScheme.surfaceContainerHighest)
                        )
                    }
                )
                .border(
                    width = if (checked) 1.dp else 0.75.dp,
                    color = if (checked) activeColorLight else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            thumbIcon?.invoke(checked)
        }
    }
}
