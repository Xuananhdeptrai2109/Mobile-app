package com.moneycare.app.ui.personal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.personal.SavingsOperation
import com.moneycare.app.ui.components.*
import com.moneycare.app.ui.theme.ExpenseRed
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Locale

private enum class GoalDialog { FORM, ACTIONS, CONTRIBUTION, DELETE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    onBackClick: () -> Unit,
    viewModel: PersonalInsightsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var selectedGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var activeDialog by remember { mutableStateOf<GoalDialog?>(null) }
    var contributionOperation by remember { mutableStateOf(SavingsOperation.DEPOSIT) }

    LaunchedEffect(Unit) { viewModel.loadGoals() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    when (activeDialog) {
        GoalDialog.FORM -> GoalFormDialog(
            goal = selectedGoal,
            isSaving = state.isSaving,
            onDismiss = { activeDialog = null },
            onSave = { title, emoji, target, targetDate ->
                viewModel.saveGoal(
                    selectedGoal?.id?.toLongOrNull(), title, emoji, target, targetDate
                ) { activeDialog = null }
            }
        )
        GoalDialog.ACTIONS -> selectedGoal?.let { goal ->
            GoalActionsDialog(
                goal = goal,
                onDismiss = { activeDialog = null },
                onDeposit = {
                    contributionOperation = SavingsOperation.DEPOSIT
                    activeDialog = GoalDialog.CONTRIBUTION
                },
                onWithdraw = {
                    contributionOperation = SavingsOperation.WITHDRAW
                    activeDialog = GoalDialog.CONTRIBUTION
                },
                onEdit = { activeDialog = GoalDialog.FORM },
                onDelete = { activeDialog = GoalDialog.DELETE }
            )
        }
        GoalDialog.CONTRIBUTION -> selectedGoal?.let { goal ->
            ContributionDialog(
                operation = contributionOperation,
                isSaving = state.isSaving,
                onDismiss = { activeDialog = GoalDialog.ACTIONS },
                onConfirm = { amount ->
                    viewModel.contribute(goal.id.toLong(), amount, contributionOperation) {
                        activeDialog = null
                    }
                }
            )
        }
        GoalDialog.DELETE -> selectedGoal?.let { goal ->
            AlertDialog(
                onDismissRequest = { activeDialog = GoalDialog.ACTIONS },
                title = { Text("Xóa mục tiêu?") },
                text = { Text("Toàn bộ tiến độ của “${goal.title}” sẽ bị xóa.") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteGoal(goal.id.toLong()) { activeDialog = null } },
                        enabled = !state.isSaving
                    ) { Text("Xóa", color = ExpenseRed) }
                },
                dismissButton = { TextButton(onClick = { activeDialog = GoalDialog.ACTIONS }) { Text("Hủy") } }
            )
        }
        null -> Unit
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { GroupDetailTopBar("Mục tiêu tiết kiệm", onBackClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedGoal = null
                activeDialog = GoalDialog.FORM
            }) { Icon(Icons.Filled.Add, contentDescription = "Thêm mục tiêu") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            FilterChip(
                selected = state.includeCompletedGoals,
                onClick = { viewModel.setIncludeCompleted(!state.includeCompletedGoals) },
                label = { Text("Hiện mục tiêu đã hoàn thành") },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            when {
                state.isLoading && state.goals.isEmpty() -> Box(
                    Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center
                ) { CircularProgressIndicator() }
                state.goals.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("🎯", style = MaterialTheme.typography.displayMedium)
                        Text("Chưa có mục tiêu tiết kiệm", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                        Text("Nhấn + để tạo mục tiêu đầu tiên", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    items(state.goals, key = { it.id }) { goal ->
                        SavingsGoalCard(
                            goal = goal,
                            onClick = {
                                selectedGoal = goal
                                activeDialog = GoalDialog.ACTIONS
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalFormDialog(
    goal: SavingsGoal?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, String?) -> Unit
) {
    var title by remember(goal?.id) { mutableStateOf(goal?.title.orEmpty()) }
    var emoji by remember(goal?.id) { mutableStateOf(goal?.emoji ?: "🎯") }
    var target by remember(goal?.id) { mutableLongStateOf(goal?.targetAmount ?: 0) }
    var targetDate by remember(goal?.id) { mutableStateOf(goal?.targetDate.orEmpty()) }
    val validDate = targetDate.isBlank() || try {
        LocalDate.parse(targetDate)
        true
    } catch (_: DateTimeParseException) {
        false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Tạo mục tiêu" else "Sửa mục tiêu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it.take(8) },
                        label = { Text("Biểu tượng") },
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(160) },
                        label = { Text("Tên mục tiêu") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                CurrencyTextField(target, { target = it }, "Số tiền mục tiêu")
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it.take(10) },
                    label = { Text("Hạn hoàn thành (không bắt buộc)") },
                    placeholder = { Text("yyyy-MM-dd") },
                    isError = !validDate,
                    supportingText = if (!validDate) ({ Text("Nhập theo định dạng yyyy-MM-dd") }) else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, emoji, target, targetDate.takeIf(String::isNotBlank)) },
                enabled = title.isNotBlank() && target > 0 && validDate && !isSaving
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun GoalActionsDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${goal.emoji} ${goal.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${goal.currentAmount.money()} / ${goal.targetAmount.money()}", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { goal.progress }, modifier = Modifier.fillMaxWidth())
                goal.targetDate?.let { Text("Hạn: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                if (goal.isCompleted) Text("Đã hoàn thành", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDeposit, modifier = Modifier.weight(1f)) { Text("Nạp thêm") }
                    OutlinedButton(onClick = onWithdraw, modifier = Modifier.weight(1f), enabled = goal.currentAmount > 0) { Text("Rút bớt") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text("Chỉnh sửa") } },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Xóa", color = ExpenseRed) }
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
        }
    )
}

@Composable
private fun ContributionDialog(
    operation: SavingsOperation,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amount by remember(operation) { mutableLongStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (operation == SavingsOperation.DEPOSIT) "Nạp vào mục tiêu" else "Rút khỏi mục tiêu") },
        text = { CurrencyTextField(amount, { amount = it }, "Số tiền") },
        confirmButton = {
            TextButton(onClick = { onConfirm(amount) }, enabled = amount > 0 && !isSaving) { Text("Xác nhận") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

private fun Long.money(): String = "${NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(this)} đ"
