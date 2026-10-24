package com.moneycare.app.data.local.entity

import androidx.room.Entity

/** Records that a complete remote collection was fetched, including an empty one. */
@Entity(
    tableName = "cache_markers",
    primaryKeys = ["userId", "cacheKey"]
)
data class CacheMarkerEntity(
    val userId: Long,
    val cacheKey: String,
    val updatedAtEpochMillis: Long
)
