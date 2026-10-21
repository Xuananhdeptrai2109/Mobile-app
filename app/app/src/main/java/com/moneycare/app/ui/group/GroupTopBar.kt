package com.moneycare.app.ui.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.moneycare.app.ui.theme.CardBackground
import com.moneycare.app.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupTopBar(
    groupName: String,
    isAdmin: Boolean,
    onBackClick: () -> Unit,
    onEditGroup: () -> Unit,
    onManageBudget: () -> Unit,
    onGroupSettings: () -> Unit,
    onViewReport: () -> Unit,
    onDeleteGroup: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = groupName,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
            }
        },
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Thêm", tint = TextPrimary)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                if (isAdmin) {
                    DropdownMenuItem(
                        text = { Text("Chỉnh sửa nhóm") },
                        onClick = { menuExpanded = false; onEditGroup() }
                    )
                    DropdownMenuItem(
                        text = { Text("Quản lý ngân sách") },
                        onClick = { menuExpanded = false; onManageBudget() }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Báo cáo & xuất dữ liệu") },
                    onClick = { menuExpanded = false; onViewReport() }
                )
                DropdownMenuItem(
                    text = { Text("Cài đặt nhóm") },
                    onClick = { menuExpanded = false; onGroupSettings() }
                )
                if (isAdmin) {
                    DropdownMenuItem(
                        text = { Text("Xóa nhóm", color = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDeleteGroup() }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CardBackground,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}
