package com.moneycare.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupTaskSummary
import com.moneycare.app.ui.components.GroupCard
import com.moneycare.app.ui.components.GroupQuickAction
import com.moneycare.app.ui.components.GroupTaskItem
import com.moneycare.app.ui.components.SectionHeader
import com.moneycare.app.ui.components.TipCard
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.TextPrimary
import com.moneycare.app.ui.theme.TextSecondary

/**
 * Content shown when the "Nhóm" tab is selected: the list of the user's groups
 * and their shared tasks.
 */
@Composable
fun GroupHomeContent(
    groups: List<ExpenseGroup>,
    groupTasks: List<GroupTaskSummary>,
    onCreateGroupClick: () -> Unit,
    onGroupClick: (ExpenseGroup) -> Unit,
    onGroupQuickAction: (ExpenseGroup, GroupQuickAction) -> Unit,
    onTaskClick: (GroupTaskSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            SectionHeader(
                title = "Nhóm của bạn",
                actionLabel = "Tạo nhóm +",
                onActionClick = onCreateGroupClick
            )
        }
        item {
            if (groups.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = onCreateGroupClick),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏖️", fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chưa có nhóm nào",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Bấm 'Tạo nhóm +' để bắt đầu chia sẻ chi tiêu cùng bạn bè!",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groups, key = { it.id }) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onGroupClick(group) },
                            onQuickAction = { action -> onGroupQuickAction(group, action) }
                        )
                    }
                }
            }
        }
        item {
            SectionHeader(
                title = "Nhiệm vụ của bạn",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(groupTasks, key = { it.id }) { task ->
            GroupTaskItem(task = task, onClick = { onTaskClick(task) })
        }
        item {
            TipCard(
                message = "Hãy thêm chi tiết chi tiêu vào từng nhiệm vụ để tính toán chính xác hơn nhé!",
                visible = true
            )
        }
    }
}
