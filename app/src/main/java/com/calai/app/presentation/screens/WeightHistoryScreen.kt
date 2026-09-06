package com.calai.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorWeight
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
import com.calai.app.data.remote.dto.WeightLogResponseDto
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.WeightHistoryViewModel

/**
 * Lịch sử cân nặng đầy đủ: xem, sửa, xóa từng bản ghi (PATCH/DELETE weight-logs/:id).
 * Truy cập từ nút "Xem lịch sử →" trên thẻ Xu hướng cân nặng ở màn Thống kê.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightHistoryScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: WeightHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val bg = if (isDarkTheme) ObsidianBackground else IvoryBackground
    val surface = if (isDarkTheme) CharcoalSurface else PearlCard
    val border = if (isDarkTheme) CharcoalBorder else PearlBorder
    val textPrimary = if (isDarkTheme) TextWhite else TextInkPrimary
    val textSecondary = if (isDarkTheme) TextMuted else TextInkMuted
    val shadowColor = if (isDarkTheme) DarkShadow else WarmShadow

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
                        label = { Text("Cân nặng (kg)") },
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
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmEdit() },
                    enabled = !uiState.isSaving && uiState.editWeightText.toFloatOrNull() != null
                ) {
                    Text("Lưu", color = VividOrange, fontWeight = FontWeight.Bold)
                }
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
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = surface,
            title = { Text("Xóa bản ghi này?", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Xu hướng cân nặng (EWMA) sẽ được tính lại sau khi xóa.", color = textSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Xóa", color = CoralWarning, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
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
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.logs, key = { it.id }) { log ->
                            WeightLogRow(
                                log = log,
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
    surface: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    shadowColor: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    text = "${log.weightKg} kg",
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
