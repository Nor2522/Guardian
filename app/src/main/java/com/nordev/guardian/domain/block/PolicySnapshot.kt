package com.nordev.guardian.domain.block

import com.nordev.guardian.domain.model.Schedule
import com.nordev.guardian.domain.model.ScheduleMode
import com.nordev.guardian.domain.schedule.ScheduleWindow
import java.time.LocalDateTime

// The parent's rules travel together: daily limits and schedule windows are one
// snapshot for the policy engine and one unit for storage.
data class PolicySnapshot(
    val limits: Map<String, Int> = emptyMap(),
    val schedules: List<Schedule> = emptyList(),
) {
    fun decide(
        packageName: String,
        usedMillisToday: Long,
        now: LocalDateTime = LocalDateTime.now(),
        alwaysAllowed: Set<String> = emptySet(),
    ): AccessDecision {
        if (packageName in alwaysAllowed) return AccessDecision.Allowed
        val blocking = schedules.asSequence()
            .filter { it.enabled }
            .mapNotNull { schedule -> ScheduleWindow.activeEnd(schedule, now)?.let { end -> schedule to end } }
            .filter { (schedule, _) -> blocks(schedule, packageName) }
            .minByOrNull { it.second }
        if (blocking != null) {
            val schedule = blocking.first
            return AccessDecision.Blocked(BlockReason.Schedule(schedule.name, ScheduleWindow.untilTime(schedule)))
        }
        val limitMinutes = limits[packageName] ?: return AccessDecision.Allowed
        val limitMillis = limitMinutes * 60_000L
        if (usedMillisToday < limitMillis) return AccessDecision.Allowed
        return AccessDecision.Blocked(BlockReason.DailyLimit(packageName, limitMinutes))
    }

    private fun blocks(schedule: Schedule, packageName: String): Boolean =
        schedule.mode == ScheduleMode.PAUSE_ALL || schedule.allowedApps.none { it == packageName }
}
