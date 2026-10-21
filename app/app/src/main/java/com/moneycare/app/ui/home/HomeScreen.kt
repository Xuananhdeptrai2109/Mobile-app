package com.moneycare.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneycare.app.MoneyCareApp
import com.moneycare.app.data.model.HomeMode
import com.moneycare.app.ui.components.*
import com.moneycare.app.util.SessionManager

/**
 * Root home screen: header, personal/group switcher, animated content, and bottom nav.
 * All state comes from [HomeViewModel]; this Composable stays purely presentational.
 */
@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSavingsGoal: () -> Unit = {},
    onNavigateToTransactionDetail: (String) -> Unit = {},
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToGroupDetail: (String) -> Unit = { _ -> },
    onGroupQuickAction: (String, GroupQuickAction) -> Unit = { groupId, _ -> onNavigateToGroupDetail(groupId) },
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val userName = remember {
        try {
            MoneyCareApp.instance.sessionManager.getUserName() ?: "Người dùng"
        } catch (e: Throwable) {
            "Người dùng"
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    androidx.compose.runtime.LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavigation(
                selectedItem = BottomNavItem.HOME,
                onItemSelected = { item ->
                    when (item) {
                        BottomNavItem.HOME -> Unit
                        BottomNavItem.TRANSACTIONS -> onNavigateToTransactions()
                        BottomNavItem.STATISTICS -> onNavigateToStatistics()
                        BottomNavItem.PROFILE -> onNavigateToProfile()
                    }
                },
                onAddClick = {
                    if (uiState.selectedMode == HomeMode.GROUP) {
                        onNavigateToCreateGroup()
                    } else {
                        onNavigateToAddTransaction()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppHeader(
                userName = userName,
                hasUnreadNotifications = hasUnreadNotifications,
                onNotificationClick = onNavigateToNotifications,
                onSettingsClick = onNavigateToSettings
            )

            PersonalGroupSwitcher(
                selectedMode = uiState.selectedMode,
                onModeSelected = viewModel::selectMode
            )

            if (uiState.isLoading) {
                LoadingAnimation()
            } else {
                AnimatedContent(
                    targetState = uiState.selectedMode,
                    transitionSpec = {
                        (slideInHorizontally { width -> width / 4 } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> -width / 4 } + fadeOut())
                    },
                    label = "homeModeSwitch"
                ) { mode ->
                    when (mode) {
                        HomeMode.PERSONAL -> PersonalHomeContent(
                            month = uiState.month,
                            onPreviousMonth = { viewModel.changeMonth(-1) },
                            onNextMonth = { viewModel.changeMonth(1) },
                            balance = uiState.balance,
                            isBalanceVisible = uiState.isBalanceVisible,
                            onToggleBalanceVisibility = viewModel::toggleBalanceVisibility,
                            income = uiState.income,
                            expense = uiState.expense,
                            savingsGoals = uiState.savingsGoals,
                            recentTransactions = uiState.recentTransactions,
                            onSavingsGoalClick = { _ -> onNavigateToSavingsGoal() },
                            onManageSavingsGoalsClick = onNavigateToSavingsGoal,
                            onSeeAllTransactionsClick = onNavigateToTransactions,
                            onTransactionClick = { onNavigateToTransactionDetail(it.id) }
                        )

                        HomeMode.GROUP -> GroupHomeContent(
                            groups = uiState.groups,
                            groupTasks = uiState.groupTasks,
                            onCreateGroupClick = onNavigateToCreateGroup,
                            onGroupClick = { group -> onNavigateToGroupDetail(group.id) },
                            onGroupQuickAction = { group, action -> onGroupQuickAction(group.id, action) },
                            onTaskClick = { task -> onNavigateToGroupDetail(task.groupId) }
                        )
                    }
                }
            }
        }
    }
}
