package com.moneycare.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.moneycare.app.ui.theme.MoneyCareTheme

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewHomeScreen() {
    MoneyCareTheme {
        HomeScreen(
            onNavigateToTransactions = {},
            onNavigateToAddTransaction = {},
            onNavigateToStatistics = {},
            onNavigateToProfile = {},
            onNavigateToSavingsGoal = {},
            onNavigateToCreateGroup = {},
            onNavigateToGroupDetail = {},
            onNavigateToNotifications = {},
            onNavigateToSettings = {}
        )
    }
}

