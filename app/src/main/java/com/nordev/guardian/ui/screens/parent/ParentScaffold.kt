package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nordev.guardian.R
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.data.prefs.PolicyRepository
import com.nordev.guardian.data.prefs.SettingsRepository
import com.nordev.guardian.data.usage.UsageSnapshot
import com.nordev.guardian.data.web.BlocklistRepository
import com.nordev.guardian.domain.block.PolicySnapshot
import com.nordev.guardian.domain.web.WebFilterPolicy
import com.nordev.guardian.service.admin.GuardianProtection
import com.nordev.guardian.ui.components.rememberNotificationsVisible
import com.nordev.guardian.ui.nav.Routes
import com.nordev.guardian.ui.nav.ThemeMode
import com.nordev.guardian.ui.theme.GuardianSpacing
import com.nordev.guardian.ui.theme.LocalGuardianGradients
import kotlinx.coroutines.launch

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
    onOpenNotificationSettings: () -> Unit,
    onTurnOnFilter: () -> Unit,
    onOpenVpnSettings: () -> Unit,
    onOpenNetworkSettings: () -> Unit,
    policyRepository: PolicyRepository,
    pinRepository: ParentPinRepository,
    settingsRepository: SettingsRepository,
    startTab: String,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != Routes.PARENT_SETTINGS &&
        currentRoute != Routes.PARENT_WEB_ADD &&
        currentRoute != Routes.PARENT_WEB_TURN_OFF &&
        currentRoute != Routes.PARENT_SETTINGS_REMOVE
    val snackbarHostState = remember { SnackbarHostState() }
    val warningText = stringResource(R.string.parent_session_warning)
    val scope = rememberCoroutineScope()
    val policy by policyRepository.snapshot.collectAsState(initial = PolicySnapshot())
    val webFilter by remember(policyRepository) { policyRepository.webFilter }
        .collectAsState(initial = WebFilterPolicy.Default)
    val adultList = remember(context) { BlocklistRepository(context).adultList() }
    val notificationsVisible = rememberNotificationsVisible()

    // The status review action lands on the affected tab. Today stays the
    // graph start so the bottom bar still clears back to the first tab.
    LaunchedEffect(Unit) {
        if (startTab != Routes.PARENT_TODAY) navController.navigate(startTab)
    }

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
        containerColor = Color.Transparent,
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
                    notificationsVisible = notificationsVisible,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                )
            }
            composable(Routes.PARENT_LIMITS) {
                LimitsScreen(
                    onOpenSettings = { navController.navigate(Routes.PARENT_SETTINGS) },
                    limits = policy.limits,
                    onEditLimit = { packageName, minutes ->
                        val updated = policy.limits.toMutableMap().apply {
                            if (minutes > 0) {
                                put(packageName, minutes)
                            } else {
                                remove(packageName)
                            }
                        }
                        scope.launch { policyRepository.saveLimits(updated) }
                    },
                    usage = usage,
                )
            }
            composable(Routes.PARENT_SCHEDULES) {
                SchedulesScreen(
                    onOpenSettings = { navController.navigate(Routes.PARENT_SETTINGS) },
                    schedules = policy.schedules,
                    onSaveSchedule = { schedule ->
                        val updated = policy.schedules.filterNot { it.id == schedule.id } + schedule
                        scope.launch { policyRepository.saveSchedules(updated) }
                    },
                    onDeleteSchedule = { scheduleId ->
                        val updated = policy.schedules.filterNot { it.id == scheduleId }
                        scope.launch { policyRepository.saveSchedules(updated) }
                    },
                )
            }
            composable(Routes.PARENT_WEB) {
                WebScreen(
                    policy = webFilter,
                    adultListVersion = adultList.version,
                    adultListCount = adultList.domains.size,
                    onOpenSettings = { navController.navigate(Routes.PARENT_SETTINGS) },
                    onSetEnabled = { enabled ->
                        scope.launch {
                            policyRepository.saveWebFilter(webFilter.copy(enabled = enabled))
                            // Start only after the stored switch is true, so the
                            // service's own policy collector does not stop it
                            // while it is still reading the old value.
                            if (enabled) onTurnOnFilter()
                        }
                    },
                    onRequestTurnOff = {
                        navController.navigate(Routes.PARENT_WEB_TURN_OFF)
                    },
                    onAddSite = { navController.navigate(Routes.PARENT_WEB_ADD) },
                    onRemoveSite = { domain ->
                        scope.launch {
                            policyRepository.saveWebFilter(
                                webFilter.copy(customDomains = webFilter.customDomains - domain),
                            )
                        }
                    },
                    onTurnOnFilter = onTurnOnFilter,
                    onOpenVpnSettings = onOpenVpnSettings,
                    onOpenNetworkSettings = onOpenNetworkSettings,
                )
            }
            composable(Routes.PARENT_WEB_ADD) {
                AddSiteScreen(
                    existingDomains = webFilter.customDomains,
                    adultDomains = adultList.domains,
                    onBack = { navController.popBackStack() },
                    onSave = { domain ->
                        scope.launch {
                            policyRepository.saveWebFilter(
                                webFilter.copy(customDomains = webFilter.customDomains + domain),
                            )
                        }
                        navController.popBackStack()
                    },
                )
            }
            composable(Routes.PARENT_WEB_TURN_OFF) {
                TurnOffWebFilterScreen(
                    pinRepository = pinRepository,
                    onBack = { navController.popBackStack() },
                    onTurnOff = {
                        policyRepository.saveWebFilter(webFilter.copy(enabled = false))
                    },
                )
            }
            composable(Routes.PARENT_SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    themeMode = themeMode,
                    onThemeChange = onThemeChange,
                    onOpenRemoval = { navController.navigate(Routes.PARENT_SETTINGS_REMOVE) },
                )
            }
            composable(Routes.PARENT_SETTINGS_REMOVE) {
                RemoveGuardianScreen(
                    pinRepository = pinRepository,
                    onBack = { navController.popBackStack() },
                    onTurnOffProtection = {
                        policyRepository.saveWebFilter(webFilter.copy(enabled = false))
                        // The parent is leaving on purpose: the uninstall check
                        // becomes a neutral Off, not a broken check.
                        settingsRepository.setUninstallProtectionWanted(false)
                        GuardianProtection.turnOff(context)
                    },
                )
            }
        }
    }
}

// The bar floats over the ambient wash with a hairline instead of a tonal
// elevator, so the parent tabs keep the same depth as the child screens.
@Composable
private fun GuardianBottomBar(
    navController: NavHostController,
    currentRoute: String?,
) {
    Column {
        HorizontalDivider(
            thickness = GuardianSpacing.hairline,
            color = LocalGuardianGradients.current.cardBorder,
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp,
        ) {
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
}
