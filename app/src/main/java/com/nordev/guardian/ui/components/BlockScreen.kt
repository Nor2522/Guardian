package com.nordev.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import com.nordev.guardian.R
import com.nordev.guardian.domain.block.AccessDecision
import com.nordev.guardian.domain.block.BlockReason
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
// Full screen block surface. The host keeps the system bars visible; this
// surface does not draw behind them. The child can always leave via Home or Back.
@Composable
fun BlockScreen(
    decision: AccessDecision,
    appName: String,
    icon: ImageVector? = null,
    onGoHome: () -> Unit,
    onWhyPaused: () -> Unit,
) {
    val reason = (decision as? AccessDecision.Blocked)?.reason

    val title = when (reason) {
        is BlockReason.DailyLimit -> stringResource(R.string.block_daily_limit_title, appName)
        is BlockReason.Schedule -> stringResource(R.string.block_schedule_title)
        null -> stringResource(R.string.block_daily_limit_title, appName)
    }
    val body = when (reason) {
        is BlockReason.DailyLimit ->
            stringResource(R.string.block_daily_limit_body, reason.limitMinutes)
        is BlockReason.Schedule ->
            stringResource(R.string.block_schedule_body, reason.until.toDisplay())
        null -> stringResource(R.string.block_daily_limit_body, 0)
    }
    val reset = when (reason) {
        is BlockReason.DailyLimit -> stringResource(R.string.block_daily_limit_reset)
        is BlockReason.Schedule -> stringResource(R.string.block_schedule_reset, reason.until.toDisplay())
        null -> stringResource(R.string.block_daily_limit_reset)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 420.dp)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BlockIcon(reason = reason, icon = icon)
            Spacer(Modifier.size(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { paneTitle = title },
            )
            Spacer(Modifier.size(16.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(24.dp))
            Text(
                text = reset,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(16.dp),
            )
            Spacer(Modifier.size(28.dp))
            Button(onClick = onGoHome, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.block_go_home))
            }
            Spacer(Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.block_calls_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    ModalBottomSheet(onDismissRequest = onWhyPaused) {
        Text(
            text = stringResource(R.string.block_why_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.block_why_body, appName),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun LocalTime.toDisplay(): String = DateTimeFormatter.ofPattern("HH:mm").format(this)

@Composable
private fun BlockIcon(
    reason: BlockReason?,
    icon: ImageVector?,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(36.dp),
            ),
    ) {
        Icon(
            imageVector = icon ?: Icons.Filled.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(40.dp),
        )
    }
}
