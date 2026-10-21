package com.moneycare.app.ui.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.data.model.personal.PersonalCategoryResponse
import com.moneycare.app.data.model.personal.PersonalTransactionRequest
import com.moneycare.app.data.model.personal.WalletResponse
import com.moneycare.app.data.repository.PersonalFinanceRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonalTransactionUiState(
    val month: String = SimpleDateFormat("yyyy-MM", Locale.US).format(Calendar.getInstance().time),
    val transactions: List<Transaction> = emptyList(),
    val categories: List<PersonalCategoryResponse> = emptyList(),
    val wallets: List<WalletResponse> = emptyList(),
    val selectedType: TransactionType? = null,
    val selectedCategoryId: Long? = null,
    val query: String = "",
    val page: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val selectedTransaction: Transaction? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class PersonalTransactionViewModel(
    private val repository: PersonalFinanceRepository = PersonalFinanceRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(PersonalTransactionUiState())
    val uiState: StateFlow<PersonalTransactionUiState> = _uiState.asStateFlow()

    fun loadInitialData() {
        loadReferenceData()
        refresh()
    }

    fun loadReferenceData() {
        viewModelScope.launch {
            runCatching { repository.getCategories() to repository.getWallets() }
                .onSuccess { (categories, wallets) ->
                    _uiState.update { it.copy(categories = categories, wallets = wallets) }
                }
                .onFailure(::showError)
        }
    }

    fun refresh() = loadPage(page = 0, append = false)

    fun loadMore() {
        val state = _uiState.value
        if (!state.isLoadingMore && state.page + 1 < state.totalPages) {
            loadPage(page = state.page + 1, append = true)
        }
    }

    fun changeMonth(offset: Int) {
        val parsed = SimpleDateFormat("yyyy-MM", Locale.US).parse(_uiState.value.month) ?: return
        val calendar = Calendar.getInstance().apply {
            time = parsed
            add(Calendar.MONTH, offset)
        }
        _uiState.update {
            it.copy(month = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time))
        }
        refresh()
    }

    fun selectType(type: TransactionType?) {
        _uiState.update { it.copy(selectedType = type, selectedCategoryId = null) }
        refresh()
    }

    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        refresh()
    }

    fun updateQuery(query: String) = _uiState.update { it.copy(query = query) }

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedTransaction = null, errorMessage = null) }
            runCatching { repository.getTransaction(id) }
                .onSuccess { transaction ->
                    _uiState.update { it.copy(isLoading = false, selectedTransaction = transaction) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    showError(error)
                }
        }
    }

    fun clearSelectedTransaction() = _uiState.update { it.copy(selectedTransaction = null) }

    fun saveTransaction(
        id: Long?,
        request: PersonalTransactionRequest,
        onSuccess: (Transaction) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                if (id == null) repository.createTransaction(request)
                else repository.updateTransaction(id, request)
            }.onSuccess { saved ->
                _uiState.update { it.copy(isSaving = false, selectedTransaction = saved) }
                refresh()
                onSuccess(saved)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false) }
                showError(error)
            }
        }
    }

    fun deleteTransaction(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.deleteTransaction(id) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, selectedTransaction = null) }
                    refresh()
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    showError(error)
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun loadPage(page: Int, append: Boolean) {
        val filters = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                if (append) it.copy(isLoadingMore = true, errorMessage = null)
                else it.copy(isLoading = true, errorMessage = null)
            }
            runCatching {
                repository.getTransactions(
                    month = filters.month,
                    type = filters.selectedType,
                    categoryId = filters.selectedCategoryId,
                    query = filters.query,
                    page = page
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        transactions = if (append) it.transactions + result.content else result.content,
                        page = result.page,
                        totalPages = result.totalPages,
                        totalElements = result.totalElements,
                        isLoading = false,
                        isLoadingMore = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, isLoadingMore = false) }
                showError(error)
            }
        }
    }

    private fun showError(error: Throwable) {
        _uiState.update {
            it.copy(errorMessage = error.localizedMessage ?: "Không thể tải dữ liệu. Vui lòng thử lại.")
        }
    }
}
