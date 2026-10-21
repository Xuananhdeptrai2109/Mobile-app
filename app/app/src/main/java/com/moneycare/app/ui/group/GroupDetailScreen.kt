package com.moneycare.app.ui.group

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.ui.components.MemberPickerBottomSheet
import com.moneycare.app.ui.theme.ScreenBackground
import com.moneycare.app.viewmodel.GroupDetailTab
import com.moneycare.app.viewmodel.GroupDetailUiState
import com.moneycare.app.viewmodel.GroupDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBackClick: () -> Unit = {},
    onNavigateToAddExpense: (String) -> Unit = {},
    onNavigateToAddTask: (String) -> Unit = {},
    onNavigateToEditTask: (String) -> Unit = {},
    onNavigateToMemberDetail: (String) -> Unit = {},
    onNavigateToExpenseDetail: (String) -> Unit = {},
    onNavigateToReport: (String) -> Unit = {},
    initialTab: GroupDetailTab = GroupDetailTab.OVERVIEW,
    onGroupClosed: () -> Unit = onBackClick,
    modifier: Modifier = Modifier,
    viewModel: GroupDetailViewModel = viewModel()
) {
    val uiState: GroupDetailUiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val group: ExpenseGroup? = uiState.group
    val currentUserIsOwner = group?.members?.any { member ->
        member.isCurrentUser && member.role.equals("OWNER", ignoreCase = true)
    } == true

    LaunchedEffect(groupId, initialTab) {
        viewModel.selectTab(initialTab)
        viewModel.loadGroup(groupId)
    }

    LaunchedEffect(uiState.errorMessage) {
        if (group != null) {
            uiState.errorMessage?.let { message ->
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
        }
    }

    val memberSheetState = rememberModalBottomSheetState()
    var showMemberPicker by remember { mutableStateOf(false) }
    var showEditGroup by remember { mutableStateOf(false) }
    var showBudget by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<GroupMember?>(null) }
    var memberToRemove by remember { mutableStateOf<GroupMember?>(null) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = ScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GroupTopBar(
                groupName = group?.name ?: "",
                isAdmin = currentUserIsOwner,
                onBackClick = onBackClick,
                onEditGroup = { showEditGroup = true },
                onManageBudget = { showBudget = true },
                onGroupSettings = { viewModel.selectTab(GroupDetailTab.MEMBERS) },
                onViewReport = { onNavigateToReport(groupId) },
                onDeleteGroup = { showDeleteConfirmation = true },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && group == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null && group == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiState.errorMessage ?: "Đã có lỗi xảy ra")
                    }
                }
                group != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        GroupTabSwitcher(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = viewModel::selectTab,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        AnimatedContent(
                            targetState = uiState.selectedTab,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideIntoTab())
                                    .togetherWith(fadeOut(tween(150)))
                            },
                            label = "groupDetailTabContent"
                        ) { tab ->
                            when (tab) {
                                GroupDetailTab.OVERVIEW -> GroupOverviewContent(
                                    group = group,
                                    onSeeAllExpenses = { viewModel.selectTab(GroupDetailTab.EXPENSES) },
                                    onAddExpense = { onNavigateToAddExpense(groupId) },
                                    onEditGroupName = {
                                        if (currentUserIsOwner) showEditGroup = true
                                        else viewModel.showMessage("Chỉ chủ nhóm mới được chỉnh sửa nhóm")
                                    },
                                    onAddMember = { showMemberPicker = true },
                                    onExpenseClick = { expense -> onNavigateToExpenseDetail(expense.id) },
                                    onMemberClick = { member -> onNavigateToMemberDetail(member.id) },
                                    canManageGroup = currentUserIsOwner
                                )
                                GroupDetailTab.EXPENSES -> GroupExpenseContent(
                                    group = group,
                                    currentUser = group.members.find { member -> member.isCurrentUser },
                                    selectedFilter = uiState.selectedExpenseFilter,
                                    onFilterSelected = viewModel::selectExpenseFilter,
                                    onAddExpense = { onNavigateToAddExpense(groupId) },
                                    onExpenseClick = { expense -> onNavigateToExpenseDetail(expense.id) }
                                )
                                GroupDetailTab.SETTLEMENT -> GroupSettlementContent(
                                    group = group,
                                    onSettleDebt = { fromId, toId ->
                                        viewModel.settleDebtBetweenMembers(fromId, toId)
                                    },
                                    onSyncToWallet = { walletId, categoryId, amount ->
                                        viewModel.syncTripToPersonalWallet(walletId, categoryId, amount) {
                                            viewModel.showMessage("Đã đồng bộ chi phí vào ví cá nhân thành công!")
                                        }
                                    }
                                )
                                GroupDetailTab.TASKS -> GroupTaskContent(
                                    group = group,
                                    onToggleTask = viewModel::toggleTaskStatus,
                                    onTaskClick = { task -> onNavigateToEditTask(task.id) },
                                    onAddTask = { onNavigateToAddTask(groupId) }
                                )
                                GroupDetailTab.MEMBERS -> GroupMemberContent(
                                    group = group,
                                    currentUserIsAdmin = currentUserIsOwner,
                                    onViewMemberDetail = { member -> onNavigateToMemberDetail(member.id) },
                                    onEditMember = { memberToEdit = it },
                                    onRemoveMember = { memberToRemove = it },
                                    onInviteMember = { showMemberPicker = true }
                                )
                                GroupDetailTab.ACTIVITY -> GroupActivityContent(uiState.activities)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMemberPicker) {
        MemberPickerBottomSheet(
            title = "Thêm thành viên",
            allMembers = emptyList(),
            onMemberSelected = {},
            onAddNewMember = { name: String -> viewModel.addMember(name) },
            onDismiss = { showMemberPicker = false },
            sheetState = memberSheetState
        )
    }

    if (showEditGroup && group != null) {
        EditGroupDialog(
            group = group,
            isSaving = uiState.isSaving,
            onDismiss = { showEditGroup = false },
            onConfirm = { name, description, budget ->
                viewModel.updateGroup(name, description, budget) { showEditGroup = false }
            }
        )
    }

    if (showBudget && group != null) {
        EditGroupDialog(
            group = group,
            budgetOnly = true,
            isSaving = uiState.isSaving,
            onDismiss = { showBudget = false },
            onConfirm = { name, description, budget ->
                viewModel.updateGroup(name, description, budget) { showBudget = false }
            }
        )
    }

    if (showDeleteConfirmation) {
        ConfirmGroupActionDialog(
            title = "Xóa nhóm?",
            message = "Toàn bộ chi tiêu, nhiệm vụ và thành viên của nhóm sẽ bị xóa vĩnh viễn.",
            confirmLabel = "Xóa nhóm",
            destructive = true,
            isSaving = uiState.isSaving,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = { viewModel.deleteGroup(onGroupClosed) }
        )
    }

    memberToEdit?.let { member ->
        EditMemberDialog(
            initialName = member.name,
            isSaving = uiState.isSaving,
            onDismiss = { memberToEdit = null },
            onConfirm = { name ->
                viewModel.updateMember(member.id, name) { memberToEdit = null }
            }
        )
    }

    memberToRemove?.let { member ->
        ConfirmGroupActionDialog(
            title = "Xóa thành viên?",
            message = "Xóa ${member.name} khỏi nhóm? Thành viên có lịch sử chi tiêu sẽ được giữ lại để bảo toàn dữ liệu.",
            confirmLabel = "Xóa thành viên",
            destructive = true,
            isSaving = uiState.isSaving,
            onDismiss = { memberToRemove = null },
            onConfirm = { viewModel.removeMember(member.id) { memberToRemove = null } }
        )
    }
}

private fun slideIntoTab(): androidx.compose.animation.EnterTransition =
    slideInHorizontally(
        animationSpec = tween(220),
        initialOffsetX = { fullWidth -> fullWidth / 12 }
    )
