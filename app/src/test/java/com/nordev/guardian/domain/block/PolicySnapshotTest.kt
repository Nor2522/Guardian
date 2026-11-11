package com.nordev.guardian.domain.block

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
}
