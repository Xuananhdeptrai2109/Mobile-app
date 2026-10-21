package com.moneycare.app.data.repository

import com.moneycare.app.MoneyCareApp
import com.moneycare.app.data.api.ApiService
import com.moneycare.app.data.api.RetrofitClient
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupTaskSummary
import com.moneycare.app.data.model.PersonalDashboard
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.group.toUiModel
import com.moneycare.app.util.SessionManager

class HybridExpenseRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) : ExpenseRepository {
    private val personalRepository = PersonalFinanceRepository(apiService)

    private val sessionManager: SessionManager?
        get() = try { MoneyCareApp.instance.sessionManager } catch (e: Throwable) { null }

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
        return emptyList()
    }
}
