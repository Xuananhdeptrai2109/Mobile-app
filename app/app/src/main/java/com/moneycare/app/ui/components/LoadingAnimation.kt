package com.moneycare.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneycare.app.ui.theme.PrimaryGreen

/**
 * Placeholder loading / empty-state component.
 *
 * Swap the CircularProgressIndicator + emoji below for a real
 * `com.airbnb.lottie.compose.LottieAnimation` once you have .json assets
 * (e.g. res/raw/loading_dino.json, res/raw/empty_transactions.json).
 * Keeping the same call signature means screens don't need to change later:
 *
 * LottieAnimation(composition, progress = { progress }, modifier = ...)
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    label: String = "Đang tải dữ liệu..."
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = PrimaryGreen)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Friendly empty state, e.g. for a brand-new account with no transactions yet. */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "🦖", fontSize = 48.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

