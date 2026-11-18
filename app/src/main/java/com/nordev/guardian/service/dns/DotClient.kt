package com.nordev.guardian.service.dns

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

// DNS over TLS (RFC 7858) to CleanBrowsing's free Family filter. This is the
// filter's only upstream, so a failure returns null and the caller answers
// SERVFAIL. Falling back to the phone's own resolver would keep the switch green
// while the filter is off, which is the one outcome this design refuses.
//
// The socket goes out on the physical network: the tunnel only routes the two
// DNS addresses, so a connection to CleanBrowsing is never captured by our own
// TUN device and needs no protect().
class DotClient(
    private val hostname: String = HOSTNAME,
    private val addresses: List<String> = ADDRESSES,
    private val timeoutMs: Int = TIMEOUT_MS,
    private val tlsFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory,
) {
    private val lock = Any()
    private var session: Session? = null
    private var endpoint = 0

    fun query(payload: ByteArray): ByteArray? {
        if (payload.size < HEADER || payload.size > DotFraming.MAX_MESSAGE) return null
        val id = DotFraming.readU16(payload, 0)
        synchronized(lock) {
            var attempts = 0
            while (attempts < 2) {
                attempts += 1
                val active = session ?: open()
                if (active == null) return null
                session = active
                try {
                    return active.exchange(payload, id)
                } catch (_: IOException) {
                    // A cached TLS session dies on every network change. Retry
                    // once from a fresh socket before reporting the filter down.
                    closeLocked()
                }
            }
            return null
        }
    }

    fun close() {
        synchronized(lock) { closeLocked() }
    }

    private fun closeLocked() {
        session?.close()
        session = null
    }

    private fun open(): Session? {
        if (addresses.isEmpty()) return null
        for (offset in addresses.indices) {
            val index = (endpoint + offset) % addresses.size
            val socket = Socket()
            try {
                socket.connect(InetSocketAddress(addresses[index], PORT), timeoutMs)
            } catch (_: IOException) {
                runCatching { socket.close() }
                continue
            }
            val tls = try {
                startTls(socket)
            } catch (_: IOException) {
                runCatching { socket.close() }
                continue
            }
            endpoint = index
            return Session(tls)
        }
        return null
    }

    private fun startTls(socket: Socket): SSLSocket {
        val tls = tlsFactory.createSocket(socket, hostname, PORT, true) as SSLSocket
        tls.soTimeout = timeoutMs
        val parameters = tls.sslParameters
        parameters.serverNames = listOf(SNIHostName(hostname))
        // Without this the handshake succeeds against any certificate. The
        // resolver's name is checked against the certificate instead.
        parameters.endpointIdentificationAlgorithm = "HTTPS"
        tls.sslParameters = parameters
        tls.startHandshake()
        return tls
    }

    private class Session(private val tls: SSLSocket) {
        private val input = DataInputStream(BufferedInputStream(tls.inputStream))
        private val output = DataOutputStream(BufferedOutputStream(tls.outputStream))

        fun exchange(payload: ByteArray, id: Int): ByteArray {
            DotFraming.write(payload, output)
            return DotFraming.read(input, id)
        }

        fun close() {
            runCatching { tls.close() }
        }
    }

    companion object {
        const val PORT = 853

        // CleanBrowsing free Family filter, https://cleanbrowsing.org/filters.
        const val HOSTNAME = "family-filter-dns.cleanbrowsing.org"
        val ADDRESSES = listOf(
            "185.228.168.168",
            "185.228.169.168",
            "2a0d:2a00:1::",
            "2a0d:2a00:2::",
        )
        private const val TIMEOUT_MS = 4_000
        private const val HEADER = 12
    }
}
