package com.nordev.guardian.ui.nav

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nordev.guardian.R
import com.nordev.guardian.data.permissions.DeviceAdminAccessChecker
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.data.prefs.PolicyRepository
import com.nordev.guardian.data.prefs.SettingsRepository
import com.nordev.guardian.service.DnsFilterVpnService
import com.nordev.guardian.service.admin.GuardianDeviceAdminReceiver
import com.nordev.guardian.ui.screens.gate.GateScreen
import com.nordev.guardian.ui.screens.onboarding.DisclosureScreen
import com.nordev.guardian.ui.screens.onboarding.DisclosureType
import com.nordev.guardian.ui.screens.onboarding.DoneScreen
import com.nordev.guardian.ui.screens.onboarding.PermissionsScreen
import com.nordev.guardian.ui.screens.onboarding.PinScreen
import com.nordev.guardian.ui.screens.onboarding.PrivacyScreen
import com.nordev.guardian.ui.screens.onboarding.WelcomeScreen
import com.nordev.guardian.ui.screens.parent.ParentScaffold
import com.nordev.guardian.ui.screens.status.StatusGateTarget
import com.nordev.guardian.ui.screens.status.StatusScreen
import com.nordev.guardian.ui.usage.UsageViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Theme is still in-memory. The PIN and per-permission consent persist.
enum class ThemeMode { System, Light, Dark }

object Routes {
    const val STATUS = "status"
    const val GATE = "gate"
    const val ONBOARDING_WELCOME = "onboarding/welcome"
    const val ONBOARDING_PRIVACY = "onboarding/privacy"
    const val ONBOARDING_PIN = "onboarding/pin"
    const val ONBOARDING_PERMISSIONS = "onboarding/permissions"
    const val ONBOARDING_DISCLOSURE = "onboarding/disclosure/{type}"
    const val ONBOARDING_DONE = "onboarding/done"
    const val PARENT = "parent"
    const val PARENT_TODAY = "parent/today"
    const val PARENT_LIMITS = "parent/limits"
    const val PARENT_SCHEDULES = "parent/schedules"
    const val PARENT_WEB = "parent/web"
    const val PARENT_WEB_ADD = "parent/web/add"
    const val PARENT_SETTINGS = "parent/settings"
    const val PARENT_SETTINGS_REMOVE = "parent/settings/remove"

    fun disclosureRoute(type: DisclosureType): String = "onboarding/disclosure/${type.name}"

    fun disclosureTypeFrom(value: String): DisclosureType =
        DisclosureType.entries.firstOrNull { it.name == value } ?: DisclosureType.UsageAccess
}

@Composable
fun GuardianNavHost(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onSecureChange: (Boolean) -> Unit = {},
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember(context) { ParentPinRepository(context) }
    val settingsRepository = remember(context) { SettingsRepository(context) }
    val policyRepository = remember(context) { PolicyRepository(context) }
    val scope = rememberCoroutineScope()
    val session = viewModel<ParentSessionViewModel>().session
    val usageViewModel = viewModel<UsageViewModel>()
    val usage by usageViewModel.state.collectAsState()
    val webFilter by remember(policyRepository) { policyRepository.webFilter }
        .collectAsState(initial = null)
    val filterEnabled = rememberUpdatedState(webFilter?.enabled)
    val lifecycleOwner = LocalLifecycleOwner.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isParentRoute = currentRoute?.startsWith(Routes.PARENT) == true
    var showLockWarning by remember { mutableStateOf(false) }
    var requestNotifications by remember { mutableStateOf(false) }

    // Review on the status screen arms a destination behind the gate. Nothing
    // in the child's view changes; the target is consumed on a correct PIN.
    var pendingStatusTarget by rememberSaveable { mutableStateOf(StatusGateTarget.Parent.name) }
    var parentStartTab by rememberSaveable { mutableStateOf(Routes.PARENT_TODAY) }
    val vpnConsentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && VpnService.prepare(context) == null) {
            DnsFilterVpnService.start(context)
        }
    }

    // Background, screen off, and recents all stop the activity; that lock is
    // separate from the five idle minutes counted below. A configuration change
    // also stops the activity, but the parent never left Guardian, so keep the
    // session through rotation, theme, or font size changes.
    DisposableEffect(lifecycleOwner) {
        val activity = lifecycleOwner as? Activity
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && activity?.isChangingConfigurations != true) {
                session.lock()
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                usageViewModel.refresh()
                if (filterEnabled.value == true) ensureFilterStarted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // The switch is on by default, but the tunnel needs the system consent. This
    // waits for the stored value: the optimistic default would start the filter
    // for a moment even when the parent had turned it off.
    LaunchedEffect(webFilter?.enabled) {
        when (webFilter?.enabled) {
            true -> ensureFilterStarted(context)
            false -> DnsFilterVpnService.stop(context)
            null -> Unit
        }
    }

    // Back-stack guard: a restored parent route never shows without an unlock.
    LaunchedEffect(currentRoute, session.isUnlocked) {
        if (isParentRoute && !session.isUnlocked) {
            navController.navigate(Routes.GATE) {
                popUpTo(Routes.PARENT) { inclusive = true }
            }
        }
    }

    LaunchedEffect(session.isUnlocked) {
        while (session.isUnlocked) {
            showLockWarning = session.warningDue()
            if (session.expired()) session.lock()
            delay(1_000)
        }
        showLockWarning = false
    }

    LaunchedEffect(isParentRoute) {
        onSecureChange(isParentRoute)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        session.touch()
                    }
                }
            },
    ) {
        NavHost(navController = navController, startDestination = Routes.STATUS) {
            composable(Routes.STATUS) {
                StatusScreen(
                    onOpenGate = { target ->
                        pendingStatusTarget = target.name
                        navController.navigate(Routes.GATE)
                    },
                    usage = usage,
                )
            }
            composable(Routes.GATE) {
                GateScreen(
                    repository = repository,
                    onExitToOnboardingWelcome = {
                        navController.navigate(Routes.ONBOARDING_WELCOME) {
                            popUpTo(Routes.GATE) { inclusive = true }
                        }
                    },
                    onUnlocked = {
                        session.unlock()
                        val target = StatusGateTarget.entries
                            .firstOrNull { it.name == pendingStatusTarget }
                            ?: StatusGateTarget.Parent
                        val destination = when (target) {
                            StatusGateTarget.Parent -> {
                                parentStartTab = Routes.PARENT_TODAY
                                Routes.PARENT
                            }
                            StatusGateTarget.Permissions -> Routes.ONBOARDING_PERMISSIONS
                            StatusGateTarget.Web -> {
                                parentStartTab = Routes.PARENT_WEB
                                Routes.PARENT
                            }
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.GATE) { inclusive = true }
                        }
                        pendingStatusTarget = StatusGateTarget.Parent.name
                    },
                )
            }
            composable(Routes.ONBOARDING_WELCOME) {
                WelcomeScreen(
                    onBack = { navController.popBackStack() },
                    onGetStarted = { navController.navigate(Routes.ONBOARDING_PRIVACY) },
                )
            }
            composable(Routes.ONBOARDING_PRIVACY) {
                PrivacyScreen(
                    onBack = { navController.popBackStack() },
                    onIUnderstand = { navController.navigate(Routes.ONBOARDING_PIN) },
                )
            }
            composable(Routes.ONBOARDING_PIN) {
                PinScreen(
                    onBack = { navController.popBackStack() },
                    repository = repository,
                    onContinue = { navController.navigate(Routes.ONBOARDING_PERMISSIONS) },
                )
            }
            composable(Routes.ONBOARDING_PERMISSIONS) {
                PermissionsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDisclosure = { type -> navController.navigate(Routes.disclosureRoute(type)) },
                    onOpenUsageSettings = { launchUsageAccessSettings(context) },
                    onOpenNotificationSettings = { launchNotificationSettings(context) },
                    onOpenAccessibilitySettings = { launchAccessibilitySettings(context) },
                    onOpenVpnSettings = { launchVpnAccess(context, vpnConsentLauncher::launch) },
                    onOpenDeviceAdminSettings = { launchDeviceAdmin(context) },
                    requestNotifications = requestNotifications,
                    onNotificationsRequested = { requestNotifications = false },
                    onContinue = { navController.navigate(Routes.ONBOARDING_DONE) },
                    onFinishLater = {
                        navController.navigate(Routes.STATUS) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                Routes.ONBOARDING_DISCLOSURE,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                val typeName = entry.arguments?.getString("type").orEmpty()
                DisclosureScreen(
                    type = Routes.disclosureTypeFrom(typeName),
                    onBack = { navController.popBackStack() },
                    onConsented = {
                        when (typeName) {
                            DisclosureType.UsageAccess.name -> {
                                scope.launch { settingsRepository.recordUsageConsent() }
                                navController.popBackStack()
                                launchUsageAccessSettings(context)
                            }
                            DisclosureType.Notifications.name -> {
                                scope.launch { settingsRepository.recordNotificationConsent() }
                                requestNotifications = true
                                navController.popBackStack()
                            }
                            DisclosureType.Accessibility.name -> {
                                scope.launch { settingsRepository.recordAccessibilityConsent() }
                                navController.popBackStack()
                                launchAccessibilitySettings(context)
                            }
                            DisclosureType.VpnFilter.name -> {
                                scope.launch { settingsRepository.recordVpnConsent() }
                                navController.popBackStack()
                                launchVpnAccess(context, vpnConsentLauncher::launch)
                            }
                            DisclosureType.DeviceAdmin.name -> {
                                scope.launch { settingsRepository.recordDeviceAdminConsent() }
                                navController.popBackStack()
                                launchDeviceAdmin(context)
                            }
                            else -> navController.popBackStack()
                        }
                    },
                )
            }
            composable(Routes.ONBOARDING_DONE) {
                DoneScreen(
                    onBack = { navController.popBackStack() },
                    onFinishSetup = { navController.popBackStack() },
                    onOpenStatus = {
                        navController.navigate(Routes.STATUS) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.PARENT) {
                ParentScaffold(
                    themeMode = themeMode,
                    onThemeChange = onThemeChange,
                    showLockWarning = showLockWarning && isParentRoute,
                    usage = usage,
                    onOpenUsageSettings = { launchUsageAccessSettings(context) },
                    onTurnOnFilter = {
                        val consent = VpnService.prepare(context)
                        if (consent != null) {
                            vpnConsentLauncher.launch(consent)
                        } else {
                            ensureFilterStarted(context)
                        }
                    },
                    onOpenVpnSettings = {
                        runCatching { context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS)) }
                    },
                    onOpenNetworkSettings = {
                        runCatching { context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
                    },
                    policyRepository = policyRepository,
                    pinRepository = repository,
                    settingsRepository = settingsRepository,
                    startTab = parentStartTab,
                )
            }
        }
    }
}

private fun launchUsageAccessSettings(context: Context) {
    runCatching { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
}

// startForegroundService throws when the system grant is missing or the caller
// is in the background. Both are expected while onboarding, so check first and
// let the Web tab report a filter that is not running.
private fun ensureFilterStarted(context: Context) {
    if (DnsFilterVpnService.isRunning()) return
    runCatching {
        if (VpnService.prepare(context) == null) DnsFilterVpnService.start(context)
    }
}

private fun launchNotificationSettings(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
        )
    }
}

private fun launchAccessibilitySettings(context: Context) {
    runCatching { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
}

// The system dialog reads getCallingPackage(), which Android only fills in for
// result launches. A plain startActivity makes it finish without showing.
private fun launchVpnAccess(context: Context, launchForResult: (Intent) -> Unit) {
    runCatching {
        val consent = VpnService.prepare(context)
        if (consent != null) {
            launchForResult(consent)
        } else {
            context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
        }
    }
}

// Adding an admin takes the component plus the explanation. Samsung closes that
// screen without drawing it once the admin is active, so the granted row opens
// the admin list. Android exposes no public action for that list, so target the
// platform Settings activity and fall back to Security settings when missing.
private fun launchDeviceAdmin(context: Context) {
    runCatching {
        if (DeviceAdminAccessChecker(context).isGranted()) {
            launchAdminList(context)
        } else {
            context.startActivity(
                Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        ComponentName(context, GuardianDeviceAdminReceiver::class.java),
                    )
                    .putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        context.getString(R.string.device_admin_explanation),
                    ),
            )
        }
    }
}

private fun launchAdminList(context: Context) {
    val list = Intent().setComponent(
        ComponentName("com.android.settings", "com.android.settings.Settings\$DeviceAdminSettingsActivity"),
    )
    try {
        context.startActivity(list)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
}
