package com.nordev.guardian.data.permissions

import android.app.AppOpsManager
import android.content.Context
import android.os.Process

// Usage access is an app-op, not a runtime permission. The system settings
// screen flips it; reading the app-op is the same source Android uses.
class UsageAccessChecker(private val context: Context) {
    fun isGranted(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        return appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        ) == AppOpsManager.MODE_ALLOWED
    }
}
