package com.nordev.guardian.service

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import com.nordev.guardian.domain.block.AccessDecision
import com.nordev.guardian.domain.block.PolicySnapshot
import com.nordev.guardian.ui.nav.BlockState

// Declared so the user can grant Accessibility in system settings. The service
// watches window state changes and asks the policy engine whether the focused
// app is blocked. It does not read node text, perform gestures, or retrieve
// window content.
class GuardianAccessibilityService : AccessibilityService() {
    private var policy = PolicySnapshot(emptyMap())

    fun setPolicy(snapshot: PolicySnapshot) {
        policy = snapshot
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val window = focusedWindow() ?: return
        val packageName = window.root?.packageName?.toString() ?: return
        val decision = evaluate(packageName)
        if (decision is AccessDecision.Blocked) {
            BlockState.block(packageName)
        }
    }

    private fun focusedWindow(): AccessibilityWindowInfo? {
        val windows = getWindows() ?: return null
        return windows.firstOrNull { it.isFocused } ?: windows.firstOrNull { it.isActive }
    }

    private fun evaluate(packageName: String): AccessDecision {
        val now = System.currentTimeMillis()
        val statsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        val usedMillis = statsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - MILLIS_PER_DAY,
            now,
        )?.firstOrNull { it.packageName == packageName }?.totalTimeInForeground ?: 0L
        return policy.decide(packageName, usedMillis)
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
    }
}
