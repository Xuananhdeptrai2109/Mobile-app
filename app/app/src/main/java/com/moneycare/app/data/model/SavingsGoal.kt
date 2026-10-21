package com.moneycare.app.data.model

data class SavingsGoal(
    val id: String,
    val title: String,
    val currentAmount: Long,
    val targetAmount: Long,
    val emoji: String = "🎯",
    val targetDate: String? = null,
    val isCompleted: Boolean = false
) {
    val progress: Float
        get() = if (targetAmount == 0L) 0f
        else (currentAmount.toFloat() / targetAmount).coerceIn(0f, 1f)
}
