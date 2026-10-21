package com.moneycare.app.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Wraps a screen's content and staggers each direct child's entrance
 * animation (fade + slide up), matching the required "elements appear one
 * by one" behaviour: Back Button -> Title -> Illustration -> Fields ->
 * Button -> Social Login.
 *
 * Usage:
 * ```
 * AnimatedScreenContainer { itemIndex ->
 *     StaggerItem(itemIndex(0)) { AppTopBar(...) }
 *     StaggerItem(itemIndex(1)) { Text("Đăng nhập") }
 *     ...
 * }
 * ```
 * Simpler usage: wrap each child directly with [StaggerItem] and pass an
 * explicit, increasing `index`.
 */
@Composable
fun StaggerItem(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMillis: Int = 60,
    content: @Composable () -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * baseDelayMillis).toLong())
        visibleState.targetState = true
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = tween(350, easing = LinearOutSlowInEasing)) +
            slideInVertically(
                animationSpec = tween(350, easing = LinearOutSlowInEasing),
                initialOffsetY = { fullHeight -> fullHeight / 6 }
            )
    ) {
        content()
    }
}

/** Simple vertical container — kept separate from [StaggerItem] so screens
 * can compose their own spacing/arrangement freely. */
@Composable
fun AnimatedScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(modifier = modifier, content = content)
}
