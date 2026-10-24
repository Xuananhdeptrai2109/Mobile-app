package com.moneycare.app.ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneycare.app.data.model.personal.WalletResponse
import com.moneycare.app.ui.components.GroupDetailTopBar
import com.moneycare.app.ui.components.ErrorRetryContent
import com.moneycare.app.ui.state.LoadState
import com.moneycare.app.util.formatVnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletManagementScreen(
    onBackClick: () -> Unit,
    viewModel: WalletManagementViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var editingWalletId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showWalletForm by rememberSaveable { mutableStateOf(false) }
    var deletingWalletId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editingWallet = state.wallets.firstOrNull { it.walletId == editingWalletId }
    val deletingWallet = state.wallets.firstOrNull { it.walletId == deletingWalletId }

    LaunchedEffect(Unit) { viewModel.loadWallets() }
    LaunchedEffect(state.errorMessage) {
        if (state.wallets.isNotEmpty()) state.errorMessage?.let {
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
            onDismissRequest = { deletingWalletId = null },
            title = { Text("Xóa ví?") },
            text = { Text("Ví “${wallet.name}” chỉ có thể xóa khi chưa có giao dịch.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteWallet(wallet) { deletingWalletId = null } },
                    enabled = !state.isSaving
                ) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingWalletId = null }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { GroupDetailTopBar("Quản lý ví", onBackClick) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingWalletId = null
                    showWalletForm = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm ví")
            }
        }
    ) { padding ->
        when (val content = state.contentState) {
            LoadState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is LoadState.Error -> ErrorRetryContent(
                message = content.message,
                onRetry = viewModel::loadWallets,
                modifier = Modifier.padding(padding)
            )

            LoadState.Empty -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) { Text("Chưa có ví. Nhấn + để tạo ví đầu tiên.") }

            is LoadState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.wallets, key = { it.walletId }) { wallet ->
                    WalletCard(
                        wallet = wallet,
                        actionsEnabled = !state.isSaving,
                        onEdit = {
                            editingWalletId = wallet.walletId
                            showWalletForm = true
                        },
                        onMakeDefault = { viewModel.makeDefault(wallet) },
                        onDelete = { deletingWalletId = wallet.walletId }
                    )
                }
            }
        }
    }
}

// Bảng màu pastel nhạt, nhẹ nhàng, trang nhã hài hòa với theme của app
private val WalletPastelColors = listOf(
    Color(0xFFE8F5E9), // Mint Green nhẹ
    Color(0xFFE0F2F1), // Soft Teal nhẹ
    Color(0xFFE3F2FD), // Soft Blue nhẹ
    Color(0xFFEDE7F6), // Lavender nhẹ
    Color(0xFFFFF3E0), // Peach / Cam đào nhạt
    Color(0xFFFCE4EC), // Pastel Rose / Hồng phấn
    Color(0xFFF1F8E9), // Light Lime nhẹ
    Color(0xFFFFF8E1), // Soft Amber / Vàng kem
    Color(0xFFE0F7FA), // Soft Aqua
    Color(0xFFF3E5F5)  // Light Orchid
)

private val WalletAccentColors = listOf(
    Color(0xFF2E7D32),
    Color(0xFF00695C),
    Color(0xFF1565C0),
    Color(0xFF6A1B9A),
    Color(0xFFD84315),
    Color(0xFFAD1457),
    Color(0xFF558B2F),
    Color(0xFFEF6C00),
    Color(0xFF00838F),
    Color(0xFF7B1FA2)
)

private fun getWalletPastelColor(walletId: Long): Color {
    val index = (walletId % WalletPastelColors.size).toInt().let { if (it < 0) -it else it }
    return WalletPastelColors[index]
}

private fun getWalletAccentColor(walletId: Long): Color {
    val index = (walletId % WalletAccentColors.size).toInt().let { if (it < 0) -it else it }
    return WalletAccentColors[index]
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
    val bgColor = getWalletPastelColor(wallet.walletId)
    val accentColor = getWalletAccentColor(wallet.walletId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accentColor.copy(alpha = 0.16f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = wallet.name.trim().firstOrNull()?.uppercase() ?: "V",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        wallet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    if (wallet.isDefault) {
                        Surface(
                            color = accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Mặc định",
                                color = accentColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    formatVnd(wallet.balance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    wallet.currency,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, enabled = actionsEnabled) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Tùy chọn ví",
                        tint = Color(0xFF475569)
                    )
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
    var name by rememberSaveable(wallet?.walletId) { mutableStateOf(wallet?.name.orEmpty()) }
    var makeDefault by rememberSaveable(wallet?.walletId) {
        mutableStateOf(wallet?.isDefault ?: false)
    }
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
