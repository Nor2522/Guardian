package com.nordev.guardian.domain.block

import com.nordev.guardian.domain.model.Schedule
import com.nordev.guardian.domain.model.ScheduleMode
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolicySnapshotTest {
    private val snapshot = PolicySnapshot(mapOf("com.example.chat" to 5))

    @Test
    fun appWithoutLimitIsAlwaysAllowed() {
        assertEquals(AccessDecision.Allowed, snapshot.decide("com.example.other", 10 * 60_000L))
    }

    @Test
    fun appUnderLimitIsAllowed() {
        assertEquals(AccessDecision.Allowed, snapshot.decide("com.example.chat", 4 * 60_000L))
    }

    @Test
    fun appAtLimitIsBlocked() {
        assertEquals(
            AccessDecision.Blocked(BlockReason.DailyLimit("com.example.chat", 5)),
            snapshot.decide("com.example.chat", 5 * 60_000L),
        )
    }

    @Test
    fun appOverLimitIsBlocked() {
        assertEquals(
            AccessDecision.Blocked(BlockReason.DailyLimit("com.example.chat", 5)),
            snapshot.decide("com.example.chat", 9 * 60_000L + 59_000L),
        )
    }

    @Test
    fun bedtimeBlocksAppDuringWindow() {
        val policy = PolicySnapshot(emptyMap(), listOf(bedtime(days = setOf(DayOfWeek.MONDAY))))

        assertEquals(
            AccessDecision.Blocked(BlockReason.Schedule("Bedtime", LocalTime.of(7, 0))),
            policy.decide("com.example.chat", 0L, LocalDateTime.of(2026, 11, 16, 23, 0)),
        )
    }

    @Test
    fun bedtimeAllowsAppBeforeStartTime() {
        val policy = PolicySnapshot(emptyMap(), listOf(bedtime(days = setOf(DayOfWeek.MONDAY))))

        assertEquals(AccessDecision.Allowed, policy.decide("com.example.chat", 0L, LocalDateTime.of(2026, 11, 16, 10, 0)))
    }

    @Test
    fun crossMidnightBedtimeStaysActiveIntoNextDay() {
        val policy = PolicySnapshot(emptyMap(), listOf(bedtime(days = setOf(DayOfWeek.MONDAY))))

        assertEquals(
            AccessDecision.Blocked(BlockReason.Schedule("Bedtime", LocalTime.of(7, 0))),
            policy.decide("com.example.chat", 0L, LocalDateTime.of(2026, 11, 17, 2, 0)),
        )
    }

    @Test
    fun studyScheduleBlocksAppsOutsideAllowList() {
        val policy = PolicySnapshot(
            emptyMap(),
            listOf(
                Schedule(
                    id = "sched-study",
                    name = "Study",
                    days = setOf(DayOfWeek.FRIDAY),
                    startMinute = 16 * 60,
                    endMinute = 18 * 60,
                    mode = ScheduleMode.STUDY,
                    allowedApps = setOf("com.example.chat"),
                ),
            ),
        )

        assertEquals(
            AccessDecision.Blocked(BlockReason.Schedule("Study", LocalTime.of(18, 0))),
            policy.decide("com.example.other", 0L, LocalDateTime.of(2026, 11, 13, 17, 0)),
        )
        assertEquals(AccessDecision.Allowed, policy.decide("com.example.chat", 0L, LocalDateTime.of(2026, 11, 13, 17, 0)))
    }

    @Test
    fun overlappingSchedulesUseEarliestEndToChooseReason() {
        val policy = PolicySnapshot(
            emptyMap(),
            listOf(
                Schedule(
                    id = "sched-late",
                    name = "Late",
                    days = setOf(DayOfWeek.MONDAY),
                    startMinute = 21 * 60,
                    endMinute = 23 * 60,
                    mode = ScheduleMode.PAUSE_ALL,
                ),
                bedtime(days = setOf(DayOfWeek.MONDAY)),
            ),
        )

        assertEquals(
            AccessDecision.Blocked(BlockReason.Schedule("Late", LocalTime.of(23, 0))),
            policy.decide("com.example.chat", 0L, LocalDateTime.of(2026, 11, 16, 22, 30)),
        )
    }

    @Test
    fun alwaysAllowedPackageIsNeverBlocked() {
        val policy = PolicySnapshot(
            mapOf("com.sec.android.app.clockpackage" to 5),
            listOf(bedtime(days = setOf(DayOfWeek.MONDAY))),
        )

        assertEquals(
            AccessDecision.Allowed,
            policy.decide(
                packageName = "com.sec.android.app.clockpackage",
                usedMillisToday = 999 * 60_000L,
                now = LocalDateTime.of(2026, 11, 16, 23, 0),
                alwaysAllowed = AlwaysAllowed.packages,
            ),
        )
    }

    @Test
    fun disabledScheduleDoesNothing() {
        val policy = PolicySnapshot(
            emptyMap(),
            listOf(
                Schedule(
                    id = "sched-off",
                    name = "Bedtime",
                    days = setOf(DayOfWeek.MONDAY),
                    startMinute = 22 * 60,
                    endMinute = 7 * 60,
                    mode = ScheduleMode.PAUSE_ALL,
                    enabled = false,
                ),
            ),
        )

        assertEquals(AccessDecision.Allowed, policy.decide("com.example.chat", 0L, LocalDateTime.of(2026, 11, 16, 23, 0)))
    }

    private fun bedtime(days: Set<DayOfWeek>): Schedule = Schedule(
        id = "sched-bedtime",
        name = "Bedtime",
        days = days,
        startMinute = 22 * 60,
        endMinute = 7 * 60,
        mode = ScheduleMode.PAUSE_ALL,
    )
}
