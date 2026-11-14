package com.nordev.guardian.ui.nav

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.nordev.guardian.domain.block.BlockReason

// Memory-only block state in the same process as the accessibility service.
// The service writes the blocked decision here when the policy blocks the
// focused app; the nav host renders the block screen from it.
object BlockState {
    var blockedPackage: String? by mutableStateOf<String?>(null)
        private set

    var blockedReason: BlockReason? by mutableStateOf<BlockReason?>(null)
        private set

    fun block(packageName: String, reason: BlockReason) {
        blockedPackage = packageName
        blockedReason = reason
    }

    fun clear() {
        blockedPackage = null
        blockedReason = null
    }
}
