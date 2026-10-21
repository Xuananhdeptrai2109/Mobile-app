package com.moneycare.app.data.repository

import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.data.model.personal.PersonalTransactionResponse
import com.moneycare.app.data.model.personal.SavingsGoalResponse
import com.moneycare.app.data.model.personal.SavingsGoalStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalFinanceMapperTest {
    @Test
    fun `maps API transaction to UI model`() {
        val response = PersonalTransactionResponse(
            transactionId = 42,
            title = "Ăn trưa",
            amount = 50_000,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            categoryName = "Ăn uống",
            categoryIconName = "fastfood",
            categoryColorCode = "#F97316",
            walletId = 3,
            walletName = "Ví chính",
            note = "Cơm văn phòng",
            receiptImageUrl = null,
            transactionTime = "2026-10-14T12:30:00",
            createdAt = "2026-10-14T12:31:00",
            updatedAt = "2026-10-14T12:31:00"
        )

        val transaction = response.toUiModel()

        assertEquals("42", transaction.id)
        assertEquals(50_000, transaction.amount)
        assertEquals("14/10/2026, 12:30", transaction.date)
        assertEquals("3", transaction.walletId)
        assertEquals("fastfood", transaction.categoryIconName)
    }

    @Test
    fun `keeps malformed timestamp readable`() {
        assertEquals("unknown", "unknown".toDisplayDateTime())
    }

    @Test
    fun `maps completed savings goal`() {
        val response = SavingsGoalResponse(
            goalId = 8,
            title = "Mua laptop",
            emoji = "💻",
            targetAmount = 20_000_000,
            currentAmount = 20_000_000,
            progressPercentage = 100.0,
            targetDate = "2027-01-01",
            status = SavingsGoalStatus.COMPLETED,
            createdAt = "2026-10-14T10:00:00",
            updatedAt = "2026-10-14T10:00:00"
        )

        val goal = response.toUiModel()

        assertEquals("8", goal.id)
        assertEquals(true, goal.isCompleted)
        assertEquals(1f, goal.progress)
        assertEquals("2027-01-01", goal.targetDate)
    }
}
