package com.calai.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.theme.AppElevation
import com.calai.app.presentation.theme.ShadowCtaAmbient
import com.calai.app.presentation.theme.ShadowCtaSpot
import com.calai.app.presentation.theme.ShadowDarkL4Ambient
import com.calai.app.presentation.theme.ShadowDarkL4Spot
import com.calai.app.presentation.theme.ShadowLightL4Ambient
import com.calai.app.presentation.theme.ShadowLightL4Spot

enum class DockTab {
    HOME,
    STATISTICS,
    SCAN,
    CHAT,
    PROFILE
}

/**
 * Floating Island Bottom Navigation — Premium redesign
 * - Shadow rõ hơn cho toàn dock (floating tách khỏi background)
 * - Camera/SCAN tab: size 54dp, gradient orange background, shadow riêng → primary action focal point
 * - Active tab: orange background + subtle glow
 * - Press: scale down 0.88f mượt
 */
@Composable
fun FloatingBottomDock(
    currentTab: DockTab,
    onTabSelected: (DockTab) -> Unit,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    val dockAmbient = if (isDarkTheme) ShadowDarkL4Ambient else ShadowLightL4Ambient
    val dockSpot    = if (isDarkTheme) ShadowDarkL4Spot    else ShadowLightL4Spot

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(68.dp)
                .shadow(
                    elevation = AppElevation.L4,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = dockAmbient,
                    spotColor = dockSpot
                )
                .clip(RoundedCornerShape(34.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White,
                            MaterialTheme.colorScheme.outline
                        )
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                icon = Icons.Default.Home,
                isSelected = currentTab == DockTab.HOME,
                isDarkTheme = isDarkTheme,
                onClick = { onTabSelected(DockTab.HOME) }
            )
            DockItem(
                icon = Icons.Default.AutoGraph,
                isSelected = currentTab == DockTab.STATISTICS,
                isDarkTheme = isDarkTheme,
                onClick = { onTabSelected(DockTab.STATISTICS) }
            )
            // Camera — PRIMARY ACTION (hero item)
            HeroCameraItem(
                isSelected = currentTab == DockTab.SCAN,
                isDarkTheme = isDarkTheme,
                onClick = { onTabSelected(DockTab.SCAN) }
            )
            DockItem(
                icon = Icons.Default.AutoAwesome,
                isSelected = currentTab == DockTab.CHAT,
                isDarkTheme = isDarkTheme,
                onClick = { onTabSelected(DockTab.CHAT) }
            )
            DockItem(
                icon = Icons.Default.Person,
                isSelected = currentTab == DockTab.PROFILE,
                isDarkTheme = isDarkTheme,
                onClick = { onTabSelected(DockTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun RowScope.DockItem(
    icon: ImageVector,
    isSelected: Boolean,
    isDarkTheme: Boolean = true,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) CtaSolidOrange else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "dock_bg"
    )
    val iconColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "dock_icon"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "dock_press"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .wrapContentWidth()
            .size(46.dp)
            .scale(pressScale)
            .shadow(
                elevation = if (isSelected) AppElevation.L2 else AppElevation.L1,
                shape = CircleShape,
                ambientColor = if (isSelected) ShadowCtaAmbient else ShadowCtaAmbient.copy(alpha = 0.08f),
                spotColor    = if (isSelected) ShadowCtaSpot    else ShadowCtaSpot.copy(alpha = 0.08f)
            )
            .clip(CircleShape)
            .background(bgColor)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun RowScope.HeroCameraItem(
    isSelected: Boolean,
    isDarkTheme: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "camera_press"
    )

    val cameraShadow = CtaSolidOrange.copy(alpha = if (isDarkTheme) 0.5f else 0.35f)

    Box(
        modifier = Modifier
            .weight(1f)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .scale(pressScale)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = cameraShadow,
                    spotColor = cameraShadow
                )
                .clip(CircleShape)
                .background(CtaSolidOrange)
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(interactionSource = interactionSource, indication = null) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Quét AI",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
