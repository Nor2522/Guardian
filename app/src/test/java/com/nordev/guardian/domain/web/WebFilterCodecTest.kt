package com.nordev.guardian.domain.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebFilterCodecTest {
    @Test
    fun emptyDocumentMeansFilterOnWithNoCustomSites() {
        val policy = WebFilterCodec.decode("")

        assertTrue(policy.enabled)
        assertTrue(policy.customDomains.isEmpty())
    }

    @Test
    fun roundTripsTheSwitchAndDomains() {
        val original = WebFilterPolicy(
            enabled = false,
            customDomains = setOf("games.example.com", "example.org"),
        )

        val decoded = WebFilterCodec.decode(WebFilterCodec.encode(original))

        assertEquals(original, decoded)
        assertFalse(decoded.enabled)
    }

    @Test
    fun decodeNormalizesDropsJunkAndDeduplicates() {
        val policy = WebFilterCodec.decode(
            """
            enabled 1
            https://www.Example.com/path
            example.com
            not a domain
            -=-
            """.trimIndent(),
        )

        assertTrue(policy.enabled)
        assertEquals(setOf("example.com"), policy.customDomains)
    }

    @Test
    fun aDomainThatStartsWithEnabledIsNotTheSwitch() {
        val policy = WebFilterCodec.decode("enabled.com")

        assertTrue(policy.enabled)
        assertEquals(setOf("enabled.com"), policy.customDomains)
    }
}
