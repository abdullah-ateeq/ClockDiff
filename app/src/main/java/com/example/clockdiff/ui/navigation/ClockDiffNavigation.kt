package com.example.clockdiff.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.clockdiff.ui.screens.AboutScreen
import com.example.clockdiff.ui.screens.HistoryScreen
import com.example.clockdiff.ui.screens.HomeScreen
import com.example.clockdiff.ui.screens.ResultsScreen
import com.example.clockdiff.ui.screens.SettingsScreen
import com.example.clockdiff.ui.screens.SplashScreen
import com.example.clockdiff.ui.viewmodel.ClockDiffUiState
import com.example.clockdiff.ui.viewmodel.ClockDiffViewModel

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Default.Schedule, Icons.Outlined.Schedule)
    object Home : Screen("home", "Calculator", Icons.Default.Schedule, Icons.Outlined.Schedule)
    object Results : Screen("results", "Result", Icons.Default.Schedule, Icons.Outlined.Schedule)
    object History : Screen("history", "History", Icons.Default.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings, Icons.Outlined.Settings)
    object About : Screen("about", "About", Icons.Default.Info, Icons.Outlined.Info)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.History,
    Screen.Settings,
    Screen.About
)

@Composable
fun ClockDiffApp(
    viewModel: ClockDiffViewModel,
    uiState: ClockDiffUiState
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
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
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
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
                    onStartTimeChange = viewModel::setStartTime,
                    onEndTimeChange = viewModel::setEndTime,
                    onIncludeDatesChange = viewModel::setIncludeDates,
                    onStartDateChange = viewModel::setStartDate,
                    onEndDateChange = viewModel::setEndDate,
                    onCrossMidnightChange = viewModel::setCrossMidnight,
                    onSwapTimes = viewModel::swapTimes,
                    onSetStartToNow = viewModel::setStartToNow,
                    onSetEndToNow = viewModel::setEndToNow,
                    onReset = viewModel::resetInputs,
                    onCalculate = {
                        viewModel.calculate()
                        navController.navigate(Screen.Results.route)
                    }
                )
            }

            composable(Screen.Results.route) {
                ResultsScreen(
                    result = uiState.calculationResult,
                    is24Hour = uiState.settings.is24HourFormat,
                    isSavedToHistory = uiState.isSavedToHistory,
                    onSaveToHistory = viewModel::saveCurrentResultToHistory,
                    onResetAndCalculateAgain = {
                        viewModel.resetInputs()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    history = uiState.history,
                    is24Hour = uiState.settings.is24HourFormat,
                    onLoadItem = { item ->
                        viewModel.loadFromHistory(item)
                        navController.navigate(Screen.Results.route)
                    },
                    onDeleteItem = viewModel::deleteHistoryItem,
                    onClearAll = viewModel::clearAllHistory,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = uiState.settings,
                    historyCount = uiState.history.size,
                    onThemeModeChange = viewModel::setThemeMode,
                    on24HourChange = viewModel::setIs24HourFormat,
                    onDefaultCrossMidnightChange = viewModel::setDefaultCrossMidnight,
                    onClearAllHistory = viewModel::clearAllHistory
                )
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
