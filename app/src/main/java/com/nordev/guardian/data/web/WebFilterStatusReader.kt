package com.nordev.guardian.data.web

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.VpnService
import com.nordev.guardian.service.DnsFilterVpnService

// The honest state of the filter, read from the system instead of the stored
// switch. A saved "on" is not proof the tunnel is up.
enum class WebFilterStatus { On, Off, NeedsConsent, Stopped, AnotherVpn, NoNetwork }

class WebFilterStatusReader(private val context: Context) {

    // getAllNetworks is deprecated in API 37 but remains the only synchronous
    // way to enumerate every active tunnel in one pass.
    @Suppress("DEPRECATION")
    fun read(enabled: Boolean): WebFilterStatus {
        if (!enabled) return WebFilterStatus.Off
        val manager = context.getSystemService(ConnectivityManager::class.java)
            ?: return WebFilterStatus.Stopped
        val vpnNetworks = manager.allNetworks.orEmpty().filter { network ->
            manager.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
        if (vpnNetworks.any { isOurTunnel(manager, it) }) return WebFilterStatus.On
        // Android allows one VPN at a time. A foreign tunnel explains why ours
        // cannot run, so report it instead of a generic stopped state.
        if (vpnNetworks.isNotEmpty()) return WebFilterStatus.AnotherVpn
        if (VpnService.prepare(context) != null) return WebFilterStatus.NeedsConsent
        if (!hasInternet(manager)) return WebFilterStatus.NoNetwork
        return WebFilterStatus.Stopped
    }

    private fun isOurTunnel(manager: ConnectivityManager, network: Network): Boolean {
        val properties = manager.getLinkProperties(network) ?: return false
        return properties.linkAddresses.any { link ->
            val host = link.address.hostAddress
            host == DnsFilterVpnService.ADDRESS_V4 || host == DnsFilterVpnService.ADDRESS_V6
        }
    }

    private fun hasInternet(manager: ConnectivityManager): Boolean {
        val active = manager.activeNetwork ?: return false
        return manager.getNetworkCapabilities(active)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
