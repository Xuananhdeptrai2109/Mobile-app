package com.moneycare.app.ui.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.ui.theme.TextPrimary

@Composable
fun GroupMemberContent(
    group: ExpenseGroup,
    currentUserIsAdmin: Boolean,
    onViewMemberDetail: (GroupMember) -> Unit,
    onEditMember: (GroupMember) -> Unit,
    onRemoveMember: (GroupMember) -> Unit,
    onInviteMember: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Thành viên nhóm",
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(group.members, key = { it.id }) { member ->
            GroupMemberItem(
                member = member,
                isCurrentUserAdmin = currentUserIsAdmin,
                onViewDetail = { onViewMemberDetail(member) },
                onEdit = { onEditMember(member) },
                onRemoveFromGroup = { onRemoveMember(member) }
            )
        }

        if (currentUserIsAdmin) {
            item {
                InviteMemberButton(onClick = onInviteMember)
            }
        }
    }
}
