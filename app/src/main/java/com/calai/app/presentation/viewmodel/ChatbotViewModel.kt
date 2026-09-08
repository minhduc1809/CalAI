package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.ChatPlanDto
import com.calai.app.data.remote.dto.ChatQuotaInfoDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isFallback: Boolean = false
)

data class ChatbotUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Xin chào! Tôi là CalAI Nutrition Coach. Hãy chia sẻ về mục tiêu thể hình, chế độ ăn uống hoặc nhờ tôi gợi ý bữa ăn chuẩn calo & dinh dưỡng nhé! ✨",
            isUser = false
        )
    ),
    val isTyping: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val isUpgrading: Boolean = false,
    val quota: ChatQuotaInfoDto = ChatQuotaInfoDto(),
    val plans: List<ChatPlanDto> = emptyList(),
    val suggestedPrompts: List<String> = listOf(
        "Gợi ý bữa tối dưới 500 kcal giàu đạm",
        "Sau buổi tập gym nên ăn gì?",
        "Bữa phụ Eat Clean khi đói chiều?",
        "Cách tính thâm hụt calo an toàn"
    ),
    val errorMessage: String? = null,
    val upgradeSuccessMessage: String? = null
)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            // 1. Tải hạn mức bản hiện tại (Plus, Pro, Max)
            repository.fetchChatQuota().onSuccess { q ->
                _uiState.update { it.copy(quota = q) }
            }

            // 2. Tải danh mục các bản nâng cấp
            repository.fetchChatPlans().onSuccess { p ->
                _uiState.update { it.copy(plans = p) }
            }

            // 3. Tải lịch sử hội thoại 7 ngày từ DB
            _uiState.update { it.copy(isLoadingHistory = true) }
            repository.fetchChatHistory().onSuccess { history ->
                if (history.messages.isNotEmpty()) {
                    val loaded = history.messages.map { dto ->
                        ChatMessage(
                            id = dto.id,
                            text = dto.content,
                            isUser = dto.role == "user"
                        )
                    }
                    _uiState.update { current ->
                        current.copy(
                            messages = loaded,
                            quota = history.quota ?: current.quota,
                            isLoadingHistory = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingHistory = false) }
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingHistory = false) }
            }
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        val userMessage = ChatMessage(text = trimmed, isUser = true)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isTyping = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = repository.chatAi(trimmed)
            result.onSuccess { response ->
                val aiMessage = ChatMessage(
                    text = response.reply,
                    isUser = false,
                    isFallback = response.isFallback
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + aiMessage,
                        isTyping = false,
                        quota = response.quota ?: it.quota
                    )
                }
            }.onFailure { error ->
                val fallbackReply = ChatMessage(
                    text = "Hiện tại tôi đang gặp chút gián đoạn kết nối. Bạn hãy thử lại sau ít giây hoặc hỏi về các món ăn cụ thể nhé!",
                    isUser = false
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + fallbackReply,
                        isTyping = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }

    fun purchasePlan(packageId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpgrading = true, errorMessage = null) }
            repository.purchaseChatPlan(packageId).onSuccess { updatedQuota ->
                _uiState.update {
                    it.copy(
                        isUpgrading = false,
                        quota = updatedQuota,
                        upgradeSuccessMessage = "Chúc mừng bạn đã nâng cấp thành công lên ${updatedQuota.tierName}!"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isUpgrading = false,
                        errorMessage = error.message ?: "Nâng cấp gói thất bại. Vui lòng thử lại sau."
                    )
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearChatHistory().onSuccess {
                _uiState.update {
                    it.copy(
                        messages = listOf(
                            ChatMessage(
                                text = "Đã làm mới lịch sử trò chuyện. Bạn cần tôi hỗ trợ gì về dinh dưỡng hôm nay nhé! ✨",
                                isUser = false
                            )
                        )
                    )
                }
            }
        }
    }

    fun dismissUpgradeSuccess() {
        _uiState.update { it.copy(upgradeSuccessMessage = null) }
    }
}
