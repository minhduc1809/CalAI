package com.calai.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.calai.app.presentation.theme.*

enum class DockTab {
    HOME,
    STATISTICS,
    SCAN,
    CHAT,
    PROFILE
}

/**
 * Thanh điều hướng nổi dạng đảo (Dark Luxury Floating Island Dock)
 * Tuân thủ quy tắc 9.2:
 * - Lớp MaterialTheme.colorScheme.surfaceVariant + viền MaterialTheme.colorScheme.outline + bo góc 32dp
 * - Tab active với nền VividOrange
 * - Icon Glassmorphism mờ nhẹ khi không active
 */
@Composable
fun FloatingBottomDock(
    currentTab: DockTab,
    onTabSelected: (DockTab) -> Unit,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            // Tab quét AI ở chính giữa
            DockItem(
                icon = Icons.Default.CameraAlt,
                isSelected = currentTab == DockTab.SCAN,
                isHero = true,
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
private fun DockItem(
    icon: ImageVector,
    isSelected: Boolean,
    isHero: Boolean = false,
    isDarkTheme: Boolean = true,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> VividOrange
            isHero -> MaterialTheme.colorScheme.surfaceContainerHighest
            else -> Color.Transparent
        },
        label = "dock_bg"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onBackground
            isHero -> VividOrange
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "dock_icon"
    )

    // Hiệu ứng nhấn rõ ràng (Spec mục 3 - Sạch sẽ & phản hồi rõ khi bấm)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "dock_item_press_scale"
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .scale(pressScale)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (isHero && !isSelected) {
                    Modifier.border(1.dp, VividOrange.copy(alpha = 0.4f), CircleShape)
                } else Modifier
            )
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

