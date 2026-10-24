package com.moneycare.app.ui.group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.moneycare.app.ui.components.ErrorRetryContent
import com.moneycare.app.ui.components.GroupDetailTopBar
import com.moneycare.app.ui.theme.ScreenBackground

/**
 * Visible loading/error shell for child routes that depend on a group.
 * Keeping this outside each form prevents a restored route from becoming a blank screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupRouteStateScreen(
    title: String,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = ScreenBackground,
        topBar = {
            GroupDetailTopBar(
                title = title,
                onBackClick = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (errorMessage == null) {
                CircularProgressIndicator()
            } else {
                ErrorRetryContent(message = errorMessage, onRetry = onRetry)
            }
        }
    }
}
