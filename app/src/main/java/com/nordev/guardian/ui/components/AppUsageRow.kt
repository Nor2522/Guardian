package com.nordev.guardian.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.AppUsage
import com.nordev.guardian.ui.theme.GuardianSizes
import com.nordev.guardian.ui.theme.GuardianSpacing
import com.nordev.guardian.ui.theme.LocalGuardianGradients

@Composable
fun AppUsageRow(
    app: AppUsage,
    duration: String,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val border = LocalGuardianGradients.current.cardBorder
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = GuardianSpacing.xs)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(GuardianSpacing.hairline, border, shape)
            .padding(GuardianSpacing.md)
            .semantics(mergeDescendants = true) {},
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(GuardianSizes.iconBadge)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            val icon = app.icon
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(40.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.width(GuardianSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(GuardianSpacing.xxs))
            Text(
                text = duration,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.no_limit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
