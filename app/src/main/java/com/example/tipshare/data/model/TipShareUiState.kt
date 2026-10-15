package com.example.tipshare.data.model

/**
 * UI State for the TipShare screen.
 */
data class TipShareUiState(
    val billInput: String,
    val currency: CurrencyOption,
    val tipMode: TipMode,
    val tipPercentageInput: String,
    val customTipInput: String,
    val peopleCountInput: String,
    val result: TipShareResult?,
    val history: List<TipShareHistory>,
    val errorMessage: String?,
    val isCalculating: Boolean
)
