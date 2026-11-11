package com.nordev.guardian.data.usage

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class UsageDaysTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun sevenDaysEndTodayInOrder() {
        val today = LocalDate.of(2026, 11, 11)
        val days = UsageDays.lastSevenDays(today)

        assertEquals(7, days.size)
        assertEquals(LocalDate.of(2026, 11, 5), days.first())
        assertEquals(today, days.last())
        assertEquals(days.sorted(), days)
    }

    @Test
    fun splitAcrossMidnightGivesEachDayItsOwnMinutes() {
        val start = ZonedDateTime.of(2026, 11, 10, 23, 30, 0, 0, zone)
            .toInstant()
            .toEpochMilli()
        val end = ZonedDateTime.of(2026, 11, 11, 0, 30, 0, 0, zone)
            .toInstant()
            .toEpochMilli()

        val slices = UsageDays.splitAcrossDays(start, end, zone)

        assertEquals(2, slices.size)
        assertEquals(LocalDate.of(2026, 11, 10), slices[0].date)
        assertEquals(30 * 60_000L, slices[0].millis)
        assertEquals(LocalDate.of(2026, 11, 11), slices[1].date)
        assertEquals(30 * 60_000L, slices[1].millis)
    }

    @Test
    fun emptyOrReversedIntervalHasNoSlices() {
        assertEquals(0, UsageDays.splitAcrossDays(100L, 100L, zone).size)
        assertEquals(0, UsageDays.splitAcrossDays(200L, 100L, zone).size)
    }
}
