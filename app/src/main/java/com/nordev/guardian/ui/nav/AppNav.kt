package com.nordev.guardian.ui.nav

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.data.prefs.SettingsRepository
import com.nordev.guardian.ui.screens.gate.GateScreen
import com.nordev.guardian.ui.screens.onboarding.DisclosureScreen
import com.nordev.guardian.ui.screens.onboarding.DisclosureType
import com.nordev.guardian.ui.screens.onboarding.DoneScreen
import com.nordev.guardian.ui.screens.onboarding.PermissionsScreen
import com.nordev.guardian.ui.screens.onboarding.PinScreen
import com.nordev.guardian.ui.screens.onboarding.PrivacyScreen
import com.nordev.guardian.ui.screens.onboarding.WelcomeScreen
import com.nordev.guardian.ui.screens.parent.ParentScaffold
import com.nordev.guardian.ui.screens.status.StatusScreen
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
    const val PARENT_SETTINGS = "parent/settings"

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
    val scope = rememberCoroutineScope()
    val session = viewModel<ParentSessionViewModel>().session
    val lifecycleOwner = LocalLifecycleOwner.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isParentRoute = currentRoute?.startsWith(Routes.PARENT) == true
    var showLockWarning by remember { mutableStateOf(false) }
    var requestNotifications by remember { mutableStateOf(false) }
    val vpnConsentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { }

    // Background, screen off, and recents all stop the activity; that lock is
    // separate from the five idle minutes counted below.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) session.lock()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                StatusScreen(onOpenGate = { navController.navigate(Routes.GATE) })
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
                        navController.navigate(Routes.PARENT) {
                            popUpTo(Routes.GATE) { inclusive = true }
                        }
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
                )
            }
        }
    }
}

private fun launchUsageAccessSettings(context: Context) {
    runCatching { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
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
