package com.nordev.guardian.data.permissions

import android.app.NotificationManager
import android.content.Context

// On Android 13 and later this reflects the POST_NOTIFICATIONS grant.
// Before that it reflects the app-level notification toggle in Settings.
class NotificationAccessChecker(private val context: Context) {
    fun isGranted(): Boolean =
        context.getSystemService(NotificationManager::class.java)?.areNotificationsEnabled() == true
}
