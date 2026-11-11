package com.nordev.guardian.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.DurationDisplay
import com.nordev.guardian.data.usage.durationDisplay

// Localized duration text. Callers pass the "no usage" wording because the hero
// says "None today" while a chart label says "None".
@Composable
fun durationText(millis: Long, noneLabel: String): String = when (val display = durationDisplay(millis)) {
    DurationDisplay.None -> noneLabel
    DurationDisplay.LessThanMinute -> stringResource(R.string.duration_less_than_minute)
    is DurationDisplay.Minutes -> pluralStringResource(
        R.plurals.duration_minutes,
        display.minutes,
        display.minutes,
    )
    is DurationDisplay.HoursMinutes -> if (display.minutes == 0) {
        stringResource(R.string.duration_hours, display.hours)
    } else {
        stringResource(R.string.duration_hours_minutes, display.hours, display.minutes)
    }
}
