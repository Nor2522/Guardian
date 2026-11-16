package com.nordev.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.GuardianSizes
import com.nordev.guardian.ui.theme.LocalGuardianGradients
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
    val gradients = LocalGuardianGradients.current
    val (gradient, onContainer, icon) = when (state) {
        StatusHeroState.Healthy -> Triple(
            gradients.heroHealthy,
            status.onSuccessContainer,
            Icons.Outlined.GppGood,
        )
        StatusHeroState.Attention -> Triple(
            gradients.heroAttention,
            status.onWarningContainer,
            Icons.Outlined.Warning,
        )
        StatusHeroState.Setup -> Triple(
            gradients.heroSetup,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Outlined.HealthAndSafety,
        )
    }
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(gradient)
            .drawBehind {
                val ink = onContainer.copy(alpha = 0.07f)
                drawCircle(
                    color = ink,
                    radius = size.width * 0.52f,
                    center = Offset(size.width * 0.96f, -size.height * 0.10f),
                    style = Stroke(width = 24.dp.toPx()),
                )
                drawCircle(
                    color = ink.copy(alpha = 0.45f),
                    radius = size.width * 0.66f,
                    center = Offset(size.width * 0.96f, -size.height * 0.10f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconBadge(
                icon = icon,
                size = GuardianSizes.iconBadgeLarge,
                iconSize = 30.dp,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                contentColor = onContainer,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = onContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = onContainer.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
