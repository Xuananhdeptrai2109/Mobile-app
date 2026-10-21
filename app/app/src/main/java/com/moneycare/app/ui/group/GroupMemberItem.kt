package com.moneycare.app.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.ui.theme.CardBackground
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.TextPrimary
import com.moneycare.app.ui.theme.TextSecondary

@Composable
fun GroupMemberItem(
    member: GroupMember,
    isCurrentUserAdmin: Boolean,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onRemoveFromGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isOwner = member.role.equals("OWNER", ignoreCase = true)
    val displayName = if (member.isCurrentUser) "${member.name} (Bạn)" else member.name
    val roleLabel = if (isOwner) "Chủ nhóm" else "Thành viên"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LightGreen)
                .clickable { onViewDetail() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = member.name.take(1), color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
                .clickable { onViewDetail() }
        ) {
            Text(text = displayName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = roleLabel, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = TextSecondary)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Xem chi tiết") },
                    onClick = { menuExpanded = false; onViewDetail() }
                )
                if (isCurrentUserAdmin && !member.isCurrentUser) {
                    DropdownMenuItem(
                        text = { Text("Chỉnh sửa tên") },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Xóa khỏi nhóm", color = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onRemoveFromGroup() }
                    )
                }
            }
        }
    }
}
