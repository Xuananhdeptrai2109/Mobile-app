package com.moneycare.app.ui.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupTask
import com.moneycare.app.ui.theme.DividerColor
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.TextPrimary

@Composable
fun GroupTaskContent(
    group: ExpenseGroup,
    onToggleTask: (String) -> Unit,
    onTaskClick: (GroupTask) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completed = group.tasks.count { it.isCompleted }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TaskProgressCard(completedTasks = completed, totalTasks = group.tasks.size)
        }

        item {
            Text(
                text = "Danh sách nhiệm vụ",
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        itemsIndexed(group.tasks) { index, task ->
            GroupTaskItem(
                task = task,
                onToggle = { onToggleTask(task.id) },
                onClick = { onTaskClick(task) }
            )
            if (index != group.tasks.lastIndex) {
                Divider(color = DividerColor, thickness = 1.dp)
            }
        }

        item {
            Button(
                onClick = onAddTask,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(text = "Thêm nhiệm vụ", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
