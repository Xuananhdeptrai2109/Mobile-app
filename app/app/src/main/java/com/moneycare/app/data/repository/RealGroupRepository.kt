package com.moneycare.app.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.moneycare.app.data.api.ApiService
import com.moneycare.app.data.api.RetrofitClient
import com.moneycare.app.data.model.ExpenseGroup
import com.moneycare.app.data.model.GroupExpense
import com.moneycare.app.data.model.GroupMember
import com.moneycare.app.data.model.GroupTask
import com.moneycare.app.data.model.ExpenseSplit
import com.moneycare.app.data.model.ExpenseSplitMethod
import com.moneycare.app.data.model.group.AddGroupMemberRequest
import com.moneycare.app.data.model.group.CreateGroupExpenseRequest
import com.moneycare.app.data.model.group.CreateGroupRequest
import com.moneycare.app.data.model.group.CreateGroupTaskRequest
import com.moneycare.app.data.model.group.GroupActivityResponse
import com.moneycare.app.data.model.group.GroupExpenseSplitRequest
import com.moneycare.app.data.model.group.GroupReportResponse
import com.moneycare.app.data.model.group.ExpenseExtractionResponse
import com.moneycare.app.data.model.group.UpdateGroupMemberRequest
import com.moneycare.app.data.model.group.UpdateGroupRequest
import com.moneycare.app.data.model.group.toUiModel
import com.moneycare.app.util.SessionManager
import com.moneycare.app.MoneyCareApp
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class RealGroupRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {
    private companion object {
        const val MAX_UPLOAD_BYTES = 10 * 1024 * 1024
        val IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/heic", "image/heif")
        val AUDIO_MIME_TYPES = setOf(
            "audio/mpeg", "audio/mp4", "audio/wav", "audio/x-wav",
            "audio/ogg", "application/ogg", "audio/webm", "audio/aac"
        )
    }

    private val sessionManager: SessionManager?
        get() = try { MoneyCareApp.instance.sessionManager } catch (e: Throwable) { null }

    private fun getUserId(): Long = sessionManager?.getUserId() ?: -1L

    suspend fun getGroup(groupId: String): ExpenseGroup {
        val id = groupId.toLongOrNull()
            ?: throw IllegalArgumentException("Mã nhóm không hợp lệ")
        val groupResponse = apiService.getGroup(id)
        val expensesResponse = apiService.getGroupExpenses(id)
        val tasksResponse = apiService.getGroupTasks(id)

        val currentUserId = getUserId()
        val uiGroup = groupResponse.toUiModel()
        val members = groupResponse.members ?: emptyList()
        val markedMembers = uiGroup.members.map { member ->
            val memberResponse = members.find { it.memberId.toString() == member.id }
            member.copy(isCurrentUser = memberResponse?.userId == currentUserId)
        }
        val uiExpenses = (expensesResponse ?: emptyList()).map { response ->
            val expense = response.toUiModel(members)
            expense.copy(
                paidBy = markedMembers.find { it.id == expense.paidBy.id } ?: expense.paidBy,
                splitMembers = expense.splitMembers.map { split ->
                    split.copy(member = markedMembers.find { it.id == split.member.id } ?: split.member)
                }
            )
        }
        val uiTasks = (tasksResponse ?: emptyList()).map { it.toUiModel(members) }

        return uiGroup.copy(
            expenses = uiExpenses,
            tasks = uiTasks,
            totalExpense = uiExpenses.sumOf { it.amount },
            members = markedMembers
        )
    }

    suspend fun createGroup(
        name: String,
        description: String?,
        budget: Long?,
        imageBase64: String? = null,
        memberNames: List<String> = emptyList()
    ): ExpenseGroup {
        if (sessionManager?.isLoggedIn() != true) {
            throw IllegalStateException("Bạn cần đăng nhập lại để thực hiện tác vụ này")
        }

        val request = CreateGroupRequest(
            name = name,
            description = description,
            imageBase64 = imageBase64,
            budget = budget ?: 0L,
            memberNames = memberNames
        )
        val response = apiService.createGroup(request)
        return response.toUiModel()
    }

    suspend fun updateGroup(
        groupId: String,
        name: String? = null,
        description: String? = null,
        budget: Long? = null
    ): ExpenseGroup {
        val id = requireGroupId(groupId)
        return apiService.updateGroup(id, UpdateGroupRequest(name, description, budget = budget)).toUiModel()
    }

    suspend fun deleteGroup(groupId: String) {
        apiService.deleteGroup(requireGroupId(groupId))
    }

    suspend fun addMember(groupId: String, name: String): GroupMember {
        val id = groupId.toLongOrNull() ?: throw IllegalArgumentException("Mã nhóm không hợp lệ")
        val response = apiService.addMember(id, AddGroupMemberRequest(name))
        return response.toUiModel()
    }

    suspend fun updateMember(groupId: String, memberId: String, name: String): GroupMember {
        val response = apiService.updateMember(
            requireGroupId(groupId),
            requireMemberId(memberId),
            UpdateGroupMemberRequest(name)
        )
        return response.toUiModel()
    }

    suspend fun removeMember(groupId: String, memberId: String) {
        apiService.removeMember(requireGroupId(groupId), requireMemberId(memberId))
    }

    suspend fun addExpense(
        groupId: String,
        title: String,
        amount: Long,
        paidByMemberId: Long,
        splitMemberIds: List<Long>,
        splitMethod: ExpenseSplitMethod = ExpenseSplitMethod.EQUAL,
        splitMembers: List<ExpenseSplit> = emptyList(),
        date: String,
        note: String?
    ): GroupExpense {
        val id = groupId.toLongOrNull() ?: throw IllegalArgumentException("Mã nhóm không hợp lệ")

        val response = apiService.createExpense(
            id,
            CreateGroupExpenseRequest(
                name = title,
                amount = amount,
                paidByMemberId = paidByMemberId,
                splitMemberIds = splitMemberIds,
                splitMethod = splitMethod.apiValue,
                splitDetails = splitMembers.mapNotNull { split ->
                    split.member.id.toLongOrNull()?.let { memberId ->
                        GroupExpenseSplitRequest(
                            memberId = memberId,
                            amount = split.amount.takeIf { splitMethod == ExpenseSplitMethod.CUSTOM },
                            percentage = split.percentage.takeIf { splitMethod == ExpenseSplitMethod.PERCENTAGE }
                        )
                    }
                },
                note = note,
                expenseDate = date,
                receiptImageUrl = null
            )
        )
        return response.toUiModel(emptyList())
    }

    suspend fun updateExpense(
        groupId: String,
        expenseId: String,
        title: String,
        amount: Long,
        paidByMemberId: Long,
        splitMemberIds: List<Long>,
        splitMethod: ExpenseSplitMethod = ExpenseSplitMethod.EQUAL,
        splitMembers: List<ExpenseSplit> = emptyList(),
        date: String,
        note: String?
    ): GroupExpense {
        val response = apiService.updateExpense(
            requireGroupId(groupId),
            expenseId.toLongOrNull() ?: throw IllegalArgumentException("Mã chi tiêu không hợp lệ"),
            CreateGroupExpenseRequest(
                name = title,
                amount = amount,
                paidByMemberId = paidByMemberId,
                splitMemberIds = splitMemberIds,
                splitMethod = splitMethod.apiValue,
                splitDetails = splitMembers.mapNotNull { split ->
                    split.member.id.toLongOrNull()?.let { memberId ->
                        GroupExpenseSplitRequest(
                            memberId = memberId,
                            amount = split.amount.takeIf { splitMethod == ExpenseSplitMethod.CUSTOM },
                            percentage = split.percentage.takeIf { splitMethod == ExpenseSplitMethod.PERCENTAGE }
                        )
                    }
                },
                note = note,
                expenseDate = date,
                receiptImageUrl = null
            )
        )
        return response.toUiModel(emptyList())
    }

    suspend fun deleteExpense(groupId: String, expenseId: String) {
        val expense = expenseId.toLongOrNull() ?: throw IllegalArgumentException("Mã chi tiêu không hợp lệ")
        apiService.deleteExpense(requireGroupId(groupId), expense)
    }

    suspend fun toggleSplitPayment(
        groupId: String,
        expenseId: String,
        splitId: String
    ): com.moneycare.app.data.model.group.GroupExpenseSplitResponse {
        return apiService.toggleSplitPayment(
            requireGroupId(groupId),
            expenseId.toLongOrNull() ?: throw IllegalArgumentException("Mã chi tiêu không hợp lệ"),
            splitId.toLongOrNull() ?: throw IllegalArgumentException("Mã phần chia không hợp lệ")
        )
    }

    suspend fun addTask(
        groupId: String,
        title: String,
        assigneeMemberId: Long?,
        dueDate: String?,
        description: String?
    ): GroupTask {
        val id = groupId.toLongOrNull() ?: throw IllegalArgumentException("Mã nhóm không hợp lệ")
        val response = apiService.createTask(
            id,
            CreateGroupTaskRequest(title, assigneeMemberId, normalizeDueDate(dueDate), description)
        )
        return response.toUiModel(emptyList())
    }

    suspend fun updateTask(
        groupId: String,
        taskId: String,
        title: String,
        assigneeMemberId: Long?,
        dueDate: String?,
        description: String?
    ): GroupTask {
        val gId = groupId.toLongOrNull()
        val tId = taskId.toLongOrNull()
        if (gId == null || tId == null) throw IllegalArgumentException("Mã nhóm hoặc nhiệm vụ không hợp lệ")
        val response = apiService.updateTask(
            gId,
            tId,
            CreateGroupTaskRequest(title, assigneeMemberId, normalizeDueDate(dueDate), description)
        )
        return response.toUiModel(emptyList())
    }

    suspend fun toggleTask(groupId: String, taskId: String) {
        val gId = groupId.toLongOrNull()
        val tId = taskId.toLongOrNull()
        if (gId == null || tId == null) throw IllegalArgumentException("Mã nhóm hoặc nhiệm vụ không hợp lệ")
        apiService.toggleTask(gId, tId)
    }

    suspend fun deleteTask(groupId: String, taskId: String) {
        val gId = groupId.toLongOrNull()
        val tId = taskId.toLongOrNull()
        if (gId == null || tId == null) throw IllegalArgumentException("Mã nhóm hoặc nhiệm vụ không hợp lệ")
        apiService.deleteTask(gId, tId)
    }

    suspend fun getGroupActivities(groupId: String): List<GroupActivityResponse> =
        apiService.getGroupActivities(requireGroupId(groupId))

    suspend fun getGroupReport(groupId: String): GroupReportResponse =
        apiService.getGroupReport(requireGroupId(groupId))

    suspend fun exportGroupReport(groupId: String): ByteArray =
        apiService.exportGroupReport(requireGroupId(groupId)).bytes()

    suspend fun extractFromReceipt(context: Context, uri: Uri): ExpenseExtractionResponse {
        val body = createUploadPart(context, uri, "image", IMAGE_MIME_TYPES)
        return apiService.extractFromReceipt(body)
    }

    suspend fun extractFromAudio(context: Context, uri: Uri): ExpenseExtractionResponse {
        val body = createUploadPart(context, uri, "audio", AUDIO_MIME_TYPES)
        return apiService.extractFromAudio(body)
    }

    private fun createUploadPart(
        context: Context,
        uri: Uri,
        fieldName: String,
        allowedMimeTypes: Set<String>
    ): MultipartBody.Part {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)?.lowercase()
            ?: throw IllegalArgumentException("Không xác định được định dạng file")
        if (mimeType !in allowedMimeTypes) {
            throw IllegalArgumentException("Định dạng file không được hỗ trợ")
        }

        val declaredLength = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        if (declaredLength > MAX_UPLOAD_BYTES) {
            throw IllegalArgumentException("File tải lên tối đa 10 MB")
        }

        val bytes = resolver.openInputStream(uri)?.use { input ->
            readBytesWithLimit(input, MAX_UPLOAD_BYTES)
        } ?: throw IllegalArgumentException("Không mở được file")

        val fileName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: if (fieldName == "image") "receipt" else "recording"

        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, fileName, requestFile)
    }

    private fun readBytesWithLimit(input: InputStream, maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > maxBytes) {
                throw IllegalArgumentException("File tải lên tối đa 10 MB")
            }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun normalizeDueDate(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value).toString()
        } catch (_: DateTimeParseException) {
            try {
                LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
            } catch (_: DateTimeParseException) {
                throw IllegalArgumentException("Ngày hết hạn phải có định dạng dd/MM/yyyy")
            }
        }
    }

    private fun requireGroupId(value: String): Long = value.toLongOrNull()
        ?: throw IllegalArgumentException("Mã nhóm không hợp lệ")

    private fun requireMemberId(value: String): Long = value.toLongOrNull()
        ?: throw IllegalArgumentException("Mã thành viên không hợp lệ")

}
