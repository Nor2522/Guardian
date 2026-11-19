package com.nordev.guardian.data.permissions

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import com.nordev.guardian.BuildConfig

// Accessibility is granted per service in system settings. The manager lists
// only services the user has enabled.
class AccessibilityAccessChecker(private val context: Context) {
    @Volatile
    private var lastLogged: Boolean? = null

    fun isGranted(): Boolean = runCatching {
        val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
        val services = manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
        )
        val names = services.map { it.resolveInfo.serviceInfo.packageName }
        val listed = names.any { it == context.packageName }
        if (BuildConfig.DEBUG && listed != lastLogged) {
            lastLogged = listed
            val setting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            )
            Log.d(TAG, "listed=$listed manager=$names setting=$setting")
        }
        listed
    }.onFailure {
        if (BuildConfig.DEBUG) Log.w(TAG, "check failed", it)
    }.getOrDefault(false)

    private companion object {
        const val TAG = "GuardianA11y"
    }
}
