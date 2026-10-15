package com.example.tipshare.ui.splash

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Calculate
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var stage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(220)
        stage = 1 // $100
        delay(220)
        stage = 2 // 15%
        delay(220)
        stage = 3 // $115
        delay(220)
        stage = 4 // $28.75 EACH
        delay(250)
        stage = 5 // TipShare branding
        delay(250)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Step 1: $100
            AnimatedVisibility(
                visible = stage >= 1,
                enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 2 }
            ) {
                Text(
                    text = "$100",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (stage >= 2) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(vertical = 2.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            // Step 2: 15%
            AnimatedVisibility(
                visible = stage >= 2,
                enter = fadeIn(tween(180))
            ) {
                Text(
                    text = "15%",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }

            if (stage >= 3) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(vertical = 2.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            // Step 3: $115
            AnimatedVisibility(
                visible = stage >= 3,
                enter = fadeIn(tween(180))
            ) {
                Text(
                    text = "$115",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (stage >= 4) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(vertical = 2.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            // Step 4: $28.75 EACH
            AnimatedVisibility(
                visible = stage >= 4,
                enter = fadeIn(tween(180))
            ) {
                Text(
                    text = "$28.75 EACH",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Step 5: TipShare Branding
            AnimatedVisibility(
                visible = stage >= 5,
                enter = fadeIn(tween(200))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "TipShare",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
