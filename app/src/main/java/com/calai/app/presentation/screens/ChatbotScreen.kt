package com.calai.app.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calai.app.presentation.components.DockTab
import com.calai.app.presentation.components.FloatingBottomDock
import com.calai.app.presentation.theme.*
import com.calai.app.presentation.viewmodel.ChatMessage
import com.calai.app.presentation.viewmodel.ChatbotViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    onNavigateTab: (DockTab) -> Unit,
    isDarkTheme: Boolean = true,
    viewModel: ChatbotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showUpgradeSheet by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Tự động cuộn xuống tin nhắn mới nhất
    LaunchedEffect(uiState.messages.size, uiState.isTyping) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) ObsidianBackground else IvoryBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // 1. TOP HEADER: AI Coach Status Bar & Plan Tier Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PastelLavender.copy(alpha = 0.2f))
                        .border(1.dp, PastelLavender.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PastelLavender,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CalAI Nutrition Coach",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextWhite else TextInkPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MintJade)
                        )
                        Text(
                            text = "Trợ lý dinh dưỡng AI • Trực tuyến",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) TextMuted else TextInkMuted
                        )
                    }
                }

                // Badge Bản Nâng Cấp (Plus / Pro / Max)
                Surface(
                    color = when (uiState.quota.currentTier) {
                        "MAX" -> VividOrange.copy(alpha = 0.2f)
                        "PRO" -> PastelLavender.copy(alpha = 0.2f)
                        "PLUS" -> MintJade.copy(alpha = 0.2f)
                        else -> CharcoalBorder.copy(alpha = 0.5f)
                    },
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (uiState.quota.currentTier) {
                            "MAX" -> VividOrange
                            "PRO" -> PastelLavender
                            "PLUS" -> MintJade
                            else -> CharcoalBorder
                        }
                    ),
                    modifier = Modifier.clickable { showUpgradeSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = when (uiState.quota.currentTier) {
                                "MAX" -> VividOrange
                                "PRO" -> PastelLavender
                                "PLUS" -> MintJade
                                else -> TextMuted
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = uiState.quota.tierName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                    }
                }

                // Nút Xóa lịch sử trò chuyện
                IconButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa lịch sử",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Thanh tiến trình hạn mức trò chuyện (% còn lại)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = uiState.quota.statusMessage,
                    fontSize = 11.5.sp,
                    color = if (uiState.quota.status == "LOW" || uiState.quota.status == "EXHAUSTED") CrimsonError else TextMuted
                )
                Text(
                    text = "Hạn mức: ${uiState.quota.remainingPercent}%",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (uiState.quota.remainingPercent > 20) MintJade else CrimsonError
                )
            }
            LinearProgressIndicator(
                progress = { (uiState.quota.remainingPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (uiState.quota.remainingPercent > 20) MintJade else CrimsonError,
                trackColor = CharcoalBorder.copy(alpha = 0.4f),
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. SUGGESTED CHIPS ROW (Gợi ý câu hỏi nhanh)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.suggestedPrompts.forEach { prompt ->
                    Surface(
                        color = if (isDarkTheme) CharcoalSurface else PearlCard,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isDarkTheme) CharcoalBorder else PearlBorder
                        ),
                        modifier = Modifier.clickable {
                            inputText = prompt
                            viewModel.sendMessage(prompt)
                            inputText = ""
                        }
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            color = if (isDarkTheme) TextWhite else TextInkPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 3. DANH SÁCH TIN NHẮN (MESSAGES LIST)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(message = message, isDarkTheme = isDarkTheme)
                }

                if (uiState.isTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // 4. KHUNG NHẬP LIỆU (INPUT BAR)
            Surface(
                color = if (isDarkTheme) CharcoalSurface else PearlCard,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isDarkTheme) CharcoalBorder else PearlBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Hỏi AI Coach về thực đơn, calo...",
                                fontSize = 14.sp,
                                color = if (isDarkTheme) TextMuted else TextInkMuted
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary,
                            unfocusedTextColor = if (isDarkTheme) TextWhite else TextInkPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isTyping,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) PastelLavender
                                else (if (isDarkTheme) CharcoalBorder else PearlBorder)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            tint = if (inputText.isNotBlank()) ObsidianBackground else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // BottomSheet Nâng Cấp 3 Bản: Plus, Pro, Max
        if (showUpgradeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showUpgradeSheet = false },
                containerColor = ObsidianBackground
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nâng Cấp Bản AI Coach",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Chọn gói trải nghiệm để trò chuyện thoải mái và đồng hành dài hạn",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

                    val plans = if (uiState.plans.isNotEmpty()) uiState.plans else listOf(
                        com.calai.app.data.remote.dto.ChatPlanDto("PLUS", "Bản Plus", 29000L, "Mở rộng trò chuyện với AI Coach, phân tích sâu thực đơn & chế độ ăn"),
                        com.calai.app.data.remote.dto.ChatPlanDto("PRO", "Bản Pro", 59000L, "Trò chuyện không giới hạn, phân tích dinh dưỡng cá nhân hóa chuyên sâu", isPopular = true),
                        com.calai.app.data.remote.dto.ChatPlanDto("MAX", "Bản Max", 99000L, "Bản cao cấp nhất - Huấn luyện viên AI toàn diện đồng hành mọi lúc mọi nơi", bestValue = true)
                    )

                    plans.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            isCurrent = uiState.quota.currentTier == plan.id,
                            onSelect = {
                                viewModel.purchasePlan(plan.id)
                                showUpgradeSheet = false
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Dialog xác nhận xóa lịch sử trò chuyện
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Làm mới cuộc trò chuyện?") },
                text = { Text("Toàn bộ lịch sử trò chuyện trong 7 ngày qua sẽ được xóa sạch để bắt đầu phiên mới.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearHistory()
                            showClearConfirmDialog = false
                        }
                    ) {
                        Text("Xóa lịch sử", color = CrimsonError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Hủy", color = TextWhite)
                    }
                },
                containerColor = CharcoalSurface
            )
        }

        // Dock điều hướng
        FloatingBottomDock(
            currentTab = DockTab.CHAT,
            onTabSelected = onNavigateTab,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PlanCard(
    plan: com.calai.app.data.remote.dto.ChatPlanDto,
    isCurrent: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        plan.bestValue -> VividOrange
        plan.isPopular -> PastelLavender
        else -> CharcoalBorder
    }

    Surface(
        color = CharcoalSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(if (plan.isPopular || plan.bestValue) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrent) { onSelect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = plan.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    if (plan.isPopular) {
                        Surface(
                            color = PastelLavender.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Phổ biến",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PastelLavender,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (plan.bestValue) {
                        Surface(
                            color = VividOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Tốt nhất",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VividOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(plan.priceVnd)
                Text(
                    text = formattedPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintJade
                )
            }

            Text(
                text = plan.description,
                fontSize = 12.5.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
            )

            Button(
                onClick = onSelect,
                enabled = !isCurrent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) CharcoalBorder else PastelLavender,
                    contentColor = ObsidianBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCurrent) "Đang sử dụng bản này" else "Nâng Cấp Ngay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PastelLavender.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PastelLavender,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = if (message.isUser) {
                PastelLavender
            } else {
                if (isDarkTheme) CharcoalSurface else PearlCard
            },
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            ),
            border = if (!message.isUser) {
                androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isDarkTheme) CharcoalBorder else PearlBorder
                )
            } else null,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = if (message.isUser) ObsidianBackground else (if (isDarkTheme) TextWhite else TextInkPrimary),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing_dots")
    val dotAlpha1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dotAlpha2 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dotAlpha3 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier.padding(start = 36.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(PastelLavender.copy(alpha = dotAlpha1))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(PastelLavender.copy(alpha = dotAlpha2))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(PastelLavender.copy(alpha = dotAlpha3))
        )
    }
}
