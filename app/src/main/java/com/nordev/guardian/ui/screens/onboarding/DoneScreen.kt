package com.nordev.guardian.ui.screens.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.ProtectionRow

// The three checks never pass in this step, so they are listed as Off — the
// same states the status screen reports once checks ship.
@Composable
fun DoneScreen(
    onBack: () -> Unit,
    onFinishSetup: () -> Unit,
    onOpenStatus: () -> Unit,
) {
    OnboardingShell(
        stepNumber = 5,
        onBack = onBack,
        primaryLabel = stringResource(R.string.onboarding_done_finish),
        onPrimary = onFinishSetup,
        secondaryLabel = stringResource(R.string.onboarding_done_open_status),
        onSecondary = onOpenStatus,
    ) {
        Text(
            text = stringResource(R.string.onboarding_done_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(24.dp))
        ProtectionRow(
            icon = Icons.Outlined.Timelapse,
            title = stringResource(R.string.check_app_limits),
            stateText = stringResource(R.string.permission_state_off),
        )
        ProtectionRow(
            icon = Icons.Outlined.Public,
            title = stringResource(R.string.check_web_filter),
            stateText = stringResource(R.string.permission_state_off),
        )
        ProtectionRow(
            icon = Icons.Outlined.Shield,
            title = stringResource(R.string.check_uninstall),
            stateText = stringResource(R.string.permission_state_off),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_done_missing),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(R.string.onboarding_done_change_later),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
