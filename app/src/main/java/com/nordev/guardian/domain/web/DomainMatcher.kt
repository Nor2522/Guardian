package com.nordev.guardian.domain.web

// A stored domain blocks itself and its subdomains. Matching walks the host
// from the left so badexample.com never matches example.com.
class DomainMatcher(domains: Set<String>) {
    private val domains: Set<String> = domains.toSet()

    val size: Int get() = domains.size

    fun blocks(host: String): Boolean {
        var candidate = host
        while (true) {
            if (candidate in domains) return true
            val dot = candidate.indexOf('.')
            if (dot < 0) return false
            candidate = candidate.substring(dot + 1)
        }
    }
}
