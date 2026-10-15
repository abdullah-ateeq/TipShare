package com.example.tipshare.data.model

/**
 * Result of a tip calculation and equal split among people.
 * All monetary amounts are represented as integer minor units (e.g. cents).
 */
data class TipShareResult(
    val billMinorUnits: Long,
    val tipMinorUnits: Long,
    val totalMinorUnits: Long,
    val peopleCount: Int,
    val perPersonMinorUnits: List<Long>,
    val currency: CurrencyOption,
    val tipPercentageBasisPoints: Int?,
    val isCustomTip: Boolean,
    val roundingAdjustmentMinorUnits: Long
)
