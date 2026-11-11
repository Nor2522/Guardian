package com.nordev.guardian.data.usage

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DaySlice(val date: LocalDate, val millis: Long)

// Foreground spans are split at local midnight so each day owns its own minutes.
// The seven day list ends today and is ordered oldest first, matching the chart.
object UsageDays {
    fun lastSevenDays(today: LocalDate): List<LocalDate> =
        (6L downTo 0L).map(today::minusDays)

    fun splitAcrossDays(startMillis: Long, endMillis: Long, zone: ZoneId): List<DaySlice> {
        if (endMillis <= startMillis) return emptyList()
        val slices = mutableListOf<DaySlice>()
        var cursor = startMillis
        while (cursor < endMillis) {
            val date = Instant.ofEpochMilli(cursor).atZone(zone).toLocalDate()
            val nextMidnight = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val sliceEnd = minOf(endMillis, nextMidnight)
            // A zone rule can collapse a day; never loop on the same instant.
            if (sliceEnd <= cursor) break
            slices += DaySlice(date, sliceEnd - cursor)
            cursor = sliceEnd
        }
        return slices
    }
}
