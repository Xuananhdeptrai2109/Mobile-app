package com.moneycare.app.data.model.group

import com.moneycare.app.data.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun GroupResponse.toUiModel(): ExpenseGroup {
    return ExpenseGroup(
        id = groupId.toString(),
        name = name ?: "",
        description = description ?: "",
        heroImageUrl = imageBase64,
        budget = budget ?: 0L,
        totalExpense = 0L,
        members = (members ?: emptyList()).map { it.toUiModel() },
        expenses = emptyList()
    )
}

fun GroupMemberResponse.toUiModel(): GroupMember {
    return GroupMember(
        id = memberId.toString(),
        name = fullName ?: "",
        role = role ?: "Thành viên",
        isCurrentUser = false,
        userId = userId
    )
}

fun GroupExpenseResponse.toUiModel(groupMembers: List<GroupMemberResponse>?): GroupExpense {
    val membersList = groupMembers ?: emptyList()
    val payer = membersList.find { it.memberId == paidByMemberId }?.toUiModel()
        ?: GroupMember(paidByMemberId.toString(), paidByFullName ?: "")

    val splitList = splits ?: emptyList()
    val parsedSplitMethod = runCatching { ExpenseSplitMethod.valueOf(splitMethod) }
        .getOrDefault(ExpenseSplitMethod.EQUAL)
    var assignedPercentage = 0.0

    return GroupExpense(
        id = expenseId.toString(),
        groupId = groupId.toString(),
        title = name ?: "",
        amount = amount,
        category = ExpenseCategory.OTHER,
        paidBy = payer,
        date = expenseDate ?: "",
        splitMethod = parsedSplitMethod,
        splitMembers = splitList.mapIndexed { index, split ->
            val percentage = if (parsedSplitMethod == ExpenseSplitMethod.PERCENTAGE && amount > 0L) {
                val value = if (index == splitList.lastIndex) {
                    100.0 - assignedPercentage
                } else {
                    kotlin.math.round((split.splitAmount * 100.0 / amount) * 100.0) / 100.0
                }
                assignedPercentage += value
                value
            } else null
            ExpenseSplit(
                member = membersList.find { it.memberId == split.memberId }?.toUiModel()
                    ?: GroupMember(split.memberId.toString(), split.fullName ?: ""),
                amount = split.splitAmount,
                percentage = percentage,
                isPaid = split.isPaid,
                id = split.splitId.toString()
            )
        },
        note = note,
        isSettled = splitList.isNotEmpty() && splitList.all { it.isPaid }
    )
}

fun GroupTaskResponse.toUiModel(groupMembers: List<GroupMemberResponse>?): GroupTask {
    val membersList = groupMembers ?: emptyList()
    val assigneeMember = membersList.find { it.memberId == assigneeMemberId }?.toUiModel()
        ?: GroupMember(assigneeMemberId?.toString() ?: "0", assigneeName ?: "Chưa phân công")

    return GroupTask(
        id = taskId.toString(),
        groupId = groupId.toString(),
        title = title ?: "",
        assignee = assigneeMember,
        dueDate = dueDate?.let { value ->
            runCatching {
                LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }.getOrDefault(value)
        } ?: "",
        description = description,
        isCompleted = isCompleted
    )
}
