package com.moneycare.app.util

import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupMember

/**
 * Đại diện cho một giao dịch chuyển khoản rút gọn giữa 2 thành viên trong nhóm.
 */
data class SettlementTransaction(
    val fromMemberId: String,
    val fromMemberName: String,
    val toMemberId: String,
    val toMemberName: String,
    val amount: Long,
    val isPaid: Boolean = false
)

/**
 * Thuật toán Greedy Debt Simplification rút gọn số lượng giao dịch thanh toán
 * giữa các thành viên trong nhóm xuống mức tối thiểu (tối đa N - 1 giao dịch).
 */
object DebtSimplifier {

    /**
     * Tính toán số dư ròng (Net Balance) của từng thành viên trong nhóm.
     * Net Balance = Tổng tiền đã ứng chi trả cho người khác - Tổng tiền nợ người khác chưa trả.
     * - balance > 0: Chủ nợ (cần nhận lại tiền từ nhóm)
     * - balance < 0: Con nợ (cần trả tiền cho nhóm)
     */
    fun calculateNetBalances(group: ExpenseGroup): Map<String, Long> {
        val balances = mutableMapOf<String, Long>()
        group.members.forEach { balances[it.id] = 0L }

        for (expense in group.expenses) {
            val payerId = expense.paidBy.id
            for (split in expense.splitMembers) {
                val beneficiaryId = split.member.id
                // Chỉ tính toán các khoản chia nợ chưa thanh toán giữa 2 người khác nhau
                if (beneficiaryId != payerId && !split.isPaid) {
                    balances[beneficiaryId] = (balances[beneficiaryId] ?: 0L) - split.amount
                    balances[payerId] = (balances[payerId] ?: 0L) + split.amount
                }
            }
        }
        // A settlement is separate from the original expense splits. Paying moves
        // the debtor and creditor balances toward zero without rewriting history.
        for (settlement in group.settlements) {
            balances[settlement.fromMemberId] =
                (balances[settlement.fromMemberId] ?: 0L) + settlement.amount
            balances[settlement.toMemberId] =
                (balances[settlement.toMemberId] ?: 0L) - settlement.amount
        }
        return balances
    }

    /**
     * Áp dụng thuật toán Greedy để bù trừ công nợ tối ưu giữa danh sách con nợ và chủ nợ.
     */
    fun simplifyDebts(
        members: List<GroupMember>,
        netBalances: Map<String, Long>
    ): List<SettlementTransaction> {
        val memberMap = members.associateBy { it.id }

        // Danh sách con nợ (balance < 0) với số tiền nợ dương
        val debtors = mutableListOf<Pair<String, Long>>()
        // Danh sách chủ nợ (balance > 0) với số tiền nhận dương
        val creditors = mutableListOf<Pair<String, Long>>()

        for ((memberId, balance) in netBalances) {
            if (balance < 0L) {
                debtors.add(memberId to -balance)
            } else if (balance > 0L) {
                creditors.add(memberId to balance)
            }
        }

        // Sắp xếp giảm dần theo số tiền để bù trừ lớn nhất trước (Greedy)
        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val settlements = mutableListOf<SettlementTransaction>()
        var d = 0
        var c = 0

        while (d < debtors.size && c < creditors.size) {
            val (debtorId, debtAmount) = debtors[d]
            val (creditorId, creditAmount) = creditors[c]

            val settleAmount = minOf(debtAmount, creditAmount)
            if (settleAmount > 0L) {
                val debtorName = memberMap[debtorId]?.name ?: "Thành viên"
                val creditorName = memberMap[creditorId]?.name ?: "Thành viên"

                settlements.add(
                    SettlementTransaction(
                        fromMemberId = debtorId,
                        fromMemberName = debtorName,
                        toMemberId = creditorId,
                        toMemberName = creditorName,
                        amount = settleAmount
                    )
                )
            }

            val remainingDebt = debtAmount - settleAmount
            val remainingCredit = creditAmount - settleAmount

            if (remainingDebt == 0L) {
                d++
            } else {
                debtors[d] = debtorId to remainingDebt
            }

            if (remainingCredit == 0L) {
                c++
            } else {
                creditors[c] = creditorId to remainingCredit
            }
        }

        return settlements
    }
}
