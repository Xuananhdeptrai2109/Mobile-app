package com.moneycare.app.ui.group.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.ui.components.*
import com.moneycare.app.ui.theme.ScreenBackground
import com.moneycare.app.viewmodel.GroupDetailViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    taskId: String? = null,
    onBackClick: () -> Unit,
    viewModel: GroupDetailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.group ?: return
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }
    
    val existingTask = remember(taskId, group.tasks) {
        taskId?.let { id -> group.tasks.find { it.id == id } }
    }
    val isEditing = existingTask != null
    val currentUserIsOwner = group.members.any {
        it.isCurrentUser && it.role.equals("OWNER", ignoreCase = true)
    }

    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var assignee by remember { 
        mutableStateOf(
            existingTask?.assignee 
                ?: group.members.firstOrNull { it.isCurrentUser } 
                ?: group.members.first()
        ) 
    }
    var dueDate by remember {
        mutableStateOf(
            existingTask?.dueDate
                ?: LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        )
    }
    var description by remember { mutableStateOf(existingTask?.description ?: "") }
    
    val memberSheetState = rememberModalBottomSheetState()
    var showMemberSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = ScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GroupDetailTopBar(
                title = if (isEditing) "Sửa nhiệm vụ" else "Thêm nhiệm vụ",
                onBackClick = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
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
                    Column {
                        Text(
                            text = "Tên nhiệm vụ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AppTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Ví dụ: Đặt vé máy bay"
                        )
                    }
                }
                
                item {
                    MemberSelectorRow(
                        label = "Người thực hiện",
                        selectedMember = assignee,
                        onClick = { showMemberSheet = true }
                    )
                }
                
                item {
                    Column {
                        Text(
                            text = "Thời hạn",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AppTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            placeholder = "Ví dụ: 16/08/2026"
                        )
                    }
                }
                
                item {
                    Column {
                        Text(
                            text = "Mô tả",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AppTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Thêm chi tiết về nhiệm vụ"
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = if (isEditing) "Lưu thay đổi" else "Tạo nhiệm vụ",
                    onClick = {
                        if (isEditing && taskId != null) {
                            viewModel.updateTask(
                                taskId = taskId,
                                title = title,
                                assignee = assignee,
                                dueDate = dueDate,
                                description = if (description.isBlank()) null else description,
                                onSuccess = onBackClick
                            )
                        } else {
                            viewModel.addTask(
                                title = title,
                                assignee = assignee,
                                dueDate = dueDate,
                                description = if (description.isBlank()) null else description,
                                onSuccess = onBackClick
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && !uiState.isSaving
                )

                if (isEditing && taskId != null && currentUserIsOwner) {
                    SecondaryButton(
                        text = "Xóa nhiệm vụ",
                        onClick = {
                            viewModel.deleteTask(taskId, onSuccess = onBackClick)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    
    if (showMemberSheet) {
        MemberPickerBottomSheet(
            allMembers = group.members,
            onMemberSelected = { assignee = it },
            onDismiss = { showMemberSheet = false },
            sheetState = memberSheetState
        )
    }
}
