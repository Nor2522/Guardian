package com.nordev.guardian.domain.block

// Outcome of evaluating the active rules for one foreground package.
// Allowed means the child may use the app; Blocked means the policy engine
// picked a reason and the UI should show the block screen.
sealed interface AccessDecision {
    data object Allowed : AccessDecision

    data class Blocked(val reason: BlockReason) : AccessDecision
}
