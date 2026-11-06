package com.nordev.guardian.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

// In-memory only this step; persistence arrives at Step 3.
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
) {
    val navController = rememberNavController()
    // In memory only: process death clears it, and Step 3 replaces it with a
    // hashed preference. Never written to saved state or logs.
    var parentPin by remember { mutableStateOf<String?>(null) }
    NavHost(navController = navController, startDestination = Routes.STATUS) {
        composable(Routes.STATUS) {
            StatusScreen(onOpenGate = { navController.navigate(Routes.GATE) })
        }
        composable(Routes.GATE) {
            GateScreen(
                storedPin = parentPin,
                onExitToOnboardingWelcome = {
                    navController.navigate(Routes.ONBOARDING_WELCOME) {
                        popUpTo(Routes.GATE) { inclusive = true }
                    }
                },
                onUnlocked = {
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
                onPinSet = { parentPin = it },
                onContinue = { navController.navigate(Routes.ONBOARDING_PERMISSIONS) },
            )
        }
        composable(Routes.ONBOARDING_PERMISSIONS) {
            PermissionsScreen(
                onBack = { navController.popBackStack() },
                onOpenDisclosure = { type -> navController.navigate(Routes.disclosureRoute(type)) },
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
                onConsented = { navController.popBackStack() },
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
            ParentScaffold(themeMode = themeMode, onThemeChange = onThemeChange)
        }
    }
}
