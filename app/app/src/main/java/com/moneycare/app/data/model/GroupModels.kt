package com.moneycare.app.data.model

/**
 * Root model representing a spending / trip group (e.g. "Đà Nẵng Trip").
 */
data class ExpenseGroup(
    val id: String,
    val name: String,
    val description: String = "",
    val emoji: String = "🏝️",
    val heroImageUrl: String? = null,
    val budget: Long = 0,
    val totalExpense: Long = 0,
    val members: List<GroupMember> = emptyList(),
    val expenses: List<GroupExpense> = emptyList(),
    val tasks: List<GroupTask> = emptyList()
) {
    val memberCount: Int get() = members.size
    val progress: Float get() = if (budget > 0) totalExpense.toFloat() / budget else 0f
}

data class GroupMember(
    val id: String,
    val name: String,
    val role: String = "Thành viên",
    val avatarUrl: String? = null,
    val isCurrentUser: Boolean = false,
    val userId: Long? = null
)

data class ExpenseSplit(
    val member: GroupMember,
    val amount: Long,
    val percentage: Double? = null,
    val isPaid: Boolean = false,
    val id: String = ""
)

enum class ExpenseSplitMethod(val apiValue: String, val label: String) {
    EQUAL("EQUAL", "Chia đều"),
    PERCENTAGE("PERCENTAGE", "Theo tỷ lệ"),
    CUSTOM("CUSTOM", "Tùy chỉnh")
}

data class GroupExpense(
    val id: String,
    val groupId: String,
    val title: String,
    val amount: Long,
    val category: ExpenseCategory,
    val paidBy: GroupMember,
    val date: String,
    val time: String = "",
    val splitMethod: ExpenseSplitMethod = ExpenseSplitMethod.EQUAL,
    val splitMembers: List<ExpenseSplit> = emptyList(),
    val note: String? = null,
    val linkedTaskId: String? = null,
    val isSettled: Boolean = false
) {
    val splitMemberCount: Int get() = splitMembers.size
}

enum class ExpenseCategory(val label: String, val emoji: String) {
    FOOD("Ăn uống", "🍜"),
    TRANSPORT("Di chuyển", "🚕"),
    HOTEL("Khách sạn", "🏨"),
    TICKET("Vé tham quan", "🏝️"),
    BREAKFAST("Bữa sáng", "🍳"),
    FLIGHT("Máy bay", "✈️"),
    OTHER("Khác", "🧾")
}

data class GroupTask(
    val id: String,
    val groupId: String,
    val title: String,
    val assignee: GroupMember,
    val assigneeAvatarUrl: String? = null,
    val dueDate: String,
    val description: String? = null,
    val isCompleted: Boolean,
    val linkedExpenseId: String? = null
)

data class MemberStats(
    val totalPaid: Long,
    val totalOwed: Long,
    val balance: Long
)
