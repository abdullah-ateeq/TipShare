package com.example.tipshare.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tipshare.ui.about.AboutScreen
import com.example.tipshare.ui.history.HistoryScreen
import com.example.tipshare.ui.home.HomeScreen
import com.example.tipshare.ui.settings.SettingsScreen
import com.example.tipshare.ui.splash.SplashScreen
import com.example.tipshare.ui.theme.TipShareTheme
import com.example.tipshare.util.HapticHelper

@Composable
fun TipShareMainApp(
    viewModel: TipShareViewModel = viewModel(
        factory = TipShareViewModel.provideFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val view = LocalView.current

    TipShareTheme(
        themeSetting = settingsState.theme,
        dynamicColor = settingsState.dynamicColor
    ) {
        val showBottomBar = currentRoute != Screen.Splash.route

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        Screen.bottomNavScreens.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        HapticHelper.performClick(view, settingsState.hapticFeedback)
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    screen.icon?.let {
                                        Icon(imageVector = it, contentDescription = screen.title)
                                    }
                                },
                                label = { Text(screen.title) }
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        uiState = uiState,
                        settingsState = settingsState,
                        onBillChanged = viewModel::onBillInputChanged,
                        onCurrencyChanged = viewModel::onCurrencySelected,
                        onTipModeChanged = viewModel::onTipModeSelected,
                        onTipPercentageChanged = viewModel::onTipPercentageChanged,
                        onQuickTipPresetSelected = viewModel::onQuickTipPresetSelected,
                        onCustomTipChanged = viewModel::onCustomTipAmountChanged,
                        onPeopleCountChanged = viewModel::onPeopleCountChanged,
                        onQuickPeoplePresetSelected = viewModel::onQuickPeoplePresetSelected,
                        onCalculateClicked = viewModel::calculateSplit,
                        onResetClicked = viewModel::reset,
                        onNewCalculationClicked = viewModel::newCalculation
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        history = uiState.history,
                        settingsState = settingsState,
                        onHistoryItemClicked = { item ->
                            viewModel.restoreFromHistory(item)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onDeleteHistoryItem = viewModel::deleteHistoryItem,
                        onClearAllHistory = viewModel::clearAllHistory
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        settingsState = settingsState,
                        onThemeChanged = viewModel::updateTheme,
                        onDynamicColorChanged = viewModel::updateDynamicColor,
                        onHapticFeedbackChanged = viewModel::updateHapticFeedback,
                        onAnimationsChanged = viewModel::updateAnimations,
                        onDefaultCurrencyChanged = viewModel::updateDefaultCurrency,
                        onDefaultTipChanged = viewModel::updateDefaultTip,
                        onDefaultPeopleChanged = viewModel::updateDefaultPeople,
                        onConfirmHistoryDeleteChanged = viewModel::updateConfirmHistoryDelete,
                        onPreserveLastCalculationChanged = viewModel::updatePreserveLastCalculation,
                        onClearAllHistory = viewModel::clearAllHistory,
                        onResetSettings = viewModel::resetSettingsToDefault
                    )
                }

                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}
