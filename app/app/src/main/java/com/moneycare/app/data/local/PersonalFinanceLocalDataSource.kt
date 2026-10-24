package com.moneycare.app.data.local

import androidx.room.withTransaction
import com.moneycare.app.data.local.entity.DashboardEntity
import com.moneycare.app.data.local.entity.CacheMarkerEntity
import com.moneycare.app.data.local.entity.TransactionEntity
import com.moneycare.app.data.local.entity.WalletEntity
import com.moneycare.app.data.model.PersonalDashboard
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.data.model.personal.PagedResponse
import com.moneycare.app.data.model.personal.PersonalDashboardResponse
import com.moneycare.app.data.model.personal.PersonalTransactionResponse
import com.moneycare.app.data.model.personal.WalletResponse

interface PersonalFinanceCache {
    suspend fun replaceTransactionPage(
        userId: Long,
        month: String,
        page: Int,
        replaceWholeMonth: Boolean,
        items: List<PersonalTransactionResponse>
    )

    suspend fun getTransactions(
        userId: Long,
        month: String,
        type: TransactionType?,
        categoryId: Long?,
        query: String?,
        page: Int,
        size: Int
    ): PagedResponse<Transaction>?

    suspend fun getTransaction(userId: Long, id: Long): Transaction?
    suspend fun upsertTransaction(userId: Long, item: PersonalTransactionResponse)
    suspend fun deleteTransaction(userId: Long, id: Long)
    suspend fun replaceWallets(userId: Long, wallets: List<WalletResponse>)
    suspend fun getWallets(userId: Long): List<WalletResponse>?
    suspend fun upsertWallet(userId: Long, wallet: WalletResponse)
    suspend fun deleteWallet(userId: Long, id: Long)
    suspend fun cacheDashboard(userId: Long, response: PersonalDashboardResponse)
    suspend fun getDashboard(userId: Long, month: String): PersonalDashboard?
}

class PersonalFinanceLocalDataSource(
    private val database: MoneyCareDatabase
) : PersonalFinanceCache {
    private val dao = database.personalFinanceDao()

    override suspend fun replaceTransactionPage(
        userId: Long,
        month: String,
        page: Int,
        replaceWholeMonth: Boolean,
        items: List<PersonalTransactionResponse>
    ) = database.withTransaction {
        if (page == 0 && replaceWholeMonth) dao.deleteTransactionsForMonth(userId, month)
        dao.upsertTransactions(items.map { it.toEntity(userId) })
        if (page == 0 && replaceWholeMonth) {
            dao.upsertCacheMarker(CacheMarkerEntity(userId, transactionCacheKey(month), System.currentTimeMillis()))
        }
    }

    override suspend fun getTransactions(
        userId: Long,
        month: String,
        type: TransactionType?,
        categoryId: Long?,
        query: String?,
        page: Int,
        size: Int
    ): PagedResponse<Transaction>? {
        if (!dao.hasCacheMarker(userId, transactionCacheKey(month))) return null
        val normalizedQuery = query?.trim().orEmpty()
        val total = dao.countTransactions(userId, month, type?.name, categoryId, normalizedQuery)
        val content = dao.findTransactions(
            userId = userId,
            month = month,
            type = type?.name,
            categoryId = categoryId,
            query = normalizedQuery,
            limit = size,
            offset = page * size
        ).map(TransactionEntity::toModel)
        val totalPages = if (total == 0L) 0 else ((total + size - 1) / size).toInt()
        return PagedResponse(
            content = content,
            page = page,
            size = size,
            totalElements = total,
            totalPages = totalPages,
            first = page == 0,
            last = totalPages == 0 || page >= totalPages - 1
        )
    }

    override suspend fun getTransaction(userId: Long, id: Long): Transaction? =
        dao.findTransaction(userId, id)?.toModel()

    override suspend fun upsertTransaction(userId: Long, item: PersonalTransactionResponse) =
        dao.upsertTransactions(listOf(item.toEntity(userId)))

    override suspend fun deleteTransaction(userId: Long, id: Long) = dao.deleteTransaction(userId, id)

    override suspend fun replaceWallets(userId: Long, wallets: List<WalletResponse>) =
        database.withTransaction {
            dao.deleteWallets(userId)
            dao.upsertWallets(wallets.map { it.toEntity(userId) })
            dao.upsertCacheMarker(CacheMarkerEntity(userId, WALLETS_CACHE_KEY, System.currentTimeMillis()))
        }

    override suspend fun getWallets(userId: Long): List<WalletResponse>? =
        if (dao.hasCacheMarker(userId, WALLETS_CACHE_KEY)) {
            dao.findWallets(userId).map(WalletEntity::toResponse)
        } else {
            null
        }

    override suspend fun upsertWallet(userId: Long, wallet: WalletResponse) =
        database.withTransaction {
            if (wallet.isDefault) dao.clearDefaultWallet(userId)
            dao.upsertWallets(listOf(wallet.toEntity(userId)))
        }

    override suspend fun deleteWallet(userId: Long, id: Long) = dao.deleteWallet(userId, id)

    override suspend fun cacheDashboard(userId: Long, response: PersonalDashboardResponse) =
        database.withTransaction {
            dao.upsertDashboard(
                DashboardEntity(
                    userId = userId,
                    month = response.month,
                    balance = response.balance,
                    income = response.income,
                    expense = response.expense,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
            dao.upsertTransactions(response.recentTransactions.map { it.toEntity(userId) })
        }

    override suspend fun getDashboard(userId: Long, month: String): PersonalDashboard? {
        val dashboard = dao.findDashboard(userId, month) ?: return null
        return PersonalDashboard(
            month = dashboard.month,
            balance = dashboard.balance,
            income = dashboard.income,
            expense = dashboard.expense,
            recentTransactions = dao.findRecentTransactions(userId, month).map(TransactionEntity::toModel)
        )
    }

    private fun transactionCacheKey(month: String) = "transactions:$month"

    private companion object {
        const val WALLETS_CACHE_KEY = "wallets"
    }
}

private fun PersonalTransactionResponse.toEntity(userId: Long) = TransactionEntity(
    userId = userId,
    transactionId = transactionId,
    title = title,
    amount = amount,
    type = type.name,
    categoryId = categoryId,
    categoryName = categoryName,
    categoryIconName = categoryIconName,
    categoryColorCode = categoryColorCode,
    walletId = walletId,
    walletName = walletName,
    note = note,
    receiptImageUrl = receiptImageUrl,
    sourceGroupId = sourceGroupId,
    transactionTime = transactionTime,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun TransactionEntity.toModel() = Transaction(
    id = transactionId.toString(),
    title = title,
    amount = amount,
    type = TransactionType.valueOf(type),
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

private fun WalletResponse.toEntity(userId: Long) = WalletEntity(
    userId = userId,
    walletId = walletId,
    name = name,
    balance = balance,
    currency = currency,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun WalletEntity.toResponse() = WalletResponse(
    walletId = walletId,
    name = name,
    balance = balance,
    currency = currency,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun String.toDisplayDateTime(): String {
    val date = take(10).split("-")
    val time = drop(11).take(5)
    return if (date.size == 3 && time.length == 5) {
        "${date[2]}/${date[1]}/${date[0]}, $time"
    } else this
}
