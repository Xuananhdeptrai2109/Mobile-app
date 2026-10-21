package com.moneycare.app.ui.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupExpense
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.TextPrimary

@Composable
fun GroupOverviewContent(
    group: ExpenseGroup,
    onSeeAllExpenses: () -> Unit,
    onAddExpense: () -> Unit,
    onEditGroupName: () -> Unit,
    onAddMember: () -> Unit,
    onExpenseClick: (GroupExpense) -> Unit,
    onMemberClick: (GroupMember) -> Unit,
    canManageGroup: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GroupHeroSection(
                    groupName = group.name,
                    members = group.members,
                    onEditName = onEditGroupName,
                    onAddMember = onAddMember,
                    onMemberClick = onMemberClick,
                    canManageGroup = canManageGroup,
                    heroImageUrl = group.heroImageUrl
                )
            }

            item {
                GroupBudgetCard(totalExpense = group.totalExpense, budget = group.budget)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GroupStatCard(
                        label = "Thành viên",
                        value = group.members.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    GroupStatCard(
                        label = "Giao dịch",
                        value = group.expenses.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    val completed = group.tasks.count { it.isCompleted }
                    GroupStatCard(
                        label = "Nhiệm vụ",
                        value = "$completed/${group.tasks.size}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Giao dịch gần đây",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Xem tất cả",
                        color = PrimaryGreen,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clickable(onClick = onSeeAllExpenses)
                            .padding(4.dp)
                    )
                }
            }

            items(group.expenses.take(5)) { expense ->
                GroupExpenseItem(
                    expense = expense, 
                    showSplitInfo = false,
                    modifier = Modifier.clickable { onExpenseClick(expense) }
                )
            }
        }

        FloatingActionButton(
            onClick = onAddExpense,
            containerColor = PrimaryGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm chi tiêu")
        }
    }
}
