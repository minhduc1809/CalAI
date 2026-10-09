package com.calai.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calai.app.presentation.theme.AppElevation
import com.calai.app.presentation.theme.ShadowDarkL1Ambient
import com.calai.app.presentation.theme.ShadowDarkL1Spot
import com.calai.app.presentation.theme.ShadowLightL1Ambient
import com.calai.app.presentation.theme.ShadowLightL1Spot
import com.calai.app.presentation.theme.VividOrange
import com.calai.app.presentation.theme.VividOrangeGlow

/**
 * Input dùng chung cho MỌI form Thêm/Sửa trong app (Part 4.6/7.1/8 —
 * CalAI_FINAL_Design_Code_Rules v3): Label đặt RIÊNG phía trên ô nhập (không dùng label
 * nổi kiểu Material mặc định — đó là nguyên nhân các form trước đây (VD "Tạo món ăn
 * riêng") trông như form OS mặc định, không giống phần còn lại của app), viền sáng +
 * quầng glow nhẹ khi focus, bo góc lớn đồng bộ Card (16dp) thay vì bo nhỏ mặc định.
 *
 * Dùng lại đúng 1 component này cho mọi field trong CreateCustomFoodDialog,
 * QuickAddDialog, và các form Thêm/Sửa khác — tránh mỗi màn tự vẽ OutlinedTextField
 * riêng theo cách khác nhau (Part 1.1 — không copy-paste UI giữa các màn).
 */
@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    labelFontSize: androidx.compose.ui.unit.TextUnit = 12.5.sp,
    /** Chấm tròn màu semantic trước label (VD macro Protein/Carb/Fat) — thay cho icon/emoji rời rạc. */
    leadingDot: Color? = null,
    /** Hint nhỏ căn phải cùng hàng label (VD "bắt buộc") — không dùng dấu * trần không giải thích. */
    trailingHint: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) VividOrange else MaterialTheme.colorScheme.outline,
        label = "app_textfield_border"
    )
    val shape = RoundedCornerShape(16.dp)
    // Elevation shadow: L1 always present (field is a real surface), L2+brand when focused
    val shadowElevation = if (isFocused) AppElevation.L2 else AppElevation.L1
    val shadowAmbient = if (isFocused) VividOrangeGlow
        else if (isDarkTheme) ShadowDarkL1Ambient else ShadowLightL1Ambient
    val shadowSpot = if (isFocused) VividOrange.copy(alpha = 0.22f)
        else if (isDarkTheme) ShadowDarkL1Spot else ShadowLightL1Spot

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingDot != null) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(leadingDot)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = label,
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailingHint != null) {
                Text(
                    text = trailingHint,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) } },
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            interactionSource = interactionSource,
            shape = shape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = VividOrange
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    ambientColor = shadowAmbient,
                    spotColor = shadowSpot
                )
                .clip(shape)
                .border(width = if (isFocused) 1.5.dp else 1.dp, color = borderColor, shape = shape)
        )
    }
}

/** Biến thể padding(4.dp) cho các ô nhập số hẹp xếp thành hàng (VD Protein/Carb/Fat). */
@Composable
fun AppTextFieldCompact(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number,
    leadingDot: Color? = null,
) = AppTextField(
    label = label,
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    keyboardType = keyboardType,
    labelFontSize = 11.sp,
    leadingDot = leadingDot,
)
