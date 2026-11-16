package com.nordev.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.GuardianSpacing
import com.nordev.guardian.ui.theme.LocalGuardianGradients

// Value uses displaySmall, which carries tabular figures so durations stay readable.
// Label and chip wrap independently, so the layout stacks instead of clipping at 200% font.
@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    chipLabel: String? = null,
) {
    val gradients = LocalGuardianGradients.current
    GuardianCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(gradients.ambientTop, Color.Transparent),
                            center = Offset(size.width * 1.02f, -size.height * 0.08f),
                            radius = size.width * 0.85f,
                        ),
                    )
                }
                .padding(20.dp),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(gradients.accentLine),
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (chipLabel != null) {
                        Spacer(Modifier.width(GuardianSpacing.sm))
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Text(
                                text = chipLabel,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
