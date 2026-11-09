package com.nordev.guardian.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

// Declared so the user can grant Accessibility in system settings. The block
// logic arrives in Step 6; this service does not read screen content.
class GuardianAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
}
