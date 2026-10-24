package com.moneycare.app.data.model.personal

import com.moneycare.app.data.model.TransactionType
import com.google.gson.annotations.SerializedName

data class PersonalCategoryResponse(
    val categoryId: Long,
    val name: String,
    val type: TransactionType,
    val iconName: String,
    val colorCode: String
)

data class WalletResponse(
    val walletId: Long,
    val name: String,
    val balance: Long,
    val currency: String,
    @SerializedName("isDefault") val isDefault: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class CreateWalletRequest(
    val name: String,
    @SerializedName("isDefault") val isDefault: Boolean = false
)

data class UpdateWalletRequest(
    val name: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean? = null
)

data class NotificationResponse(
    val notificationId: Long,
    val title: String,
    val message: String,
    @SerializedName("isRead") val isRead: Boolean,
    val createdAt: String
)

data class UnreadNotificationCountResponse(val unreadCount: Long)

data class PersonalTransactionResponse(
    val transactionId: Long,
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String,
    val categoryIconName: String,
    val categoryColorCode: String,
    val walletId: Long,
    val walletName: String,
    val note: String?,
    val receiptImageUrl: String?,
    val sourceGroupId: Long? = null,
    val transactionTime: String,
    val createdAt: String,
    val updatedAt: String
)

data class SyncGroupExpenseRequest(
    val walletId: Long,
    val categoryId: Long
)

data class PersonalTransactionRequest(
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val categoryId: Long,
    val walletId: Long,
    val note: String?,
    val receiptImageUrl: String? = null,
    val transactionTime: String
)

data class PersonalDashboardResponse(
    val month: String,
    val balance: Long,
    val income: Long,
    val expense: Long,
    val recentTransactions: List<PersonalTransactionResponse>
)

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)

data class MonthlyBudgetResponse(
    val budgetId: Long?,
    val month: String,
    val amount: Long,
    val spent: Long,
    val remaining: Long,
    val usagePercentage: Double,
    val exceeded: Boolean
)

data class MonthlyBudgetRequest(val amount: Long)

data class CategoryStatisticResponse(
    val categoryId: Long,
    val categoryName: String,
    val iconName: String,
    val colorCode: String,
    val amount: Long,
    val percentage: Double,
    val transactionCount: Long
)

data class DailyExpenseResponse(val date: String, val amount: Long)

data class PersonalStatisticsResponse(
    val month: String,
    val income: Long,
    val expense: Long,
    val net: Long,
    val savingsRate: Double,
    val previousMonthExpense: Long,
    val expenseChangePercentage: Double,
    val budget: MonthlyBudgetResponse,
    val expenseByCategory: List<CategoryStatisticResponse>,
    val dailyExpenses: List<DailyExpenseResponse>
)

enum class SavingsGoalStatus { ACTIVE, COMPLETED }
enum class SavingsOperation { DEPOSIT, WITHDRAW }

data class SavingsGoalResponse(
    val goalId: Long,
    val title: String,
    val emoji: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val progressPercentage: Double,
    val targetDate: String?,
    val status: SavingsGoalStatus,
    val createdAt: String,
    val updatedAt: String
)

data class SavingsGoalRequest(
    val title: String,
    val emoji: String?,
    val targetAmount: Long,
    val targetDate: String?
)

data class SavingsContributionRequest(
    val amount: Long,
    val operation: SavingsOperation
)
