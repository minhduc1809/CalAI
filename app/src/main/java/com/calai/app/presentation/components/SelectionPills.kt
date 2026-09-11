package com.calai.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.*

/**
 * Pill lựa chọn dùng chung cho các màn có nhiều lựa chọn dạng nút bo tròn
 * (GoalSetupScreen, OnboardingScreen...) — tránh copy-paste giữa các screen (CODING_RULES.md mục 3).
 */
@Composable
fun SelectionPill(
    label: String,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) VividOrange else (MaterialTheme.colorScheme.surface))
            .border(1.dp, if (isSelected) VividOrange else (MaterialTheme.colorScheme.outline), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TextWhite else (MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@Composable
fun RateSelectionPill(
    rate: Float,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) VividOrange else (MaterialTheme.colorScheme.surface))
            .border(1.dp, if (isSelected) VividOrange else (MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$rate kg",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TextWhite else (MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@Composable
fun MacroStyleOptionRow(
    label: String,
    desc: String,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) VividOrangeSoft else (MaterialTheme.colorScheme.surface))
            .border(1.dp, if (isSelected) VividOrange else (MaterialTheme.colorScheme.outline), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) VividOrange else (MaterialTheme.colorScheme.onBackground)
            )
            Text(desc, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = VividOrange)
        )
    }
}
