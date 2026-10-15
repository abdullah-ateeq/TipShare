package com.example.tipshare.domain

import com.example.tipshare.data.model.CurrencyOption
import com.example.tipshare.data.model.TipShareResult

/**
 * High-level formatting functions for TipShare money, percentages, and shareable strings.
 */
object TipShareFormatter {

    /**
     * Formats minor units into localized currency string.
     */
    fun formatMoney(minorUnits: Long, currency: CurrencyOption): String {
        return MoneyFormatter.format(minorUnits, currency, includeSymbol = true)
    }

    /**
     * Formats basis points into clean percentage string.
     * Examples: 1500 -> "15%", 1250 -> "12.5%", 1875 -> "18.75%", 0 -> "0%"
     */
    fun formatTipPercentage(basisPoints: Int): String {
        val whole = basisPoints / 100
        val frac = basisPoints % 100
        return when {
            frac == 0 -> "$whole%"
            frac % 10 == 0 -> "$whole.${frac / 10}%"
            else -> String.format(java.util.Locale.US, "%d.%02d%%", whole, frac)
        }
    }

    /**
     * Formats count of people.
     */
    fun formatPeopleCount(count: Int): String {
        return if (count == 1) "1 person" else "$count people"
    }

    /**
     * Formats full result for copying to clipboard.
     */
    fun formatCopyText(result: TipShareResult): String {
        val billFormatted = formatMoney(result.billMinorUnits, result.currency)
        val tipFormatted = formatMoney(result.tipMinorUnits, result.currency)
        val totalFormatted = formatMoney(result.totalMinorUnits, result.currency)

        val tipLine = if (result.isCustomTip) {
            val implied = TipShareCalculator.calculateImpliedBasisPoints(result.billMinorUnits, result.tipMinorUnits)
            if (implied != null) {
                "Tip: $tipFormatted (${formatTipPercentage(implied)})"
            } else {
                "Tip: $tipFormatted"
            }
        } else {
            val bp = result.tipPercentageBasisPoints ?: 0
            "Tip: ${formatTipPercentage(bp)} ($tipFormatted)"
        }

        val sb = StringBuilder()
        sb.append("TipShare Result\n\n")
        sb.append("Bill: $billFormatted\n")
        sb.append("$tipLine\n")
        sb.append("Total: $totalFormatted\n\n")
        sb.append("People: ${result.peopleCount}\n\n")

        for (i in result.perPersonMinorUnits.indices) {
            val personAmount = formatMoney(result.perPersonMinorUnits[i], result.currency)
            sb.append("Person ${i + 1}: $personAmount\n")
        }

        sb.append("\n")
        if (result.peopleCount == 1) {
            sb.append("Total to pay: $totalFormatted")
        } else {
            val allSame = result.perPersonMinorUnits.distinct().size == 1
            if (allSame) {
                sb.append("Each person pays: ${formatMoney(result.perPersonMinorUnits[0], result.currency)}")
            } else {
                sb.append("Average per person: ${formatMoney(result.totalMinorUnits / result.peopleCount, result.currency)}")
            }
        }

        return sb.toString()
    }

    /**
     * Formats result for sharing via system intent.
     */
    fun formatShareText(result: TipShareResult): String {
        val billFormatted = formatMoney(result.billMinorUnits, result.currency)
        val tipFormatted = formatMoney(result.tipMinorUnits, result.currency)
        val totalFormatted = formatMoney(result.totalMinorUnits, result.currency)

        val tipLine = if (result.isCustomTip) {
            val implied = TipShareCalculator.calculateImpliedBasisPoints(result.billMinorUnits, result.tipMinorUnits)
            if (implied != null) {
                "Tip: $tipFormatted (equivalent to ${formatTipPercentage(implied)})"
            } else {
                "Tip: $tipFormatted"
            }
        } else {
            val bp = result.tipPercentageBasisPoints ?: 0
            "Tip: ${formatTipPercentage(bp)} = $tipFormatted"
        }

        val sb = StringBuilder()
        sb.append("TipShare\n\n")
        sb.append("Bill: $billFormatted\n")
        sb.append("$tipLine\n")
        sb.append("Total: $totalFormatted\n\n")

        if (result.peopleCount == 1) {
            sb.append("1 person\n\n")
            sb.append("$totalFormatted total")
        } else {
            sb.append("${result.peopleCount} people\n\n")
            val allSame = result.perPersonMinorUnits.distinct().size == 1
            if (allSame) {
                sb.append("${formatMoney(result.perPersonMinorUnits[0], result.currency)} each")
            } else {
                for (i in result.perPersonMinorUnits.indices) {
                    sb.append("Person ${i + 1}: ${formatMoney(result.perPersonMinorUnits[i], result.currency)}\n")
                }
            }
        }

        return sb.toString()
    }
}
