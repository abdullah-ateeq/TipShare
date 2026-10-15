package com.example.tipshare.data.model

/**
 * Historical record of a tip calculation.
 */
data class TipShareHistory(
    val id: String,
    val billMinorUnits: Long,
    val tipMinorUnits: Long,
    val currencyCode: String,
    val currencySymbol: String,
    val tipPercentageBasisPoints: Int?,
    val customTip: Boolean,
    val peopleCount: Int,
    val calculatedAt: Long
)
