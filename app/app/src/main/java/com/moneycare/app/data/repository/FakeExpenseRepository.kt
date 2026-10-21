package com.moneycare.app.data.repository

import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupTaskSummary
import com.moneycare.app.data.model.PersonalDashboard
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.data.model.TransactionType

/** Fake data source for previews and isolated tests only. */
class FakeExpenseRepository : ExpenseRepository {
    override suspend fun getPersonalDashboard(month: String) = PersonalDashboard(
        month = month,
        balance = 8_450_000,
        income = 12_000_000,
        expense = 3_550_000,
        recentTransactions = listOf(
            Transaction(
                id = "t1",
                title = "Ăn trưa",
                amount = 50_000,
                type = TransactionType.EXPENSE,
                category = "Ăn uống",
                date = "Hôm nay, 12:30",
                categoryIconName = "fastfood"
            ),
            Transaction(
                id = "t2",
                title = "Lương tháng này",
                amount = 12_000_000,
                type = TransactionType.INCOME,
                category = "Lương",
                date = "Hôm qua, 08:00",
                categoryIconName = "payments"
            )
        )
    )

    override suspend fun getSavingsGoals(): List<SavingsGoal> = emptyList()

    override suspend fun getGroups(): List<ExpenseGroup> = emptyList()

    override suspend fun getGroupTasks(): List<GroupTaskSummary> = emptyList()
}
