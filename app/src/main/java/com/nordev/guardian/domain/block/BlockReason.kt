package com.nordev.guardian.domain.block

import java.time.LocalTime

// Why a foreground app was blocked. DailyLimit comes from a parent set limit;
// Schedule comes from a bedtime or study schedule in a later step.
sealed interface BlockReason {
    data class Schedule(val name: String, val until: LocalTime) : BlockReason

    data class DailyLimit(val packageName: String, val limitMinutes: Int) : BlockReason
}
