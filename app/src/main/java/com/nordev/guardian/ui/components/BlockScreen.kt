package com.nordev.guardian.ui.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.nordev.guardian.R
import com.nordev.guardian.domain.block.AccessDecision
import com.nordev.guardian.domain.block.BlockReason
import com.nordev.guardian.ui.theme.GuardianSizes
import com.nordev.guardian.ui.theme.GuardianSpacing
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
    var showWhy by remember { mutableStateOf(false) }

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

    GuardianBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .heightIn(min = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = GuardianSpacing.xl,
                        end = GuardianSpacing.xl,
                        top = GuardianSpacing.lg,
                        bottom = GuardianSpacing.lg,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BlockIcon(reason = reason, icon = icon)
                Spacer(Modifier.size(GuardianSpacing.xl))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { paneTitle = title },
                )
                Spacer(Modifier.size(GuardianSpacing.md))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(GuardianSpacing.xl))
                GuardianCard {
                    Text(
                        text = reset,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(GuardianSpacing.lg),
                    )
                }
                Spacer(Modifier.size(GuardianSpacing.xl))
                GuardianPrimaryButton(
                    label = stringResource(R.string.block_go_home),
                    onClick = onGoHome,
                )
                TextButton(onClick = onWhyPaused) {
                    Text(
                        text = stringResource(R.string.block_why_title),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(Modifier.size(GuardianSpacing.sm))
                Text(
                    text = stringResource(R.string.block_calls_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    if (showWhy) {
        ModalBottomSheet(onDismissRequest = { showWhy = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = GuardianSpacing.xl,
                        end = GuardianSpacing.xl,
                        bottom = GuardianSpacing.xl,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.block_why_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.size(GuardianSpacing.sm))
                Text(
                    text = stringResource(R.string.block_why_body, appName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(GuardianSpacing.md))
                TextButton(
                    onClick = { showWhy = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = GuardianSizes.minTouch),
                ) {
                    Text(
                        text = stringResource(R.string.dialog_close),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

private fun LocalTime.toDisplay(): String = DateTimeFormatter.ofPattern("HH:mm").format(this)

@Composable
private fun BlockIcon(
    reason: BlockReason?,
    icon: ImageVector?,
) {
    val schedule = reason is BlockReason.Schedule
    IconBadge(
        icon = icon ?: Icons.Filled.Shield,
        size = 88.dp,
        iconSize = 44.dp,
        containerColor = if (schedule) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (schedule) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
    )
}
