package com.nordev.guardian.data.web

import android.content.Context
import com.nordev.guardian.domain.web.AdultBlocklist

// Loads the shipped list once per process. The asset is read only; custom
// domains live in the policy store instead.
class BlocklistRepository(private val context: Context) {
    @Volatile
    private var cached: AdultBlocklist? = null

    fun adultList(): AdultBlocklist {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val text = context.assets.open(ASSET).bufferedReader().use { it.readText() }
            return AdultBlocklist.parse(text).also { cached = it }
        }
    }

    private companion object {
        const val ASSET = "adult-domains.txt"
    }
}
