package com.moneycare.app.ui.personal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.personal.SavingsOperation
import com.moneycare.app.ui.components.*
import com.moneycare.app.ui.state.LoadState
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
    var selectedGoalId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeDialogName by rememberSaveable { mutableStateOf<String?>(null) }
    var contributionOperationName by rememberSaveable {
        mutableStateOf(SavingsOperation.DEPOSIT.name)
    }
    val selectedGoal = state.goals.firstOrNull { it.id == selectedGoalId }
    val activeDialog = activeDialogName?.let(GoalDialog::valueOf)
    val contributionOperation = SavingsOperation.valueOf(contributionOperationName)

    LaunchedEffect(Unit) { viewModel.loadGoals() }
    LaunchedEffect(state.errorMessage) {
        if (state.goals.isNotEmpty()) state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    when (activeDialog) {
        GoalDialog.FORM -> GoalFormDialog(
            goal = selectedGoal,
            isSaving = state.isSaving,
            onDismiss = { activeDialogName = null },
            onSave = { title, emoji, target, targetDate ->
                viewModel.saveGoal(
                    selectedGoal?.id?.toLongOrNull(), title, emoji, target, targetDate
                ) { activeDialogName = null }
            }
        )
        GoalDialog.ACTIONS -> selectedGoal?.let { goal ->
            GoalActionsDialog(
                goal = goal,
                onDismiss = { activeDialogName = null },
                onDeposit = {
                    contributionOperationName = SavingsOperation.DEPOSIT.name
                    activeDialogName = GoalDialog.CONTRIBUTION.name
                },
                onWithdraw = {
                    contributionOperationName = SavingsOperation.WITHDRAW.name
                    activeDialogName = GoalDialog.CONTRIBUTION.name
                },
                onEdit = { activeDialogName = GoalDialog.FORM.name },
                onDelete = { activeDialogName = GoalDialog.DELETE.name }
            )
        }
        GoalDialog.CONTRIBUTION -> selectedGoal?.let { goal ->
            ContributionDialog(
                operation = contributionOperation,
                isSaving = state.isSaving,
                onDismiss = { activeDialogName = GoalDialog.ACTIONS.name },
                onConfirm = { amount ->
                    viewModel.contribute(goal.id.toLong(), amount, contributionOperation) {
                        activeDialogName = null
                    }
                }
            )
        }
        GoalDialog.DELETE -> selectedGoal?.let { goal ->
            AlertDialog(
                onDismissRequest = { activeDialogName = GoalDialog.ACTIONS.name },
                title = { Text("Xóa mục tiêu?") },
                text = { Text("Toàn bộ tiến độ của “${goal.title}” sẽ bị xóa.") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteGoal(goal.id.toLong()) { activeDialogName = null } },
                        enabled = !state.isSaving
                    ) { Text("Xóa", color = ExpenseRed) }
                },
                dismissButton = { TextButton(onClick = { activeDialogName = GoalDialog.ACTIONS.name }) { Text("Hủy") } }
            )
        }
        null -> Unit
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { GroupDetailTopBar("Mục tiêu tiết kiệm", onBackClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedGoalId = null
                activeDialogName = GoalDialog.FORM.name
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
            when (val content = state.goalsContentState) {
                LoadState.Loading -> Box(
                    Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center
                ) { CircularProgressIndicator() }
                is LoadState.Error -> ErrorRetryContent(
                    message = content.message,
                    onRetry = viewModel::loadGoals
                )
                LoadState.Empty -> Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("🎯", style = MaterialTheme.typography.displayMedium)
                        Text("Chưa có mục tiêu tiết kiệm", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                        Text("Nhấn + để tạo mục tiêu đầu tiên", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is LoadState.Success -> LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    items(state.goals, key = { it.id }) { goal ->
                        SavingsGoalCard(
                            goal = goal,
                            onClick = {
                                selectedGoalId = goal.id
                                activeDialogName = GoalDialog.ACTIONS.name
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
    var title by rememberSaveable(goal?.id) { mutableStateOf(goal?.title.orEmpty()) }
    var emoji by rememberSaveable(goal?.id) { mutableStateOf(goal?.emoji ?: "🎯") }
    var target by rememberSaveable(goal?.id) { mutableLongStateOf(goal?.targetAmount ?: 0) }
    var targetDate by rememberSaveable(goal?.id) { mutableStateOf(goal?.targetDate.orEmpty()) }
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
    var amount by rememberSaveable(operation) { mutableLongStateOf(0) }
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
