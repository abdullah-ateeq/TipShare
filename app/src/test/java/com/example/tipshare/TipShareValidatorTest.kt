package com.example.tipshare

import com.example.tipshare.data.model.TipMode
import com.example.tipshare.domain.CurrencyRepository
import com.example.tipshare.domain.TipShareValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TipShareValidatorTest {

    private val usd = CurrencyRepository.USD

    @Test
    fun testValidateBill() {
        // Valid bills
        assertTrue(TipShareValidator.validateBill("100.00", usd) is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateBill("0", usd) is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateBill("1250.50", usd) is TipShareValidator.ValidationResult.Valid)

        // Empty bill
        val emptyResult = TipShareValidator.validateBill("", usd)
        assertTrue(emptyResult is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Bill amount is required.", (emptyResult as TipShareValidator.ValidationResult.Invalid).message)

        // Negative bill
        val negativeResult = TipShareValidator.validateBill("-50.00", usd)
        assertTrue(negativeResult is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Bill amount cannot be negative.", (negativeResult as TipShareValidator.ValidationResult.Invalid).message)

        // Invalid format
        val invalidResult = TipShareValidator.validateBill("abc", usd)
        assertTrue(invalidResult is TipShareValidator.ValidationResult.Invalid)
    }

    @Test
    fun testValidateTipPercentage() {
        // Valid percentages (0 to 100)
        assertTrue(TipShareValidator.validateTipPercentage("0") is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateTipPercentage("10") is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateTipPercentage("12.5") is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateTipPercentage("15") is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateTipPercentage("18.5") is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateTipPercentage("20") is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateTipPercentage("100") is TipShareValidator.ValidationResult.Valid)

        // Negative percentage (< 0)
        val negative = TipShareValidator.validateTipPercentage("-1")
        assertTrue(negative is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Tip percentage must be between 0% and 100%.", (negative as TipShareValidator.ValidationResult.Invalid).message)

        // Percentage > 100
        val over100 = TipShareValidator.validateTipPercentage("101")
        assertTrue(over100 is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Tip percentage must be between 0% and 100%.", (over100 as TipShareValidator.ValidationResult.Invalid).message)

        // Empty percentage
        val empty = TipShareValidator.validateTipPercentage("")
        assertTrue(empty is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Tip percentage is required.", (empty as TipShareValidator.ValidationResult.Invalid).message)
    }

    @Test
    fun testValidateCustomTip() {
        // Valid custom tips
        assertTrue(TipShareValidator.validateCustomTip("0.00", usd) is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateCustomTip("15.00", usd) is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validateCustomTip("25", usd) is TipShareValidator.ValidationResult.Valid)

        // Negative custom tip
        val negative = TipShareValidator.validateCustomTip("-10", usd)
        assertTrue(negative is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Tip amount cannot be negative.", (negative as TipShareValidator.ValidationResult.Invalid).message)

        // Empty custom tip
        val empty = TipShareValidator.validateCustomTip("", usd)
        assertTrue(empty is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Tip amount is required.", (empty as TipShareValidator.ValidationResult.Invalid).message)
    }

    @Test
    fun testValidatePeopleCount() {
        // Valid people count (1 to 50)
        assertTrue(TipShareValidator.validatePeopleCount("1") is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validatePeopleCount("4") is TipShareValidator.ValidationResult.Valid)
        assertTrue(TipShareValidator.validatePeopleCount("50") is TipShareValidator.ValidationResult.Valid)

        // Zero people
        val zero = TipShareValidator.validatePeopleCount("0")
        assertTrue(zero is TipShareValidator.ValidationResult.Invalid)
        assertEquals("At least 1 person is required.", (zero as TipShareValidator.ValidationResult.Invalid).message)

        // More than 50 people
        val fiftyOne = TipShareValidator.validatePeopleCount("51")
        assertTrue(fiftyOne is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Maximum 50 people.", (fiftyOne as TipShareValidator.ValidationResult.Invalid).message)

        // Negative people
        val negative = TipShareValidator.validatePeopleCount("-5")
        assertTrue(negative is TipShareValidator.ValidationResult.Invalid)
        assertEquals("At least 1 person is required.", (negative as TipShareValidator.ValidationResult.Invalid).message)

        // Empty
        val empty = TipShareValidator.validatePeopleCount("")
        assertTrue(empty is TipShareValidator.ValidationResult.Invalid)
        assertEquals("Number of people is required.", (empty as TipShareValidator.ValidationResult.Invalid).message)
    }

    @Test
    fun testValidateAll() {
        // Valid Percent mode
        assertTrue(
            TipShareValidator.validateAll(
                billInput = "100.00",
                currency = usd,
                tipMode = TipMode.PERCENT,
                tipPercentageInput = "15",
                customTipInput = "0",
                peopleCountInput = "4"
            ) is TipShareValidator.ValidationResult.Valid
        )

        // Valid Amount mode
        assertTrue(
            TipShareValidator.validateAll(
                billInput = "100.00",
                currency = usd,
                tipMode = TipMode.AMOUNT,
                tipPercentageInput = "0",
                customTipInput = "20.00",
                peopleCountInput = "4"
            ) is TipShareValidator.ValidationResult.Valid
        )

        // Invalid bill in validateAll
        val invalidBill = TipShareValidator.validateAll(
            billInput = "-100",
            currency = usd,
            tipMode = TipMode.PERCENT,
            tipPercentageInput = "15",
            customTipInput = "0",
            peopleCountInput = "4"
        )
        assertTrue(invalidBill is TipShareValidator.ValidationResult.Invalid)

        // Invalid people in validateAll
        val invalidPeople = TipShareValidator.validateAll(
            billInput = "100.00",
            currency = usd,
            tipMode = TipMode.PERCENT,
            tipPercentageInput = "15",
            customTipInput = "0",
            peopleCountInput = "0"
        )
        assertTrue(invalidPeople is TipShareValidator.ValidationResult.Invalid)
    }
}
