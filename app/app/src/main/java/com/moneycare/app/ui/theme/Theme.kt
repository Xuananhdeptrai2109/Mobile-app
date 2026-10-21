package com.moneycare.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = CardBackground,
    secondary = DarkGreen,
    background = ScreenBackground,
    surface = CardBackground,
    error = ExpenseRed
)

@Composable
fun MoneyCareTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}
