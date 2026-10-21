package com.moneycare.app.ui.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupExpense
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.ui.theme.TextPrimary
import com.moneycare.app.viewmodel.ExpenseFilter
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun GroupExpenseContent(
    group: ExpenseGroup,
    currentUser: GroupMember?,
    selectedFilter: ExpenseFilter,
    onFilterSelected: (ExpenseFilter) -> Unit,
    onAddExpense: () -> Unit,
    onExpenseClick: (GroupExpense) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredExpenses = remember(group.expenses, selectedFilter, currentUser) {
        when (selectedFilter) {
            ExpenseFilter.ALL -> group.expenses
            ExpenseFilter.MY_EXPENSES -> group.expenses.filter {
                it.paidBy == currentUser || it.paidBy.name == "Bạn"
            }
            ExpenseFilter.UNPAID -> group.expenses.filter { !it.isSettled }
        }
    }
    val expensesByMonth = remember(filteredExpenses) {
        filteredExpenses.groupBy { expense -> expense.date.take(7) }
            .toSortedMap(compareByDescending { it })
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ExpenseFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
        }

        item {
            GroupBudgetCard(
                totalExpense = group.totalExpense,
                budget = group.budget,
                showAddButton = true,
                onAddClick = onAddExpense
            )
        }

        if (expensesByMonth.isEmpty()) {
            item {
                Text(
                    text = "Chưa có chi tiêu phù hợp",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        expensesByMonth.forEach { (month, expenses) ->
            item(key = "month_$month") {
                val label = runCatching {
                    YearMonth.parse(month).format(
                        DateTimeFormatter.ofPattern("'Tháng' M, yyyy", Locale.forLanguageTag("vi"))
                    )
                }.getOrDefault("Khác")
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(expenses, key = { it.id }) { expense ->
                GroupExpenseItem(
                    expense = expense,
                    modifier = Modifier.clickable { onExpenseClick(expense) }
                )
            }
        }
    }
}
