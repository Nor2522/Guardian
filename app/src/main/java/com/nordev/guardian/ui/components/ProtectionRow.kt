package com.nordev.guardian.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.GuardianSpacing
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
    val badge: @Composable () -> Unit = {
        IconBadge(
            icon = icon,
            size = 44.dp,
            iconSize = 21.dp,
            containerColor = iconContainer,
            contentColor = iconContent,
        )
    }
    val body: @Composable (Modifier) -> Unit = { bodyModifier ->
        Column(modifier = bodyModifier) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(GuardianSpacing.xs))
            StatusPill(text = stateText, tone = tone)
            if (reason != null) {
                Spacer(Modifier.height(GuardianSpacing.xxs))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = reasonColor,
                )
            }
        }
    }
    val action: @Composable (Modifier) -> Unit = { actionModifier ->
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                modifier = actionModifier.heightIn(min = 48.dp),
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // Below this width a side action steals most of the text column, so the
        // action moves under the copy instead of squeezing it into slivers.
        val stackAction = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.5f
        if (stackAction) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = GuardianSpacing.sm)
                    .semantics(mergeDescendants = true) {},
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    badge()
                    Spacer(Modifier.width(GuardianSpacing.md))
                    body(Modifier.weight(1f))
                }
                action(Modifier.align(Alignment.End))
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .padding(vertical = GuardianSpacing.md)
                    .semantics(mergeDescendants = true) {},
            ) {
                badge()
                Spacer(Modifier.width(GuardianSpacing.md))
                body(Modifier.weight(1f))
                action(Modifier.padding(start = GuardianSpacing.sm))
            }
        }
    }
}
