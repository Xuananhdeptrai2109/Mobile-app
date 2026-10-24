package com.moneycare.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "cached_dashboards",
    primaryKeys = ["userId", "month"]
)
data class DashboardEntity(
    val userId: Long,
    val month: String,
    val balance: Long,
    val income: Long,
    val expense: Long,
    val updatedAtEpochMillis: Long
)
