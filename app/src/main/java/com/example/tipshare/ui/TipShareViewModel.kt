package com.example.tipshare.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tipshare.data.model.CalculationState
import com.example.tipshare.data.model.CurrencyOption
import com.example.tipshare.data.model.ThemeSetting
import com.example.tipshare.data.model.TipMode
import com.example.tipshare.data.model.TipShareHistory
import com.example.tipshare.data.model.TipShareResult
import com.example.tipshare.data.model.TipShareUiState
import com.example.tipshare.data.repository.SettingsRepository
import com.example.tipshare.data.repository.SharedPreferencesHistoryRepository
import com.example.tipshare.data.repository.SharedPreferencesSettingsRepository
import com.example.tipshare.data.repository.TipShareHistoryRepository
import com.example.tipshare.domain.CurrencyRepository
import com.example.tipshare.domain.MoneyParser
import com.example.tipshare.domain.TipShareCalculator
import com.example.tipshare.domain.TipShareValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SettingsUiState(
    val theme: ThemeSetting = ThemeSetting.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val defaultCurrency: CurrencyOption = CurrencyRepository.USD,
    val defaultTipPercentage: String = "15",
    val defaultPeopleCount: Int = 4,
    val confirmHistoryDelete: Boolean = true,
    val preserveLastCalculation: Boolean = true
)

class TipShareViewModel(
    private val historyRepository: TipShareHistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TipShareUiState(
            billInput = "100.00",
            currency = CurrencyRepository.USD,
            tipMode = TipMode.PERCENT,
            tipPercentageInput = "15",
            customTipInput = "15.00",
            peopleCountInput = "4",
            result = null,
            history = emptyList(),
            errorMessage = null,
            isCalculating = false
        )
    )
    val uiState: StateFlow<TipShareUiState> = _uiState.asStateFlow()

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()

    private val _calculationState = MutableStateFlow<CalculationState>(CalculationState.Idle)
    val calculationState: StateFlow<CalculationState> = _calculationState.asStateFlow()

    init {
        loadSettingsAndState()
    }

    private fun loadSettingsAndState() {
        val theme = settingsRepository.getTheme()
        val dynamicColor = settingsRepository.isDynamicColorEnabled()
        val haptic = settingsRepository.isHapticFeedbackEnabled()
        val anim = settingsRepository.isAnimationsEnabled()
        val defCurrCode = settingsRepository.getDefaultCurrencyCode()
        val defCurr = CurrencyRepository.getCurrencyByCode(defCurrCode)
        val defTip = settingsRepository.getDefaultTipPercentage()
        val defPeople = settingsRepository.getDefaultPeopleCount()
        val confirmDel = settingsRepository.isConfirmHistoryDelete()
        val preserveCalc = settingsRepository.isPreserveLastCalculation()

        _settingsState.value = SettingsUiState(
            theme = theme,
            dynamicColor = dynamicColor,
            hapticFeedback = haptic,
            animations = anim,
            defaultCurrency = defCurr,
            defaultTipPercentage = defTip,
            defaultPeopleCount = defPeople,
            confirmHistoryDelete = confirmDel,
            preserveLastCalculation = preserveCalc
        )

        val historyList = historyRepository.getHistory()

        if (preserveCalc) {
            val lastBill = settingsRepository.getLastBillInput()
            val lastCurrCode = settingsRepository.getLastCurrencyCode()
            val lastCurr = CurrencyRepository.getCurrencyByCode(lastCurrCode)
            val lastTipModeStr = settingsRepository.getLastTipMode()
            val lastTipMode = try {
                TipMode.valueOf(lastTipModeStr)
            } catch (_: Exception) {
                TipMode.PERCENT
            }
            val lastTipPercent = settingsRepository.getLastTipPercentageInput()
            val lastCustomTip = settingsRepository.getLastCustomTipInput()
            val lastPeople = settingsRepository.getLastPeopleCountInput()

            _uiState.value = TipShareUiState(
                billInput = lastBill,
                currency = lastCurr,
                tipMode = lastTipMode,
                tipPercentageInput = lastTipPercent,
                customTipInput = lastCustomTip,
                peopleCountInput = lastPeople,
                result = null,
                history = historyList,
                errorMessage = null,
                isCalculating = false
            )
        } else {
            _uiState.value = TipShareUiState(
                billInput = "100.00",
                currency = defCurr,
                tipMode = TipMode.PERCENT,
                tipPercentageInput = defTip,
                customTipInput = "15.00",
                peopleCountInput = defPeople.toString(),
                result = null,
                history = historyList,
                errorMessage = null,
                isCalculating = false
            )
        }
    }

    fun onBillInputChanged(input: String) {
        _uiState.update {
            it.copy(
                billInput = input,
                result = null,
                errorMessage = null
            )
        }
        _calculationState.value = CalculationState.Idle
        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastBillInput(input)
        }
    }

    fun onCurrencySelected(currency: CurrencyOption) {
        _uiState.update {
            it.copy(
                currency = currency,
                result = null,
                errorMessage = null
            )
        }
        _calculationState.value = CalculationState.Idle
        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastCurrencyCode(currency.code)
        }
    }

    fun onTipModeSelected(mode: TipMode) {
        _uiState.update {
            it.copy(
                tipMode = mode,
                result = null,
                errorMessage = null
            )
        }
        _calculationState.value = CalculationState.Idle
        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastTipMode(mode.name)
        }
    }

    fun onTipPercentageChanged(input: String) {
        _uiState.update {
            it.copy(
                tipPercentageInput = input,
                result = null,
                errorMessage = null
            )
        }
        _calculationState.value = CalculationState.Idle
        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastTipPercentageInput(input)
        }
    }

    fun onQuickTipPresetSelected(percentage: Int) {
        val text = percentage.toString()
        _uiState.update {
            it.copy(
                tipMode = TipMode.PERCENT,
                tipPercentageInput = text,
                result = null,
                errorMessage = null
            )
        }
        _calculationState.value = CalculationState.Idle
        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastTipMode(TipMode.PERCENT.name)
            settingsRepository.setLastTipPercentageInput(text)
        }
    }

    fun onCustomTipAmountChanged(input: String) {
        _uiState.update {
            it.copy(
                customTipInput = input,
                result = null,
                errorMessage = null
            )
        }
        _calculationState.value = CalculationState.Idle
        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastCustomTipInput(input)
        }
    }

    fun onPeopleCountChanged(input: String) {
        _uiState.update {
            it.copy(
                peopleCountInput = input,
                result = null,
                errorMessage = null
            )
        }
        _calculationState.value = CalculationState.Idle
        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastPeopleCountInput(input)
        }
    }

    fun onQuickPeoplePresetSelected(count: Int) {
        val text = count.toString()
        _uiState.update {
            it.copy(
                peopleCountInput = text,
                result = null,
                errorMessage = null
            )
        }
        _calculationState.value = CalculationState.Idle
        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastPeopleCountInput(text)
        }
    }

    fun calculateSplit() {
        val state = _uiState.value
        val validation = TipShareValidator.validateAll(
            billInput = state.billInput,
            currency = state.currency,
            tipMode = state.tipMode,
            tipPercentageInput = state.tipPercentageInput,
            customTipInput = state.customTipInput,
            peopleCountInput = state.peopleCountInput
        )

        if (validation is TipShareValidator.ValidationResult.Invalid) {
            _uiState.update { it.copy(errorMessage = validation.message, result = null) }
            _calculationState.value = CalculationState.Error(validation.message)
            return
        }

        try {
            val billMinorUnits = MoneyParser.parseToMinorUnits(state.billInput, state.currency.decimalPlaces)
                .getOrThrow()

            val tipBasisPoints: Int?
            val customTipMinorUnits: Long?

            when (state.tipMode) {
                TipMode.PERCENT -> {
                    tipBasisPoints = TipShareValidator.parseTipPercentageBasisPoints(state.tipPercentageInput)
                        .getOrThrow()
                    customTipMinorUnits = null
                }
                TipMode.AMOUNT -> {
                    tipBasisPoints = null
                    customTipMinorUnits = MoneyParser.parseToMinorUnits(state.customTipInput, state.currency.decimalPlaces)
                        .getOrThrow()
                }
            }

            val peopleCount = state.peopleCountInput.trim().toInt()

            val result = TipShareCalculator.calculate(
                billMinorUnits = billMinorUnits,
                tipPercentageBasisPoints = tipBasisPoints,
                customTipMinorUnits = customTipMinorUnits,
                peopleCount = peopleCount,
                currency = state.currency
            )

            val historyItem = TipShareHistory(
                id = UUID.randomUUID().toString(),
                billMinorUnits = result.billMinorUnits,
                tipMinorUnits = result.tipMinorUnits,
                currencyCode = result.currency.code,
                currencySymbol = result.currency.symbol,
                tipPercentageBasisPoints = result.tipPercentageBasisPoints,
                customTip = result.isCustomTip,
                peopleCount = result.peopleCount,
                calculatedAt = System.currentTimeMillis()
            )

            historyRepository.saveCalculation(historyItem)
            val updatedHistory = historyRepository.getHistory()

            _uiState.update {
                it.copy(
                    result = result,
                    history = updatedHistory,
                    errorMessage = null,
                    isCalculating = false
                )
            }
            _calculationState.value = CalculationState.Calculated

            if (settingsRepository.isPreserveLastCalculation()) {
                settingsRepository.setLastBillInput(state.billInput)
                settingsRepository.setLastCurrencyCode(state.currency.code)
                settingsRepository.setLastTipMode(state.tipMode.name)
                settingsRepository.setLastTipPercentageInput(state.tipPercentageInput)
                settingsRepository.setLastCustomTipInput(state.customTipInput)
                settingsRepository.setLastPeopleCountInput(state.peopleCountInput)
            }
        } catch (e: Exception) {
            val msg = e.message ?: "An unexpected calculation error occurred."
            _uiState.update { it.copy(errorMessage = msg, result = null) }
            _calculationState.value = CalculationState.Error(msg)
        }
    }

    fun reset() {
        val defCurr = settingsRepository.getDefaultCurrencyCode()
            .let { CurrencyRepository.getCurrencyByCode(it) }
        val defTip = settingsRepository.getDefaultTipPercentage()
        val defPeople = settingsRepository.getDefaultPeopleCount()

        _uiState.update {
            it.copy(
                billInput = "100.00",
                currency = defCurr,
                tipMode = TipMode.PERCENT,
                tipPercentageInput = defTip,
                customTipInput = "15.00",
                peopleCountInput = defPeople.toString(),
                result = null,
                errorMessage = null,
                isCalculating = false
            )
        }
        _calculationState.value = CalculationState.Idle

        if (settingsRepository.isPreserveLastCalculation()) {
            settingsRepository.setLastBillInput("100.00")
            settingsRepository.setLastCurrencyCode(defCurr.code)
            settingsRepository.setLastTipMode(TipMode.PERCENT.name)
            settingsRepository.setLastTipPercentageInput(defTip)
            settingsRepository.setLastCustomTipInput("15.00")
            settingsRepository.setLastPeopleCountInput(defPeople.toString())
        }
    }

    fun newCalculation() {
        _uiState.update {
            it.copy(
                result = null,
                errorMessage = null,
                isCalculating = false
            )
        }
        _calculationState.value = CalculationState.Idle
    }

    fun restoreFromHistory(item: TipShareHistory) {
        val curr = CurrencyRepository.getCurrencyByCode(item.currencyCode)
        val mode = if (item.customTip) TipMode.AMOUNT else TipMode.PERCENT

        val billString = if (curr.decimalPlaces == 0) {
            item.billMinorUnits.toString()
        } else {
            var div = 1L
            for (i in 0 until curr.decimalPlaces) div *= 10L
            val whole = item.billMinorUnits / div
            val frac = item.billMinorUnits % div
            String.format(java.util.Locale.US, "%d.%0${curr.decimalPlaces}d", whole, frac)
        }

        val tipPercentString = if (item.tipPercentageBasisPoints != null) {
            val whole = item.tipPercentageBasisPoints / 100
            val frac = item.tipPercentageBasisPoints % 100
            if (frac == 0) "$whole" else if (frac % 10 == 0) "$whole.${frac / 10}" else "$whole.$frac"
        } else {
            "15"
        }

        val customTipString = if (item.customTip) {
            if (curr.decimalPlaces == 0) {
                item.tipMinorUnits.toString()
            } else {
                var div = 1L
                for (i in 0 until curr.decimalPlaces) div *= 10L
                val whole = item.tipMinorUnits / div
                val frac = item.tipMinorUnits % div
                String.format(java.util.Locale.US, "%d.%0${curr.decimalPlaces}d", whole, frac)
            }
        } else {
            "15.00"
        }

        _uiState.update {
            it.copy(
                billInput = billString,
                currency = curr,
                tipMode = mode,
                tipPercentageInput = tipPercentString,
                customTipInput = customTipString,
                peopleCountInput = item.peopleCount.toString(),
                result = null,
                errorMessage = null
            )
        }

        // Immediately recalculate
        calculateSplit()
    }

    fun deleteHistoryItem(id: String) {
        historyRepository.deleteCalculation(id)
        _uiState.update {
            it.copy(history = historyRepository.getHistory())
        }
    }

    fun clearAllHistory() {
        historyRepository.clearHistory()
        _uiState.update {
            it.copy(history = emptyList())
        }
    }

    fun updateTheme(theme: ThemeSetting) {
        settingsRepository.setTheme(theme)
        _settingsState.update { it.copy(theme = theme) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        settingsRepository.setDynamicColorEnabled(enabled)
        _settingsState.update { it.copy(dynamicColor = enabled) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        settingsRepository.setHapticFeedbackEnabled(enabled)
        _settingsState.update { it.copy(hapticFeedback = enabled) }
    }

    fun updateAnimations(enabled: Boolean) {
        settingsRepository.setAnimationsEnabled(enabled)
        _settingsState.update { it.copy(animations = enabled) }
    }

    fun updateDefaultCurrency(currency: CurrencyOption) {
        settingsRepository.setDefaultCurrencyCode(currency.code)
        _settingsState.update { it.copy(defaultCurrency = currency) }
    }

    fun updateDefaultTip(tipPercentage: String) {
        settingsRepository.setDefaultTipPercentage(tipPercentage)
        _settingsState.update { it.copy(defaultTipPercentage = tipPercentage) }
    }

    fun updateDefaultPeople(peopleCount: Int) {
        settingsRepository.setDefaultPeopleCount(peopleCount)
        _settingsState.update { it.copy(defaultPeopleCount = peopleCount) }
    }

    fun updateConfirmHistoryDelete(enabled: Boolean) {
        settingsRepository.setConfirmHistoryDelete(enabled)
        _settingsState.update { it.copy(confirmHistoryDelete = enabled) }
    }

    fun updatePreserveLastCalculation(enabled: Boolean) {
        settingsRepository.setPreserveLastCalculation(enabled)
        _settingsState.update { it.copy(preserveLastCalculation = enabled) }
    }

    fun resetSettingsToDefault() {
        settingsRepository.resetSettingsToDefault()
        loadSettingsAndState()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val prefs = context.applicationContext.getSharedPreferences("tipshare_prefs", Context.MODE_PRIVATE)
                    val historyRepo = SharedPreferencesHistoryRepository(prefs)
                    val settingsRepo = SharedPreferencesSettingsRepository(prefs)
                    return TipShareViewModel(historyRepo, settingsRepo) as T
                }
            }
        }
    }
}
