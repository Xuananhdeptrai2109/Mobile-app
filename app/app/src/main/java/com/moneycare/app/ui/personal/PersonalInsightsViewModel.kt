package com.moneycare.app.ui.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.personal.*
import com.moneycare.app.data.repository.PersonalFinanceRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PersonalInsightsUiState(
    val month: String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date()),
    val statistics: PersonalStatisticsResponse? = null,
    val goals: List<SavingsGoal> = emptyList(),
    val includeCompletedGoals: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class PersonalInsightsViewModel(
    private val repository: PersonalFinanceRepository = PersonalFinanceRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(PersonalInsightsUiState())
    val uiState: StateFlow<PersonalInsightsUiState> = _uiState.asStateFlow()

    fun loadStatistics() {
        val month = _uiState.value.month
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getStatistics(month) }
                .onSuccess { statistics ->
                    _uiState.update { it.copy(isLoading = false, statistics = statistics) }
                }
                .onFailure { fail(it) }
        }
    }

    fun changeMonth(offset: Int) {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
        val date = formatter.parse(_uiState.value.month) ?: return
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.MONTH, offset)
        }
        _uiState.update { it.copy(month = formatter.format(calendar.time)) }
        loadStatistics()
    }

    fun saveBudget(amount: Long, onSuccess: () -> Unit = {}) {
        val month = _uiState.value.month
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.updateBudget(month, amount) }
                .onSuccess {
                    _uiState.update { state -> state.copy(isSaving = false) }
                    loadStatistics()
                    onSuccess()
                }
                .onFailure { fail(it) }
        }
    }

    fun deleteBudget(onSuccess: () -> Unit = {}) {
        val month = _uiState.value.month
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.deleteBudget(month) }
                .onSuccess {
                    _uiState.update { state -> state.copy(isSaving = false) }
                    loadStatistics()
                    onSuccess()
                }
                .onFailure { fail(it) }
        }
    }

    fun loadGoals() {
        val includeCompleted = _uiState.value.includeCompletedGoals
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getSavingsGoals(includeCompleted) }
                .onSuccess { goals -> _uiState.update { it.copy(isLoading = false, goals = goals) } }
                .onFailure { fail(it) }
        }
    }

    fun setIncludeCompleted(include: Boolean) {
        _uiState.update { it.copy(includeCompletedGoals = include) }
        loadGoals()
    }

    fun saveGoal(
        id: Long?,
        title: String,
        emoji: String,
        targetAmount: Long,
        targetDate: String?,
        onSuccess: () -> Unit
    ) {
        val request = SavingsGoalRequest(
            title = title.trim(),
            emoji = emoji.trim().takeIf(String::isNotEmpty),
            targetAmount = targetAmount,
            targetDate = targetDate
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                if (id == null) repository.createSavingsGoal(request)
                else repository.updateSavingsGoal(id, request)
            }.onSuccess {
                _uiState.update { state -> state.copy(isSaving = false) }
                loadGoals()
                onSuccess()
            }.onFailure { fail(it) }
        }
    }

    fun contribute(
        id: Long,
        amount: Long,
        operation: SavingsOperation,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.contributeSavingsGoal(id, amount, operation) }
                .onSuccess {
                    _uiState.update { state -> state.copy(isSaving = false) }
                    loadGoals()
                    onSuccess()
                }
                .onFailure { fail(it) }
        }
    }

    fun deleteGoal(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.deleteSavingsGoal(id) }
                .onSuccess {
                    _uiState.update { state -> state.copy(isSaving = false) }
                    loadGoals()
                    onSuccess()
                }
                .onFailure { fail(it) }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun fail(error: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                errorMessage = error.localizedMessage ?: "Không thể tải dữ liệu. Vui lòng thử lại."
            )
        }
    }
}
