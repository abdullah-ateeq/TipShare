package com.example.tipshare.domain

import com.example.tipshare.data.model.CurrencyOption

/**
 * Repository providing predefined currency options and lookups.
 */
object CurrencyRepository {
    val USD = CurrencyOption(code = "USD", symbol = "$", name = "US Dollar", decimalPlaces = 2)
    val EUR = CurrencyOption(code = "EUR", symbol = "€", name = "Euro", decimalPlaces = 2)
    val GBP = CurrencyOption(code = "GBP", symbol = "£", name = "British Pound", decimalPlaces = 2)
    val PKR = CurrencyOption(code = "PKR", symbol = "Rs.", name = "Pakistani Rupee", decimalPlaces = 2)
    val AED = CurrencyOption(code = "AED", symbol = "AED", name = "UAE Dirham", decimalPlaces = 2)
    val SAR = CurrencyOption(code = "SAR", symbol = "SAR", name = "Saudi Riyal", decimalPlaces = 2)
    val CAD = CurrencyOption(code = "CAD", symbol = "CA$", name = "Canadian Dollar", decimalPlaces = 2)
    val AUD = CurrencyOption(code = "AUD", symbol = "AU$", name = "Australian Dollar", decimalPlaces = 2)
    val INR = CurrencyOption(code = "INR", symbol = "₹", name = "Indian Rupee", decimalPlaces = 2)
    val JPY = CurrencyOption(code = "JPY", symbol = "¥", name = "Japanese Yen", decimalPlaces = 0)
    val CNY = CurrencyOption(code = "CNY", symbol = "¥", name = "Chinese Yuan", decimalPlaces = 2)
    val OTHER = CurrencyOption(code = "OTHER", symbol = "¤", name = "Other / Custom", decimalPlaces = 2)

    val supportedCurrencies: List<CurrencyOption> = listOf(
        USD, EUR, GBP, PKR, AED, SAR, CAD, AUD, INR, JPY, CNY, OTHER
    )

    fun getDefaultCurrency(): CurrencyOption = USD

    fun getCurrencyByCode(code: String): CurrencyOption {
        return supportedCurrencies.find { it.code.equals(code, ignoreCase = true) } ?: USD
    }
}
