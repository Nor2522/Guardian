package com.nordev.guardian.domain.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainMatcherTest {
    private val matcher = DomainMatcher(setOf("example.com", "sub.example.org"))

    @Test
    fun matchesTheStoredDomainAndItsSubdomains() {
        assertTrue(matcher.blocks("example.com"))
        assertTrue(matcher.blocks("www.example.com"))
        assertTrue(matcher.blocks("a.b.example.com"))
        assertTrue(matcher.blocks("sub.example.org"))
    }

    @Test
    fun doesNotMatchALookalikeSuffix() {
        assertFalse(matcher.blocks("badexample.com"))
        assertFalse(matcher.blocks("notexample.com"))
        assertFalse(matcher.blocks("example.com.evil.net"))
    }

    @Test
    fun doesNotMatchBareTldOrEmptyHost() {
        assertFalse(matcher.blocks("com"))
        assertFalse(matcher.blocks(""))
    }
}

class AdultBlocklistTest {
    @Test
    fun parsesMetadataAndNormalizedDomains() {
        val list = AdultBlocklist.parse(
            """
            # Guardian adult domain list
            # version: 2026.11
            # license: test fixture
            example.com
            www.example.com
            sub.example.org
            http://Example.NET/path
            not a domain
            example.com
            """.trimIndent(),
        )

        assertEquals("2026.11", list.version)
        assertEquals("test fixture", list.license)
        assertEquals(setOf("example.com", "sub.example.org", "example.net"), list.domains)
        assertEquals(3, list.matcher.size)
    }

    @Test
    fun fallsBackToUnknownVersion() {
        val list = AdultBlocklist.parse("example.com")
        assertEquals(AdultBlocklist.DEFAULT_VERSION, list.version)
        assertEquals(1, list.domains.size)
    }
}
