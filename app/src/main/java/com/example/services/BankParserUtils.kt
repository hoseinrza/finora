package com.example.services

import android.content.Context
import android.provider.Telephony
import com.example.data.AccountEntity
import com.example.data.AccountType
import com.example.data.FinoraDatabase
import com.example.data.NotificationEntity
import com.example.data.PaymentMethod
import com.example.data.TransactionCategory
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

data class ParsedTransaction(
    val amount: Long,
    val type: TransactionType,
    val bankName: String,
    val accountOrCard: String?,
    val title: String,
    val category: TransactionCategory,
    val rawText: String,
    val balanceAfter: Long? = null
)

object BankParserUtils {

    private val BANKS = listOf(
        "بلوبانک", "بلو", "سامان", "ملی", "پاسارگاد", "تجارت", "ملت",
        "پارسیان", "صادارات", "سپه", "کشاورزی", "مسکن", "آینده", "رفاه", "شهر"
    )

    // A live SMS receipt and a manual sync over the same time window can both pick up the same
    // message; treat same amount+type within this window as the same transaction.
    private const val DEDUP_WINDOW_MS = 3 * 60 * 1000L

    fun parseText(rawText: String): ParsedTransaction? {
        if (rawText.isBlank()) return null

        val normalized = normalizeDigits(rawText)

        // Check for financial keywords
        val isExpense = normalized.contains("برداشت") ||
                normalized.contains("خرید") ||
                normalized.contains("بدهکار") ||
                normalized.contains("کسر") ||
                normalized.contains("انتقال از") ||
                normalized.contains("پرداخت")

        val isIncome = normalized.contains("واریز") ||
                normalized.contains("بستانکار") ||
                normalized.contains("افزایش") ||
                normalized.contains("انتقال به") ||
                normalized.contains("حقوق")

        if (!isExpense && !isIncome) {
            return null // Not a bank transaction SMS/Notification
        }

        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // Find bank name
        val bankName = BANKS.firstOrNull { normalized.contains(it) } ?: "کارت بانکی"

        val isRial = normalized.contains("ریال")

        // Extract amount
        var extractedAmount = extractAmount(normalized) ?: return null

        // If unit is Rial (ریال), convert to Toman by / 10
        if (isRial) {
            extractedAmount /= 10
        }

        if (extractedAmount <= 0) return null

        var balanceAfter = extractBalance(normalized)
        if (isRial) {
            balanceAfter = balanceAfter?.div(10)
        }

        // Guess category based on text
        val category = guessCategory(normalized, type)

        val title = if (type == TransactionType.INCOME) "واریز بانکی ($bankName)" else "تراکنش بانکی ($bankName)"

        return ParsedTransaction(
            amount = extractedAmount,
            type = type,
            bankName = bankName,
            accountOrCard = null,
            title = title,
            category = category,
            rawText = rawText,
            balanceAfter = balanceAfter
        )
    }

    // Matches a number written as either thousands-grouped (commas, Persian/Arabic thousands
    // separators, or periods) or a bare run of 4-12 digits.
    private const val NUMBER_PATTERN = "([0-9]{1,3}(?:[\\,،٬\\.][0-9]{3})+|[0-9]{4,12})"

    private fun extractAmount(text: String): Long? {
        // Bank SMS almost always state the transaction amount immediately followed by its unit
        // (ریال/تومان). Anchoring on that unit avoids grabbing the wrong number when the message
        // also contains a card number, tracking/reference number, balance, or a date/time - any
        // of which can otherwise look like "the first long digit run in the text" to a looser regex.
        val anchored = Pattern.compile("$NUMBER_PATTERN\\s*(?:ریال|تومان)")
        anchored.matcher(text).let { if (it.find()) return parseNumber(it.group(1)) }

        // Fallback for messages that don't state a currency unit next to the amount.
        val loose = Pattern.compile(NUMBER_PATTERN)
        loose.matcher(text).let { if (it.find()) return parseNumber(it.group(1)) }

        return null
    }

    private fun extractBalance(text: String): Long? {
        val regex = Pattern.compile("موجودی[^0-9]{0,10}$NUMBER_PATTERN")
        val matcher = regex.matcher(text)
        return if (matcher.find()) parseNumber(matcher.group(1)) else null
    }

    private fun parseNumber(raw: String?): Long? {
        return raw
            ?.replace(",", "")
            ?.replace("،", "")
            ?.replace("٬", "")
            ?.replace(".", "")
            ?.trim()
            ?.toLongOrNull()
    }

    private fun guessCategory(text: String, type: TransactionType): TransactionCategory {
        if (type == TransactionType.INCOME) {
            return if (text.contains("حقوق") || text.contains("دستمزد")) TransactionCategory.SALARY
            else TransactionCategory.INVESTMENT
        }

        return when {
            text.contains("غذا") || text.contains("رستوران") || text.contains("اسنپ فود") || text.contains("سوپر") -> TransactionCategory.FOOD
            text.contains("اسنپ") || text.contains("تپسی") || text.contains("بنزین") || text.contains("تاکسی") -> TransactionCategory.TRANSPORT
            text.contains("قبض") || text.contains("شارژ") || text.contains("همراه اول") || text.contains("ایرانسل") -> TransactionCategory.BILL
            text.contains("دارو") || text.contains("مطب") || text.contains("بیمارستان") || text.contains("داروخانه") -> TransactionCategory.HEALTH
            text.contains("پوشاک") || text.contains("دیجی کالا") || text.contains("فروشگاه") -> TransactionCategory.SHOPPING
            text.contains("اجاره") || text.contains("مسکن") -> TransactionCategory.HOUSING
            else -> TransactionCategory.OTHER
        }
    }

    fun normalizeDigits(input: String): String {
        return input
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
            .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
    }

    /**
     * True if a transaction matching [parsed] (same amount + type, within [DEDUP_WINDOW_MS] of
     * [aroundMillis]) already exists. When a balance was parsed from this message, an existing
     * candidate with a *different* known balance is not treated as the same transaction (two
     * distinct transactions can coincidentally share an amount within the window).
     */
    private suspend fun isDuplicate(db: FinoraDatabase, parsed: ParsedTransaction, aroundMillis: Long): Boolean {
        val candidates = db.transactionDao().findSimilar(
            amount = parsed.amount,
            type = parsed.type,
            from = aroundMillis - DEDUP_WINDOW_MS,
            to = aroundMillis + DEDUP_WINDOW_MS
        )
        if (candidates.isEmpty()) return false
        val balance = parsed.balanceAfter ?: return true
        return candidates.any { it.balanceAfter == null || it.balanceAfter == balance }
    }

    /**
     * Keeps the matching bank account's balance in sync with the most recent SMS we've seen for
     * it. [asOfMillis] is the message's own timestamp (not "now"), so that processing messages
     * out of order - e.g. a historical sync running after a live SMS already updated the balance -
     * never overwrites a newer known balance with an older one.
     */
    private suspend fun upsertAccountBalance(db: FinoraDatabase, bankName: String, balance: Long, asOfMillis: Long) {
        val existing = db.accountDao().findByName(bankName)
        if (existing == null) {
            db.accountDao().insertAccount(
                AccountEntity(name = bankName, type = AccountType.BANK, balance = balance, balanceUpdatedAt = asOfMillis)
            )
        } else if (existing.balanceUpdatedAt == null || asOfMillis >= existing.balanceUpdatedAt) {
            db.accountDao().updateAccount(existing.copy(balance = balance, balanceUpdatedAt = asOfMillis))
        }
    }

    fun processAndSave(context: Context, rawText: String, source: String) {
        val parsed = parseText(rawText) ?: return
        val db = FinoraDatabase.getDatabase(context)

        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            if (isDuplicate(db, parsed, now)) return@launch

            // Save transaction to Room
            val transaction = TransactionEntity(
                title = parsed.title,
                amount = parsed.amount,
                type = parsed.type,
                category = parsed.category,
                paymentMethod = PaymentMethod.BANK_CARD,
                accountName = parsed.bankName,
                date = now,
                description = "ثبت خودکار از $source\nمتن: ${parsed.rawText.take(100)}",
                balanceAfter = parsed.balanceAfter
            )
            db.transactionDao().insertTransaction(transaction)

            parsed.balanceAfter?.let { upsertAccountBalance(db, parsed.bankName, it, now) }

            // Save Notification log to Room
            val notif = NotificationEntity(
                title = "ثبت خودکار تراکنش بانکی",
                message = "مبلغ ${java.text.DecimalFormat("#,###").format(parsed.amount)} تومان (${parsed.type.titleFa}) از $source اضافه شد.",
                date = now,
                type = "MILESTONE"
            )
            db.notificationDao().insertNotification(notif)

            // Trigger real Android system notification on status bar
            NotificationHelper.showNotification(
                context = context,
                title = "تراکنش جدید: ${parsed.title}",
                message = "مبلغ ${java.text.DecimalFormat("#,###").format(parsed.amount)} تومان ثبت شد"
            )
        }
    }

    /**
     * Reads recent SMS from the device inbox (requires READ_SMS) and inserts any bank
     * transactions found, skipping ones that look like duplicates of an already-recorded
     * transaction (see [isDuplicate]) and keeping each detected bank account's balance current
     * (see [upsertAccountBalance]).
     *
     * @return the number of new transactions inserted.
     */
    suspend fun syncRecentSms(context: Context, daysBack: Int = 30): Int = withContext(Dispatchers.IO) {
        val db = FinoraDatabase.getDatabase(context)
        val sinceMillis = System.currentTimeMillis() - daysBack * 24 * 3600 * 1000L

        var insertedCount = 0
        val projection = arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE)

        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(sinceMillis.toString()),
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                val body = cursor.getString(bodyIndex) ?: continue
                val smsDate = cursor.getLong(dateIndex)
                val parsed = parseText(body) ?: continue

                if (isDuplicate(db, parsed, smsDate)) continue

                db.transactionDao().insertTransaction(
                    TransactionEntity(
                        title = parsed.title,
                        amount = parsed.amount,
                        type = parsed.type,
                        category = parsed.category,
                        paymentMethod = PaymentMethod.BANK_CARD,
                        accountName = parsed.bankName,
                        date = smsDate,
                        description = "ثبت خودکار از پیامک\nمتن: ${parsed.rawText.take(100)}",
                        balanceAfter = parsed.balanceAfter
                    )
                )
                parsed.balanceAfter?.let { upsertAccountBalance(db, parsed.bankName, it, smsDate) }
                insertedCount++
            }
        }

        if (insertedCount > 0) {
            db.notificationDao().insertNotification(
                NotificationEntity(
                    title = "همگام‌سازی پیامک‌ها",
                    message = "$insertedCount تراکنش جدید از پیامک‌های اخیر شما ثبت شد.",
                    date = System.currentTimeMillis(),
                    type = "MILESTONE"
                )
            )
        }

        insertedCount
    }
}
