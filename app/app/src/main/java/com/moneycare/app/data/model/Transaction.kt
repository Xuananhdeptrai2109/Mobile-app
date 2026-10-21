package com.moneycare.app.data.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class Transaction(
    val id: String,
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val category: String,
    val date: String,
    val categoryId: String = "",
    val categoryIconName: String = "receipt",
    val categoryColorCode: String = "#2E7D32",
    val walletId: String = "",
    val walletName: String = "",
    val note: String? = null,
    val receiptImageUrl: String? = null,
    val transactionTime: String = ""
)

data class PersonalDashboard(
    val month: String,
    val balance: Long,
    val income: Long,
    val expense: Long,
    val recentTransactions: List<Transaction>
)

