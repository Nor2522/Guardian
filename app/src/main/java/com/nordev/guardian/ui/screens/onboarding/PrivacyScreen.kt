package com.nordev.guardian.ui.screens.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.openPrivacyPolicy

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onIUnderstand: () -> Unit,
) {
    var showSummary by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    OnboardingShell(
        stepNumber = 2,
        onBack = onBack,
        primaryLabel = stringResource(R.string.onboarding_privacy_i_understand),
        onPrimary = onIUnderstand,
    ) {
        Text(
            text = stringResource(R.string.onboarding_privacy_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(24.dp))
        PrivacyBlock(Icons.Outlined.Timelapse, stringResource(R.string.onboarding_privacy_p1))
        PrivacyBlock(Icons.Outlined.Public, stringResource(R.string.onboarding_privacy_p2))
        PrivacyBlock(Icons.Outlined.Shield, stringResource(R.string.onboarding_privacy_p3))
        Spacer(Modifier.height(8.dp))
        // A real browser opens the hosted policy; a phone without one still
        // gets the summary instead of a dead link.
        TextButton(
            onClick = {
                if (!openPrivacyPolicy(context)) showSummary = true
            },
        ) {
            Text(text = stringResource(R.string.onboarding_read_policy))
        }
    }
    if (showSummary) {
        AlertDialog(
            onDismissRequest = { showSummary = false },
            title = { Text(text = stringResource(R.string.privacy_summary_title)) },
            text = { Text(text = stringResource(R.string.privacy_summary_body)) },
            confirmButton = {
                TextButton(onClick = { showSummary = false }) {
                    Text(text = stringResource(R.string.dialog_close))
                }
            },
        )
    }
}

@Composable
private fun PrivacyBlock(
    icon: ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
