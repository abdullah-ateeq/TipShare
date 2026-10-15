package com.example.tipshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tipshare.data.model.TipShareResult
import com.example.tipshare.domain.TipShareCalculator
import com.example.tipshare.domain.TipShareFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultBreakdownCard(
    result: TipShareResult,
    onCopyClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onNewCalculationClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpandedBreakdown by remember { mutableStateOf(false) }

    val billStr = TipShareFormatter.formatMoney(result.billMinorUnits, result.currency)
    val tipStr = TipShareFormatter.formatMoney(result.tipMinorUnits, result.currency)
    val totalStr = TipShareFormatter.formatMoney(result.totalMinorUnits, result.currency)
    val firstPersonStr = if (result.perPersonMinorUnits.isNotEmpty()) {
        TipShareFormatter.formatMoney(result.perPersonMinorUnits[0], result.currency)
    } else totalStr

    val isSinglePerson = result.peopleCount == 1
    val hasUnevenSplit = result.roundingAdjustmentMinorUnits > 0 && !isSinglePerson

    // Accessibility announcement for screen readers
    val a11ySummary = if (isSinglePerson) {
        "Total to pay $totalStr. Bill amount $billStr, Tip amount $tipStr."
    } else {
        "Each person pays $firstPersonStr. Total with tip $totalStr. Bill amount $billStr, Tip $tipStr for ${result.peopleCount} people."
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11ySummary }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 1. Hero: Amount each person pays
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSinglePerson) "TOTAL TO PAY" else "EACH PERSON PAYS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isSinglePerson) totalStr else "$firstPersonStr EACH",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                if (hasUnevenSplit) {
                    Text(
                        text = "(Amounts vary slightly due to rounding)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Summary Details
            DetailRow(label = "Bill Amount", value = billStr)
            Spacer(modifier = Modifier.height(8.dp))

            val tipLabel = if (result.isCustomTip) {
                val implied = TipShareCalculator.calculateImpliedBasisPoints(result.billMinorUnits, result.tipMinorUnits)
                if (implied != null) {
                    "Tip Amount (${TipShareFormatter.formatTipPercentage(implied)})"
                } else {
                    "Tip Amount"
                }
            } else {
                val bp = result.tipPercentageBasisPoints ?: 0
                "Tip (${TipShareFormatter.formatTipPercentage(bp)})"
            }
            DetailRow(label = tipLabel, value = tipStr)
            Spacer(modifier = Modifier.height(8.dp))

            DetailRow(
                label = "Total with Tip",
                value = totalStr,
                isEmphasized = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            DetailRow(
                label = "Number of People",
                value = TipShareFormatter.formatPeopleCount(result.peopleCount)
            )

            // 3. Person Breakdown
            if (!isSinglePerson) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Individual Breakdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (result.peopleCount > 4) {
                        TextButton(onClick = { isExpandedBreakdown = !isExpandedBreakdown }) {
                            Text(if (isExpandedBreakdown) "Show Less" else "View All (${result.peopleCount})")
                        }
                    }
                }

                val displayCount = if (result.peopleCount <= 4 || isExpandedBreakdown) {
                    result.peopleCount
                } else {
                    3
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until displayCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Person ${i + 1}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = TipShareFormatter.formatMoney(result.perPersonMinorUnits[i], result.currency),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                    if (result.peopleCount > 4 && !isExpandedBreakdown) {
                        Text(
                            text = "+ ${result.peopleCount - 3} more people...",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                }

                // Rounding notice
                if (hasUnevenSplit) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rounded to the nearest cent so the individual amounts add up exactly.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: COPY, SHARE, NEW CALCULATION
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyClicked,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy result",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy")
                }

                OutlinedButton(
                    onClick = onShareClicked,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share result",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }

                OutlinedButton(
                    onClick = onNewCalculationClicked,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New calculation",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isEmphasized: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isEmphasized)
                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            else
                MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = value,
            style = if (isEmphasized)
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            else
                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
