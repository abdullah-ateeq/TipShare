package com.example.tipshare.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tipshare.data.model.TipMode
import com.example.tipshare.data.model.TipShareUiState
import com.example.tipshare.domain.MoneyParser
import com.example.tipshare.domain.TipShareCalculator
import com.example.tipshare.domain.TipShareFormatter
import com.example.tipshare.ui.SettingsUiState
import com.example.tipshare.ui.components.CalculationChainCard
import com.example.tipshare.ui.components.CurrencySelectorModal
import com.example.tipshare.ui.components.ResultBreakdownCard
import com.example.tipshare.util.HapticHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: TipShareUiState,
    settingsState: SettingsUiState,
    onBillChanged: (String) -> Unit,
    onCurrencyChanged: (com.example.tipshare.data.model.CurrencyOption) -> Unit,
    onTipModeChanged: (TipMode) -> Unit,
    onTipPercentageChanged: (String) -> Unit,
    onQuickTipPresetSelected: (Int) -> Unit,
    onCustomTipChanged: (String) -> Unit,
    onPeopleCountChanged: (String) -> Unit,
    onQuickPeoplePresetSelected: (Int) -> Unit,
    onCalculateClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onNewCalculationClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showCurrencyModal by remember { mutableStateOf(false) }

    if (showCurrencyModal) {
        CurrencySelectorModal(
            selectedCurrency = uiState.currency,
            onCurrencySelected = onCurrencyChanged,
            onDismissRequest = { showCurrencyModal = false }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "TipShare",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Calculate a tip and split the total.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Bill Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL BILL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Currency Selector Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .clickable {
                                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                                    showCurrencyModal = true
                                }
                                .semantics {
                                    contentDescription = "Selected currency: ${uiState.currency.name}, ${uiState.currency.symbol}. Tap to change."
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${uiState.currency.code} (${uiState.currency.symbol})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Bill Amount Input
                        OutlinedTextField(
                            value = uiState.billInput,
                            onValueChange = onBillChanged,
                            label = { Text("Bill Amount") },
                            placeholder = { Text("100.00") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "Bill amount, ${uiState.billInput} ${uiState.currency.name}"
                                }
                        )
                    }
                }
            }

            // Tip Mode & Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TIP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.sp
                            )
                        )

                        // Segmented Tip Mode: PERCENT vs AMOUNT
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = uiState.tipMode == TipMode.PERCENT,
                                onClick = {
                                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                                    onTipModeChanged(TipMode.PERCENT)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("PERCENT")
                            }
                            SegmentedButton(
                                selected = uiState.tipMode == TipMode.AMOUNT,
                                onClick = {
                                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                                    onTipModeChanged(TipMode.AMOUNT)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("AMOUNT")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uiState.tipMode == TipMode.PERCENT) {
                        OutlinedTextField(
                            value = uiState.tipPercentageInput,
                            onValueChange = onTipPercentageChanged,
                            label = { Text("Tip Percentage") },
                            trailingIcon = { Text("%", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Tip percentage, ${uiState.tipPercentageInput} percent"
                                }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Tip Presets: 0%, 10%, 15%, 18%, 20%, 25%
                        Text(
                            text = "Quick tip presets",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val tipPresets = listOf(0, 10, 15, 18, 20, 25)
                            tipPresets.forEach { preset ->
                                val isSelected = uiState.tipPercentageInput.trim() == preset.toString()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                                        onQuickTipPresetSelected(preset)
                                    },
                                    label = { Text("$preset%") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    } else {
                        // Custom Tip Amount Mode
                        OutlinedTextField(
                            value = uiState.customTipInput,
                            onValueChange = onCustomTipChanged,
                            label = { Text("Tip Amount (${uiState.currency.symbol})") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Custom tip amount, ${uiState.customTipInput} ${uiState.currency.name}"
                                }
                        )

                        // Secondary Implied Tip Percentage
                        val billParsed = MoneyParser.parseToMinorUnits(uiState.billInput, uiState.currency.decimalPlaces).getOrNull()
                        val tipParsed = MoneyParser.parseToMinorUnits(uiState.customTipInput, uiState.currency.decimalPlaces).getOrNull()
                        if (billParsed != null && tipParsed != null && billParsed > 0L) {
                            val implied = TipShareCalculator.calculateImpliedBasisPoints(billParsed, tipParsed)
                            if (implied != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Equivalent to ${TipShareFormatter.formatTipPercentage(implied)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // People Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NUMBER OF PEOPLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.peopleCountInput,
                        onValueChange = onPeopleCountChanged,
                        label = { Text("People (1 to 50)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Number of people, ${uiState.peopleCountInput}"
                            }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick People Presets: 1, 2, 3, 4, 5, 6, 8, 10
                    Text(
                        text = "Quick split presets",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val peoplePresets = listOf(1, 2, 3, 4, 5, 6, 8, 10)
                        peoplePresets.forEach { count ->
                            val isSelected = uiState.peopleCountInput.trim() == count.toString()
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                                    onQuickPeoplePresetSelected(count)
                                },
                                label = { Text(if (count == 1) "Solo (1)" else "$count") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Error Message (if any)
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Primary Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                        onCalculateClicked()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(54.dp)
                        .semantics { contentDescription = "Calculate tip and split" }
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CALCULATE SPLIT",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = {
                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                        onResetClicked()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESET")
                }
            }

            // Result Display Section (when calculated)
            if (uiState.result != null) {
                Spacer(modifier = Modifier.height(8.dp))

                // Hero & Breakdown Card
                ResultBreakdownCard(
                    result = uiState.result,
                    onCopyClicked = {
                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                        val text = TipShareFormatter.formatCopyText(uiState.result)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("TipShare Result", text))
                        scope.launch {
                            snackbarHostState.showSnackbar("Result copied to clipboard")
                        }
                    },
                    onShareClicked = {
                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                        val text = TipShareFormatter.formatShareText(uiState.result)
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share TipShare")
                        context.startActivity(shareIntent)
                    },
                    onNewCalculationClicked = {
                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                        onNewCalculationClicked()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Visual Calculation Chain Card
                CalculationChainCard(
                    result = uiState.result,
                    animationsEnabled = settingsState.animations
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
