package com.moneycare.app.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.personal.WalletResponse
import com.moneycare.app.data.model.personal.PersonalCategoryResponse
import com.moneycare.app.ui.theme.DarkGreen
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.SoftRed
import com.moneycare.app.ui.theme.TextPrimary
import com.moneycare.app.ui.theme.TextSecondary
import com.moneycare.app.util.DebtSimplifier
import com.moneycare.app.util.SettlementTransaction
import com.moneycare.app.util.toVnd

@Composable
fun GroupSettlementContent(
    group: ExpenseGroup,
    onSettleDebt: (String, String, Long) -> Unit,
    onSyncToWallet: (walletId: Long, categoryId: Long) -> Unit,
    wallets: List<WalletResponse>,
    categories: List<PersonalCategoryResponse>,
    isLoadingSyncOptions: Boolean,
    onLoadSyncOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val netBalances = remember(group.expenses, group.members, group.settlements) {
        DebtSimplifier.calculateNetBalances(group)
    }
    val simplifiedSettlements = remember(netBalances, group.members) {
        DebtSimplifier.simplifyDebts(group.members, netBalances)
    }

    // Tính tổng chi phí mà người dùng hiện tại phải chịu trong chuyến đi
    val currentUser = remember(group.members) {
        group.members.firstOrNull { it.isCurrentUser } ?: group.members.firstOrNull()
    }
    val myTotalSpent = remember(group.expenses, currentUser) {
        currentUser?.let { user ->
            val owed = group.expenses.flatMap { it.splitMembers }
                .filter { it.member.id == user.id }
                .sumOf { it.amount }
            if (owed > 0L) owed else group.expenses.filter { it.paidBy.id == user.id }.sumOf { it.amount }
        } ?: 0L
    }

    var showSyncDialog by rememberSaveable { mutableStateOf(false) }
    var selectedWalletId by rememberSaveable { mutableStateOf<Long?>(null) }
    val defaultCategoryId = remember(categories) { categories.firstOrNull()?.categoryId }

    LaunchedEffect(wallets) {
        if (selectedWalletId == null || wallets.none { it.walletId == selectedWalletId }) {
            selectedWalletId = wallets.firstOrNull { it.isDefault }?.walletId
                ?: wallets.firstOrNull()?.walletId
        }
    }

    // Dialog Đồng bộ vào ví cá nhân
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Sync,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Đồng bộ vào ví cá nhân", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ghi nhận phần tiền thực chi của bạn trong chuyến đi này vào ví cá nhân để giữ báo cáo chi tiêu chính xác.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Tổng tiền thực chi của bạn:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = myTotalSpent.toVnd(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen
                            )
                        }
                    }

                    if (isLoadingSyncOptions) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
                    } else if (wallets.isNotEmpty()) {
                        Text("Chọn ví ghi nhận khoản trừ:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            wallets.forEach { wallet ->
                                Surface(
                                    onClick = { selectedWalletId = wallet.walletId },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedWalletId == wallet.walletId) PrimaryGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (selectedWalletId == wallet.walletId) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen) else null,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(wallet.name, fontWeight = FontWeight.Medium)
                                        Text(wallet.balance.toVnd(), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        if (categories.isEmpty()) {
                            Text(
                                "Không có danh mục chi tiêu để đồng bộ.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = onLoadSyncOptions) { Text("Thử lại") }
                        }
                    } else {
                        Text(
                            "Không tải được ví cá nhân hoặc tài khoản chưa có ví.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = onLoadSyncOptions) { Text("Thử lại") }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val walletId = selectedWalletId ?: wallets.firstOrNull()?.walletId
                        if (walletId != null) {
                            defaultCategoryId?.let { categoryId ->
                                onSyncToWallet(walletId, categoryId)
                                showSyncDialog = false
                            }
                        }
                    },
                    enabled = myTotalSpent > 0L && selectedWalletId != null && defaultCategoryId != null,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Đồng bộ ngay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Thẻ đồng bộ chi tiêu vào ví cá nhân
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = DarkGreen)
                            Spacer(Modifier.width(8.dp))
                            Text("Chi phí cá nhân của bạn", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(myTotalSpent.toVnd(), fontWeight = FontWeight.ExtraBold, color = DarkGreen, fontSize = 18.sp)
                    }

                    Text(
                        text = "Sau khi chuyến đi kết thúc, bạn có thể tự động ghi nhận số tiền này vào sổ thu chi cá nhân mà không cần nhập tay lại.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Button(
                        onClick = {
                            showSyncDialog = true
                            onLoadSyncOptions()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Đồng bộ vào Ví cá nhân")
                    }
                }
            }
        }

        // 2. Kế hoạch chuyển khoản rút gọn (Debt Simplification)
        item {
            Text(
                text = "Kế hoạch Quyết toán Rút gọn (Tối ưu)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Thuật toán Greedy tự động bù trừ nợ chéo, giúp nhóm thanh toán xong với số lần chuyển khoản ít nhất.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (simplifiedSettlements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(48.dp))
                        Text(
                            text = "Tất cả công nợ đã được thanh toán sòng phẳng! 🎉",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Không có thành viên nào còn nợ tiền trong nhóm.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(simplifiedSettlements) { settle ->
                SettlementCard(
                    settle = settle,
                    onSettleClick = {
                        onSettleDebt(settle.fromMemberId, settle.toMemberId, settle.amount)
                    }
                )
            }
        }

        // 3. Tình trạng số dư ròng của từng thành viên
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Bảng cân đối thành viên (Net Balance)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(group.members, key = { it.id }) { member ->
            val balance = netBalances[member.id] ?: 0L
            MemberBalanceRow(member = member, netBalance = balance)
        }
    }
}

@Composable
private fun SettlementCard(
    settle: SettlementTransaction,
    onSettleClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Người trả (Debtor)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(36.dp).background(SoftRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(settle.fromMemberName.take(1).uppercase(), color = SoftRed, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(settle.fromMemberName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Cần trả", style = MaterialTheme.typography.labelSmall, color = SoftRed)
                    }
                }

                // Mũi tên & Số tiền
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = settle.amount.toVnd(),
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGreen,
                        fontSize = 15.sp
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Người nhận (Creditor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(settle.toMemberName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Nhận về", style = MaterialTheme.typography.labelSmall, color = DarkGreen)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(36.dp).background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(settle.toMemberName.take(1).uppercase(), color = DarkGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Nút bấm xác nhận thanh toán
            OutlinedButton(
                onClick = onSettleClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                Spacer(Modifier.width(6.dp))
                Text("Xác nhận đã trả tiền (${settle.fromMemberName} ➔ ${settle.toMemberName})", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MemberBalanceRow(
    member: com.moneycare.app.data.model.GroupMember,
    netBalance: Long
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.name.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(member.name, fontWeight = FontWeight.SemiBold)
                    if (member.isCurrentUser) {
                        Text("Bạn", style = MaterialTheme.typography.labelSmall, color = PrimaryGreen)
                    }
                }
            }

            when {
                netBalance > 0L -> {
                    Text(
                        text = "+${netBalance.toVnd()}",
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                netBalance < 0L -> {
                    Text(
                        text = netBalance.toVnd(),
                        color = SoftRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> {
                    Text(
                        text = "0 đ (Đã hòa)",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
