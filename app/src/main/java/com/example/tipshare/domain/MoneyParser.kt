package com.example.tipshare.domain

/**
 * Parses user input strings into monetary minor units (e.g. cents) safely without floating-point errors.
 */
object MoneyParser {
    // Maximum allowable major unit value to avoid integer overflow
    const val MAX_MAJOR_UNITS: Long = 100_000_000L

    /**
     * Parses a string input into integer minor units according to the currency's decimal places.
     */
    fun parseToMinorUnits(input: String, decimalPlaces: Int): Result<Long> {
        val trimmed = input.trim().replace(",", "").replace(" ", "")
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Amount cannot be empty"))
        }
        if (trimmed.startsWith("-")) {
            return Result.failure(IllegalArgumentException("Negative amounts are not allowed"))
        }

        val parts = trimmed.split(".")
        if (parts.size > 2) {
            return Result.failure(IllegalArgumentException("Invalid decimal format"))
        }

        val wholePartStr = parts[0]
        if (wholePartStr.isNotEmpty() && !wholePartStr.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Amount must contain digits only"))
        }

        val whole = if (wholePartStr.isEmpty()) 0L else {
            wholePartStr.toLongOrNull() ?: return Result.failure(IllegalArgumentException("Amount is too large"))
        }

        if (whole > MAX_MAJOR_UNITS) {
            return Result.failure(IllegalArgumentException("Amount is too large"))
        }

        if (decimalPlaces == 0) {
            if (parts.size > 1 && parts[1].isNotEmpty() && parts[1].any { it != '0' }) {
                return Result.failure(IllegalArgumentException("Decimals are not supported for this currency"))
            }
            return Result.success(whole)
        }

        val fractionStr = if (parts.size > 1) parts[1] else ""
        if (!fractionStr.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Fraction must contain digits only"))
        }

        if (fractionStr.length > decimalPlaces) {
            return Result.failure(IllegalArgumentException("Maximum $decimalPlaces decimal places allowed"))
        }

        val paddedFractionStr = fractionStr.padEnd(decimalPlaces, '0')
        val fraction = if (paddedFractionStr.isEmpty()) 0L else {
            paddedFractionStr.toLongOrNull() ?: return Result.failure(IllegalArgumentException("Amount is too large"))
        }

        var multiplier = 1L
        for (i in 0 until decimalPlaces) {
            multiplier = Math.multiplyExact(multiplier, 10L)
        }

        return try {
            val totalMinorUnits = Math.addExact(Math.multiplyExact(whole, multiplier), fraction)
            Result.success(totalMinorUnits)
        } catch (e: ArithmeticException) {
            Result.failure(IllegalArgumentException("Amount is too large"))
        }
    }
}
