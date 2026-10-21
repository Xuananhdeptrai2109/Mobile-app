package com.moneycare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.ui.theme.ExpenseRed
import com.moneycare.app.ui.theme.ExpenseRedBg
import com.moneycare.app.ui.theme.IncomeGreen
import com.moneycare.app.ui.theme.IncomeGreenBg
import com.moneycare.app.util.toSignedVnd

/**
 * A single row in the "Giao dịch gần đây" (Recent transactions) list.
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val iconBg = if (isIncome) IncomeGreenBg else ExpenseRedBg

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transactionIcon(transaction.categoryIconName),
                contentDescription = null,
                tint = amountColor
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = transaction.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = transaction.amount.toSignedVnd(isIncome),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

private fun transactionIcon(iconName: String) = when (iconName.lowercase()) {
    "fastfood" -> Icons.Filled.Fastfood
    "directions_bus" -> Icons.Filled.DirectionsBus
    "shopping_bag" -> Icons.Filled.ShoppingBag
    "bolt" -> Icons.Filled.Bolt
    "local_cafe" -> Icons.Filled.LocalCafe
    "health_and_safety" -> Icons.Filled.HealthAndSafety
    "school" -> Icons.Filled.School
    "payments" -> Icons.Filled.Payments
    "redeem" -> Icons.Filled.Redeem
    "savings" -> Icons.Filled.Savings
    "account_balance_wallet" -> Icons.Filled.AccountBalanceWallet
    else -> Icons.Filled.ReceiptLong
}

/** Section header used above lists, e.g. "Giao dịch gần đây   Xem tất cả". */
@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = com.moneycare.app.ui.theme.PrimaryGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.then(
                    if (onActionClick != null) {
                        Modifier.clickableNoRipple(onActionClick)
                    } else Modifier
                )
            )
        }
    }
}

