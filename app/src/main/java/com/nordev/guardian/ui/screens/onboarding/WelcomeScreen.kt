package com.nordev.guardian.ui.screens.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R

@Composable
fun WelcomeScreen(
    onBack: () -> Unit,
    onGetStarted: () -> Unit,
) {
    OnboardingShell(
        stepNumber = 1,
        onBack = onBack,
        primaryLabel = stringResource(R.string.onboarding_get_started),
        onPrimary = onGetStarted,
    ) {
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_p1),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_p2),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
