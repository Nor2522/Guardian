package com.nordev.guardian.ui.screens.status

import com.nordev.guardian.data.web.WebFilterStatus

// The three protection checks on the status screen. Each one pairs the parent's
// choice with the phone's real state; Off is neutral, Attention means the parent
// asked for a check that cannot run right now.
enum class ProtectionCheck { Limits, Web, Uninstall }

enum class CheckState { On, Off, Attention }

// Reasons are enum values so the model stays testable and the screen owns copy.
enum class CheckIssue {
    LimitsUsage,
    LimitsAccessibility,
    LimitsUsageAndAccessibility,
    WebNeedsConsent,
    WebStopped,
    WebAnotherVpn,
    WebNoNetwork,
    UninstallOff,
}

data class CheckStatus(
    val check: ProtectionCheck,
    val state: CheckState,
    val issue: CheckIssue? = null,
)

enum class StatusHero { Setup, Healthy, Attention, Off }

data class ProtectionOverview(
    val hero: StatusHero,
    val checks: List<CheckStatus>,
) {
    val attentionCount: Int get() = checks.count { it.state == CheckState.Attention }
    val attentionIssue: CheckIssue?
        get() = checks.firstOrNull { it.state == CheckState.Attention }?.issue
}

// One place decides what the status screen says. The screen only supplies the
// measured state and renders the result.
object ProtectionStatusModel {
    fun overview(
        hasPin: Boolean,
        limitsConfigured: Boolean,
        schedulesConfigured: Boolean,
        usageAccessGranted: Boolean,
        accessibilityGranted: Boolean,
        webFilterEnabled: Boolean,
        webFilterStatus: WebFilterStatus,
        uninstallWanted: Boolean,
        adminActive: Boolean,
    ): ProtectionOverview {
        if (!hasPin) {
            return ProtectionOverview(hero = StatusHero.Setup, checks = emptyList())
        }
        val checks = listOf(
            limitsCheck(
                configured = limitsConfigured || schedulesConfigured,
                usageGranted = usageAccessGranted,
                accessibilityGranted = accessibilityGranted,
            ),
            webCheck(webFilterEnabled, webFilterStatus),
            uninstallCheck(uninstallWanted, adminActive),
        )
        val hero = when {
            checks.any { it.state == CheckState.Attention } -> StatusHero.Attention
            checks.any { it.state == CheckState.On } -> StatusHero.Healthy
            else -> StatusHero.Off
        }
        return ProtectionOverview(hero = hero, checks = checks)
    }

    private fun limitsCheck(
        configured: Boolean,
        usageGranted: Boolean,
        accessibilityGranted: Boolean,
    ): CheckStatus {
        if (!configured) return CheckStatus(ProtectionCheck.Limits, CheckState.Off)
        if (usageGranted && accessibilityGranted) {
            return CheckStatus(ProtectionCheck.Limits, CheckState.On)
        }
        val issue = when {
            !usageGranted && !accessibilityGranted -> CheckIssue.LimitsUsageAndAccessibility
            !usageGranted -> CheckIssue.LimitsUsage
            else -> CheckIssue.LimitsAccessibility
        }
        return CheckStatus(ProtectionCheck.Limits, CheckState.Attention, issue)
    }

    private fun webCheck(enabled: Boolean, status: WebFilterStatus): CheckStatus {
        if (!enabled) return CheckStatus(ProtectionCheck.Web, CheckState.Off)
        return when (status) {
            WebFilterStatus.On -> CheckStatus(ProtectionCheck.Web, CheckState.On)
            WebFilterStatus.NeedsConsent ->
                CheckStatus(ProtectionCheck.Web, CheckState.Attention, CheckIssue.WebNeedsConsent)
            WebFilterStatus.AnotherVpn ->
                CheckStatus(ProtectionCheck.Web, CheckState.Attention, CheckIssue.WebAnotherVpn)
            WebFilterStatus.NoNetwork ->
                CheckStatus(ProtectionCheck.Web, CheckState.Attention, CheckIssue.WebNoNetwork)
            // Off and Stopped cannot claim filtering while the switch is on.
            WebFilterStatus.Off, WebFilterStatus.Stopped ->
                CheckStatus(ProtectionCheck.Web, CheckState.Attention, CheckIssue.WebStopped)
        }
    }

    private fun uninstallCheck(wanted: Boolean, adminActive: Boolean): CheckStatus = when {
        adminActive -> CheckStatus(ProtectionCheck.Uninstall, CheckState.On)
        // The parent asked for uninstall protection but the admin is gone.
        wanted -> CheckStatus(
            ProtectionCheck.Uninstall,
            CheckState.Attention,
            CheckIssue.UninstallOff,
        )
        // Never enabled, or deliberately turned off on the removal screen.
        else -> CheckStatus(ProtectionCheck.Uninstall, CheckState.Off)
    }
}
