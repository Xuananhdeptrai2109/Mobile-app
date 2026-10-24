package com.moneycare.app.data.repository

import com.moneycare.app.data.api.ApiService
import com.moneycare.app.data.local.PersonalFinanceCache
import com.moneycare.app.data.model.PersonalDashboard
import com.moneycare.app.data.model.Transaction
import com.moneycare.app.data.model.TransactionType
import com.moneycare.app.data.model.personal.PagedResponse
import com.moneycare.app.data.model.personal.PersonalDashboardResponse
import com.moneycare.app.data.model.personal.PersonalTransactionResponse
import com.moneycare.app.data.model.personal.WalletResponse
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalFinanceRepositoryOfflineTest {

    @Test
    fun `cached empty transaction month is returned as Empty instead of network Error`() = runBlocking {
        val cache = FakeCache(
            transactions = PagedResponse(
                content = emptyList(),
                page = 0,
                size = 20,
                totalElements = 0,
                totalPages = 0,
                first = true,
                last = true
            )
        )
        val repository = PersonalFinanceRepository(failingApi(), cache) { 7L }

        val result = repository.getTransactions(month = "2026-10")

        assertTrue(result.content.isEmpty())
        assertEquals(0L, result.totalElements)
    }

    @Test
    fun `cached empty wallet collection is returned as Empty instead of network Error`() = runBlocking {
        val repository = PersonalFinanceRepository(failingApi(), FakeCache(wallets = emptyList())) { 7L }

        assertTrue(repository.getWallets().isEmpty())
    }

    @Test
    fun `missing cache snapshot preserves the original network error`() = runBlocking {
        val repository = PersonalFinanceRepository(failingApi(), FakeCache()) { 7L }

        val error = runCatching { repository.getTransactions(month = "2026-10") }.exceptionOrNull()

        assertTrue(generateSequence(error) { it.cause }.any { it is IOException })
    }

    private fun failingApi(): ApiService = Proxy.newProxyInstance(
        ApiService::class.java.classLoader,
        arrayOf(ApiService::class.java)
    ) { _, method, _ ->
        when (method.name) {
            "toString" -> "FailingApiService"
            "hashCode" -> 0
            "equals" -> false
            else -> throw IOException("offline")
        }
    } as ApiService

    private class FakeCache(
        private val transactions: PagedResponse<Transaction>? = null,
        private val wallets: List<WalletResponse>? = null
    ) : PersonalFinanceCache {
        override suspend fun replaceTransactionPage(
            userId: Long,
            month: String,
            page: Int,
            replaceWholeMonth: Boolean,
            items: List<PersonalTransactionResponse>
        ) = Unit

        override suspend fun getTransactions(
            userId: Long,
            month: String,
            type: TransactionType?,
            categoryId: Long?,
            query: String?,
            page: Int,
            size: Int
        ) = transactions

        override suspend fun getTransaction(userId: Long, id: Long): Transaction? = null
        override suspend fun upsertTransaction(userId: Long, item: PersonalTransactionResponse) = Unit
        override suspend fun deleteTransaction(userId: Long, id: Long) = Unit
        override suspend fun replaceWallets(userId: Long, wallets: List<WalletResponse>) = Unit
        override suspend fun getWallets(userId: Long) = wallets
        override suspend fun upsertWallet(userId: Long, wallet: WalletResponse) = Unit
        override suspend fun deleteWallet(userId: Long, id: Long) = Unit
        override suspend fun cacheDashboard(userId: Long, response: PersonalDashboardResponse) = Unit
        override suspend fun getDashboard(userId: Long, month: String): PersonalDashboard? = null
    }
}
