package com.nordev.guardian.domain.web

import java.net.IDN
import java.util.Locale

// Turns what a parent types into the ASCII, lowercase, bare domain the filter
// stores. Accepts a full URL or a host with a port, rejects anything that
// cannot become one DNS label chain.
object DomainName {
    private const val MAX_LENGTH = 253
    private const val MAX_LABEL = 63

    fun normalize(raw: String): String? {
        var value = raw.trim()
        if (value.isEmpty()) return null

        // Scheme, then authority only.
        value = value.substringAfter("://", value)
        value = value.substringBefore('/')
        value = value.substringBefore('?')
        value = value.substringBefore('#')
        value = value.substringAfterLast('@')
        if (!value.startsWith("[")) {
            val colon = value.indexOf(':')
            if (colon >= 0) value = value.substring(0, colon)
        }
        value = value.trimEnd('.')
        if (value.length < 3) return null
        if (value.startsWith("www.", ignoreCase = true)) value = value.substring(4)

        val ascii = try {
            IDN.toASCII(value, IDN.ALLOW_UNASSIGNED)
        } catch (_: IllegalArgumentException) {
            return null
        }
        val host = ascii.lowercase(Locale.ROOT)
        if (host.length > MAX_LENGTH) return null
        if (!host.contains('.')) return null

        val labels = host.split('.')
        if (labels.size < 2) return null
        for (label in labels) {
            if (label.isEmpty() || label.length > MAX_LABEL) return null
            if (label.startsWith('-') || label.endsWith('-')) return null
            if (label.any { it !in 'a'..'z' && it !in '0'..'9' && it != '-' }) return null
        }
        // An IPv4 literal is not a domain and suffix matching would mislead.
        if (labels.all { label -> label.all { it in '0'..'9' } }) return null
        return host
    }

    // Stored domains are punycode. Show the Unicode form back to the parent so
    // non-Latin sites stay readable.
    fun display(domain: String): String =
        runCatching { IDN.toUnicode(domain, IDN.ALLOW_UNASSIGNED) }
            .getOrDefault(domain)
            .ifEmpty { domain }
}
