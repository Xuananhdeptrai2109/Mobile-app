package com.moneycare.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneycare.app.MoneyCareApp
import com.moneycare.app.ui.auth.forgot.ForgotPasswordScreen
import com.moneycare.app.ui.auth.login.LoginScreen
import com.moneycare.app.ui.auth.register.RegisterScreen
import com.moneycare.app.ui.group.GroupDetailScreen
import com.moneycare.app.ui.group.GroupReportScreen
import com.moneycare.app.ui.group.create.CreateGroupScreen
import com.moneycare.app.ui.group.expense.AddExpenseScreen
import com.moneycare.app.ui.group.expense.ExpenseDetailScreen
import com.moneycare.app.ui.group.member.MemberDetailScreen
import com.moneycare.app.ui.group.task.AddTaskScreen
import com.moneycare.app.ui.home.HomeScreen
import com.moneycare.app.ui.personal.PersonalTransactionViewModel
import com.moneycare.app.ui.personal.PersonalInsightsViewModel
import com.moneycare.app.ui.personal.NotificationScreen
import com.moneycare.app.ui.personal.NotificationViewModel
import com.moneycare.app.ui.personal.SavingsGoalsScreen
import com.moneycare.app.ui.personal.StatisticsScreen
import com.moneycare.app.ui.personal.TransactionDetailScreen
import com.moneycare.app.ui.personal.TransactionFormScreen
import com.moneycare.app.ui.personal.TransactionListScreen
import com.moneycare.app.ui.personal.WalletManagementScreen
import com.moneycare.app.ui.settings.SettingsScreen
import com.moneycare.app.ui.welcome.WelcomeScreen
import com.moneycare.app.viewmodel.GroupDetailViewModel
import com.moneycare.app.viewmodel.GroupDetailTab
import com.moneycare.app.ui.components.GroupQuickAction

/**
 * Route names for the app's entry flow:
 * Welcome -> Login <-> Register -> Home.
 */
object MoneyCareRoutes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val GROUP_DETAIL = "group_detail"
    const val GROUP_REPORT = "group_report"
    const val ADD_EXPENSE = "add_expense"
    const val EXPENSE_DETAIL = "expense_detail"
    const val MEMBER_DETAIL = "member_detail"
    const val ADD_TASK = "add_task"
    const val CREATE_GROUP = "create_group"
    const val SETTINGS = "settings"
    const val PERSONAL_TRANSACTIONS = "personal_transactions"
    const val PERSONAL_TRANSACTION_FORM = "personal_transaction_form"
    const val PERSONAL_TRANSACTION_DETAIL = "personal_transaction_detail"
    const val PERSONAL_STATISTICS = "personal_statistics"
    const val SAVINGS_GOALS = "savings_goals"
    const val WALLETS = "wallets"
    const val NOTIFICATIONS = "notifications"
}

private const val TRANSITION_DURATION_MS = 320

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String? = null
) {
    // Shared ViewModel for Group features within the same navigation session if needed,
    // but here we can also use scoped viewmodels.
    val groupViewModel: GroupDetailViewModel = viewModel()
    val personalTransactionViewModel: PersonalTransactionViewModel = viewModel()
    val personalInsightsViewModel: PersonalInsightsViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()
    val notificationState by notificationViewModel.uiState.collectAsState()
    val sessionManager = MoneyCareApp.instance.sessionManager
    val isLoggedIn by sessionManager.isLoggedInFlow.collectAsState()
    var hasAuthenticatedInThisProcess by remember { mutableStateOf(isLoggedIn) }
    val resolvedStartDestination = remember(startDestination) {
        startDestination ?: if (sessionManager.isLoggedIn()) {
            MoneyCareRoutes.HOME
        } else {
            MoneyCareRoutes.WELCOME
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            hasAuthenticatedInThisProcess = true
            notificationViewModel.loadUnreadCount()
        } else if (hasAuthenticatedInThisProcess) {
            hasAuthenticatedInThisProcess = false
            navController.navigate(MoneyCareRoutes.WELCOME) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(navController, isLoggedIn) {
        if (isLoggedIn) {
            navController.currentBackStackEntryFlow.collect { entry ->
                if (entry.destination.route == MoneyCareRoutes.HOME) {
                    notificationViewModel.loadUnreadCount()
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = resolvedStartDestination
    ) {
        composable(
            route = MoneyCareRoutes.WELCOME,
            enterTransition = { fadeIn(tween(TRANSITION_DURATION_MS)) },
            exitTransition = { fadeOut(tween(TRANSITION_DURATION_MS)) }
        ) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(MoneyCareRoutes.LOGIN) },
                onRegisterClick = { navController.navigate(MoneyCareRoutes.REGISTER) }
            )
        }

        composable(
            route = MoneyCareRoutes.LOGIN,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION_MS),
                    initialOffsetX = { it / 3 }
                ) + fadeIn(tween(TRANSITION_DURATION_MS))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION_MS),
                    targetOffsetX = { -it / 3 }
                ) + fadeOut(tween(TRANSITION_DURATION_MS))
            }
        ) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(MoneyCareRoutes.HOME) {
                        popUpTo(MoneyCareRoutes.WELCOME) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(MoneyCareRoutes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(MoneyCareRoutes.FORGOT_PASSWORD) }
            )
        }

        composable(
            route = MoneyCareRoutes.REGISTER,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION_MS),
                    initialOffsetX = { it / 3 }
                ) + fadeIn(tween(TRANSITION_DURATION_MS))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION_MS),
                    targetOffsetX = { it / 3 }
                ) + fadeOut(tween(TRANSITION_DURATION_MS))
            }
        ) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(MoneyCareRoutes.HOME) {
                        popUpTo(MoneyCareRoutes.WELCOME) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(MoneyCareRoutes.HOME) {
            HomeScreen(
                onNavigateToTransactions = {
                    navController.navigate(MoneyCareRoutes.PERSONAL_TRANSACTIONS)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(MoneyCareRoutes.PERSONAL_TRANSACTION_FORM)
                },
                onNavigateToStatistics = {
                    navController.navigate(MoneyCareRoutes.PERSONAL_STATISTICS)
                },
                onNavigateToProfile = {
                    navController.navigate(MoneyCareRoutes.SETTINGS)
                },
                onNavigateToSavingsGoal = {
                    navController.navigate(MoneyCareRoutes.SAVINGS_GOALS)
                },
                onNavigateToTransactionDetail = { transactionId ->
                    navController.navigate("${MoneyCareRoutes.PERSONAL_TRANSACTION_DETAIL}/$transactionId")
                },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate("${MoneyCareRoutes.GROUP_DETAIL}/$groupId")
                },
                onGroupQuickAction = { groupId, action ->
                    if (action == GroupQuickAction.REPORT) {
                        navController.navigate("${MoneyCareRoutes.GROUP_REPORT}/$groupId")
                    } else {
                        navController.navigate("${MoneyCareRoutes.GROUP_DETAIL}/$groupId?tab=${action.name}")
                    }
                },
                onNavigateToCreateGroup = {
                    navController.navigate(MoneyCareRoutes.CREATE_GROUP)
                },
                onNavigateToNotifications = {
                    navController.navigate(MoneyCareRoutes.NOTIFICATIONS)
                },
                hasUnreadNotifications = notificationState.unreadCount > 0,
                onNavigateToSettings = { navController.navigate(MoneyCareRoutes.SETTINGS) }
            )
        }

        composable(MoneyCareRoutes.PERSONAL_TRANSACTIONS) {
            TransactionListScreen(
                onBackClick = { navController.popBackStack() },
                onAddClick = { navController.navigate(MoneyCareRoutes.PERSONAL_TRANSACTION_FORM) },
                onTransactionClick = { transactionId ->
                    navController.navigate("${MoneyCareRoutes.PERSONAL_TRANSACTION_DETAIL}/$transactionId")
                },
                onNavigateHome = {
                    navController.popBackStack(MoneyCareRoutes.HOME, inclusive = false)
                },
                onNavigateStatistics = {
                    navController.navigate(MoneyCareRoutes.PERSONAL_STATISTICS)
                },
                onNavigateProfile = { navController.navigate(MoneyCareRoutes.SETTINGS) },
                viewModel = personalTransactionViewModel
            )
        }

        composable(MoneyCareRoutes.PERSONAL_STATISTICS) {
            StatisticsScreen(
                onNavigateHome = {
                    navController.popBackStack(MoneyCareRoutes.HOME, inclusive = false)
                },
                onNavigateTransactions = {
                    navController.navigate(MoneyCareRoutes.PERSONAL_TRANSACTIONS) {
                        launchSingleTop = true
                    }
                },
                onAddTransaction = {
                    navController.navigate(MoneyCareRoutes.PERSONAL_TRANSACTION_FORM)
                },
                onNavigateProfile = { navController.navigate(MoneyCareRoutes.SETTINGS) },
                viewModel = personalInsightsViewModel
            )
        }

        composable(MoneyCareRoutes.SAVINGS_GOALS) {
            SavingsGoalsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = personalInsightsViewModel
            )
        }

        composable(
            route = "${MoneyCareRoutes.PERSONAL_TRANSACTION_FORM}?transactionId={transactionId}",
            arguments = listOf(navArgument("transactionId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            TransactionFormScreen(
                transactionId = backStackEntry.arguments?.getString("transactionId"),
                onBackClick = { navController.popBackStack() },
                onSaved = { transactionId ->
                    navController.popBackStack()
                    navController.navigate("${MoneyCareRoutes.PERSONAL_TRANSACTION_DETAIL}/$transactionId") {
                        launchSingleTop = true
                    }
                },
                viewModel = personalTransactionViewModel
            )
        }

        composable(
            route = "${MoneyCareRoutes.PERSONAL_TRANSACTION_DETAIL}/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: return@composable
            TransactionDetailScreen(
                transactionId = transactionId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate("${MoneyCareRoutes.PERSONAL_TRANSACTION_FORM}?transactionId=$id")
                },
                onDeleted = { navController.popBackStack() },
                viewModel = personalTransactionViewModel
            )
        }

        composable(
            route = "${MoneyCareRoutes.GROUP_DETAIL}/{groupId}?tab={tab}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("tab") {
                    type = NavType.StringType
                    defaultValue = GroupDetailTab.OVERVIEW.name
                }
            ),
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(TRANSITION_DURATION_MS),
                    initialOffsetX = { it }
                ) + fadeIn(tween(TRANSITION_DURATION_MS))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(TRANSITION_DURATION_MS),
                    targetOffsetX = { it }
                ) + fadeOut(tween(TRANSITION_DURATION_MS))
            }
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val initialTab = backStackEntry.arguments?.getString("tab")
                ?.let { value -> GroupDetailTab.entries.firstOrNull { it.name == value } }
                ?: GroupDetailTab.OVERVIEW
            GroupDetailScreen(
                groupId = groupId,
                initialTab = initialTab,
                onBackClick = { navController.popBackStack() },
                onNavigateToAddExpense = { navController.navigate(MoneyCareRoutes.ADD_EXPENSE) },
                onNavigateToAddTask = { id -> navController.navigate(MoneyCareRoutes.ADD_TASK) },
                onNavigateToEditTask = { taskId -> navController.navigate("${MoneyCareRoutes.ADD_TASK}?taskId=$taskId") },
                onNavigateToMemberDetail = { memberId -> 
                    navController.navigate("${MoneyCareRoutes.MEMBER_DETAIL}/$memberId")
                },
                onNavigateToExpenseDetail = { expenseId ->
                    navController.navigate("${MoneyCareRoutes.EXPENSE_DETAIL}/$expenseId")
                },
                onNavigateToReport = { id -> navController.navigate("${MoneyCareRoutes.GROUP_REPORT}/$id") },
                viewModel = groupViewModel
            )
        }

        composable(
            route = "${MoneyCareRoutes.GROUP_REPORT}/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            GroupReportScreen(
                groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "${MoneyCareRoutes.ADD_EXPENSE}?expenseId={expenseId}",
            arguments = listOf(navArgument("expenseId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            AddExpenseScreen(
                expenseId = backStackEntry.arguments?.getString("expenseId"),
                onBackClick = { navController.popBackStack() },
                viewModel = groupViewModel
            )
        }

        composable(
            route = "${MoneyCareRoutes.EXPENSE_DETAIL}/{expenseId}",
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            ExpenseDetailScreen(
                expenseId = expenseId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate("${MoneyCareRoutes.ADD_EXPENSE}?expenseId=$id")
                },
                viewModel = groupViewModel
            )
        }

        composable(
            route = "${MoneyCareRoutes.MEMBER_DETAIL}/{memberId}",
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
            MemberDetailScreen(
                memberId = memberId,
                onBackClick = { navController.popBackStack() },
                viewModel = groupViewModel
            )
        }

        composable(
            route = "${MoneyCareRoutes.ADD_TASK}?taskId={taskId}",
            arguments = listOf(navArgument("taskId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            AddTaskScreen(
                taskId = taskId,
                onBackClick = { navController.popBackStack() },
                viewModel = groupViewModel
            )
        }

        composable(MoneyCareRoutes.CREATE_GROUP) {
            CreateGroupScreen(
                onBack = { navController.popBackStack() },
                onGroupCreated = { navController.popBackStack() }
            )
        }

        composable(MoneyCareRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onBackClick = { navController.popBackStack() })
        }

        composable(MoneyCareRoutes.SETTINGS) {
            SettingsScreen(
                userName = MoneyCareApp.instance.sessionManager.getUserName(),
                onBackClick = { navController.popBackStack() },
                onManageWallets = { navController.navigate(MoneyCareRoutes.WALLETS) },
                onLogout = {
                    MoneyCareApp.instance.sessionManager.clearSession()
                }
            )
        }

        composable(MoneyCareRoutes.WALLETS) {
            WalletManagementScreen(onBackClick = { navController.popBackStack() })
        }

        composable(MoneyCareRoutes.NOTIFICATIONS) {
            NotificationScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = notificationViewModel
            )
        }
    }
}
