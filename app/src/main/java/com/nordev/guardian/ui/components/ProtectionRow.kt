package com.nordev.guardian.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.LocalGuardianStatusColors

// On, Off, and Attention let the row choose a tone and icon tint without
// guessing from localized text. Unknown stays neutral for previews and callers
// that only have a label.
enum class ProtectionState { On, Off, Attention, Unknown }

// The state is spelled out in text next to the icon, never as color alone.
// Row merges into one TalkBack node; the text button stays a separate focus target.
@Composable
fun ProtectionRow(
    icon: ImageVector,
    title: String,
    stateText: String,
    modifier: Modifier = Modifier,
    state: ProtectionState = ProtectionState.Unknown,
    reason: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val status = LocalGuardianStatusColors.current
    val tone = when (state) {
        ProtectionState.On -> GuardianTone.Success
        ProtectionState.Attention -> GuardianTone.Attention
        ProtectionState.Off, ProtectionState.Unknown -> GuardianTone.Neutral
    }
    val iconContainer = when (state) {
        ProtectionState.On -> status.successContainer
        ProtectionState.Attention -> status.warningContainer
        ProtectionState.Off, ProtectionState.Unknown -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val iconContent = when (state) {
        ProtectionState.On -> status.success
        ProtectionState.Attention -> status.warning
        ProtectionState.Off, ProtectionState.Unknown -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val reasonColor: Color = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = 72.dp)
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) {},
    ) {
        IconBadge(
            icon = icon,
            size = 44.dp,
            iconSize = 21.dp,
            containerColor = iconContainer,
            contentColor = iconContent,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            StatusPill(text = stateText, tone = tone)
            if (reason != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = reasonColor,
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = onAction,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
