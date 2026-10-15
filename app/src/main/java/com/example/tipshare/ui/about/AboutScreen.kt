package com.example.tipshare.ui.about

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "TipShare",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Version 1.0",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "An offline utility for calculating tips and dividing the final bill among people.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Features Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "FEATURES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                val features = listOf(
                    "Tip percentage calculation",
                    "Custom tip amount",
                    "Equal bill splitting",
                    "Multiple currencies (USD, EUR, GBP, PKR, JPY, etc.)",
                    "Exact monetary rounding (half-up minor unit precision)",
                    "Quick tip presets (0%, 10%, 15%, 18%, 20%, 25%)",
                    "Quick people presets (1 to 50 people)",
                    "Calculation history (up to 10 entries)",
                    "Copy and share formatted results"
                )

                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        // Privacy & Offline Guarantee Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRIVACY & OFFLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "All calculations happen on your device.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "No internet permission requested. Fully functional in Airplane Mode. No analytics, no ads, and no external tracking.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
