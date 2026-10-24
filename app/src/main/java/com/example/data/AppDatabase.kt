package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.example.data.dao.AccountDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.TrackingPeriodDao
import com.example.data.dao.TransactionDao
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.NotificationItem
import com.example.data.model.TrackingPeriod
import com.example.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Account::class,
        Transaction::class,
        Category::class,
        TrackingPeriod::class,
        NotificationItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun trackingPeriodDao(): TrackingPeriodDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `notifications` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `transactionId` INTEGER,
                        `type` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `isRead` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pockitify_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val accountDao = database.accountDao()
            val categoryDao = database.categoryDao()
            val trackingPeriodDao = database.trackingPeriodDao()

            // 1. Default Accounts (insert if not already present)
            val defaultAccounts = listOf(
                Account(
                    id = "abl",
                    name = "Allied Bank",
                    fullName = "Allied Bank Limited",
                    accountNumberMasked = "•••• •••• 4829",
                    balancePaisa = 0L,
                    accountType = "BANK",
                    colorHex = "#0C2B4E"
                ),
                Account(
                    id = "ubl",
                    name = "UBL",
                    fullName = "United Bank Limited",
                    accountNumberMasked = "•••• •••• 7153",
                    balancePaisa = 0L,
                    accountType = "BANK",
                    colorHex = "#0A4F9E"
                ),
                Account(
                    id = "wallet",
                    name = "Cash Wallet",
                    fullName = "Purse",
                    accountNumberMasked = "Cash in Hand",
                    balancePaisa = 0L,
                    accountType = "WALLET",
                    colorHex = "#064E3B"
                )
            )
            for (acc in defaultAccounts) {
                if (accountDao.getAccountById(acc.id) == null) {
                    accountDao.insertAccount(acc)
                }
            }

            // 2. Updated Default Categories (Credit: Salary, Pocket Money, Other; Expense: Utility Bills, Mobile Package, Grocery, Other)
            val updatedDefaultCategories = listOf(
                // Credit Categories
                Category(name = "Salary", type = "CREDIT", iconKey = "salary", isDefault = true),
                Category(name = "Pocket Money", type = "CREDIT", iconKey = "wallet", isDefault = true),
                Category(name = "Other", type = "CREDIT", iconKey = "other", isDefault = true),

                // Expense Categories: Utility Bills, Mobile Package, Grocery, Other
                Category(name = "Utility Bills", type = "EXPENSE", iconKey = "bills", isDefault = true),
                Category(name = "Mobile Package", type = "EXPENSE", iconKey = "mobile", isDefault = true),
                Category(name = "Grocery", type = "EXPENSE", iconKey = "grocery", isDefault = true),
                Category(name = "Other", type = "EXPENSE", iconKey = "other", isDefault = true)
            )

            for (cat in updatedDefaultCategories) {
                val existing = categoryDao.findCategoryByNameAndType(cat.name, cat.type)
                if (existing == null) {
                    categoryDao.insertCategory(cat)
                }
            }

            // Clean up disallowed categories (Medicines, Monthly Expenditure, Birds, Bird Food, Cat Food) and any duplicates
            categoryDao.removeDisallowedExpenseCategories()
            categoryDao.removeDuplicateCategories()

            // 3. Initial Active Tracking Period
            val existingPeriod = trackingPeriodDao.getActivePeriod()
            if (existingPeriod == null) {
                val initialPeriod = TrackingPeriod(
                    name = "October 2026",
                    startedAt = System.currentTimeMillis(),
                    isActive = true
                )
                trackingPeriodDao.insertPeriod(initialPeriod)
            }
        }
    }
}
