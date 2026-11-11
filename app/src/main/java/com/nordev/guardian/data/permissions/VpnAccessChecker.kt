package com.nordev.guardian.data.permissions

import android.content.Context
import android.net.VpnService

// The system keeps one VPN consent per app. prepare() returns an Intent while
// consent is missing and null once the user has allowed the connection.
class VpnAccessChecker(private val context: Context) {
    fun isGranted(): Boolean = VpnService.prepare(context) == null
}
