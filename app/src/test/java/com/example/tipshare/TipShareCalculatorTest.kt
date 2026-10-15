package com.example.tipshare

import com.example.tipshare.data.model.CurrencyOption
import com.example.tipshare.domain.CurrencyRepository
import com.example.tipshare.domain.TipShareCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TipShareCalculatorTest {

    private val usd = CurrencyRepository.USD
    private val jpy = CurrencyRepository.JPY
    private val pkr = CurrencyRepository.PKR
    private val eur = CurrencyRepository.EUR
    private val gbp = CurrencyRepository.GBP

    // 1. Zero tip
    @Test
    fun testZeroTip() {
        // Bill $100, Tip 0%, People 4 -> Each $25
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 0,
            customTipMinorUnits = null,
            peopleCount = 4,
            currency = usd
        )
        assertEquals(0L, result.tipMinorUnits)
        assertEquals(10000L, result.totalMinorUnits)
        assertEquals(listOf(2500L, 2500L, 2500L, 2500L), result.perPersonMinorUnits)
    }

    // 2. 10% tip
    @Test
    fun test10PercentTip() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 1000,
            customTipMinorUnits = null,
            peopleCount = 2,
            currency = usd
        )
        assertEquals(1000L, result.tipMinorUnits)
        assertEquals(11000L, result.totalMinorUnits)
        assertEquals(listOf(5500L, 5500L), result.perPersonMinorUnits)
    }

    // 3. 15% tip - Critical Test
    @Test
    fun test15PercentTipCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 4,
            currency = usd
        )
        assertEquals(1500L, result.tipMinorUnits)
        assertEquals(11500L, result.totalMinorUnits)
        assertEquals(2875L, result.perPersonMinorUnits[0])
    }

    // 4. 18% tip
    @Test
    fun test18PercentTip() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 5000L,
            tipPercentageBasisPoints = 1800,
            customTipMinorUnits = null,
            peopleCount = 2,
            currency = usd
        )
        assertEquals(900L, result.tipMinorUnits)
        assertEquals(5900L, result.totalMinorUnits)
        assertEquals(listOf(2950L, 2950L), result.perPersonMinorUnits)
    }

    // 5. 20% tip
    @Test
    fun test20PercentTip() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 2000,
            customTipMinorUnits = null,
            peopleCount = 1,
            currency = usd
        )
        assertEquals(2000L, result.tipMinorUnits)
        assertEquals(12000L, result.totalMinorUnits)
        assertEquals(listOf(12000L), result.perPersonMinorUnits)
    }

    // 6. 25% tip
    @Test
    fun test25PercentTip() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 8000L,
            tipPercentageBasisPoints = 2500,
            customTipMinorUnits = null,
            peopleCount = 4,
            currency = usd
        )
        assertEquals(2000L, result.tipMinorUnits)
        assertEquals(10000L, result.totalMinorUnits)
        assertEquals(listOf(2500L, 2500L, 2500L, 2500L), result.perPersonMinorUnits)
    }

    // 7. Decimal tip - Critical Test (Bill $80, Tip 12.5% -> Tip $10, Total $90)
    @Test
    fun testDecimalTipCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 8000L,
            tipPercentageBasisPoints = 1250,
            customTipMinorUnits = null,
            peopleCount = 3,
            currency = usd
        )
        assertEquals(1000L, result.tipMinorUnits)
        assertEquals(9000L, result.totalMinorUnits)
        assertEquals(listOf(3000L, 3000L, 3000L), result.perPersonMinorUnits)
    }

    // 8. Custom tip - Critical Test (Bill $100, Custom tip $20, People 4 -> $30 each)
    @Test
    fun testCustomTipCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = null,
            customTipMinorUnits = 2000L,
            peopleCount = 4,
            currency = usd
        )
        assertTrue(result.isCustomTip)
        assertEquals(2000L, result.tipMinorUnits)
        assertEquals(12000L, result.totalMinorUnits)
        assertEquals(listOf(3000L, 3000L, 3000L, 3000L), result.perPersonMinorUnits)
    }

    // 9. Equal split
    @Test
    fun testEqualSplit() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 6000L,
            tipPercentageBasisPoints = 2000,
            customTipMinorUnits = null,
            peopleCount = 3,
            currency = usd
        )
        assertEquals(1200L, result.tipMinorUnits)
        assertEquals(7200L, result.totalMinorUnits)
        assertEquals(listOf(2400L, 2400L, 2400L), result.perPersonMinorUnits)
        assertEquals(0L, result.roundingAdjustmentMinorUnits)
    }

    // 10. One person - Critical Test (Bill $100, Tip 20%, People 1 -> $120.00)
    @Test
    fun testOnePersonCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 2000,
            customTipMinorUnits = null,
            peopleCount = 1,
            currency = usd
        )
        assertEquals(12000L, result.totalMinorUnits)
        assertEquals(1, result.perPersonMinorUnits.size)
        assertEquals(12000L, result.perPersonMinorUnits[0])
    }

    // 11. Two people
    @Test
    fun testTwoPeople() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 5000L,
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 2,
            currency = usd
        )
        assertEquals(750L, result.tipMinorUnits)
        assertEquals(5750L, result.totalMinorUnits)
        assertEquals(listOf(2875L, 2875L), result.perPersonMinorUnits)
    }

    // 12. Three people - Critical Test (Bill $100, Tip 15%, People 3 -> 38.34, 38.33, 38.33)
    @Test
    fun testThreePeopleCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 3,
            currency = usd
        )
        assertEquals(1500L, result.tipMinorUnits)
        assertEquals(11500L, result.totalMinorUnits)
        assertEquals(listOf(3834L, 3833L, 3833L), result.perPersonMinorUnits)
        assertEquals(11500L, result.perPersonMinorUnits.sum())
    }

    // 13. Four people - Critical Test (Bill $100, Tip 15%, People 4 -> 28.75 each)
    @Test
    fun testFourPeopleCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 4,
            currency = usd
        )
        assertEquals(11500L, result.totalMinorUnits)
        assertEquals(listOf(2875L, 2875L, 2875L, 2875L), result.perPersonMinorUnits)
    }

    // 14. Ten people
    @Test
    fun testTenPeople() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 10000L,
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 10,
            currency = usd
        )
        assertEquals(11500L, result.totalMinorUnits)
        assertEquals(10, result.perPersonMinorUnits.size)
        assertEquals(1150L, result.perPersonMinorUnits[0])
        assertEquals(11500L, result.perPersonMinorUnits.sum())
    }

    // 15. Fifty people (Maximum allowed limit)
    @Test
    fun testFiftyPeople() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 200000L, // $2,000.00
            tipPercentageBasisPoints = 2000,
            customTipMinorUnits = null,
            peopleCount = 50,
            currency = usd
        )
        assertEquals(240000L, result.totalMinorUnits)
        assertEquals(50, result.perPersonMinorUnits.size)
        assertEquals(4800L, result.perPersonMinorUnits[0])
        assertEquals(240000L, result.perPersonMinorUnits.sum())
    }

    // 16. Even split
    @Test
    fun testEvenSplit() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 12000L,
            tipPercentageBasisPoints = 0,
            customTipMinorUnits = null,
            peopleCount = 6,
            currency = usd
        )
        assertEquals(0L, result.roundingAdjustmentMinorUnits)
        assertTrue(result.perPersonMinorUnits.all { it == 2000L })
    }

    // 17. Uneven split & 18. One-cent remainder
    @Test
    fun testOneCentRemainder() {
        // Total $10.00 (1000 cents) / 3 people = 334, 333, 333 (1 cent remainder)
        val result = TipShareCalculator.calculate(
            billMinorUnits = 1000L,
            tipPercentageBasisPoints = 0,
            customTipMinorUnits = null,
            peopleCount = 3,
            currency = usd
        )
        assertEquals(1L, result.roundingAdjustmentMinorUnits)
        assertEquals(listOf(334L, 333L, 333L), result.perPersonMinorUnits)
        assertEquals(1000L, result.perPersonMinorUnits.sum())
    }

    // 19. Multiple-cent remainder
    @Test
    fun testMultipleCentRemainder() {
        // Total $10.00 (1000 cents) / 7 people = 143, 143, 143, 143, 143, 143, 142 (6 cent remainder: 1000 % 7 = 6)
        val result = TipShareCalculator.calculate(
            billMinorUnits = 1000L,
            tipPercentageBasisPoints = 0,
            customTipMinorUnits = null,
            peopleCount = 7,
            currency = usd
        )
        assertEquals(6L, result.roundingAdjustmentMinorUnits)
        assertEquals(7, result.perPersonMinorUnits.size)
        for (i in 0 until 6) {
            assertEquals(143L, result.perPersonMinorUnits[i])
        }
        assertEquals(142L, result.perPersonMinorUnits[6])
        assertEquals(1000L, result.perPersonMinorUnits.sum())
    }

    // 20. USD
    @Test
    fun testUSD() {
        val result = TipShareCalculator.calculate(10000L, 1500, null, 4, usd)
        assertEquals("USD", result.currency.code)
        assertEquals(2, result.currency.decimalPlaces)
    }

    // 21. EUR
    @Test
    fun testEUR() {
        val result = TipShareCalculator.calculate(5000L, 1000, null, 2, eur)
        assertEquals("EUR", result.currency.code)
        assertEquals(5500L, result.totalMinorUnits)
        assertEquals(listOf(2750L, 2750L), result.perPersonMinorUnits)
    }

    // 22. GBP
    @Test
    fun testGBP() {
        val result = TipShareCalculator.calculate(8000L, 1250, null, 3, gbp)
        assertEquals("GBP", result.currency.code)
        assertEquals(9000L, result.totalMinorUnits)
        assertEquals(listOf(3000L, 3000L, 3000L), result.perPersonMinorUnits)
    }

    // 23. PKR - Critical Test (Bill Rs. 1000.00, Tip 15%, People 4 -> Tip Rs. 150.00, Total Rs. 1,150.00, Each Rs. 287.50)
    @Test
    fun testPKRCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 100000L, // 1000.00
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 4,
            currency = pkr
        )
        assertEquals(15000L, result.tipMinorUnits) // Rs. 150.00
        assertEquals(115000L, result.totalMinorUnits) // Rs. 1,150.00
        assertEquals(listOf(28750L, 28750L, 28750L, 28750L), result.perPersonMinorUnits) // Rs. 287.50 each
        assertEquals(115000L, result.perPersonMinorUnits.sum())
    }

    // 24. JPY - Critical Test (Bill ¥1000, Tip 15%, People 3 -> ¥384, ¥383, ¥383, Total ¥1150)
    @Test
    fun testJPYCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 1000L, // ¥1000
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 3,
            currency = jpy
        )
        assertEquals(0, result.currency.decimalPlaces)
        assertEquals(150L, result.tipMinorUnits) // ¥150
        assertEquals(1150L, result.totalMinorUnits) // ¥1150
        assertEquals(listOf(384L, 383L, 383L), result.perPersonMinorUnits)
        assertEquals(1150L, result.perPersonMinorUnits.sum())
    }

    // 25. Zero-decimal currency & 26. Two-decimal currency
    @Test
    fun testZeroAndTwoDecimalCurrencies() {
        assertEquals(0, jpy.decimalPlaces)
        assertEquals(2, usd.decimalPlaces)
    }

    // 27. Custom currency
    @Test
    fun testCustomCurrency() {
        val custom = CurrencyOption("XYZ", "X", "Custom Currency", 2)
        val result = TipShareCalculator.calculate(5000L, 1000, null, 2, custom)
        assertEquals("XYZ", result.currency.code)
        assertEquals("X", result.currency.symbol)
        assertEquals(5500L, result.totalMinorUnits)
    }

    // 30. Tip rounding - Critical Test (Bill $99.99, Tip 15% -> Exact 14.9985 -> Rounded 15.00, Total 114.99)
    @Test
    fun testTipRoundingCritical() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 9999L,
            tipPercentageBasisPoints = 1500,
            customTipMinorUnits = null,
            peopleCount = 1,
            currency = usd
        )
        assertEquals(1500L, result.tipMinorUnits)
        assertEquals(11499L, result.totalMinorUnits)
    }

    // 31. Total calculation & 32. Split rounding
    @Test
    fun testTotalCalculationAndSplitRounding() {
        val result = TipShareCalculator.calculate(
            billMinorUnits = 4999L,
            tipPercentageBasisPoints = 2000,
            customTipMinorUnits = null,
            peopleCount = 3,
            currency = usd
        )
        // 4999 * 2000 = 9,998,000 -> (9,998,000 + 5000) / 10000 = 1000
        assertEquals(1000L, result.tipMinorUnits)
        assertEquals(5999L, result.totalMinorUnits)
        // 5999 / 3 = 1999 rem 2 -> 2000, 2000, 1999
        assertEquals(listOf(2000L, 2000L, 1999L), result.perPersonMinorUnits)
        assertEquals(5999L, result.perPersonMinorUnits.sum())
    }

    // 33. Allocation invariant - Critical Test (sum(personAmounts) == total)
    @Test
    fun testAllocationInvariantExhaustive() {
        val testBills = listOf(0L, 1L, 99L, 100L, 1000L, 9999L, 12345L, 50000L, 999999L)
        val testBps = listOf(0, 500, 1000, 1250, 1500, 1800, 2000, 2500, 3333, 10000)
        val testPeople = (1..50).toList()

        for (b in testBills) {
            for (bp in testBps) {
                for (p in listOf(1, 2, 3, 4, 7, 13, 29, 50)) {
                    val result = TipShareCalculator.calculate(b, bp, null, p, usd)
                    assertEquals("Invariant failed for bill=$b, bp=$bp, people=$p", result.totalMinorUnits, result.perPersonMinorUnits.sum())
                }
            }
        }
    }

    // 34. Deterministic remainder assignment - Critical Test
    @Test
    fun testDeterministicRemainderAssignment() {
        // Run $115 ÷ 3 multiple times; person 1 must always get the extra cent
        for (i in 1..20) {
            val result = TipShareCalculator.calculate(10000L, 1500, null, 3, usd)
            assertEquals(3834L, result.perPersonMinorUnits[0])
            assertEquals(3833L, result.perPersonMinorUnits[1])
            assertEquals(3833L, result.perPersonMinorUnits[2])
        }
    }

    // 35. Zero bill & 36. Zero tip
    @Test
    fun testZeroBillZeroTip() {
        val result = TipShareCalculator.calculate(0L, 0, null, 4, usd)
        assertEquals(0L, result.tipMinorUnits)
        assertEquals(0L, result.totalMinorUnits)
        assertEquals(listOf(0L, 0L, 0L, 0L), result.perPersonMinorUnits)
    }

    @Test
    fun testZeroBillPositivePercentTip() {
        val result = TipShareCalculator.calculate(0L, 1500, null, 2, usd)
        assertEquals(0L, result.tipMinorUnits)
        assertEquals(0L, result.totalMinorUnits)
        assertEquals(listOf(0L, 0L), result.perPersonMinorUnits)
    }

    // 37. Custom tip on zero bill
    @Test
    fun testCustomTipOnZeroBill() {
        val result = TipShareCalculator.calculate(0L, null, 1000L, 2, usd)
        assertEquals(1000L, result.tipMinorUnits)
        assertEquals(1000L, result.totalMinorUnits)
        assertEquals(listOf(500L, 500L), result.perPersonMinorUnits)
    }

    // 38. Negative bill rejection
    @Test(expected = IllegalArgumentException::class)
    fun testNegativeBillRejection() {
        TipShareCalculator.calculate(-100L, 1500, null, 4, usd)
    }

    // 39. Negative tip rejection
    @Test(expected = IllegalArgumentException::class)
    fun testNegativeTipRejection() {
        TipShareCalculator.calculate(10000L, null, -500L, 4, usd)
    }

    // 40. Invalid percentage (< 0)
    @Test(expected = IllegalArgumentException::class)
    fun testNegativePercentageRejection() {
        TipShareCalculator.calculate(10000L, -100, null, 4, usd)
    }

    // 41. Percentage > 100 rejection
    @Test(expected = IllegalArgumentException::class)
    fun testPercentageGreaterThan100Rejection() {
        TipShareCalculator.calculate(10000L, 10001, null, 4, usd)
    }

    // 42. Zero people rejection
    @Test(expected = IllegalArgumentException::class)
    fun testZeroPeopleRejection() {
        TipShareCalculator.calculate(10000L, 1500, null, 0, usd)
    }

    // 43. More than 50 people rejection
    @Test(expected = IllegalArgumentException::class)
    fun testMoreThan50PeopleRejection() {
        TipShareCalculator.calculate(10000L, 1500, null, 51, usd)
    }

    // 44. Large bill
    @Test
    fun testLargeBill() {
        val largeBill = 50_000_000_00L // $50,000,000.00
        val result = TipShareCalculator.calculate(largeBill, 2000, null, 5, usd)
        assertEquals(10_000_000_00L, result.tipMinorUnits)
        assertEquals(60_000_000_00L, result.totalMinorUnits)
        assertEquals(12_000_000_00L, result.perPersonMinorUnits[0])
        assertEquals(60_000_000_00L, result.perPersonMinorUnits.sum())
    }

    // 45. Overflow protection
    @Test
    fun testOverflowProtection() {
        try {
            // Bill so high that multiplyExact will overflow Long.MAX_VALUE
            TipShareCalculator.calculate(Long.MAX_VALUE - 10, 2000, null, 2, usd)
            fail("Expected ArithmeticException on overflow")
        } catch (e: ArithmeticException) {
            // Success: safe arithmetic prevents silent numeric wrapping
        }
    }

    @Test
    fun testCalculateImpliedBasisPoints() {
        // $25 tip on $100 bill = 2500 basis points (25%)
        val bp = TipShareCalculator.calculateImpliedBasisPoints(10000L, 2500L)
        assertEquals(2500, bp)

        // Zero bill returns null
        val bpZero = TipShareCalculator.calculateImpliedBasisPoints(0L, 2500L)
        assertEquals(null, bpZero)
    }
}
