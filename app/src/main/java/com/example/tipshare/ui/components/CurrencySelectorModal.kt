package com.example.tipshare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tipshare.data.model.CurrencyOption
import com.example.tipshare.domain.CurrencyRepository

@Composable
fun CurrencySelectorModal(
    selectedCurrency: CurrencyOption,
    onCurrencySelected: (CurrencyOption) -> Unit,
    onDismissRequest: () -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    if (showCustomDialog) {
        CustomCurrencyDialog(
            initialCurrency = selectedCurrency,
            onDismiss = { showCustomDialog = false },
            onConfirm = { custom ->
                showCustomDialog = false
                onCurrencySelected(custom)
                onDismissRequest()
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Select Currency",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(CurrencyRepository.supportedCurrencies) { currency ->
                    val isSelected = currency.code == selectedCurrency.code
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (currency.code == "OTHER") {
                                    showCustomDialog = true
                                } else {
                                    onCurrencySelected(currency)
                                    onDismissRequest()
                                }
                            }
                            .semantics {
                                contentDescription = "${currency.name}, ${currency.code}, symbol ${currency.symbol}"
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currency.symbol,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.width(44.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = currency.name,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CustomCurrencyDialog(
    initialCurrency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (CurrencyOption) -> Unit
) {
    var code by remember { mutableStateOf(if (initialCurrency.code == "OTHER") "CUST" else initialCurrency.code) }
    var symbol by remember { mutableStateOf(initialCurrency.symbol) }
    var name by remember { mutableStateOf(initialCurrency.name) }
    var decimalPlaces by remember { mutableIntStateOf(initialCurrency.decimalPlaces) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Currency", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it.uppercase() },
                    label = { Text("Currency Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { if (it.length <= 4) symbol = it },
                    label = { Text("Symbol (e.g. $, kr, pts)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Currency Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Decimal Places:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(0, 2).forEach { places ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { decimalPlaces = places }
                        ) {
                            RadioButton(
                                selected = decimalPlaces == places,
                                onClick = { decimalPlaces = places }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$places decimals")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalCode = code.trim().ifEmpty { "CUST" }
                    val finalSymbol = symbol.trim().ifEmpty { "¤" }
                    val finalName = name.trim().ifEmpty { "Custom" }
                    onConfirm(
                        CurrencyOption(
                            code = finalCode,
                            symbol = finalSymbol,
                            name = finalName,
                            decimalPlaces = decimalPlaces
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
