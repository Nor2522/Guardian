package com.nordev.guardian.ui.nav

import com.nordev.guardian.domain.pin.PinPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentSessionTest {
    private var now = 0L
    private val session = ParentSession(clock = { now })

    @Test
    fun startsLocked() {
        assertFalse(session.isUnlocked)
    }

    @Test
    fun unlockStartsTheClock() {
        session.unlock()
        assertTrue(session.isUnlocked)
    }

    @Test
    fun expiresAfterFiveIdleMinutes() {
        session.unlock()
        now = PinPolicy.SESSION_TIMEOUT_MILLIS - 1
        assertFalse(session.expired())
        now = PinPolicy.SESSION_TIMEOUT_MILLIS
        assertTrue(session.expired())
    }

    @Test
    fun touchPostponesExpiry() {
        session.unlock()
        now = PinPolicy.SESSION_TIMEOUT_MILLIS - 1
        session.touch()
        now = PinPolicy.SESSION_TIMEOUT_MILLIS + 1
        assertFalse(session.expired())
    }

    @Test
    fun lockEndsTheSession() {
        session.unlock()
        session.lock()
        assertFalse(session.isUnlocked)
        assertFalse(session.expired())
    }
}
