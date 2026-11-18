package com.nordev.guardian.service.dns

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DotFramingTest {
    @Test
    fun writesALengthPrefixThenTheMessage() {
        val query = byteArrayOf(0x12, 0x34, 1, 2, 3)
        val out = ByteArrayOutputStream()

        DotFraming.write(query, DataOutputStream(out))

        assertArrayEquals(byteArrayOf(0, 5, 0x12, 0x34, 1, 2, 3), out.toByteArray())
    }

    @Test
    fun readsAReplyWithTheMatchingId() {
        val reply = ByteArray(20).apply {
            this[0] = 0x7f
            this[1] = 0x0a.toByte()
        }
        val framed = ByteArrayOutputStream().also {
            DotFraming.write(reply, DataOutputStream(it))
        }.toByteArray()

        val parsed = DotFraming.read(DataInputStream(ByteArrayInputStream(framed)), 0x7f0a)

        assertArrayEquals(reply, parsed)
    }

    @Test
    fun rejectsAReplyWithAnotherId() {
        val reply = ByteArray(12)
        val framed = ByteArrayOutputStream().also {
            DotFraming.write(reply, DataOutputStream(it))
        }.toByteArray()

        try {
            DotFraming.read(DataInputStream(ByteArrayInputStream(framed)), 0x0042)
            fail("expected IOException")
        } catch (_: IOException) {
            // A mismatched id means the connection is out of step, not a reply.
        }
    }

    @Test
    fun rejectsAShortLength() {
        val framed = byteArrayOf(0, 3, 1, 2, 3)

        try {
            DotFraming.read(DataInputStream(ByteArrayInputStream(framed)), 0)
            fail("expected IOException")
        } catch (_: IOException) {
            // Anything under a DNS header cannot be a reply.
        }
    }

    @Test
    fun rejectsAnOversizedLength() {
        val framed = byteArrayOf(0x20, 0x01)

        try {
            DotFraming.read(DataInputStream(ByteArrayInputStream(framed)), 0)
            fail("expected IOException")
        } catch (_: IOException) {
            // Longer than any message the filter will read.
        }
    }

    @Test
    fun rejectsATruncatedMessage() {
        val framed = byteArrayOf(0, 20, 1, 2, 3)

        try {
            DotFraming.read(DataInputStream(ByteArrayInputStream(framed)), 0)
            fail("expected EOF")
        } catch (_: EOFException) {
            // A closed connection mid-message is a normal network change.
        }
    }

    @Test
    fun buildsAProbeQueryForTheGivenName() {
        val query = DnsMessage.query("cleanbrowsing.org", id = 0)

        val question = DnsMessage.parseQuestion(query)

        assertEquals("cleanbrowsing.org", question?.name)
        assertEquals(1, question?.type)
        assertEquals(query.size, question?.endOffset)
        assertEquals(1, query[5].toInt())
        assertTrue(query.size < DotFraming.MAX_MESSAGE)
    }
}
