package com.nordev.guardian.domain.web

// The shipped adult list. Read only for the parent; the asset header carries
// the version and license so the Web tab can show them without a second file.
data class AdultBlocklist(
    val version: String,
    val license: String,
    val domains: Set<String>,
) {
    val matcher: DomainMatcher = DomainMatcher(domains)

    companion object {
        const val DEFAULT_VERSION = "unknown"

        fun parse(text: String): AdultBlocklist {
            var version = DEFAULT_VERSION
            var license = ""
            val domains = linkedSetOf<String>()
            text.lineSequence().forEach { raw ->
                val line = raw.trim()
                if (line.isEmpty()) return@forEach
                if (line.startsWith("#")) {
                    val body = line.removePrefix("#").trim()
                    when {
                        body.startsWith("version:", ignoreCase = true) ->
                            version = body.substringAfter(':').trim().ifEmpty { DEFAULT_VERSION }
                        body.startsWith("license:", ignoreCase = true) ->
                            license = body.substringAfter(':').trim()
                        body.startsWith("source:", ignoreCase = true) -> Unit
                    }
                    return@forEach
                }
                val domain = DomainName.normalize(line) ?: return@forEach
                domains += domain
            }
            return AdultBlocklist(version = version, license = license, domains = domains)
        }
    }
}
