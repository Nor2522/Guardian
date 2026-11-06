package com.nordev.guardian.ui.screens.status

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.MetricCard
import com.nordev.guardian.ui.components.StatusHero
import com.nordev.guardian.ui.components.StatusHeroState

// Protection checks and active rules are hidden in this step: nothing ships
// before Step 3, and the design hides features rather than showing them as off.
@Composable
fun StatusScreen(
    onOpenGate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(8.dp))
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
        Spacer(Modifier.height(32.dp))
        MetricCard(
            label = stringResource(R.string.screen_time_today_label),
            value = stringResource(R.string.usage_today_none),
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onOpenGate,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.status_finish_setup),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.status_footer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
    }
}
