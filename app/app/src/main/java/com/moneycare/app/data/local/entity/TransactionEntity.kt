package com.moneycare.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "cached_transactions",
    primaryKeys = ["userId", "transactionId"]
)
data class TransactionEntity(
    val userId: Long,
    val transactionId: Long,
    val title: String,
    val amount: Long,
    val type: String,
    val categoryId: Long,
    val categoryName: String,
    val categoryIconName: String,
    val categoryColorCode: String,
    val walletId: Long,
    val walletName: String,
    val note: String?,
    val receiptImageUrl: String?,
    val sourceGroupId: Long?,
    val transactionTime: String,
    val createdAt: String,
    val updatedAt: String
)
