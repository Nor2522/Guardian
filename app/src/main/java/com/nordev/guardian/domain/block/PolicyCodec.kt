package com.nordev.guardian.domain.block

import com.nordev.guardian.domain.model.Schedule
import com.nordev.guardian.domain.model.ScheduleMode
import java.time.DayOfWeek

// One record per line, tab separated. Tab, newline, carriage return, and
// backslash are escaped so a parent-typed name can hold any character. Decoding
// never throws: a line that does not validate is dropped, so a damaged store
// degrades to fewer rules instead of a crash at boot.
object PolicyCodec {
    private const val MINUTES_PER_DAY = 1440

    fun encodeLimits(limits: Map<String, Int>): String =
        limits.entries.joinToString("\n") { (packageName, minutes) ->
            listOf(packageName, minutes.toString()).joinToString("\t", transform = ::escape)
        }

    fun decodeLimits(text: String): Map<String, Int> =
        text.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val fields = splitFields(line)
                if (fields.size != 2) return@mapNotNull null
                val packageName = fields[0]
                val minutes = fields[1].toIntOrNull() ?: return@mapNotNull null
                if (packageName.isBlank() || minutes !in 1..MINUTES_PER_DAY) return@mapNotNull null
                packageName to minutes
            }
            .toMap()

    fun encodeSchedules(schedules: List<Schedule>): String =
        schedules.joinToString("\n") { schedule ->
            listOf(
                schedule.id,
                schedule.name,
                schedule.days.joinToString(",") { it.name },
                schedule.startMinute.toString(),
                schedule.endMinute.toString(),
                schedule.mode.name,
                if (schedule.enabled) "1" else "0",
                schedule.allowedApps.joinToString(","),
            ).joinToString("\t", transform = ::escape)
        }

    fun decodeSchedules(text: String): List<Schedule> {
        val byId = LinkedHashMap<String, Schedule>()
        text.lineSequence()
            .filter { it.isNotBlank() }
            .forEach { line ->
                val schedule = decodeSchedule(line) ?: return@forEach
                byId.remove(schedule.id)
                byId[schedule.id] = schedule
            }
        return byId.values.toList()
    }

    private fun decodeSchedule(line: String): Schedule? {
        val fields = splitFields(line)
        if (fields.size != 8) return null
        val mode = ScheduleMode.entries.firstOrNull { it.name == fields[5] } ?: return null
        val days = fields[2].split(",")
            .mapNotNull { name -> DayOfWeek.entries.firstOrNull { it.name == name } }
            .toSet()
        val allowedApps = fields[7].split(",").filter { it.isNotBlank() }.toSet()
        return runCatching {
            Schedule(
                id = fields[0],
                name = fields[1],
                days = days,
                startMinute = fields[3].toInt(),
                endMinute = fields[4].toInt(),
                mode = mode,
                allowedApps = allowedApps,
                enabled = fields[6] == "1",
            )
        }.getOrNull()
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '\t' -> append("\\t")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(char)
            }
        }
    }

    private fun splitFields(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var index = 0
        while (index < line.length) {
            val char = line[index]
            if (char == '\\' && index + 1 < line.length) {
                when (val escaped = line[index + 1]) {
                    't' -> current.append('\t')
                    'n' -> current.append('\n')
                    'r' -> current.append('\r')
                    '\\' -> current.append('\\')
                    else -> {
                        current.append('\\')
                        current.append(escaped)
                    }
                }
                index += 2
                continue
            }
            if (char == '\t') {
                fields += current.toString()
                current.clear()
            } else {
                current.append(char)
            }
            index += 1
        }
        fields += current.toString()
        return fields
    }
}
