package com.nordev.guardian.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.DayUsage
import com.nordev.guardian.ui.theme.GuardianMotion
import com.nordev.guardian.ui.theme.LocalGuardianGradients
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
    val gradients = LocalGuardianGradients.current

    GuardianCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(20.dp)
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
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            ) {
                days.forEachIndexed { index, day ->
                    val fraction = if (maxMillis > 0L) {
                        day.millis.toFloat() / maxMillis
                    } else {
                        0f
                    }
                    val animatedFraction by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(
                            durationMillis = GuardianMotion.slowMillis,
                            easing = FastOutSlowInEasing,
                        ),
                        label = "weekBar",
                    )
                    val isToday = index == days.lastIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier.height(92.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.64f)
                                    .height(92.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                                    ),
                            )
                            if (day.millis > 0L) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.64f)
                                        .height((92f * animatedFraction).coerceAtLeast(5f).dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isToday) gradients.barStrong else gradients.barMuted,
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
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                        )
                    }
                }
            }
        }
    }
}
