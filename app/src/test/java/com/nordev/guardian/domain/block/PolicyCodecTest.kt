package com.nordev.guardian.domain.block

import com.nordev.guardian.domain.model.Schedule
import com.nordev.guardian.domain.model.ScheduleMode
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class PolicyCodecTest {
    @Test
    fun limitsRoundTrip() {
        val limits = mapOf("com.example.chat" to 30, "com.example.game" to 120)

        assertEquals(limits, PolicyCodec.decodeLimits(PolicyCodec.encodeLimits(limits)))
    }

    @Test
    fun limitsSurviveSeparatorsInThePackageName() {
        val limits = mapOf("com.example\todd\\name" to 15)

        assertEquals(limits, PolicyCodec.decodeLimits(PolicyCodec.encodeLimits(limits)))
    }

    @Test
    fun corruptLimitLinesAreDropped() {
        val text = """
            com.example.keep	30
            com.example.zero	0
            com.example.huge	5000
            com.example.text	soon
            only-one-field
            	
        """.trimIndent()

        assertEquals(mapOf("com.example.keep" to 30), PolicyCodec.decodeLimits(text))
    }

    @Test
    fun schedulesRoundTrip() {
        val schedules = listOf(
            Schedule(
                id = "sched-bedtime",
                name = "Bedtime",
                days = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                startMinute = 1320,
                endMinute = 420,
                mode = ScheduleMode.PAUSE_ALL,
            ),
            Schedule(
                id = "sched-study",
                name = "Study",
                days = setOf(DayOfWeek.MONDAY),
                startMinute = 960,
                endMinute = 1080,
                mode = ScheduleMode.STUDY,
                allowedApps = setOf("com.example.dictionary", "com.example.notes"),
                enabled = false,
            ),
        )

        assertEquals(schedules, PolicyCodec.decodeSchedules(PolicyCodec.encodeSchedules(schedules)))
    }

    @Test
    fun scheduleNameSurvivesTabsNewlinesAndBackslashes() {
        val schedule = Schedule(
            id = "sched-odd",
            name = "Tab\there\nLine\\two",
            days = setOf(DayOfWeek.FRIDAY),
            startMinute = 60,
            endMinute = 120,
            mode = ScheduleMode.PAUSE_ALL,
        )

        val decoded = PolicyCodec.decodeSchedules(PolicyCodec.encodeSchedules(listOf(schedule)))

        assertEquals(listOf(schedule), decoded)
        assertEquals("Tab\there\nLine\\two", decoded.single().name)
    }

    @Test
    fun studyScheduleWithoutAllowedAppsIsDropped() {
        val valid = Schedule(
            id = "sched-keep",
            name = "Study",
            days = setOf(DayOfWeek.MONDAY),
            startMinute = 600,
            endMinute = 660,
            mode = ScheduleMode.STUDY,
            allowedApps = setOf("com.example.notes"),
        )
        val text = PolicyCodec.encodeSchedules(listOf(valid)) + "\n" +
            "sched-empty\tStudy\tMONDAY\t600\t660\tSTUDY\t1\t"

        assertEquals(listOf(valid), PolicyCodec.decodeSchedules(text))
    }

    @Test
    fun duplicateIdsKeepTheLastRecord() {
        val text = listOf(
            schedule(id = "sched-1", name = "Old"),
            schedule(id = "sched-1", name = "New"),
        ).joinToString("\n") { PolicyCodec.encodeSchedules(listOf(it)) }

        val decoded = PolicyCodec.decodeSchedules(text)

        assertEquals(listOf("New"), decoded.map { it.name })
    }

    private fun schedule(id: String, name: String) = Schedule(
        id = id,
        name = name,
        days = setOf(DayOfWeek.MONDAY),
        startMinute = 600,
        endMinute = 660,
        mode = ScheduleMode.PAUSE_ALL,
    )
}
