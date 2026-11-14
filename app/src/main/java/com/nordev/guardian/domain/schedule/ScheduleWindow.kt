package com.nordev.guardian.domain.schedule

import com.nordev.guardian.domain.model.Schedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Window math for one schedule. The window is anchored on its start day, so a
// cross-midnight window (endMinute below startMinute) ends the next day and stays
// active through the early morning hours.
object ScheduleWindow {
    fun startTime(schedule: Schedule): LocalTime = LocalTime.of(schedule.startMinute / 60, schedule.startMinute % 60)

    fun endTime(schedule: Schedule): LocalTime = LocalTime.of(schedule.endMinute / 60, schedule.endMinute % 60)

    fun endOf(schedule: Schedule, start: LocalDateTime): LocalDateTime =
        if (schedule.endMinute > schedule.startMinute) {
            start.toLocalDate().atTime(endTime(schedule))
        } else {
            start.plusDays(1).toLocalDate().atTime(endTime(schedule))
        }

    // Exclusive end of the window that covers `now`, or null when no day of the
    // schedule is active at that moment. A window can start on the previous day
    // when it crosses midnight, so the previous day is checked as well.
    fun activeEnd(schedule: Schedule, now: LocalDateTime): LocalDateTime? {
        var day = now.toLocalDate()
        repeat(2) {
            val start = day.atTime(startTime(schedule))
            val end = endOf(schedule, start)
            if (schedule.days.contains(day.dayOfWeek) && !now.isBefore(start) && now.isBefore(end)) {
                return end
            }
            day = day.minusDays(1)
        }
        return null
    }

    fun isActive(schedule: Schedule, now: LocalDateTime): Boolean = activeEnd(schedule, now) != null

    // Time of day the window lifts, for the block screen copy.
    fun untilTime(schedule: Schedule): LocalTime = endTime(schedule)
}
