package com.nordev.guardian.service.dns

// Minimal IPv4/IPv6 + UDP handling for a DNS-only tunnel. Only first-fragment,
// plain UDP packets are understood; anything else is ignored.
data class UdpPacket(
    val ipv6: Boolean,
    val source: ByteArray,
    val destination: ByteArray,
    val sourcePort: Int,
    val destinationPort: Int,
    val payload: ByteArray,
)

object IpPacket {
    private const val PROTOCOL_UDP = 17
    private const val IPV4_HEADER = 20
    private const val IPV6_HEADER = 40
    private const val UDP_HEADER = 8

    fun parseUdp(packet: ByteArray, length: Int = packet.size): UdpPacket? {
        if (length < IPV4_HEADER) return null
        return when ((packet[0].toInt() and 0xF0) shr 4) {
            4 -> parseIpv4(packet, length)
            6 -> parseIpv6(packet, length)
            else -> null
        }
    }

    // Reply source is the request's destination (the tunnel address) and the
    // reply destination is the request's source, so the caller needs no
    // constants here.
    fun buildUdpReply(request: UdpPacket, payload: ByteArray): ByteArray {
        val udpLength = UDP_HEADER + payload.size
        val udp = ByteArray(udpLength)
        writeU16(udp, 0, request.destinationPort)
        writeU16(udp, 2, request.sourcePort)
        writeU16(udp, 4, udpLength)
        payload.copyInto(udp, UDP_HEADER)
        return if (request.ipv6) buildIpv6(request, udp) else buildIpv4(request, udp)
    }

    private fun parseIpv4(packet: ByteArray, length: Int): UdpPacket? {
        val headerLength = (packet[0].toInt() and 0x0F) * 4
        if (headerLength < IPV4_HEADER || length < headerLength + UDP_HEADER) return null
        if (packet[9].toInt() and 0xFF != PROTOCOL_UDP) return null
        val fragment = readU16(packet, 6)
        if (fragment and 0x3FFF != 0) return null
        val total = readU16(packet, 2).coerceAtMost(length)
        if (total < headerLength + UDP_HEADER) return null
        val udpLength = readU16(packet, headerLength + 4)
        if (udpLength < UDP_HEADER || headerLength + udpLength > total) return null
        return UdpPacket(
            ipv6 = false,
            source = packet.copyOfRange(12, 16),
            destination = packet.copyOfRange(16, 20),
            sourcePort = readU16(packet, headerLength),
            destinationPort = readU16(packet, headerLength + 2),
            payload = packet.copyOfRange(headerLength + UDP_HEADER, headerLength + udpLength),
        )
    }

    private fun parseIpv6(packet: ByteArray, length: Int): UdpPacket? {
        if (length < IPV6_HEADER + UDP_HEADER) return null
        if (packet[6].toInt() and 0xFF != PROTOCOL_UDP) return null
        val total = (IPV6_HEADER + readU16(packet, 4)).coerceAtMost(length)
        if (total < IPV6_HEADER + UDP_HEADER) return null
        val udpLength = readU16(packet, IPV6_HEADER + 4)
        if (udpLength < UDP_HEADER || IPV6_HEADER + udpLength > total) return null
        return UdpPacket(
            ipv6 = true,
            source = packet.copyOfRange(8, 24),
            destination = packet.copyOfRange(24, 40),
            sourcePort = readU16(packet, IPV6_HEADER),
            destinationPort = readU16(packet, IPV6_HEADER + 2),
            payload = packet.copyOfRange(IPV6_HEADER + UDP_HEADER, IPV6_HEADER + udpLength),
        )
    }

    private fun buildIpv4(request: UdpPacket, udp: ByteArray): ByteArray {
        val total = IPV4_HEADER + udp.size
        val out = ByteArray(total)
        out[0] = 0x45
        out[8] = 64
        out[9] = PROTOCOL_UDP.toByte()
        writeU16(out, 2, total)
        writeU16(out, 6, 0x4000)
        request.destination.copyInto(out, 12)
        request.source.copyInto(out, 16)
        writeU16(out, 10, checksum(out, 0, IPV4_HEADER))
        writeU16(udp, 6, 0)
        val pseudo = ByteArray(12)
        request.destination.copyInto(pseudo, 0)
        request.source.copyInto(pseudo, 4)
        pseudo[9] = PROTOCOL_UDP.toByte()
        writeU16(pseudo, 10, udp.size)
        writeU16(udp, 6, checksum(pseudo, 0, pseudo.size, udp, 0, udp.size))
        udp.copyInto(out, IPV4_HEADER)
        return out
    }

    private fun buildIpv6(request: UdpPacket, udp: ByteArray): ByteArray {
        val out = ByteArray(IPV6_HEADER + udp.size)
        out[0] = 0x60
        out[6] = PROTOCOL_UDP.toByte()
        out[7] = 64
        writeU16(out, 4, udp.size)
        request.destination.copyInto(out, 8)
        request.source.copyInto(out, 24)
        writeU16(udp, 6, 0)
        val pseudo = ByteArray(40)
        request.destination.copyInto(pseudo, 0)
        request.source.copyInto(pseudo, 16)
        writeU16(pseudo, 34, udp.size)
        pseudo[39] = PROTOCOL_UDP.toByte()
        writeU16(udp, 6, checksum(pseudo, 0, pseudo.size, udp, 0, udp.size))
        udp.copyInto(out, IPV6_HEADER)
        return out
    }

    // One's complement checksum over one or two slices. Returns a non-zero
    // value so a computed zero becomes 0xFFFF, as UDP requires.
    private fun checksum(
        first: ByteArray,
        firstOffset: Int,
        firstLength: Int,
        second: ByteArray? = null,
        secondOffset: Int = 0,
        secondLength: Int = 0,
    ): Int {
        var sum = 0
        var i = firstOffset
        val firstEnd = firstOffset + firstLength
        while (i + 1 < firstEnd) {
            sum += ((first[i].toInt() and 0xFF) shl 8) or (first[i + 1].toInt() and 0xFF)
            sum = (sum and 0xFFFF) + (sum shr 16)
            i += 2
        }
        if (i < firstEnd) {
            sum += (first[i].toInt() and 0xFF) shl 8
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        if (second != null) {
            i = secondOffset
            val secondEnd = secondOffset + secondLength
            while (i + 1 < secondEnd) {
                sum += ((second[i].toInt() and 0xFF) shl 8) or (second[i + 1].toInt() and 0xFF)
                sum = (sum and 0xFFFF) + (sum shr 16)
                i += 2
            }
            if (i < secondEnd) {
                sum += (second[i].toInt() and 0xFF) shl 8
                sum = (sum and 0xFFFF) + (sum shr 16)
            }
        }
        val folded = (sum and 0xFFFF) + (sum shr 16)
        val result = folded.inv() and 0xFFFF
        return if (result == 0) 0xFFFF else result
    }

    private fun readU16(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)

    private fun writeU16(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value shr 8).toByte()
        data[offset + 1] = value.toByte()
    }
}
