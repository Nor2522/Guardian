package com.nordev.guardian.service.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.nordev.guardian.service.DnsFilterVpnService
import com.nordev.guardian.service.GuardianAccessibilityService

// The parent removal path turns protection off before Android shows the
// uninstall screen. Order matters: the tunnel and the guard go down first, then
// the admin, so nothing keeps running if the parent cancels the uninstall.
object GuardianProtection {
    fun turnOff(context: Context) {
        DnsFilterVpnService.stop(context)
        GuardianAccessibilityService.requestStop()
        val manager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, GuardianDeviceAdminReceiver::class.java)
        runCatching { manager?.removeActiveAdmin(admin) }
    }
}
