package com.moneycare.app.ui.group.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.components.*
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.ScreenBackground
import com.moneycare.app.ui.group.ConfirmGroupActionDialog
import com.moneycare.app.ui.group.EditMemberDialog
import com.moneycare.app.ui.group.GroupRouteStateScreen
import com.moneycare.app.util.formatVnd
import com.moneycare.app.viewmodel.GroupDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    groupId: String,
    memberId: String,
    onBackClick: () -> Unit,
    viewModel: GroupDetailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showRemoveConfirmation by rememberSaveable { mutableStateOf(false) }
    
    LaunchedEffect(groupId) {
        viewModel.ensureGroupLoaded(groupId)
    }

    LaunchedEffect(groupId, memberId, uiState.group) {
        if (uiState.group?.id == groupId) viewModel.loadMemberDetail(memberId)
    }

    val member = uiState.selectedMember?.takeIf { it.id == memberId }
    val stats = uiState.memberStats
    if (member == null || stats == null) {
        GroupRouteStateScreen(
            title = "Chi tiết thành viên",
            errorMessage = uiState.errorMessage,
            onRetry = { viewModel.loadGroup(groupId) },
            onBackClick = onBackClick,
            modifier = modifier
        )
        return
    }
    val currentUserIsOwner = uiState.group?.members?.any {
        it.isCurrentUser && it.role.equals("OWNER", ignoreCase = true)
    } == true
    val canRemove = currentUserIsOwner && !member.isCurrentUser &&
            !member.role.equals("OWNER", ignoreCase = true)

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
                title = "Chi tiết thành viên",
                onBackClick = onBackClick,
                menuItems = buildList {
                    if (canRemove) {
                        add("Sửa tên" to { showEditDialog = true })
                        add("Xóa khỏi nhóm" to { showRemoveConfirmation = true })
                    }
                },
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(LightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.take(1),
                            style = MaterialTheme.typography.displayMedium,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (member.isCurrentUser) "${member.name} (Bạn)" else member.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (member.role.equals("OWNER", ignoreCase = true)) "Chủ nhóm" else "Thành viên",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MemberStatCard(
                        label = "Đã trả",
                        value = formatVnd(stats.totalPaid),
                        tone = StatTone.POSITIVE,
                        modifier = Modifier.weight(1f)
                    )
                    MemberStatCard(
                        label = "Còn nợ",
                        value = formatVnd(stats.totalOwed),
                        tone = StatTone.NEGATIVE,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Text(
                    text = "Lịch sử chi tiêu",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(uiState.memberExpenses) { expense ->
                MemberTransactionItem(expense = expense, currentMember = member)
            }
        }
    }

    if (showEditDialog) {
        EditMemberDialog(
            initialName = member.name,
            isSaving = uiState.isSaving,
            onDismiss = { showEditDialog = false },
            onConfirm = { name ->
                viewModel.updateMember(member.id, name) {
                    showEditDialog = false
                    viewModel.loadMemberDetail(member.id)
                }
            }
        )
    }

    if (showRemoveConfirmation) {
        ConfirmGroupActionDialog(
            title = "Xóa thành viên?",
            message = "Xóa ${member.name} khỏi nhóm? Thành viên có lịch sử chi tiêu sẽ không thể bị xóa.",
            confirmLabel = "Xóa thành viên",
            destructive = true,
            isSaving = uiState.isSaving,
            onDismiss = { showRemoveConfirmation = false },
            onConfirm = { viewModel.removeMember(member.id, onBackClick) }
        )
    }
}
