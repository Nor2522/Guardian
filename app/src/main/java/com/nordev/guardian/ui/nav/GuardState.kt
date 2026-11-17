package com.nordev.guardian.ui.nav

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// What kind of system screen the guard is covering. The guard matches the
// window package and class only; it never reads the text on the screen.
enum class GuardKind {
    WebFilterSettings,
    AdminProtection,
}

// Memory-only, same process as the accessibility service. A correct PIN grants
// a short grace so the parent can finish changing the screen they opened.
object GuardState {
    var kind: GuardKind? by mutableStateOf<GuardKind?>(null)
        private set

    @Volatile
    private var graceKind: GuardKind? = null

    @Volatile
    private var graceUntil: Long = 0L

    fun guard(kind: GuardKind) {
        this.kind = kind
    }

    fun clear() {
        kind = null
    }

    fun inGrace(kind: GuardKind): Boolean =
        graceKind == kind && SystemClock.elapsedRealtime() < graceUntil

    fun grantGrace(kind: GuardKind) {
        graceKind = kind
        graceUntil = SystemClock.elapsedRealtime() + GRACE_MILLIS
        clear()
    }

    private const val GRACE_MILLIS = 60_000L
}
