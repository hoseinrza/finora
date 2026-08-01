package com.example.services

import android.content.Context
import com.example.data.FinoraDatabase
import com.example.data.NotificationEntity
import com.example.data.PaymentMethod
import com.example.data.TransactionCategory
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

data class ParsedTransaction(
    val amount: Long,
    val type: TransactionType,
    val bankName: String,
    val accountOrCard: String?,
    val title: String,
    val category: TransactionCategory,
    val rawText: String
)

object BankParserUtils {

    private val BANKS = listOf(
        "بلوبانک", "بلو", "سامان", "ملی", "پاسارگاد", "تجارت", "ملت",
        "پارسیان", "صادارات", "سپه", "کشاورزی", "مسکن", "آینده", "رفاه", "شهر"
    )

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

        // Extract amount
        var extractedAmount = extractAmount(normalized) ?: return null

        // If unit is Rial (ریال), convert to Toman by / 10
        if (normalized.contains("ریال")) {
            extractedAmount /= 10
        }

        if (extractedAmount <= 0) return null

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
            rawText = rawText
        )
    }

    private fun extractAmount(text: String): Long? {
        // Regex matches digits with optional commas or periods e.g. 1,500,000 or 1500000
        val regex = Pattern.compile("(?:مبلغ|مبلغ:|مبلغ :|برداشت|واریز|خرید)?\\s*([0-9]{1,3}(?:[\\,،\\.][0-9]{3})+|[0-9]{4,12})")
        val matcher = regex.matcher(text)

        if (matcher.find()) {
            val amountStr = matcher.group(1)
                ?.replace(",", "")
                ?.replace("،", "")
                ?.replace(".", "")
                ?.trim() ?: return null
            return amountStr.toLongOrNull()
        }
        return null
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

    fun processAndSave(context: Context, rawText: String, source: String) {
        val parsed = parseText(rawText) ?: return
        val db = FinoraDatabase.getDatabase(context)

        CoroutineScope(Dispatchers.IO).launch {
            // Save transaction to Room
            val transaction = TransactionEntity(
                title = parsed.title,
                amount = parsed.amount,
                type = parsed.type,
                category = parsed.category,
                paymentMethod = PaymentMethod.BANK_CARD,
                accountName = parsed.bankName,
                date = System.currentTimeMillis(),
                description = "ثبت خودکار از $source\nمتن: ${parsed.rawText.take(100)}"
            )
            db.transactionDao().insertTransaction(transaction)

            // Save Notification log to Room
            val notif = NotificationEntity(
                title = "ثبت خودکار تراکنش بانکی",
                message = "مبلغ ${java.text.DecimalFormat("#,###").format(parsed.amount)} تومان (${parsed.type.titleFa}) از $source اضافه شد.",
                date = System.currentTimeMillis(),
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
}
