package com.moneycare.app.ui.personal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.data.model.personal.PersonalCategoryResponse
import com.moneycare.app.data.model.personal.PersonalTransactionRequest
import com.moneycare.app.data.model.personal.WalletResponse
import com.moneycare.app.ui.components.AppTextField
import com.moneycare.app.ui.components.CurrencyTextField
import com.moneycare.app.ui.components.GroupDetailTopBar
import com.moneycare.app.ui.components.PrimaryButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    transactionId: String?,
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: PersonalTransactionViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val editingId = transactionId?.toLongOrNull()

    var title by remember(transactionId) { mutableStateOf("") }
    var amount by remember(transactionId) { mutableLongStateOf(0L) }
    var type by remember(transactionId) { mutableStateOf(TransactionType.EXPENSE) }
    var categoryId by remember(transactionId) { mutableStateOf<Long?>(null) }
    var walletId by remember(transactionId) { mutableStateOf<Long?>(null) }
    var note by remember(transactionId) { mutableStateOf("") }
    var transactionTime by remember(transactionId) { mutableStateOf(Calendar.getInstance()) }
    var initialized by remember(transactionId) { mutableStateOf(false) }
    var showCreateWalletDialog by remember { mutableStateOf(false) }
    var newWalletName by remember { mutableStateOf("") }

    LaunchedEffect(transactionId) {
        viewModel.clearSelectedTransaction()
        viewModel.loadReferenceData()
        if (editingId != null) viewModel.loadTransaction(editingId)
    }
    LaunchedEffect(state.selectedTransaction?.id, state.wallets, state.categories) {
        if (!initialized && editingId == null && state.wallets.isNotEmpty() && state.categories.isNotEmpty()) {
            walletId = state.wallets.firstOrNull { it.isDefault }?.walletId ?: state.wallets.first().walletId
            categoryId = state.categories.firstOrNull { it.type == type }?.categoryId
            initialized = true
        }
        val transaction = state.selectedTransaction
        if (!initialized && transaction != null && transaction.id == transactionId) {
            title = transaction.title
            amount = transaction.amount
            type = transaction.type
            categoryId = transaction.categoryId.toLongOrNull()
            walletId = transaction.walletId.toLongOrNull()
            note = transaction.note.orEmpty()
            transactionTime = transaction.transactionTime.toCalendar()
            initialized = true
        }
    }
    LaunchedEffect(type) {
        if (initialized && state.categories.none { it.categoryId == categoryId && it.type == type }) {
            categoryId = state.categories.firstOrNull { it.type == type }?.categoryId
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showCreateWalletDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isSaving) showCreateWalletDialog = false
            },
            title = { Text("Thêm ví mới") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Nhập tên ví (ví dụ: Ví MoMo, ZaloPay, Vietcombank...). Ví sẽ tự động xuất hiện trong Quản lý ví và được lưu để chọn cho các lần sau.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newWalletName,
                        onValueChange = { newWalletName = it.take(100) },
                        label = { Text("Tên ví") },
                        placeholder = { Text("Ví dụ: Ví MoMo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newWalletName.trim()
                        if (trimmed.isNotEmpty()) {
                            viewModel.createWalletAndSelect(
                                name = trimmed,
                                onSuccess = { created ->
                                    walletId = created.walletId
                                    showCreateWalletDialog = false
                                }
                            )
                        }
                    },
                    enabled = newWalletName.trim().isNotEmpty() && !state.isSaving
                ) {
                    Text(if (state.isSaving) "Đang lưu..." else "Tạo và chọn ví")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateWalletDialog = false },
                    enabled = !state.isSaving
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            GroupDetailTopBar(
                title = if (editingId == null) "Thêm giao dịch" else "Sửa giao dịch",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = type == TransactionType.EXPENSE,
                            onClick = { type = TransactionType.EXPENSE },
                            label = { Text("Chi tiêu") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == TransactionType.INCOME,
                            onClick = { type = TransactionType.INCOME },
                            label = { Text("Thu nhập") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item { CurrencyTextField(value = amount, onValueChange = { amount = it }, label = "Số tiền") }
                item {
                    FieldLabel("Tên giao dịch")
                    AppTextField(
                        value = title,
                        onValueChange = { title = it.take(160) },
                        placeholder = "Ví dụ: Ăn trưa"
                    )
                }
                item {
                    SelectionField(
                        label = "Danh mục",
                        selectedText = state.categories.firstOrNull { it.categoryId == categoryId }?.name.orEmpty(),
                        items = state.categories.filter { it.type == type },
                        itemText = { it.name },
                        onSelected = { categoryId = it.categoryId }
                    )
                }
                item {
                    WalletSelectionField(
                        label = "Ví",
                        selectedText = state.wallets.firstOrNull { it.walletId == walletId }?.name.orEmpty(),
                        wallets = state.wallets,
                        onWalletSelected = { walletId = it.walletId },
                        onAddNewWalletClick = {
                            newWalletName = ""
                            showCreateWalletDialog = true
                        }
                    )
                }
                item {
                    FieldLabel("Ngày và giờ")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        transactionTime = (transactionTime.clone() as Calendar).apply {
                                            set(year, month, day)
                                        }
                                    },
                                    transactionTime.get(Calendar.YEAR),
                                    transactionTime.get(Calendar.MONTH),
                                    transactionTime.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                            Text(SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(transactionTime.time), modifier = Modifier.padding(start = 6.dp))
                        }
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        transactionTime = (transactionTime.clone() as Calendar).apply {
                                            set(Calendar.HOUR_OF_DAY, hour)
                                            set(Calendar.MINUTE, minute)
                                        }
                                    },
                                    transactionTime.get(Calendar.HOUR_OF_DAY),
                                    transactionTime.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = null)
                            Text(SimpleDateFormat("HH:mm", Locale.US).format(transactionTime.time), modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
                item {
                    FieldLabel("Ghi chú")
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it.take(500) },
                        placeholder = { Text("Thông tin bổ sung (không bắt buộc)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            PrimaryButton(
                text = if (editingId == null) "Tạo giao dịch" else "Lưu thay đổi",
                onClick = {
                    val selectedCategoryId = categoryId ?: return@PrimaryButton
                    val selectedWalletId = walletId ?: return@PrimaryButton
                    val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(transactionTime.time)
                    viewModel.saveTransaction(
                        id = editingId,
                        request = PersonalTransactionRequest(
                            title = title.trim(),
                            amount = amount,
                            type = type,
                            categoryId = selectedCategoryId,
                            walletId = selectedWalletId,
                            note = note.trim().takeIf { it.isNotEmpty() },
                            transactionTime = timestamp
                        ),
                        onSuccess = { onSaved(it.id) }
                    )
                },
                enabled = title.isNotBlank() && amount > 0 && categoryId != null && walletId != null,
                isLoading = state.isSaving,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun <T> SelectionField(
    label: String,
    selectedText: String,
    items: List<T>,
    itemText: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        FieldLabel(label)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Chọn $label") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemText(item)) },
                        onClick = {
                            onSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletSelectionField(
    label: String,
    selectedText: String,
    wallets: List<WalletResponse>,
    onWalletSelected: (WalletResponse) -> Unit,
    onAddNewWalletClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        FieldLabel(label)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Chọn $label") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                wallets.forEach { wallet ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    wallet.name,
                                    fontWeight = if (wallet.isDefault) FontWeight.Bold else FontWeight.Normal
                                )
                                if (wallet.isDefault) {
                                    Text(
                                        "(Mặc định)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onWalletSelected(wallet)
                            expanded = false
                        }
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                "Ví khác... (Thêm ví mới)",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onAddNewWalletClick()
                    }
                )
            }
        }
    }
}

private fun String.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    runCatching {
        val date = take(10).split("-").map(String::toInt)
        val time = drop(11).take(5).split(":").map(String::toInt)
        calendar.set(date[0], date[1] - 1, date[2], time[0], time[1], 0)
    }
    return calendar
}
