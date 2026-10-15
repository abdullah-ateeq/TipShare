package com.example.tipshare.domain

import com.example.tipshare.data.model.CurrencyOption
import com.example.tipshare.data.model.TipMode

/**
 * Validates user inputs for bill amount, tip percentage, custom tip amount, and people count.
 */
object TipShareValidator {

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val message: String) : ValidationResult
    }

    /**
     * Validates bill input string.
     */
    fun validateBill(billInput: String, currency: CurrencyOption): ValidationResult {
        val trimmed = billInput.trim().replace(",", "").replace(" ", "")
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Bill amount is required.")
        }
        if (trimmed.startsWith("-")) {
            return ValidationResult.Invalid("Bill amount cannot be negative.")
        }
        val parseResult = MoneyParser.parseToMinorUnits(trimmed, currency.decimalPlaces)
        return parseResult.fold(
            onSuccess = { ValidationResult.Valid },
            onFailure = { e ->
                ValidationResult.Invalid(e.message ?: "Invalid bill amount.")
            }
        )
    }

    /**
     * Parses and validates a tip percentage input (0 to 100%), returning basis points (0..10000).
     */
    fun parseTipPercentageBasisPoints(input: String): Result<Int> {
        val trimmed = input.trim().replace("%", "").replace(" ", "")
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Tip percentage is required."))
        }
        if (trimmed.startsWith("-")) {
            return Result.failure(IllegalArgumentException("Tip percentage must be between 0% and 100%."))
        }

        val parts = trimmed.split(".")
        if (parts.size > 2) {
            return Result.failure(IllegalArgumentException("Invalid tip percentage format."))
        }

        val wholePart = parts[0]
        if (wholePart.isNotEmpty() && !wholePart.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Tip percentage must contain digits only."))
        }
        val whole = if (wholePart.isEmpty()) 0 else {
            wholePart.toIntOrNull() ?: return Result.failure(IllegalArgumentException("Tip percentage must be between 0% and 100%."))
        }

        val fractionPart = if (parts.size > 1) parts[1] else ""
        if (fractionPart.isNotEmpty() && !fractionPart.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Tip percentage must contain digits only."))
        }
        if (fractionPart.length > 2) {
            return Result.failure(IllegalArgumentException("Maximum 2 decimal places for percentage."))
        }

        val paddedFraction = fractionPart.padEnd(2, '0')
        val fraction = if (paddedFraction.isEmpty()) 0 else {
            paddedFraction.toIntOrNull() ?: return Result.failure(IllegalArgumentException("Tip percentage must be between 0% and 100%."))
        }

        val basisPoints = whole * 100 + fraction
        if (basisPoints < 0 || basisPoints > 10000) {
            return Result.failure(IllegalArgumentException("Tip percentage must be between 0% and 100%."))
        }

        return Result.success(basisPoints)
    }

    /**
     * Validates tip percentage input string.
     */
    fun validateTipPercentage(input: String): ValidationResult {
        return parseTipPercentageBasisPoints(input).fold(
            onSuccess = { ValidationResult.Valid },
            onFailure = { e ->
                ValidationResult.Invalid(e.message ?: "Tip percentage must be between 0% and 100%.")
            }
        )
    }

    /**
     * Validates custom tip input string.
     */
    fun validateCustomTip(input: String, currency: CurrencyOption): ValidationResult {
        val trimmed = input.trim().replace(",", "").replace(" ", "")
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Tip amount is required.")
        }
        if (trimmed.startsWith("-")) {
            return ValidationResult.Invalid("Tip amount cannot be negative.")
        }
        val parseResult = MoneyParser.parseToMinorUnits(trimmed, currency.decimalPlaces)
        return parseResult.fold(
            onSuccess = { ValidationResult.Valid },
            onFailure = { e ->
                ValidationResult.Invalid(e.message ?: "Invalid tip amount.")
            }
        )
    }

    /**
     * Validates people count input string (1 to 50).
     */
    fun validatePeopleCount(input: String): ValidationResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Number of people is required.")
        }
        if (trimmed.startsWith("-")) {
            return ValidationResult.Invalid("At least 1 person is required.")
        }
        val count = trimmed.toIntOrNull()
            ?: return ValidationResult.Invalid("Please enter a valid number of people.")

        return when {
            count < 1 -> ValidationResult.Invalid("At least 1 person is required.")
            count > 50 -> ValidationResult.Invalid("Maximum 50 people.")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates all inputs based on the selected tip mode.
     */
    fun validateAll(
        billInput: String,
        currency: CurrencyOption,
        tipMode: TipMode,
        tipPercentageInput: String,
        customTipInput: String,
        peopleCountInput: String
    ): ValidationResult {
        val billVal = validateBill(billInput, currency)
        if (billVal is ValidationResult.Invalid) return billVal

        when (tipMode) {
            TipMode.PERCENT -> {
                val tipVal = validateTipPercentage(tipPercentageInput)
                if (tipVal is ValidationResult.Invalid) return tipVal
            }
            TipMode.AMOUNT -> {
                val customTipVal = validateCustomTip(customTipInput, currency)
                if (customTipVal is ValidationResult.Invalid) return customTipVal
            }
        }

        val peopleVal = validatePeopleCount(peopleCountInput)
        if (peopleVal is ValidationResult.Invalid) return peopleVal

        return ValidationResult.Valid
    }
}
