package com.nordev.guardian.data.web

import com.nordev.guardian.service.dns.DnsMessage
import com.nordev.guardian.service.dns.DotClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Whether CleanBrowsing answers. The filter has no other upstream, so this is the
// difference between "sites are filtered" and "sites do not load", and the Web
// tab has to say which one it is instead of keeping a green card.
object UpstreamHealth {
    enum class State { Unknown, Connected, Unreachable }

    @Volatile
    var state: State = State.Unknown
        private set

    fun markConnected() {
        state = State.Connected
    }

    fun markUnreachable() {
        state = State.Unreachable
    }

    fun reset() {
        state = State.Unknown
    }

    suspend fun probe(): State = withContext(Dispatchers.IO) {
        val client = DotClient()
        try {
            val reply = client.query(DnsMessage.query(PROBE_NAME))
            state = if (reply == null) State.Unreachable else State.Connected
        } finally {
            client.close()
        }
        state
    }

    private const val PROBE_NAME = "cleanbrowsing.org"
}
