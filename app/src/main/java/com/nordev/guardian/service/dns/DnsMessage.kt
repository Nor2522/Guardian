package com.nordev.guardian.service.dns

import java.io.ByteArrayOutputStream
import kotlin.random.Random

// Parses the question of a DNS query and builds the small set of replies the
// filter needs. Queries never use name compression, replies reuse the query
// bytes so the question section stays intact.
data class DnsQuestion(
    val name: String,
    val type: Int,
    val endOffset: Int,
)

object DnsMessage {
    private const val HEADER = 12

    // Only the reachability probe needs a query built from scratch; every other
    // packet is a client query passed through unchanged.
    fun query(name: String, type: Int = 1, id: Int = Random.nextInt(0x10000)): ByteArray {
        val out = ByteArrayOutputStream()
        val header = ByteArray(HEADER)
        header[0] = (id ushr 8).toByte()
        header[1] = id.toByte()
        header[2] = 0x01
        header[5] = 0x01
        out.write(header)
        name.split('.').forEach { label ->
            val bytes = label.toByteArray(Charsets.US_ASCII)
            out.write(bytes.size)
            out.write(bytes)
        }
        out.write(0)
        out.write(0)
        out.write(type)
        out.write(0)
        out.write(1)
        return out.toByteArray()
    }

    fun parseQuestion(payload: ByteArray, length: Int = payload.size): DnsQuestion? {
        if (length < HEADER) return null
        val questions = readU16(payload, 4)
        if (questions != 1) return null
        var offset = HEADER
        val name = StringBuilder()
        while (true) {
            if (offset >= length) return null
            val labelLength = payload[offset].toInt() and 0xFF
            offset += 1
            if (labelLength == 0) break
            if (labelLength and 0xC0 != 0) return null
            if (offset + labelLength > length) return null
            if (name.isNotEmpty()) name.append('.')
            for (i in 0 until labelLength) {
                val byte = payload[offset + i].toInt() and 0xFF
                // A dot inside a label would make suffix matching lie.
                if (byte == 0x2E) return null
                name.append(
                    if (byte in 0x41..0x5A) (byte + 0x20).toChar() else byte.toChar(),
                )
            }
            offset += labelLength
            if (name.length > 253) return null
        }
        if (offset + 4 > length) return null
        val type = readU16(payload, offset)
        return DnsQuestion(name.toString(), type, offset + 4)
    }

    fun nxDomain(query: ByteArray, questionEnd: Int): ByteArray =
        errorResponse(query, questionEnd, rcode = 3)

    fun servFail(query: ByteArray, questionEnd: Int): ByteArray =
        errorResponse(query, questionEnd, rcode = 2)

    // Keeps the query id and the recursion-desired bit, sets the reply bits,
    // echoes the question, and clears every count but QDCOUNT.
    private fun errorResponse(query: ByteArray, questionEnd: Int, rcode: Int): ByteArray {
        val end = questionEnd.coerceIn(HEADER, query.size)
        val out = query.copyOf(end)
        out[2] = ((query[2].toInt() and 0x01) or 0x80).toByte()
        out[3] = (0x80 or (rcode and 0x0F)).toByte()
        for (i in 6..11) out[i] = 0
        return out
    }

    private fun readU16(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
}
