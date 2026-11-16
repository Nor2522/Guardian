package com.nordev.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.LocalGuardianStatusColors

// Blocking problems use warningContainer, never error: error is reserved for
// destructive actions and failures. Informational uses a quiet surface and an info icon.
enum class ProblemSeverity { Blocking, Informational }

@Composable
fun ProblemCard(
    severity: ProblemSeverity,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val blocking = severity == ProblemSeverity.Blocking
    val status = LocalGuardianStatusColors.current
    val container = if (blocking) status.warningContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = if (blocking) status.onWarningContainer else MaterialTheme.colorScheme.onSurface
    val accent = if (blocking) status.warning else MaterialTheme.colorScheme.primary
    val muted = if (blocking) {
        status.onWarningContainer.copy(alpha = 0.80f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    GuardianCard(
        modifier = modifier,
        containerColor = container,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(accent),
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(
                        icon = if (blocking) Icons.Outlined.Warning else Icons.Outlined.Info,
                        size = 40.dp,
                        iconSize = 20.dp,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
                        contentColor = accent,
                        halo = false,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted,
                )
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = onAction,
                        colors = ButtonDefaults.textButtonColors(contentColor = accent),
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.width(16.dp),
                        )
                    }
                }
            }
        }
    }
}
