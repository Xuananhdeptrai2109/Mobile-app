package com.moneycare.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.ExpenseSplit
import com.moneycare.app.data.model.ExpenseSplitMethod
import com.moneycare.app.data.model.GroupExpense
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.data.model.GroupTask
import com.moneycare.app.data.model.MemberStats
import com.moneycare.app.data.model.group.GroupActivityResponse
import com.moneycare.app.data.repository.RealGroupRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

enum class GroupDetailTab(val label: String) {
    OVERVIEW("Tổng quan"),
    EXPENSES("Chi tiêu"),
    SETTLEMENT("Quyết toán"),
    TASKS("Nhiệm vụ"),
    MEMBERS("Thành viên"),
    ACTIVITY("Hoạt động")
}

enum class ExpenseFilter(val label: String) {
    ALL("Tất cả"),
    MY_EXPENSES("Của tôi"),
    UNPAID("Chưa thanh toán")
}

data class GroupDetailUiState(
    val group: ExpenseGroup? = null,
    val selectedTab: GroupDetailTab = GroupDetailTab.OVERVIEW,
    val selectedExpenseFilter: ExpenseFilter = ExpenseFilter.ALL,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isAiAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    
    // For Expense/Member/Task detail
    val selectedExpense: GroupExpense? = null,
    val selectedMember: GroupMember? = null,
    val selectedTask: GroupTask? = null,
    val memberStats: MemberStats? = null,
    val memberExpenses: List<GroupExpense> = emptyList(),
    val activities: List<GroupActivityResponse> = emptyList()
)

class GroupDetailViewModel(
    private val repository: RealGroupRepository = RealGroupRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val group = repository.getGroup(groupId)
                group to runCatching { repository.getGroupActivities(groupId) }.getOrDefault(emptyList())
            }
                .onSuccess { (group, activities) ->
                    _uiState.update { it.copy(group = group, activities = activities, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Đã có lỗi xảy ra")
                    }
                }
        }
    }

    fun selectTab(tab: GroupDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectExpenseFilter(filter: ExpenseFilter) {
        _uiState.update { it.copy(selectedExpenseFilter = filter) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun updateGroup(
        name: String,
        description: String,
        budget: Long,
        onSuccess: () -> Unit = {}
    ) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.updateGroup(group.id, name, description, budget) }
                .onSuccess { updated ->
                    _uiState.update {
                        val current = it.group ?: group
                        it.copy(
                            group = current.copy(
                                name = updated.name,
                                description = updated.description,
                                budget = updated.budget,
                                heroImageUrl = updated.heroImageUrl ?: current.heroImageUrl
                            ),
                            isSaving = false
                        )
                    }
                    refreshActivities(group.id)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.toUserMessage("Không thể cập nhật nhóm"))
                    }
                }
        }
    }

    fun deleteGroup(onSuccess: () -> Unit) = runGroupAction(
        errorMessage = "Không thể xóa nhóm",
        action = { group -> repository.deleteGroup(group.id) },
        onSuccess = onSuccess
    )

    private fun runGroupAction(
        errorMessage: String,
        action: suspend (ExpenseGroup) -> Unit,
        onSuccess: () -> Unit
    ) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { action(group) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.toUserMessage(errorMessage))
                    }
                }
        }
    }

    fun loadExpenseDetail(expenseId: String) {
        val expense = _uiState.value.group?.expenses?.find { it.id == expenseId }
        _uiState.update { it.copy(selectedExpense = expense) }
    }

    fun loadMemberDetail(memberId: String) {
        val group = _uiState.value.group ?: return
        val member = group.members.find { it.id == memberId }
        val expenses = group.expenses.filter { expense ->
            expense.paidBy.id == memberId || expense.splitMembers.any { it.member.id == memberId }
        }
        
        val totalPaid = expenses.filter { it.paidBy.id == memberId }.sumOf { it.amount }
        val totalOwed = expenses.flatMap { it.splitMembers }
            .filter { it.member.id == memberId && !it.isPaid }
            .sumOf { it.amount }
            
        val stats = MemberStats(
            totalPaid = totalPaid,
            totalOwed = totalOwed,
            balance = totalPaid - totalOwed
        )
        
        _uiState.update { it.copy(
            selectedMember = member,
            memberStats = stats,
            memberExpenses = expenses
        ) }
    }

    fun loadTaskDetail(taskId: String) {
        val task = _uiState.value.group?.tasks?.find { it.id == taskId }
        _uiState.update { it.copy(selectedTask = task) }
    }

    fun toggleTaskStatus(taskId: String) {
        val group = _uiState.value.group ?: return
        val updatedTasks = group.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(isCompleted = !task.isCompleted)
            } else task
        }
        _uiState.update { it.copy(group = group.copy(tasks = updatedTasks)) }
        viewModelScope.launch {
            try {
                repository.toggleTask(group.id, taskId)
                refreshActivities(group.id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        group = group,
                        errorMessage = e.toUserMessage("Không thể cập nhật nhiệm vụ")
                    )
                }
            }
        }
    }

    fun addTask(
        title: String,
        assignee: GroupMember,
        dueDate: String,
        description: String?,
        onSuccess: () -> Unit = {}
    ) {
        val group = _uiState.value.group ?: return
        val assigneeMemberId = assignee.id.toLongOrNull()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val newTask = repository.addTask(
                    groupId = group.id,
                    title = title,
                    assigneeMemberId = assigneeMemberId,
                    dueDate = dueDate,
                    description = description
                )
                _uiState.update {
                    val currentGroup = it.group ?: group
                    it.copy(
                        group = currentGroup.copy(tasks = currentGroup.tasks + newTask),
                        isSaving = false,
                        selectedTab = GroupDetailTab.TASKS
                    )
                }
                refreshActivities(group.id)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Không thể tạo nhiệm vụ")
                    )
                }
            }
        }
    }

    fun updateTask(
        taskId: String,
        title: String,
        assignee: GroupMember,
        dueDate: String,
        description: String?,
        onSuccess: () -> Unit = {}
    ) {
        val group = _uiState.value.group ?: return
        val assigneeMemberId = assignee.id.toLongOrNull()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val updatedTask = repository.updateTask(
                    groupId = group.id,
                    taskId = taskId,
                    title = title,
                    assigneeMemberId = assigneeMemberId,
                    dueDate = dueDate,
                    description = description
                )
                _uiState.update {
                    val currentGroup = it.group ?: group
                    it.copy(
                        group = currentGroup.copy(
                            tasks = currentGroup.tasks.map { task ->
                                if (task.id == taskId) updatedTask else task
                            }
                        ),
                        isSaving = false,
                        selectedTab = GroupDetailTab.TASKS
                    )
                }
                refreshActivities(group.id)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Không thể cập nhật nhiệm vụ")
                    )
                }
            }
        }
    }

    fun deleteTask(taskId: String, onSuccess: () -> Unit = {}) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                repository.deleteTask(group.id, taskId)
                _uiState.update {
                    val currentGroup = it.group ?: group
                    it.copy(
                        group = currentGroup.copy(
                            tasks = currentGroup.tasks.filterNot { task -> task.id == taskId }
                        ),
                        isSaving = false,
                        selectedTab = GroupDetailTab.TASKS
                    )
                }
                refreshActivities(group.id)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Không thể xóa nhiệm vụ")
                    )
                }
            }
        }
    }

    fun addMember(name: String, onSuccess: () -> Unit = {}) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            runCatching {
                repository.addMember(group.id, name)
            }.onSuccess { newMember ->
                _uiState.update {
                    val currentGroup = it.group ?: group
                    it.copy(group = currentGroup.copy(members = currentGroup.members + newMember))
                }
                refreshActivities(group.id)
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.toUserMessage("Không thể thêm thành viên"))
                }
            }
        }
    }

    fun updateMember(memberId: String, name: String, onSuccess: () -> Unit = {}) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.updateMember(group.id, memberId, name) }
                .onSuccess { updated ->
                    _uiState.update {
                        val current = it.group ?: group
                        it.copy(
                            group = current.copy(members = current.members.map { member ->
                                if (member.id == memberId) updated.copy(isCurrentUser = member.isCurrentUser) else member
                            }),
                            isSaving = false
                        )
                    }
                    refreshActivities(group.id)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.toUserMessage("Không thể sửa thành viên"))
                    }
                }
        }
    }

    fun removeMember(memberId: String, onSuccess: () -> Unit = {}) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.removeMember(group.id, memberId) }
                .onSuccess {
                    _uiState.update { state ->
                        val current = state.group ?: group
                        state.copy(
                            group = current.copy(
                                members = current.members.filterNot { member -> member.id == memberId },
                                tasks = current.tasks.map { task ->
                                    if (task.assignee.id == memberId) {
                                        task.copy(assignee = GroupMember("0", "Chưa phân công"))
                                    } else task
                                }
                            ),
                            selectedMember = null,
                            isSaving = false
                        )
                    }
                    refreshActivities(group.id)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.toUserMessage("Không thể xóa thành viên"))
                    }
                }
        }
    }

    fun addExpense(
        title: String,
        amount: Long,
        paidBy: GroupMember,
        splitMembers: List<ExpenseSplit>,
        splitMethod: ExpenseSplitMethod = ExpenseSplitMethod.EQUAL,
        note: String?,
        onSuccess: () -> Unit = {}
    ) {
        val group = _uiState.value.group ?: return
        val isoDate = java.time.LocalDate.now().toString()

        viewModelScope.launch {
            val paidById = paidBy.id.toLongOrNull()
            if (paidById == null) {
                _uiState.update { it.copy(errorMessage = "Người thanh toán không hợp lệ") }
                return@launch
            }
            val splitIds = splitMembers.mapNotNull { it.member.id.toLongOrNull() }
            val finalSplitIds = if (splitIds.isEmpty()) listOf(paidById) else splitIds

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                repository.addExpense(
                    groupId = group.id,
                    title = title,
                    amount = amount,
                    paidByMemberId = paidById,
                    splitMemberIds = finalSplitIds,
                    splitMethod = splitMethod,
                    splitMembers = splitMembers,
                    date = isoDate,
                    note = note
                )
            }.onSuccess { newExpense ->
                _uiState.update {
                    val currentGroup = it.group ?: group
                    val updatedExpense = enrichExpense(newExpense, currentGroup)
                    val updatedExpenses = currentGroup.expenses + updatedExpense
                    it.copy(
                        group = currentGroup.copy(
                            expenses = updatedExpenses,
                            totalExpense = updatedExpenses.sumOf { expense -> expense.amount }
                        ),
                        isSaving = false,
                        selectedTab = GroupDetailTab.EXPENSES
                    )
                }
                refreshActivities(group.id)
                onSuccess()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Không thể tạo chi tiêu")
                    )
                }
            }
        }
    }

    fun updateExpense(
        expenseId: String,
        title: String,
        amount: Long,
        paidBy: GroupMember,
        splitMembers: List<ExpenseSplit>,
        splitMethod: ExpenseSplitMethod = ExpenseSplitMethod.EQUAL,
        note: String?,
        onSuccess: () -> Unit = {}
    ) {
        val group = _uiState.value.group ?: return
        val existing = group.expenses.find { it.id == expenseId } ?: return
        val paidById = paidBy.id.toLongOrNull()
        if (paidById == null) {
            showMessage("Người thanh toán không hợp lệ")
            return
        }
        val splitIds = splitMembers.mapNotNull { it.member.id.toLongOrNull() }
        if (splitIds.isEmpty()) {
            showMessage("Phải chọn ít nhất một người để chia tiền")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                repository.updateExpense(
                    group.id,
                    expenseId,
                    title,
                    amount,
                    paidById,
                    splitIds,
                    splitMethod,
                    splitMembers,
                    existing.date,
                    note
                )
            }.onSuccess { response ->
                _uiState.update { state ->
                    val current = state.group ?: group
                    val updated = enrichExpense(response, current)
                    val expenses = current.expenses.map { expense ->
                        if (expense.id == expenseId) updated else expense
                    }
                    state.copy(
                        group = current.copy(
                            expenses = expenses,
                            totalExpense = expenses.sumOf { it.amount }
                        ),
                        selectedExpense = updated,
                        isSaving = false
                    )
                }
                refreshActivities(group.id)
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = error.toUserMessage("Không thể cập nhật chi tiêu"))
                }
            }
        }
    }

    fun deleteExpense(expenseId: String, onSuccess: () -> Unit = {}) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.deleteExpense(group.id, expenseId) }
                .onSuccess {
                    _uiState.update { state ->
                        val current = state.group ?: group
                        val expenses = current.expenses.filterNot { expense -> expense.id == expenseId }
                        state.copy(
                            group = current.copy(
                                expenses = expenses,
                                totalExpense = expenses.sumOf { it.amount }
                            ),
                            selectedExpense = null,
                            isSaving = false
                        )
                    }
                    refreshActivities(group.id)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.toUserMessage("Không thể xóa chi tiêu"))
                    }
                }
        }
    }

    fun toggleSplitPayment(expenseId: String, splitId: String) {
        val group = _uiState.value.group ?: return
        if (splitId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.toggleSplitPayment(group.id, expenseId, splitId) }
                .onSuccess { response ->
                    _uiState.update { state ->
                        val current = state.group ?: group
                        val expenses = current.expenses.map { expense ->
                            if (expense.id != expenseId) expense else {
                                val splits = expense.splitMembers.map { split ->
                                    if (split.id == splitId) split.copy(isPaid = response.isPaid) else split
                                }
                                expense.copy(
                                    splitMembers = splits,
                                    isSettled = splits.isNotEmpty() && splits.all { it.isPaid }
                                )
                            }
                        }
                        state.copy(
                            group = current.copy(expenses = expenses),
                            selectedExpense = expenses.find { it.id == expenseId },
                            isSaving = false
                        )
                    }
                    refreshActivities(group.id)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.toUserMessage("Không thể cập nhật thanh toán")
                        )
                    }
                }
        }
    }

    fun syncTripToPersonalWallet(
        walletId: Long,
        categoryId: Long,
        amount: Long,
        onSuccess: () -> Unit
    ) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val personalRepo = com.moneycare.app.data.repository.PersonalFinanceRepository()
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                personalRepo.createTransaction(
                    com.moneycare.app.data.model.personal.PersonalTransactionRequest(
                        title = "Chi tiêu chuyến đi: ${group.name}",
                        amount = amount,
                        type = com.moneycare.app.data.model.TransactionType.EXPENSE,
                        categoryId = categoryId,
                        walletId = walletId,
                        note = "Tự động đồng bộ chi phí từ nhóm ${group.name}",
                        transactionTime = timestamp
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.toUserMessage("Không thể đồng bộ vào ví cá nhân")
                    )
                }
            }
        }
    }

    fun settleDebtBetweenMembers(fromMemberId: String, toMemberId: String, onSuccess: () -> Unit = {}) {
        val group = _uiState.value.group ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val updatedExpenses = group.expenses.map { expense ->
                    if (expense.paidBy.id == toMemberId) {
                        val newSplits = expense.splitMembers.map { split ->
                            if (split.member.id == fromMemberId && !split.isPaid) {
                                if (split.id.isNotBlank()) {
                                    runCatching { repository.toggleSplitPayment(group.id, expense.id, split.id) }
                                }
                                split.copy(isPaid = true)
                            } else split
                        }
                        expense.copy(
                            splitMembers = newSplits,
                            isSettled = newSplits.isNotEmpty() && newSplits.all { it.isPaid }
                        )
                    } else expense
                }
                group.copy(expenses = updatedExpenses)
            }.onSuccess { updatedGroup ->
                _uiState.update { it.copy(group = updatedGroup, isSaving = false) }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.toUserMessage("Không thể cập nhật quyết toán")
                    )
                }
            }
        }
    }

    private fun enrichExpense(expense: GroupExpense, group: ExpenseGroup): GroupExpense {
        val payer = group.members.find { it.id == expense.paidBy.id } ?: expense.paidBy
        return expense.copy(
            paidBy = payer,
            splitMembers = expense.splitMembers.map { split ->
                split.copy(member = group.members.find { it.id == split.member.id } ?: split.member)
            }
        )
    }

    private fun refreshActivities(groupId: String) {
        viewModelScope.launch {
            runCatching { repository.getGroupActivities(groupId) }
                .onSuccess { activities -> _uiState.update { it.copy(activities = activities) } }
        }
    }

    fun extractExpenseFromReceipt(
        context: android.content.Context,
        uri: android.net.Uri,
        onExtracted: (String, Long, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalyzing = true, errorMessage = null) }
            runCatching {
                repository.extractFromReceipt(context, uri)
            }.onSuccess { response ->
                _uiState.update { it.copy(isAiAnalyzing = false) }
                val name = response.name ?: response.merchantName ?: ""
                val amount = response.amount ?: 0L
                val note = response.note
                if (!name.isNullOrBlank() || amount > 0L) {
                    onExtracted(name, amount, note)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isAiAnalyzing = false,
                        errorMessage = "Không thể phân tích hóa đơn: ${e.localizedMessage ?: "Lỗi kết nối AI"}"
                    )
                }
            }
        }
    }

    fun extractExpenseFromAudio(
        context: android.content.Context,
        uri: android.net.Uri,
        onExtracted: (String, Long, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalyzing = true, errorMessage = null) }
            runCatching {
                repository.extractFromAudio(context, uri)
            }.onSuccess { response ->
                _uiState.update { it.copy(isAiAnalyzing = false) }
                val name = response.name ?: ""
                val amount = response.amount ?: 0L
                val note = response.note
                if (!name.isNullOrBlank() || amount > 0L) {
                    onExtracted(name, amount, note)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isAiAnalyzing = false,
                        errorMessage = "Không thể phân tích giọng nói: ${e.localizedMessage ?: "Lỗi kết nối AI"}"
                    )
                }
            }
        }
    }
}

private fun Throwable.toUserMessage(fallback: String): String {
    if (this is HttpException) {
        val apiMessage = runCatching {
            response()?.errorBody()?.string()?.let { body: String ->
                val values: Map<*, *> = Gson().fromJson(body, Map::class.java)
                values["message"]?.toString()
            }
        }.getOrNull()
        if (!apiMessage.isNullOrBlank()) return apiMessage
    }
    return localizedMessage?.takeIf { it.isNotBlank() } ?: fallback
}
