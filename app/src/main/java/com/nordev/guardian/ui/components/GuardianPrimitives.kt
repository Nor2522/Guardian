package com.nordev.guardian.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.GuardianMotion
import com.nordev.guardian.ui.theme.GuardianSizes
import com.nordev.guardian.ui.theme.LocalGuardianGradients
import com.nordev.guardian.ui.theme.LocalGuardianStatusColors

// Ambient wash behind every screen. It is one static draw pass, so it costs less
// than an image and keeps the near-black and green-neutral surfaces from reading
// as flat. Content stays on the plain background for contrast.
@Composable
fun GuardianBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val gradients = LocalGuardianGradients.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(gradients.ambientTop, Color.Transparent),
                        center = Offset(size.width * 0.92f, -size.height * 0.04f),
                        radius = size.width.coerceAtLeast(size.height * 0.55f) * 0.95f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(gradients.ambientBottom, Color.Transparent),
                        center = Offset(size.width * 0.04f, size.height * 1.02f),
                        radius = size.width * 0.9f,
                    ),
                )
            },
    ) {
        // Text outside a card has no Surface to inherit from; without this it
        // falls back to black and disappears on the dark scheme.
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
        ) {
            content()
        }
    }
}

// The standard card: a 16 dp tonal surface with a hairline that makes the edge
// readable on both themes. Status cards pass their own container and content.
@Composable
fun GuardianCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    val border = LocalGuardianGradients.current.cardBorder
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, border),
    ) {
        content()
    }
}

// Icon tile with a soft halo ring. The shape is a squircle rather than a plain
// circle, which gives lists and heroes a single recognisable rhythm.
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = GuardianSizes.iconBadge,
    iconSize: Dp = size * 0.5f,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    halo: Boolean = true,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .drawBehind {
                val radius = this.size.minDimension * 0.34f
                if (halo) {
                    // Soft falloff instead of a flat disc, so the glow never
                    // reads as a hard-edged smudge behind the tile.
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                containerColor.copy(alpha = 0.24f),
                                containerColor.copy(alpha = 0.08f),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = this.size.minDimension * 0.72f,
                        ),
                        radius = this.size.minDimension * 0.72f,
                    )
                }
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(containerColor, lerp(containerColor, contentColor, 0.07f)),
                    ),
                    cornerRadius = CornerRadius(radius, radius),
                )
                drawRoundRect(
                    color = contentColor.copy(alpha = 0.14f),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

enum class GuardianTone { Neutral, Primary, Success, Attention }

@Composable
fun StatusPill(
    text: String,
    tone: GuardianTone,
    modifier: Modifier = Modifier,
) {
    val status = LocalGuardianStatusColors.current
    val container: Color
    val content: Color
    val dot: Color
    when (tone) {
        GuardianTone.Neutral -> {
            container = MaterialTheme.colorScheme.surfaceContainerHigh
            content = MaterialTheme.colorScheme.onSurfaceVariant
            dot = MaterialTheme.colorScheme.outline
        }
        GuardianTone.Primary -> {
            container = MaterialTheme.colorScheme.primaryContainer
            content = MaterialTheme.colorScheme.onPrimaryContainer
            dot = MaterialTheme.colorScheme.primary
        }
        GuardianTone.Success -> {
            container = status.successContainer
            content = status.onSuccessContainer
            dot = status.success
        }
        GuardianTone.Attention -> {
            container = status.warningContainer
            content = status.onWarningContainer
            dot = status.warning
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dot),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content,
        )
    }
}

// One primary action with the brand gradient. The transparent Button surface
// keeps the ripple, focus ring, and enabled semantics from Material while the
// gradient is drawn underneath, so nothing about the control is reimplemented.
@Composable
fun GuardianPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    destructive: Boolean = false,
    loading: Boolean = false,
) {
    val gradients = LocalGuardianGradients.current
    val shape = MaterialTheme.shapes.extraLarge
    val background = when {
        destructive -> SolidColor(MaterialTheme.colorScheme.error)
        enabled -> gradients.primaryButton
        else -> SolidColor(MaterialTheme.colorScheme.surfaceContainerHigh)
    }
    val contentColor = when {
        destructive -> MaterialTheme.colorScheme.onError
        enabled -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(background),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = contentColor,
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
                disabledElevation = 0.dp,
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = GuardianSizes.primaryButtonHeight),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// Rounded progress rail shared by limits and the week chart. The stop indicator
// is removed so the bar reads as a clean capsule at every value.
@Composable
fun GuardianProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    height: Dp = 6.dp,
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape),
        color = color,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}

// Onboarding progress as five capsules. The row carries the progress semantics
// so TalkBack reads the step once instead of five unnamed boxes.
@Composable
fun StepProgress(
    step: Int,
    modifier: Modifier = Modifier,
    total: Int = 5,
) {
    val safeStep = step.coerceIn(0, total)
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceContainerHigh
    Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = safeStep.toFloat(),
                range = 0f..total.toFloat(),
            )
        },
    ) {
        repeat(total) { index ->
            val active = index < safeStep
            val color by animateColorAsState(
                targetValue = if (active) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = GuardianMotion.standardMillis),
                label = "stepProgress",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
