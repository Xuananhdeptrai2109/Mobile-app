package com.moneycare.app.ui.group.create

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneycare.app.util.getInitials
import com.moneycare.app.ui.components.ValidationMessage
import com.moneycare.app.ui.theme.*
import java.io.ByteArrayOutputStream

// ============================================================
// SCREEN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit = {},
    onGroupCreated: () -> Unit = {},
    viewModel: CreateGroupViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf(listOf(MemberUi("current", "Tôi", PrimaryGreen))) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMemberPicker by remember { mutableStateOf(false) }

    // State for Group Image
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var selectedBase64 by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val (bitmap, base64) = processUriToBitmapAndBase64(context, uri)
            selectedBitmap = bitmap
            selectedBase64 = base64
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onGroupCreated()
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            CreateGroupTopBar(onBack = onBack)
        },
        bottomBar = {
            CreateGroupButton(
                enabled = groupName.isNotBlank() && !uiState.isLoading,
                isLoading = uiState.isLoading,
                onClick = {
                    if (groupName.isBlank()) {
                        errorMessage = "Tên nhóm không được để trống"
                    } else {
                        errorMessage = null
                        viewModel.createGroup(
                            name = groupName,
                            description = description,
                            budget = budgetInput.toLongOrNull(),
                            members = selectedMembers,
                            imageBase64 = selectedBase64
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

                item {
                    GroupImagePickerHeader(
                        selectedBitmap = selectedBitmap,
                        onPickImageClick = { imagePickerLauncher.launch("image/*") },
                        onRemoveImageClick = {
                            selectedImageUri = null
                            selectedBitmap = null
                            selectedBase64 = null
                        }
                    )
                }

                item {
                    GroupNameField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        errorMessage = errorMessage
                    )
                }

                item {
                    GroupDescriptionField(
                        value = description,
                        onValueChange = { description = it }
                    )
                }

                item {
                    BudgetInputField(
                        value = budgetInput,
                        onValueChange = { input -> budgetInput = input.filter { it.isDigit() } }
                    )
                }

                item {
                    SelectedMembersSection(
                        members = selectedMembers,
                        onAddClick = { showMemberPicker = true },
                        onRemoveMember = { member -> 
                            selectedMembers = selectedMembers.filter { it.id != member.id }
                        }
                    )
                }

                if (uiState.error != null) {
                    item {
                        ValidationMessage(
                            message = uiState.error!!, 
                            isError = true,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
            
            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            }
        }
    }

    if (showMemberPicker) {
        MemberPickerBottomSheet(
            initiallySelected = selectedMembers,
            onDismiss = { showMemberPicker = false },
            onUpdate = {
                selectedMembers = it
            }
        )
    }
}

// ============================================================
// HELPER: PROCESS IMAGE URI TO BITMAP & BASE64
// ============================================================
private fun processUriToBitmapAndBase64(context: Context, uri: Uri): Pair<ImageBitmap?, String?> {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return Pair(null, null)
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return Pair(null, null)
        inputStream.close()

        val maxDimension = 1024
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = Math.min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
            val newWidth = Math.round(ratio * width)
            val newHeight = Math.round(ratio * height)
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        
        Pair(scaledBitmap.asImageBitmap(), base64String)
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(null, null)
    }
}

// ============================================================
// TOP BAR
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGroupTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Tạo nhóm mới",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        )
    )
}

// ============================================================
// GROUP IMAGE PICKER HEADER (Ảnh ban đầu + Thêm ảnh)
// ============================================================
@Composable
private fun GroupImagePickerHeader(
    selectedBitmap: ImageBitmap?,
    onPickImageClick: () -> Unit,
    onRemoveImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
                    .background(LightGreen)
                    .border(2.dp, PrimaryGreen.copy(alpha = 0.6f), CircleShape)
                    .clickable(onClick = onPickImageClick),
                contentAlignment = Alignment.Center
            ) {
                if (selectedBitmap != null) {
                    Image(
                        bitmap = selectedBitmap,
                        contentDescription = "Ảnh nhóm",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Initial Default Group Avatar Header
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🏖️", fontSize = 40.sp)
                    }
                }
            }

            // Camera Badge Icon
            Surface(
                onClick = onPickImageClick,
                shape = CircleShape,
                color = PrimaryGreen,
                contentColor = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Thêm ảnh",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPickImageClick) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (selectedBitmap != null) "Thay đổi ảnh" else "Thêm ảnh nhóm",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            if (selectedBitmap != null) {
                TextButton(onClick = onRemoveImageClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Về ảnh mặc định",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// FIELD — TÊN NHÓM
// ============================================================
@Composable
private fun GroupNameField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?
) {
    Column {
        FieldLabel("Tên nhóm")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 50) onValueChange(it) },
            placeholder = { Text("Ví dụ: Đà Nẵng Trip", color = TextSecondary) },
            singleLine = true,
            isError = errorMessage != null,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        )
        if (errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(errorMessage, color = ExpenseRed, fontSize = 12.sp)
        }
    }
}

// ============================================================
// FIELD — MÔ TẢ
// ============================================================
@Composable
private fun GroupDescriptionField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        FieldLabel("Mô tả (tùy chọn)")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 150) onValueChange(it) },
            placeholder = { Text("Nhập mô tả", color = TextSecondary) },
            maxLines = 2,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        )
    }
}

// ============================================================
// FIELD — NGÂN SÁCH
// ============================================================
@Composable
private fun BudgetInputField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        FieldLabel("Ngân sách (tùy chọn)")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = formatVnd(value.toLongOrNull()),
            onValueChange = { input -> onValueChange(input.filter { it.isDigit() } ) },
            placeholder = { Text("0", color = TextSecondary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                Text("đ", color = TextSecondary, modifier = Modifier.padding(end = 12.dp))
            },
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        )
    }
}

/** Format số Long thành chuỗi có dấu chấm ngăn cách hàng nghìn kiểu VN. */
private fun formatVnd(amount: Long?): String {
    if (amount == null) return ""
    return amount.toString().reversed().chunked(3).joinToString(".").reversed()
}

// ============================================================
// THÀNH VIÊN
// ============================================================
@Composable
private fun SelectedMembersSection(
    members: List<MemberUi>,
    onAddClick: () -> Unit,
    onRemoveMember: (MemberUi) -> Unit
) {
    Column {
        FieldLabel("Thành viên (${members.size})")
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSoft),
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(members) { member ->
                    Box {
                        MemberAvatar(member)
                    }
                }
                item {
                    AddMemberButton(onAddClick)
                }
            }
        }
    }
}

@Composable
private fun MemberAvatar(member: MemberUi) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(member.avatarColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getInitials(member.name),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun AddMemberButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, OutlineSoft, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Thêm thành viên", tint = PrimaryGreen)
        }
    }
}

// ============================================================
// BOTTOM SHEET — CHỌN THÀNH VIÊN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberPickerBottomSheet(
    initiallySelected: List<MemberUi>,
    onDismiss: () -> Unit,
    onUpdate: (List<MemberUi>) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    val selectedMembers = initiallySelected

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Thêm thành viên",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                "Bạn có thể nhập tên để thêm nhanh thành viên",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text("Nhập tên (VD: Tùng, Lan...)", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            val newMember = MemberUi(
                                id = "custom_${System.currentTimeMillis()}",
                                name = nameInput,
                                avatarColor = listOf(
                                    Color(0xFF8FD3A6), Color(0xFFF4C95D), 
                                    Color(0xFFE88B8B), Color(0xFF76A9EA)
                                ).random()
                            )
                            onUpdate(selectedMembers + newMember)
                            nameInput = ""
                            onDismiss()
                        }
                    },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Thêm", color = Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryGreen,
    unfocusedBorderColor = OutlineSoft,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White
)

@Composable
private fun CreateGroupButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    disabledContainerColor = OutlineSoft
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Tạo nhóm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
