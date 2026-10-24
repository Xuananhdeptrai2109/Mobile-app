package com.moneycare.app.util

import com.moneycare.app.data.model.ExpenseCategory
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.ExpenseSplit
import com.moneycare.app.data.model.GroupExpense
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.data.model.GroupSettlement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtSimplifierTest {
    private val an = GroupMember(id = "1", name = "An")
    private val binh = GroupMember(id = "2", name = "Bình")

    @Test
    fun paidSettlementReducesBalancesWithoutChangingExpenseHistory() {
        val expense = GroupExpense(
            id = "10",
            groupId = "5",
            title = "Ăn tối",
            amount = 100_000,
            category = ExpenseCategory.FOOD,
            paidBy = an,
            date = "2026-10-22",
            splitMembers = listOf(ExpenseSplit(member = binh, amount = 100_000))
        )
        val group = ExpenseGroup(
            id = "5",
            name = "Trip",
            members = listOf(an, binh),
            expenses = listOf(expense),
            settlements = listOf(
                GroupSettlement(
                    id = "20",
                    fromMemberId = binh.id,
                    fromMemberName = binh.name,
                    toMemberId = an.id,
                    toMemberName = an.name,
                    amount = 40_000,
                    settledAt = "2026-10-22T12:00:00"
                )
            )
        )

        val balances = DebtSimplifier.calculateNetBalances(group)
        val plan = DebtSimplifier.simplifyDebts(group.members, balances)

        assertEquals(60_000L, balances[an.id])
        assertEquals(-60_000L, balances[binh.id])
        assertEquals(0L, balances.values.sum())
        assertEquals(1, plan.size)
        assertEquals(60_000L, plan.single().amount)
        assertEquals(binh.id, plan.single().fromMemberId)
        assertEquals(an.id, plan.single().toMemberId)
        assertTrue(expense.splitMembers.none { it.isPaid })
    }

    @Test
    fun simplificationConservesAllMoney() {
        val chi = GroupMember(id = "3", name = "Chi")
        val balances = mapOf(an.id to 70_000L, binh.id to -20_000L, chi.id to -50_000L)

        val plan = DebtSimplifier.simplifyDebts(listOf(an, binh, chi), balances)

        assertEquals(70_000L, plan.sumOf { it.amount })
        assertTrue(plan.all { it.amount > 0 })
        assertTrue(plan.size <= 2)
    }
}
