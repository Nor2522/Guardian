package com.nordev.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.DayUsage
import java.time.format.DateTimeFormatter

// One bar per day, oldest first and today last. The chart is one accessibility
// node that reads the weekly total and every day's value; bars stay unfocusable.
@Composable
fun WeekChart(
    days: List<DayUsage>,
    weekMillis: Long,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    val shortDay = remember(locale) { DateTimeFormatter.ofPattern("EEE", locale) }
    val fullDay = remember(locale) { DateTimeFormatter.ofPattern("EEEE", locale) }
    val none = stringResource(R.string.duration_none)
    val maxMillis = days.maxOfOrNull { it.millis } ?: 0L
    val dayParts = days.map { day ->
        "${fullDay.format(day.date)} ${durationText(day.millis, none)}"
    }
    val description = stringResource(
        R.string.week_chart_cd,
        durationText(weekMillis, none),
        dayParts.joinToString(", "),
    )

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clearAndSetSemantics { contentDescription = description },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.week_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = durationText(weekMillis, none),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
            ) {
                days.forEachIndexed { index, day ->
                    val fraction = if (maxMillis > 0L) {
                        day.millis.toFloat() / maxMillis
                    } else {
                        0f
                    }
                    val isToday = index == days.lastIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier.height(88.dp),
                        ) {
                            if (day.millis > 0L) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.62f)
                                        .height((88f * fraction).coerceAtLeast(4f).dp)
                                        .background(
                                            color = if (isToday) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.primaryContainer
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                        ),
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = shortDay.format(day.date).take(2),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isToday) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
