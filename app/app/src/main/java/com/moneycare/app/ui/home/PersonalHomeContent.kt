package com.moneycare.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.ui.components.BalanceCard
import com.moneycare.app.ui.components.IncomeExpenseRow
import com.moneycare.app.ui.components.SavingsGoalCard
import com.moneycare.app.ui.components.SectionHeader
import com.moneycare.app.ui.components.TransactionItem

@Composable
fun PersonalHomeContent(
    month: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    balance: Long,
    isBalanceVisible: Boolean,
    onToggleBalanceVisibility: () -> Unit,
    income: Long,
    expense: Long,
    savingsGoals: List<SavingsGoal>,
    recentTransactions: List<Transaction>,
    onSavingsGoalClick: (SavingsGoal) -> Unit,
    onManageSavingsGoalsClick: () -> Unit,
    onSeeAllTransactionsClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Tháng trước")
                }
                Text(
                    text = month.toMonthLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Tháng sau")
                }
            }
        }
        item {
            BalanceCard(
                balance = balance,
                isBalanceVisible = isBalanceVisible,
                onToggleVisibility = onToggleBalanceVisibility
            )
        }
        item { IncomeExpenseRow(income = income, expense = expense) }

        item {
            SectionHeader(
                title = "Mục tiêu tiết kiệm",
                actionLabel = "Quản lý",
                onActionClick = onManageSavingsGoalsClick
            )
        }
        if (savingsGoals.isNotEmpty()) {
            items(savingsGoals, key = { it.id }) { goal ->
                SavingsGoalCard(goal = goal, onClick = { onSavingsGoalClick(goal) })
            }
        } else {
            item {
                Text(
                    text = "Tạo mục tiêu đầu tiên để theo dõi tiến độ tiết kiệm.",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SectionHeader(
                title = "Giao dịch gần đây",
                actionLabel = "Xem tất cả",
                onActionClick = onSeeAllTransactionsClick
            )
        }
        if (recentTransactions.isEmpty()) {
            item {
                Text(
                    text = "Chưa có giao dịch trong tháng này",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recentTransactions, key = { it.id }) { transaction ->
                TransactionItem(transaction = transaction, onClick = { onTransactionClick(transaction) })
            }
        }
        item { Column(Modifier) {} }
    }
}

private fun String.toMonthLabel(): String {
    val parts = split("-")
    return if (parts.size == 2) {
        "Tổng quan tháng ${parts[1].toIntOrNull() ?: parts[1]}/${parts[0]}"
    } else this
}
