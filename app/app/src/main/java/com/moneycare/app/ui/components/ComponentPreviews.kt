package com.moneycare.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupTaskSummary
import com.moneycare.app.data.model.HomeMode
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.ui.theme.MoneyCareTheme

/**
 * @Preview functions for the main reusable components, using small inline mock data
 * so each can be inspected in isolation in Android Studio's preview pane.
 */

@Preview(showBackground = true)
@Composable
private fun PreviewAppHeader() {
    MoneyCareTheme {
        AppHeader(
            userName = "An Nhiên 🌿",
            hasUnreadNotifications = true,
            onNotificationClick = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSwitcher() {
    MoneyCareTheme {
        PersonalGroupSwitcher(selectedMode = HomeMode.PERSONAL, onModeSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBalanceCard() {
    MoneyCareTheme {
        BalanceCard(balance = 8_450_000, isBalanceVisible = true, onToggleVisibility = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewIncomeExpenseRow() {
    MoneyCareTheme {
        IncomeExpenseRow(income = 12_000_000, expense = 3_550_000)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSavingsGoalCard() {
    MoneyCareTheme {
        SavingsGoalCard(
            goal = SavingsGoal(id = "g1", title = "Mua laptop mới 💻", currentAmount = 7_000_000, targetAmount = 10_000_000),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTransactionItem() {
    MoneyCareTheme {
        TransactionItem(
            transaction = Transaction(
                id = "t1",
                title = "Ăn trưa",
                amount = 50_000,
                type = TransactionType.EXPENSE,
                category = "Ăn uống",
                date = "Hôm nay, 12:30",
                categoryIconName = "fastfood"
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun PreviewGroupCard() {
    MoneyCareTheme {
        GroupCard(
            group = ExpenseGroup(
                id = "g1",
                name = "Đà Nẵng Trip",
                totalExpense = 5_000_000,
                budget = 9_000_000
            ),
            onClick = {},
            onQuickAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewGroupTaskItem() {
    MoneyCareTheme {
        GroupTaskItem(
            task = GroupTaskSummary(
                id = "task1",
                groupId = "g1",
                groupName = "Đà Nẵng Trip",
                members = listOf("Anh", "Minh", "Lan", "Hưng"),
                completed = 3,
                total = 4
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTipCard() {
    MoneyCareTheme {
        TipCard(message = "Hãy thêm chi tiết chi tiêu vào từng nhiệm vụ để tính toán chính xác hơn nhé!", visible = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBottomNav() {
    MoneyCareTheme {
        AppBottomNavigation(selectedItem = BottomNavItem.HOME, onItemSelected = {}, onAddClick = {})
    }
}

