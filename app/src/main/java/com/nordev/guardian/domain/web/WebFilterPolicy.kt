package com.nordev.guardian.domain.web

// What the parent chose on the Web tab. Custom domains are stored in the same
// form DomainName produces, so matching never has to normalize at query time.
data class WebFilterPolicy(
    val enabled: Boolean,
    val customDomains: Set<String>,
) {
    companion object {
        // A parental control app starts with the filter on; the parent can turn
        // it off on the Web tab.
        val Default = WebFilterPolicy(enabled = true, customDomains = emptySet())
    }
}
