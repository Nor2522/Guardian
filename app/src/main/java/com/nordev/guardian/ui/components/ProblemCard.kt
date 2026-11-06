package com.nordev.guardian.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = contentColor,
        ),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (blocking) Icons.Outlined.Warning else Icons.Outlined.Info,
                    contentDescription = null,
                    tint = if (blocking) status.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (blocking) status.warning else MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
