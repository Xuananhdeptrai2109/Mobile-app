package com.moneycare.app.ui.group

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.theme.CardBackground
import com.moneycare.app.ui.theme.ChipBackground
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.TextSecondary
import com.moneycare.app.viewmodel.ExpenseFilter

@Composable
fun ExpenseFilterRow(
    selectedFilter: ExpenseFilter,
    onFilterSelected: (ExpenseFilter) -> Unit,
    onOpenAdvancedFilter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExpenseFilter.entries.forEach { filter ->
            FilterChip(
                label = filter.label,
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }

        if (onOpenAdvancedFilter != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
                    .clickable(onClick = onOpenAdvancedFilter),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Bộ lọc nâng cao", tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) PrimaryGreen else ChipBackground,
        label = "filterChipBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else TextSecondary,
        label = "filterChipText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = textColor, style = MaterialTheme.typography.bodyMedium)
    }
}
