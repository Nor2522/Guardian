package com.nordev.guardian.ui.nav

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// Memory-only block state in the same process as the accessibility service.
// The service writes the blocked package here when the policy blocks the
// focused app; the nav host renders the block screen from it.
object BlockState {
    var blockedPackage: String? by mutableStateOf<String?>(null)
        private set

    fun block(packageName: String) {
        blockedPackage = packageName
    }

    fun clear() {
        blockedPackage = null
    }
}
