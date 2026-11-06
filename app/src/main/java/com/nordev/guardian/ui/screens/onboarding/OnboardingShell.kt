package com.nordev.guardian.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.StickyActionBar

// Shared onboarding chrome: back-only top bar, thin progress, scrollable body,
// and one pinned primary action. Step titles live in each screen's content.
@Composable
internal fun OnboardingShell(
    stepNumber: Int,
    onBack: () -> Unit,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GuardianTopBar(title = "", onBack = onBack)
        Text(
            text = stringResource(R.string.onboarding_progress, stepNumber),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LinearProgressIndicator(
            progress = { stepNumber / 5f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(4.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            content = content,
        )
        StickyActionBar(
            primaryLabel = primaryLabel,
            onPrimary = onPrimary,
            secondaryLabel = secondaryLabel,
            onSecondary = onSecondary,
        )
    }
}
