package com.calai.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

/**
 * Row lựa chọn có icon tròn màu bên trái (phong cách "linh hoạt, đa dạng" — mỗi lựa chọn
 * có 1 màu điểm nhấn riêng thay vì đồng loạt 1 màu cam) + dấu tick khi được chọn.
 * Dùng cho các câu hỏi định danh/nhận diện (Gender, Diet Type icon-based...).
 */
@Composable
fun IconOptionRow(
    icon: ImageVector,
    accentColor: Color,
    label: String,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface)
            .border(
                if (isSelected) 1.8.dp else 1.dp,
                if (isSelected) accentColor else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = if (isDarkTheme) 0.22f else 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
            }
        }
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
