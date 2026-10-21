package com.moneycare.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneycare.app.data.model.HomeMode
import com.moneycare.app.ui.theme.PrimaryGreen
import com.moneycare.app.ui.theme.SoftGray

/**
 * Segmented control used to switch between the Personal and Group home views.
 */
@Composable
fun PersonalGroupSwitcher(
    selectedMode: HomeMode,
    onModeSelected: (HomeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .background(SoftGray, RoundedCornerShape(28.dp))
            .padding(4.dp)
    ) {
        SwitcherTab(
            label = "Cá nhân",
            icon = Icons.Filled.Person,
            selected = selectedMode == HomeMode.PERSONAL,
            onClick = { onModeSelected(HomeMode.PERSONAL) },
            modifier = Modifier.weight(1f)
        )
        SwitcherTab(
            label = "Nhóm",
            icon = Icons.Filled.Group,
            selected = selectedMode == HomeMode.GROUP,
            onClick = { onModeSelected(HomeMode.GROUP) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SwitcherTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) PrimaryGreen else Color.Transparent,
        label = "tabBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabContent"
    )

    Box(
        modifier = modifier
            .clickable(
                interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .background(backgroundColor, RoundedCornerShape(24.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
            )
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}


