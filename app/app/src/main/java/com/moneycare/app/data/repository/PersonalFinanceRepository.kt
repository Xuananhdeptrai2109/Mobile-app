package com.moneycare.app.data.repository

import com.moneycare.app.data.api.ApiService
import com.moneycare.app.data.api.RetrofitClient
import com.moneycare.app.data.local.PersonalFinanceCache
import com.moneycare.app.data.model.PersonalDashboard
import com.moneycare.app.data.model.SavingsGoal
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.data.model.personal.*

class PersonalFinanceRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val localDataSource: PersonalFinanceCache? = null,
    private val userIdProvider: () -> Long = {
        runCatching { com.moneycare.app.MoneyCareApp.instance.sessionManager.getUserId() }.getOrDefault(-1L)
    }
) {
    suspend fun getDashboard(month: String): PersonalDashboard {
        val userId = userIdProvider()
        return try {
            apiService.getPersonalDashboard(month).let { response ->
                if (userId > 0) localDataSource?.cacheDashboard(userId, response)
                PersonalDashboard(
                    month = response.month,
                    balance = response.balance,
                    income = response.income,
                    expense = response.expense,
                    recentTransactions = response.recentTransactions.map { it.toUiModel() }
                )
            }
        } catch (remoteError: Exception) {
            localDataSource?.takeIf { userId > 0 }?.getDashboard(userId, month)
                ?: throw remoteError
        }
    }

    suspend fun getTransactions(
        month: String,
        type: TransactionType? = null,
        categoryId: Long? = null,
        query: String? = null,
        page: Int = 0,
        size: Int = 20
    ): PagedResponse<Transaction> {
        val userId = userIdProvider()
        return try {
            val response = apiService.getPersonalTransactions(
                month = month,
                type = type?.name,
                categoryId = categoryId,
                query = query?.trim()?.takeIf { it.isNotEmpty() },
                page = page,
                size = size
            )
            if (userId > 0) {
                localDataSource?.replaceTransactionPage(
                    userId = userId,
                    month = month,
                    page = page,
                    replaceWholeMonth = type == null && categoryId == null && query.isNullOrBlank(),
                    items = response.content
                )
            }
            PagedResponse(
                content = response.content.map { it.toUiModel() },
                page = response.page,
                size = response.size,
                totalElements = response.totalElements,
                totalPages = response.totalPages,
                first = response.first,
                last = response.last
            )
        } catch (remoteError: Exception) {
            val cached = localDataSource?.takeIf { userId > 0 }?.getTransactions(
                userId, month, type, categoryId, query, page, size
            )
            cached ?: throw remoteError
        }
    }

    suspend fun getTransaction(id: Long): Transaction {
        val userId = userIdProvider()
        return try {
            val response = apiService.getPersonalTransaction(id)
            if (userId > 0) localDataSource?.upsertTransaction(userId, response)
            response.toUiModel()
        } catch (remoteError: Exception) {
            localDataSource?.takeIf { userId > 0 }?.getTransaction(userId, id)
                ?: throw remoteError
        }
    }

    suspend fun createTransaction(request: PersonalTransactionRequest): Transaction {
        val response = apiService.createPersonalTransaction(request)
        userIdProvider().takeIf { it > 0 }?.let { localDataSource?.upsertTransaction(it, response) }
        return response.toUiModel()
    }

    suspend fun syncGroupToWallet(
        groupId: Long,
        walletId: Long,
        categoryId: Long
    ): Transaction {
        val response = apiService.syncGroupExpenseToPersonal(
            groupId,
            SyncGroupExpenseRequest(walletId = walletId, categoryId = categoryId)
        )
        userIdProvider().takeIf { it > 0 }?.let { localDataSource?.upsertTransaction(it, response) }
        return response.toUiModel()
    }

    suspend fun updateTransaction(id: Long, request: PersonalTransactionRequest): Transaction {
        val response = apiService.updatePersonalTransaction(id, request)
        userIdProvider().takeIf { it > 0 }?.let { localDataSource?.upsertTransaction(it, response) }
        return response.toUiModel()
    }

    suspend fun deleteTransaction(id: Long) {
        apiService.deletePersonalTransaction(id)
        userIdProvider().takeIf { it > 0 }?.let { localDataSource?.deleteTransaction(it, id) }
    }

    suspend fun getCategories(type: TransactionType? = null): List<PersonalCategoryResponse> =
        apiService.getPersonalCategories(type?.name)

    suspend fun getWallets(): List<WalletResponse> {
        val userId = userIdProvider()
        return try {
            apiService.getWallets().also { wallets ->
                if (userId > 0) localDataSource?.replaceWallets(userId, wallets)
            }
        } catch (remoteError: Exception) {
            val cached = localDataSource?.takeIf { userId > 0 }?.getWallets(userId)
            cached ?: throw remoteError
        }
    }

    suspend fun createWallet(name: String, makeDefault: Boolean): WalletResponse {
        val response = apiService.createWallet(CreateWalletRequest(name.trim(), makeDefault))
        userIdProvider().takeIf { it > 0 }?.let { localDataSource?.upsertWallet(it, response) }
        return response
    }

    suspend fun updateWallet(id: Long, name: String?, makeDefault: Boolean?): WalletResponse {
        val response = apiService.updateWallet(
            id,
            UpdateWalletRequest(
                name = name?.trim(),
                isDefault = makeDefault
            )
        )
        userIdProvider().takeIf { it > 0 }?.let { localDataSource?.upsertWallet(it, response) }
        return response
    }

    suspend fun deleteWallet(id: Long) {
        apiService.deleteWallet(id)
        userIdProvider().takeIf { it > 0 }?.let { localDataSource?.deleteWallet(it, id) }
    }

    suspend fun getNotifications(): List<NotificationResponse> = apiService.getNotifications()

    suspend fun getUnreadNotificationCount(): Long =
        apiService.getUnreadNotificationCount().unreadCount

    suspend fun markNotificationRead(id: Long): NotificationResponse =
        apiService.markNotificationRead(id)

    suspend fun markAllNotificationsRead() = apiService.markAllNotificationsRead()

    suspend fun getStatistics(month: String): PersonalStatisticsResponse =
        apiService.getPersonalStatistics(month)

    suspend fun updateBudget(month: String, amount: Long): MonthlyBudgetResponse =
        apiService.updateMonthlyBudget(month, MonthlyBudgetRequest(amount))

    suspend fun deleteBudget(month: String) = apiService.deleteMonthlyBudget(month)

    suspend fun getSavingsGoals(includeCompleted: Boolean = false): List<SavingsGoal> =
        apiService.getSavingsGoals(includeCompleted).map { it.toUiModel() }

    suspend fun createSavingsGoal(request: SavingsGoalRequest): SavingsGoal =
        apiService.createSavingsGoal(request).toUiModel()

    suspend fun updateSavingsGoal(id: Long, request: SavingsGoalRequest): SavingsGoal =
        apiService.updateSavingsGoal(id, request).toUiModel()

    suspend fun contributeSavingsGoal(
        id: Long,
        amount: Long,
        operation: SavingsOperation
    ): SavingsGoal = apiService.contributeSavingsGoal(
        id,
        SavingsContributionRequest(amount, operation)
    ).toUiModel()

    suspend fun deleteSavingsGoal(id: Long) = apiService.deleteSavingsGoal(id)
}

internal fun SavingsGoalResponse.toUiModel(): SavingsGoal = SavingsGoal(
    id = goalId.toString(),
    title = title,
    currentAmount = currentAmount,
    targetAmount = targetAmount,
    emoji = emoji,
    targetDate = targetDate,
    isCompleted = status == SavingsGoalStatus.COMPLETED
)

internal fun PersonalTransactionResponse.toUiModel(): Transaction = Transaction(
    id = transactionId.toString(),
    title = title,
    amount = amount,
    type = type,
    category = categoryName,
    date = transactionTime.toDisplayDateTime(),
    categoryId = categoryId.toString(),
    categoryIconName = categoryIconName,
    categoryColorCode = categoryColorCode,
    walletId = walletId.toString(),
    walletName = walletName,
    note = note,
    receiptImageUrl = receiptImageUrl,
    transactionTime = transactionTime
)

internal fun String.toDisplayDateTime(): String {
    val date = take(10).split("-")
    val time = drop(11).take(5)
    return if (date.size == 3 && time.length == 5) {
        "${date[2]}/${date[1]}/${date[0]}, $time"
    } else this
}
