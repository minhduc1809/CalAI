package com.calai.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import com.calai.app.presentation.theme.OnPrimary
import com.calai.app.presentation.theme.VividOrange

const val THEME_LIGHT = "LIGHT"
const val THEME_DARK = "DARK"

private data class ThemeOption(val mode: String, val label: String, val icon: ImageVector)

private val OPTIONS = listOf(
    ThemeOption(THEME_LIGHT, "Sáng", Icons.Default.LightMode),
    ThemeOption(THEME_DARK, "Tối", Icons.Default.DarkMode),
)

/** Chọn giao diện Sáng / Tối / Theo hệ thống. Đổi có hiệu lực ngay, không cần mở lại ứng dụng. */
@Composable
fun ThemeModeSelector(mode: String, onModeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OPTIONS.forEach { opt ->
            val selected = mode == opt.mode
            val bg by animateColorAsState(if (selected) VividOrange else MaterialTheme.colorScheme.surfaceVariant, tween(160), label = "theme-opt-bg")
            val fg = if (selected) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(62.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable(role = Role.RadioButton) { onModeChange(opt.mode) }
                    .semantics { this.selected = selected; contentDescription = "Giao diện ${opt.label}" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(opt.icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
                Text(
                    text = opt.label,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = fg,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
