package com.nordev.guardian.data.permissions

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager

// Accessibility is granted per service in system settings. The manager lists
// only services the user has enabled.
class AccessibilityAccessChecker(private val context: Context) {
    fun isGranted(): Boolean {
        val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
    }
}
