package com.nordev.guardian.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import com.nordev.guardian.BuildConfig
import com.nordev.guardian.data.prefs.PolicyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// The stored switch is a promise to the parent, so the tunnel has to come back
// on its own after a reboot and after an app update, instead of waiting for the
// parent to open Guardian. The system starts accessibility and restores Device
// Admin; the filter is the one layer that needs this nudge.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> Unit
            else -> return
        }
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (DnsFilterVpnService.isRunning()) return@launch
                if (!PolicyRepository(context).webFilter.first().enabled) return@launch
                // Consent can be withdrawn while the app is not running. Without
                // it the start would throw, so leave the filter stopped and let
                // the Web tab report the real state.
                if (VpnService.prepare(context) != null) return@launch
                DnsFilterVpnService.start(context)
                if (BuildConfig.DEBUG) Log.d(TAG, "filter restored")
            } catch (_: Exception) {
                if (BuildConfig.DEBUG) Log.w(TAG, "filter not restored")
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "GuardianBoot"
    }
}
