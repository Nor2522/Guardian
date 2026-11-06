package com.nordev.guardian.ui.screens.onboarding

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R

@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onOpenDisclosure: (DisclosureType) -> Unit,
    onContinue: () -> Unit,
    onFinishLater: () -> Unit,
) {
    var showLaterDialog by rememberSaveable { mutableStateOf(false) }
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
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_permissions_build_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        PermissionCard(
            icon = Icons.Outlined.Timelapse,
            name = stringResource(R.string.perm_usage),
            reason = stringResource(R.string.perm_usage_reason),
            setupLabel = stringResource(R.string.permission_set_up),
            onSetup = { onOpenDisclosure(DisclosureType.UsageAccess) },
        )
        PermissionCard(
            icon = Icons.Outlined.Notifications,
            name = stringResource(R.string.perm_notifications),
            reason = stringResource(R.string.perm_notifications_reason),
            setupLabel = stringResource(R.string.permission_set_up),
            onSetup = { onOpenDisclosure(DisclosureType.Notifications) },
        )
        PermissionCard(
            icon = Icons.Outlined.Accessibility,
            name = stringResource(R.string.perm_accessibility),
            reason = stringResource(R.string.perm_accessibility_reason),
            setupLabel = stringResource(R.string.permission_set_up),
            onSetup = { onOpenDisclosure(DisclosureType.Accessibility) },
        )
        PermissionCard(
            icon = Icons.Outlined.VpnKey,
            name = stringResource(R.string.perm_vpn),
            reason = stringResource(R.string.perm_vpn_reason),
            setupLabel = stringResource(R.string.permission_set_up),
            onSetup = { onOpenDisclosure(DisclosureType.VpnFilter) },
        )
        PermissionCard(
            icon = Icons.Outlined.AdminPanelSettings,
            name = stringResource(R.string.perm_admin),
            reason = stringResource(R.string.perm_admin_reason),
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
                    text = stringResource(R.string.permission_state_off),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onSetup) {
                Text(text = setupLabel)
            }
        }
    }
}
