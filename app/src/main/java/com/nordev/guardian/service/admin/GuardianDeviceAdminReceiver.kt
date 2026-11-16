package com.nordev.guardian.service.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.nordev.guardian.data.prefs.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Being an active admin is the whole mechanism: Android refuses to uninstall a
// package with one. No wipe, lock, or password policies are requested, so this
// receiver overrides no policy callbacks.
class GuardianDeviceAdminReceiver : DeviceAdminReceiver() {

    // Remember that the parent chose uninstall protection. If the admin later
    // disappears, the status screen reports a broken check instead of a neutral
    // off. The parent removal path clears the flag.
    override fun onEnabled(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                SettingsRepository(context).setUninstallProtectionWanted(true)
            } finally {
                pending.finish()
            }
        }
    }
}
