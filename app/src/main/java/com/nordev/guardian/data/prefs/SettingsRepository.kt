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

    suspend fun recordUsageConsent(now: Long = System.currentTimeMillis()) {
        store.edit { it[USAGE_CONSENT_AT] = now }
    }

    private companion object {
        val USAGE_CONSENT_AT = longPreferencesKey("usage_access_consent_at")
    }
}
