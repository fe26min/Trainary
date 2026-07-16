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
import com.loadcast.app.data.RecordStore
import com.loadcast.app.ui.screens.AnalysisScreen
import com.loadcast.app.ui.screens.HomeScreen
import com.loadcast.app.ui.screens.RecommendationScreen
import com.loadcast.app.ui.screens.RecordFormScreen
import com.loadcast.app.ui.screens.RecordsScreen
import com.loadcast.app.ui.screens.SettingsScreen
import java.time.LocalDate

/** 하단 내비게이션 4탭 — 홈 · 기록 · 분석 · 설정 (디자인 문서 섹션 03). */
enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "홈", Icons.Filled.Home),
    RECORDS("records", "기록", Icons.AutoMirrored.Filled.List),
    ANALYSIS("analysis", "분석", Icons.AutoMirrored.Filled.TrendingUp),
    SETTINGS("settings", "설정", Icons.Filled.Settings),
}

private object Routes {
    const val ADD = "add"
    const val EDIT = "edit/{id}"
    const val RECOMMEND = "recommend"
    fun edit(id: String) = "edit/$id"
}

@Composable
fun LoadcastApp(store: RecordStore) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val today = LocalDate.now()

    // 하단 탭은 최상위 4개 화면에서만 노출 (폼·상세에서는 숨김)
    val showBottomBar = Tab.entries.any { tab -> currentDestination?.hierarchy?.any { it.route == tab.route } == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Tab.HOME.route) {
                HomeScreen(
                    records = store.records,
                    today = today,
                    firstUseDate = store.firstUseDate(today),
                    onOpenRecommendation = { navController.navigate(Routes.RECOMMEND) },
                    onOpenAnalysis = { navController.navigate(Tab.ANALYSIS.route) },
                    onAdd = { navController.navigate(Routes.ADD) },
                    onOpenRecord = { navController.navigate(Routes.edit(it)) },
                )
            }
            composable(Tab.RECORDS.route) {
                RecordsScreen(
                    records = store.records,
                    onAdd = { navController.navigate(Routes.ADD) },
                    onOpenRecord = { navController.navigate(Routes.edit(it)) },
                )
            }
            composable(Tab.ANALYSIS.route) {
                AnalysisScreen(records = store.records, today = today, firstUseDate = store.firstUseDate(today))
            }
            composable(Tab.SETTINGS.route) {
                SettingsScreen(recordCount = store.records.size)
            }
            composable(Routes.ADD) {
                RecordFormScreen(
                    existing = null,
                    today = today,
                    onSave = { store.upsert(it); navController.popBackStack() },
                    onDelete = {},
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.EDIT) { entry ->
                val id = entry.arguments?.getString("id")
                val existing = id?.let(store::find)
                RecordFormScreen(
                    existing = existing,
                    today = today,
                    onSave = { store.upsert(it); navController.popBackStack() },
                    onDelete = { store.delete(it); navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.RECOMMEND) {
                RecommendationScreen(
                    records = store.records,
                    today = today,
                    firstUseDate = store.firstUseDate(today),
                    onAdd = { navController.navigate(Routes.ADD) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
