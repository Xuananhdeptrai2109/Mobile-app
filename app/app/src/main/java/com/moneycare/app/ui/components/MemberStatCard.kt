package com.moneycare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.theme.LightGreen
import com.moneycare.app.ui.theme.PrimaryGreen

enum class StatTone {
    POSITIVE, NEGATIVE, NEUTRAL
}

@Composable
fun MemberStatCard(
    label: String,
    value: String,
    tone: StatTone,
    modifier: Modifier = Modifier
) {
    val bgColor = when (tone) {
        StatTone.POSITIVE -> LightGreen
        StatTone.NEGATIVE -> Color(0xFFFDEDEC)
        StatTone.NEUTRAL -> Color(0xFFF5F6F8)
    }
    
    val contentColor = when (tone) {
        StatTone.POSITIVE -> PrimaryGreen
        StatTone.NEGATIVE -> MaterialTheme.colorScheme.error
        StatTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
