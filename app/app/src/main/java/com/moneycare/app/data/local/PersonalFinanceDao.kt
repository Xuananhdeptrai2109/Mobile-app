package com.moneycare.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.moneycare.app.data.local.entity.DashboardEntity
import com.moneycare.app.data.local.entity.CacheMarkerEntity
import com.moneycare.app.data.local.entity.TransactionEntity
import com.moneycare.app.data.local.entity.WalletEntity

@Dao
interface PersonalFinanceDao {
    @Upsert
    suspend fun upsertCacheMarker(item: CacheMarkerEntity)

    @Query("SELECT COUNT(*) > 0 FROM cache_markers WHERE userId = :userId AND cacheKey = :cacheKey")
    suspend fun hasCacheMarker(userId: Long, cacheKey: String): Boolean

    @Upsert
    suspend fun upsertTransactions(items: List<TransactionEntity>)

    @Query("DELETE FROM cached_transactions WHERE userId = :userId AND substr(transactionTime, 1, 7) = :month")
    suspend fun deleteTransactionsForMonth(userId: Long, month: String)

    @Query(
        """
        SELECT * FROM cached_transactions
        WHERE userId = :userId
          AND substr(transactionTime, 1, 7) = :month
          AND (:type IS NULL OR type = :type)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:query = '' OR title LIKE '%' || :query || '%' COLLATE NOCASE)
        ORDER BY transactionTime DESC, createdAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun findTransactions(
        userId: Long,
        month: String,
        type: String?,
        categoryId: Long?,
        query: String,
        limit: Int,
        offset: Int
    ): List<TransactionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM cached_transactions
        WHERE userId = :userId
          AND substr(transactionTime, 1, 7) = :month
          AND (:type IS NULL OR type = :type)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:query = '' OR title LIKE '%' || :query || '%' COLLATE NOCASE)
        """
    )
    suspend fun countTransactions(
        userId: Long,
        month: String,
        type: String?,
        categoryId: Long?,
        query: String
    ): Long

    @Query("SELECT * FROM cached_transactions WHERE userId = :userId AND transactionId = :transactionId")
    suspend fun findTransaction(userId: Long, transactionId: Long): TransactionEntity?

    @Query("SELECT * FROM cached_transactions WHERE userId = :userId AND substr(transactionTime, 1, 7) = :month ORDER BY transactionTime DESC, createdAt DESC LIMIT 5")
    suspend fun findRecentTransactions(userId: Long, month: String): List<TransactionEntity>

    @Query("DELETE FROM cached_transactions WHERE userId = :userId AND transactionId = :transactionId")
    suspend fun deleteTransaction(userId: Long, transactionId: Long)

    @Upsert
    suspend fun upsertWallets(items: List<WalletEntity>)

    @Query("DELETE FROM cached_wallets WHERE userId = :userId")
    suspend fun deleteWallets(userId: Long)

    @Query("UPDATE cached_wallets SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultWallet(userId: Long)

    @Query("SELECT * FROM cached_wallets WHERE userId = :userId ORDER BY isDefault DESC, walletId ASC")
    suspend fun findWallets(userId: Long): List<WalletEntity>

    @Query("DELETE FROM cached_wallets WHERE userId = :userId AND walletId = :walletId")
    suspend fun deleteWallet(userId: Long, walletId: Long)

    @Upsert
    suspend fun upsertDashboard(item: DashboardEntity)

    @Query("SELECT * FROM cached_dashboards WHERE userId = :userId AND month = :month")
    suspend fun findDashboard(userId: Long, month: String): DashboardEntity?
}
