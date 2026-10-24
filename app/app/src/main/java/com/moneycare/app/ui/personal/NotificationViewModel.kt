package com.moneycare.app.ui.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.MoneyCareApp
import com.moneycare.app.data.model.personal.NotificationResponse
import com.moneycare.app.data.repository.PersonalFinanceRepository
import com.moneycare.app.ui.state.LoadState
import com.moneycare.app.ui.state.resolveLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val unreadCount: Long = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val contentState: LoadState<List<NotificationResponse>>
        get() = resolveLoadState(isLoading, notifications, errorMessage)
}

class NotificationViewModel(
    private val repository: PersonalFinanceRepository = MoneyCareApp.instance.container.personalFinanceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadUnreadCount() {
        viewModelScope.launch {
            runCatching { repository.getUnreadNotificationCount() }
                .onSuccess { count -> _uiState.update { it.copy(unreadCount = count) } }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getNotifications() }
                .onSuccess { notifications ->
                    _uiState.update {
                        it.copy(
                            notifications = notifications,
                            unreadCount = notifications.count { item -> !item.isRead }.toLong(),
                            isLoading = false
                        )
                    }
                }
                .onFailure(::fail)
        }
    }

    fun markRead(notification: NotificationResponse) {
        if (notification.isRead) return
        viewModelScope.launch {
            runCatching { repository.markNotificationRead(notification.notificationId) }
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.notificationId == updated.notificationId) updated else it
                            },
                            unreadCount = (state.unreadCount - 1).coerceAtLeast(0)
                        )
                    }
                }
                .onFailure(::fail)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            runCatching { repository.markAllNotificationsRead() }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(isRead = true) },
                            unreadCount = 0
                        )
                    }
                }
                .onFailure(::fail)
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun fail(error: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = error.localizedMessage ?: "Không thể tải thông báo."
            )
        }
    }
}
