package com.example.tipshare.data.repository

import android.content.SharedPreferences
import com.example.tipshare.data.model.ThemeSetting

interface SettingsRepository {
    fun getTheme(): ThemeSetting
    fun setTheme(theme: ThemeSetting)

    fun isDynamicColorEnabled(): Boolean
    fun setDynamicColorEnabled(enabled: Boolean)

    fun isHapticFeedbackEnabled(): Boolean
    fun setHapticFeedbackEnabled(enabled: Boolean)

    fun isAnimationsEnabled(): Boolean
    fun setAnimationsEnabled(enabled: Boolean)

    fun getDefaultCurrencyCode(): String
    fun setDefaultCurrencyCode(code: String)

    fun getDefaultTipPercentage(): String
    fun setDefaultTipPercentage(tip: String)

    fun getDefaultPeopleCount(): Int
    fun setDefaultPeopleCount(count: Int)

    fun isConfirmHistoryDelete(): Boolean
    fun setConfirmHistoryDelete(enabled: Boolean)

    fun isPreserveLastCalculation(): Boolean
    fun setPreserveLastCalculation(enabled: Boolean)

    // Last calculation inputs for persistence
    fun getLastBillInput(): String
    fun setLastBillInput(input: String)

    fun getLastCurrencyCode(): String
    fun setLastCurrencyCode(code: String)

    fun getLastTipMode(): String
    fun setLastTipMode(mode: String)

    fun getLastTipPercentageInput(): String
    fun setLastTipPercentageInput(input: String)

    fun getLastCustomTipInput(): String
    fun setLastCustomTipInput(input: String)

    fun getLastPeopleCountInput(): String
    fun setLastPeopleCountInput(input: String)

    fun resetSettingsToDefault()
}

class SharedPreferencesSettingsRepository(
    private val prefs: SharedPreferences
) : SettingsRepository {

    companion object {
        const val KEY_THEME = "pref_theme"
        const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        const val KEY_HAPTIC_FEEDBACK = "pref_haptic_feedback"
        const val KEY_ANIMATIONS = "pref_animations"
        const val KEY_DEFAULT_CURRENCY = "pref_default_currency"
        const val KEY_DEFAULT_TIP = "pref_default_tip"
        const val KEY_DEFAULT_PEOPLE = "pref_default_people"
        const val KEY_CONFIRM_HISTORY_DELETE = "pref_confirm_history_delete"
        const val KEY_PRESERVE_LAST_CALC = "pref_preserve_last_calc"

        const val KEY_LAST_BILL = "pref_last_bill"
        const val KEY_LAST_CURRENCY = "pref_last_currency"
        const val KEY_LAST_TIP_MODE = "pref_last_tip_mode"
        const val KEY_LAST_TIP_PERCENT = "pref_last_tip_percent"
        const val KEY_LAST_CUSTOM_TIP = "pref_last_custom_tip"
        const val KEY_LAST_PEOPLE = "pref_last_people"

        const val DEFAULT_BILL = "100.00"
        const val DEFAULT_TIP_PERCENT = "15"
        const val DEFAULT_CUSTOM_TIP = "15.00"
        const val DEFAULT_PEOPLE = 4
        const val DEFAULT_CURRENCY = "USD"
        const val DEFAULT_TIP_MODE = "PERCENT"
    }

    override fun getTheme(): ThemeSetting {
        val name = prefs.getString(KEY_THEME, ThemeSetting.SYSTEM.name) ?: ThemeSetting.SYSTEM.name
        return try {
            ThemeSetting.valueOf(name)
        } catch (_: Exception) {
            ThemeSetting.SYSTEM
        }
    }

    override fun setTheme(theme: ThemeSetting) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    override fun isDynamicColorEnabled(): Boolean = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    override fun setDynamicColorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    override fun isHapticFeedbackEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
    override fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    override fun isAnimationsEnabled(): Boolean = prefs.getBoolean(KEY_ANIMATIONS, true)
    override fun setAnimationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
    }

    override fun getDefaultCurrencyCode(): String = prefs.getString(KEY_DEFAULT_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    override fun setDefaultCurrencyCode(code: String) {
        prefs.edit().putString(KEY_DEFAULT_CURRENCY, code).apply()
    }

    override fun getDefaultTipPercentage(): String = prefs.getString(KEY_DEFAULT_TIP, DEFAULT_TIP_PERCENT) ?: DEFAULT_TIP_PERCENT
    override fun setDefaultTipPercentage(tip: String) {
        prefs.edit().putString(KEY_DEFAULT_TIP, tip).apply()
    }

    override fun getDefaultPeopleCount(): Int = prefs.getInt(KEY_DEFAULT_PEOPLE, DEFAULT_PEOPLE)
    override fun setDefaultPeopleCount(count: Int) {
        prefs.edit().putInt(KEY_DEFAULT_PEOPLE, count).apply()
    }

    override fun isConfirmHistoryDelete(): Boolean = prefs.getBoolean(KEY_CONFIRM_HISTORY_DELETE, true)
    override fun setConfirmHistoryDelete(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_HISTORY_DELETE, enabled).apply()
    }

    override fun isPreserveLastCalculation(): Boolean = prefs.getBoolean(KEY_PRESERVE_LAST_CALC, true)
    override fun setPreserveLastCalculation(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRESERVE_LAST_CALC, enabled).apply()
    }

    override fun getLastBillInput(): String = prefs.getString(KEY_LAST_BILL, DEFAULT_BILL) ?: DEFAULT_BILL
    override fun setLastBillInput(input: String) {
        prefs.edit().putString(KEY_LAST_BILL, input).apply()
    }

    override fun getLastCurrencyCode(): String = prefs.getString(KEY_LAST_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    override fun setLastCurrencyCode(code: String) {
        prefs.edit().putString(KEY_LAST_CURRENCY, code).apply()
    }

    override fun getLastTipMode(): String = prefs.getString(KEY_LAST_TIP_MODE, DEFAULT_TIP_MODE) ?: DEFAULT_TIP_MODE
    override fun setLastTipMode(mode: String) {
        prefs.edit().putString(KEY_LAST_TIP_MODE, mode).apply()
    }

    override fun getLastTipPercentageInput(): String = prefs.getString(KEY_LAST_TIP_PERCENT, DEFAULT_TIP_PERCENT) ?: DEFAULT_TIP_PERCENT
    override fun setLastTipPercentageInput(input: String) {
        prefs.edit().putString(KEY_LAST_TIP_PERCENT, input).apply()
    }

    override fun getLastCustomTipInput(): String = prefs.getString(KEY_LAST_CUSTOM_TIP, DEFAULT_CUSTOM_TIP) ?: DEFAULT_CUSTOM_TIP
    override fun setLastCustomTipInput(input: String) {
        prefs.edit().putString(KEY_LAST_CUSTOM_TIP, input).apply()
    }

    override fun getLastPeopleCountInput(): String = prefs.getString(KEY_LAST_PEOPLE, DEFAULT_PEOPLE.toString()) ?: DEFAULT_PEOPLE.toString()
    override fun setLastPeopleCountInput(input: String) {
        prefs.edit().putString(KEY_LAST_PEOPLE, input).apply()
    }

    override fun resetSettingsToDefault() {
        prefs.edit()
            .putString(KEY_THEME, ThemeSetting.SYSTEM.name)
            .putBoolean(KEY_DYNAMIC_COLOR, true)
            .putBoolean(KEY_HAPTIC_FEEDBACK, true)
            .putBoolean(KEY_ANIMATIONS, true)
            .putString(KEY_DEFAULT_CURRENCY, DEFAULT_CURRENCY)
            .putString(KEY_DEFAULT_TIP, DEFAULT_TIP_PERCENT)
            .putInt(KEY_DEFAULT_PEOPLE, DEFAULT_PEOPLE)
            .putBoolean(KEY_CONFIRM_HISTORY_DELETE, true)
            .putBoolean(KEY_PRESERVE_LAST_CALC, true)
            .apply()
    }
}
