package com.nordev.guardian.domain.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainNameTest {
    @Test
    fun stripsSchemeWwwPathAndQuery() {
        assertEquals("example.com", DomainName.normalize("https://www.Example.com/watch?v=1"))
        assertEquals("example.com", DomainName.normalize("http://example.com"))
        assertEquals("example.com", DomainName.normalize("example.com/path"))
    }

    @Test
    fun keepsSubdomainsAndDropsTrailingDotAndPort() {
        assertEquals("sub.example.com", DomainName.normalize("sub.example.com."))
        assertEquals("example.com", DomainName.normalize("example.com:8443"))
        assertEquals("ads.example.com", DomainName.normalize("https://ads.example.com/x"))
    }

    @Test
    fun stripsUserInfo() {
        assertEquals("example.com", DomainName.normalize("https://user:pass@example.com/x"))
    }

    @Test
    fun convertsUnicodeToPunycode() {
        assertEquals("xn--mnchen-3ya.de", DomainName.normalize("MÜNCHEN.DE"))
    }

    @Test
    fun rejectsValuesThatAreNotDomains() {
        assertNull(DomainName.normalize(""))
        assertNull(DomainName.normalize("example"))
        assertNull(DomainName.normalize("http://"))
        assertNull(DomainName.normalize("exa mple.com"))
        assertNull(DomainName.normalize("8.8.8.8"))
        assertNull(DomainName.normalize("-bad.example.com"))
        assertNull(DomainName.normalize("bad-.example.com"))
    }

    @Test
    fun rejectsLabelsLongerThanSixtyThree() {
        val label = "a".repeat(64)
        assertNull(DomainName.normalize("$label.com"))
    }
}
