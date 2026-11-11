package com.nordev.guardian.data.usage

// Kept as data rather than a formatted string so the text layer can use
// localized resources and the math stays unit-testable.
sealed interface DurationDisplay {
    data object None : DurationDisplay
    data object LessThanMinute : DurationDisplay
    data class Minutes(val minutes: Int) : DurationDisplay
    data class HoursMinutes(val hours: Int, val minutes: Int) : DurationDisplay
}

fun durationDisplay(millis: Long): DurationDisplay {
    if (millis <= 0L) return DurationDisplay.None
    if (millis < 60_000L) return DurationDisplay.LessThanMinute
    val totalMinutes = ((millis + 30_000L) / 60_000L).toInt().coerceAtLeast(1)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours == 0) {
        DurationDisplay.Minutes(minutes)
    } else {
        DurationDisplay.HoursMinutes(hours, minutes)
    }
}
