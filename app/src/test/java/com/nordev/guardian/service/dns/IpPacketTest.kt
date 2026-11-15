package com.nordev.guardian.service.dns

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IpPacketTest {
    private val payload = byteArrayOf(
        0x12, 0x34, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00,
    )

    @Test
    fun parsesIpv4Udp() {
        val parsed = IpPacket.parseUdp(ipv4Query())!!

        assertFalse(parsed.ipv6)
        assertArrayEquals(byteArrayOf(10, 0, 0, 2), parsed.source)
        assertArrayEquals(byteArrayOf(10, 111, 222.toByte(), 1), parsed.destination)
        assertEquals(40000, parsed.sourcePort)
        assertEquals(53, parsed.destinationPort)
        assertArrayEquals(payload, parsed.payload)
    }

    @Test
    fun parsesIpv6Udp() {
        val parsed = IpPacket.parseUdp(ipv6Query())!!

        assertTrue(parsed.ipv6)
        assertEquals(40000, parsed.sourcePort)
        assertEquals(53, parsed.destinationPort)
        assertArrayEquals(payload, parsed.payload)
    }

    @Test
    fun rejectsFragmentsOtherProtocolsAndBadLengths() {
        assertNull(IpPacket.parseUdp(ipv4Query().also { it[6] = 0x20 }))
        assertNull(IpPacket.parseUdp(ipv4Query().also { it[9] = 6 }))
        assertNull(IpPacket.parseUdp(ipv4Query().also { it[25] = 0xFF.toByte() }))
        assertNull(IpPacket.parseUdp(ipv4Query().copyOf(24)))
        assertNull(IpPacket.parseUdp(ByteArray(19)))
        assertNull(IpPacket.parseUdp(byteArrayOf(0x70)))
    }

    @Test
    fun ipv4ReplySwapsAddressesAndPortsAndKeepsThePayload() {
        val request = IpPacket.parseUdp(ipv4Query())!!
        val reply = IpPacket.parseUdp(IpPacket.buildUdpReply(request, payload))!!

        assertArrayEquals(byteArrayOf(10, 111, 222.toByte(), 1), reply.source)
        assertArrayEquals(byteArrayOf(10, 0, 0, 2), reply.destination)
        assertEquals(53, reply.sourcePort)
        assertEquals(40000, reply.destinationPort)
        assertArrayEquals(payload, reply.payload)
    }

    @Test
    fun ipv6ReplySwapsAddressesAndPortsAndKeepsThePayload() {
        val request = IpPacket.parseUdp(ipv6Query())!!
        val bytes = IpPacket.buildUdpReply(request, payload)
        val reply = IpPacket.parseUdp(bytes)!!

        assertTrue(reply.ipv6)
        assertEquals(53, reply.sourcePort)
        assertEquals(40000, reply.destinationPort)
        assertArrayEquals(payload, reply.payload)
        assertArrayEquals(tunnelV6(), reply.source)
    }

    @Test
    fun replyCarriesValidIpv4AndUdpChecksums() {
        val request = IpPacket.parseUdp(ipv4Query())!!
        val reply = IpPacket.buildUdpReply(request, payload)

        assertEquals(0xFFFF, foldedSum(reply.copyOfRange(0, 20)))

        val udp = reply.copyOfRange(20, reply.size)
        val pseudo = byteArrayOf(
            reply[12], reply[13], reply[14], reply[15],
            reply[16], reply[17], reply[18], reply[19],
            0, 17,
            (udp.size shr 8).toByte(), udp.size.toByte(),
        )
        assertEquals(0xFFFF, foldedSum(pseudo + udp))
    }

    @Test
    fun replyCarriesValidIpv6UdpChecksum() {
        val request = IpPacket.parseUdp(ipv6Query())!!
        val reply = IpPacket.buildUdpReply(request, payload)
        val udp = reply.copyOfRange(40, reply.size)
        val pseudo = ByteArray(40).apply {
            reply.copyInto(this, 0, 8, 24)
            reply.copyInto(this, 16, 24, 40)
            this[35] = udp.size.toByte()
            this[39] = 17
        }

        assertEquals(0xFFFF, foldedSum(pseudo + udp))
    }

    private fun foldedSum(data: ByteArray): Int {
        var sum = 0
        var index = 0
        while (index + 1 < data.size) {
            sum += ((data[index].toInt() and 0xFF) shl 8) or (data[index + 1].toInt() and 0xFF)
            index += 2
        }
        if (index < data.size) sum += (data[index].toInt() and 0xFF) shl 8
        sum = (sum and 0xFFFF) + (sum shr 16)
        sum = (sum and 0xFFFF) + (sum shr 16)
        return sum and 0xFFFF
    }

    private fun ipv4Query(): ByteArray {
        val total = 20 + 8 + payload.size
        val out = ByteArray(total)
        out[0] = 0x45
        out[2] = (total shr 8).toByte()
        out[3] = total.toByte()
        out[8] = 64
        out[9] = 17
        byteArrayOf(10, 0, 0, 2).copyInto(out, 12)
        byteArrayOf(10, 111, 222.toByte(), 1).copyInto(out, 16)
        writePort(out, 20, 40000)
        writePort(out, 22, 53)
        writePort(out, 24, 8 + payload.size)
        payload.copyInto(out, 28)
        return out
    }

    private fun ipv6Query(): ByteArray {
        val udpLength = 8 + payload.size
        val out = ByteArray(40 + udpLength)
        out[0] = 0x60
        out[4] = (udpLength shr 8).toByte()
        out[5] = udpLength.toByte()
        out[6] = 17
        out[7] = 64
        byteArrayOf(0xfd.toByte(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2).copyInto(out, 8)
        tunnelV6().copyInto(out, 24)
        writePort(out, 40, 40000)
        writePort(out, 42, 53)
        writePort(out, 44, udpLength)
        payload.copyInto(out, 48)
        return out
    }

    private fun tunnelV6(): ByteArray = byteArrayOf(
        0xfd.toByte(), 0x00, 0x11, 0x11, 0x22, 0x22, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
    )

    private fun writePort(out: ByteArray, offset: Int, port: Int) {
        out[offset] = (port shr 8).toByte()
        out[offset + 1] = port.toByte()
    }
}
