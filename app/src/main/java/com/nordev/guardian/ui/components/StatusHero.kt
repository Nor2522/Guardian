package com.nordev.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.LocalGuardianStatusColors

// The hero states the status screen models: fully on, one or more broken checks,
// and setup still running. The icon pair is chosen so color never carries the state alone.
enum class StatusHeroState { Healthy, Attention, Setup }

@Composable
fun StatusHero(
    state: StatusHeroState,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val status = LocalGuardianStatusColors.current
    val (container, onContainer, icon) = when (state) {
        StatusHeroState.Healthy -> Triple(
            status.successContainer,
            status.onSuccessContainer,
            Icons.Outlined.GppGood as ImageVector,
        )
        StatusHeroState.Attention -> Triple(
            status.warningContainer,
            status.onWarningContainer,
            Icons.Outlined.Warning as ImageVector,
        )
        StatusHeroState.Setup -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Outlined.HealthAndSafety as ImageVector,
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .background(color = container, shape = CircleShape),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onContainer,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
