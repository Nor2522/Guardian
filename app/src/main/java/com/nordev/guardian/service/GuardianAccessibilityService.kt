package com.nordev.guardian.service

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.nordev.guardian.domain.block.AccessDecision
import com.nordev.guardian.domain.block.BlockReason
import com.nordev.guardian.domain.block.PolicySnapshot
import com.nordev.guardian.ui.nav.BlockState
import java.time.LocalDateTime

// Declared so the user can grant Accessibility in system settings. The service
// watches window state changes and asks the policy engine whether the focused
// app is blocked. It does not read node text, perform gestures, or retrieve
// window content.
class GuardianAccessibilityService : AccessibilityService() {
    private val policy: PolicySnapshot
        get() = sharedPolicy

    override fun onCreate() {
        super.onCreate()
        GuardianAccessibilityService.instance = this
    }

    fun setPolicy(snapshot: PolicySnapshot) {
        sharedPolicy = snapshot
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName || packageName == SYSTEM_UI_PACKAGE) return
        val decision = evaluate(packageName)
        if (decision is AccessDecision.Blocked) {
            BlockState.block(packageName, decision.reason)
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
        return policy.decide(packageName, usedMillis, LocalDateTime.now())
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"

        // Shared across instances: the platform can create more than one service
        // instance, and the last one is not always the one receiving events.
        @Volatile
        private var sharedPolicy = PolicySnapshot(emptyMap())

        @Volatile
        var instance: GuardianAccessibilityService? = null
            private set
    }
}
