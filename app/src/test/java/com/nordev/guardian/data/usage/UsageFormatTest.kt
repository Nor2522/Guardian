package com.nordev.guardian.data.usage

import org.junit.Assert.assertEquals
import org.junit.Test

class UsageFormatTest {
    @Test
    fun noUsageIsNone() {
        assertEquals(DurationDisplay.None, durationDisplay(0L))
    }

    @Test
    fun underOneMinuteIsLessThanMinute() {
        assertEquals(DurationDisplay.LessThanMinute, durationDisplay(59_999L))
    }

    @Test
    fun exactMinuteRoundsToMinutes() {
        assertEquals(DurationDisplay.Minutes(1), durationDisplay(60_000L))
        assertEquals(DurationDisplay.Minutes(45), durationDisplay(45 * 60_000L))
    }

    @Test
    fun exactHourHasNoMinutes() {
        assertEquals(DurationDisplay.HoursMinutes(1, 0), durationDisplay(60 * 60_000L))
    }

    @Test
    fun hourAndMinutesKeepBoth() {
        assertEquals(
            DurationDisplay.HoursMinutes(1, 20),
            durationDisplay(80 * 60_000L),
        )
    }

    @Test
    fun nearHourRoundsUp() {
        assertEquals(
            DurationDisplay.HoursMinutes(1, 0),
            durationDisplay(59 * 60_000L + 40_000L),
        )
    }
}
