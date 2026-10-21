package com.moneycare.app.data.repository

import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupTaskSummary
import com.moneycare.app.data.model.PersonalDashboard
import com.moneycare.app.data.model.SavingsGoal

interface ExpenseRepository {
    suspend fun getPersonalDashboard(month: String): PersonalDashboard
    suspend fun getSavingsGoals(): List<SavingsGoal>
    suspend fun getGroups(): List<ExpenseGroup>
    suspend fun getGroupTasks(): List<GroupTaskSummary>
}
