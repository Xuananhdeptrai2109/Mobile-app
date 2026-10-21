package com.moneycare.app.data.model.group

import com.moneycare.app.data.model.ExpenseSplitMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupMappersTest {

    @Test
    fun groupResponsePreservesDescriptionAndLinkedUser() {
        val response = GroupResponse(
            groupId = 10,
            name = "Đà Nẵng",
            description = "Chuyến đi cuối tuần",
            budget = 2_000_000,
            createdByUserId = 1,
            createdByFullName = "An",
            members = listOf(
                GroupMemberResponse(
                    memberId = 20,
                    userId = 1,
                    fullName = "An",
                    email = "an@example.com",
                    phoneNumber = null,
                    role = "OWNER",
                    joinedAt = "2026-10-07T00:00:00"
                )
            ),
            createdAt = "2026-10-07T00:00:00",
            updatedAt = "2026-10-07T00:00:00",
            message = "ok",
            success = true
        )

        val model = response.toUiModel()

        assertEquals("Chuyến đi cuối tuần", model.description)
        assertEquals(1L, model.members.single().userId)
        assertEquals("OWNER", model.members.single().role)
        assertFalse(model.members.single().isCurrentUser)
    }

    @Test
    fun expenseResponsePreservesSplitIdentityAndPaymentStatus() {
        val response = GroupExpenseResponse(
            expenseId = 30,
            groupId = 10,
            name = "Ăn tối",
            amount = 100_000,
            paidByMemberId = 1,
            paidByFullName = "An",
            note = null,
            expenseDate = "2026-10-14",
            receiptImageUrl = null,
            splits = listOf(
                GroupExpenseSplitResponse(
                    splitId = 40,
                    memberId = 2,
                    fullName = "Lan",
                    splitAmount = 100_000,
                    isPaid = true
                )
            ),
            createdAt = "2026-10-14T00:00:00",
            updatedAt = "2026-10-14T00:00:00",
            message = "ok",
            success = true
        )

        val model = response.toUiModel(emptyList())

        assertEquals("40", model.splitMembers.single().id)
        assertTrue(model.splitMembers.single().isPaid)
        assertTrue(model.isSettled)
    }

    @Test
    fun percentageExpenseProducesTwoDecimalPercentagesThatTotalOneHundred() {
        val response = GroupExpenseResponse(
            expenseId = 31,
            groupId = 10,
            name = "Taxi",
            amount = 101,
            paidByMemberId = 1,
            paidByFullName = "An",
            splitMethod = "PERCENTAGE",
            note = null,
            expenseDate = "2026-10-18",
            receiptImageUrl = null,
            splits = listOf(
                GroupExpenseSplitResponse(41, 1, "An", 33, false),
                GroupExpenseSplitResponse(42, 2, "Binh", 33, false),
                GroupExpenseSplitResponse(43, 3, "Chi", 35, false)
            ),
            createdAt = "2026-10-18T00:00:00",
            updatedAt = "2026-10-18T00:00:00",
            message = "ok",
            success = true
        )

        val model = response.toUiModel(emptyList())

        assertEquals(ExpenseSplitMethod.PERCENTAGE, model.splitMethod)
        assertEquals(100.0, model.splitMembers.sumOf { it.percentage ?: 0.0 }, 0.0001)
        assertTrue(model.splitMembers.all { percentage ->
            val text = percentage.percentage.toString().substringAfter('.', "")
            text.length <= 2
        })
    }
}
