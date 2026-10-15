package com.example.tipshare

import com.example.tipshare.data.model.ThemeSetting
import com.example.tipshare.data.model.TipMode
import com.example.tipshare.data.model.TipShareHistory
import com.example.tipshare.data.repository.SharedPreferencesHistoryRepository
import com.example.tipshare.data.repository.SharedPreferencesSettingsRepository
import com.example.tipshare.domain.CurrencyRepository
import com.example.tipshare.testutil.FakeSharedPreferences
import com.example.tipshare.ui.TipShareViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class TipShareRepositoryAndViewModelTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var historyRepository: SharedPreferencesHistoryRepository
    private lateinit var settingsRepository: SharedPreferencesSettingsRepository
    private lateinit var viewModel: TipShareViewModel

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        historyRepository = SharedPreferencesHistoryRepository(fakePrefs)
        settingsRepository = SharedPreferencesSettingsRepository(fakePrefs)
        viewModel = TipShareViewModel(historyRepository, settingsRepository)
    }

    // 46. History save & 47. History load
    @Test
    fun testHistorySaveAndLoad() {
        val item1 = TipShareHistory(
            id = UUID.randomUUID().toString(),
            billMinorUnits = 10000L,
            tipMinorUnits = 1500L,
            currencyCode = "USD",
            currencySymbol = "$",
            tipPercentageBasisPoints = 1500,
            customTip = false,
            peopleCount = 4,
            calculatedAt = System.currentTimeMillis()
        )
        historyRepository.saveCalculation(item1)

        val history = historyRepository.getHistory()
        assertEquals(1, history.size)
        assertEquals(item1.id, history[0].id)
        assertEquals(10000L, history[0].billMinorUnits)
        assertEquals(1500L, history[0].tipMinorUnits)
        assertEquals("USD", history[0].currencyCode)
        assertEquals(4, history[0].peopleCount)
    }

    // 48. History limit (max 10, adding 11th evicts oldest)
    @Test
    fun testHistoryLimitMax10() {
        for (i in 1..12) {
            val item = TipShareHistory(
                id = "id_$i",
                billMinorUnits = (i * 1000).toLong(),
                tipMinorUnits = (i * 100).toLong(),
                currencyCode = "USD",
                currencySymbol = "$",
                tipPercentageBasisPoints = 1000,
                customTip = false,
                peopleCount = 2,
                calculatedAt = System.currentTimeMillis() + i
            )
            historyRepository.saveCalculation(item)
        }

        val history = historyRepository.getHistory()
        assertEquals(10, history.size)
        // Most recent should be id_12
        assertEquals("id_12", history[0].id)
        // Oldest preserved should be id_3 (id_1 and id_2 evicted)
        assertEquals("id_3", history[9].id)
    }

    // 49. History deletion (single item)
    @Test
    fun testHistorySingleDeletion() {
        val item1 = TipShareHistory("id_1", 1000L, 100L, "USD", "$", 1000, false, 2, 100L)
        val item2 = TipShareHistory("id_2", 2000L, 200L, "USD", "$", 1000, false, 2, 200L)
        historyRepository.saveCalculation(item1)
        historyRepository.saveCalculation(item2)

        assertEquals(2, historyRepository.getHistory().size)

        historyRepository.deleteCalculation("id_1")
        val updated = historyRepository.getHistory()
        assertEquals(1, updated.size)
        assertEquals("id_2", updated[0].id)
    }

    // 50. History clear
    @Test
    fun testHistoryClear() {
        val item1 = TipShareHistory("id_1", 1000L, 100L, "USD", "$", 1000, false, 2, 100L)
        historyRepository.saveCalculation(item1)
        assertEquals(1, historyRepository.getHistory().size)

        historyRepository.clearHistory()
        assertTrue(historyRepository.getHistory().isEmpty())
    }

    // 51. Settings persistence
    @Test
    fun testSettingsPersistence() {
        settingsRepository.setTheme(ThemeSetting.DARK)
        assertEquals(ThemeSetting.DARK, settingsRepository.getTheme())

        settingsRepository.setDynamicColorEnabled(false)
        assertFalse(settingsRepository.isDynamicColorEnabled())

        settingsRepository.setHapticFeedbackEnabled(false)
        assertFalse(settingsRepository.isHapticFeedbackEnabled())

        settingsRepository.setAnimationsEnabled(false)
        assertFalse(settingsRepository.isAnimationsEnabled())

        settingsRepository.setDefaultCurrencyCode("PKR")
        assertEquals("PKR", settingsRepository.getDefaultCurrencyCode())

        settingsRepository.setDefaultTipPercentage("20")
        assertEquals("20", settingsRepository.getDefaultTipPercentage())

        settingsRepository.setDefaultPeopleCount(6)
        assertEquals(6, settingsRepository.getDefaultPeopleCount())

        settingsRepository.setConfirmHistoryDelete(false)
        assertFalse(settingsRepository.isConfirmHistoryDelete())

        settingsRepository.setPreserveLastCalculation(false)
        assertFalse(settingsRepository.isPreserveLastCalculation())
    }

    // 52. Last-calculation persistence
    @Test
    fun testLastCalculationPersistence() {
        settingsRepository.setLastBillInput("250.75")
        settingsRepository.setLastCurrencyCode("EUR")
        settingsRepository.setLastTipMode(TipMode.AMOUNT.name)
        settingsRepository.setLastTipPercentageInput("18")
        settingsRepository.setLastCustomTipInput("35.00")
        settingsRepository.setLastPeopleCountInput("5")

        assertEquals("250.75", settingsRepository.getLastBillInput())
        assertEquals("EUR", settingsRepository.getLastCurrencyCode())
        assertEquals("AMOUNT", settingsRepository.getLastTipMode())
        assertEquals("18", settingsRepository.getLastTipPercentageInput())
        assertEquals("35.00", settingsRepository.getLastCustomTipInput())
        assertEquals("5", settingsRepository.getLastPeopleCountInput())
    }

    // 53. Reset test
    @Test
    fun testReset() {
        // Pre-populate history
        val item = TipShareHistory("hist_1", 5000L, 500L, "USD", "$", 1000, false, 2, 100L)
        historyRepository.saveCalculation(item)

        // Modify UI state away from defaults
        viewModel.onBillChanged("500.00")
        viewModel.onTipPercentageChanged("25")
        viewModel.onPeopleCountChanged("10")
        viewModel.calculateSplit()
        assertNotNull(viewModel.uiState.value.result)

        // Reset
        viewModel.reset()

        val state = viewModel.uiState.value
        assertEquals("100.00", state.billInput)
        assertEquals("USD", state.currency.code)
        assertEquals(TipMode.PERCENT, state.tipMode)
        assertEquals("15", state.tipPercentageInput)
        assertEquals("4", state.peopleCountInput)
        assertNull(state.result)
        assertNull(state.errorMessage)

        // History remains untouched!
        assertEquals(2, historyRepository.getHistory().size)
    }

    // History Restore Test
    @Test
    fun testHistoryRestore() {
        val historyItem = TipShareHistory(
            id = "restore_test",
            billMinorUnits = 12000L, // $120.00
            tipMinorUnits = 2400L,  // 20% = $24.00
            currencyCode = "USD",
            currencySymbol = "$",
            tipPercentageBasisPoints = 2000,
            customTip = false,
            peopleCount = 3,
            calculatedAt = 123456789L
        )

        viewModel.restoreFromHistory(historyItem)

        val state = viewModel.uiState.value
        assertEquals("120.00", state.billInput)
        assertEquals("USD", state.currency.code)
        assertEquals(TipMode.PERCENT, state.tipMode)
        assertEquals("20", state.tipPercentageInput)
        assertEquals("3", state.peopleCountInput)

        // Recalculates immediately
        assertNotNull(state.result)
        assertEquals(14400L, state.result?.totalMinorUnits)
        assertEquals(3, state.result?.peopleCount)
        assertEquals(listOf(4800L, 4800L, 4800L), state.result?.perPersonMinorUnits)
    }

    // 56. Quick tip presets
    @Test
    fun testQuickTipPresets() {
        viewModel.onQuickTipPresetSelected(20)
        assertEquals("20", viewModel.uiState.value.tipPercentageInput)
        assertEquals(TipMode.PERCENT, viewModel.uiState.value.tipMode)

        viewModel.onQuickTipPresetSelected(0)
        assertEquals("0", viewModel.uiState.value.tipPercentageInput)

        viewModel.onQuickTipPresetSelected(18)
        assertEquals("18", viewModel.uiState.value.tipPercentageInput)
    }

    // 57. Quick people presets
    @Test
    fun testQuickPeoplePresets() {
        viewModel.onQuickPeoplePresetSelected(1)
        assertEquals("1", viewModel.uiState.value.peopleCountInput)

        viewModel.onQuickPeoplePresetSelected(6)
        assertEquals("6", viewModel.uiState.value.peopleCountInput)

        viewModel.onQuickPeoplePresetSelected(10)
        assertEquals("10", viewModel.uiState.value.peopleCountInput)
    }

    // 58. Mode switching
    @Test
    fun testModeSwitching() {
        viewModel.onTipModeSelected(TipMode.AMOUNT)
        assertEquals(TipMode.AMOUNT, viewModel.uiState.value.tipMode)
        assertNull(viewModel.uiState.value.result)

        viewModel.onTipModeSelected(TipMode.PERCENT)
        assertEquals(TipMode.PERCENT, viewModel.uiState.value.tipMode)
        assertNull(viewModel.uiState.value.result)
    }

    // 59. Rapid calculation protection & input invalidation
    @Test
    fun testInputChangesClearResult() {
        viewModel.onBillChanged("100.00")
        viewModel.onTipPercentageChanged("15")
        viewModel.onPeopleCountChanged("4")
        viewModel.calculateSplit()

        assertNotNull(viewModel.uiState.value.result)

        // Changing bill invalidates result
        viewModel.onBillChanged("105.00")
        assertNull(viewModel.uiState.value.result)

        viewModel.calculateSplit()
        assertNotNull(viewModel.uiState.value.result)

        // Changing tip invalidates result
        viewModel.onTipPercentageChanged("20")
        assertNull(viewModel.uiState.value.result)

        viewModel.calculateSplit()
        assertNotNull(viewModel.uiState.value.result)

        // Changing people invalidates result
        viewModel.onPeopleCountChanged("5")
        assertNull(viewModel.uiState.value.result)

        viewModel.calculateSplit()
        assertNotNull(viewModel.uiState.value.result)

        // Changing currency invalidates result
        viewModel.onCurrencySelected(CurrencyRepository.EUR)
        assertNull(viewModel.uiState.value.result)
    }

    @Test
    fun testRapidDuplicateCalculationDoesNotDuplicateHistory() {
        viewModel.onBillChanged("100.00")
        viewModel.onTipPercentageChanged("15")
        viewModel.onPeopleCountChanged("4")

        viewModel.calculateSplit()
        val countAfterFirst = historyRepository.getHistory().size

        // Second identical calculation in quick succession
        viewModel.calculateSplit()
        val countAfterSecond = historyRepository.getHistory().size

        assertEquals(countAfterFirst, countAfterSecond)
    }

    private fun TipShareViewModel.onBillChanged(s: String) = onBillInputChanged(s)
}
