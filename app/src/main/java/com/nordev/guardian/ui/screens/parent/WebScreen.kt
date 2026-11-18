package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nordev.guardian.R
import com.nordev.guardian.data.web.UpstreamHealth
import com.nordev.guardian.data.web.WebFilterStatus
import com.nordev.guardian.data.web.WebFilterStatusReader
import com.nordev.guardian.domain.web.DomainName
import com.nordev.guardian.domain.web.WebFilterPolicy
import com.nordev.guardian.ui.components.EmptyState
import com.nordev.guardian.ui.components.GuardianCard
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.IconBadge
import com.nordev.guardian.ui.components.ProblemCard
import com.nordev.guardian.ui.components.ProblemSeverity
import com.nordev.guardian.ui.components.SectionHeader
import com.nordev.guardian.ui.theme.GuardianSizes
import com.nordev.guardian.ui.theme.GuardianSpacing
import com.nordev.guardian.ui.theme.LocalGuardianGradients
import com.nordev.guardian.ui.theme.LocalGuardianStatusColors
import java.text.NumberFormat
import kotlinx.coroutines.delay

// Status is read from the system on resume and while the tab is visible, so the
// card never keeps a green "on" after the tunnel stops or consent is revoked.
@Composable
fun WebScreen(
    policy: WebFilterPolicy,
    adultListVersion: String,
    adultListCount: Int,
    onOpenSettings: () -> Unit,
    onSetEnabled: (Boolean) -> Unit,
    onRequestTurnOff: () -> Unit,
    onAddSite: () -> Unit,
    onRemoveSite: (String) -> Unit,
    onTurnOnFilter: () -> Unit,
    onOpenVpnSettings: () -> Unit,
    onOpenNetworkSettings: () -> Unit,
) {
    val context = LocalContext.current
    val reader = remember(context) { WebFilterStatusReader(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var status by remember { mutableStateOf(WebFilterStatus.Off) }
    var upstream by remember { mutableStateOf(UpstreamHealth.state) }
    var resumeTick by remember { mutableIntStateOf(0) }
    var pendingRemoval by rememberSaveable { mutableStateOf<String?>(null) }
    var showHowItWorks by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, policy.enabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                status = reader.read(policy.enabled)
                resumeTick += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // A green tunnel is not proof the filter works: CleanBrowsing has to answer.
    // The probe runs when the tab is shown and again after every resume.
    LaunchedEffect(status, resumeTick) {
        if (status == WebFilterStatus.On) upstream = UpstreamHealth.probe()
    }

    LaunchedEffect(policy.enabled) {
        while (true) {
            status = reader.read(policy.enabled)
            delay(2_000)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GuardianTopBar(
                title = stringResource(R.string.tab_web),
                onSettings = onOpenSettings,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            WebStatusCard(
                status = status,
                upstream = upstream,
                onTurnOnFilter = onTurnOnFilter,
                onOpenVpnSettings = onOpenVpnSettings,
                onOpenNetworkSettings = onOpenNetworkSettings,
            )
            Spacer(Modifier.height(16.dp))
            FilterSwitchRow(
                enabled = policy.enabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        onSetEnabled(true)
                    } else {
                        onRequestTurnOff()
                    }
                },
            )
            Spacer(Modifier.height(24.dp))
            SectionHeader(
                title = stringResource(R.string.section_blocked_sites),
                count = pluralStringResource(
                    R.plurals.web_site_count,
                    policy.customDomains.size,
                    policy.customDomains.size,
                ),
            )
            if (policy.customDomains.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.empty_web),
                )
            } else {
                GuardianCard {
                    policy.customDomains.sorted().forEachIndexed { index, domain ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = LocalGuardianGradients.current.cardBorder,
                                thickness = GuardianSpacing.hairline,
                            )
                        }
                        SiteRow(
                            domain = domain,
                            onRemove = { pendingRemoval = domain },
                        )
                    }
                }
            }
            Spacer(Modifier.height(GuardianSpacing.lg))
            OutlinedButton(
                onClick = onAddSite,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = GuardianSizes.minTouch),
            ) {
                Text(
                    text = stringResource(R.string.web_add_site),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(
                    R.string.web_adult_list_info,
                    adultListVersion,
                    NumberFormat.getIntegerInstance().format(adultListCount),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = { showHowItWorks = true },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(
                    text = stringResource(R.string.web_how_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(8.dp))
            ProblemCard(
                severity = ProblemSeverity.Informational,
                title = stringResource(R.string.web_help_title),
                body = stringResource(R.string.web_help_body),
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    pendingRemoval?.let { domain ->
        RemoveSiteDialog(
            domain = domain,
            onDismiss = { pendingRemoval = null },
            onConfirm = {
                pendingRemoval = null
                onRemoveSite(domain)
            },
        )
    }

    if (showHowItWorks) {
        HowFilteringWorksSheet(onDismiss = { showHowItWorks = false })
    }
}

@Composable
private fun WebStatusCard(
    status: WebFilterStatus,
    upstream: UpstreamHealth.State,
    onTurnOnFilter: () -> Unit,
    onOpenVpnSettings: () -> Unit,
    onOpenNetworkSettings: () -> Unit,
) {
    when (status) {
        WebFilterStatus.On -> if (upstream == UpstreamHealth.State.Unreachable) {
            ProblemCard(
                severity = ProblemSeverity.Blocking,
                title = stringResource(R.string.web_status_upstream_title),
                body = stringResource(R.string.web_status_upstream_body),
            )
        } else {
            OnStatusCard()
        }
        WebFilterStatus.Off -> ProblemCard(
            severity = ProblemSeverity.Informational,
            title = stringResource(R.string.web_filter_off_title),
            body = stringResource(R.string.web_filter_off_body),
        )
        WebFilterStatus.NeedsConsent -> ProblemCard(
            severity = ProblemSeverity.Blocking,
            title = stringResource(R.string.web_status_needs_consent_title),
            body = stringResource(R.string.web_status_needs_consent_body),
            actionLabel = stringResource(R.string.web_action_turn_on),
            onAction = onTurnOnFilter,
        )
        WebFilterStatus.Stopped -> ProblemCard(
            severity = ProblemSeverity.Blocking,
            title = stringResource(R.string.web_status_stopped_title),
            body = stringResource(R.string.web_status_stopped_body),
            actionLabel = stringResource(R.string.web_action_turn_on),
            onAction = onTurnOnFilter,
        )
        WebFilterStatus.AnotherVpn -> ProblemCard(
            severity = ProblemSeverity.Blocking,
            title = stringResource(R.string.web_status_another_vpn_title),
            body = stringResource(R.string.web_status_another_vpn_body),
            actionLabel = stringResource(R.string.web_action_open_vpn_settings),
            onAction = onOpenVpnSettings,
        )
        WebFilterStatus.NoNetwork -> ProblemCard(
            severity = ProblemSeverity.Blocking,
            title = stringResource(R.string.web_status_no_network_title),
            body = stringResource(R.string.web_status_no_network_body),
            actionLabel = stringResource(R.string.web_action_open_network_settings),
            onAction = onOpenNetworkSettings,
        )
    }
}

@Composable
private fun OnStatusCard() {
    val statusColors = LocalGuardianStatusColors.current
    GuardianCard(
        containerColor = statusColors.successContainer,
        contentColor = statusColors.onSuccessContainer,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(GuardianSpacing.lg),
        ) {
            IconBadge(
                icon = Icons.Outlined.Shield,
                containerColor = statusColors.success,
                contentColor = statusColors.onSuccessContainer,
            )
            Spacer(Modifier.width(GuardianSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.web_status_on_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(GuardianSpacing.xxs))
                Text(
                    text = stringResource(R.string.web_status_on_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun FilterSwitchRow(
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    GuardianCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = GuardianSizes.minTouch)
                .padding(GuardianSpacing.lg),
        ) {
            IconBadge(icon = Icons.Outlined.Public)
            Spacer(Modifier.width(GuardianSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.web_filter_switch),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(GuardianSpacing.xxs))
                Text(
                    text = stringResource(R.string.web_filter_switch_supporting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(GuardianSpacing.md))
            Switch(
                checked = enabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun SiteRow(
    domain: String,
    onRemove: () -> Unit,
) {
    val display = DomainName.display(domain)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = GuardianSizes.minTouch)
            .padding(start = GuardianSpacing.lg, end = GuardianSpacing.xs),
    ) {
        Text(
            text = display,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.web_remove_site_cd, display),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RemoveSiteDialog(
    domain: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val cancelFocus = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    R.string.web_remove_site_title,
                    DomainName.display(domain),
                ),
            )
        },
        text = { Text(text = stringResource(R.string.web_remove_site_body)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(text = stringResource(R.string.web_remove))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.focusRequester(cancelFocus),
            ) {
                Text(text = stringResource(R.string.web_cancel))
            }
        },
    )
    LaunchedEffect(cancelFocus) { cancelFocus.requestFocus() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HowFilteringWorksSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.web_how_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.web_how_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(
                    text = stringResource(R.string.web_how_close),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
