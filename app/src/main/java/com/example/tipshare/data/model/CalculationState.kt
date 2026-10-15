package com.example.tipshare.data.model

/**
 * Lifecycle status of the current calculation.
 */
sealed interface CalculationState {
    data object Idle : CalculationState
    data object Calculated : CalculationState
    data class Error(val message: String) : CalculationState
}
