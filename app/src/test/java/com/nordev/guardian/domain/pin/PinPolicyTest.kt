package com.nordev.guardian.domain.pin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinPolicyTest {
    @Test
    fun fourthFailureDoesNotLock() {
        val next = PinPolicy.nextFailure(failedAttempts = 3, now = 1_000)
        assertEquals(4, next.failedAttempts)
        assertEquals(0L, next.lockedUntil)
    }

    @Test
    fun fifthFailureLocksForOneMinute() {
        val next = PinPolicy.nextFailure(failedAttempts = 4, now = 1_000)
        assertEquals(0, next.failedAttempts)
        assertEquals(61_000L, next.lockedUntil)
    }

    @Test
    fun remainingLockTimeNeverGoesNegative() {
        assertEquals(0L, PinPolicy.remainingLockMillis(lockedUntil = 1_000, now = 2_000))
        assertEquals(500L, PinPolicy.remainingLockMillis(lockedUntil = 1_500, now = 1_000))
    }

    @Test
    fun weakPinsAreRejected() {
        assertTrue(PinPolicy.isWeak("111111"))
        assertTrue(PinPolicy.isWeak("123456"))
        assertTrue(PinPolicy.isWeak("654321"))
        assertTrue(PinPolicy.isWeak("12345"))
    }

    @Test
    fun anUnpredictablePinPasses() {
        assertFalse(PinPolicy.isWeak("258036"))
    }
}
