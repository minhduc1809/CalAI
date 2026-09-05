package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val suggestedPrompts: List<String> = listOf(
        "Gợi ý bữa tối dưới 500 kcal giàu đạm",
        "Sau buổi tập gym nên ăn gì?",
        "Bữa phụ Eat Clean khi đói chiều?",
        "Cách tính thâm hụt calo an toàn"
    ),
    val errorMessage: String? = null
)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

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
                        isTyping = false
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
}
