package com.example.tipshare.data.model

/**
 * Represents a supported currency with its display symbol and minor unit precision.
 */
data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String,
    val decimalPlaces: Int
)
