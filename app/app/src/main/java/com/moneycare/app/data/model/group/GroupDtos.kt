package com.moneycare.app.data.model.group

data class CreateGroupRequest(
    val name: String,
    val description: String?,
    val imageBase64: String? = null,
    val budget: Long,
    val memberNames: List<String> = emptyList()
)

data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val imageBase64: String? = null,
    val budget: Long? = null
)

data class GroupResponse(
    val groupId: Long,
    val name: String,
    val description: String?,
    val imageBase64: String? = null,
    val budget: Long?,
    val createdByUserId: Long,
    val createdByFullName: String,
    val members: List<GroupMemberResponse>,
    val createdAt: String,
    val updatedAt: String,
    val message: String,
    val success: Boolean
)

data class GroupMemberResponse(
    val memberId: Long,
    val userId: Long?,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?,
    val role: String,
    val joinedAt: String
)

data class AddGroupMemberRequest(
    val name: String
)

data class UpdateGroupMemberRequest(val name: String)

data class CreateGroupExpenseRequest(
    val name: String,
    val amount: Long,
    val paidByMemberId: Long,
    val splitMemberIds: List<Long>,
    val splitMethod: String = "EQUAL",
    val splitDetails: List<GroupExpenseSplitRequest> = emptyList(),
    val note: String?,
    val expenseDate: String,
    val receiptImageUrl: String?
)

data class GroupExpenseSplitRequest(
    val memberId: Long,
    val amount: Long? = null,
    val percentage: Double? = null
)

data class CreateGroupSettlementRequest(
    val fromMemberId: Long,
    val toMemberId: Long,
    val amount: Long
)

data class GroupSettlementResponse(
    val settlementId: Long,
    val groupId: Long,
    val fromMemberId: Long,
    val fromMemberName: String,
    val toMemberId: Long,
    val toMemberName: String,
    val amount: Long,
    val settledAt: String
)

data class GroupExpenseResponse(
    val expenseId: Long,
    val groupId: Long,
    val name: String,
    val amount: Long,
    val paidByMemberId: Long,
    val paidByFullName: String,
    val splitMethod: String = "EQUAL",
    val note: String?,
    val expenseDate: String,
    val receiptImageUrl: String?,
    val splits: List<GroupExpenseSplitResponse>,
    val createdAt: String,
    val updatedAt: String,
    val message: String,
    val success: Boolean
)

data class GroupExpenseSplitResponse(
    val splitId: Long,
    val memberId: Long,
    val fullName: String,
    val splitAmount: Long,
    val isPaid: Boolean
)

data class CreateGroupTaskRequest(
    val title: String,
    val assigneeMemberId: Long?,
    val dueDate: String?,
    val description: String?
)

data class GroupTaskResponse(
    val taskId: Long,
    val groupId: Long,
    val title: String,
    val assigneeMemberId: Long?,
    val assigneeName: String?,
    val dueDate: String?,
    val description: String?,
    val isCompleted: Boolean,
    val message: String,
    val success: Boolean
)

data class ExpenseExtractionResponse(
    val name: String?,
    val amount: Long?,
    val note: String?,
    val expenseDate: String?,
    val merchantName: String?,
    val rawText: String?,
    val message: String?,
    val success: Boolean
)

data class GroupActivityResponse(
    val activityId: Long,
    val groupId: Long,
    val actorName: String,
    val type: String,
    val title: String,
    val description: String?,
    val relatedEntityType: String?,
    val relatedEntityId: Long?,
    val createdAt: String
)

data class GroupMemberReportResponse(
    val memberId: Long,
    val fullName: String,
    val totalPaid: Long,
    val totalOwed: Long,
    val settledAmount: Long,
    val outstandingAmount: Long,
    val netBalance: Long
)

data class GroupReportResponse(
    val groupId: Long,
    val groupName: String,
    val budget: Long,
    val totalExpense: Long,
    val settledAmount: Long,
    val outstandingAmount: Long,
    val expenseCount: Int,
    val taskCount: Int,
    val completedTaskCount: Int,
    val members: List<GroupMemberReportResponse>
)
