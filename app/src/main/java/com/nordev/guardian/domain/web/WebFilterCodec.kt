package com.nordev.guardian.domain.web

// One small versioned document for the Web tab. The first line carries the
// switch so "off" survives a restart; every other line is a normalized domain.
// Decoding never throws: a bad line is dropped and an empty document means the
// first-run default of filter on.
object WebFilterCodec {
    private const val SWITCH = "enabled"

    fun encode(policy: WebFilterPolicy): String = buildString {
        append(SWITCH).append(' ').append(if (policy.enabled) "1" else "0")
        policy.customDomains.sorted().forEach { domain ->
            append('\n').append(domain)
        }
    }

    fun decode(text: String): WebFilterPolicy {
        var enabled = WebFilterPolicy.Default.enabled
        val domains = linkedSetOf<String>()
        text.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEach
            if (line.startsWith("$SWITCH ")) {
                enabled = line.removePrefix("$SWITCH ").trim() != "0"
                return@forEach
            }
            DomainName.normalize(line)?.let { domains += it }
        }
        return WebFilterPolicy(enabled = enabled, customDomains = domains)
    }
}
