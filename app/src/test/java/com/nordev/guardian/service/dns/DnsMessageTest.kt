package com.nordev.guardian.service.dns

import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DnsMessageTest {
    @Test
    fun parsesAQueryAndLowercasesTheName() {
        val query = query("WWW.Example.COM")

        val question = DnsMessage.parseQuestion(query)

        assertEquals("www.example.com", question?.name)
        assertEquals(1, question?.type)
        assertEquals(query.size, question?.endOffset)
    }

    @Test
    fun parsesTheRootQuestion() {
        val question = DnsMessage.parseQuestion(query(""))

        assertEquals("", question?.name)
    }

    @Test
    fun rejectsTruncatedAndMalformedQuestions() {
        assertNull(DnsMessage.parseQuestion(ByteArray(11)))
        assertNull(DnsMessage.parseQuestion(query("example.com").copyOf(14)))
        assertNull(DnsMessage.parseQuestion(query("example.com").copyOf(15)))

        val twoQuestions = query("example.com").also {
            it[4] = 0
            it[5] = 2
        }
        assertNull(DnsMessage.parseQuestion(twoQuestions))
    }

    @Test
    fun rejectsACompressionPointerInTheQuestion() {
        val query = query("example.com")
        query[12] = 0xC0.toByte()

        assertNull(DnsMessage.parseQuestion(query))
    }

    @Test
    fun nxDomainKeepsIdEchoesTheQuestionAndClearsCounts() {
        val query = query("www.example.com")
        val question = DnsMessage.parseQuestion(query)!!

        val reply = DnsMessage.nxDomain(query, question.endOffset)

        assertEquals(query.size, reply.size)
        assertEquals(0x12, reply[0].toInt() and 0xFF)
        assertEquals(0x34, reply[1].toInt() and 0xFF)
        assertEquals(0x81, reply[2].toInt() and 0xFF)
        assertEquals(0x83, reply[3].toInt() and 0xFF)
        assertEquals(0, reply[6].toInt())
        assertEquals(0, reply[11].toInt())
        assertArrayEquals(
            query.copyOfRange(12, question.endOffset),
            reply.copyOfRange(12, question.endOffset),
        )
    }

    @Test
    fun servFailUsesRcodeTwo() {
        val query = query("example.com")
        val reply = DnsMessage.servFail(query, DnsMessage.parseQuestion(query)!!.endOffset)

        assertEquals(0x82, reply[3].toInt() and 0xFF)
    }

    private fun query(name: String, type: Int = 1): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(0x12, 0x34, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
        if (name.isNotEmpty()) {
            name.split('.').forEach { label ->
                out.write(label.length)
                out.write(label.toByteArray(Charsets.US_ASCII))
            }
        }
        out.write(0)
        out.write((type shr 8) and 0xFF)
        out.write(type and 0xFF)
        out.write(0)
        out.write(1)
        return out.toByteArray()
    }
}
