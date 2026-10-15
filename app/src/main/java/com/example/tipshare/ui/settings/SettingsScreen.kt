package com.example.tipshare.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tipshare.data.model.CurrencyOption
import com.example.tipshare.data.model.ThemeSetting
import com.example.tipshare.ui.SettingsUiState
import com.example.tipshare.ui.components.CurrencySelectorModal
import com.example.tipshare.util.HapticHelper

@Composable
fun SettingsScreen(
    settingsState: SettingsUiState,
    onThemeChanged: (ThemeSetting) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onHapticFeedbackChanged: (Boolean) -> Unit,
    onAnimationsChanged: (Boolean) -> Unit,
    onDefaultCurrencyChanged: (CurrencyOption) -> Unit,
    onDefaultTipChanged: (String) -> Unit,
    onDefaultPeopleChanged: (Int) -> Unit,
    onConfirmHistoryDeleteChanged: (Boolean) -> Unit,
    onPreserveLastCalculationChanged: (Boolean) -> Unit,
    onClearAllHistory: () -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showCurrencyModal by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    if (showCurrencyModal) {
        CurrencySelectorModal(
            selectedCurrency = settingsState.defaultCurrency,
            onCurrencySelected = onDefaultCurrencyChanged,
            onDismissRequest = { showCurrencyModal = false }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History", fontWeight = FontWeight.Bold) },
            text = { Text("Clear all recent tip calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings", fontWeight = FontWeight.Bold) },
            text = { Text("Restore all settings to their original defaults?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetSettings()
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Preferences and application defaults",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.outline
                )
            )
        }

        // Appearance Card
        SettingsGroupCard(title = "APPEARANCE") {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    ThemeSetting.SYSTEM to "System",
                    ThemeSetting.LIGHT to "Light",
                    ThemeSetting.DARK to "Dark"
                ).forEach { (setting, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            HapticHelper.performClick(view, settingsState.hapticFeedback)
                            onThemeChanged(setting)
                        }
                    ) {
                        RadioButton(
                            selected = settingsState.theme == setting,
                            onClick = {
                                HapticHelper.performClick(view, settingsState.hapticFeedback)
                                onThemeChanged(setting)
                            }
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSwitchRow(
                title = "Dynamic Color",
                subtitle = "Match system Material You colors (Android 12+)",
                checked = settingsState.dynamicColor,
                onCheckedChange = {
                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                    onDynamicColorChanged(it)
                }
            )

            SettingsSwitchRow(
                title = "Animations",
                subtitle = "Subtle visual calculation flow",
                checked = settingsState.animations,
                onCheckedChange = {
                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                    onAnimationsChanged(it)
                }
            )

            SettingsSwitchRow(
                title = "Haptic Feedback",
                subtitle = "Subtle vibration on button taps",
                checked = settingsState.hapticFeedback,
                onCheckedChange = {
                    HapticHelper.performClick(view, !settingsState.hapticFeedback)
                    onHapticFeedbackChanged(it)
                }
            )
        }

        // Calculation Defaults Card
        SettingsGroupCard(title = "CALCULATION DEFAULTS") {
            // Default Currency
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                        showCurrencyModal = true
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Default Currency",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${settingsState.defaultCurrency.name} (${settingsState.defaultCurrency.symbol})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = settingsState.defaultCurrency.code,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Default Tip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Default Tip",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Initial tip percentage on reset",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("10", "15", "18", "20").forEach { tip ->
                        val isSelected = settingsState.defaultTipPercentage == tip
                        TextButton(
                            onClick = {
                                HapticHelper.performClick(view, settingsState.hapticFeedback)
                                onDefaultTipChanged(tip)
                            },
                            colors = if (isSelected) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.textButtonColors()
                        ) {
                            Text("$tip%")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Default People
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Default People",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Initial number of people on reset",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 2, 4, 6).forEach { count ->
                        val isSelected = settingsState.defaultPeopleCount == count
                        TextButton(
                            onClick = {
                                HapticHelper.performClick(view, settingsState.hapticFeedback)
                                onDefaultPeopleChanged(count)
                            },
                            colors = if (isSelected) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.textButtonColors()
                        ) {
                            Text(count.toString())
                        }
                    }
                }
            }
        }

        // Behavior Card
        SettingsGroupCard(title = "BEHAVIOR & DATA") {
            SettingsSwitchRow(
                title = "Preserve Last Calculation",
                subtitle = "Restore bill, tip, and people when reopening app",
                checked = settingsState.preserveLastCalculation,
                onCheckedChange = {
                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                    onPreserveLastCalculationChanged(it)
                }
            )

            SettingsSwitchRow(
                title = "Confirm History Delete",
                subtitle = "Ask for confirmation before deleting calculations",
                checked = settingsState.confirmHistoryDelete,
                onCheckedChange = {
                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                    onConfirmHistoryDeleteChanged(it)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                    showClearHistoryDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear History", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                    showResetSettingsDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
