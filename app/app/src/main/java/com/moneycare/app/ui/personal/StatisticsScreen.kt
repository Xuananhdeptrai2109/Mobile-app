package com.moneycare.app.ui.personal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.personal.CategoryStatisticResponse
import com.moneycare.app.ui.components.*
import com.moneycare.app.ui.state.LoadState
import com.moneycare.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateHome: () -> Unit,
    onNavigateTransactions: () -> Unit,
    onAddTransaction: () -> Unit,
    onNavigateProfile: () -> Unit,
    viewModel: PersonalInsightsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadStatistics() }
    LaunchedEffect(state.errorMessage) {
        if (state.statistics != null) state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showBudgetDialog) {
        BudgetDialog(
            initialAmount = state.statistics?.budget?.amount ?: 0,
            isSaving = state.isSaving,
            onDismiss = { showBudgetDialog = false },
            onSave = { viewModel.saveBudget(it) { showBudgetDialog = false } },
            onDelete = if ((state.statistics?.budget?.amount ?: 0) > 0) {
                { viewModel.deleteBudget { showBudgetDialog = false } }
            } else null
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Thống kê", fontWeight = FontWeight.Bold) }) },
        bottomBar = {
            AppBottomNavigation(
                selectedItem = BottomNavItem.STATISTICS,
                onItemSelected = {
                    when (it) {
                        BottomNavItem.HOME -> onNavigateHome()
                        BottomNavItem.TRANSACTIONS -> onNavigateTransactions()
                        BottomNavItem.STATISTICS -> Unit
                        BottomNavItem.PROFILE -> onNavigateProfile()
                    }
                },
                onAddClick = onAddTransaction
            )
        }
    ) { padding ->
        when (val content = state.statisticsContentState) {
            LoadState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is LoadState.Error -> ErrorRetryContent(
                message = content.message,
                onRetry = viewModel::loadStatistics,
                modifier = Modifier.padding(padding)
            )

            LoadState.Empty -> ErrorRetryContent(
                message = "Chưa có dữ liệu thống kê",
                onRetry = viewModel::loadStatistics,
                modifier = Modifier.padding(padding)
            )

            is LoadState.Success -> {
                val statistics = content.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.changeMonth(-1) }) {
                                Icon(Icons.Filled.ChevronLeft, "Tháng trước")
                            }
                            Text(state.month.toMonthTitle(), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.changeMonth(1) }) {
                                Icon(Icons.Filled.ChevronRight, "Tháng sau")
                            }
                        }
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatisticCard("Thu nhập", statistics.income, IncomeGreen, Modifier.weight(1f))
                            StatisticCard("Chi tiêu", statistics.expense, ExpenseRed, Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextMetricCard(
                                "Chênh lệch",
                                statistics.net.toMoney(),
                                if (statistics.net >= 0) IncomeGreen else ExpenseRed,
                                Modifier.weight(1f)
                            )
                            TextMetricCard(
                                "Tỷ lệ tiết kiệm",
                                "${statistics.savingsRate.formatPercent()}%",
                                PrimaryGreen,
                                Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        BudgetCard(
                            amount = statistics.budget.amount,
                            spent = statistics.budget.spent,
                            usage = statistics.budget.usagePercentage,
                            exceeded = statistics.budget.exceeded,
                            onEdit = { showBudgetDialog = true }
                        )
                    }
                    item { SectionHeader(title = "Chi tiêu theo danh mục") }
                    if (statistics.expenseByCategory.isEmpty()) {
                        item { EmptyStatisticsText("Chưa có chi tiêu trong tháng này") }
                    } else {
                        items(statistics.expenseByCategory.size) { index ->
                            CategoryStatisticRow(statistics.expenseByCategory[index])
                        }
                    }
                    item { SectionHeader(title = "Xu hướng theo ngày") }
                    if (statistics.dailyExpenses.isEmpty()) {
                        item { EmptyStatisticsText("Chưa có dữ liệu theo ngày") }
                    } else {
                        val max = statistics.dailyExpenses.maxOf { it.amount }.coerceAtLeast(1)
                        items(statistics.dailyExpenses.size) { index ->
                            val day = statistics.dailyExpenses[index]
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(day.date.takeLast(2), modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelLarge)
                                LinearProgressIndicator(
                                    progress = { day.amount.toFloat() / max.toFloat() },
                                    modifier = Modifier.weight(1f).height(9.dp),
                                    color = PrimaryGreen,
                                    trackColor = MintContainer
                                )
                                Text(day.amount.toMoney(), modifier = Modifier.padding(start = 10.dp), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticCard(label: String, amount: Long, color: Color, modifier: Modifier = Modifier) {
    TextMetricCard(label, amount.toMoney(), color, modifier)
}

@Composable
private fun TextMetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun BudgetCard(amount: Long, spent: Long, usage: Double, exceeded: Boolean, onEdit: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Ngân sách tháng", fontWeight = FontWeight.Bold)
                TextButton(onClick = onEdit) { Text(if (amount > 0) "Điều chỉnh" else "Thiết lập") }
            }
            if (amount == 0L) {
                Text("Đặt hạn mức để theo dõi chi tiêu trong tháng.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LinearProgressIndicator(
                    progress = { (usage / 100).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = if (exceeded) ExpenseRed else PrimaryGreen,
                    trackColor = if (exceeded) ExpenseRedBg else MintContainer
                )
                Text(
                    "Đã dùng ${spent.toMoney()} / ${amount.toMoney()} (${usage.formatPercent()}%)",
                    color = if (exceeded) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryStatisticRow(item: CategoryStatisticResponse) {
    val color = remember(item.colorCode) {
        runCatching { Color(android.graphics.Color.parseColor(item.colorCode)) }.getOrDefault(PrimaryGreen)
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${item.categoryName} · ${item.transactionCount} giao dịch", fontWeight = FontWeight.Medium)
            Text(item.amount.toMoney(), fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (item.percentage / 100).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp),
            color = color,
            trackColor = color.copy(alpha = .15f)
        )
    }
}

@Composable
private fun EmptyStatisticsText(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun BudgetDialog(
    initialAmount: Long,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
    onDelete: (() -> Unit)?
) {
    var amount by rememberSaveable(initialAmount) { mutableLongStateOf(initialAmount) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ngân sách tháng") },
        text = { CurrencyTextField(amount, { amount = it }, "Hạn mức") },
        confirmButton = {
            TextButton(onClick = { onSave(amount) }, enabled = amount > 0 && !isSaving) { Text("Lưu") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete, enabled = !isSaving) { Text("Xóa", color = ExpenseRed) }
                TextButton(onClick = onDismiss) { Text("Hủy") }
            }
        }
    )
}

private fun Long.toMoney(): String = "${NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(this)} đ"
private fun Double.formatPercent(): String = if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.US, "%.1f", this)
private fun String.toMonthTitle(): String {
    val values = split("-")
    return if (values.size == 2) "Tháng ${values[1].toIntOrNull() ?: values[1]}/${values[0]}" else this
}
