package com.moneycare.app.data.repository

import com.moneycare.app.data.api.ApiService
import com.moneycare.app.data.api.RetrofitClient
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupTaskSummary
import com.moneycare.app.data.model.PersonalDashboard
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.group.toUiModel
import com.moneycare.app.util.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class HybridExpenseRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val personalRepository: PersonalFinanceRepository = PersonalFinanceRepository(apiService),
    private val sessionManagerProvider: () -> SessionManager? = {
        try { com.moneycare.app.MoneyCareApp.instance.sessionManager } catch (_: Throwable) { null }
    }
) : ExpenseRepository {
    private val sessionManager: SessionManager?
        get() = sessionManagerProvider()

    override suspend fun getPersonalDashboard(month: String): PersonalDashboard =
        personalRepository.getDashboard(month)

    override suspend fun getSavingsGoals(): List<SavingsGoal> =
        personalRepository.getSavingsGoals(includeCompleted = false)

    override suspend fun getGroups(): List<ExpenseGroup> {
        if (sessionManager?.isLoggedIn() != true) {
            throw IllegalStateException("Bạn cần đăng nhập lại để tải dữ liệu")
        }
        return apiService.getMyGroups().map { it.toUiModel() }
    }

    override suspend fun getGroupTasks(): List<GroupTaskSummary> {
        if (sessionManager?.isLoggedIn() != true) {
            throw IllegalStateException("Bạn cần đăng nhập lại để tải dữ liệu")
        }
        return coroutineScope {
            apiService.getMyGroups().map { group ->
                async {
                    val tasks = runCatching { apiService.getGroupTasks(group.groupId) }
                        .getOrDefault(emptyList())
                    GroupTaskSummary(
                        id = "group-${group.groupId}",
                        groupId = group.groupId.toString(),
                        groupName = group.name,
                        members = group.members.map { it.fullName }.distinct(),
                        completed = tasks.count { it.isCompleted },
                        total = tasks.size
                    )
                }
            }.awaitAll()
        }
    }
}
