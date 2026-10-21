package com.moneycare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moneycare.app.ui.theme.MintContainer
import com.moneycare.app.ui.theme.MintPrimaryDark

/**
 * Minimal back button used at the top of auth screens. Kept as its own
 * small component (rather than a full Material TopAppBar) since the auth
 * screens need a transparent, unobtrusive back affordance, not a bar.
 */
@Composable
fun AppTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onBackClick,
        modifier = modifier
            .size(44.dp)
            .background(color = MintContainer, shape = CircleShape)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Quay lại",
            tint = MintPrimaryDark
        )
    }
}
