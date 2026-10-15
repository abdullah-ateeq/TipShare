package com.example.tipshare.domain

import com.example.tipshare.data.model.CurrencyOption
import com.example.tipshare.data.model.TipShareResult

/**
 * Core calculator for TipShare.
 *
 * Implements strict integer minor-unit math with standard half-up monetary rounding,
 * deterministic split remainder distribution, and overflow protection.
 */
object TipShareCalculator {
    const val BASIS_POINTS_DIVISOR = 10000L

    /**
     * Calculates the tip, final total, and equal division among people.
     *
     * @param billMinorUnits Original bill in minor units (e.g. cents). Must be >= 0.
     * @param tipPercentageBasisPoints Tip percentage in basis points (100% = 10000). 0..10000.
     * @param customTipMinorUnits Custom tip amount in minor units. Must be >= 0.
     * @param peopleCount Number of people splitting the bill. Must be in 1..50.
     * @param currency Currency option with decimal precision.
     */
    fun calculate(
        billMinorUnits: Long,
        tipPercentageBasisPoints: Int?,
        customTipMinorUnits: Long?,
        peopleCount: Int,
        currency: CurrencyOption
    ): TipShareResult {
        require(billMinorUnits >= 0) { "Bill amount cannot be negative." }
        require(peopleCount >= 1) { "At least 1 person is required." }
        require(peopleCount <= 50) { "Maximum 50 people." }

        val isCustomTip = customTipMinorUnits != null
        val tipMinorUnits: Long = if (isCustomTip) {
            val customTip = customTipMinorUnits ?: 0L
            require(customTip >= 0) { "Tip amount cannot be negative." }
            customTip
        } else {
            val basisPoints = tipPercentageBasisPoints ?: 0
            require(basisPoints in 0..10000) { "Tip percentage must be between 0% and 100%." }
            if (billMinorUnits == 0L || basisPoints == 0) {
                0L
            } else {
                // Half-up monetary rounding: (numerator + divisor / 2) / divisor
                val numerator = Math.multiplyExact(billMinorUnits, basisPoints.toLong())
                val rounded = (numerator + (BASIS_POINTS_DIVISOR / 2L)) / BASIS_POINTS_DIVISOR
                rounded
            }
        }

        val totalMinorUnits = Math.addExact(billMinorUnits, tipMinorUnits)

        val base = totalMinorUnits / peopleCount
        val remainder = (totalMinorUnits % peopleCount).toInt()

        val perPersonMinorUnits = ArrayList<Long>(peopleCount)
        var sum = 0L
        for (i in 0 until peopleCount) {
            val amount = if (i < remainder) {
                Math.addExact(base, 1L)
            } else {
                base
            }
            perPersonMinorUnits.add(amount)
            sum = Math.addExact(sum, amount)
        }

        // Strict invariant check: sum of per-person allocations must equal total exactly
        check(sum == totalMinorUnits) {
            "Allocation invariant violated: sum($sum) != total($totalMinorUnits)"
        }

        return TipShareResult(
            billMinorUnits = billMinorUnits,
            tipMinorUnits = tipMinorUnits,
            totalMinorUnits = totalMinorUnits,
            peopleCount = peopleCount,
            perPersonMinorUnits = perPersonMinorUnits,
            currency = currency,
            tipPercentageBasisPoints = tipPercentageBasisPoints,
            isCustomTip = isCustomTip,
            roundingAdjustmentMinorUnits = remainder.toLong()
        )
    }

    /**
     * Calculates the implied tip percentage in basis points (1 unit = 0.01%) for a custom tip amount.
     * Returns null if bill is zero.
     */
    fun calculateImpliedBasisPoints(billMinorUnits: Long, tipMinorUnits: Long): Int? {
        if (billMinorUnits <= 0L) return null
        return try {
            val numerator = Math.multiplyExact(tipMinorUnits, 10000L)
            // Round to nearest basis point
            val rounded = (numerator + (billMinorUnits / 2L)) / billMinorUnits
            rounded.toInt()
        } catch (e: ArithmeticException) {
            null
        }
    }
}
