package com.moneycare.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneycare.app.ui.theme.PrimaryGreen

enum class BottomNavItem(val label: String) {
    HOME("Trang chủ"),
    TRANSACTIONS("Giao dịch"),
    STATISTICS("Thống kê"),
    PROFILE("Cá nhân")
}

/**
 * Bottom navigation bar with 4 destinations plus a raised, floating "+" add button in the middle.
 */
@Composable
fun AppBottomNavigation(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {}

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavSlot(BottomNavItem.HOME, Icons.Filled.Home, selectedItem, onItemSelected, Modifier.weight(1f))
            NavSlot(BottomNavItem.TRANSACTIONS, Icons.Filled.Receipt, selectedItem, onItemSelected, Modifier.weight(1f))
            // Spacer slot reserved for the floating add button
            Box(modifier = Modifier.weight(1f))
            NavSlot(BottomNavItem.STATISTICS, Icons.Filled.Assessment, selectedItem, onItemSelected, Modifier.weight(1f))
            NavSlot(BottomNavItem.PROFILE, Icons.Filled.Person, selectedItem, onItemSelected, Modifier.weight(1f))
        }

        AddFloatingButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
        )
    }
}

@Composable
private fun RowScope.NavSlot(
    item: BottomNavItem,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = item == selectedItem
    val tint = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onItemSelected(item) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = item.label, tint = tint)
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun AddFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "addButtonScale"
    )

    FloatingActionButton(
        onClick = {
            pressed = true
            onClick()
            pressed = false
        },
        modifier = modifier
            .size(60.dp)
            .scale(scale),
        containerColor = PrimaryGreen,
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "Thêm giao dịch", modifier = Modifier.size(28.dp))
    }
}

