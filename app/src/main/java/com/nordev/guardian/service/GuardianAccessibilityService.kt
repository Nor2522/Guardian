package com.nordev.guardian.service

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.nordev.guardian.BuildConfig
import com.nordev.guardian.data.prefs.PolicyRepository
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
        if (packageName in alwaysAllowed) {
            if (BuildConfig.DEBUG) Log.d(TAG, "skip $packageName")
            return
        }
        val className = event.className?.toString()
        val guardKind = guardKindFor(packageName, className)
        if (guardKind != null && webFilterEnabled && !GuardState.inGrace(guardKind)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "guard $packageName/$className")
            GuardState.guard(guardKind)
            runCatching {
                startActivity(
                    Intent(this, GuardActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP,
                    ),
                )
            }
            return
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
    private fun guardKindFor(packageName: String, className: String?): GuardKind? {
        if (packageName != SETTINGS_PACKAGE || className == null) return null
        return when {
            className.endsWith("VpnSettingsActivity") -> GuardKind.WebFilterSettings
            className.endsWith(".VpnSettings") -> GuardKind.WebFilterSettings
            else -> null
        }
    }

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
        private const val TAG = "Guardian"
        private const val SETTINGS_PACKAGE = "com.android.settings"

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
