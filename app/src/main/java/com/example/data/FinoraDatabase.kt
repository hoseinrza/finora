package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.security.DatabasePassphraseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase as SqlCipherDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        SavingGoalEntity::class,
        AccountEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class FinoraDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingGoalDao(): SavingGoalDao
    abstract fun accountDao(): AccountDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: FinoraDatabase? = null

        // Renamed from the original "finora_database": SQLCipher can't open a file that was
        // created as a plain SQLite DB (wrong file format, not a Room migration concern), so the
        // encrypted database intentionally lives in a new file. See
        // docs/DATABASE_ENCRYPTION_MIGRATION.md for the full rationale and rollout notes.
        private const val DATABASE_NAME = "finora_database_encrypted.db"

        fun getDatabase(context: Context): FinoraDatabase {
            return INSTANCE ?: synchronized(this) {
                SqlCipherDatabase.loadLibs(context)
                val passphrase = DatabasePassphraseProvider.getOrCreatePassphrase(context)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinoraDatabase::class.java,
                    DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial sample data asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    seedInitialData(database)
                }
            }
        }

        private suspend fun seedInitialData(db: FinoraDatabase) {
            // Seed Accounts
            val acc1 = AccountEntity(name = "حساب سپهر پاسارگاد", type = AccountType.BANK, balance = 42500000L, accountNumber = "IR56-0570", currency = "تومان", colorHex = "#10B981")
            val acc2 = AccountEntity(name = "کیف پول بلوبانک", type = AccountType.WALLET, balance = 8200000L, accountNumber = "IR12-0620", currency = "تومان", colorHex = "#06B6D4")
            val acc3 = AccountEntity(name = "تتر کریپتو", type = AccountType.CRYPTO, balance = 15000000L, accountNumber = "TR7NHq...", currency = "تومان", colorHex = "#8B5CF6")
            db.accountDao().insertAccount(acc1)
            db.accountDao().insertAccount(acc2)
            db.accountDao().insertAccount(acc3)

            // Seed Budgets
            db.budgetDao().insertBudget(BudgetEntity(category = TransactionCategory.FOOD, amountLimit = 12000000L))
            db.budgetDao().insertBudget(BudgetEntity(category = TransactionCategory.SHOPPING, amountLimit = 8000000L))
            db.budgetDao().insertBudget(BudgetEntity(category = TransactionCategory.TRANSPORT, amountLimit = 4000000L))
            db.budgetDao().insertBudget(BudgetEntity(category = TransactionCategory.ENTERTAINMENT, amountLimit = 5000000L))

            // Seed Saving Goals
            db.savingGoalDao().insertGoal(SavingGoalEntity(title = "صندوق لوازمل خانگی جدید", targetAmount = 50000000L, currentAmount = 32000000L, colorHex = "#10B981"))
            db.savingGoalDao().insertGoal(SavingGoalEntity(title = "سفر کیش پاییز ۱۴۰۳", targetAmount = 25000000L, currentAmount = 18500000L, colorHex = "#06B6D4"))
            db.savingGoalDao().insertGoal(SavingGoalEntity(title = "صندوق اضطراری ۳ ماهه", targetAmount = 100000000L, currentAmount = 65000000L, colorHex = "#8B5CF6"))

            // Seed Transactions
            val now = System.currentTimeMillis()
            val dayInMs = 24 * 3600 * 1000L

            db.transactionDao().insertTransaction(TransactionEntity(title = "واریز حقوق مردادماه", amount = 65000000L, type = TransactionType.INCOME, category = TransactionCategory.SALARY, date = now - 1 * dayInMs, description = "شرکت فناوران دانش"))
            db.transactionDao().insertTransaction(TransactionEntity(title = "خرید هایپرمارکت فروشگاهی", amount = 3450000L, type = TransactionType.EXPENSE, category = TransactionCategory.FOOD, date = now - 2 * dayInMs, description = "خرید اقلام پروتئینی و لبنیات"))
            db.transactionDao().insertTransaction(TransactionEntity(title = "پروژه برنامه‌نویسی فریلنس", amount = 18000000L, type = TransactionType.INCOME, category = TransactionCategory.INVESTMENT, date = now - 3 * dayInMs, description = "طراحی رابط کاربری اپلیکیشن"))
            db.transactionDao().insertTransaction(TransactionEntity(title = "پرداخت اجاره مسکن", amount = 18000000L, type = TransactionType.EXPENSE, category = TransactionCategory.HOUSING, date = now - 4 * dayInMs, description = "اجاره ماهانه آپارتمان"))
            db.transactionDao().insertTransaction(TransactionEntity(title = "بنزین و سرویس خودرو", amount = 850000L, type = TransactionType.EXPENSE, category = TransactionCategory.TRANSPORT, date = now - 5 * dayInMs))
            db.transactionDao().insertTransaction(TransactionEntity(title = "خرید کفش ورزشی آنلاین", amount = 2900000L, type = TransactionType.EXPENSE, category = TransactionCategory.SHOPPING, date = now - 6 * dayInMs))

            // Seed Notifications
            db.notificationDao().insertNotification(NotificationEntity(title = "هشدار بودجه خوراکی", message = "۷۵٪ از بودجه خوراکی این ماه مصرف شده است.", type = "ALERT"))
            db.notificationDao().insertNotification(NotificationEntity(title = "هدف پس‌انداز به ۶۴٪ رسید", message = "شما به ۶۴٪ از هدف «صندوق لوازمل خانگی جدید» رسیدید!", type = "MILESTONE"))
            db.notificationDao().insertNotification(NotificationEntity(title = "گزارش مالی هوشمند هفته", message = "پس‌انداز این هفته شما ۱۲٪ نسبت به هفته گذشته افزایش یافت.", type = "REPORT"))
        }
    }
}
