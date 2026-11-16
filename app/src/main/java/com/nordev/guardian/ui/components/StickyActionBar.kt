package com.nordev.guardian.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.GuardianSizes
import com.nordev.guardian.ui.theme.GuardianSpacing

// One primary action pinned above the system inset, with an optional text action.
// Callers scroll their own content above it.
@Composable
fun StickyActionBar(
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    primaryDestructive: Boolean = false,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = GuardianSpacing.gutter, end = GuardianSpacing.gutter, bottom = GuardianSpacing.gutter),
    ) {
        GuardianPrimaryButton(
            label = primaryLabel,
            onClick = onPrimary,
            enabled = primaryEnabled,
            destructive = primaryDestructive,
        )
        if (secondaryLabel != null && onSecondary != null) {
            Spacer(Modifier.height(GuardianSpacing.sm))
            TextButton(
                onClick = onSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = GuardianSizes.minTouch),
            ) {
                Text(text = secondaryLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
