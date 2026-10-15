package com.example.tipshare.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tipshare.data.model.TipShareHistory
import com.example.tipshare.domain.CurrencyRepository
import com.example.tipshare.domain.TipShareFormatter
import com.example.tipshare.ui.SettingsUiState
import com.example.tipshare.util.HapticHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    history: List<TipShareHistory>,
    settingsState: SettingsUiState,
    onHistoryItemClicked: (TipShareHistory) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var itemToDelete by remember { mutableStateOf<String?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    // Single item delete confirmation dialog
    if (itemToDelete != null) {
        if (settingsState.confirmHistoryDelete) {
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Delete Entry", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete this calculation from history?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            itemToDelete?.let { onDeleteHistoryItem(it) }
                            itemToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        } else {
            itemToDelete?.let { onDeleteHistoryItem(it) }
            itemToDelete = null
        }
    }

    // Clear all history confirmation dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear History", fontWeight = FontWeight.Bold) },
            text = { Text("Clear all recent tip calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllHistory()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top row with title and clear button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recent Calculations",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Last 10 tip splits (tap to restore)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = {
                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                        showClearAllDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear all history",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No calculations yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "When you calculate tips, your recent splits will appear here.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(history, key = { it.id }) { item ->
                    val currency = CurrencyRepository.getCurrencyByCode(item.currencyCode)
                    val billStr = TipShareFormatter.formatMoney(item.billMinorUnits, currency)
                    val tipStr = TipShareFormatter.formatMoney(item.tipMinorUnits, currency)
                    val totalMinor = item.billMinorUnits + item.tipMinorUnits
                    val perPersonBase = totalMinor / item.peopleCount
                    val perPersonStr = TipShareFormatter.formatMoney(perPersonBase, currency)

                    val tipDesc = if (item.customTip) {
                        "Custom $tipStr"
                    } else {
                        val bp = item.tipPercentageBasisPoints ?: 0
                        "${TipShareFormatter.formatTipPercentage(bp)} tip"
                    }

                    val dateFormatted = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        .format(Date(item.calculatedAt))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticHelper.performClick(view, settingsState.hapticFeedback)
                                onHistoryItemClicked(item)
                            }
                            .semantics {
                                contentDescription = "Calculation: Bill $billStr, $tipDesc, for ${item.peopleCount} people. $perPersonStr each."
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$billStr + $tipDesc · ${item.peopleCount} ${if (item.peopleCount == 1) "person" else "people"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (item.peopleCount == 1) "$perPersonStr total" else "$perPersonStr each",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }

                            IconButton(
                                onClick = {
                                    HapticHelper.performClick(view, settingsState.hapticFeedback)
                                    itemToDelete = item.id
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete calculation",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
