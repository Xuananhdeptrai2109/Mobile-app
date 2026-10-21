package com.moneycare.app.ui.personal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneycare.app.data.model.personal.WalletResponse
import com.moneycare.app.ui.components.GroupDetailTopBar
import com.moneycare.app.util.formatVnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletManagementScreen(
    onBackClick: () -> Unit,
    viewModel: WalletManagementViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var editingWallet by remember { mutableStateOf<WalletResponse?>(null) }
    var showWalletForm by remember { mutableStateOf(false) }
    var deletingWallet by remember { mutableStateOf<WalletResponse?>(null) }

    LaunchedEffect(Unit) { viewModel.loadWallets() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showWalletForm) {
        WalletFormDialog(
            wallet = editingWallet,
            isSaving = state.isSaving,
            onDismiss = { showWalletForm = false },
            onSave = { name, makeDefault ->
                viewModel.saveWallet(editingWallet, name, makeDefault) {
                    showWalletForm = false
                }
            }
        )
    }

    deletingWallet?.let { wallet ->
        AlertDialog(
            onDismissRequest = { deletingWallet = null },
            title = { Text("Xóa ví?") },
            text = { Text("Ví “${wallet.name}” chỉ có thể xóa khi chưa có giao dịch.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteWallet(wallet) { deletingWallet = null } },
                    enabled = !state.isSaving
                ) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingWallet = null }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { GroupDetailTopBar("Quản lý ví", onBackClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingWallet = null
                    showWalletForm = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm ví")
            }
        }
    ) { padding ->
        when {
            state.isLoading && state.wallets.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.wallets.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) { Text("Chưa có ví. Nhấn + để tạo ví đầu tiên.") }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.wallets, key = { it.walletId }) { wallet ->
                    WalletCard(
                        wallet = wallet,
                        actionsEnabled = !state.isSaving,
                        onEdit = {
                            editingWallet = wallet
                            showWalletForm = true
                        },
                        onMakeDefault = { viewModel.makeDefault(wallet) },
                        onDelete = { deletingWallet = wallet }
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletCard(
    wallet: WalletResponse,
    actionsEnabled: Boolean,
    onEdit: () -> Unit,
    onMakeDefault: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(wallet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (wallet.isDefault) {
                        AssistChip(onClick = {}, enabled = false, label = { Text("Mặc định") })
                    }
                }
                Text(formatVnd(wallet.balance), style = MaterialTheme.typography.titleLarge)
                Text(wallet.currency, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, enabled = actionsEnabled) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Tùy chọn ví")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Đổi tên") },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    if (!wallet.isDefault) {
                        DropdownMenuItem(
                            text = { Text("Đặt làm ví mặc định") },
                            onClick = { menuExpanded = false; onMakeDefault() }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa ví", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletFormDialog(
    wallet: WalletResponse?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var name by remember(wallet?.walletId) { mutableStateOf(wallet?.name.orEmpty()) }
    var makeDefault by remember(wallet?.walletId) { mutableStateOf(wallet?.isDefault ?: false) }
    val validName = name.trim().isNotEmpty() && name.trim().length <= 100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (wallet == null) "Thêm ví" else "Chỉnh sửa ví") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tên ví") },
                    singleLine = true,
                    isError = name.isNotEmpty() && !validName
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Đặt làm ví mặc định")
                    Switch(
                        checked = makeDefault,
                        onCheckedChange = { makeDefault = it },
                        enabled = wallet?.isDefault != true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, makeDefault) },
                enabled = validName && !isSaving
            ) { Text(if (isSaving) "Đang lưu…" else "Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
