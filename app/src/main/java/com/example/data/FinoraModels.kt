package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * نوع تراکنش (درآمد، هزینه، انتقال)
 */
enum class TransactionType(val titleFa: String) {
    INCOME("درآمد"),
    EXPENSE("هزینه"),
    TRANSFER("انتقال")
}

/**
 * دسته‌بندی‌های استاندارد تراکنش
 */
enum class TransactionCategory(val titleFa: String, val iconName: String) {
    FOOD("خوراکی و غذا", "restaurant"),
    SHOPPING("خرید و پوشاک", "shopping_bag"),
    HOUSING("مسکن و اجاره", "home"),
    TRANSPORT("حمل و نقل", "directions_car"),
    BILL("قبوض و خدمات", "receipt_long"),
    HEALTH("سلامت و درمان", "medical_services"),
    ENTERTAINMENT("تفریح و سرگرمی", "sports_esports"),
    SALARY("حقوق و درآمد", "payments"),
    INVESTMENT("سرمایه‌گذاری", "trending_up"),
    SAVING("پس‌انداز", "savings"),
    OTHER("سایر", "category")
}

/**
 * روش‌های پرداخت
 */
enum class PaymentMethod(val titleFa: String) {
    BANK_CARD("کارت بانکی"),
    CASH("نقد"),
    INTERNET_BANK("اینترنت بانک"),
    CRYPTO("رمزارز"),
    WALLET("کیف پول دیجیتال")
}

/**
 * جدول تراکنش‌ها در Room Database
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val paymentMethod: PaymentMethod = PaymentMethod.BANK_CARD,
    val accountName: String = "حساب اصلی",
    val date: Long = System.currentTimeMillis(),
    val description: String? = null,
    val isRecurring: Boolean = false,
    val tag: String? = null,
    // Account balance as reported by the source bank SMS right after this transaction, when
    // parsed. Used (together with amount/type/date) to detect the same SMS being processed twice
    // (e.g. once live via BankSmsReceiver, once via a manual sync over the same time window).
    val balanceAfter: Long? = null
)

/**
 * جدول بودجه‌بندی ماهانه دسته‌بندی‌ها
 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: TransactionCategory,
    val amountLimit: Long,
    val month: Int = 5,
    val year: Int = 1403
)

/**
 * جدول اهداف پس‌انداز
 */
@Entity(tableName = "saving_goals")
data class SavingGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Long,
    val currentAmount: Long = 0,
    val targetDate: Long = System.currentTimeMillis() + 90L * 24 * 3600 * 1000,
    val categoryIcon: String = "savings",
    val colorHex: String = "#10B981",
    val isCompleted: Boolean = false
)

/**
 * انواع حساب‌های مالی / کیف پول
 */
enum class AccountType(val titleFa: String) {
    BANK("حساب بانکی"),
    WALLET("کیف پول"),
    CARD("کارت اعتباری"),
    CRYPTO("کیف پول کریپتو")
}

/**
 * جدول حساب‌ها و کیف‌پول‌ها
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Long,
    val accountNumber: String? = null,
    val currency: String = "تومان",
    val colorHex: String = "#0F172A",
    // Timestamp (from the source SMS, not insert time) of the last balance figure applied to
    // this account, so out-of-order SMS processing never overwrites a newer known balance.
    val balanceUpdatedAt: Long? = null
)

/**
 * جدول اعلان‌ها و هشدارهای هوشمند
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val date: Long = System.currentTimeMillis(),
    val type: String = "ALERT", // ALERT, REMINDER, MILESTONE, REPORT
    val isRead: Boolean = false
)
