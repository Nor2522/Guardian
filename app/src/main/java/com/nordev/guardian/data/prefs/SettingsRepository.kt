package com.nordev.guardian.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "settings")

// Consent is stored per permission. A missing key means the disclosure has not
// been accepted yet; the system grant itself is checked separately.
class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore

    val usageConsentAt: Flow<Long> = store.data.map { it[USAGE_CONSENT_AT] ?: 0L }
    val notificationConsentAt: Flow<Long> = store.data.map { it[NOTIFICATION_CONSENT_AT] ?: 0L }
    val accessibilityConsentAt: Flow<Long> = store.data.map { it[ACCESSIBILITY_CONSENT_AT] ?: 0L }
    val vpnConsentAt: Flow<Long> = store.data.map { it[VPN_CONSENT_AT] ?: 0L }

    suspend fun recordUsageConsent(now: Long = System.currentTimeMillis()) {
        store.edit { it[USAGE_CONSENT_AT] = now }
    }

    suspend fun recordNotificationConsent(now: Long = System.currentTimeMillis()) {
        store.edit { it[NOTIFICATION_CONSENT_AT] = now }
    }

    suspend fun recordAccessibilityConsent(now: Long = System.currentTimeMillis()) {
        store.edit { it[ACCESSIBILITY_CONSENT_AT] = now }
    }

    suspend fun recordVpnConsent(now: Long = System.currentTimeMillis()) {
        store.edit { it[VPN_CONSENT_AT] = now }
    }

    private companion object {
        val USAGE_CONSENT_AT = longPreferencesKey("usage_access_consent_at")
        val NOTIFICATION_CONSENT_AT = longPreferencesKey("notifications_consent_at")
        val ACCESSIBILITY_CONSENT_AT = longPreferencesKey("accessibility_consent_at")
        val VPN_CONSENT_AT = longPreferencesKey("vpn_consent_at")
    }
}
