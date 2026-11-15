package com.nordev.guardian.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nordev.guardian.domain.block.PolicyCodec
import com.nordev.guardian.domain.block.PolicySnapshot
import com.nordev.guardian.domain.model.Schedule
import com.nordev.guardian.domain.web.WebFilterCodec
import com.nordev.guardian.domain.web.WebFilterPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.policyStore by preferencesDataStore(name = "policy")

// Rules are stored as two versioned text documents. The keys carry the version
// so a future format change can read the old key and migrate instead of guessing.
class PolicyRepository(context: Context) {
    private val store = context.applicationContext.policyStore

    val snapshot: Flow<PolicySnapshot> = store.data.map { prefs ->
        PolicySnapshot(
            limits = PolicyCodec.decodeLimits(prefs[LIMITS_V1].orEmpty()),
            schedules = PolicyCodec.decodeSchedules(prefs[SCHEDULES_V1].orEmpty()),
        )
    }

    val webFilter: Flow<WebFilterPolicy> = store.data.map { prefs ->
        WebFilterCodec.decode(prefs[WEB_V1].orEmpty())
    }

    suspend fun saveLimits(limits: Map<String, Int>) {
        val encoded = PolicyCodec.encodeLimits(limits)
        store.edit { it[LIMITS_V1] = encoded }
    }

    suspend fun saveSchedules(schedules: List<Schedule>) {
        val encoded = PolicyCodec.encodeSchedules(schedules)
        store.edit { it[SCHEDULES_V1] = encoded }
    }

    suspend fun saveWebFilter(policy: WebFilterPolicy) {
        val encoded = WebFilterCodec.encode(policy)
        store.edit { it[WEB_V1] = encoded }
    }

    private companion object {
        val LIMITS_V1 = stringPreferencesKey("limits_v1")
        val SCHEDULES_V1 = stringPreferencesKey("schedules_v1")
        val WEB_V1 = stringPreferencesKey("web_v1")
    }
}
