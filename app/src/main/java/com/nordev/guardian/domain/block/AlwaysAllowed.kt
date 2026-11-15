package com.nordev.guardian.domain.block

// Packages Guardian never pauses, whatever the rules say. Calls, alarms, the
// launcher, and System UI must keep working so the child is never trapped and
// can always reach a parent. The accessibility service adds the current home
// package and the enabled keyboards, which differ per phone.
object AlwaysAllowed {
    private val STATIC = setOf(
        // Dialer and emergency dialer.
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.emergency",
        "com.android.phone",
        // Clock and alarms.
        "com.android.deskclock",
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.samsung.android.app.clockpack",
        // System surfaces.
        "com.android.systemui",
        "android",
    )

    val packages: Set<String> get() = STATIC

    fun contains(packageName: String): Boolean = packageName in STATIC
}
