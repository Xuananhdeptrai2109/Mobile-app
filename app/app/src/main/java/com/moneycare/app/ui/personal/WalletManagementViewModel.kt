package com.moneycare.app.ui.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.data.model.personal.WalletResponse
import com.moneycare.app.data.repository.PersonalFinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun normalizeWalletName(value: String): String? =
    value.trim().takeIf { it.isNotEmpty() && it.length <= 100 }

data class WalletManagementUiState(
    val wallets: List<WalletResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class WalletManagementViewModel(
    private val repository: PersonalFinanceRepository = PersonalFinanceRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(WalletManagementUiState())
    val uiState: StateFlow<WalletManagementUiState> = _uiState.asStateFlow()

    fun loadWallets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getWallets() }
                .onSuccess { wallets ->
                    _uiState.update { it.copy(wallets = wallets, isLoading = false) }
                }
                .onFailure(::fail)
        }
    }

    fun saveWallet(
        wallet: WalletResponse?,
        name: String,
        makeDefault: Boolean,
        onSuccess: () -> Unit
    ) {
        val normalizedName = normalizeWalletName(name)
        if (normalizedName == null) {
            _uiState.update { it.copy(errorMessage = "Tên ví phải từ 1 đến 100 ký tự") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                if (wallet == null) {
                    repository.createWallet(normalizedName, makeDefault)
                } else {
                    repository.updateWallet(
                        id = wallet.walletId,
                        name = normalizedName,
                        makeDefault = if (makeDefault && !wallet.isDefault) true else null
                    )
                }
            }.onSuccess {
                _uiState.update { state -> state.copy(isSaving = false) }
                loadWallets()
                onSuccess()
            }.onFailure(::fail)
        }
    }

    fun makeDefault(wallet: WalletResponse) {
        if (wallet.isDefault) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.updateWallet(wallet.walletId, null, true) }
                .onSuccess {
                    _uiState.update { state -> state.copy(isSaving = false) }
                    loadWallets()
                }
                .onFailure(::fail)
        }
    }

    fun deleteWallet(wallet: WalletResponse, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.deleteWallet(wallet.walletId) }
                .onSuccess {
                    _uiState.update { state -> state.copy(isSaving = false) }
                    loadWallets()
                    onSuccess()
                }
                .onFailure(::fail)
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun fail(error: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                errorMessage = error.localizedMessage ?: "Không thể xử lý ví. Vui lòng thử lại."
            )
        }
    }
}
