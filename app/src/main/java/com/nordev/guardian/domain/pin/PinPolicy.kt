package com.nordev.guardian.domain.pin

// Policy constants and the pure lockout step. Keeping this free of Android
// types lets the same rules run in unit tests and in the repository.
object PinPolicy {
    const val DIGITS = 6
    const val MAX_ATTEMPTS = 5
    const val LOCKOUT_MILLIS = 60_000L
    const val SESSION_TIMEOUT_MILLIS = 5 * 60_000L
    const val SESSION_WARNING_MILLIS = 30_000L

    data class FailureState(
        val failedAttempts: Int,
        val lockedUntil: Long,
    )

    fun nextFailure(failedAttempts: Int, now: Long): FailureState {
        val attempts = failedAttempts + 1
        return if (attempts >= MAX_ATTEMPTS) {
            FailureState(failedAttempts = 0, lockedUntil = now + LOCKOUT_MILLIS)
        } else {
            FailureState(failedAttempts = attempts, lockedUntil = 0L)
        }
    }

    fun remainingLockMillis(lockedUntil: Long, now: Long): Long =
        (lockedUntil - now).coerceAtLeast(0L)

    fun isWeak(pin: String): Boolean {
        if (pin.length != DIGITS) return true
        val sameDigits = pin.all { it == pin[0] }
        val ascending = pin.zipWithNext().all { (current, next) -> next - current == 1 }
        val descending = pin.zipWithNext().all { (current, next) -> current - next == 1 }
        return sameDigits || ascending || descending
    }
}
