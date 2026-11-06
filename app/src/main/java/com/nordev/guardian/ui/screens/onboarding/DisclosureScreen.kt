package com.nordev.guardian.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.StickyActionBar

// One entry per permission; consent is never shared between permissions.
// Type names double as the navigation argument values.
enum class DisclosureType(
    val titleRes: Int,
    val bodyRes: Int,
    val actionRes: Int,
    val icon: ImageVector,
) {
    UsageAccess(
        R.string.perm_usage,
        R.string.disclosure_usage_body,
        R.string.disclosure_action_usage,
        Icons.Outlined.Timelapse,
    ),
    Notifications(
        R.string.perm_notifications,
        R.string.disclosure_notifications_body,
        R.string.disclosure_action_notifications,
        Icons.Outlined.Notifications,
    ),
    Accessibility(
        R.string.perm_accessibility,
        R.string.disclosure_accessibility_body,
        R.string.disclosure_action_accessibility,
        Icons.Outlined.Accessibility,
    ),
    VpnFilter(
        R.string.perm_vpn,
        R.string.disclosure_vpn_body,
        R.string.disclosure_action_vpn,
        Icons.Outlined.VpnKey,
    ),
    DeviceAdmin(
        R.string.perm_admin,
        R.string.disclosure_admin_body,
        R.string.disclosure_action_admin,
        Icons.Outlined.AdminPanelSettings,
    ),
}

// The named action launches no system screen in this step; it only records
// consent by returning to the permissions step. Step 4 wires the system intent.
@Composable
fun DisclosureScreen(
    type: DisclosureType,
    onBack: () -> Unit,
    onConsented: () -> Unit,
) {
    var consented by rememberSaveable { mutableStateOf(false) }
    var showConsentError by rememberSaveable { mutableStateOf(false) }
    val consentLabel = stringResource(R.string.disclosure_consent)
    Column(modifier = Modifier.fillMaxSize()) {
        GuardianTopBar(title = stringResource(type.titleRes), onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(type.bodyRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = consented,
                        role = Role.Checkbox,
                        onValueChange = {
                            consented = it
                            if (it) showConsentError = false
                        },
                    )
                    .semantics { contentDescription = consentLabel },
            ) {
                Checkbox(checked = consented, onCheckedChange = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = consentLabel,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            if (showConsentError) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.disclosure_consent_needed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        StickyActionBar(
            primaryLabel = stringResource(type.actionRes),
            onPrimary = {
                if (consented) {
                    onConsented()
                } else {
                    showConsentError = true
                }
            },
            secondaryLabel = stringResource(R.string.disclosure_not_now),
            onSecondary = onBack,
        )
    }
}
