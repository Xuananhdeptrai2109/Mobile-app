package com.moneycare.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupTaskSummary
import com.moneycare.app.data.model.HomeMode
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.data.repository.ExpenseRepository
import com.moneycare.app.data.repository.HybridExpenseRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class HomeUiState(
    val selectedMode: HomeMode = HomeMode.PERSONAL,
    val isBalanceVisible: Boolean = true,
    val isLoading: Boolean = false,
    val month: String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date()),
    val balance: Long = 0,
    val income: Long = 0,
    val expense: Long = 0,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val groups: List<ExpenseGroup> = emptyList(),
    val groupTasks: List<GroupTaskSummary> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: ExpenseRepository = HybridExpenseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun selectMode(mode: HomeMode) = _uiState.update { it.copy(selectedMode = mode) }

    fun toggleBalanceVisibility() =
        _uiState.update { it.copy(isBalanceVisible = !it.isBalanceVisible) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun changeMonth(offset: Int) {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
        val parsed = formatter.parse(_uiState.value.month) ?: return
        val calendar = java.util.Calendar.getInstance().apply {
            time = parsed
            add(java.util.Calendar.MONTH, offset)
        }
        _uiState.update { it.copy(month = formatter.format(calendar.time)) }
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val month = _uiState.value.month
            supervisorScope {
                val dashboard = async { runCatching { repository.getPersonalDashboard(month) } }
                val groups = async { runCatching { repository.getGroups() } }
                val goals = async { runCatching { repository.getSavingsGoals() } }
                val tasks = async { runCatching { repository.getGroupTasks() } }

                val dashboardResult = dashboard.await()
                val groupsResult = groups.await()
                val goalsResult = goals.await()
                val tasksResult = tasks.await()
                val firstError = dashboardResult.exceptionOrNull() ?: groupsResult.exceptionOrNull()

                _uiState.update { current ->
                    val personal = dashboardResult.getOrNull()
                    current.copy(
                        isLoading = false,
                        month = personal?.month ?: current.month,
                        balance = personal?.balance ?: current.balance,
                        income = personal?.income ?: current.income,
                        expense = personal?.expense ?: current.expense,
                        recentTransactions = personal?.recentTransactions ?: current.recentTransactions,
                        savingsGoals = goalsResult.getOrDefault(current.savingsGoals),
                        groups = groupsResult.getOrDefault(current.groups),
                        groupTasks = tasksResult.getOrDefault(current.groupTasks),
                        errorMessage = firstError?.localizedMessage
                    )
                }
            }
        }
    }
}
