package com.nordev.guardian.data.permissions

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.nordev.guardian.service.admin.GuardianDeviceAdminReceiver

// Device Admin has no app-op. The system tracks the active admin component for
// each user, so ask it directly.
class DeviceAdminAccessChecker(private val context: Context) {
    fun isGranted(): Boolean {
        val manager = context.getSystemService(DevicePolicyManager::class.java) ?: return false
        val component = ComponentName(context, GuardianDeviceAdminReceiver::class.java)
        return manager.isAdminActive(component)
    }
}
