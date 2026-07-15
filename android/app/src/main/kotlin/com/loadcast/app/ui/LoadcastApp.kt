package com.loadcast.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.loadcast.app.ui.screens.AnalysisScreen
import com.loadcast.app.ui.screens.HomeScreen
import com.loadcast.app.ui.screens.RecordsScreen
import com.loadcast.app.ui.screens.SettingsScreen

/** 하단 내비게이션 4탭 — 홈 · 기록 · 분석 · 설정 (디자인 문서 섹션 03). */
enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "홈", Icons.Filled.Home),
    RECORDS("records", "기록", Icons.AutoMirrored.Filled.List),
    ANALYSIS("analysis", "분석", Icons.AutoMirrored.Filled.TrendingUp),
    SETTINGS("settings", "설정", Icons.Filled.Settings),
}

@Composable
fun LoadcastApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy
                            ?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Tab.HOME.route) { HomeScreen() }
            composable(Tab.RECORDS.route) { RecordsScreen() }
            composable(Tab.ANALYSIS.route) { AnalysisScreen() }
            composable(Tab.SETTINGS.route) { SettingsScreen() }
        }
    }
}
