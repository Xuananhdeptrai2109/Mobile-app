package com.moneycare.app.ui.personal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.ui.components.GroupDetailTopBar
import com.moneycare.app.ui.components.ErrorRetryContent
import com.moneycare.app.ui.state.LoadState
import com.moneycare.app.ui.theme.ExpenseRed
import com.moneycare.app.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: PersonalTransactionViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(transactionId) { viewModel.loadTransaction(transactionId.toLongOrNull() ?: return@LaunchedEffect) }
    LaunchedEffect(state.errorMessage) {
        if (state.selectedTransaction != null) state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa giao dịch?") },
            text = { Text("Số dư ví sẽ được cập nhật lại. Thao tác này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTransaction(transactionId.toLong(), onDeleted)
                    }
                ) { Text("Xóa", color = ExpenseRed) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { GroupDetailTopBar(title = "Chi tiết giao dịch", onBackClick = onBackClick) }
    ) { padding ->
        when (val content = state.detailState) {
            LoadState.Loading -> androidx.compose.foundation.layout.Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is LoadState.Error -> ErrorRetryContent(
                message = content.message,
                onRetry = { transactionId.toLongOrNull()?.let(viewModel::loadTransaction) },
                modifier = Modifier.padding(padding)
            )

            LoadState.Empty -> ErrorRetryContent(
                message = "Không tìm thấy giao dịch",
                onRetry = { transactionId.toLongOrNull()?.let(viewModel::loadTransaction) },
                modifier = Modifier.padding(padding)
            )

            is LoadState.Success -> {
            val transaction = content.data
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(transaction.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = (if (transaction.type == TransactionType.INCOME) "+" else "-") + transaction.amount.toVndText(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(transaction.date, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                DetailRow("Loại", if (transaction.type == TransactionType.INCOME) "Thu nhập" else "Chi tiêu")
                HorizontalDivider()
                DetailRow("Danh mục", transaction.category)
                HorizontalDivider()
                DetailRow("Ví", transaction.walletName)
                if (!transaction.note.isNullOrBlank()) {
                    HorizontalDivider()
                    DetailRow("Ghi chú", transaction.note)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isSaving
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = ExpenseRed)
                        Text("Xóa", color = ExpenseRed, modifier = Modifier.padding(start = 6.dp))
                    }
                    Button(
                        onClick = { onEditClick(transaction.id) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isSaving
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Text("Chỉnh sửa", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp))
    }
}

private fun Long.toVndText(): String = "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(this)} đ"
