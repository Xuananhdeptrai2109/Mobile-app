package com.moneycare.app.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.ExpenseCategory
import com.moneycare.app.data.model.GroupExpense
import com.moneycare.app.ui.theme.CardBackground
import com.moneycare.app.ui.theme.ExpenseRed
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.TextPrimary
import com.moneycare.app.ui.theme.TextSecondary
import com.moneycare.app.util.formatVnd

private fun categoryEmoji(category: ExpenseCategory): String = category.emoji

@Composable
fun GroupExpenseItem(
    expense: GroupExpense,
    modifier: Modifier = Modifier,
    showSplitInfo: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBackground)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LightGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(categoryEmoji(expense.category), style = MaterialTheme.typography.titleMedium)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = expense.title,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${expense.paidBy.name} đã thanh toán",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${expense.date}, ${expense.time}",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatVnd(expense.amount),
                color = ExpenseRed,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            if (showSplitInfo) {
                Text(
                    text = "Chia cho ${expense.splitMemberCount} người",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
