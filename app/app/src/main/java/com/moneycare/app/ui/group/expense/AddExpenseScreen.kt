package com.moneycare.app.ui.group.expense

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneycare.app.data.model.ExpenseSplit
import com.moneycare.app.data.model.ExpenseSplitMethod
import com.moneycare.app.ui.components.*
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.viewmodel.GroupDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseId: String? = null,
    onBackClick: () -> Unit,
    viewModel: GroupDetailViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.group ?: return
    val existingExpense = remember(group.expenses, expenseId) {
        expenseId?.let { id -> group.expenses.find { it.id == id } }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }
    
    var title by remember(expenseId) { mutableStateOf(existingExpense?.title.orEmpty()) }
    var amount by remember(expenseId) { mutableLongStateOf(existingExpense?.amount ?: 0L) }
    var noteInput by remember(expenseId) { mutableStateOf(existingExpense?.note.orEmpty()) }
    
    var paidBy by remember(group.id) { 
        mutableStateOf(existingExpense?.paidBy ?: group.members.firstOrNull { it.isCurrentUser } ?: group.members.firstOrNull() ?: com.moneycare.app.data.model.GroupMember("", "N/A"))
    }
    var splitMembers by remember(group.id) { 
        mutableStateOf(existingExpense?.splitMembers ?: group.members.map { ExpenseSplit(it, 0L) })
    }
    var splitMethod by remember(expenseId) {
        mutableStateOf(existingExpense?.splitMethod ?: ExpenseSplitMethod.EQUAL)
    }
    val splitIsValid = remember(splitMembers, splitMethod, amount) {
        when (splitMethod) {
            ExpenseSplitMethod.EQUAL -> splitMembers.isNotEmpty()
            ExpenseSplitMethod.CUSTOM -> splitMembers.isNotEmpty() &&
                splitMembers.all { it.amount > 0L } && splitMembers.sumOf { it.amount } == amount
            ExpenseSplitMethod.PERCENTAGE -> splitMembers.isNotEmpty() &&
                splitMembers.all { (it.percentage ?: 0.0) > 0.0 } &&
                kotlin.math.abs(splitMembers.sumOf { it.percentage ?: 0.0 } - 100.0) < 0.001
        }
    }
    
    val memberSheetState = rememberModalBottomSheetState()
    var showMemberSheet by remember { mutableStateOf(false) }

    // Receipt Image Picker Launcher for AI extraction
    val receiptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.extractExpenseFromReceipt(context, uri) { extractedTitle, extractedAmount, extractedNote ->
                if (extractedTitle.isNotBlank()) title = extractedTitle
                if (extractedAmount > 0L) amount = extractedAmount
                if (!extractedNote.isNullOrBlank()) noteInput = extractedNote
                Toast.makeText(context, "✨ AI đã trích xuất hóa đơn thành công!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Audio Picker Launcher for AI extraction
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.extractExpenseFromAudio(context, uri) { extractedTitle, extractedAmount, extractedNote ->
                if (extractedTitle.isNotBlank()) title = extractedTitle
                if (extractedAmount > 0L) amount = extractedAmount
                if (!extractedNote.isNullOrBlank()) noteInput = extractedNote
                Toast.makeText(context, "✨ AI đã phân tích giọng nói thành công!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GroupDetailTopBar(
                title = if (existingExpense == null) "Thêm chi tiêu" else "Sửa chi tiêu",
                onBackClick = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        CurrencyTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = "Số tiền"
                        )
                    }

                    item {
                        Column {
                            Text(
                                text = "Tên chi tiêu",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            AppTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = "Ví dụ: Ăn tối hải sản"
                            )
                        }
                    }

                    item {
                        MemberSelectorRow(
                            label = "Người thanh toán",
                            selectedMember = paidBy,
                            onClick = { showMemberSheet = true }
                        )
                    }

                    item {
                        Column {
                            Text(
                                text = "Chia cho",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            GroupAvatarRow(
                                members = group.members,
                                selectedMembers = splitMembers.map { it.member },
                                onToggleMember = { member ->
                                    if (splitMembers.any { it.member == member }) {
                                        splitMembers = splitMembers.filter { it.member != member }
                                    } else {
                                        splitMembers = splitMembers + ExpenseSplit(member, 0L)
                                    }
                                    splitMembers = redistributeSplits(splitMembers, amount, splitMethod)
                                }
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Cách chia tiền",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExpenseSplitMethod.entries.forEach { method ->
                                    FilterChip(
                                        selected = splitMethod == method,
                                        onClick = {
                                            splitMethod = method
                                            splitMembers = redistributeSplits(splitMembers, amount, method)
                                        },
                                        label = { Text(method.label, maxLines = 1) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            splitMembers.forEachIndexed { index, split ->
                                val label = when (splitMethod) {
                                    ExpenseSplitMethod.EQUAL -> {
                                        val equal = equalShare(amount, splitMembers.size, index)
                                        "${split.member.name}: ${equal} VND"
                                    }
                                    ExpenseSplitMethod.CUSTOM -> split.member.name
                                    ExpenseSplitMethod.PERCENTAGE -> split.member.name
                                }
                                if (splitMethod == ExpenseSplitMethod.EQUAL) {
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    OutlinedTextField(
                                        value = when (splitMethod) {
                                            ExpenseSplitMethod.CUSTOM -> split.amount.takeIf { it > 0L }?.toString().orEmpty()
                                            ExpenseSplitMethod.PERCENTAGE -> split.percentage
                                                ?.let { value -> if (value % 1.0 == 0.0) value.toInt().toString() else "%.2f".format(value) }
                                                .orEmpty()
                                            ExpenseSplitMethod.EQUAL -> ""
                                        },
                                        onValueChange = { input ->
                                            splitMembers = splitMembers.mapIndexed { itemIndex, item ->
                                                if (itemIndex != index) item else when (splitMethod) {
                                                    ExpenseSplitMethod.CUSTOM -> item.copy(
                                                        amount = input.filter(Char::isDigit).toLongOrNull() ?: 0L
                                                    )
                                                    ExpenseSplitMethod.PERCENTAGE -> item.copy(
                                                        percentage = input.replace(',', '.').toDoubleOrNull()
                                                    )
                                                    ExpenseSplitMethod.EQUAL -> item
                                                }
                                            }
                                        },
                                        label = { Text(label) },
                                        suffix = { Text(if (splitMethod == ExpenseSplitMethod.CUSTOM) "VND" else "%") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = if (splitMethod == ExpenseSplitMethod.CUSTOM) {
                                                KeyboardType.Number
                                            } else KeyboardType.Decimal
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            if (!splitIsValid) {
                                Text(
                                    text = when (splitMethod) {
                                        ExpenseSplitMethod.CUSTOM -> "Tổng số tiền chia phải bằng $amount VND"
                                        ExpenseSplitMethod.PERCENTAGE -> "Tổng tỷ lệ phải bằng 100%"
                                        ExpenseSplitMethod.EQUAL -> "Hãy chọn ít nhất một thành viên"
                                    },
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    item {
                        Column {
                            Text(
                                text = "✨ Trích xuất tự động bằng AI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AttachmentButton(
                                    icon = Icons.Filled.Mic,
                                    label = "Giọng nói",
                                    isActive = false,
                                    onClick = { audioLauncher.launch("audio/*") },
                                    modifier = Modifier.weight(1f)
                                )
                                AttachmentButton(
                                    icon = Icons.Filled.PhotoCamera,
                                    label = "Ảnh hóa đơn",
                                    isActive = false,
                                    onClick = { receiptLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (noteInput.isNotBlank()) {
                        item {
                            Column {
                                Text(
                                    text = "Ghi chú từ AI",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                AppTextField(
                                    value = noteInput,
                                    onValueChange = { noteInput = it },
                                    placeholder = "Ghi chú chi tiêu"
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }

                PrimaryButton(
                    text = if (existingExpense == null) "Tạo chi tiêu" else "Lưu thay đổi",
                    onClick = {
                        if (existingExpense == null) {
                            viewModel.addExpense(
                                title = title,
                                amount = amount,
                                paidBy = paidBy,
                                splitMembers = splitMembers,
                                splitMethod = splitMethod,
                                note = if (noteInput.isBlank()) null else noteInput,
                                onSuccess = onBackClick
                            )
                        } else {
                            viewModel.updateExpense(
                                expenseId = existingExpense.id,
                                title = title,
                                amount = amount,
                                paidBy = paidBy,
                                splitMembers = splitMembers,
                                splitMethod = splitMethod,
                                note = if (noteInput.isBlank()) null else noteInput,
                                onSuccess = onBackClick
                            )
                        }
                    },
                    modifier = Modifier.padding(20.dp),
                    enabled = title.isNotBlank() && amount > 0 && splitIsValid && !uiState.isSaving
                )
            }

            // AI Analyzing Loading Overlay
            if (uiState.isAiAnalyzing) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = PrimaryGreen)
                                Text(
                                    text = "✨ Gemini AI đang trích xuất dữ liệu...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PrimaryGreen
                                )
                                Text(
                                    text = "Vui lòng chờ trong giây lát",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMemberSheet) {
        MemberPickerBottomSheet(
            allMembers = group.members,
            onMemberSelected = { paidBy = it },
            onDismiss = { showMemberSheet = false },
            sheetState = memberSheetState
        )
    }
}

@Composable
private fun AttachmentButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) PrimaryGreen.copy(alpha = 0.15f) else LightGreen.copy(alpha = 0.4f),
        label = "bgColor"
    )

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) PrimaryGreen else LightGreen
        )
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryGreen,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = PrimaryGreen
            )
        }
    }
}

private fun redistributeSplits(
    splits: List<ExpenseSplit>,
    totalAmount: Long,
    method: ExpenseSplitMethod
): List<ExpenseSplit> {
    if (splits.isEmpty()) return splits
    return when (method) {
        ExpenseSplitMethod.EQUAL -> splits
        ExpenseSplitMethod.CUSTOM -> splits.mapIndexed { index, split ->
            split.copy(amount = equalShare(totalAmount, splits.size, index), percentage = null)
        }
        ExpenseSplitMethod.PERCENTAGE -> {
            val base = kotlin.math.floor(10000.0 / splits.size) / 100.0
            var assigned = 0.0
            splits.mapIndexed { index, split ->
                val percentage = if (index == splits.lastIndex) 100.0 - assigned else base
                assigned += percentage
                split.copy(amount = 0L, percentage = percentage)
            }
        }
    }
}

private fun equalShare(totalAmount: Long, memberCount: Int, index: Int): Long {
    if (memberCount <= 0) return 0L
    val base = totalAmount / memberCount
    return if (index == memberCount - 1) totalAmount - base * (memberCount - 1) else base
}
