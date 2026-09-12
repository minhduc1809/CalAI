package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.data.remote.dto.WeightLogResponseDto
import com.calai.app.presentation.components.AppButton
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.WeightHistoryViewModel

/**
 * Lịch sử cân nặng đầy đủ: xem, thêm, sửa, xóa từng bản ghi (POST/PATCH/DELETE weight-logs).
 * Hỗ trợ đa đơn vị (kg / lb) theo cài đặt hệ thống.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightHistoryScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: WeightHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val border = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow
    val inputUnitLabel = if (uiState.weightUnit == "lb") "lb" else "kg"

    if (uiState.isAddingLog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelAdd() },
            containerColor = surface,
            title = { Text("Ghi cân nặng mới", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.addWeightText,
                        onValueChange = viewModel::setAddWeight,
                        label = { Text("Cân nặng ($inputUnitLabel)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = VividOrange,
                            unfocusedBorderColor = border
                        )
                    )
                    OutlinedTextField(
                        value = uiState.addNoteText,
                        onValueChange = viewModel::setAddNote,
                        label = { Text("Ghi chú (tùy chọn)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = VividOrange,
                            unfocusedBorderColor = border
                        )
                    )
                    if (uiState.errorMessage != null) {
                        Text(
                            uiState.errorMessage!!,
                            color = CrimsonError,
                            fontSize = 12.5.sp
                        )
                    }
                }
            },
            confirmButton = {
                AppButton(
                    text = "Lưu",
                    onClick = { viewModel.confirmAdd() },
                    enabled = uiState.addWeightText.toFloatOrNull() != null,
                    isLoading = uiState.isSaving,
                    modifier = Modifier.width(100.dp),
                    height = 40.dp,
                    shape = RoundedCornerShape(10.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelAdd() }) {
                    Text("Hủy", color = textSecondary)
                }
            }
        )
    }

    if (uiState.editingLog != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelEdit() },
            containerColor = surface,
            title = { Text("Sửa bản ghi cân nặng", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.editWeightText,
                        onValueChange = viewModel::setEditWeight,
                        label = { Text("Cân nặng ($inputUnitLabel)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = VividOrange,
                            unfocusedBorderColor = border
                        )
                    )
                    OutlinedTextField(
                        value = uiState.editNoteText,
                        onValueChange = viewModel::setEditNote,
                        label = { Text("Ghi chú (không bắt buộc)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = VividOrange,
                            unfocusedBorderColor = border
                        )
                    )
                    if (uiState.errorMessage != null) {
                        Text(
                            uiState.errorMessage!!,
                            color = CrimsonError,
                            fontSize = 12.5.sp
                        )
                    }
                }
            },
            confirmButton = {
                AppButton(
                    text = "Lưu",
                    onClick = { viewModel.confirmEdit() },
                    enabled = uiState.editWeightText.toFloatOrNull() != null,
                    isLoading = uiState.isSaving,
                    modifier = Modifier.width(100.dp),
                    height = 40.dp,
                    shape = RoundedCornerShape(10.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelEdit() }) {
                    Text("Hủy", color = textSecondary)
                }
            }
        )
    }

    if (uiState.pendingDeleteLog != null) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isDeleting) viewModel.cancelDelete() },
            containerColor = surface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CrimsonError.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonError)
                }
            },
            title = { Text("Xóa bản ghi cân nặng này?", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Bản ghi sẽ bị xóa vĩnh viễn và không thể khôi phục. Xu hướng cân nặng (EWMA) sẽ được tính lại sau khi xóa.",
                        color = textSecondary,
                        fontSize = 13.sp
                    )
                    if (uiState.errorMessage != null) {
                        Text(uiState.errorMessage!!, color = CrimsonError, fontSize = 12.5.sp)
                    }
                }
            },
            confirmButton = {
                AppButton(
                    text = "Xóa",
                    onClick = { viewModel.confirmDelete() },
                    isLoading = uiState.isDeleting,
                    gradientColors = listOf(CrimsonError, CrimsonError.copy(alpha = 0.85f)),
                    glowColor = CrimsonError.copy(alpha = 0.4f),
                    modifier = Modifier.width(100.dp),
                    height = 40.dp,
                    shape = RoundedCornerShape(10.dp)
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelDelete() },
                    enabled = !uiState.isDeleting,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, border)
                ) {
                    Text("Hủy", color = textSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử cân nặng", fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startAdd() }) {
                        Icon(Icons.Default.Add, contentDescription = "Ghi cân nặng", tint = VividOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.startAdd() },
                containerColor = VividOrange,
                contentColor = TextWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Ghi cân nặng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = VividOrange,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.logs.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = textSecondary, modifier = Modifier.size(40.dp))
                        Text("Chưa có bản ghi cân nặng nào", color = textSecondary, fontSize = 14.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.logs, key = { it.id }) { log ->
                            WeightLogRow(
                                log = log,
                                weightUnit = uiState.weightUnit,
                                surface = surface,
                                border = border,
                                shadowColor = shadowColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                onEdit = { viewModel.startEdit(log) },
                                onDelete = { viewModel.requestDelete(log) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightLogRow(
    log: WeightLogResponseDto,
    weightUnit: String,
    surface: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    shadowColor: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayWeight = UserPreferencesManager.formatWeight(log.weightKg, weightUnit)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp), ambientColor = shadowColor, spotColor = shadowColor)
            .clip(RoundedCornerShape(18.dp))
            .background(surface)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayWeight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(text = log.date, fontSize = 12.sp, color = textSecondary)
                if (!log.note.isNullOrBlank()) {
                    Text(text = log.note, fontSize = 12.sp, color = textSecondary, maxLines = 1)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = textSecondary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = CoralWarning, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
