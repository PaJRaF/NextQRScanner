package com.nextqr.scanner.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nextqr.scanner.presentation.detail.ScanDetailScreen
import com.nextqr.scanner.presentation.generator.GeneratorScreen
import com.nextqr.scanner.presentation.history.HistoryScreen
import com.nextqr.scanner.presentation.navigation.Routes
import com.nextqr.scanner.presentation.navigation.TopLevelDestination
import com.nextqr.scanner.presentation.onboarding.OnboardingScreen
import com.nextqr.scanner.presentation.premium.PremiumScreen
import com.nextqr.scanner.presentation.scanner.ScannerScreen
import com.nextqr.scanner.presentation.settings.SettingsScreen

@Composable
fun NextQrApp(onboardingCompleted: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val showBottomBar = TopLevelDestination.entries.any { dest ->
        currentRoute?.hierarchy?.any { it.route == dest.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { dest ->
                        val selected =
                            currentRoute?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = null) },
                            label = { Text(stringResource(dest.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted) Routes.SCANNER else Routes.ONBOARDING,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.SCANNER) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.SCANNER) {
                ScannerScreen(
                    onOpenDetail = { navController.navigate(Routes.detail(it)) },
                    onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                )
            }
            composable(Routes.GENERATOR) {
                GeneratorScreen(onOpenPremium = { navController.navigate(Routes.PREMIUM) })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onOpenDetail = { navController.navigate(Routes.detail(it)) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onOpenPremium = { navController.navigate(Routes.PREMIUM) })
            }
            composable(Routes.PREMIUM) {
                PremiumScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("scanId") { type = NavType.LongType }),
            ) {
                ScanDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
