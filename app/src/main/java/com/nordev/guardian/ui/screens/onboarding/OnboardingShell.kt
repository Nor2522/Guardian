package com.nordev.guardian.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.StepProgress
import com.nordev.guardian.ui.components.StickyActionBar
import com.nordev.guardian.ui.theme.GuardianSpacing

// Shared onboarding chrome: back-only top bar, thin progress, scrollable body,
// and one pinned primary action. Step titles live in each screen's content.
@Composable
internal fun OnboardingShell(
    stepNumber: Int,
    onBack: () -> Unit,
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GuardianTopBar(
            title = "",
            onBack = onBack,
            modifier = Modifier.fillMaxWidth(),
        )
        Column(
            modifier = Modifier
                .widthIn(max = GuardianSpacing.maxContentWidth)
                .fillMaxWidth()
                .padding(horizontal = GuardianSpacing.gutter),
        ) {
            Text(
                text = stringResource(R.string.onboarding_progress, stepNumber),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            StepProgress(
                step = stepNumber,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = GuardianSpacing.maxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GuardianSpacing.gutter, vertical = 8.dp),
            content = content,
        )
        StickyActionBar(
            primaryLabel = primaryLabel,
            onPrimary = onPrimary,
            primaryEnabled = primaryEnabled,
            secondaryLabel = secondaryLabel,
            onSecondary = onSecondary,
        )
    }
}
