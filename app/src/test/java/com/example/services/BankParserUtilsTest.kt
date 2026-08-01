package com.example.services

import com.example.data.TransactionCategory
import com.example.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Regression coverage for [BankParserUtils.parseText] / [BankParserUtils.normalizeDigits] -
 * the most business-critical, regex-heavy code in the app (turns raw bank SMS/notification text
 * into a [ParsedTransaction]). `parseText` and `normalizeDigits` are pure functions with no
 * Android/Context dependency, so this runs as a plain JVM unit test (no Robolectric needed).
 */
class BankParserUtilsTest {

    // --- normalizeDigits -----------------------------------------------------

    @Test
    fun `normalizeDigits converts Persian digits to Western`() {
        assertEquals("0123456789", BankParserUtils.normalizeDigits("۰۱۲۳۴۵۶۷۸۹"))
    }

    @Test
    fun `normalizeDigits converts Arabic-Indic digits to Western`() {
        assertEquals("0123456789", BankParserUtils.normalizeDigits("٠١٢٣٤٥٦٧٨٩"))
    }

    @Test
    fun `normalizeDigits leaves Western digits and other text untouched`() {
        assertEquals("Hello 123 world", BankParserUtils.normalizeDigits("Hello 123 world"))
    }

    // --- parseText: non-transaction text --------------------------------------

    @Test
    fun `parseText returns null for blank text`() {
        assertNull(BankParserUtils.parseText(""))
        assertNull(BankParserUtils.parseText("   "))
    }

    @Test
    fun `parseText returns null when no financial keyword is present`() {
        assertNull(BankParserUtils.parseText("سلام، قرار ما فردا ساعت ۵ عصر هست."))
    }

    @Test
    fun `parseText returns null when a keyword is present but no amount can be found`() {
        assertNull(BankParserUtils.parseText("خرید شما با موفقیت انجام شد"))
    }

    // --- parseText: expense detection ------------------------------------------

    @Test
    fun `parseText detects a withdrawal SMS as an expense`() {
        val result = BankParserUtils.parseText("برداشت 1,200,000 تومان - بانک ملت")
        assertNotNull(result)
        assertEquals(TransactionType.EXPENSE, result!!.type)
        assertEquals(1_200_000L, result.amount)
        assertEquals("ملت", result.bankName)
    }

    @Test
    fun `parseText detects a purchase SMS with Persian digits as an expense`() {
        val result = BankParserUtils.parseText("خرید ۳۵۰,۰۰۰ تومان با کارت بانک سامان")
        assertNotNull(result)
        assertEquals(TransactionType.EXPENSE, result!!.type)
        assertEquals(350_000L, result.amount)
        assertEquals("سامان", result.bankName)
    }

    @Test
    fun `parseText converts Rial amounts to Toman`() {
        val result = BankParserUtils.parseText("واریز 2,500,000 ریال به حساب بلوبانک")
        assertNotNull(result)
        assertEquals(TransactionType.INCOME, result!!.type)
        assertEquals(250_000L, result.amount) // 2,500,000 rial / 10 = 250,000 toman
    }

    // --- parseText: income detection --------------------------------------------

    @Test
    fun `parseText detects a deposit SMS as income`() {
        val result = BankParserUtils.parseText("واریز حقوق 15,000,000 تومان بانک ملی")
        assertNotNull(result)
        assertEquals(TransactionType.INCOME, result!!.type)
        assertEquals(15_000_000L, result.amount)
        assertEquals(TransactionCategory.SALARY, result.category)
        assertEquals("ملی", result.bankName)
    }

    @Test
    fun `parseText falls back to the default bank label when no known bank name matches`() {
        val result = BankParserUtils.parseText("واریز 500000 به حساب شما")
        assertNotNull(result)
        assertEquals("کارت بانکی", result!!.bankName)
    }

    // --- parseText: category guessing ---------------------------------------

    @Test
    fun `parseText categorizes a restaurant purchase as FOOD`() {
        val result = BankParserUtils.parseText("خرید 450000 تومان از رستوران")
        assertEquals(TransactionCategory.FOOD, result?.category)
    }

    @Test
    fun `parseText categorizes a taxi fare as TRANSPORT`() {
        val result = BankParserUtils.parseText("پرداخت 85000 تومان به تاکسی")
        assertEquals(TransactionCategory.TRANSPORT, result?.category)
    }

    @Test
    fun `parseText categorizes a bill payment as BILL`() {
        val result = BankParserUtils.parseText("پرداخت قبض 320000 تومان ایرانسل")
        assertEquals(TransactionCategory.BILL, result?.category)
    }

    @Test
    fun `parseText categorizes an unrecognized expense as OTHER`() {
        val result = BankParserUtils.parseText("برداشت 99000 تومان")
        assertEquals(TransactionCategory.OTHER, result?.category)
    }

    @Test
    fun `parseText categorizes non-salary income as INVESTMENT`() {
        val result = BankParserUtils.parseText("واریز 4500000 تومان بابت پروژه فریلنس")
        assertEquals(TransactionCategory.INVESTMENT, result?.category)
    }

    // --- amount format regression tests --------------------------------------

    @Test
    fun `parseText extracts an unseparated multi-digit amount`() {
        val result = BankParserUtils.parseText("خرید 65000000 تومان")
        assertEquals(65_000_000L, result?.amount)
    }

    @Test
    fun `parseText extracts a comma-grouped amount`() {
        val result = BankParserUtils.parseText("برداشت 1,850,000 تومان")
        assertEquals(1_850_000L, result?.amount)
    }

    @Test
    fun `parseText preserves the raw SMS text unmodified`() {
        val raw = "برداشت 1,200,000 تومان - بانک ملت"
        val result = BankParserUtils.parseText(raw)
        assertEquals(raw, result?.rawText)
    }
}
