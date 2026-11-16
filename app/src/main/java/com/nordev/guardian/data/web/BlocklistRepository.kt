package com.nordev.guardian.data.web

import android.content.Context
import com.nordev.guardian.domain.web.AdultBlocklist

// Loads the shipped list once per process. The VPN service and the Web tab
// share the same parsed set; the asset is read only and custom domains live in
// the policy store instead.
class BlocklistRepository(private val context: Context) {
    fun adultList(): AdultBlocklist {
        cached?.let { return it }
        synchronized(LOCK) {
            cached?.let { return it }
            val text = context.applicationContext.assets
                .open(ASSET)
                .bufferedReader()
                .use { it.readText() }
            return AdultBlocklist.parse(text).also { cached = it }
        }
    }

    private companion object {
        const val ASSET = "adult-domains.txt"
        val LOCK = Any()

        @Volatile
        var cached: AdultBlocklist? = null
    }
}
