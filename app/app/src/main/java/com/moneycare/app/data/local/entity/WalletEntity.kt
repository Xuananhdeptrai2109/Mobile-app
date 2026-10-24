package com.moneycare.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "cached_wallets",
    primaryKeys = ["userId", "walletId"]
)
data class WalletEntity(
    val userId: Long,
    val walletId: Long,
    val name: String,
    val balance: Long,
    val currency: String,
    val isDefault: Boolean,
    val createdAt: String,
    val updatedAt: String
)
