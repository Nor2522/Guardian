package com.nordev.guardian.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.nordev.guardian.BuildConfig
import com.nordev.guardian.MainActivity
import com.nordev.guardian.R
import com.nordev.guardian.data.prefs.PolicyRepository
import com.nordev.guardian.data.web.BlocklistRepository
import com.nordev.guardian.data.web.UpstreamHealth
import com.nordev.guardian.domain.web.DomainMatcher
import com.nordev.guardian.service.dns.DnsMessage
import com.nordev.guardian.service.dns.DnsQuestion
import com.nordev.guardian.service.dns.DotClient
import com.nordev.guardian.service.dns.IpPacket
import com.nordev.guardian.service.dns.UdpPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

// A DNS-only tunnel. Apps ask the tunnel address for names; blocked names get
// NXDOMAIN locally, everything else is resolved through CleanBrowsing's free
// Family filter over DNS over TLS. There is no second upstream: if CleanBrowsing
// cannot be reached the query fails, because falling back to the phone's
// resolver would silently remove the filter. No page content is read and no
// query is stored.
class DnsFilterVpnService : VpnService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val forwarder = Executors.newFixedThreadPool(FORWARD_THREADS)
    private val writeLock = Any()
    private val dot = DotClient()

    @Volatile
    private var adultMatcher = DomainMatcher(emptySet())

    @Volatile
    private var customMatcher = DomainMatcher(emptySet())

    private var tunnel: ParcelFileDescriptor? = null
    private var output: FileOutputStream? = null
    private var reader: Thread? = null

    @Volatile
    private var policyEnabled = true

    override fun onCreate() {
        super.onCreate()
        createChannel()
        adultMatcher = BlocklistRepository(this).adultList().matcher
        val policyRepository = PolicyRepository(this)
        scope.launch {
            policyRepository.webFilter.collect { policy ->
                customMatcher = DomainMatcher(policy.customDomains)
                policyEnabled = policy.enabled
                // Turning the switch off tears the tunnel down now. The system
                // keeps a VPN service bound, so stopSelf alone would not reach
                // onDestroy and the tunnel would keep filtering.
                if (!policy.enabled) stopFilter()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopFilter()
            return START_NOT_STICKY
        }
        synchronized(this) {
            if (running) return START_STICKY
            startForeground(NOTIFICATION_ID, buildNotification())
            val descriptor = try {
                establishTunnel()
            } catch (_: Exception) {
                null
            }
            if (descriptor == null) {
                Log.w(TAG, "tunnel not established")
                stopFilter()
                return START_NOT_STICKY
            }
            tunnel = descriptor
            output = FileOutputStream(descriptor.fileDescriptor)
            running = true
            reader = Thread({ readLoop(descriptor) }, "guardian-dns-reader").apply { start() }
            if (BuildConfig.DEBUG) Log.d(TAG, "filter up")
            // Probe once at start so the Web tab can report an unreachable
            // filter before the first app query fails.
            scope.launch { UpstreamHealth.probe() }
            // The switch may have turned off while the tunnel was being set up.
            if (!policyEnabled) {
                stopFilter()
                return START_NOT_STICKY
            }
            return START_STICKY
        }
    }

    override fun onRevoke() {
        stopFilter()
    }

    @Synchronized
    private fun stopFilter() {
        closeTunnel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @Synchronized
    private fun closeTunnel() {
        running = false
        try {
            tunnel?.close()
        } catch (_: IOException) {
        }
        tunnel = null
        output = null
        reader?.interrupt()
        reader = null
    }

    override fun onDestroy() {
        closeTunnel()
        dot.close()
        forwarder.shutdownNow()
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun establishTunnel(): ParcelFileDescriptor? {
        val builder = Builder()
            .setSession(getString(R.string.filter_session_name))
            .setMtu(TUNNEL_MTU)
            // establish() hands back a non-blocking descriptor by default, so an
            // idle tunnel made the reader spin at a full core. Blocking reads
            // cost nothing until a packet actually arrives.
            .setBlocking(true)
            .addAddress(ADDRESS_V4, 32)
            .addDnsServer(DNS_V4)
            .addRoute(DNS_V4, 32)
            .addAddress(ADDRESS_V6, 128)
            .addDnsServer(DNS_V6)
            .addRoute(DNS_V6, 128)
            .addDisallowedApplication(packageName)
        return builder.establish()
    }

    private fun readLoop(descriptor: ParcelFileDescriptor) {
        val input = FileInputStream(descriptor.fileDescriptor)
        val buffer = ByteArray(MAX_PACKET)
        var emptyReads = 0
        while (running) {
            val length = try {
                input.read(buffer)
            } catch (e: IOException) {
                if (running && BuildConfig.DEBUG) Log.d(TAG, "reader stopped: ${e.message}")
                break
            }
            if (length <= 0) {
                // The system builds the tunnel interface asynchronously, so the
                // first reads can come back empty before any packet is attached.
                // Give it a moment, then treat the descriptor as gone: an empty
                // read never means "no packet yet", and spinning on one burned a
                // full core while quietly dropping every query.
                if (++emptyReads > EMPTY_READ_LIMIT) {
                    if (running && BuildConfig.DEBUG) Log.d(TAG, "reader ended: read=$length")
                    break
                }
                if (emptyReads == 1 && BuildConfig.DEBUG) {
                    Log.d(TAG, "reader empty read, waiting for the tunnel")
                }
                try {
                    Thread.sleep(EMPTY_READ_DELAY_MILLIS)
                } catch (_: InterruptedException) {
                    break
                }
                continue
            }
            emptyReads = 0
            val packet = IpPacket.parseUdp(buffer, length) ?: continue
            if (packet.destinationPort != DNS_PORT) continue
            if (!isTunnelDnsAddress(packet.destination)) continue
            val question = DnsMessage.parseQuestion(packet.payload) ?: continue
            if (adultMatcher.blocks(question.name) || customMatcher.blocks(question.name)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "filtered ${question.name}")
                writeResponse(packet, DnsMessage.nxDomain(packet.payload, question.endOffset))
                continue
            }
            dispatch(packet, question)
        }
        // The reader is the filter. A dead reader with the switch still on would
        // promise protection that is not there, so stop the tunnel and let the
        // Web tab report Stopped until the next start.
        if (running) stopFilter()
    }

    private fun dispatch(packet: UdpPacket, question: DnsQuestion) {
        try {
            forwarder.execute { forward(packet, question) }
        } catch (_: RejectedExecutionException) {
            writeResponse(packet, DnsMessage.servFail(packet.payload, question.endOffset))
        }
    }

    private fun forward(packet: UdpPacket, question: DnsQuestion) {
        val reply = dot.query(packet.payload)
        if (reply == null) {
            UpstreamHealth.markUnreachable()
            if (BuildConfig.DEBUG) Log.d(TAG, "cleanbrowsing unreachable")
            writeResponse(packet, DnsMessage.servFail(packet.payload, question.endOffset))
            return
        }
        UpstreamHealth.markConnected()
        writeResponse(packet, reply)
    }

    private fun isTunnelDnsAddress(address: ByteArray): Boolean =
        address.contentEquals(DNS_V4_BYTES) || address.contentEquals(DNS_V6_BYTES)

    private fun writeResponse(request: UdpPacket, dns: ByteArray) {
        if (dns.size + IPV6_HEADER + UDP_HEADER > TUNNEL_MTU) return
        val stream = output ?: return
        val bytes = IpPacket.buildUdpReply(request, dns)
        synchronized(writeLock) {
            try {
                stream.write(bytes)
            } catch (_: IOException) {
                // The tunnel is closing; the reader loop will exit too.
            }
        }
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.filter_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.filter_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(getString(R.string.filter_notification_title))
            .setContentText(getString(R.string.filter_notification_text))
            .setContentIntent(open)
            .setOngoing(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val TAG = "GuardianDns"
        private const val ACTION_STOP = "com.nordev.guardian.action.STOP_FILTER"
        private const val CHANNEL_ID = "web_filter"
        private const val NOTIFICATION_ID = 43
        private const val DNS_PORT = 53
        private const val EMPTY_READ_LIMIT = 20
        private const val EMPTY_READ_DELAY_MILLIS = 100L
        private const val TUNNEL_MTU = 1500
        private const val IPV6_HEADER = 40
        private const val UDP_HEADER = 8
        private const val MAX_PACKET = 32767
        private const val FORWARD_THREADS = 4
        internal const val ADDRESS_V4 = "10.111.222.1"
        private const val DNS_V4 = "10.111.222.2"
        internal const val ADDRESS_V6 = "fd00:1111:2222::1"
        private const val DNS_V6 = "fd00:1111:2222::2"

        // The DNS server address must not be an address of the tunnel interface,
        // or the kernel delivers the query locally and the reader never sees it.
        private val DNS_V4_BYTES = byteArrayOf(10, 111, 222.toByte(), 2)
        private val DNS_V6_BYTES = byteArrayOf(
            0xfd.toByte(), 0x00, 0x11, 0x11, 0x22, 0x22, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02,
        )

        @Volatile
        private var running = false

        fun isRunning(): Boolean = running

        fun start(context: Context) {
            context.startForegroundService(Intent(context, DnsFilterVpnService::class.java))
        }

        fun stop(context: Context) {
            // A VPN service is bound by the system, so stopService does not call
            // onDestroy. Send an explicit stop that tears the interface down.
            if (!running) {
                context.stopService(Intent(context, DnsFilterVpnService::class.java))
                return
            }
            runCatching {
                context.startService(
                    Intent(context, DnsFilterVpnService::class.java).setAction(ACTION_STOP),
                )
            }
        }
    }
}
