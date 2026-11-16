package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.UsageSnapshot
import com.nordev.guardian.ui.components.AppUsageRow
import com.nordev.guardian.ui.components.EmptyState
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.MetricCard
import com.nordev.guardian.ui.components.ProblemCard
import com.nordev.guardian.ui.components.ProblemSeverity
import com.nordev.guardian.ui.components.SectionHeader
import com.nordev.guardian.ui.components.WeekChart
import com.nordev.guardian.ui.components.durationText
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Missing Usage access replaces the totals and chart. The app section stays so
// the screen keeps its shape, but it shows the same honest empty line instead
// of zeros that look like real usage.
@Composable
fun TodayScreen(
    usage: UsageSnapshot?,
    onOpenSettings: () -> Unit,
    onOpenUsageSettings: () -> Unit,
) {
    val locale = LocalLocale.current.platformLocale
    val dateFormat = remember(locale) { DateTimeFormatter.ofPattern("EEEE, d MMMM", locale) }
    val dateLine = dateFormat.format(LocalDate.now())
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GuardianTopBar(
                title = stringResource(R.string.tab_today),
                onSettings = onOpenSettings,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = dateLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            when {
                usage == null -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                !usage.accessGranted -> {
                    ProblemCard(
                        severity = ProblemSeverity.Blocking,
                        title = stringResource(R.string.today_problem_title),
                        body = stringResource(R.string.today_problem_body),
                        actionLabel = stringResource(R.string.action_open_settings),
                        onAction = onOpenUsageSettings,
                    )
                    Spacer(Modifier.height(32.dp))
                    SectionHeader(title = stringResource(R.string.section_apps_today))
                    EmptyState(
                        icon = Icons.Outlined.Shield,
                        title = stringResource(R.string.empty_today),
                    )
                }
                else -> {
                    MetricCard(
                        label = stringResource(R.string.screen_time_today_label),
                        value = durationText(
                            millis = usage.todayMillis,
                            noneLabel = stringResource(R.string.usage_today_none),
                        ),
                        chipLabel = if (usage.estimated) {
                            stringResource(R.string.estimated)
                        } else {
                            null
                        },
                    )
                    Spacer(Modifier.height(24.dp))
                    WeekChart(days = usage.days, weekMillis = usage.weekMillis)
                    Spacer(Modifier.height(24.dp))
                    SectionHeader(
                        title = stringResource(R.string.section_apps_today),
                        count = pluralStringResource(
                            R.plurals.apps_count,
                            usage.apps.size,
                            usage.apps.size,
                        ),
                    )
                    if (usage.apps.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.Shield,
                            title = stringResource(R.string.empty_today),
                        )
                    } else {
                        val none = stringResource(R.string.duration_none)
                        usage.apps.forEach { app ->
                            AppUsageRow(
                                app = app,
                                duration = durationText(app.millis, none),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
