package com.nordev.guardian.ui.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nordev.guardian.domain.pin.PinPolicy

// Memory-only parent session. It starts on a successful unlock and ends on
// five idle minutes, on ON_STOP, on a locked route, or on process death.
class ParentSession(
    private val timeoutMillis: Long = PinPolicy.SESSION_TIMEOUT_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    var isUnlocked by mutableStateOf(false)
        private set

    private var lastActiveAt by mutableLongStateOf(0L)

    fun unlock() {
        isUnlocked = true
        lastActiveAt = clock()
    }

    fun touch() {
        if (isUnlocked) lastActiveAt = clock()
    }

    fun lock() {
        isUnlocked = false
    }

    fun remainingMillis(): Long =
        if (!isUnlocked) 0L else (timeoutMillis - (clock() - lastActiveAt)).coerceAtLeast(0L)

    fun warningDue(): Boolean =
        isUnlocked && remainingMillis() in 1..PinPolicy.SESSION_WARNING_MILLIS

    fun expired(): Boolean =
        isUnlocked && clock() - lastActiveAt >= timeoutMillis
}
