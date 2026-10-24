package com.moneycare.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.MoneyCareApp
import com.moneycare.app.data.model.group.GroupReportResponse
import com.moneycare.app.data.repository.RealGroupRepository
import com.moneycare.app.ui.state.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupReportUiState(
    val report: GroupReportResponse? = null,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val errorMessage: String? = null
) {
    val contentState: LoadState<GroupReportResponse>
        get() = when {
            isLoading && report == null -> LoadState.Loading
            errorMessage != null && report == null -> LoadState.Error(errorMessage)
            report == null -> LoadState.Empty
            else -> LoadState.Success(report)
        }
}

class GroupReportViewModel(
    private val repository: RealGroupRepository = MoneyCareApp.instance.container.groupRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupReportUiState())
    val uiState = _uiState.asStateFlow()

    fun load(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getGroupReport(groupId) }
                .onSuccess { report -> _uiState.update { it.copy(report = report, isLoading = false) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Không thể tải báo cáo") }
                }
        }
    }

    fun export(groupId: String, onReady: (ByteArray) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            runCatching { repository.exportGroupReport(groupId) }
                .onSuccess { bytes ->
                    _uiState.update { it.copy(isExporting = false) }
                    onReady(bytes)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isExporting = false, errorMessage = error.localizedMessage ?: "Không thể xuất báo cáo") }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
