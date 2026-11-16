package com.nordev.guardian.ui.screens.status

import com.nordev.guardian.data.web.WebFilterStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProtectionStatusModelTest {
    private fun overview(
        hasPin: Boolean = true,
        limitsConfigured: Boolean = true,
        schedulesConfigured: Boolean = false,
        usageAccessGranted: Boolean = true,
        accessibilityGranted: Boolean = true,
        webFilterEnabled: Boolean = true,
        webFilterStatus: WebFilterStatus = WebFilterStatus.On,
        uninstallWanted: Boolean = true,
        adminActive: Boolean = true,
    ) = ProtectionStatusModel.overview(
        hasPin = hasPin,
        limitsConfigured = limitsConfigured,
        schedulesConfigured = schedulesConfigured,
        usageAccessGranted = usageAccessGranted,
        accessibilityGranted = accessibilityGranted,
        webFilterEnabled = webFilterEnabled,
        webFilterStatus = webFilterStatus,
        uninstallWanted = uninstallWanted,
        adminActive = adminActive,
    )

    @Test
    fun noPinHidesChecksAndAsksForSetup() {
        val result = overview(hasPin = false)
        assertEquals(StatusHero.Setup, result.hero)
        assertEquals(emptyList<CheckStatus>(), result.checks)
    }

    @Test
    fun everyWorkingCheckIsHealthy() {
        val result = overview()
        assertEquals(StatusHero.Healthy, result.hero)
        assertEquals(0, result.attentionCount)
        assertEquals(
            listOf(CheckState.On, CheckState.On, CheckState.On),
            result.checks.map { it.state },
        )
    }

    @Test
    fun configuredLimitsNeedBothUsageAccessAndAccessibility() {
        val usageOff = overview(usageAccessGranted = false)
        assertEquals(CheckState.Attention, usageOff.checks[0].state)
        assertEquals(CheckIssue.LimitsUsage, usageOff.checks[0].issue)

        val accessibilityOff = overview(accessibilityGranted = false)
        assertEquals(CheckState.Attention, accessibilityOff.checks[0].state)
        assertEquals(CheckIssue.LimitsAccessibility, accessibilityOff.checks[0].issue)

        val bothOff = overview(usageAccessGranted = false, accessibilityGranted = false)
        assertEquals(CheckState.Attention, bothOff.checks[0].state)
        assertEquals(CheckIssue.LimitsUsageAndAccessibility, bothOff.checks[0].issue)
    }

    @Test
    fun aScheduleCountsAsConfiguredLimits() {
        val result = overview(limitsConfigured = false, schedulesConfigured = true)
        assertEquals(CheckState.On, result.checks[0].state)
    }

    @Test
    fun noRulesLeavesLimitsOffAndNeutral() {
        val result = overview(
            limitsConfigured = false,
            schedulesConfigured = false,
            usageAccessGranted = false,
            accessibilityGranted = false,
        )
        assertEquals(CheckState.Off, result.checks[0].state)
        assertNull(result.checks[0].issue)
    }

    @Test
    fun webFilterOffIsNeutralEvenWhenTheTunnelIsDown() {
        val result = overview(webFilterEnabled = false, webFilterStatus = WebFilterStatus.Stopped)
        assertEquals(CheckState.Off, result.checks[1].state)
        assertNull(result.checks[1].issue)
    }

    @Test
    fun webFilterOnReportsTheMatchingProblem() {
        mapOf(
            WebFilterStatus.NeedsConsent to CheckIssue.WebNeedsConsent,
            WebFilterStatus.Stopped to CheckIssue.WebStopped,
            WebFilterStatus.AnotherVpn to CheckIssue.WebAnotherVpn,
            WebFilterStatus.NoNetwork to CheckIssue.WebNoNetwork,
        ).forEach { (status, issue) ->
            val result = overview(webFilterStatus = status)
            assertEquals(status.name, CheckState.Attention, result.checks[1].state)
            assertEquals(status.name, issue, result.checks[1].issue)
        }
    }

    @Test
    fun uninstallIsAttentionOnlyWhenTheParentAskedForIt() {
        val broken = overview(adminActive = false)
        assertEquals(CheckState.Attention, broken.checks[2].state)
        assertEquals(CheckIssue.UninstallOff, broken.checks[2].issue)

        val neverEnabled = overview(uninstallWanted = false, adminActive = false)
        assertEquals(CheckState.Off, neverEnabled.checks[2].state)
        assertNull(neverEnabled.checks[2].issue)
    }

    @Test
    fun allOffIsTheOffHero() {
        val result = overview(
            limitsConfigured = false,
            webFilterEnabled = false,
            uninstallWanted = false,
            adminActive = false,
        )
        assertEquals(StatusHero.Off, result.hero)
        assertEquals(
            listOf(CheckState.Off, CheckState.Off, CheckState.Off),
            result.checks.map { it.state },
        )
    }

    @Test
    fun anyAttentionWinsOverTheOnChecks() {
        val result = overview(webFilterStatus = WebFilterStatus.Stopped)
        assertEquals(StatusHero.Attention, result.hero)
        assertEquals(1, result.attentionCount)
        assertEquals(CheckIssue.WebStopped, result.attentionIssue)
    }
}
