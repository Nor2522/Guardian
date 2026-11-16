package com.nordev.guardian.ui.screens.status

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.UsageSnapshot
import com.nordev.guardian.ui.components.GuardianPrimaryButton
import com.nordev.guardian.ui.components.IconBadge
import com.nordev.guardian.ui.components.MetricCard
import com.nordev.guardian.ui.components.StatusHero
import com.nordev.guardian.ui.components.StatusHeroState
import com.nordev.guardian.ui.components.durationText
import com.nordev.guardian.ui.theme.GuardianSpacing

// Protection checks and active rules are hidden in this step: nothing ships
// before Step 3, and the design hides features rather than showing them as off.
@Composable
fun StatusScreen(
    onOpenGate: () -> Unit,
    usage: UsageSnapshot?,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight()
                .widthIn(max = GuardianSpacing.maxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = GuardianSpacing.gutter),
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Outlined.Shield,
                    size = 40.dp,
                    iconSize = 22.dp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    halo = false,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.height(24.dp))
            StatusHero(
                state = StatusHeroState.Setup,
                title = stringResource(R.string.hero_setup_title),
                body = stringResource(R.string.hero_setup_body),
            )
            Spacer(Modifier.height(24.dp))
            MetricCard(
                label = stringResource(R.string.screen_time_today_label),
                value = when {
                    usage == null -> stringResource(R.string.usage_loading)
                    !usage.accessGranted -> stringResource(R.string.usage_unavailable)
                    else -> durationText(
                        millis = usage.todayMillis,
                        noneLabel = stringResource(R.string.usage_today_none),
                    )
                },
                chipLabel = if (usage?.estimated == true) {
                    stringResource(R.string.estimated)
                } else {
                    null
                },
            )
            Spacer(Modifier.height(24.dp))
            GuardianPrimaryButton(
                label = stringResource(R.string.status_finish_setup),
                onClick = onOpenGate,
                icon = Icons.Outlined.Lock,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.status_footer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
