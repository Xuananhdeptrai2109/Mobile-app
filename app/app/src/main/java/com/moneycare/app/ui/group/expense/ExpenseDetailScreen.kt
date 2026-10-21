package com.moneycare.app.ui.group.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.components.ExpenseSplitMemberItem
import com.moneycare.app.ui.components.ExpenseSummaryCard
import com.moneycare.app.ui.components.GroupDetailTopBar
import com.moneycare.app.ui.theme.ScreenBackground
import com.moneycare.app.ui.group.ConfirmGroupActionDialog
import com.moneycare.app.viewmodel.GroupDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: GroupDetailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    LaunchedEffect(expenseId) {
        viewModel.loadExpenseDetail(expenseId)
    }
    
    val expense = uiState.selectedExpense ?: return
    val currentUserIsOwner = uiState.group?.members?.any {
        it.isCurrentUser && it.role.equals("OWNER", ignoreCase = true)
    } == true

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = ScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GroupDetailTopBar(
                title = "Chi tiết chi tiêu",
                onBackClick = onBackClick,
                menuItems = listOf(
                    "Sửa chi tiêu" to { onEditClick(expense.id) },
                    "Xóa chi tiêu" to { showDeleteConfirmation = true }
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ExpenseSummaryCard(expense = expense)
            }
            
            if (!expense.note.isNullOrBlank()) {
                item {
                    Column {
                        Text(
                            text = "Ghi chú",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = expense.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Chia tiền (${expense.splitMemberCount} người)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(expense.splitMembers) { split ->
                ExpenseSplitMemberItem(
                    split = split,
                    onTogglePayment = if (currentUserIsOwner && !uiState.isSaving) {
                        { viewModel.toggleSplitPayment(expense.id, split.id) }
                    } else null
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        ConfirmGroupActionDialog(
            title = "Xóa chi tiêu?",
            message = "Chi tiêu ${expense.title} và thông tin chia tiền sẽ bị xóa vĩnh viễn.",
            confirmLabel = "Xóa chi tiêu",
            destructive = true,
            isSaving = uiState.isSaving,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = { viewModel.deleteExpense(expense.id, onBackClick) }
        )
    }
}
