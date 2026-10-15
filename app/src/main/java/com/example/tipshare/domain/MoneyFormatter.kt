package com.example.tipshare.domain

import com.example.tipshare.data.model.CurrencyOption
import java.util.Locale

/**
 * Formats minor units into properly localized and styled currency strings.
 */
object MoneyFormatter {
    /**
     * Formats integer minor units with optional currency symbol and standard thousand separators.
     * Examples: $100.00, Rs. 5,000.00, ¥1,250
     */
    fun format(
        minorUnits: Long,
        currency: CurrencyOption,
        includeSymbol: Boolean = true
    ): String {
        val decimalPlaces = currency.decimalPlaces
        val isNegative = minorUnits < 0
        val absMinorUnits = Math.abs(minorUnits)

        val formattedNumber = if (decimalPlaces == 0) {
            String.format(Locale.US, "%,d", absMinorUnits)
        } else {
            var divisor = 1L
            for (i in 0 until decimalPlaces) {
                divisor = Math.multiplyExact(divisor, 10L)
            }
            val wholePart = absMinorUnits / divisor
            val fractionPart = absMinorUnits % divisor
            val wholeStr = String.format(Locale.US, "%,d", wholePart)
            val fractionStr = String.format(Locale.US, "%0${decimalPlaces}d", fractionPart)
            "$wholeStr.$fractionStr"
        }

        val prefix = if (isNegative) "-" else ""
        if (!includeSymbol) {
            return prefix + formattedNumber
        }

        val symbol = currency.symbol
        val spacer = if (symbol.length > 1) " " else ""
        return "$prefix$symbol$spacer$formattedNumber"
    }
}
