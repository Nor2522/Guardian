package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.UsageSnapshot
import com.nordev.guardian.domain.block.PolicySnapshot
import com.nordev.guardian.service.GuardianAccessibilityService
import com.nordev.guardian.ui.nav.Routes
import com.nordev.guardian.ui.nav.ThemeMode

private class ParentTab(
    val route: String,
    val labelRes: Int,
    val outlined: ImageVector,
    val filled: ImageVector,
)

private val parentTabs = listOf(
    ParentTab(Routes.PARENT_TODAY, R.string.tab_today, Icons.Outlined.Today, Icons.Filled.Today),
    ParentTab(Routes.PARENT_LIMITS, R.string.tab_limits, Icons.Outlined.Timer, Icons.Filled.Timer),
    ParentTab(Routes.PARENT_SCHEDULES, R.string.tab_schedules, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    ParentTab(Routes.PARENT_WEB, R.string.tab_web, Icons.Outlined.Public, Icons.Filled.Public),
)

@Composable
fun ParentScaffold(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    showLockWarning: Boolean,
    usage: UsageSnapshot?,
    onOpenUsageSettings: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != Routes.PARENT_SETTINGS
    val snackbarHostState = remember { SnackbarHostState() }
    val warningText = stringResource(R.string.parent_session_warning)
    val limitsState = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(showLockWarning) {
        if (showLockWarning) {
            snackbarHostState.showSnackbar(
                message = warningText,
                duration = SnackbarDuration.Short,
            )
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                GuardianBottomBar(navController = navController, currentRoute = currentRoute)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.PARENT_TODAY,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Routes.PARENT_TODAY) {
                TodayScreen(
                    usage = usage,
                    onOpenSettings = { navController.navigate(Routes.PARENT_SETTINGS) },
                    onOpenUsageSettings = onOpenUsageSettings,
                )
            }
            composable(Routes.PARENT_LIMITS) {
                LimitsScreen(
                    onOpenSettings = { navController.navigate(Routes.PARENT_SETTINGS) },
                    limits = limitsState.value,
                    onEditLimit = { packageName, minutes ->
                        val updated = limitsState.value.toMutableMap().apply {
                            if (minutes > 0) {
                                put(packageName, minutes)
                            } else {
                                remove(packageName)
                            }
                        }
                        limitsState.value = updated
                        GuardianAccessibilityService.instance?.setPolicy(PolicySnapshot(updated))
                    },
                    usage = usage,
                )
            }
            composable(Routes.PARENT_SCHEDULES) {
                SchedulesScreen(onOpenSettings = { navController.navigate(Routes.PARENT_SETTINGS) })
            }
            composable(Routes.PARENT_WEB) {
                WebScreen(onOpenSettings = { navController.navigate(Routes.PARENT_SETTINGS) })
            }
            composable(Routes.PARENT_SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    themeMode = themeMode,
                    onThemeChange = onThemeChange,
                )
            }
        }
    }
}

@Composable
private fun GuardianBottomBar(
    navController: NavHostController,
    currentRoute: String?,
) {
    NavigationBar {
        parentTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.filled else tab.outlined,
                        contentDescription = null,
                    )
                },
                label = { Text(text = stringResource(tab.labelRes)) },
            )
        }
    }
}
