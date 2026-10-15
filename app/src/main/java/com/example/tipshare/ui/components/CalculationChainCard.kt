package com.example.tipshare.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.delay

@Composable
fun CalculationChainCard(
    result: TipShareResult,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var stepVisible by remember(result) {
        mutableIntStateOf(if (animationsEnabled) 0 else 5)
    }

    LaunchedEffect(result, animationsEnabled) {
        if (animationsEnabled) {
            stepVisible = 1
            delay(80)
            stepVisible = 2
            delay(80)
            stepVisible = 3
            delay(80)
            stepVisible = 4
            delay(80)
            stepVisible = 5
        } else {
            stepVisible = 5
        }
    }

    val billStr = TipShareFormatter.formatMoney(result.billMinorUnits, result.currency)
    val tipStr = TipShareFormatter.formatMoney(result.tipMinorUnits, result.currency)
    val totalStr = TipShareFormatter.formatMoney(result.totalMinorUnits, result.currency)
    val eachStr = if (result.perPersonMinorUnits.isNotEmpty()) {
        TipShareFormatter.formatMoney(result.perPersonMinorUnits[0], result.currency)
    } else totalStr

    val tipLabel = if (result.isCustomTip) {
        val implied = TipShareCalculator.calculateImpliedBasisPoints(result.billMinorUnits, result.tipMinorUnits)
        if (implied != null) {
            "Custom Tip (${TipShareFormatter.formatTipPercentage(implied)})"
        } else {
            "Custom Tip"
        }
    } else {
        val bp = result.tipPercentageBasisPoints ?: 0
        TipShareFormatter.formatTipPercentage(bp)
    }

    val accessibilityDesc = "Calculation chain: Bill $billStr, Tip $tipLabel, plus $tipStr tip, equals $totalStr total, divided by ${result.peopleCount} people, equals $eachStr each."

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityDesc }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CALCULATION FLOW",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step 1: Bill
            AnimatedVisibility(
                visible = stepVisible >= 1,
                enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { -it / 2 }
            ) {
                ChainNode(
                    primaryText = billStr,
                    subText = "BILL",
                    isHero = false
                )
            }

            if (stepVisible >= 2) {
                ChainArrow(text = tipLabel)
            }

            // Step 2: Tip amount
            AnimatedVisibility(
                visible = stepVisible >= 2,
                enter = fadeIn(tween(150))
            ) {
                ChainNode(
                    primaryText = "+$tipStr",
                    subText = "TIP ADDED",
                    isHero = false,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
            }

            if (stepVisible >= 3) {
                ChainArrow(text = null)
            }

            // Step 3: Total
            AnimatedVisibility(
                visible = stepVisible >= 3,
                enter = fadeIn(tween(150))
            ) {
                ChainNode(
                    primaryText = totalStr,
                    subText = "TOTAL WITH TIP",
                    isHero = false
                )
            }

            if (stepVisible >= 4) {
                ChainArrow(text = "÷ ${result.peopleCount} ${if (result.peopleCount == 1) "person" else "people"}")
            }

            // Step 4 & 5: Each person pays
            AnimatedVisibility(
                visible = stepVisible >= 5,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
            ) {
                val heroLabel = if (result.peopleCount == 1) "$eachStr TOTAL" else "$eachStr EACH"
                ChainNode(
                    primaryText = heroLabel,
                    subText = if (result.peopleCount == 1) "AMOUNT TO PAY" else "EACH PERSON PAYS",
                    isHero = true,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ChainNode(
    primaryText: String,
    subText: String,
    isHero: Boolean,
    accentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = primaryText,
            style = if (isHero)
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            else
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
        )
        Text(
            text = subText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun ChainArrow(text: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        if (text != null) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
