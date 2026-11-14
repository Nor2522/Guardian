package com.nordev.guardian.domain.model

import java.time.DayOfWeek

// One parent-created pause window. Bedtime and study hours share this shape.
// startMinute and endMinute are minutes after local midnight; endMinute below
// startMinute means the window crosses midnight and ends the next day.
data class Schedule(
    val id: String,
    val name: String,
    val days: Set<DayOfWeek>,
    val startMinute: Int,
    val endMinute: Int,
    val mode: ScheduleMode,
    val allowedApps: Set<String> = emptySet(),
    val enabled: Boolean = true,
) {
    init {
        require(name.isNotBlank()) { "Schedule name must not be blank." }
        require(name.length <= 40) { "Schedule name must be at most 40 characters." }
        require(days.isNotEmpty()) { "A schedule needs at least one day." }
        require(startMinute in 0..1439) { "Start minute must be within one day." }
        require(endMinute in 0..1439) { "End minute must be within one day." }
        require(endMinute != startMinute) { "Start and end times must be different." }
        require(mode == ScheduleMode.PAUSE_ALL || allowedApps.isNotEmpty()) { "Study schedules need at least one allowed app." }
    }
}

enum class ScheduleMode {
    // Pauses every app during the window.
    PAUSE_ALL,

    // Only allows the listed apps during the window.
    STUDY,
}
