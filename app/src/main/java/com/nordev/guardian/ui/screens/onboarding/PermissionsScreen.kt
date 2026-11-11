package com.nordev.guardian.ui.screens.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nordev.guardian.R
import com.nordev.guardian.data.permissions.AccessibilityAccessChecker
import com.nordev.guardian.data.permissions.NotificationAccessChecker
import com.nordev.guardian.data.permissions.UsageAccessChecker
import com.nordev.guardian.data.permissions.VpnAccessChecker
import com.nordev.guardian.data.prefs.SettingsRepository
import com.nordev.guardian.ui.theme.LocalGuardianStatusColors

@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onOpenDisclosure: (DisclosureType) -> Unit,
    onOpenUsageSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenVpnSettings: () -> Unit,
    requestNotifications: Boolean,
    onNotificationsRequested: () -> Unit,
    onContinue: () -> Unit,
    onFinishLater: () -> Unit,
) {
    val context = LocalContext.current
    val usageChecker = remember(context) { UsageAccessChecker(context) }
    val notificationChecker = remember(context) { NotificationAccessChecker(context) }
    val accessibilityChecker = remember(context) { AccessibilityAccessChecker(context) }
    val vpnChecker = remember(context) { VpnAccessChecker(context) }
    val settings = remember(context) { SettingsRepository(context) }
    var usageGranted by remember { mutableStateOf(usageChecker.isGranted()) }
    var notificationsGranted by remember { mutableStateOf(notificationChecker.isGranted()) }
    var accessibilityGranted by remember { mutableStateOf(accessibilityChecker.isGranted()) }
    var vpnGranted by remember { mutableStateOf(vpnChecker.isGranted()) }
    val usageConsentAt by settings.usageConsentAt.collectAsState(initial = 0L)
    val notificationConsentAt by settings.notificationConsentAt.collectAsState(initial = 0L)
    val accessibilityConsentAt by settings.accessibilityConsentAt.collectAsState(initial = 0L)
    val vpnConsentAt by settings.vpnConsentAt.collectAsState(initial = 0L)
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> notificationsGranted = granted }
    val lifecycleOwner = LocalLifecycleOwner.current
    var showLaterDialog by rememberSaveable { mutableStateOf(false) }

    // The disclosure action records consent, pops back here, and asks for the
    // runtime permission exactly once. Later taps open the Settings toggle.
    LaunchedEffect(requestNotifications) {
        if (requestNotifications) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            onNotificationsRequested()
        }
    }

    // The system settings screen flips the app-op outside this process, so
    // re-read it every time the permissions step comes back to the foreground.
    DisposableEffect(lifecycleOwner, usageChecker, notificationChecker, accessibilityChecker, vpnChecker) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageGranted = usageChecker.isGranted()
                notificationsGranted = notificationChecker.isGranted()
                accessibilityGranted = accessibilityChecker.isGranted()
                vpnGranted = vpnChecker.isGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val usageActionLabel = stringResource(
        if (usageGranted || usageConsentAt > 0L) R.string.action_open_settings else R.string.permission_set_up,
    )
    val notificationActionLabel = stringResource(
        if (notificationsGranted || notificationConsentAt > 0L) {
            R.string.action_open_settings
        } else {
            R.string.permission_set_up
        },
    )
    val accessibilityActionLabel = stringResource(
        if (accessibilityGranted || accessibilityConsentAt > 0L) {
            R.string.action_open_settings
        } else {
            R.string.permission_set_up
        },
    )
    val vpnActionLabel = stringResource(
        if (vpnGranted || vpnConsentAt > 0L) R.string.action_open_settings else R.string.permission_set_up,
    )
    OnboardingShell(
        stepNumber = 4,
        onBack = onBack,
        primaryLabel = stringResource(R.string.onboarding_continue),
        onPrimary = onContinue,
        secondaryLabel = stringResource(R.string.onboarding_finish_later),
        onSecondary = { showLaterDialog = true },
    ) {
        Text(
            text = stringResource(R.string.onboarding_permissions_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_permissions_body),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))
        PermissionCard(
            icon = Icons.Outlined.Timelapse,
            name = stringResource(R.string.perm_usage),
            reason = stringResource(R.string.perm_usage_reason),
            status = stringResource(
                if (usageGranted) R.string.permission_state_on else R.string.permission_state_off,
            ),
            statusOn = usageGranted,
            setupLabel = usageActionLabel,
            onSetup = {
                if (usageGranted || usageConsentAt > 0L) {
                    onOpenUsageSettings()
                } else {
                    onOpenDisclosure(DisclosureType.UsageAccess)
                }
            },
        )
        PermissionCard(
            icon = Icons.Outlined.Notifications,
            name = stringResource(R.string.perm_notifications),
            reason = stringResource(R.string.perm_notifications_reason),
            status = stringResource(
                if (notificationsGranted) R.string.permission_state_on else R.string.permission_state_off,
            ),
            statusOn = notificationsGranted,
            setupLabel = notificationActionLabel,
            onSetup = {
                if (notificationsGranted || notificationConsentAt > 0L) {
                    onOpenNotificationSettings()
                } else {
                    onOpenDisclosure(DisclosureType.Notifications)
                }
            },
        )
        PermissionCard(
            icon = Icons.Outlined.Accessibility,
            name = stringResource(R.string.perm_accessibility),
            reason = stringResource(R.string.perm_accessibility_reason),
            status = stringResource(
                if (accessibilityGranted) R.string.permission_state_on else R.string.permission_state_off,
            ),
            statusOn = accessibilityGranted,
            setupLabel = accessibilityActionLabel,
            onSetup = {
                if (accessibilityGranted || accessibilityConsentAt > 0L) {
                    onOpenAccessibilitySettings()
                } else {
                    onOpenDisclosure(DisclosureType.Accessibility)
                }
            },
        )
        PermissionCard(
            icon = Icons.Outlined.VpnKey,
            name = stringResource(R.string.perm_vpn),
            reason = stringResource(R.string.perm_vpn_reason),
            status = stringResource(
                if (vpnGranted) R.string.permission_state_on else R.string.permission_state_off,
            ),
            statusOn = vpnGranted,
            setupLabel = vpnActionLabel,
            onSetup = {
                if (vpnGranted || vpnConsentAt > 0L) {
                    onOpenVpnSettings()
                } else {
                    onOpenDisclosure(DisclosureType.VpnFilter)
                }
            },
        )
        PermissionCard(
            icon = Icons.Outlined.AdminPanelSettings,
            name = stringResource(R.string.perm_admin),
            reason = stringResource(R.string.perm_admin_reason),
            status = stringResource(R.string.permission_state_off),
            statusOn = false,
            setupLabel = stringResource(R.string.permission_set_up),
            onSetup = { onOpenDisclosure(DisclosureType.DeviceAdmin) },
        )
        Spacer(Modifier.height(16.dp))
    }
    if (showLaterDialog) {
        AlertDialog(
            onDismissRequest = { showLaterDialog = false },
            title = { Text(text = stringResource(R.string.onboarding_later_title)) },
            text = { Text(text = stringResource(R.string.onboarding_later_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showLaterDialog = false
                    onFinishLater()
                }) {
                    Text(text = stringResource(R.string.onboarding_finish_later))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLaterDialog = false }) {
                    Text(text = stringResource(R.string.onboarding_cancel))
                }
            },
        )
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    name: String,
    reason: String,
    status: String,
    statusOn: Boolean,
    setupLabel: String,
    onSetup: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 56.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = CircleShape),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (statusOn) {
                        LocalGuardianStatusColors.current.success
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onSetup) {
                Text(text = setupLabel)
            }
        }
    }
}
