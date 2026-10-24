package com.moneycare.app.di

import android.content.Context
import com.moneycare.app.data.api.RetrofitClient
import com.moneycare.app.data.local.MoneyCareDatabase
import com.moneycare.app.data.local.PersonalFinanceLocalDataSource
import com.moneycare.app.data.repository.AuthRepository
import com.moneycare.app.data.repository.HybridExpenseRepository
import com.moneycare.app.data.repository.PersonalFinanceRepository
import com.moneycare.app.data.repository.RealGroupRepository
import com.moneycare.app.util.SessionManager

/**
 * Small manual dependency container. It keeps object creation out of screens and
 * avoids adding a dependency-injection framework that the team would also have
 * to explain and maintain.
 */
class AppContainer(
    @Suppress("UNUSED_PARAMETER") context: Context,
    sessionManager: SessionManager
) {
    private val apiService = RetrofitClient.apiService
    private val database = MoneyCareDatabase.create(context)
    private val personalFinanceLocalDataSource = PersonalFinanceLocalDataSource(database)

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, sessionManager)
    }

    val personalFinanceRepository: PersonalFinanceRepository by lazy {
        PersonalFinanceRepository(
            apiService = apiService,
            localDataSource = personalFinanceLocalDataSource,
            userIdProvider = sessionManager::getUserId
        )
    }

    val groupRepository: RealGroupRepository by lazy {
        RealGroupRepository(apiService)
    }

    val expenseRepository: HybridExpenseRepository by lazy {
        HybridExpenseRepository(
            apiService = apiService,
            personalRepository = personalFinanceRepository,
            sessionManagerProvider = { sessionManager }
        )
    }
}
