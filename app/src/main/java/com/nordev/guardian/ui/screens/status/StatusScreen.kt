package com.nordev.guardian.ui.screens.status

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nordev.guardian.R
import com.nordev.guardian.data.permissions.AccessibilityAccessChecker
import com.nordev.guardian.data.permissions.DeviceAdminAccessChecker
import com.nordev.guardian.data.permissions.UsageAccessChecker
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.data.prefs.PolicyRepository
import com.nordev.guardian.data.prefs.SettingsRepository
import com.nordev.guardian.data.usage.UsageSnapshot
import com.nordev.guardian.data.web.WebFilterStatus
import com.nordev.guardian.data.web.WebFilterStatusReader
import com.nordev.guardian.ui.components.GuardianCard
import com.nordev.guardian.ui.components.GuardianPrimaryButton
import com.nordev.guardian.ui.components.IconBadge
import com.nordev.guardian.ui.components.MetricCard
import com.nordev.guardian.ui.components.ProtectionRow
import com.nordev.guardian.ui.components.ProtectionState
import com.nordev.guardian.ui.components.SectionHeader
import com.nordev.guardian.ui.components.StatusHero
import com.nordev.guardian.ui.components.StatusHeroState
import com.nordev.guardian.ui.components.durationText
import com.nordev.guardian.ui.theme.GuardianSpacing
import kotlinx.coroutines.delay

// Where the gate should land after a successful unlock. The pending target
// never reaches the child: it is held by the nav host behind the gate.
enum class StatusGateTarget { Parent, Permissions, Web }

private const val SLOW_TICK_MILLIS = 5_000L

// Every check pairs the parent's stored choice with the phone's real state.
// The screen re-reads the system on resume and on a slow tick, never caching an
// On that the system has already revoked.
@Composable
fun StatusScreen(
    onOpenGate: (StatusGateTarget) -> Unit,
    usage: UsageSnapshot?,
) {
    val context = LocalContext.current
    val pinRepository = remember { ParentPinRepository(context) }
    val policyRepository = remember { PolicyRepository(context) }
    val settingsRepository = remember { SettingsRepository(context) }
    val usageChecker = remember { UsageAccessChecker(context) }
    val accessibilityChecker = remember { AccessibilityAccessChecker(context) }
    val adminChecker = remember { DeviceAdminAccessChecker(context) }
    val webStatusReader = remember { WebFilterStatusReader(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val policy by policyRepository.snapshot.collectAsState(initial = null)
    val webFilter by policyRepository.webFilter.collectAsState(initial = null)
    val uninstallWanted by settingsRepository.uninstallProtectionWanted
        .collectAsState(initial = false)

    var hasPin by remember { mutableStateOf<Boolean?>(null) }
    var usageGranted by remember { mutableStateOf(false) }
    var accessibilityGranted by remember { mutableStateOf(false) }
    var adminActive by remember { mutableStateOf(false) }
    var webStatus by remember { mutableStateOf(WebFilterStatus.Off) }
    var refreshToken by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshToken++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(SLOW_TICK_MILLIS)
            refreshToken++
        }
    }

    LaunchedEffect(refreshToken, webFilter?.enabled) {
        hasPin = pinRepository.state().hasPin
        usageGranted = usageChecker.isGranted()
        accessibilityGranted = accessibilityChecker.isGranted()
        adminActive = adminChecker.isGranted()
        webStatus = webStatusReader.read(webFilter?.enabled == true)
    }

    // A pre-existing admin (for example after an update) still counts as the
    // parent's choice. Record it so a later deactivation reads as broken.
    LaunchedEffect(adminActive, uninstallWanted) {
        if (adminActive && !uninstallWanted) {
            settingsRepository.setUninstallProtectionWanted(true)
        }
    }

    val loadedPolicy = policy
    val loadedFilter = webFilter
    val overview = if (hasPin != null && loadedPolicy != null && loadedFilter != null) {
        ProtectionStatusModel.overview(
            hasPin = hasPin == true,
            limitsConfigured = loadedPolicy.limits.isNotEmpty(),
            schedulesConfigured = loadedPolicy.schedules.any { it.enabled },
            usageAccessGranted = usageGranted,
            accessibilityGranted = accessibilityGranted,
            webFilterEnabled = loadedFilter.enabled,
            webFilterStatus = webStatus,
            uninstallWanted = uninstallWanted,
            adminActive = adminActive,
        )
    } else {
        null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight()
                .widthIn(max = GuardianSpacing.maxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = GuardianSpacing.gutter),
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Outlined.Shield,
                    size = 40.dp,
                    iconSize = 22.dp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    halo = false,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.height(24.dp))
            if (overview == null) {
                // Local reads finish in a frame or two. Keep the layout stable
                // instead of guessing at a protection state.
                Spacer(Modifier.height(148.dp))
            } else {
                StatusHero(
                    state = overview.hero.toHeroState(),
                    title = heroTitle(overview),
                    body = heroBody(overview),
                )
            }
            Spacer(Modifier.height(24.dp))
            MetricCard(
                label = stringResource(R.string.screen_time_today_label),
                value = when {
                    usage == null -> stringResource(R.string.usage_loading)
                    !usage.accessGranted -> stringResource(R.string.usage_unavailable)
                    else -> durationText(
                        millis = usage.todayMillis,
                        noneLabel = stringResource(R.string.usage_today_none),
                    )
                },
                chipLabel = if (usage?.estimated == true) {
                    stringResource(R.string.estimated)
                } else {
                    null
                },
            )
            if (overview != null && overview.checks.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionHeader(
                    title = stringResource(R.string.status_checks_title),
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(8.dp))
                GuardianCard {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        overview.checks.forEachIndexed { index, check ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant
                                        .copy(alpha = 0.5f),
                                )
                            }
                            ProtectionRow(
                                icon = check.check.icon(),
                                title = stringResource(check.check.titleRes()),
                                stateText = stringResource(check.state.labelRes()),
                                state = check.state.protectionState(),
                                reason = check.issue?.let { stringResource(it.textRes()) },
                                actionLabel = if (check.state == CheckState.Attention) {
                                    stringResource(R.string.check_review)
                                } else {
                                    null
                                },
                                onAction = if (check.state == CheckState.Attention) {
                                    { onOpenGate(check.check.gateTarget()) }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            when (hasPin) {
                null -> Spacer(Modifier.height(52.dp))
                true -> FilledTonalButton(
                    onClick = { onOpenGate(StatusGateTarget.Parent) },
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.gate_title))
                }
                false -> GuardianPrimaryButton(
                    label = stringResource(R.string.status_finish_setup),
                    onClick = { onOpenGate(StatusGateTarget.Parent) },
                    icon = Icons.Outlined.Lock,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.status_footer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun heroTitle(overview: ProtectionOverview): String = stringResource(
    when (overview.hero) {
        StatusHero.Setup -> R.string.hero_setup_title
        StatusHero.Healthy -> R.string.hero_healthy_title
        StatusHero.Attention -> R.string.hero_attention_title
        StatusHero.Off -> R.string.hero_off_title
    },
)

@Composable
private fun heroBody(overview: ProtectionOverview): String = when (overview.hero) {
    StatusHero.Setup -> stringResource(R.string.hero_setup_body)
    StatusHero.Healthy -> stringResource(R.string.hero_healthy_body)
    StatusHero.Off -> stringResource(R.string.hero_off_body)
    StatusHero.Attention -> {
        val issue = overview.attentionIssue
        if (overview.attentionCount == 1 && issue != null) {
            stringResource(issue.textRes())
        } else {
            pluralStringResource(
                R.plurals.hero_attention_many,
                overview.attentionCount,
                overview.attentionCount,
            )
        }
    }
}

private fun StatusHero.toHeroState(): StatusHeroState = when (this) {
    StatusHero.Setup -> StatusHeroState.Setup
    StatusHero.Healthy -> StatusHeroState.Healthy
    StatusHero.Attention -> StatusHeroState.Attention
    StatusHero.Off -> StatusHeroState.Off
}

private fun ProtectionCheck.titleRes(): Int = when (this) {
    ProtectionCheck.Limits -> R.string.check_app_limits
    ProtectionCheck.Web -> R.string.check_web_filter
    ProtectionCheck.Uninstall -> R.string.check_uninstall
}

private fun ProtectionCheck.icon() = when (this) {
    ProtectionCheck.Limits -> Icons.Outlined.Timelapse
    ProtectionCheck.Web -> Icons.Outlined.Public
    ProtectionCheck.Uninstall -> Icons.Outlined.Shield
}

private fun ProtectionCheck.gateTarget(): StatusGateTarget = when (this) {
    // Missing system access is fixed on the permission list.
    ProtectionCheck.Limits, ProtectionCheck.Uninstall -> StatusGateTarget.Permissions
    // The Web tab owns consent, the filter switch, and the problem cards.
    ProtectionCheck.Web -> StatusGateTarget.Web
}

private fun CheckState.labelRes(): Int = when (this) {
    CheckState.On -> R.string.permission_state_on
    CheckState.Off -> R.string.permission_state_off
    CheckState.Attention -> R.string.check_state_attention
}

private fun CheckState.protectionState(): ProtectionState = when (this) {
    CheckState.On -> ProtectionState.On
    CheckState.Off -> ProtectionState.Off
    CheckState.Attention -> ProtectionState.Attention
}

private fun CheckIssue.textRes(): Int = when (this) {
    CheckIssue.LimitsUsage -> R.string.check_limits_issue_usage
    CheckIssue.LimitsAccessibility -> R.string.check_limits_issue_accessibility
    CheckIssue.LimitsUsageAndAccessibility -> R.string.check_limits_issue_both
    CheckIssue.WebNeedsConsent -> R.string.check_web_issue_consent
    CheckIssue.WebStopped -> R.string.check_web_issue_stopped
    CheckIssue.WebAnotherVpn -> R.string.check_web_issue_another_vpn
    CheckIssue.WebNoNetwork -> R.string.check_web_issue_no_network
    CheckIssue.UninstallOff -> R.string.check_uninstall_issue
}
