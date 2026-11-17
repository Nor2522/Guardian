package com.nordev.guardian.service

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.nordev.guardian.BuildConfig
import com.nordev.guardian.data.prefs.PolicyRepository
import com.nordev.guardian.data.prefs.SettingsRepository
import com.nordev.guardian.domain.block.AccessDecision
import com.nordev.guardian.domain.block.AlwaysAllowed
import com.nordev.guardian.domain.block.PolicySnapshot
import com.nordev.guardian.ui.block.BlockActivity
import com.nordev.guardian.ui.guard.GuardActivity
import com.nordev.guardian.ui.nav.BlockState
import com.nordev.guardian.ui.nav.GuardKind
import com.nordev.guardian.ui.nav.GuardState
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Declared so the user can grant Accessibility in system settings. The service
// watches window state changes and asks the policy engine whether the focused
// app is blocked. It does not read node text, perform gestures, or retrieve
// window content.
class GuardianAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val policy: PolicySnapshot
        get() = sharedPolicy

    @Volatile
    private var webFilterEnabled = true

    @Volatile
    private var uninstallProtectionWanted = false

    // The delayed Settings check must not fire after the parent already left.
    @Volatile
    private var lastWindowPackage: String? = null

    // Home, keyboards, the dialer, the clock, and Guardian itself never get a
    // pause screen. The static part lives in the domain; the rest depends on
    // this phone.
    private val alwaysAllowed: Set<String> by lazy {
        val keyboards = (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.enabledInputMethodList
            ?.map { it.packageName }
            ?.toSet()
            .orEmpty()
        val home = runCatching {
            packageManager.resolveActivity(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                PackageManager.MATCH_DEFAULT_ONLY,
            )?.activityInfo?.packageName
        }.getOrNull()
        buildSet {
            addAll(AlwaysAllowed.packages)
            addAll(keyboards)
            home?.let(::add)
            add(packageName)
        }
    }

    // Rules come from storage, not from a parent screen. The service is started
    // by the system at boot, so schedules and limits apply before anyone opens
    // Guardian.
    override fun onCreate() {
        super.onCreate()
        val repository = PolicyRepository(this)
        scope.launch {
            repository.snapshot.collect { sharedPolicy = it }
        }
        scope.launch {
            repository.webFilter.collect { filter ->
                webFilterEnabled = filter.enabled
                if (!filter.enabled) GuardState.clear()
            }
        }
        scope.launch {
            SettingsRepository(applicationContext).uninstallProtectionWanted.collect { wanted ->
                uninstallProtectionWanted = wanted
            }
        }
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        scope.cancel()
        super.onDestroy()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        lastWindowPackage = packageName
        if (packageName in alwaysAllowed) {
            if (BuildConfig.DEBUG) Log.d(TAG, "skip $packageName")
            return
        }
        val className = event.className?.toString()
        val guardKind = guardKindFor(packageName, className)
        if (guardKind != null && raiseGuard(guardKind, className)) return
        if (packageName in SETTINGS_PACKAGES) {
            // Samsung shows SecDeviceAdminAdd as an appcompat dialog, so the
            // event class is androidx.appcompat.app.AlertDialog and the activity
            // name never arrives here. Settings screens also share SubSettings.
            // The resumed activity class from UsageStats is the same package and
            // class information, without reading a single view.
            scope.launch {
                delay(SETTINGS_CLASS_DELAY_MS)
                if (lastWindowPackage != packageName) return@launch
                val resumed = resumedSettingsActivity(packageName) ?: return@launch
                val kind = guardKindFor(packageName, resumed) ?: return@launch
                raiseGuard(kind, resumed)
            }
        }
        val decision = evaluate(packageName)
        if (BuildConfig.DEBUG) Log.d(TAG, "decide $packageName -> $decision")
        if (decision is AccessDecision.Blocked) {
            BlockState.block(packageName, decision.reason)
            runCatching {
                startActivity(
                    Intent(this, BlockActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP,
                    ),
                )
            }
        } else {
            BlockState.clear()
        }
    }

    // Arming depends on the parent's intent, not on the screen: a guard fires
    // only while the matching protection is on, and a correct PIN grants grace
    // so the parent can finish the change they started.
    private fun raiseGuard(kind: GuardKind, className: String?): Boolean {
        val applies = when (kind) {
            GuardKind.WebFilterSettings -> webFilterEnabled
            GuardKind.AdminProtection -> uninstallProtectionWanted
        }
        if (!applies || GuardState.inGrace(kind)) return false
        if (BuildConfig.DEBUG) Log.d(TAG, "guard $kind $className")
        GuardState.guard(kind)
        runCatching {
            startActivity(
                Intent(this, GuardActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP,
                ),
            )
        }
        return true
    }

    private fun resumedSettingsActivity(packageName: String): String? {
        val stats = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val now = System.currentTimeMillis()
        val events = stats.queryEvents(now - SETTINGS_LOOKBACK_MS, now) ?: return null
        val event = UsageEvents.Event()
        var resumed: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType != UsageEvents.Event.ACTIVITY_RESUMED) continue
            if (event.packageName != packageName) continue
            resumed = event.className
        }
        return resumed
    }

    private fun evaluate(packageName: String): AccessDecision {
        val now = System.currentTimeMillis()
        val statsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        val usedMillis = statsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - MILLIS_PER_DAY,
            now,
        )?.firstOrNull { it.packageName == packageName }?.totalTimeInForeground ?: 0L
        return policy.decide(packageName, usedMillis, LocalDateTime.now(), alwaysAllowed)
    }

    override fun onInterrupt() = Unit

    // Package and class only. Measured on the SM-A155F: the system VPN list is
    // com.android.settings.Settings$VpnSettingsActivity. The suffix also catches
    // AOSP's VpnSettings fragment and OEM wrappers. Private DNS is not a
    // standalone screen on this phone, and Quick Settings cannot be guarded.
    // The DeviceAdminAdd suffix plus the Samsung deactivation class cover the
    // uninstall protection guard.
    private fun guardKindFor(packageName: String, className: String?): GuardKind? {
        if (className == null || packageName !in SETTINGS_PACKAGES) return null
        return when {
            className.endsWith("DeviceAdminAdd") || className == SAMSUNG_DEVICE_ADMIN_CLASS ->
                GuardKind.AdminProtection
            className.endsWith("VpnSettingsActivity") -> GuardKind.WebFilterSettings
            className.endsWith(".VpnSettings") -> GuardKind.WebFilterSettings
            else -> null
        }
    }

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
        private const val SETTINGS_CLASS_DELAY_MS = 300L
        private const val SETTINGS_LOOKBACK_MS = 2_000L
        private const val TAG = "Guardian"
        private const val SETTINGS_PACKAGE = "com.android.settings"
        private val SETTINGS_PACKAGES = setOf(SETTINGS_PACKAGE, "com.samsung.android.settings")
        private const val SAMSUNG_DEVICE_ADMIN_CLASS =
            "com.samsung.android.settings.applications.specialaccess.SecDeviceAdminAdd"

        // The platform owns the service lifecycle. The parent removal screen
        // only needs the live instance so the service can disable itself; no
        // other component holds a reference.
        @Volatile
        private var instance: GuardianAccessibilityService? = null

        fun requestStop() {
            instance?.disableSelf()
        }

        // Shared across instances: the platform can create more than one service
        // instance, and the last one is not always the one receiving events.
        @Volatile
        private var sharedPolicy = PolicySnapshot()
    }
}
