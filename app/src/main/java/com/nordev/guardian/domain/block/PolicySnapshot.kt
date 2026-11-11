package com.nordev.guardian.domain.block

class PolicySnapshot(
    private val limits: Map<String, Int>,
) {
    fun decide(packageName: String, usedMillisToday: Long): AccessDecision {
        val limitMinutes = limits[packageName] ?: return AccessDecision.Allowed
        val limitMillis = limitMinutes * 60_000L
        if (usedMillisToday < limitMillis) return AccessDecision.Allowed
        return AccessDecision.Blocked(BlockReason.DailyLimit(packageName, limitMinutes))
    }
}
