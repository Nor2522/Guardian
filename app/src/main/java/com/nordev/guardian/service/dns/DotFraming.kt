package com.nordev.guardian.service.dns

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

// DNS over TLS carries one message per two byte length prefix (RFC 7858 over the
// TCP framing in RFC 1035 section 4.2.2). Kept separate from the socket so the
// framing can be tested without a server.
object DotFraming {
    const val MAX_MESSAGE = 4_096
    private const val HEADER = 12

    @Throws(IOException::class)
    fun write(query: ByteArray, output: DataOutputStream) {
        if (query.size > MAX_MESSAGE) throw IOException("query too large")
        output.writeShort(query.size)
        output.write(query)
        output.flush()
    }

    @Throws(IOException::class)
    fun read(input: DataInputStream, expectedId: Int): ByteArray {
        val length = input.readUnsignedShort()
        if (length < HEADER || length > MAX_MESSAGE) throw IOException("message length $length")
        val reply = ByteArray(length)
        input.readFully(reply)
        if (readU16(reply, 0) != expectedId) throw IOException("reply id mismatch")
        return reply
    }

    fun readU16(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
}
