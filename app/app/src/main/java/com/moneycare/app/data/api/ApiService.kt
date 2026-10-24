package com.moneycare.app.data.api

import com.moneycare.app.data.model.auth.AuthResponse
import com.moneycare.app.data.model.auth.LoginRequest
import com.moneycare.app.data.model.auth.RegisterRequest
import com.moneycare.app.data.model.group.*
import com.moneycare.app.data.model.personal.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // Personal finance endpoints
    @GET("categories")
    suspend fun getPersonalCategories(
        @Query("type") type: String? = null
    ): List<PersonalCategoryResponse>

    @GET("wallets")
    suspend fun getWallets(): List<WalletResponse>

    @POST("wallets")
    suspend fun createWallet(@Body request: CreateWalletRequest): WalletResponse

    @PATCH("wallets/{walletId}")
    suspend fun updateWallet(
        @Path("walletId") walletId: Long,
        @Body request: UpdateWalletRequest
    ): WalletResponse

    @DELETE("wallets/{walletId}")
    suspend fun deleteWallet(@Path("walletId") walletId: Long)

    @GET("notifications")
    suspend fun getNotifications(): List<NotificationResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadNotificationCount(): UnreadNotificationCountResponse

    @PATCH("notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: Long
    ): NotificationResponse

    @PATCH("notifications/read-all")
    suspend fun markAllNotificationsRead()

    @GET("transactions")
    suspend fun getPersonalTransactions(
        @Query("month") month: String,
        @Query("type") type: String? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("query") query: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagedResponse<PersonalTransactionResponse>

    @POST("transactions")
    suspend fun createPersonalTransaction(
        @Body request: PersonalTransactionRequest
    ): PersonalTransactionResponse

    @POST("groups/{groupId}/sync-personal")
    suspend fun syncGroupExpenseToPersonal(
        @Path("groupId") groupId: Long,
        @Body request: SyncGroupExpenseRequest
    ): PersonalTransactionResponse

    @GET("transactions/{transactionId}")
    suspend fun getPersonalTransaction(
        @Path("transactionId") transactionId: Long
    ): PersonalTransactionResponse

    @PUT("transactions/{transactionId}")
    suspend fun updatePersonalTransaction(
        @Path("transactionId") transactionId: Long,
        @Body request: PersonalTransactionRequest
    ): PersonalTransactionResponse

    @DELETE("transactions/{transactionId}")
    suspend fun deletePersonalTransaction(
        @Path("transactionId") transactionId: Long
    )

    @GET("personal/dashboard")
    suspend fun getPersonalDashboard(
        @Query("month") month: String
    ): PersonalDashboardResponse

    @GET("personal/statistics")
    suspend fun getPersonalStatistics(@Query("month") month: String): PersonalStatisticsResponse

    @PUT("personal/budgets/{month}")
    suspend fun updateMonthlyBudget(
        @Path("month") month: String,
        @Body request: MonthlyBudgetRequest
    ): MonthlyBudgetResponse

    @DELETE("personal/budgets/{month}")
    suspend fun deleteMonthlyBudget(@Path("month") month: String)

    @GET("savings-goals")
    suspend fun getSavingsGoals(
        @Query("includeCompleted") includeCompleted: Boolean = false
    ): List<SavingsGoalResponse>

    @POST("savings-goals")
    suspend fun createSavingsGoal(@Body request: SavingsGoalRequest): SavingsGoalResponse

    @PUT("savings-goals/{goalId}")
    suspend fun updateSavingsGoal(
        @Path("goalId") goalId: Long,
        @Body request: SavingsGoalRequest
    ): SavingsGoalResponse

    @POST("savings-goals/{goalId}/contributions")
    suspend fun contributeSavingsGoal(
        @Path("goalId") goalId: Long,
        @Body request: SavingsContributionRequest
    ): SavingsGoalResponse

    @DELETE("savings-goals/{goalId}")
    suspend fun deleteSavingsGoal(@Path("goalId") goalId: Long)

    // Group endpoints
    @POST("groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): GroupResponse

    @GET("groups/me")
    suspend fun getMyGroups(): List<GroupResponse>

    @GET("groups/{groupId}")
    suspend fun getGroup(@Path("groupId") groupId: Long): GroupResponse

    @PATCH("groups/{groupId}")
    suspend fun updateGroup(
        @Path("groupId") groupId: Long,
        @Body request: UpdateGroupRequest
    ): GroupResponse

    @DELETE("groups/{groupId}")
    suspend fun deleteGroup(@Path("groupId") groupId: Long)

    @GET("groups/{groupId}/members")
    suspend fun getMembers(@Path("groupId") groupId: Long): List<GroupMemberResponse>

    @POST("groups/{groupId}/members")
    suspend fun addMember(
        @Path("groupId") groupId: Long,
        @Body request: AddGroupMemberRequest
    ): GroupMemberResponse

    @PATCH("groups/{groupId}/members/{memberId}")
    suspend fun updateMember(
        @Path("groupId") groupId: Long,
        @Path("memberId") memberId: Long,
        @Body request: UpdateGroupMemberRequest
    ): GroupMemberResponse

    @DELETE("groups/{groupId}/members/{memberId}")
    suspend fun removeMember(
        @Path("groupId") groupId: Long,
        @Path("memberId") memberId: Long
    )

    // Group Expense endpoints
    @POST("groups/{groupId}/expenses")
    suspend fun createExpense(
        @Path("groupId") groupId: Long,
        @Body request: CreateGroupExpenseRequest
    ): GroupExpenseResponse

    @GET("groups/{groupId}/expenses")
    suspend fun getGroupExpenses(@Path("groupId") groupId: Long): List<GroupExpenseResponse>

    @GET("groups/{groupId}/expenses/{expenseId}")
    suspend fun getExpense(
        @Path("groupId") groupId: Long,
        @Path("expenseId") expenseId: Long
    ): GroupExpenseResponse

    @PUT("groups/{groupId}/expenses/{expenseId}")
    suspend fun updateExpense(
        @Path("groupId") groupId: Long,
        @Path("expenseId") expenseId: Long,
        @Body request: CreateGroupExpenseRequest
    ): GroupExpenseResponse

    @DELETE("groups/{groupId}/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Path("groupId") groupId: Long,
        @Path("expenseId") expenseId: Long
    )

    @PATCH("groups/{groupId}/expenses/{expenseId}/splits/{splitId}/toggle-payment")
    suspend fun toggleSplitPayment(
        @Path("groupId") groupId: Long,
        @Path("expenseId") expenseId: Long,
        @Path("splitId") splitId: Long
    ): GroupExpenseSplitResponse

    @GET("groups/{groupId}/settlements")
    suspend fun getGroupSettlements(
        @Path("groupId") groupId: Long
    ): List<GroupSettlementResponse>

    @POST("groups/{groupId}/settlements")
    suspend fun createGroupSettlement(
        @Path("groupId") groupId: Long,
        @Body request: CreateGroupSettlementRequest
    ): GroupSettlementResponse

    // Group Task endpoints
    @POST("groups/{groupId}/tasks")
    suspend fun createTask(
        @Path("groupId") groupId: Long,
        @Body request: CreateGroupTaskRequest
    ): GroupTaskResponse

    @GET("groups/{groupId}/tasks")
    suspend fun getGroupTasks(@Path("groupId") groupId: Long): List<GroupTaskResponse>

    @PUT("groups/{groupId}/tasks/{taskId}")
    suspend fun updateTask(
        @Path("groupId") groupId: Long,
        @Path("taskId") taskId: Long,
        @Body request: CreateGroupTaskRequest
    ): GroupTaskResponse

    @PATCH("groups/{groupId}/tasks/{taskId}/toggle")
    suspend fun toggleTask(
        @Path("groupId") groupId: Long,
        @Path("taskId") taskId: Long
    ): GroupTaskResponse

    @DELETE("groups/{groupId}/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("groupId") groupId: Long,
        @Path("taskId") taskId: Long
    )

    @GET("groups/{groupId}/activities")
    suspend fun getGroupActivities(
        @Path("groupId") groupId: Long
    ): List<GroupActivityResponse>

    @GET("groups/{groupId}/report")
    suspend fun getGroupReport(
        @Path("groupId") groupId: Long
    ): GroupReportResponse

    @GET("groups/{groupId}/report.csv")
    suspend fun exportGroupReport(
        @Path("groupId") groupId: Long
    ): ResponseBody

    // AI Expense Extraction endpoints
    @Multipart
    @POST("ai/expenses/extract-from-receipt")
    suspend fun extractFromReceipt(
        @Part image: MultipartBody.Part
    ): ExpenseExtractionResponse

    @Multipart
    @POST("ai/expenses/extract-from-audio")
    suspend fun extractFromAudio(
        @Part audio: MultipartBody.Part
    ): ExpenseExtractionResponse
}
