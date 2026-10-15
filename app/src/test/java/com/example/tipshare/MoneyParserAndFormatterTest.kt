package com.example.tipshare

import com.example.tipshare.domain.CurrencyRepository
import com.example.tipshare.domain.MoneyFormatter
import com.example.tipshare.domain.MoneyParser
import com.example.tipshare.domain.TipShareCalculator
import com.example.tipshare.domain.TipShareFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyParserAndFormatterTest {

    private val usd = CurrencyRepository.USD
    private val jpy = CurrencyRepository.JPY
    private val pkr = CurrencyRepository.PKR

    // 28. Money parsing
    @Test
    fun testMoneyParsingStandardCases() {
        // "1" -> 1.00 -> 100 cents
        assertEquals(100L, MoneyParser.parseToMinorUnits("1", 2).getOrThrow())

        // "10" -> 10.00 -> 1000 cents
        assertEquals(1000L, MoneyParser.parseToMinorUnits("10", 2).getOrThrow())

        // "100" -> 100.00 -> 10000 cents
        assertEquals(10000L, MoneyParser.parseToMinorUnits("100", 2).getOrThrow())

        // "1000.50" -> 100050 cents
        assertEquals(100050L, MoneyParser.parseToMinorUnits("1000.50", 2).getOrThrow())

        // "1000.5" -> 100050 cents (pads single decimal to two decimal places)
        assertEquals(100050L, MoneyParser.parseToMinorUnits("1000.5", 2).getOrThrow())

        // "0" -> 0 cents
        assertEquals(0L, MoneyParser.parseToMinorUnits("0", 2).getOrThrow())

        // "0.00" -> 0 cents
        assertEquals(0L, MoneyParser.parseToMinorUnits("0.00", 2).getOrThrow())

        // ".75" -> 75 cents
        assertEquals(75L, MoneyParser.parseToMinorUnits(".75", 2).getOrThrow())

        // With commas: "1,250.75"
        assertEquals(125075L, MoneyParser.parseToMinorUnits("1,250.75", 2).getOrThrow())
    }

    @Test
    fun testMoneyParsingZeroDecimalCurrencies() {
        // JPY has 0 decimal places: "1000" -> 1000
        assertEquals(1000L, MoneyParser.parseToMinorUnits("1000", 0).getOrThrow())

        // Commas with JPY: "1,250"
        assertEquals(1250L, MoneyParser.parseToMinorUnits("1,250", 0).getOrThrow())

        // Decimals in zero-decimal currency should fail
        assertTrue(MoneyParser.parseToMinorUnits("1000.5", 0).isFailure)
    }

    @Test
    fun testMoneyParsingInvalidInputs() {
        // Empty
        assertTrue(MoneyParser.parseToMinorUnits("", 2).isFailure)
        assertTrue(MoneyParser.parseToMinorUnits("   ", 2).isFailure)

        // Negative
        assertTrue(MoneyParser.parseToMinorUnits("-100", 2).isFailure)
        assertTrue(MoneyParser.parseToMinorUnits("-0.50", 2).isFailure)

        // Non-digits
        assertTrue(MoneyParser.parseToMinorUnits("abc", 2).isFailure)
        assertTrue(MoneyParser.parseToMinorUnits("100$", 2).isFailure)

        // Multiple decimal points
        assertTrue(MoneyParser.parseToMinorUnits("10.5.2", 2).isFailure)

        // More decimal digits than allowed
        assertTrue(MoneyParser.parseToMinorUnits("10.555", 2).isFailure)

        // Beyond safe limit
        assertTrue(MoneyParser.parseToMinorUnits("999999999999", 2).isFailure)
    }

    // 29. Money formatting
    @Test
    fun testMoneyFormattingExamples() {
        // $100.00
        assertEquals("$100.00", MoneyFormatter.format(10000L, usd))

        // Rs. 5,000.00
        assertEquals("Rs. 5,000.00", MoneyFormatter.format(500000L, pkr))

        // ¥1,250
        assertEquals("¥1,250", MoneyFormatter.format(1250L, jpy))

        // Zero
        assertEquals("$0.00", MoneyFormatter.format(0L, usd))
        assertEquals("¥0", MoneyFormatter.format(0L, jpy))

        // Format without symbol
        assertEquals("100.00", MoneyFormatter.format(10000L, usd, includeSymbol = false))
        assertEquals("5,000.00", MoneyFormatter.format(500000L, pkr, includeSymbol = false))
        assertEquals("1,250", MoneyFormatter.format(1250L, jpy, includeSymbol = false))
    }

    @Test
    fun testTipPercentageFormatting() {
        assertEquals("0%", TipShareFormatter.formatTipPercentage(0))
        assertEquals("10%", TipShareFormatter.formatTipPercentage(1000))
        assertEquals("15%", TipShareFormatter.formatTipPercentage(1500))
        assertEquals("18%", TipShareFormatter.formatTipPercentage(1800))
        assertEquals("20%", TipShareFormatter.formatTipPercentage(2000))
        assertEquals("25%", TipShareFormatter.formatTipPercentage(2500))
        assertEquals("12.5%", TipShareFormatter.formatTipPercentage(1250))
        assertEquals("18.75%", TipShareFormatter.formatTipPercentage(1875))
        assertEquals("100%", TipShareFormatter.formatTipPercentage(10000))
    }

    @Test
    fun testPeopleCountFormatting() {
        assertEquals("1 person", TipShareFormatter.formatPeopleCount(1))
        assertEquals("4 people", TipShareFormatter.formatPeopleCount(4))
        assertEquals("50 people", TipShareFormatter.formatPeopleCount(50))
    }

    // 54. Copy formatting
    @Test
    fun testCopyFormatting() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 4,
            currency = usd
        )
        val copyText = TipShareFormatter.formatCopyText(result)
        assertTrue(copyText.contains("TipShare Result"))
        assertTrue(copyText.contains("Bill: $100.00"))
        assertTrue(copyText.contains("Tip: 15% ($15.00)"))
        assertTrue(copyText.contains("Total: $115.00"))
        assertTrue(copyText.contains("People: 4"))
        assertTrue(copyText.contains("Person 1: $28.75"))
        assertTrue(copyText.contains("Person 2: $28.75"))
        assertTrue(copyText.contains("Person 3: $28.75"))
        assertTrue(copyText.contains("Person 4: $28.75"))
        assertTrue(copyText.contains("Each person pays: $28.75"))
    }

    // 55. Share formatting
    @Test
    fun testShareFormatting() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 4,
            currency = usd
        )
        val shareText = TipShareFormatter.formatShareText(result)
        assertTrue(shareText.contains("TipShare"))
        assertTrue(shareText.contains("Bill: $100.00"))
        assertTrue(shareText.contains("Tip: 15% = $15.00"))
        assertTrue(shareText.contains("Total: $115.00"))
        assertTrue(shareText.contains("4 people"))
        assertTrue(shareText.contains("$28.75 each"))
    }
}
