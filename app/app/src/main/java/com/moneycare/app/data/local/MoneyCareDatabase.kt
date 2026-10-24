package com.moneycare.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moneycare.app.data.local.entity.DashboardEntity
import com.moneycare.app.data.local.entity.CacheMarkerEntity
import com.moneycare.app.data.local.entity.TransactionEntity
import com.moneycare.app.data.local.entity.WalletEntity

@Database(
    entities = [
        TransactionEntity::class,
        WalletEntity::class,
        DashboardEntity::class,
        CacheMarkerEntity::class
    ],
    version = 2,
    // This database is a disposable network cache; schema upgrades may rebuild it.
    exportSchema = false
)
abstract class MoneyCareDatabase : RoomDatabase() {
    abstract fun personalFinanceDao(): PersonalFinanceDao

    companion object {
        fun create(context: Context): MoneyCareDatabase = Room.databaseBuilder(
            context.applicationContext,
            MoneyCareDatabase::class.java,
            "moneycare-cache.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }
}
