package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.NotificationDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationUiState(
    val isLoading: Boolean = false,
    val items: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getNotifications()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, items = list.items, unreadCount = list.unreadCount) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Không thể tải thông báo") }
                }
        }
    }

    fun markRead(id: String) {
        // Optimistic update — người dùng thấy phản hồi ngay, rollback nếu API lỗi.
        val previous = _uiState.value
        _uiState.update { state ->
            state.copy(
                items = state.items.map { if (it.id == id && !it.isRead) it.copy(isRead = true) else it },
                unreadCount = (state.unreadCount - (if (previous.items.find { it.id == id }?.isRead == false) 1 else 0)).coerceAtLeast(0)
            )
        }
        viewModelScope.launch {
            repository.markNotificationRead(id).onFailure { _uiState.value = previous }
        }
    }

    fun markAllRead() {
        val previous = _uiState.value
        _uiState.update { state -> state.copy(items = state.items.map { it.copy(isRead = true) }, unreadCount = 0) }
        viewModelScope.launch {
            repository.markAllNotificationsRead().onFailure { _uiState.value = previous }
        }
    }

    fun delete(id: String) {
        val previous = _uiState.value
        _uiState.update { state ->
            val removed = state.items.find { it.id == id }
            state.copy(
                items = state.items.filterNot { it.id == id },
                unreadCount = (state.unreadCount - (if (removed?.isRead == false) 1 else 0)).coerceAtLeast(0)
            )
        }
        viewModelScope.launch {
            repository.deleteNotification(id).onFailure { _uiState.value = previous }
        }
    }
}
