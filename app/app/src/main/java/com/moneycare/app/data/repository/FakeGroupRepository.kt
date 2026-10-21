package com.moneycare.app.data.repository

import com.moneycare.app.data.model.ExpenseCategory
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.ExpenseSplit
import com.moneycare.app.data.model.GroupExpense
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.data.model.GroupTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Mock/fake data source standing in for a real backend + Room/Retrofit setup.
 */
class FakeGroupRepository {

    private val anh = GroupMember(id = "u1", name = "Anh", isCurrentUser = true, role = "Quản trị viên")
    private val minh = GroupMember(id = "u2", name = "Minh")
    private val lan = GroupMember(id = "u3", name = "Lan")
    private val hung = GroupMember(id = "u4", name = "Hưng")
    private val trang = GroupMember(id = "u5", name = "Trang")

    private val _members = MutableStateFlow(listOf(anh, minh, lan, hung, trang))
    val membersState: StateFlow<List<GroupMember>> = _members.asStateFlow()

    fun addMember(member: GroupMember) {
        _members.value = _members.value + member
    }

    private val _expenses = MutableStateFlow(
        listOf(
            GroupExpense(
                id = "e1",
                groupId = "g1",
                title = "Đặt vé máy bay",
                amount = 4_000_000L,
                category = ExpenseCategory.FLIGHT,
                paidBy = anh,
                date = "16/08/2026",
                time = "09:10",
                splitMembers = listOf(
                    ExpenseSplit(anh, 1_000_000L, isPaid = true),
                    ExpenseSplit(minh, 1_000_000L, isPaid = false),
                    ExpenseSplit(lan, 1_000_000L, isPaid = false),
                    ExpenseSplit(hung, 1_000_000L, isPaid = false)
                ),
                note = "Vé khứ hồi Hà Nội → Đà Nẵng",
                linkedTaskId = "t1"
            ),
            GroupExpense(
                id = "e2",
                groupId = "g1",
                title = "Đặt khách sạn 3 đêm",
                amount = 2_000_000L,
                category = ExpenseCategory.HOTEL,
                paidBy = minh,
                date = "16/08/2026",
                time = "14:20",
                splitMembers = listOf(
                    ExpenseSplit(anh, 500_000L, isPaid = false),
                    ExpenseSplit(minh, 500_000L, isPaid = true),
                    ExpenseSplit(lan, 500_000L, isPaid = false),
                    ExpenseSplit(hung, 500_000L, isPaid = false)
                ),
                note = null,
                linkedTaskId = "t2"
            ),
            GroupExpense(
                id = "e3",
                groupId = "g1",
                title = "Ăn tối hải sản",
                amount = 800_000L,
                category = ExpenseCategory.FOOD,
                paidBy = lan,
                date = "Hôm nay",
                time = "19:30",
                splitMembers = listOf(
                    ExpenseSplit(anh, 200_000L, isPaid = false),
                    ExpenseSplit(minh, 200_000L, isPaid = false),
                    ExpenseSplit(lan, 200_000L, isPaid = true),
                    ExpenseSplit(hung, 200_000L, isPaid = false)
                )
            ),
            GroupExpense(
                id = "e4",
                groupId = "g1",
                title = "Thuê xe máy",
                amount = 450_000L,
                category = ExpenseCategory.TRANSPORT,
                paidBy = hung,
                date = "Hôm nay",
                time = "08:00",
                splitMembers = listOf(
                    ExpenseSplit(anh, 150_000L, isPaid = false),
                    ExpenseSplit(minh, 150_000L, isPaid = false),
                    ExpenseSplit(hung, 150_000L, isPaid = true)
                )
            ),
            GroupExpense(
                id = "e5",
                groupId = "g1",
                title = "Mua vé Bà Nà Hills",
                amount = 3_500_000L,
                category = ExpenseCategory.TICKET,
                paidBy = anh,
                date = "Hôm qua",
                time = "09:00",
                splitMembers = listOf(
                    ExpenseSplit(anh, 875_000L, isPaid = true),
                    ExpenseSplit(minh, 875_000L, isPaid = false),
                    ExpenseSplit(lan, 875_000L, isPaid = false),
                    ExpenseSplit(hung, 875_000L, isPaid = false)
                )
            ),
            GroupExpense(
                id = "e6",
                groupId = "g1",
                title = "Ăn sáng mì Quảng",
                amount = 200_000L,
                category = ExpenseCategory.BREAKFAST,
                paidBy = minh,
                date = "17/08/2026",
                time = "07:30",
                splitMembers = listOf(
                    ExpenseSplit(minh, 50_000L, isPaid = true),
                    ExpenseSplit(anh, 50_000L, isPaid = false),
                    ExpenseSplit(lan, 50_000L, isPaid = false),
                    ExpenseSplit(hung, 50_000L, isPaid = false)
                )
            ),
            GroupExpense(
                id = "e7",
                groupId = "g1",
                title = "Mua quà lưu niệm",
                amount = 600_000L,
                category = ExpenseCategory.OTHER,
                paidBy = trang,
                date = "15/08/2026",
                time = "16:00",
                splitMembers = listOf(
                    ExpenseSplit(trang, 600_000L, isPaid = true)
                )
            )
        )
    )
    val expenses: StateFlow<List<GroupExpense>> = _expenses.asStateFlow()

    private val _tasks = MutableStateFlow(
        listOf(
            GroupTask(
                id = "t1",
                groupId = "g1",
                title = "Đặt vé máy bay",
                assignee = anh,
                dueDate = "15/08/2026",
                description = "Đặt vé khứ hồi Hà Nội - Đà Nẵng",
                isCompleted = true,
                linkedExpenseId = "e1"
            ),
            GroupTask(
                id = "t2",
                groupId = "g1",
                title = "Đặt khách sạn",
                assignee = minh,
                dueDate = "16/08/2026",
                description = "Đặt khách sạn 3 đêm tại Đà Nẵng",
                isCompleted = true,
                linkedExpenseId = "e2"
            ),
            GroupTask(
                id = "t3",
                groupId = "g1",
                title = "Lên lịch trình",
                assignee = lan,
                dueDate = "18/08/2026",
                description = "Lên lịch trình chi tiết cho 3 ngày 2 đêm tại Đà Nẵng",
                isCompleted = false,
                linkedExpenseId = null
            ),
            GroupTask(
                id = "t4",
                groupId = "g1",
                title = "Mua bảo hiểm du lịch",
                assignee = hung,
                dueDate = "14/08/2026",
                description = "Đảm bảo mọi người đều có bảo hiểm",
                isCompleted = true,
                linkedExpenseId = null
            ),
            GroupTask(
                id = "t5",
                groupId = "g1",
                title = "Chuẩn bị đồ dùng cá nhân",
                assignee = trang,
                dueDate = "20/08/2026",
                description = "Nhắc nhở mọi người mang theo kem chống nắng",
                isCompleted = false,
                linkedExpenseId = null
            )
        )
    )
    val tasks: StateFlow<List<GroupTask>> = _tasks.asStateFlow()

    suspend fun getGroup(groupId: String): ExpenseGroup {
        delay(150)
        return ExpenseGroup(
            id = groupId,
            name = "Đà Nẵng Trip",
            budget = 15_000_000,
            totalExpense = _expenses.value.sumOf { it.amount },
            members = _members.value,
            expenses = _expenses.value,
            tasks = _tasks.value
        )
    }

    fun getExpenseById(id: String): GroupExpense? = _expenses.value.find { it.id == id }

    fun getMemberById(id: String): GroupMember? = _members.value.find { it.id == id }

    fun getTaskById(id: String): GroupTask? = _tasks.value.find { it.id == id }

    fun addExpense(expense: GroupExpense) {
        _expenses.value = _expenses.value + expense
    }

    fun updateExpense(expense: GroupExpense) {
        _expenses.value = _expenses.value.map { if (it.id == expense.id) expense else it }
    }

    fun deleteExpense(expenseId: String) {
        _expenses.value = _expenses.value.filterNot { it.id == expenseId }
    }

    fun addTask(task: GroupTask) {
        _tasks.value = _tasks.value + task
    }

    fun updateTask(task: GroupTask) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filterNot { it.id == taskId }
    }

    fun toggleTask(taskId: String) {
        _tasks.value = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun expensesForMember(memberId: String): List<GroupExpense> =
        _expenses.value.filter { expense ->
            expense.paidBy.id == memberId || expense.splitMembers.any { it.member.id == memberId }
        }
}
