package com.calai.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * - Lớp CharcoalDock + viền CharcoalBorder + bo góc 32dp
 * - Tab active với nền VividOrange
 * - Icon Glassmorphism mờ nhẹ khi không active
 */
@Composable
fun FloatingBottomDock(
    currentTab: DockTab,
    onTabSelected: (DockTab) -> Unit,
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
                .background(CharcoalDock)
                .border(1.dp, CharcoalBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                icon = Icons.Default.Home,
                isSelected = currentTab == DockTab.HOME,
                onClick = { onTabSelected(DockTab.HOME) }
            )
            DockItem(
                icon = Icons.Default.AutoGraph,
                isSelected = currentTab == DockTab.STATISTICS,
                onClick = { onTabSelected(DockTab.STATISTICS) }
            )
            // Tab quét AI ở chính giữa
            DockItem(
                icon = Icons.Default.CameraAlt,
                isSelected = currentTab == DockTab.SCAN,
                isHero = true,
                onClick = { onTabSelected(DockTab.SCAN) }
            )
            DockItem(
                icon = Icons.Default.AutoAwesome,
                isSelected = currentTab == DockTab.CHAT,
                onClick = { onTabSelected(DockTab.CHAT) }
            )
            DockItem(
                icon = Icons.Default.Person,
                isSelected = currentTab == DockTab.PROFILE,
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
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> VividOrange
            isHero -> CharcoalCardElevated
            else -> Color.Transparent
        },
        label = "dock_bg"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isSelected -> TextWhite
            isHero -> VividOrange
            else -> TextMuted
        },
        label = "dock_icon"
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (isHero && !isSelected) {
                    Modifier.border(1.dp, VividOrange.copy(alpha = 0.4f), CircleShape)
                } else Modifier
            )
            .clickable { onClick() },
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

